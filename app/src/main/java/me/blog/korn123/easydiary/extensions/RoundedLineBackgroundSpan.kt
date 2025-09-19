package me.blog.korn123.easydiary.extensions

import android.content.Context
import android.graphics.Canvas
import android.graphics.CornerPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.text.Layout
import android.text.Spanned
import android.text.TextPaint
import android.text.style.ReplacementSpan
import android.util.TypedValue
import android.widget.TextView
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.MarkwonSpansFactory
import org.commonmark.node.FencedCodeBlock
import android.text.style.TypefaceSpan
import android.text.style.ForegroundColorSpan
import android.text.style.LineBackgroundSpan
import androidx.core.content.ContextCompat

class RoundedLineBackgroundSpan (
    private val bgColor: Int,
    private val cornerRadius: Float,
    private val paddingH: Float = 16f,
    private val paddingV: Float = 6f
) : LineBackgroundSpan {

    override fun drawBackground(
        c: Canvas,
        p: Paint,
        left: Int,
        right: Int,
        top: Int,
        baseline: Int,
        bottom: Int,
        text: CharSequence,
        start: Int,
        end: Int,
        lnum: Int
    ) {
        // 기존 Paint 상태 저장
        val oldColor = p.color
        val oldStyle = p.style
        val oldPathEffect = p.pathEffect

        // 배경 색상 & PathEffect 지정
        p.color = bgColor
        p.style = Paint.Style.FILL
        p.isAntiAlias = true
        p.pathEffect = CornerPathEffect(cornerRadius)

        // 실제 사각형 범위 계산 (좌우 패딩 포함)
        val rect = RectF(
            left.toFloat() - paddingH,
            top.toFloat() + paddingV,
            right.toFloat() + paddingH,
            bottom.toFloat() - paddingV
        )

        // 사각형 경로 생성 후 그리기
        val path = Path()
        path.addRect(rect, Path.Direction.CW)
        c.drawPath(path, p)

        // Paint 상태 복원
        p.color = oldColor
        p.style = oldStyle
        p.pathEffect = oldPathEffect
    }
}