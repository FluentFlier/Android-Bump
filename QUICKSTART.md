# Android Bump — Get running in 5 minutes

## For you (one time)

### Step 1: Turn on the backend (2 min)

1. Open [Cloudflare Dashboard](https://dash.cloudflare.com) → **My Profile** → **API Tokens** → **Create Token** → use **Edit Cloudflare Workers** template
2. In GitHub: repo **Settings** → **Secrets** → **Actions** → add `CLOUDFLARE_API_TOKEN`
3. Go to **Actions** → **Deploy Backend** → **Run workflow**

When it finishes, copy the worker URL from the job log (looks like `https://android-bump.xxxxx.workers.dev`).

### Step 2: Point the app at your backend (1 min)

Edit `app/build.gradle.kts` line 18:

```kotlin
buildConfigField("String", "DEFAULT_BASE_URL", "\"https://YOUR-WORKER-URL\"")
```

Commit and push (or re-run **Build Android APK** workflow).

### Step 3: Install the app (2 min)

1. **Actions** → **Build Android APK** → latest run → download **android-bump-debug-apk**
2. Transfer to your Android phone and install (allow unknown sources if asked)

---

## Daily use

1. Open **Android Bump** (lands on green bump screen)
2. Hold your phone near their iPhone
3. They tap the NFC banner → **Add Contact**

**iPhone person:** no app, no typing, no QR.

---

## First launch only

Tap **Use my contact** → pick yourself → done. You never see settings again unless you tap **Edit card**.
