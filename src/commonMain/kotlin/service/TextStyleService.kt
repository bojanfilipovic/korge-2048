package service

import com.soywiz.korge.html.Html
import com.soywiz.korge.ui.TextFormat
import com.soywiz.korge.ui.TextSkin
import com.soywiz.korim.color.RGBA
import com.soywiz.korim.font.Font

class TextStyleService(private val font: Font) {

    private val textFormat: TextFormat by lazy {
        TextFormat(
            color = TEXT_COLOR_BLACK,
            size = TEXT_DEFAULT_SIZE,
            font = Html.DefaultFontsCatalog.getBitmapFont(font)
        )
    }

    fun getTextSkin() =
        TextSkin(
            normal = textFormat,
            over = textFormat.copy(color = TEXT_COLOR_GRAY),
            down = textFormat.copy(color = TEXT_COLOR_DARK_GRAY)
        )

    companion object {
        private const val TEXT_DEFAULT_SIZE = 40
        private val TEXT_COLOR_BLACK = RGBA(0, 0, 0)
        private val TEXT_COLOR_GRAY = RGBA(90, 90, 90)
        private val TEXT_COLOR_DARK_GRAY = RGBA(120, 120, 120)
    }
}