package com.example.arkanoidgame;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

public class Ball {

    private float x, y;       // posición del centro
    private float vx, vy;     // velocidad (píxeles por frame)
    private int radius;
    private int screenW, screenH;

    private Paint paint;
    private Paint glowPaint;

    private float speed = SPEED; // velocidad actual (aumenta con el tiempo)
    private boolean bouncedWall = false; // flag para notificar rebote en pared

    // Velocidad inicial
    private static final float SPEED = 12f;

    public Ball(float startX, float startY, int radius, int screenW, int screenH) {
        this.x = startX;
        this.y = startY;
        this.radius = radius;
        this.screenW = screenW;
        this.screenH = screenH;

        // Ángulo inicial: 45° hacia arriba-derecha
        vx = SPEED * 0.7f;
        vy = -SPEED;

        paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setAntiAlias(true);

        glowPaint = new Paint();
        glowPaint.setColor(Color.parseColor("#44FFFFFF"));
        glowPaint.setAntiAlias(true);
        glowPaint.setMaskFilter(
                new android.graphics.BlurMaskFilter(radius * 2, android.graphics.BlurMaskFilter.Blur.NORMAL)
        );
    }

    public void update() {
        bouncedWall = false; // resetear cada frame
        x += vx;
        y += vy;

        // Rebote en paredes laterales
        if (x - radius <= 0) {
            x = radius;
            vx = Math.abs(vx);  // siempre positivo (hacia la derecha)
            bouncedWall = true; // ← marcar rebote
        }
        if (x + radius >= screenW) {
            x = screenW - radius;
            vx = -Math.abs(vx); // siempre negativo (hacia la izquierda)
            bouncedWall = true; // ← marcar rebote
        }

        // Rebote en techo
        if (y - radius <= 0) {
            y = radius;
            vy = Math.abs(vy);  // siempre positivo (hacia abajo)
            bouncedWall = true; // ← marcar rebote
        }
        // Nota: no hay rebote en el suelo — eso es una "vida perdida"
    }

    public void draw(Canvas canvas) {
        // Glow (halo) alrededor de la pelota
        canvas.drawCircle(x, y, radius * 2.5f, glowPaint);
        // Pelota sólida
        canvas.drawCircle(x, y, radius, paint);
    }

    /**
     * Ajusta el ángulo horizontal basado en dónde golpeó la paleta.
     * @param hitPoint valor 0.0 (borde izq) a 1.0 (borde der)
     */
    public void setAngle(float hitPoint) {
        // Normalizar: -1.0 (extremo izq) a +1.0 (extremo der)
        float normalized = hitPoint * 2 - 1;
        // La velocidad horizontal cambia según el punto de impacto
        vx = normalized * SPEED;
        // Mantener vy negativo (hacia arriba) con velocidad constante
        vy = -Math.abs(vy);
        // Garantizar velocidad mínima vertical para evitar pelota horizontal
        if (Math.abs(vy) < 4) vy = -4;
    }

    /** Invierte la componente Y de la velocidad (rebote vertical) */
    public void bounceY() {
        vy = -vy;
    }

    /** Invierte la componente X de la velocidad (rebote horizontal) */
    public void bounceX() {
        vx = -vx;
    }

    /** Resetea la pelota a una posición inicial */
    public void reset(float startX, float startY) {
        x = startX;
        y = startY;
        vx = SPEED * 0.7f;
        vy = -SPEED;
    }

    // ── Getters para detección de colisiones ──────────────────────────

    /** Devuelve el Rect de bounding box de la pelota */
    public Rect getBounds() {
        return new Rect(
                (int)(x - radius),
                (int)(y - radius),
                (int)(x + radius),
                (int)(y + radius)
        );
    }
    public void increaseSpeed(float amount) {
        speed += amount;
        // Normalizar el vector de velocidad a la nueva speed
        float currentSpeed = (float) Math.sqrt(vx * vx + vy * vy);
        if (currentSpeed > 0) {
            vx = (vx / currentSpeed) * speed;
            vy = (vy / currentSpeed) * speed;
        }
    }

    public boolean justBouncedWall() { return bouncedWall; }

    public float getCenterX() { return x; }
    public float getY()       { return y; }
}
