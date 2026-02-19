package com.example.arkanoidgame;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;


public class Block {

    private float x, y;
    private int width, height;
    private int color;
    private int points;
    private boolean alive = true;
    private int hp;   // Puntos de vida (bloques rojos necesitan 2 golpes)
    private boolean isSteel; //Bloques que no se eliminan
    private Paint fillPaint;
    private Paint borderPaint;
    private RectF rectF;

    private boolean invisible = false;

    public Block(float x, float y, int width, int height, int color, int points, boolean isSteel) {
        this.x      = x;
        this.y      = y;
        this.width  = width;
        this.height = height;
        this.color  = color;
        this.points = points;
        this.isSteel = isSteel;
        this.hp     = (points == 3) ? 2 : 1; // bloques rojos: 2 golpes

        fillPaint = new Paint();
        fillPaint.setColor(color);
        fillPaint.setAntiAlias(true);

        borderPaint = new Paint();
        borderPaint.setColor(Color.parseColor("#FFFFFF44")); // blanco semitransparente
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(2);
        borderPaint.setAntiAlias(true);

        if (this.isSteel) {
            // Borde plateado brillante para el acero
            borderPaint.setColor(Color.parseColor("#AAAAAA"));
            borderPaint.setStyle(Paint.Style.STROKE);
            borderPaint.setStrokeWidth(4);
        } else {
            borderPaint.setColor(Color.parseColor("#FFFFFF44"));
            borderPaint.setStyle(Paint.Style.STROKE);
            borderPaint.setStrokeWidth(2);
        }

        rectF = new RectF(x, y, x + width, y + height);
    }

    /** Recibe un golpe. Reduce HP y destruye si llega a 0 */
    public void hit() {
        if (isSteel) return; // ← el acero no se destruye
        hp--;
        if (hp <= 0) {
            alive = false;
        } else {
            // Oscurecer el color al recibir daño (bloque dañado)
            int r = (Color.red(color)   * 6) / 10;
            int g = (Color.green(color) * 6) / 10;
            int b = (Color.blue(color)  * 6) / 10;
            fillPaint.setColor(Color.rgb(r, g, b));
        }
    }

    public void draw(Canvas canvas) {
        if (!alive) return;
        canvas.drawRoundRect(rectF, 6, 6, fillPaint);
        canvas.drawRoundRect(rectF, 6, 6, borderPaint);
        // Cruz metálica encima del bloque de acero
        if (isSteel) {
            Paint crossPaint = new Paint();
            crossPaint.setColor(Color.parseColor("#CCCCCC"));
            crossPaint.setStrokeWidth(3);
            crossPaint.setAntiAlias(true);
            float cx = x + width / 2f;
            float cy = y + height / 2f;
            canvas.drawLine(cx - 10, cy, cx + 10, cy, crossPaint);
            canvas.drawLine(cx, cy - 10, cx, cy + 10, crossPaint);
        }
    }
    public Rect getBounds() {
        return new Rect((int)x, (int)y, (int)(x + width), (int)(y + height));
    }

    public void setInvisible() {
        this.invisible = true;
        this.alive     = false; // no participa en colisiones ni en conteo
    }

    public boolean isInvisible() { return invisible; }

    // ── Getters ───────────────────────────────────────────────────────
    public boolean isSteel()  { return isSteel; }
    public float getCenterX() { return x + width  / 2f; }
    public float getCenterY() { return y + height / 2f; }
    public boolean isAlive()  { return alive; }
    public int getPoints()    { return points; }


}
