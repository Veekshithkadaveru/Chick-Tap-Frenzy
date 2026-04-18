package app.krafted.chicktapfrenzy.game

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
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
    private var currentBackgroundIndex: Int = 1

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

    private var vignettePaint: Paint? = null
    private var atmospherePaint: Paint? = null
    private var overlayCanvasWidth: Int = 0
    private var overlayCanvasHeight: Int = 0

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
        val currentSnapshot: GameSessionSnapshot?
        synchronized(snapshotLock) { currentSnapshot = snapshot }

        for (i in 0 until GameSession.HOLE_COUNT) {
            val popOffset = currentSnapshot?.holes?.getOrNull(i)?.popOffset ?: 0f
            val yShift = characterSize * MAX_RISE_FRACTION * popOffset
            val dx = touchX - holeCenterXs[i]
            val dy = touchY - (holeCenterYs[i] - yShift)
            val minTapRadius = 48f * resources.displayMetrics.density
            val tapRadius = maxOf(holeRadiusX * TAP_RADIUS_SCALE, minTapRadius)
            if ((dx * dx + dy * dy) <= tapRadius * tapRadius) return i
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
        val canvasWidth = canvas.width.toFloat()
        val canvasHeight = canvas.height.toFloat()

        if (backgroundBitmaps.isEmpty()) {
            canvas.drawColor(Color.rgb(120, 180, 80))
        } else {
            val bgIndex = currentBackgroundIndex.coerceIn(0, backgroundBitmaps.size - 1)
            val bg = backgroundBitmaps[bgIndex]
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

        ensureOverlayPaints(canvasWidth.toInt(), canvasHeight.toInt())
        atmospherePaint?.let { canvas.drawRect(0f, 0f, canvasWidth, canvasHeight, it) }
        vignettePaint?.let { canvas.drawRect(0f, 0f, canvasWidth, canvasHeight, it) }
    }

    private fun ensureOverlayPaints(width: Int, height: Int) {
        if (width <= 0 || height <= 0) return
        if (width == overlayCanvasWidth && height == overlayCanvasHeight && vignettePaint != null) return

        overlayCanvasWidth = width
        overlayCanvasHeight = height

        val radial = RadialGradient(
            width / 2f,
            height * 0.55f,
            maxOf(width, height) * 0.82f,
            intArrayOf(
                Color.argb(0, 0, 0, 0),
                Color.argb(0, 0, 0, 0),
                Color.argb(170, 0, 0, 0)
            ),
            floatArrayOf(0f, 0.55f, 1f),
            Shader.TileMode.CLAMP
        )
        vignettePaint = Paint().apply { shader = radial }

        val linear = LinearGradient(
            0f, 0f, 0f, height.toFloat(),
            intArrayOf(
                Color.argb(55, 255, 215, 140),
                Color.argb(0, 0, 0, 0),
                Color.argb(60, 40, 20, 10)
            ),
            floatArrayOf(0f, 0.45f, 1f),
            Shader.TileMode.CLAMP
        )
        atmospherePaint = Paint().apply { shader = linear }
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

        val rx = holeRadiusX
        val ry = holeRadiusY * 0.5f

        canvas.save()
        clipPath.reset()
        clipPath.addOval(
            centerX - rx * CLIP_SCALE_X,
            centerY - ry * CLIP_SCALE_TOP,
            centerX + rx * CLIP_SCALE_X,
            centerY + ry * CLIP_SCALE_BOTTOM,
            Path.Direction.CW
        )
        canvas.clipPath(clipPath)

        val charLeft = centerX - charDrawSize / 2f
        val charTop = centerY - yShift
        val charRight = centerX + charDrawSize / 2f
        val charBottom = charTop + charDrawSize
        val destRect = RectF(charLeft, charTop, charRight, charBottom)
        if (hole.phase == HolePhase.SQUISHED) {
            val scaleY = (1f - hole.progress * 0.8f).coerceAtLeast(0.2f)
            canvas.scale(1f, scaleY, centerX, charBottom)
        }
        canvas.drawBitmap(bitmap, null, destRect, null)
        canvas.restore()
    }

    private fun drawHole(canvas: Canvas, hole: HoleSnapshot) {
        val centerX = holeCenterXs[hole.holeIndex]
        val centerY = holeCenterYs[hole.holeIndex]
        val rx = holeRadiusX
        val ry = holeRadiusY * 0.5f

        canvas.drawOval(
            centerX - rx - HOLE_SHADOW_OFFSET,
            centerY - ry * 0.4f + HOLE_SHADOW_OFFSET,
            centerX + rx + HOLE_SHADOW_OFFSET,
            centerY + ry + HOLE_SHADOW_OFFSET,
            holeShadowPaint
        )

        holePaint.color = HOLE_FILL_COLOR
        canvas.drawOval(
            centerX - rx,
            centerY - ry * 0.4f,
            centerX + rx,
            centerY + ry,
            holePaint
        )

        holeRimPaint.strokeWidth = (rx * 0.12f).coerceAtLeast(4f)
        canvas.drawOval(
            centerX - rx,
            centerY - ry * 0.4f,
            centerX + rx,
            centerY + ry,
            holeRimPaint
        )
    }

    private fun drawTimerArc(canvas: Canvas, hole: HoleSnapshot) {
        if (hole.phase != HolePhase.VISIBLE) return

        val centerX = holeCenterXs[hole.holeIndex]
        val centerY = holeCenterYs[hole.holeIndex]
        val rx = holeRadiusX
        val ry = holeRadiusY * 0.5f
        val arcStrokeWidth = (rx * 0.18f).coerceIn(6f, 14f)
        timerBackgroundPaint.strokeWidth = arcStrokeWidth
        timerForegroundPaint.strokeWidth = arcStrokeWidth

        val arcPadding = arcStrokeWidth / 2f + 4f
        val yShift = characterSize * MAX_RISE_FRACTION * hole.popOffset
        val arcRect = RectF(
            centerX - rx - arcPadding,
            (centerY - yShift) - ry * 0.4f - arcPadding,
            centerX + rx + arcPadding,
            (centerY - yShift) + ry + arcPadding
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
        val topPadding = cellH * 0.8f

        holeRadiusX = cellW * HOLE_RADIUS_X_FRACTION
        holeRadiusY = holeRadiusX * HOLE_ASPECT_RATIO
        characterSize = holeRadiusX * CHARACTER_SIZE_FRACTION

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
        val opts = BitmapFactory.Options().apply { inMutable = false }
        val characterEntries = arrayOf(
            Pair(5, R.drawable.chickag6_sym_5),
            Pair(6, R.drawable.chickag6_sym_6),
            Pair(7, R.drawable.chickag6_sym_7)
        )
        for ((charId, resId) in characterEntries) {
            val bmp = BitmapFactory.decodeResource(resources, resId, opts)
            if (bmp != null) {
                val scaled =
                    Bitmap.createScaledBitmap(bmp, BITMAP_SCALE_SIZE, BITMAP_SCALE_SIZE, true)
                characterBitmaps[charId] = scaled
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
        const val HOLE_RADIUS_X_FRACTION = 0.20f
        const val HOLE_ASPECT_RATIO = 0.4f
        const val CHARACTER_SIZE_FRACTION = 2.2f
        const val MAX_RISE_FRACTION = 1.0f
        const val TAP_RADIUS_SCALE = 1.4f
        const val CLIP_SCALE_X = 1.1f
        const val CLIP_SCALE_TOP = 14f
        const val CLIP_SCALE_BOTTOM = 0.55f
        const val HOLE_SHADOW_OFFSET = 5f
        const val SCORE_FLOAT_TEXT_SIZE = 16f
        const val BITMAP_SCALE_SIZE = 256
        val HOLE_FILL_COLOR = Color.rgb(30, 18, 8)
    }
}
