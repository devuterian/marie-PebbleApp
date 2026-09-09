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
Ordinary Android devices cannot install it over the Play Store APK. The owner
has a rooted phone with Core Patch and reports signature checks are disabled;
that device may support an in-place update. No uninstall or data reset is needed
for the intended owner workflow, and no device installation has been performed.

The default dummy Firebase configuration does not enable account-backed
services. The owner build uses public Firebase app identifiers extracted from
their installed APK in the ignored google-services.json; no user login tokens
or account data were extracted. Fresh login, developer cloud connections and
other credential-backed services have not been verified on a device. Other
optional proprietary build credentials are not included.

## Firmware updates

Starting with app version 1.12.0.1-marie-ver004, Pebble Time 2 (obelix_pvt)
checks the latest published stable release at devuterian/PebbleOAO on GitHub.
It uses the release tag and the exact normal_obelix_pvt_<tag>.pbz asset;
renaming the release title does not affect version checks. Drafts and
prereleases are excluded. Base firmware versions and numeric ver revisions
prevent repeated offers of the installed version or automatic downgrades.
Other watch models keep their existing update sources.

The public GitHub API needs no account token. Failed checks are retried on the
next check; successful results use the existing 15-minute cache, which the
manual update check bypasses.

Brand SVGs: https://github.com/devuterian/iconography
Firmware: https://github.com/devuterian/marie-PebbleOS

Changes were made with AI assistance (GPT-6 Astra).
