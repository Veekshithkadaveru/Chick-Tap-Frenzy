package app.krafted.chicktapfrenzy.game

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import app.krafted.chicktapfrenzy.R

class ChickGameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : SurfaceView(context, attrs, defStyleAttr), SurfaceHolder.Callback, GameRenderer {

    private var gameThread: GameThread? = null

    private val characterBitmaps = mutableMapOf<Int, Bitmap>()
    private val backgroundBitmaps = mutableListOf<Bitmap>()
    private var currentBackgroundIndex: Int = 0

    private var snapshot: GameSessionSnapshot? = null
    private val snapshotLock = Any()

    private var holeCenterXs = FloatArray(GameSession.HOLE_COUNT)
    private var holeCenterYs = FloatArray(GameSession.HOLE_COUNT)
    private var holeRadiusX: Float = 0f
    private var holeRadiusY: Float = 0f
    private var characterSize: Float = 0f
    private var gridReady = false

    private var onHoleTapped: ((Int) -> Unit)? = null
    private var onTickCallback: ((Float) -> Unit)? = null

    private val holePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val holeRimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f
        color = Color.argb(100, 60, 40, 20)
    }
    private val scoreFloatPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
        setShadowLayer(4f, 2f, 2f, Color.argb(120, 0, 0, 0))
    }
    private val holeShadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.argb(40, 0, 0, 0)
    }
    private val timerBackgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 12f
        strokeCap = Paint.Cap.ROUND
        color = Color.argb(80, 200, 200, 200)
    }
    private val timerForegroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 12f
        strokeCap = Paint.Cap.ROUND
    }
    private val clipPath = Path()

    init {
        holder.addCallback(this)
        isFocusable = true
        loadAssets()
    }

    fun setOnHoleTapped(listener: (Int) -> Unit) {
        onHoleTapped = listener
    }

    fun setOnTick(listener: (Float) -> Unit) {
        onTickCallback = listener
    }

    fun updateSnapshot(newSnapshot: GameSessionSnapshot) {
        synchronized(snapshotLock) {
            snapshot = newSnapshot
        }
    }

    fun setBackgroundIndex(index: Int) {
        currentBackgroundIndex = index.coerceIn(0, (backgroundBitmaps.size - 1).coerceAtLeast(0))
    }

    fun checkHoleTap(touchX: Float, touchY: Float): Int {
        if (!gridReady) return -1
        for (i in 0 until GameSession.HOLE_COUNT) {
            val dx = touchX - holeCenterXs[i]
            val dy = touchY - holeCenterYs[i]
            val tapRadius = holeRadiusX * TAP_RADIUS_SCALE
            val normalizedDist = (dx * dx) / (tapRadius * tapRadius) +
                    (dy * dy) / (tapRadius * tapRadius)
            if (normalizedDist <= 1f) {
                return i
            }
        }
        return -1
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val holeIndex = checkHoleTap(event.x, event.y)
            if (holeIndex != -1) {
                onHoleTapped?.invoke(holeIndex)
            }
            performClick()
            return true
        }
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        gameThread = GameThread(holder, this).apply {
            running = true
            start()
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        computeGridLayout(width, height)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        stopThread()
    }

    fun stopThread() {
        gameThread?.let { thread ->
            thread.stopLoop()
            var retry = true
            while (retry) {
                try {
                    thread.join(500)
                    retry = false
                } catch (_: InterruptedException) {
                    Thread.currentThread().interrupt()
                }
            }
        }
        gameThread = null
    }

    override fun update(deltaSeconds: Float) {
        onTickCallback?.invoke(deltaSeconds)
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        drawBackground(canvas)

        val currentSnapshot: GameSessionSnapshot
        synchronized(snapshotLock) {
            currentSnapshot = snapshot ?: return
        }

        if (!gridReady) return

        for (hole in currentSnapshot.holes) {
            drawCharacter(canvas, hole)
        }

        for (hole in currentSnapshot.holes) {
            drawHole(canvas, hole)
            drawTimerArc(canvas, hole)
        }

        drawScoreFloats(canvas, currentSnapshot.scoreFloats)
    }

    private fun drawBackground(canvas: Canvas) {
        if (backgroundBitmaps.isEmpty()) {
            canvas.drawColor(Color.rgb(120, 180, 80))
            return
        }

        val bgIndex = currentBackgroundIndex.coerceIn(0, backgroundBitmaps.size - 1)
        val bg = backgroundBitmaps[bgIndex]
        val canvasWidth = canvas.width.toFloat()
        val canvasHeight = canvas.height.toFloat()
        val scaleX = canvasWidth / bg.width
        val scaleY = canvasHeight / bg.height
        val scale = maxOf(scaleX, scaleY)
        val scaledW = bg.width * scale
        val scaledH = bg.height * scale
        val offsetX = (canvasWidth - scaledW) / 2f
        val offsetY = (canvasHeight - scaledH) / 2f
        val destRect = RectF(offsetX, offsetY, offsetX + scaledW, offsetY + scaledH)
        canvas.drawBitmap(bg, null, destRect, null)
    }

    private fun drawCharacter(canvas: Canvas, hole: HoleSnapshot) {
        if (hole.phase == HolePhase.EMPTY || hole.chickType == null) return

        val charId = hole.chickType.characterId
        val bitmap = characterBitmaps[charId] ?: return
        val centerX = holeCenterXs[hole.holeIndex]
        val centerY = holeCenterYs[hole.holeIndex]

        val popOffset = hole.popOffset
        val charDrawSize = characterSize
        val riseHeight = charDrawSize * MAX_RISE_FRACTION
        val yShift = riseHeight * popOffset

        canvas.save()
        clipPath.reset()
        clipPath.addOval(
            centerX - holeRadiusX * CLIP_SCALE_X,
            centerY - holeRadiusY * CLIP_SCALE_TOP,
            centerX + holeRadiusX * CLIP_SCALE_X,
            centerY + holeRadiusY * CLIP_SCALE_BOTTOM,
            Path.Direction.CW
        )
        canvas.clipPath(clipPath)

        val charLeft = centerX - charDrawSize / 2f
        val charTop = centerY - yShift
        val charRight = centerX + charDrawSize / 2f
        val charBottom = charTop + charDrawSize
        val destRect = RectF(charLeft, charTop, charRight, charBottom)
        canvas.drawBitmap(bitmap, null, destRect, null)
        canvas.restore()
    }

    private fun drawHole(canvas: Canvas, hole: HoleSnapshot) {
        val centerX = holeCenterXs[hole.holeIndex]
        val centerY = holeCenterYs[hole.holeIndex]

        canvas.drawOval(
            centerX - holeRadiusX - HOLE_SHADOW_OFFSET,
            centerY - holeRadiusY * 0.35f + HOLE_SHADOW_OFFSET,
            centerX + holeRadiusX + HOLE_SHADOW_OFFSET,
            centerY + holeRadiusY * 0.5f + HOLE_SHADOW_OFFSET,
            holeShadowPaint
        )

        holePaint.color = HOLE_FILL_COLOR
        canvas.drawOval(
            centerX - holeRadiusX,
            centerY - holeRadiusY * 0.35f,
            centerX + holeRadiusX,
            centerY + holeRadiusY * 0.5f,
            holePaint
        )

        canvas.drawOval(
            centerX - holeRadiusX,
            centerY - holeRadiusY * 0.35f,
            centerX + holeRadiusX,
            centerY + holeRadiusY * 0.5f,
            holeRimPaint
        )
    }

    private fun drawTimerArc(canvas: Canvas, hole: HoleSnapshot) {
        if (hole.phase != HolePhase.VISIBLE) return

        val centerX = holeCenterXs[hole.holeIndex]
        val centerY = holeCenterYs[hole.holeIndex]

        val arcPadding = 20f
        val arcRect = RectF(
            centerX - holeRadiusX - arcPadding,
            centerY - holeRadiusY * 0.35f - arcPadding,
            centerX + holeRadiusX + arcPadding,
            centerY + holeRadiusY * 0.5f + arcPadding
        )

        canvas.drawArc(arcRect, -90f, 360f, false, timerBackgroundPaint)

        val sweepAngle = (1f - hole.progress) * 360f
        if (hole.chickType?.isHazard == true) {
            timerForegroundPaint.color = Color.rgb(255, 60, 60)
        } else {
            timerForegroundPaint.color = Color.rgb(60, 255, 60)
        }

        canvas.drawArc(arcRect, -90f, sweepAngle, false, timerForegroundPaint)
    }

    private fun drawScoreFloats(canvas: Canvas, floats: List<ScoreFloat>) {
        for (sf in floats) {
            if (sf.holeIndex !in 0 until GameSession.HOLE_COUNT) continue
            val cx = holeCenterXs[sf.holeIndex]
            val baseY = holeCenterYs[sf.holeIndex] - holeRadiusY * 0.6f
            val drawY = baseY - sf.riseOffset * resources.displayMetrics.density

            scoreFloatPaint.textSize = SCORE_FLOAT_TEXT_SIZE * resources.displayMetrics.density
            scoreFloatPaint.alpha = (sf.alpha * 255).toInt().coerceIn(0, 255)
            scoreFloatPaint.color = when (sf.tone) {
                ScoreFloatTone.POSITIVE -> Color.WHITE
                ScoreFloatTone.BONUS -> Color.rgb(255, 215, 0)
                ScoreFloatTone.PENALTY -> Color.rgb(255, 60, 60)
            }
            scoreFloatPaint.alpha = (sf.alpha * 255).toInt().coerceIn(0, 255)
            canvas.drawText(sf.label, cx, drawY, scoreFloatPaint)
        }
    }

    private fun computeGridLayout(viewWidth: Int, viewHeight: Int) {
        if (viewWidth <= 0 || viewHeight <= 0) return

        val w = viewWidth.toFloat()
        val h = viewHeight.toFloat()
        val cols = GRID_COLS
        val rows = GRID_ROWS
        val cellW = w / cols
        val cellH = h / (rows + 1)
        val topPadding = cellH * 0.6f

        holeRadiusX = cellW * HOLE_RADIUS_X_FRACTION
        holeRadiusY = cellH * HOLE_RADIUS_Y_FRACTION
        characterSize = minOf(cellW, cellH) * CHARACTER_SIZE_FRACTION

        for (row in 0 until rows) {
            for (col in 0 until cols) {
                val index = row * cols + col
                if (index >= GameSession.HOLE_COUNT) break
                holeCenterXs[index] = cellW * (col + 0.5f)
                holeCenterYs[index] = topPadding + cellH * (row + 0.5f)
            }
        }
        gridReady = true
    }

    private fun loadAssets() {
        val characterResIds = intArrayOf(
            R.drawable.chickag6_sym_1,
            R.drawable.chickag6_sym_2,
            R.drawable.chickag6_sym_3,
            R.drawable.chickag6_sym_4,
            R.drawable.chickag6_sym_5,
            R.drawable.chickag6_sym_6,
            R.drawable.chickag6_sym_7
        )
        val opts = BitmapFactory.Options().apply { inMutable = false }
        for ((i, resId) in characterResIds.withIndex()) {
            val bmp = BitmapFactory.decodeResource(resources, resId, opts)
            if (bmp != null) {
                val scaled =
                    Bitmap.createScaledBitmap(bmp, BITMAP_SCALE_SIZE, BITMAP_SCALE_SIZE, true)
                characterBitmaps[i + 1] = scaled
                if (scaled !== bmp) bmp.recycle()
            }
        }

        val backgroundResIds = intArrayOf(
            R.drawable.chickag6_back_1,
            R.drawable.chickag6_back_2,
            R.drawable.chickag6_back_3,
            R.drawable.chickag6_back_4,
            R.drawable.chickag6_back_5
        )
        for (resId in backgroundResIds) {
            val bmp = BitmapFactory.decodeResource(resources, resId, opts)
            if (bmp != null) {
                backgroundBitmaps.add(bmp)
            }
        }
    }

    companion object {
        const val GRID_COLS = 2
        const val GRID_ROWS = 3
        const val HOLE_RADIUS_X_FRACTION = 0.32f
        const val HOLE_RADIUS_Y_FRACTION = 0.18f
        const val CHARACTER_SIZE_FRACTION = 0.55f
        const val MAX_RISE_FRACTION = 0.85f
        const val TAP_RADIUS_SCALE = 1.3f
        const val CLIP_SCALE_X = 1.15f
        const val CLIP_SCALE_TOP = 1.8f
        const val CLIP_SCALE_BOTTOM = 0.6f
        const val HOLE_SHADOW_OFFSET = 4f
        const val SCORE_FLOAT_TEXT_SIZE = 16f
        const val BITMAP_SCALE_SIZE = 256
        val HOLE_FILL_COLOR = Color.rgb(50, 30, 15)
    }
}
