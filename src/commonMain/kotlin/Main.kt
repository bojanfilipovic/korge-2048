import com.soywiz.korev.Key
import com.soywiz.korge.Korge
import com.soywiz.korge.input.SwipeDirection
import com.soywiz.korge.input.keys
import com.soywiz.korge.input.onClick
import com.soywiz.korge.input.onSwipe
import com.soywiz.korge.view.Container
import com.soywiz.korge.view.Graphics
import com.soywiz.korge.view.Stage
import com.soywiz.korge.view.Text
import com.soywiz.korge.view.View
import com.soywiz.korge.view.alignRightToLeftOf
import com.soywiz.korge.view.alignRightToRightOf
import com.soywiz.korge.view.alignTopToBottomOf
import com.soywiz.korge.view.alignTopToTopOf
import com.soywiz.korge.view.centerOn
import com.soywiz.korge.view.centerXOn
import com.soywiz.korge.view.graphics
import com.soywiz.korge.view.position
import com.soywiz.korge.view.text
import com.soywiz.korim.color.Colors
import com.soywiz.korim.color.RGBA
import com.soywiz.korim.font.BitmapFont
import com.soywiz.korim.font.readBitmapFont
import com.soywiz.korim.format.readBitmap
import com.soywiz.korim.text.TextAlignment
import com.soywiz.korio.file.std.resourcesVfs
import com.soywiz.korma.geom.Rectangle
import com.soywiz.korma.geom.vector.roundRect
import model.Direction
import model.PositionMap
import service.BlockService
import service.ButtonService
import service.ContainerService
import service.GameEngine
import kotlin.properties.Delegates

// todo bfilipovic: what to do with all these vars
var isAnimationRunning = false
var isGameOver = false
lateinit var map: PositionMap
lateinit var font: BitmapFont
var cellSize by Delegates.notNull<Double>()
var fieldSize by Delegates.notNull<Double>()
var leftIndent by Delegates.notNull<Double>()
var topIndent by Delegates.notNull<Double>()

suspend fun main() = Main().start()

class Main {

    suspend fun start() = Korge(width = 480, height = 640, bgcolor = RGBA(253, 247, 240)) {

        map = PositionMap()
        font = resourcesVfs["clear_sans.fnt"].readBitmapFont()
        cellSize = views.virtualWidth / 5.0     // we need 4 cells + extra space so we divide width by 5
        fieldSize = 50 + 4 * cellSize
        leftIndent = (views.virtualWidth - fieldSize) / 2
        topIndent = 150.0

        val mainStage = this@Korge

        // todo bfilipovic: to game engine initialize deps method + destructure, later Koin or similar
        val gameEngine = GameEngine(mainStage)
        val containerService = ContainerService()
        val buttonService = ButtonService()
        val blockService = BlockService(mainStage, gameEngine)

        // order is important
        val fieldContainer = containerService.fieldContainer(this)

        // setup containers
        graphics {
            position(leftIndent, topIndent)
            drawBoardSquares(10, TILE_COLOR)
        }

        val logoContainer = containerService.logoContainer(this)
        val bestScoreContainer = containerService.bestScoreContainer(this, fieldContainer, logoContainer)
        val scoreContainer = containerService.scoreContainer(this, bestScoreContainer)

        // add text to containers
        text("2048", cellSize * 0.5, Colors.WHITE).centerOn(logoContainer)
        drawScoreBestText("BEST", cellSize * 0.25, RGBA(239, 226, 210), bestScoreContainer)
        drawScoreBestValues("0", cellSize * 0.5, cellSize - 24.0, bestScoreContainer)
        drawScoreBestText("SCORE", cellSize * 0.25, RGBA(239, 226, 210), scoreContainer)
        drawScoreBestValues("0", cellSize * 0.5, cellSize - 24.0, scoreContainer)

        // load images
        val restartImg = resourcesVfs["restart.png"].readBitmap()
        val undoImg = resourcesVfs["undo.png"].readBitmap()

        val restartBlock = buttonService.createButton(this, restartImg)
            .alignTopToBottomOf(bestScoreContainer, 2.5)
            .alignRightToRightOf(fieldContainer)
            .addOnClickListener {
                blockService.restart(this)
            }

        val undoBlock = buttonService.createButton(this, undoImg)
            .alignTopToTopOf(restartBlock)
            .alignRightToLeftOf(restartBlock, 5.0)

        // creating the first initial block
        blockService.generateBlock(this)

        // assign key/swipe logic
        fieldContainer.keys {
            down {
                when (it.key) {
                    Key.LEFT -> blockService.moveBlocksTo(Direction.LEFT)
                    Key.RIGHT -> blockService.moveBlocksTo(Direction.RIGHT)
                    Key.UP -> blockService.moveBlocksTo(Direction.TOP)
                    Key.DOWN -> blockService.moveBlocksTo(Direction.BOTTOM)
                    else -> Unit
                }
            }
        }

        onSwipe(20.0) {
            when (it.direction) {
                SwipeDirection.LEFT -> blockService.moveBlocksTo(Direction.LEFT)
                SwipeDirection.RIGHT -> blockService.moveBlocksTo(Direction.RIGHT)
                SwipeDirection.TOP -> blockService.moveBlocksTo(Direction.TOP)
                SwipeDirection.BOTTOM -> blockService.moveBlocksTo(Direction.BOTTOM)
                else -> Unit
            }
        }
    }

    private fun Graphics.drawBoardSquares(padding: Int, color: RGBA = Colors.AZURE): Unit =
        this.fill(color) {
            for (i in 0..3) {
                for (j in 0..3) {
                    roundRect(
                        padding + (padding + cellSize) * i,
                        padding + (padding + cellSize) * j,
                        cellSize,
                        cellSize,
                        5.0
                    )
                }
            }
        }

    private fun Stage.drawScoreBestText(
        text: String,
        size: Double,
        color: RGBA,
        alignTo: View,
        padding: Double = 5.0
    ): Text = this.text(text, size, color, font) {
        centerXOn(alignTo)
        alignTopToTopOf(alignTo, padding)
    }

    private fun Stage.drawScoreBestValues(
        text: String,
        size: Double,
        height: Double,
        alignTo: View,
        color: RGBA = Colors.WHITE,
        padding: Double = 12.0
    ): Text =
        this.text(text, size, color, font) {
            setTextBounds(Rectangle(0.0, 0.0, alignTo.width, height))
            alignment = TextAlignment.MIDDLE_CENTER
            alignTopToTopOf(alignTo, padding)
            centerXOn(alignTo)
        }

    companion object {
        val TILE_COLOR = Colors["#cec0b2"]
    }
}

fun Container.addOnClickListener(action: () -> Unit): Container = onClick { action() }!!