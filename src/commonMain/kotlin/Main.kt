import com.soywiz.korev.Key
import com.soywiz.korge.Korge
import com.soywiz.korge.html.Html
import com.soywiz.korge.input.SwipeDirection
import com.soywiz.korge.input.keys
import com.soywiz.korge.input.onClick
import com.soywiz.korge.input.onSwipe
import com.soywiz.korge.ui.TextFormat
import com.soywiz.korge.ui.TextSkin
import com.soywiz.korge.ui.uiText
import com.soywiz.korge.view.Container
import com.soywiz.korge.view.Graphics
import com.soywiz.korge.view.Stage
import com.soywiz.korge.view.Text
import com.soywiz.korge.view.View
import com.soywiz.korge.view.alignRightToLeftOf
import com.soywiz.korge.view.alignRightToRightOf
import com.soywiz.korge.view.alignTopToBottomOf
import com.soywiz.korge.view.alignTopToTopOf
import com.soywiz.korge.view.centerBetween
import com.soywiz.korge.view.centerOn
import com.soywiz.korge.view.centerXOn
import com.soywiz.korge.view.container
import com.soywiz.korge.view.graphics
import com.soywiz.korge.view.image
import com.soywiz.korge.view.position
import com.soywiz.korge.view.roundRect
import com.soywiz.korge.view.size
import com.soywiz.korge.view.text
import com.soywiz.korim.bitmap.Bitmap
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
import model.Position
import model.PositionMap
import model.calculateNewMap
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

        // order is important
        val fieldContainer =
            roundRect(fieldSize, fieldSize, 5.0, fill = BACKGROUND_FIELD_COLOR) { position(leftIndent, topIndent) }

        // setup containers
        graphics {
            position(leftIndent, topIndent)
            drawBoardSquares(10, TILE_COLOR)
        }

        val logoContainer =
            roundRect(cellSize, cellSize, 5.0, fill = BACKGROUND_LOGO_COLOR) { position(leftIndent, 30.0) }

        val bestScoreContainer = roundRect(cellSize * 1.5, cellSize * 0.8, 5.0, fill = BACKGROUND_FIELD_COLOR) {
            alignRightToRightOf(fieldContainer)
            alignTopToTopOf(logoContainer)
        }

        val scoreContainer = roundRect(cellSize * 1.5, cellSize * 0.8, 5.0, fill = BACKGROUND_FIELD_COLOR) {
            alignRightToLeftOf(bestScoreContainer, 24.0)
            alignTopToTopOf(bestScoreContainer)
        }

        // add text to containers
        text("2048", cellSize * 0.5, Colors.WHITE).centerOn(logoContainer)
        drawScoreBestText("BEST", cellSize * 0.25, RGBA(239, 226, 210), bestScoreContainer)
        drawScoreBestValues("0", cellSize * 0.5, cellSize - 24.0, bestScoreContainer)
        drawScoreBestText("SCORE", cellSize * 0.25, RGBA(239, 226, 210), scoreContainer)
        drawScoreBestValues("0", cellSize * 0.5, cellSize - 24.0, scoreContainer)

        // load images
        val restartImg = resourcesVfs["restart.png"].readBitmap()
        val undoImg = resourcesVfs["undo.png"].readBitmap()

        val buttonSize = cellSize * 0.3

        val restartBlock = createButton(restartImg, buttonSize)
            .alignTopToBottomOf(bestScoreContainer, 2.5)
            .alignRightToRightOf(fieldContainer)
            .onClick { this.restart() }!!

        val undoBlock = createButton(undoImg, buttonSize)
            .alignTopToTopOf(restartBlock)
            .alignRightToLeftOf(restartBlock, 5.0)

        // creating the first initial block
        generateBlock()

        // assign key/swipe logic
        fieldContainer.keys {
            down {
                println("received key down event: ${it.key}")
                when (it.key) {
                    Key.LEFT -> moveBlocksTo(Direction.LEFT)
                    Key.RIGHT -> moveBlocksTo(Direction.RIGHT)
                    Key.UP -> moveBlocksTo(Direction.TOP)
                    Key.DOWN -> moveBlocksTo(Direction.BOTTOM)
                    else -> Unit
                }
            }
        }

        onSwipe(20.0) {
            println("received swipe event: ${it.direction}")
            when (it.direction) {
                SwipeDirection.LEFT -> moveBlocksTo(Direction.LEFT)
                SwipeDirection.RIGHT -> moveBlocksTo(Direction.RIGHT)
                SwipeDirection.TOP -> moveBlocksTo(Direction.TOP)
                SwipeDirection.BOTTOM -> moveBlocksTo(Direction.BOTTOM)
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

    private fun Stage.createButton(
        image: Bitmap,
        buttonSize: Double,
        color: RGBA = RGBA(185, 174, 160)
    ): Container =
        this.container {
            val background = roundRect(buttonSize, buttonSize, 5.0, fill = color)
            this.image(image) {
                size(buttonSize * 0.8, buttonSize * 0.8)
                centerOn(background)
            }
        }

    private fun Stage.moveBlocksTo(direction: Direction) {
        if (isAnimationRunning) return
        if (!map.hasAvailableMoves()) {
            if (!isGameOver) {
                isGameOver = true
                showGameOver(font, leftIndent, topIndent, fieldSize) {
                    isGameOver = false
                    restart()
                }
            }
        }

        val moves = mutableListOf<Pair<Int, Position>>()
        val merges = mutableListOf<Triple<Int, Int, Position>>()
        val newMap = calculateNewMap(map.copy(), direction, moves, merges)

        if (map != newMap) {
            isAnimationRunning = true
            showAnimation(moves, merges) {
                // when animation ends
                map = newMap
                generateBlock()
                isAnimationRunning = false
            }
        }
    }

    fun Container.restart() {
        map = PositionMap()
        blocks.values.forEach { it.removeFromParent() }
        blocks.clear()
        generateBlock()
    }

    fun Container.showGameOver(
        font: BitmapFont,
        leftIndent: Double,
        topIndent: Double,
        fieldSize: Double,
        onRestart: () -> Unit
    ) = container {
        val format = TextFormat(
            color = RGBA(0, 0, 0),
            size = 40,
            font = Html.DefaultFontsCatalog.getBitmapFont(font)
        )
        val skin = TextSkin(
            normal = format,
            over = format.copy(color = RGBA(90, 90, 90)),
            down = format.copy(color = RGBA(120, 120, 120))
        )

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

    companion object {
        val BACKGROUND_FIELD_COLOR = Colors["#b9aea0"]
        val BACKGROUND_LOGO_COLOR = Colors["#edc403"]
        val TILE_COLOR = Colors["#cec0b2"]
    }
}