package service

import com.soywiz.korev.Key
import com.soywiz.korge.input.keys
import com.soywiz.korge.input.onClick
import com.soywiz.korge.ui.uiText
import com.soywiz.korge.view.Stage
import com.soywiz.korge.view.centerBetween
import com.soywiz.korge.view.container
import com.soywiz.korge.view.position
import com.soywiz.korge.view.roundRect
import com.soywiz.korge.view.text
import com.soywiz.korim.color.Colors
import com.soywiz.korim.font.BitmapFont
import fieldSize
import font
import leftIndent
import model.Block
import model.PositionMap
import topIndent

data class Mutables(
    var isAnimationRunning: Boolean = false,
    var isGameOver: Boolean = false,
    var map: PositionMap,
    var font: BitmapFont,
    var cellSize: Double,
    var fieldSize: Double,
    var leftIndent: Double,
    var topIndent: Double,
    val blocks: Map<Int, Block>,
    var nextFreeBlockId: Int = 0
)

class GameEngine(
    private val mainStage: Stage,
    private val textStyleService: TextStyleService
) {

    fun showGameOver(onRestart: () -> Unit) = mainStage.container {
        val skin = textStyleService.getTextSkin()

        fun restart() {
            this@container.removeFromParent()
            onRestart()
        }

        position(leftIndent, topIndent)
        roundRect(fieldSize, fieldSize, 5.0, fill = Colors["#FFFFFF33"])
        text("Game Over", 60.0, Colors.BLACK, font) {
            centerBetween(0.0, 0.0, fieldSize, fieldSize)
            y -= 60
        }
        uiText("Try again", 120.0, 35.0, skin) {
            centerBetween(0.0, 0.0, fieldSize, fieldSize)
            y += 20
            onClick { restart() }
        }

        this.keys {
            down {
                when (it.key) {
                    Key.ENTER, Key.SPACE -> restart()
                    else -> Unit
                }
            }
        }
    }
}