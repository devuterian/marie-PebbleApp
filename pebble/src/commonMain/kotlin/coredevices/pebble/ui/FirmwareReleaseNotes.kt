package coredevices.pebble.ui

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalDensity
import com.mikepenz.markdown.model.ImageTransformer
import com.mikepenz.markdown.coil3.Coil3ImageTransformerImpl
import com.mikepenz.markdown.m3.Markdown

@Composable
internal fun FirmwareReleaseNotes(notes: String) {
    // The renderer uses GitHub Flavored Markdown; Coil shares the app's GIF decoder.
    Markdown(
        content = notes,
        modifier = Modifier.fillMaxWidth(),
        imageTransformer = PebbleReleaseImageTransformer,
        error = { Text(notes) },
    )
}

internal fun releaseImageSize(size: Size): Size = when (size) {
    Size(144f, 168f), Size(180f, 180f), Size(200f, 228f) -> size * 3f
    else -> size
}

private object PebbleReleaseImageTransformer : ImageTransformer by Coil3ImageTransformerImpl {
    @Composable
    override fun intrinsicSize(painter: Painter): Size =
        releaseImageSize(Coil3ImageTransformerImpl.intrinsicSize(painter))

    @Composable
    override fun transform(link: String): com.mikepenz.markdown.model.ImageData {
        val data = Coil3ImageTransformerImpl.transform(link)
        val original = Coil3ImageTransformerImpl.intrinsicSize(data.painter)
        val enlarged = releaseImageSize(original)
        return if (enlarged != original) {
            data.copy(modifier = Modifier
                .width(with(LocalDensity.current) { enlarged.width.toDp() })
                .aspectRatio(enlarged.width / enlarged.height))
        } else data
    }
}
