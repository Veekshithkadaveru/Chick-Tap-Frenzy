package app.krafted.chicktapfrenzy.game

import android.graphics.Canvas
import android.view.SurfaceHolder

interface GameRenderer {
    fun update(deltaSeconds: Float)
    fun draw(canvas: Canvas)
}

class GameThread(
    private val surfaceHolder: SurfaceHolder,
    private val renderer: GameRenderer
) : Thread("ChickGameThread") {

    @Volatile
    var running: Boolean = false

    fun stopLoop() {
        running = false
    }

    override fun run() {
        var lastTime = System.nanoTime()
        while (running) {
            val frameStart = System.nanoTime()
            val deltaNs = frameStart - lastTime
            lastTime = frameStart

            val deltaSeconds = (deltaNs / 1_000_000_000f).coerceAtMost(MAX_DELTA_SEC)
            renderer.update(deltaSeconds)

            var canvas: Canvas? = null
            try {
                canvas = surfaceHolder.lockCanvas()
                if (canvas != null) {
                    synchronized(surfaceHolder) {
                        renderer.draw(canvas)
                    }
                }
            } finally {
                if (canvas != null) {
                    surfaceHolder.unlockCanvasAndPost(canvas)
                }
            }

            val frameTimeNs = System.nanoTime() - frameStart
            val sleepNs = TARGET_FRAME_NS - frameTimeNs
            if (sleepNs > 0L) {
                val sleepMs = sleepNs / 1_000_000L
                val sleepNanos = (sleepNs % 1_000_000L).toInt()
                try {
                    sleep(sleepMs, sleepNanos)
                } catch (_: InterruptedException) {
                    currentThread().interrupt()
                }
            }
        }
    }

    companion object {
        const val TARGET_FPS: Int = 60
        const val TARGET_FRAME_NS: Long = 1_000_000_000L / TARGET_FPS
        const val MAX_DELTA_SEC: Float = 0.05f
    }
}
