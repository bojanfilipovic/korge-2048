package model

import com.soywiz.kds.IntArray2
import service.BlockService
import service.blocks
import kotlin.random.Random

data class Position(val x: Int, val y: Int)

class PositionMap(private val array: IntArray2 = IntArray2(4, 4, -1)) {

    private fun getOrNull(x: Int, y: Int): Position? = if (array[x, y] != -1) Position(x, y) else null

    private fun getNumber(x: Int, y: Int): Int =
        array.tryGet(x, y)
            ?.let { blocks[it]?.number?.ordinal ?: -1 }
            ?: -1

    operator fun get(x: Int, y: Int): Int = array[x, y]

    operator fun set(x: Int, y: Int, value: Int): Int = value.also { array[x, y] = it }

    fun forEach(action: (Int) -> Unit): Unit = array.forEach(action)

    override fun equals(other: Any?): Boolean =
        (other is PositionMap) && this.array.data.contentEquals(other.array.data)

    override fun hashCode() = array.hashCode()

    fun getRandomFreePosition(): Position? {
        val quantity = array.count { it == -1 }
        if (quantity == 0) return null
        val chosen = Random.nextInt(quantity)
        var current = -1
        array.each { x, y, value ->
            if (value == -1) {
                current++
                if (current == chosen) {
                    return Position(x, y)
                }
            }
        }
        return null
    }

    fun hasAvailableMoves(): Boolean {
        array.each { x, y, _ ->
            if (hasAdjacentEqualPosition(x, y)) return true
        }
        return false
    }

    fun copy() = PositionMap(array.copy(data = array.data.copyOf()))

    fun getNotEmptyPositionFrom(direction: Direction, line: Int): Position? {
        when (direction) {
            Direction.LEFT -> for (i in 0..3) getOrNull(i, line)?.let { return it }
            Direction.RIGHT -> for (i in 3 downTo 0) getOrNull(i, line)?.let { return it }
            Direction.TOP -> for (i in 0..3) getOrNull(line, i)?.let { return it }
            Direction.BOTTOM -> for (i in 3 downTo 0) getOrNull(line, i)?.let { return it }
        }
        return null
    }

    private fun hasAdjacentEqualPosition(x: Int, y: Int) = getNumber(x, y).let {
        it == getNumber(x - 1, y) || it == getNumber(x + 1, y) || it == getNumber(x, y - 1) || it == getNumber(x, y + 1)
    }
}

fun calculateNewMap(
    map: PositionMap,
    direction: Direction,
    moves: MutableList<Pair<Int, Position>>,
    merges: MutableList<Triple<Int, Int, Position>>,
    blockService: BlockService
    ): PositionMap {
        val newMap = PositionMap()
        val startIndex = when (direction) {
            Direction.LEFT, Direction.TOP -> 0
            Direction.RIGHT, Direction.BOTTOM -> 3
        }

        var columnRow = startIndex

        fun newPosition(line: Int) = when (direction) {
            Direction.LEFT -> Position(columnRow++, line)
            Direction.RIGHT -> Position(columnRow--, line)
            Direction.TOP -> Position(line, columnRow++)
            Direction.BOTTOM -> Position(line, columnRow--)
        }

        for (line in 0..3) {
            var currentPosition = map.getNotEmptyPositionFrom(direction, line)
            columnRow = startIndex
            while (currentPosition != null) {
                val newPosition = newPosition(line)
                val currentId = map[currentPosition.x, currentPosition.y]
                map[currentPosition.x, currentPosition.y] = -1

                val nextPosition = map.getNotEmptyPositionFrom(direction, line)
                val nextId = nextPosition?.let { map[it.x, it.y] }
                // two blocks are equal
                if (nextId != null && blockService.numberFor(currentId) == blockService.numberFor(nextId)) {
                    // merge these blocks
                    map[nextPosition.x, nextPosition.y] = -1
                    newMap[newPosition.x, newPosition.y] = currentId
                    merges += Triple(currentId, nextId, newPosition)
                } else {
                    // add old block
                    newMap[newPosition.x, newPosition.y] = currentId
                    moves += Pair(currentId, newPosition)
                }
                currentPosition = map.getNotEmptyPositionFrom(direction, line)
            }
        }

        return newMap
    }