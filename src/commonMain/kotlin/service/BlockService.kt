package service

import animateScale
import cellSize
import com.soywiz.klock.seconds
import com.soywiz.korge.animate.animateSequence
import com.soywiz.korge.view.Container
import com.soywiz.korge.view.Stage
import com.soywiz.korge.view.addTo
import com.soywiz.korge.view.position
import com.soywiz.korge.view.tween.moveTo
import com.soywiz.korio.async.launchImmediately
import com.soywiz.korma.interpolation.Easing
import font
import isAnimationRunning
import isGameOver
import kotlinx.coroutines.GlobalScope
import leftIndent
import map
import model.Block
import model.Direction
import model.Number
import model.Position
import model.PositionMap
import model.calculateNewMap
import topIndent
import kotlin.random.Random

val blocks = mutableMapOf<Int, Block>()
var nextFreeBlockId = 0

class BlockService(
    private val mainStage: Stage,
    private val gameEngine: GameEngine
) {

    fun generateBlock(container: Container) {
        val position = map.getRandomFreePosition() ?: return
        val number = if (Random.nextDouble() > 0.9) Number.ZERO else Number.ONE
        val newId = container.createNewBlock(number, position)
        map[position.x, position.y] = newId
    }

    fun numberFor(blockId: Int): Number = blocks[blockId]!!.number

    fun moveBlocksTo(direction: Direction) {
        if (isAnimationRunning) return
        if (!map.hasAvailableMoves()) {
            if (!isGameOver) {
                isGameOver = true
                gameEngine.showGameOver {
                    isGameOver = false
                    restart(mainStage)
                }
            }
        }

        val moves = mutableListOf<Pair<Int, Position>>()
        val merges = mutableListOf<Triple<Int, Int, Position>>()
        val newMap = calculateNewMap(map.copy(), direction, moves, merges, this)

        if (map != newMap) {
            isAnimationRunning = true
            mainStage.showAnimation(moves, merges) {
                // when animation ends
                map = newMap
                generateBlock(mainStage)
                isAnimationRunning = false
            }
        }
    }

    fun restart(container: Container) {
        map = PositionMap()
        blocks.values.forEach { it.removeFromParent() }
        blocks.clear()
        generateBlock(container)
    }

    private fun Container.createNewBlock(number: Number, position: Position): Int =
        createNewBlockWithId(nextFreeBlockId++, number, position)

    private fun Container.createNewBlockWithId(id: Int, number: Number, position: Position): Int =
        id.also { blocks[id] = block(number).position(columnX(position.x), rowY(position.y)) }

    private fun Container.block(number: Number) =
        Block(number, cellSize, font).addTo(this)

    private fun columnX(number: Int) =
        leftIndent + INDENT_BETWEEN_BLOCKS + (cellSize + INDENT_BETWEEN_BLOCKS) * number

    private fun rowY(number: Int) =
        topIndent + INDENT_BETWEEN_BLOCKS + (cellSize + INDENT_BETWEEN_BLOCKS) * number

    private fun Stage.showAnimation(
        moves: List<Pair<Int, Position>>,
        merges: List<Triple<Int, Int, Position>>,
        onEnd: () -> Unit
    ) = GlobalScope.launchImmediately {
        animateSequence {
            parallel {
                moves.forEach { (id, position) ->
                    blocks[id]!!.moveTo(columnX(position.x), rowY(position.y), 0.15.seconds, Easing.LINEAR)
                }
                merges.forEach { (id1, id2, position) ->
                    sequence {
                        parallel {
                            GlobalScope.launchImmediately {
                                blocks[id1]?.animateMove(position)
                                blocks[id2]?.animateMove(position)
                            }
                        }
                        block {
                            val nextNumber = numberFor(id1).next()
                            deleteBlock(id1)
                            deleteBlock(id2)
                            stage.createNewBlockWithId(id1, nextNumber, position)
                        }
                        sequenceLazy {
                            animateScale(blocks[id1]!!)
                        }
                    }
                }
            }
            block {
                onEnd()
            }
        }
    }

    private suspend fun Block?.animateMove(position: Position) =
        this!!.moveTo(columnX(position.x), rowY(position.y), 0.15.seconds, Easing.LINEAR)

    private fun deleteBlock(blockId: Int) = blocks.remove(blockId)!!.removeFromParent()

    companion object {
        const val INDENT_BETWEEN_BLOCKS = 10
    }
}