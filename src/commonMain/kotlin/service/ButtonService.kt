package service

import cellSize
import com.soywiz.korge.view.Container
import com.soywiz.korge.view.centerOn
import com.soywiz.korge.view.container
import com.soywiz.korge.view.image
import com.soywiz.korge.view.roundRect
import com.soywiz.korge.view.size
import com.soywiz.korim.bitmap.Bitmap
import com.soywiz.korim.color.RGBA

class ButtonService {

    fun createButton(
        container: Container,
        image: Bitmap,
        color: RGBA = RGBA(185, 174, 160)
    ): Container =
        container.container {
            val background = roundRect(BUTTON_SIZE, BUTTON_SIZE, 5.0, fill = color)
            this.image(image) {
                size(BUTTON_SIZE * 0.8, BUTTON_SIZE * 0.8)
                centerOn(background)
            }
        }

    companion object {
        val BUTTON_SIZE: Double = cellSize * 0.3
    }
}