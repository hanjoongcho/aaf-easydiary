package me.blog.korn123.easydiary.extensions

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.text.style.ReplacementSpan

class RoundedBackgroundSpan (
    private val bgColor: Int,                // 기본 배경 색
    private val textColor: Int,              // 기본 텍스트 색
    private val cornerRadius: Float,         // 둥근 정도
    private val keyword: String,             // 하이라이트 대상 문자열
    private val paddingH: Float = 16f,  // ➜ 좌우 패딩(px)
    private val paddingV: Float = 8f    // ➜ 상하 패딩(px)
) : ReplacementSpan() {

    private var HIGHLIGHT_COLOR: Int = 0x9FFFFF00.toInt()

    override fun getSize(
        paint: Paint,
        text: CharSequence,
        start: Int,
        end: Int,
        fm: Paint.FontMetricsInt?
    ): Int {
        // 글자 폭 + 좌우 패딩 2배
        val textWidth = paint.measureText(text, start, end)
        return (textWidth + paddingH * 2).toInt()
    }

    override fun draw(
        canvas: Canvas,
        text: CharSequence,
        start: Int,
        end: Int,
        x: Float,
        top: Int,
        y: Int,
        bottom: Int,
        paint: Paint
    ) {
        val oldColor = paint.color
        val textStr = text.subSequence(start, end).toString()

        // 전체 폭 계산
        val textWidth = paint.measureText(textStr)
        val left = x
        val right = x + textWidth + paddingH * 2
        val rect = RectF(
            left,
            y + paint.fontMetrics.ascent - paddingV,  // ascent 는 음수
            right,
            y + paint.fontMetrics.descent + paddingV
        )

        // 기본 배경 그리기
        paint.color = bgColor
        paint.isAntiAlias = true
//        val rect = RectF(x, top.toFloat(), x + totalWidth, bottom.toFloat())
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint)

        // 기본 텍스트 (하이라이트 제외 부분)
        paint.color = textColor
        canvas.drawText(textStr, x + paddingH, y.toFloat(), paint)

        // 키워드 찾기
        var searchIndex = textStr.indexOf(keyword)
        while (searchIndex >= 0) {
            val prefix = textStr.substring(0, searchIndex)
            val kwWidth = paint.measureText(keyword)
            val prefixWidth = paint.measureText(prefix)

            val kLeft = x + paddingH + prefixWidth
            val kRight = kLeft + kwWidth
            val kRect = RectF(
                kLeft,
                y + paint.fontMetrics.ascent,
                kRight,
                y + paint.fontMetrics.descent
            )

            paint.color = HIGHLIGHT_COLOR
            canvas.drawRect(kRect, paint)

            paint.color = Color.BLACK
            canvas.drawText(keyword, kLeft, y.toFloat(), paint)

            searchIndex = textStr.indexOf(keyword, searchIndex + keyword.length)
        }

        // paint 복원
        paint.color = oldColor
    }
}