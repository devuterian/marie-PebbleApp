package coredevices.pebble.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.unit.dp
import com.mikepenz.markdown.compose.components.markdownComponents
import com.mikepenz.markdown.compose.elements.MarkdownTable
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.flavours.gfm.GFMElementTypes
import org.intellij.markdown.flavours.gfm.GFMTokenTypes
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
        content = remember(notes) { normalizeReleaseImages(notes) },
        modifier = Modifier.fillMaxWidth(),
        imageTransformer = PebbleReleaseImageTransformer,
        components = markdownComponents(table = { model ->
            if (containsReleaseImage(model.node)) {
                ReleaseImageTable(model.content, model.node)
            } else {
                MarkdownTable(model.content, model.node, model.typography.text)
            }
        }),
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

private fun containsReleaseImage(node: ASTNode): Boolean =
    node.type == MarkdownElementTypes.IMAGE || node.children.any(::containsReleaseImage)

// Table inline-image placeholders can measure to zero before Coil loads the image.
// Give each cell its own normal Markdown layout so images can establish their height.
@Composable
private fun ReleaseImageTable(content: String, node: ASTNode) {
    Column(Modifier.fillMaxWidth()) {
        node.children.filter { it.type == GFMElementTypes.HEADER || it.type == GFMElementTypes.ROW }.forEach { row ->
            Row(Modifier.fillMaxWidth()) {
                row.children.filter { it.type == GFMTokenTypes.CELL }.forEach { cell ->
                    Markdown(
                        content = content.substring(cell.startOffset, cell.endOffset).trim(),
                        modifier = Modifier.weight(1f).padding(4.dp),
                        imageTransformer = PebbleReleaseImageTransformer,
                    )
                }
            }
            HorizontalDivider()
        }
    }
}
