# Android Bump

Bump your contact to any iPhone. **They install nothing.**

## Your 3 steps (5 min total)

See **[QUICKSTART.md](QUICKSTART.md)** for the full guide.

1. **GitHub Secrets** → add `CLOUDFLARE_API_TOKEN` → run **Deploy Backend** workflow
2. Copy worker URL → add secret `BACKEND_URL` → run **Build Android APK** workflow  
3. Download APK from Actions → install on your Android phone

## Daily use

Open app → hold phone near iPhone → they tap the NFC banner → contact saved.

## First launch only

Tap **Use my contact** → pick yourself → done.
