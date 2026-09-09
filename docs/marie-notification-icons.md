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

## Korean watch text

App version 1.12.0.1-marie-ver005 localizes phone-generated watch text when the
phone/app locale is Korean: sunrise/sunset pins, weather conditions and the
Open-Meteo forecast template, calendar headings and RSVP actions, all-day labels,
and notification actions/results. Event titles, names, notification content and
internal action identifiers remain unchanged. Other locales retain English.
Unrecognized server forecast prose is preserved rather than partially rewritten.
Existing timeline pins receive the new text on the next weather/calendar sync.

Validation: 326 Android host tests passed, including a Korean calendar pin test
that checks action identifiers and preservation of event text, plus actual
server weather-template translation cases. Android debug assembly passed.

## Cloud dictation repair

App version 1.12.0.1-marie-ver006 restores the public Wispr token-service and
Kirinki fallback URLs used by the official 1.11.0.3 APK. The existing
wisprAuthUrl/kirinkiUrl Gradle properties can override these defaults.
These are service addresses, not API credentials. Requests still authenticate
with the signed-in user's Firebase account. No tokens were copied into the build.

Previous Marie builds omitted both URLs, making the default RemoteOnly mode
unavailable even when Spoken Language was already set to Korean (ko).

Validated on the owner's CPH2653 using KoreanWisprTest against the live Wispr
service with the existing login. A synthetic Korean sentence, “내일 오후 세 시에
카페에서 만나자”, was supplied as 16 kHz mono PCM16; expected Korean words were
recognized. The device test passed in 4.252 seconds. This verifies authentication
and REST recognition; the watch microphone/Bluetooth capture path needs a live
watch dictation attempt. The temporary test APK and sample were removed afterward.
The instrumentation test skips unless koreanPcmFixture names a synthetic PCM
file in the target app's cache; it never records audio or sends a reply.

## Dictation wire-format follow-up

In ver006, live phone logs showed Wispr success for several watch recordings,
while decoded ver004 firmware logs contained “Unrecognized transcription format
received”. A later short recording produced Success with zero words. The exact
contents of the other rejected transcripts were not captured.

App ver007 normalizes whitespace/control separators before creating voice words
and treats an empty result as a recognition failure instead of transmitting an
invalid zero-word sentence. The firmware explicitly rejects zero-length words
and control bytes other than the punctuation backspace marker. Recognition text
is not logged; a diagnostic records only whether normalization changed a result.

Validated 231 libpebble3 host tests, including Korean UTF-8 wire payloads,
multiline/tab/NUL separators, empty results, and punctuation. Built and installed
ver007 on CPH2653. A successful watch dictation retry is still needed to confirm
that this resolves the user's full symptom.

## Opt-in firmware previews

App ver010 adds a per-watch prerelease option to the firmware source dialog.
It defaults off. Enabling requires the tester warning's confirmation; dismissing
or cancelling leaves it off. Disabling takes effect immediately. Official OTA
continues to use its existing stable source regardless of this preference.
The custom preview channel checks published releases, skips drafts, malformed
publication dates and missing/ambiguous device bundles, and selects the latest
published eligible update. Version comparisons still prohibit downgrades.
The update cache includes the channel, and toggling forces a fresh check.

Validated Android debug assembly and 13 GitHub firmware host tests. No phone
installation or physical watch OTA session was performed for this build.
