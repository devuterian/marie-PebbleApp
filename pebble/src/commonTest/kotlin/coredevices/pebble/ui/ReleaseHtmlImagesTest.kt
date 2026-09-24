package coredevices.pebble.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.parser.MarkdownParser

class ReleaseHtmlImagesTest {
    @Test fun githubTableImagesBecomeRenderableImageNodes() {
        val rows = (1..33).chunked(2).joinToString("\n") { pages ->
            "| " + pages.joinToString(" | ") { "<img src=\"https://raw.githubusercontent.com/devuterian/PebbleOAO/v4.37.0-ver007-gelato/docs/_static/images/symbols/gelato/page-$it.png\" width=\"300\" alt=\"특수문자 $it/33\">" } + " |"
        }
        val normalized = normalizeReleaseImages("| 화면 | 화면 |\n| --- | --- |\n$rows")
        val tree = MarkdownParser(GFMFlavourDescriptor()).buildMarkdownTreeFromString(normalized)
        fun count(node: ASTNode): Int = (if (node.type == MarkdownElementTypes.IMAGE) 1 else 0) + node.children.sumOf { count(it) }
        assertEquals(33, count(tree))
        assertTrue(normalized.contains("| 화면 | 화면 |"))
    }
    @Test fun preservesCodeAndExistingMarkdown() {
        val source = "**굵게** ![gif](https://example.com/a.gif)\n\n`<img src=\"https://example.com/a.png\">`\n\n```html\n<img src=\"https://example.com/b.png\">\n```"
        assertEquals(source, normalizeReleaseImages(source))
    }
    @Test fun handlesHtmlAttributesAndQueryEntities() {
        assertEquals("![a](https://example.com/a.gif?x=1&y=2)", normalizeReleaseImages("<IMG alt='a' width='300' src='https://example.com/a.gif?x=1&amp;y=2' />"))
        assertEquals("![](https://example.com/a.png)", normalizeReleaseImages("<img src=https://example.com/a.png>"))
    }
    @Test fun leavesUnsupportedSourcesAlone() {
        val input = "<img src=\"javascript:bad()\">"
        assertEquals(input, normalizeReleaseImages(input))
    }
}
