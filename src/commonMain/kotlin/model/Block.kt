package model

import com.soywiz.korge.view.Container
import com.soywiz.korge.view.centerBetween
import com.soywiz.korge.view.roundRect
import com.soywiz.korge.view.text
import com.soywiz.korim.color.Colors
import com.soywiz.korim.font.Font
import com.soywiz.korio.async.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.withContext
import model.Number.EIGHT
import model.Number.ELEVEN
import model.Number.FIFTEEN
import model.Number.FIVE
import model.Number.FOUR
import model.Number.FOURTEEN
import model.Number.NINE
import model.Number.ONE
import model.Number.SEVEN
import model.Number.SIX
import model.Number.SIXTEEN
import model.Number.TEN
import model.Number.THIRTEEN
import model.Number.THREE
import model.Number.TWELVE
import model.Number.TWO
import model.Number.ZERO
import utils.getDefaultCellSize

class Block(val number: Number, private val cellSize: Double?, val font: Font) : Container() {

    init {

        GlobalScope.launch {
            withContext(Dispatchers.Default) {
                val defaultCellSize = getDefaultCellSize(cellSize)

                roundRect(defaultCellSize, defaultCellSize, 5.0, fill = number.color)
                val textColor = when (number) {
                    ZERO, ONE -> Colors.BLACK
                    else -> Colors.WHITE
                }
                text(number.value.toString(), textSizeFor(number, cellSize!!), textColor, font) {
                    centerBetween(0.0, 0.0, defaultCellSize, defaultCellSize)
                }

            }
        }
    }

    private fun textSizeFor(number: Number, cellSize: Double) =
        when (number) {
            ZERO, ONE, TWO, THREE, FOUR, FIVE -> cellSize / 2
            SIX, SEVEN, EIGHT -> cellSize * 4 / 9
            NINE, TEN, ELEVEN, TWELVE -> cellSize * 2 / 5
            THIRTEEN, FOURTEEN, FIFTEEN -> cellSize * 7 / 20
            SIXTEEN -> cellSize * 3 / 10
        }
}