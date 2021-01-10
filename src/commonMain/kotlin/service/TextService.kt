package service

import com.soywiz.korge.view.Stage
import com.soywiz.korge.view.View
import com.soywiz.korge.view.alignTopToTopOf
import com.soywiz.korge.view.centerXOn
import com.soywiz.korge.view.text
import com.soywiz.korim.color.Colors
import com.soywiz.korim.color.RGBA
import com.soywiz.korim.font.BitmapFont
import com.soywiz.korim.text.TextAlignment
import com.soywiz.korio.async.ObservableProperty
import com.soywiz.korma.geom.Rectangle

class TextService(private val font: BitmapFont, private val cellSize: Double) {

    fun drawSimpleText(
        stage: Stage,
        text: String,
        size: Double = cellSize * 0.25,
        alignTo: View,
        padding: Double = DEFAULT_PADDING,
        color: RGBA? = DEFAULT_TEXT_COLOR
    ) {
        stage.text(text, size, color!!, font) {
            centerXOn(alignTo)
            alignTopToTopOf(alignTo, padding)
        }
    }

    fun <T> drawSimpleValue(
        stage: Stage,
        property: ObservableProperty<T>,
        size: Double = cellSize * 0.5,
        height: Double = cellSize - 24.0,
        alignTo: View,
        padding: Double = DEFAULT_PADDING,
        color: RGBA? = DEFAULT_TEXT_VALUE_COLOR
    ) {
        stage.text(property.value.toString(), size, color!!, font) {
            setTextBounds(Rectangle(0.0, 0.0, alignTo.width, height))
            alignment = TextAlignment.MIDDLE_CENTER
            alignTopToTopOf(alignTo, padding)
            centerXOn(alignTo)
            property.observe { text = it.toString() }
        }
    }

    companion object Ops {
        private val DEFAULT_TEXT_COLOR = RGBA(239, 226, 210)
        private val DEFAULT_TEXT_VALUE_COLOR = Colors.WHITE
        private const val DEFAULT_PADDING = 5.0
    }
}