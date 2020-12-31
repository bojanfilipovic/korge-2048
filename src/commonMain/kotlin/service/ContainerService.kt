package service

import cellSize
import com.soywiz.korge.view.Container
import com.soywiz.korge.view.RoundRect
import com.soywiz.korge.view.alignRightToLeftOf
import com.soywiz.korge.view.alignRightToRightOf
import com.soywiz.korge.view.alignTopToTopOf
import com.soywiz.korge.view.position
import com.soywiz.korge.view.roundRect
import com.soywiz.korim.color.Colors
import fieldSize
import leftIndent
import topIndent

class ContainerService {

    // todo bfilipovic: could be just createContainer generic
    fun fieldContainer(container: Container): RoundRect =
        container.roundRect(fieldSize, fieldSize, RX, fill = BG_FIELD_COLOR) {
            position(leftIndent, topIndent)
        }

    fun logoContainer(container: Container): RoundRect =
        container.roundRect(cellSize, cellSize, RX, fill = BG_LOGO_COLOR) {
            position(leftIndent, 30.0)
        }

    fun bestScoreContainer(container: Container, fieldContainer: RoundRect, logoContainer: RoundRect): RoundRect =
        container.roundRect(cellSize * 1.5, cellSize * 0.8, RX, fill = BG_FIELD_COLOR) {
            alignRightToRightOf(fieldContainer)
            alignTopToTopOf(logoContainer)
        }

    fun scoreContainer(container: Container, bestScoreContainer: RoundRect) =
        container.roundRect(cellSize * 1.5, cellSize * 0.8, RX, fill = BG_FIELD_COLOR) {
            alignRightToLeftOf(bestScoreContainer, 24.0)
            alignTopToTopOf(bestScoreContainer)
        }

    companion object {
        const val RX = 5.0
        val BG_FIELD_COLOR = Colors["#b9aea0"]
        val BG_LOGO_COLOR = Colors["#edc403"]
    }
}