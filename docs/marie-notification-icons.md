# Marie notification icons

Pairs with marie-PebbleOS v4.37.0-ver003-cheesecake or later. Timeline IDs
141–154 and their 25/50/80 px resources are defined by that firmware.
Nagram XF and Aliucord use the existing Telegram and Discord resources;
Reddit uses the existing Reddit resource (138).

The package mappings and default colors are in NotificationProperties.kt.
User icon and background overrides retain precedence. Text contrast is chosen
from the final background after conversion to the Pebble palette.

## Development build

Use JDK 21 and the Android SDK. Set sdk.dir in local.properties and copy
androidApp/src/google-services-dummy.json to androidApp/src/google-services.json.
Run ./gradlew :androidApp:assembleDebug. The result is a development APK signed
with the local Android debug key, not the official Pebble signing key.
It cannot replace the Play Store APK in place. Do not uninstall an existing
app without first arranging preservation of its data and settings.

The dummy Firebase configuration does not enable official account-backed
services. Core Bluetooth features are available in open-source builds; account
login, developer cloud connections and other credential-backed services need
valid build configuration. This build is not a verified replacement for every
feature of the official app. No device installation has been performed.

Brand SVGs: https://github.com/devuterian/iconography
Firmware: https://github.com/devuterian/marie-PebbleOS

Changes were made with AI assistance (GPT-6 Astra).
