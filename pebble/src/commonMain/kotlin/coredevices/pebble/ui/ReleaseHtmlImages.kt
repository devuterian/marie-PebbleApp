package coredevices.pebble.ui

import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.parser.MarkdownParser

// The native renderer supports GFM images, but not GitHub's inline HTML img tags.
// Only replace parsed HTML tokens; examples inside code spans/fences stay untouched.
internal fun normalizeReleaseImages(content: String): String {
    val root = MarkdownParser(GFMFlavourDescriptor()).buildMarkdownTreeFromString(content)
    val replacements = mutableListOf<Triple<Int, Int, String>>()
    fun visit(node: ASTNode) {
        if (node.type in setOf(MarkdownElementTypes.CODE_SPAN, MarkdownElementTypes.CODE_FENCE, MarkdownElementTypes.CODE_BLOCK)) return
        if (node.type == MarkdownTokenTypes.HTML_TAG || node.type == MarkdownElementTypes.HTML_BLOCK) {
            val html = content.substring(node.startOffset, node.endOffset)
            val converted = imageTag.replace(html) { match ->
                val attributes = imageAttribute.findAll(match.value).associate {
                    it.groupValues[1].lowercase() to it.groupValues.drop(2).firstOrNull { value -> value.isNotEmpty() }.orEmpty()
                }
                val src = attributes["src"]?.replace("&amp;", "&")
                if (src == null || !(src.startsWith("https://") || src.startsWith("http://"))) match.value
                else {
                    val alt = attributes["alt"].orEmpty().replace("\\", "\\\\")
                        .replace("[", "\\[").replace("]", "\\]").replace("|", "&#124;")
                    "![$alt](${src.replace("<", "%3C").replace(">", "%3E").replace("(", "%28").replace(")", "%29").replace(" ", "%20")})"
                }
            }
            if (converted != html) replacements += Triple(node.startOffset, node.endOffset, converted)
        } else node.children.forEach(::visit)
    }
    visit(root)
    val result = StringBuilder(content)
    replacements.sortedByDescending { it.first }.forEach { (start, end, replacement) -> result.replace(start, end, replacement) }
    return result.toString()
}

private val imageTag = Regex("""<img\b(?:[^>"']|"[^"]*"|'[^']*')*>""", RegexOption.IGNORE_CASE)
private val imageAttribute = Regex("""\b(src|alt)\s*=\s*(?:"([^"]*)"|'([^']*)'|([^\s>]+))""", RegexOption.IGNORE_CASE)
