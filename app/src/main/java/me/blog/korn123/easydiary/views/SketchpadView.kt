package me.blog.korn123.easydiary.views

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import me.blog.korn123.easydiary.domain.model.SketchPalette
import me.blog.korn123.easydiary.domain.model.SketchPoint
import me.blog.korn123.easydiary.domain.model.SketchStroke
import me.blog.korn123.easydiary.domain.model.SketchStrokeWidths

/**
 * The drawing surface of the sketchpad (issue #260).
 *
 * It paints white and draws freehand [SketchStroke]s with a plain [Canvas] + [Path] + [Paint], so the
 * sketchpad needs no drawing library. The touch points of a stroke are smoothed with [Path.quadTo], and
 * the eraser is a stroke in the canvas color, which keeps a single undo history and exports cleanly.
 *
 * The drawing itself is owned by the screen (the strokes are handed in through [submitStrokes]) and this
 * view only replays them plus the stroke that is being drawn right now, so the drawing survives a
 * configuration change. Strokes keep the pixel coordinates they were drawn at, so a rotation shows the
 * sketch at the size it was drawn rather than rescaled. A finished stroke is announced through
 * [onStrokeCommitted].
 */
class SketchpadView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {
    /**
     * Called with a stroke the user finished so the screen can add it to the undo history.
     */
    var onStrokeCommitted: ((SketchStroke) -> Unit)? = null

    var penColorArgb: Int = SketchPalette.DEFAULT_COLOR_ARGB
    var strokeWidthDp: Float = SketchStrokeWidths.DEFAULT_DP
    var isEraser: Boolean = false

    private val committedStrokes = mutableListOf<SketchStroke>()
    private val currentPoints = mutableListOf<SketchPoint>()

    private val strokePaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }
    private val dotPaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
        }
    private val canvasPaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = CANVAS_COLOR
        }
    private val smoothPath = Path()

    private val density = resources.displayMetrics.density
    private val minPointDistancePx = density * MIN_POINT_DISTANCE_DP

    /**
     * Replaces the drawing this view shows. Called from the screen whenever the undo history changes.
     */
    fun submitStrokes(strokes: List<SketchStroke>) {
        if (committedStrokes == strokes) return
        committedStrokes.clear()
        committedStrokes.addAll(strokes)
        invalidate()
    }

    /**
     * Renders the drawing into a new bitmap of the size this view shows, on a white background so the
     * file stays readable in any viewer. The caller owns the bitmap and has to recycle it.
     *
     * @return the rendered bitmap, or null when the view has not been laid out yet
     */
    fun exportBitmap(): Bitmap? {
        if (width <= 0 || height <= 0) return null
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(CANVAS_COLOR)
        committedStrokes.forEach { stroke ->
            drawPoints(canvas, stroke.points, stroke.colorArgb, stroke.widthDp, stroke.isEraser)
        }
        return bitmap
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), canvasPaint)
        committedStrokes.forEach { stroke ->
            drawPoints(canvas, stroke.points, stroke.colorArgb, stroke.widthDp, stroke.isEraser)
        }
        if (currentPoints.isNotEmpty()) {
            drawPoints(canvas, currentPoints, penColorArgb, strokeWidthDp, isEraser)
        }
    }

    @Suppress("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                parent?.requestDisallowInterceptTouchEvent(true)
                currentPoints.clear()
                currentPoints.add(SketchPoint(event.x, event.y))
                invalidate()
            }

            MotionEvent.ACTION_MOVE -> {
                appendHistoricalPoints(event)
                appendPoint(event.x, event.y)
            }

            MotionEvent.ACTION_UP -> {
                appendHistoricalPoints(event)
                appendPoint(event.x, event.y)
                commitCurrentStroke()
                parent?.requestDisallowInterceptTouchEvent(false)
            }

            MotionEvent.ACTION_CANCEL -> {
                currentPoints.clear()
                invalidate()
                parent?.requestDisallowInterceptTouchEvent(false)
            }

            else -> {}
        }
        return true
    }

    private fun appendHistoricalPoints(event: MotionEvent) {
        for (index in 0 until event.historySize) {
            appendPoint(event.getHistoricalX(index), event.getHistoricalY(index))
        }
    }

    /**
     * Adds a point only when it moved far enough from the previous one. Touch events arrive far denser than
     * the eye needs, and dropping the near duplicates keeps every replay of the history cheap.
     */
    private fun appendPoint(x: Float, y: Float) {
        val lastPoint = currentPoints.lastOrNull()
        if (lastPoint != null) {
            val distanceX = x - lastPoint.x
            val distanceY = y - lastPoint.y
            if (distanceX * distanceX + distanceY * distanceY < minPointDistancePx * minPointDistancePx) return
        }
        currentPoints.add(SketchPoint(x, y))
        invalidate()
    }

    private fun commitCurrentStroke() {
        if (currentPoints.isEmpty()) return
        val stroke =
            SketchStroke(
                colorArgb = penColorArgb,
                widthDp = strokeWidthDp,
                points = ArrayList(currentPoints),
                isEraser = isEraser,
            )
        currentPoints.clear()
        committedStrokes.add(stroke)
        invalidate()
        onStrokeCommitted?.invoke(stroke)
    }

    private fun drawPoints(
        canvas: Canvas,
        points: List<SketchPoint>,
        colorArgb: Int,
        widthDp: Float,
        isEraser: Boolean,
    ) {
        if (points.isEmpty()) return
        val paint = applyStrokePaint(colorArgb, widthDp, isEraser)
        val firstPoint = points.first()
        when (points.size) {
            1 -> {
                dotPaint.color = paint.color
                canvas.drawCircle(firstPoint.x, firstPoint.y, paint.strokeWidth / 2f, dotPaint)
            }

            else -> canvas.drawPath(buildPath(points), paint)
        }
    }

    private fun applyStrokePaint(
        colorArgb: Int,
        widthDp: Float,
        isEraser: Boolean,
    ): Paint =
        strokePaint.apply {
            color = if (isEraser) CANVAS_COLOR else colorArgb
            strokeWidth = (if (isEraser) widthDp * ERASER_WIDTH_SCALE_FACTOR else widthDp) * density
        }

    /**
     * Builds the smoothed path of a stroke: every point becomes the control point of a quadratic curve that
     * ends in the middle of it and the next point, which rounds the corners of the raw touch samples.
     */
    private fun buildPath(points: List<SketchPoint>): Path {
        smoothPath.reset()
        val firstPoint = points.first()
        smoothPath.moveTo(firstPoint.x, firstPoint.y)
        for (index in 1 until points.size) {
            val previousPoint = points[index - 1]
            val currentPoint = points[index]
            smoothPath.quadTo(
                previousPoint.x,
                previousPoint.y,
                (previousPoint.x + currentPoint.x) / 2f,
                (previousPoint.y + currentPoint.y) / 2f,
            )
        }
        val lastPoint = points.last()
        smoothPath.lineTo(lastPoint.x, lastPoint.y)
        return smoothPath
    }

    companion object {
        private const val CANVAS_COLOR = 0xFFFFFFFF.toInt()
        private const val MIN_POINT_DISTANCE_DP = 1.5f
        private const val ERASER_WIDTH_SCALE_FACTOR = 3f
    }
}
