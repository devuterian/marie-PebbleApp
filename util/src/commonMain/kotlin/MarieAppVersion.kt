package coredevices.coreapp.util

fun marieAppRevision(name: String): Int? = Regex("(?:^|-)marie-ver([0-9]+)(?:\\.apk)?$")
    .find(name)?.groupValues?.get(1)?.toIntOrNull()

fun validAppAsset(url: String, digest: String): Boolean =
    url.startsWith("https://github.com/devuterian/PebbleOAO/releases/download/") &&
        Regex("sha256:[0-9a-f]{64}").matches(digest)
