package com.example.arkanoidgame;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;

public class Paddle {

    private float x, y;
    private int width, height;
    private int screenW;

    private Paint paint;
    private RectF bounds;

    public Paddle(float x, float y, int width, int height, int screenW) {
        this.x      = x;
        this.y      = y;
        this.width  = width;
        this.height = height;
        this.screenW = screenW;

        paint = new Paint();
        paint.setAntiAlias(true);
        // Degradado horizontal azul → cian → azul
        paint.setShader(new LinearGradient(
                x, y, x + width, y,
                new int[]{Color.parseColor("#1144FF"), Color.parseColor("#00CCFF"), Color.parseColor("#1144FF")},
                null,
                Shader.TileMode.CLAMP
        ));

        bounds = new RectF(x, y, x + width, y + height);
    }

    /**
     * Mueve la paleta a la posición X dada,
     * restringida dentro de los límites de la pantalla.
     */
    public void moveTo(float newX) {
        x = Math.max(0, Math.min(newX, screenW - width));
        updateBounds();
    }

    private void updateBounds() {
        bounds.set(x, y, x + width, y + height);
        // Actualizar el degradado con la nueva posición
        paint.setShader(new LinearGradient(
                x, y, x + width, y,
                new int[]{Color.parseColor("#1144FF"), Color.parseColor("#00CCFF"), Color.parseColor("#1144FF")},
                null,
                Shader.TileMode.CLAMP
        ));
    }

    public void draw(Canvas canvas) {
        canvas.drawRoundRect(bounds, height / 2f, height / 2f, paint);
    }

    // ── Getters ───────────────────────────────────────────────────────
    public Rect getBounds() {
        return new Rect((int)x, (int)y, (int)(x + width), (int)(y + height));
    }

    public float getX()     { return x; }
    public float getY()     { return y; }
    public int   getWidth() { return width; }
}
