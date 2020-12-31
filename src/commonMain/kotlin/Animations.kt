import com.soywiz.klock.seconds
import com.soywiz.korge.animate.Animator
import com.soywiz.korge.tween.get
import com.soywiz.korma.interpolation.Easing
import model.Block

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