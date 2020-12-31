package utils

import com.soywiz.korge.view.views

suspend fun getDefaultViewWidth(): Int = views().virtualWidth

suspend fun getDefaultCellSize(cellSize: Double?) = cellSize ?: (getDefaultViewWidth() / 5.0)
