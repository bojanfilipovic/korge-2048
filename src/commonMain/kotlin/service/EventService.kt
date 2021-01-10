package service

import com.soywiz.korev.Key
import com.soywiz.korev.KeyEvent
import com.soywiz.korge.input.SwipeDirection
import com.soywiz.korge.input.SwipeInfo
import com.soywiz.korge.input.keys
import com.soywiz.korge.input.onSwipe
import com.soywiz.korge.view.RoundRect
import com.soywiz.korge.view.View
import com.soywiz.korio.async.ObservableProperty
import model.Direction

class EventService(private val blockService: BlockService) {

    fun assignKeyEvents(container: RoundRect, property: ObservableProperty<Int>) =
        container.keys {
            down { key -> key.toDirection()?.let { blockService.moveBlocksTo(it, property) } }
        }

    fun assignSwipeEvents(view: View, property: ObservableProperty<Int>) =
        view.onSwipe(20.0) { blockService.moveBlocksTo(it.toDirection(), property) }

    private fun KeyEvent.toDirection() =
        when (this.key) {
            Key.LEFT -> Direction.LEFT
            Key.RIGHT -> Direction.RIGHT
            Key.UP -> Direction.TOP
            Key.DOWN -> Direction.BOTTOM
            else -> null
        }

    private fun SwipeInfo.toDirection() =
        when (this.direction) {
            SwipeDirection.LEFT -> Direction.LEFT
            SwipeDirection.RIGHT -> Direction.RIGHT
            SwipeDirection.TOP -> Direction.TOP
            SwipeDirection.BOTTOM -> Direction.BOTTOM
        }
}