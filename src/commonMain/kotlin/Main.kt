
import com.soywiz.korge.Korge
import com.soywiz.korge.input.onClick
import com.soywiz.korge.service.storage.storage
import com.soywiz.korge.view.Container
import com.soywiz.korge.view.Graphics
import com.soywiz.korge.view.alignRightToLeftOf
import com.soywiz.korge.view.alignRightToRightOf
import com.soywiz.korge.view.alignTopToBottomOf
import com.soywiz.korge.view.alignTopToTopOf
import com.soywiz.korge.view.centerOn
import com.soywiz.korge.view.graphics
import com.soywiz.korge.view.position
import com.soywiz.korge.view.text
import com.soywiz.korim.color.Colors
import com.soywiz.korim.color.RGBA
import com.soywiz.korim.font.BitmapFont
import com.soywiz.korim.font.readBitmapFont
import com.soywiz.korim.format.readBitmap
import com.soywiz.korio.async.ObservableProperty
import com.soywiz.korio.file.std.resourcesVfs
import com.soywiz.korma.geom.vector.roundRect
import model.PositionMap
import service.BlockService
import service.ButtonService
import service.ContainerService
import service.EventService
import service.GameEngine
import service.TextService
import service.TextStyleService
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

val scoreProperty = ObservableProperty(0)
val bestProperty = ObservableProperty(0)

suspend fun main() = Main().start()

class Main {

    suspend fun start() = Korge(width = 480, height = 640, bgcolor = RGBA(253, 247, 240)) {

        map = PositionMap()
        font = resourcesVfs["clear_sans.fnt"].readBitmapFont()
        cellSize = views.virtualWidth / 5.0     // we need 4 cells + extra space so we divide width by 5
        fieldSize = 50 + 4 * cellSize
        leftIndent = (views.virtualWidth - fieldSize) / 2
        topIndent = 150.0

        scoreProperty.observe { if (it > bestProperty.value) bestProperty.update(it) }
        bestProperty.observe { storage["best"] = it.toString() }

        val storage = views.storage
        bestProperty.update(storage.getOrNull("best")?.toInt() ?: 0)

        val mainStage = this@Korge

        // todo bfilipovic: to game engine initialize deps method + destructure, later Koin or similar
        val textStyleService = TextStyleService(font)
        val textService = TextService(font, cellSize)
        val gameEngine = GameEngine(mainStage, textStyleService)
        val containerService = ContainerService()
        val buttonService = ButtonService()
        val blockService = BlockService(mainStage, gameEngine)
        val keyEventService = EventService(blockService)

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
        textService.drawSimpleText(this, "BEST", alignTo = bestScoreContainer)
        textService.drawSimpleValue(this, bestProperty, alignTo = bestScoreContainer)

        textService.drawSimpleText(this, "SCORE", alignTo = scoreContainer)
        textService.drawSimpleValue(this, scoreProperty, alignTo = scoreContainer)

        // load images
        val restartImg = resourcesVfs["restart.png"].readBitmap()
        val undoImg = resourcesVfs["undo.png"].readBitmap()

        val restartBlock = buttonService.createButton(this, restartImg)
            .alignTopToBottomOf(bestScoreContainer, 2.5)
            .alignRightToRightOf(fieldContainer)
            .addOnClickListener { blockService.restart(this, scoreProperty) }

        val undoBlock = buttonService.createButton(this, undoImg)
            .alignTopToTopOf(restartBlock)
            .alignRightToLeftOf(restartBlock, 5.0)

        // creating the first initial block
        blockService.generateBlock(this)

        // assign key/swipe logic
        keyEventService.assignKeyEvents(fieldContainer, scoreProperty)
        keyEventService.assignSwipeEvents(this, scoreProperty)
    }

    private fun Graphics.drawBoardSquares(padding: Int, color: RGBA = Colors.AZURE): Unit =
        this.fill(color) {
            repeat(4) { i ->
                repeat(4) { j ->
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

    companion object {
        val TILE_COLOR = Colors["#cec0b2"]
    }
}

fun Container.addOnClickListener(action: () -> Unit): Container = onClick { action() }!!