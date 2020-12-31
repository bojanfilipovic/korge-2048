import com.soywiz.korge.view.Container
import com.soywiz.korge.view.addTo
import com.soywiz.korge.view.position
import model.Block
import model.Number
import model.Position
import kotlin.random.Random

val blocks = mutableMapOf<Int, Block>()
var nextFreeBlockId = 0
const val INDENT_BETWEEN_BLOCKS = 10

fun numberFor(blockId: Int): Number = blocks[blockId]!!.number
fun deleteBlock(blockId: Int) = blocks.remove(blockId)!!.removeFromParent()

fun Container.generateBlock() {
    val position = map.getRandomFreePosition() ?: return
    val number = if (Random.nextDouble() > 0.9) Number.ZERO else Number.ONE
    val newId = createNewBlock(number, position)
    map[position.x, position.y] = newId
}

// todo bfilipovic: clean this up make it more readable
private fun Container.createNewBlock(
    number: Number,
    position: Position,
): Int =
    createNewBlockWithId(nextFreeBlockId++, number, position)

fun Container.createNewBlockWithId(
    id: Int,
    number: Number,
    position: Position
): Int {
    blocks[id] = block(number).position(
        columnX(position.x),
        rowY(position.y)
    )
    return id
}

fun columnX(number: Int) =
    leftIndent + INDENT_BETWEEN_BLOCKS + (cellSize + INDENT_BETWEEN_BLOCKS) * number

fun rowY(number: Int) =
    topIndent + INDENT_BETWEEN_BLOCKS + (cellSize + INDENT_BETWEEN_BLOCKS) * number

private fun Container.block(number: Number) =
    Block(number, cellSize, font).addTo(this)