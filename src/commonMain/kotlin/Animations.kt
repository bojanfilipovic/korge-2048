import com.soywiz.klock.seconds
import com.soywiz.korge.animate.Animator
import com.soywiz.korge.animate.animateSequence
import com.soywiz.korge.view.Stage
import com.soywiz.korge.view.tween.moveTo
import com.soywiz.korio.async.launchImmediately
import com.soywiz.korma.interpolation.Easing
import kotlinx.coroutines.GlobalScope
import model.Block
import com.soywiz.korge.tween.*
import model.Position

fun Stage.showAnimation(
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
                            blocks[id1]?.animatedMove(position)
                            blocks[id2]?.animatedMove(position)
                        }
                    }
                    block {
                        val nextNumber = numberFor(id1).next()
                        deleteBlock(id1)
                        deleteBlock(id2)
                        createNewBlockWithId(id1, nextNumber, position)
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

suspend fun Block?.animatedMove(position: Position) =
    this!!.moveTo(columnX(position.x), rowY(position.y), 0.15.seconds, Easing.LINEAR)

fun Animator.animateScale(block: Block) {
    val x = block.x
    val y = block.y
    val scale = block.scale
    tween(
        block::x[x - 4],
        block::y[y - 4],
        block::scale[scale + 0.1],
        time = 0.1.seconds,
        easing = Easing.LINEAR
    )
    tween(
        block::x[x],
        block::y[y],
        block::scale[scale],
        time = 0.1.seconds,
        easing = Easing.LINEAR
    )
}