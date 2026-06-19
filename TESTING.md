# Test Android Bump

Use this checklist before relying on it at an event or demo.

## Before you start

- [ ] Android phone with **NFC + HCE** (most Pixel/Samsung from 2018+)
- [ ] iPhone with **iOS 14+** (15+ recommended)
- [ ] Latest APK: https://github.com/FluentFlier/Android-Bump/releases/download/latest-apk/app-debug.apk
- [ ] Site live: https://fluentflier.github.io/Android-Bump/

## Setup test (2 min)

1. Install APK on Android
2. Open app → **Use my contact** → pick yourself
3. Confirm green **Ready to bump** screen with your name
4. NFC chip at top should say **NFC ready**

**Pass:** Setup completes in one tap, no errors.

## Bump test (the real one)

1. Android: stay on green bump screen (screen stays awake)
2. iPhone: unlocked, held **top-to-top** with Android
3. Wait 1–3 seconds for NFC banner on iPhone lock screen or top of screen
4. iPhone user **taps the banner once**
5. Safari opens → contact saves (may show Add to Contacts)

**Pass:** iPhone gets your name + phone without installing anything.

## What success looks like on Android

- Phone **vibrates** when iPhone reads the tag
- White **Contact sent!** banner appears at bottom
- Banner says ask them to tap on their iPhone

## If it fails

| Symptom | Try |
|---------|-----|
| **Embedded tag vs Android Bump popup** | Tap red banner in app → set Android Bump as default. Always pick **Android Bump**, never Embedded tag. |
| No iPhone banner | Top-to-top only; move slowly; retry 2–3 times |
| Banner but broken link | Confirm GitHub Pages is up (open site in Safari) |
| No vibration on Android | Keep app in foreground; NFC chip must say ready |
| NFC off | Tap red chip → enable NFC in Settings |
| Works once, not again | Re-open bump screen; keep app in foreground |

## Web setup test (optional)

1. On Android browser: https://fluentflier.github.io/Android-Bump/
2. Enter name + phone → Continue
3. Install app if needed → **Open in Android Bump**
4. App should land on bump screen with same contact

## Devices to try

- [ ] Pixel + iPhone 15/16
- [ ] Samsung + iPhone (top-to-top, then back-to-back if needed)
- [ ] iPhone on lock screen

## Report issues

Note: Android model, iPhone model, iOS version, and what step failed.
