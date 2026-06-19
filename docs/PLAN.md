# Android Bump — Magic UX Plan

**Goal:** iPhone person does nothing except bump + one tap on the NFC banner. No install. Feels like NameDrop.

**Hard limit (Apple):** iOS will always show an NFC notification the user must tap once. We cannot skip that without an iPhone app. Everything else is on us.

---

## iPhone experience (target)

| Step | NameDrop | Android Bump (target) |
|------|----------|------------------------|
| 1 | Hold phones together | Same |
| 2 | Full-screen card appears | NFC banner appears on lock screen |
| 3 | Tap Share / Save | Tap banner (one tap) |
| 4 | Contact saved | iOS Contacts sheet → Done |

**Success metric:** iPhone user never opens App Store, never types anything, never scans QR.

---

## Architecture (unchanged, tightened)

```
Android App (on-device)        Static landing page            iPhone
───────────                    ───────────────────            ──────
Build vCard locally       →    GitHub Pages (one-time deploy)
Encode in HTTPS URL       →    URL#base64 → .vcf download  →  Tap banner → Save
HCE broadcasts URL
```

---

## Phase 1 — Ship today (this session)

### Android
- [x] **2-field onboarding** — name + phone only; backend URL hidden in BuildConfig
- [x] **Import from Contacts** — one tap to fill from Android contact picker
- [x] **Magic bump screen** — full-screen card, pulse animation, keep screen awake
- [x] **Haptic on NFC read** — phone buzzes when iPhone reads the tag
- [x] **HCE hardening** — broader APDU SELECT/READ support for iPhone readers
- [x] **Auto-resume bump mode** — reopen app → straight to bump screen if profile exists

### Backend
- [x] **Shorter profile IDs** — smaller NFC payload, faster reads
- [x] **iOS-optimized vCard response** — headers tuned for Contacts import
- [x] **Health check** — `GET /health` for app connectivity test

### Ops
- [x] Deploy script + GitHub Actions APK build
- [ ] Operator runs `wrangler login` + `./scripts/deploy-backend.sh` once

---

## Phase 2 — After first real-world test

- Bidirectional “share yours back” web form at `/p/{id}` (still no iPhone app)
- Profile photo in vCard
- Play Store listing
- Analytics: bump success count

---

## Phase 3 — Platform moat (optional)

- iOS App Clip (only if users demand true two-way in one gesture)
- Android ↔ Android via Google Tap to Share when it ships

---

## Test matrix (before you rely on it)

1. Pixel + iPhone 15 — top-to-top, unlocked
2. Samsung + iPhone — try top-to-top, then back-to-back
3. iPhone on lock screen — NFC banner should still appear (iOS 14+)
4. Airplane mode off, Safari not required to be open

---

## Operator checklist (5 min)

```bash
# 1. Deploy backend
cd backend && npx wrangler login
# Edit wrangler.toml: set PUBLIC_BASE_URL
../scripts/deploy-backend.sh

# 2. Point app at backend (one line)
# app/build.gradle.kts → DEFAULT_BASE_URL = "https://your-worker.workers.dev"

# 3. Build & install APK (Android Studio or GitHub Actions artifact)

# 4. On Android: open app → import or enter contact → Bump screen
# 5. On iPhone: hold near → tap NFC banner → Add Contact
```

---

## GSTACK REVIEW REPORT

| Review | Status | Findings |
|--------|--------|----------|
| CEO | Scope locked | One-way Android→iPhone, zero iPhone install |
| Eng | In progress | HCE + Worker + minimal onboarding |
| Design | Target | NameDrop-like bump screen, not settings app |
| Codex | Prior art | HTTPS NDEF only; vCard direct on /c/{id} |

**VERDICT:** Phase 1 implementation in flight — usable after backend deploy + APK install.
