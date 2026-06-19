# Android Bump — get it working

Two pieces work together:

| Piece | What it is | Who uses it |
|-------|------------|-------------|
| **Web site** | Setup + iPhone landing page | You (setup), iPhone friend (save contact) |
| **Android app** | NFC broadcaster | You (every bump) |

**You cannot bump from a web app alone.** Only a native Android app can emulate an NFC tag. The website handles setup and the iPhone save screen.

---

## Step 1 — Enable the website (repo owner, once)

1. Open [github.com/FluentFlier/Android-Bump/settings/pages](https://github.com/FluentFlier/Android-Bump/settings/pages)
2. **Build and deployment → Source:** GitHub Actions
3. Push to `master` (or run **Deploy landing page** workflow)

Live site: **https://fluentflier.github.io/Android-Bump/**

---

## Step 2 — Build the Android app

1. GitHub → **Actions** → **Build Android APK** → **Run workflow**
2. When done, open the run → download **android-bump-debug-apk**
3. Install on your Android phone (enable “Install unknown apps” if prompted)

Or download from [Releases](https://github.com/FluentFlier/Android-Bump/releases/latest) once published.

---

## Step 3 — Set up your contact

**Option A — Web setup (easiest)**

1. On your phone, open **https://fluentflier.github.io/Android-Bump/**
2. Enter name + phone → **Continue**
3. Tap **Open in Android Bump** (install app first if needed)
4. App opens on the green bump screen — done

**Option B — In the app**

1. Open Android Bump
2. Tap **Use my contact** → pick yourself
3. Green bump screen appears — done

---

## Step 4 — Bump an iPhone

1. Open Android Bump (green bump screen, screen stays awake)
2. Hold phone **top-to-top** with iPhone (like NameDrop)
3. iPhone shows NFC banner → they **tap once**
4. Contact saves → Done

**iPhone needs:** iOS 14+, NFC on, no app install.

---

## Troubleshooting

| Problem | Fix |
|---------|-----|
| No NFC banner on iPhone | Try top-to-top; unlock iPhone; move slowly |
| Website 404 | Enable GitHub Pages (Step 1) |
| App won’t install | Use debug APK from Actions; allow unknown sources |
| “Contact too long” | Shorten name or use fewer fields |

---

## How it fits together

```
YOU (Android)                         IPHONE FRIEND
─────────────                         ─────────────
Web or app: set name + phone
App builds link, broadcasts via NFC
        ─── bump ───►                 NFC banner appears
                                      Tap → website opens
                                      Add to Contacts
```

No backend. No accounts. Contact data is encoded in the HTTPS link.
