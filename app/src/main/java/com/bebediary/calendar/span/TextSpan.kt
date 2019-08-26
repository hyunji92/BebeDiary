package com.bebediary.calendar.span

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.text.style.LineBackgroundSpan
import kotlin.math.min

class TextSpan(
    private var text: String,
    private var color: Int = 0xff959595.toInt()
) : LineBackgroundSpan {

    private val textRect by lazy { Rect() }

    // Max Line
    private val maxLine = 2

    // Horizontal 패딩
    private val widthPadding = 16

    override fun drawBackground(
        c: Canvas, p: Paint,
        left: Int, right: Int, top: Int, baseline: Int, bottom: Int,
        charSequence: CharSequence,
        start: Int, end: Int, lnum: Int
    ) {

        // 원래 텍스트 사이즈 저장
        val originalTextSize = p.textSize
        val originalColor = p.color

        // 새로운 텍스트 크기 설정
        p.textSize = (bottom - top) / maxLine.toFloat()

        // 새로 만들어진 텍스트
        var newText = ""

        // 현재 탐색중인 텍스트 인덱스
        var currentStartIndex = 0
        var currentTextIndex = 0
        while (currentTextIndex < text.length) {

            // 텍스트 크기 측정
            p.getTextBounds(text, currentStartIndex, currentTextIndex, textRect)
            val currentStepWidth = textRect.width()
            var nextStepWidth = textRect.width()

            // 다음 텍스트 크기 확인
            if (text.length > currentTextIndex + 1) {
                p.getTextBounds(text, currentStartIndex, currentTextIndex + 1, textRect)
                nextStepWidth = textRect.width()
            }

            // 다이어리 가로
            val width = right - left - widthPadding  // 16은 텍스트 패딩
            if (width in currentStepWidth..nextStepWidth) {
                newText += text.substring(currentStartIndex, currentTextIndex)
                newText += "\n"

                // 탐색 하는 시작 인덱스 변경
                currentStartIndex = currentTextIndex
            }

            // 탐색중인 텍스트 인덱스 증가
            ++currentTextIndex

            // 마지막 탐색일떄 모든 항목 전부 추가
            if (currentTextIndex == text.length) {
                newText += text.substring(currentStartIndex, text.length)
            }
        }

        // 텍스트 최대 두줄로 자름
        val textLines = newText.lines().run {
            subList(0, min(maxLine, count()))
        }

        // 색상 검정색으로 설정
        p.color = color

        // 글씨 작성
        textLines.forEachIndexed { index, text ->

            // 텍스트 크기 예상
            p.getTextBounds(text, 0, text.length, textRect)

            // 글씨가 적히는 위치
            val x =
                (right - left - widthPadding) / 2f - (textRect.width() / 2f) + (widthPadding / 2f)
            val y = ((bottom - top) / 2f) + ((bottom - top)) + textRect.height() * index

            // 글씨 작성
            c.drawText(text, x, y, p)
        }

        // 페인트 복구
        p.textSize = originalTextSize
        p.color = originalColor
    }
}