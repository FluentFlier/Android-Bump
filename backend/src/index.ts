export interface Env {
  PROFILES: KVNamespace;
  PUBLIC_BASE_URL?: string;
}

interface StoredProfile {
  fullName: string;
  phone: string;
  email: string;
  company?: string;
  website?: string;
  editTokenHash: string;
}

interface ProfileInput {
  fullName: string;
  phone: string;
  email: string;
  company?: string;
  website?: string;
}

function json(data: unknown, status = 200, extraHeaders: Record<string, string> = {}): Response {
  return new Response(JSON.stringify(data), {
    status,
    headers: {
      "content-type": "application/json; charset=utf-8",
      ...extraHeaders,
    },
  });
}

function text(body: string, status = 200, extraHeaders: Record<string, string> = {}): Response {
  return new Response(body, {
    status,
    headers: extraHeaders,
  });
}

/** Short URL-safe IDs — smaller NFC NDEF payload, faster iPhone reads. */
function randomId(bytes = 6): string {
  const buf = crypto.getRandomValues(new Uint8Array(bytes));
  return btoa(String.fromCharCode(...buf))
    .replace(/\+/g, "-")
    .replace(/\//g, "_")
    .replace(/=+$/, "");
}

async function hashToken(token: string): Promise<string> {
  const data = new TextEncoder().encode(token);
  const digest = await crypto.subtle.digest("SHA-256", data);
  return [...new Uint8Array(digest)].map((b) => b.toString(16).padStart(2, "0")).join("");
}

function escapeVcard(value: string): string {
  return value.replace(/\\/g, "\\\\").replace(/;/g, "\\;").replace(/\n/g, "\\n");
}

function splitName(fullName: string): { last: string; first: string } {
  const parts = fullName.trim().split(/\s+/);
  if (parts.length === 1) return { last: parts[0], first: "" };
  return { last: parts[parts.length - 1], first: parts.slice(0, -1).join(" ") };
}

function toVcard(profile: StoredProfile): string {
  const { last, first } = splitName(profile.fullName);
  const lines = [
    "BEGIN:VCARD",
    "VERSION:3.0",
    "PRODID:-//Android Bump//EN",
    `N:${escapeVcard(last)};${escapeVcard(first)};;;`,
    `FN:${escapeVcard(profile.fullName)}`,
  ];
  if (profile.company) lines.push(`ORG:${escapeVcard(profile.company)}`);
  if (profile.phone) lines.push(`TEL;TYPE=CELL,VOICE:${escapeVcard(profile.phone)}`);
  if (profile.email) lines.push(`EMAIL;TYPE=INTERNET:${escapeVcard(profile.email)}`);
  if (profile.website) lines.push(`URL:${escapeVcard(profile.website)}`);
  lines.push("END:VCARD");
  return lines.join("\r\n") + "\r\n";
}

function vcardHeaders(filename: string): Record<string, string> {
  return {
    "content-type": "text/vcard; charset=utf-8",
    "content-disposition": `attachment; filename="${filename}.vcf"`,
    "cache-control": "public, max-age=60",
    "x-robots-tag": "noindex, nofollow",
    "access-control-allow-origin": "*",
  };
}

function validateInput(input: ProfileInput): string | null {
  if (!input.fullName?.trim()) return "fullName required";
  if (!input.phone?.trim()) return "phone required";
  return null;
}

async function readProfile(env: Env, id: string): Promise<StoredProfile | null> {
  const raw = await env.PROFILES.get(`profile:${id}`);
  return raw ? (JSON.parse(raw) as StoredProfile) : null;
}

export default {
  async fetch(request: Request, env: Env): Promise<Response> {
    const url = new URL(request.url);
    const base = (env.PUBLIC_BASE_URL || url.origin).replace(/\/$/, "");

    if (url.pathname === "/health") {
      return json({ ok: true, service: "android-bump" });
    }

    if (url.pathname.startsWith("/c/")) {
      const id = url.pathname.split("/")[2];
      const profile = await readProfile(env, id);
      if (!profile) return text("Not found", 404);
      const filename = profile.fullName.replace(/[^a-zA-Z0-9_-]+/g, "-") || "contact";
      return text(toVcard(profile), 200, vcardHeaders(filename));
    }

    if (url.pathname.startsWith("/p/")) {
      const id = url.pathname.split("/")[2];
      const profile = await readProfile(env, id);
      if (!profile) return text("Not found", 404);
      const html = `<!doctype html>
<html lang="en"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<meta name="robots" content="noindex,nofollow"><meta http-equiv="refresh" content="0;url=${base}/c/${id}">
<title>${profile.fullName}</title></head><body>
<p>Opening contact for ${profile.fullName}…</p>
<p><a href="${base}/c/${id}">Add to Contacts</a></p></body></html>`;
      return text(html, 200, { "content-type": "text/html; charset=utf-8", "x-robots-tag": "noindex, nofollow" });
    }

    if (url.pathname === "/api/v1/profiles" && request.method === "POST") {
      const input = (await request.json()) as ProfileInput;
      const error = validateInput(input);
      if (error) return json({ error }, 400);

      const id = randomId(6);
      const editToken = randomId(16);
      const stored: StoredProfile = {
        fullName: input.fullName.trim(),
        phone: input.phone.trim(),
        email: (input.email || "").trim(),
        company: (input.company || "").trim(),
        website: (input.website || "").trim(),
        editTokenHash: await hashToken(editToken),
      };
      await env.PROFILES.put(`profile:${id}`, JSON.stringify(stored));
      return json({ id, editToken, shareUrl: `${base}/c/${id}` }, 201);
    }

    const updateMatch = url.pathname.match(/^\/api\/v1\/profiles\/([^/]+)$/);
    if (updateMatch && request.method === "PUT") {
      const id = updateMatch[1];
      const auth = request.headers.get("authorization") || "";
      const token = auth.startsWith("Bearer ") ? auth.slice(7) : "";
      const existing = await readProfile(env, id);
      if (!existing) return json({ error: "not found" }, 404);
      if (!token || (await hashToken(token)) !== existing.editTokenHash) {
        return json({ error: "unauthorized" }, 401);
      }

      const input = (await request.json()) as ProfileInput;
      const error = validateInput(input);
      if (error) return json({ error }, 400);

      const updated: StoredProfile = {
        ...existing,
        fullName: input.fullName.trim(),
        phone: input.phone.trim(),
        email: (input.email || "").trim(),
        company: (input.company || "").trim(),
        website: (input.website || "").trim(),
      };
      await env.PROFILES.put(`profile:${id}`, JSON.stringify(updated));
      return json({ id, editToken: token, shareUrl: `${base}/c/${id}` });
    }

    return text("Android Bump", 200, { "content-type": "text/plain; charset=utf-8" });
  },
};
