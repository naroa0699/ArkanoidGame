package com.example.arkanoidgame;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

import java.util.ArrayList;
import java.util.List;

/**
 * SpriteSheet — Gestiona animación mediante recorte de fotogramas de un bitmap.
 *
 * Técnica clave: Rect de origen (src) define el fotograma a recortar.
 * RectF de destino (dst) define dónde y cómo de grande se dibuja.
 *
 * Layout de la sprite sheet (spritesheet.png, 4×1 fotogramas):
 * ┌───────┬───────┬───────┬───────┐
 * │  [0]  │  [1]  │  [2]  │  [3]  │
 * │       │  💥   │  💥💥 │ 💥💥💥│
 * └───────┴───────┴───────┴───────┘
 *  Frame 0: sin explosión (pelota normal)
 *  Frames 1-3: animación de explosión de bloque
 */
public class SpriteSheet {

    private Bitmap sheet;           // El bitmap completo de la sprite sheet
    private int frameWidth;         // Ancho de un fotograma en píxeles
    private int frameHeight;        // Alto de un fotograma en píxeles
    private static final int TOTAL_FRAMES = 4;

    // Lista de explosiones activas
    private List<Explosion> activeExplosions = new ArrayList<>();

    // ── Clase interna para una explosión activa ──────────────────────
    private static class Explosion {
        float x, y;          // posición en pantalla
        int currentFrame;    // fotograma actual (0–2 de la animación)
        int frameCounter;    // contador para ralentizar la animación
        boolean finished;

        Explosion(float x, float y) {
            this.x = x;
            this.y = y;
            this.currentFrame = 0;
            this.frameCounter = 0;
            this.finished = false;
        }
    }

    public SpriteSheet(Context context) {
        // generamos directamente por código
        sheet = generateSpriteSheet();

        frameWidth  = sheet.getWidth()  / TOTAL_FRAMES;
        frameHeight = sheet.getHeight();
    }
    // Método utilitario para generar la sprite sheet programáticamente
    public static Bitmap generateSpriteSheet() {
        int fw = 64, fh = 64;
        Bitmap bmp = Bitmap.createBitmap(fw * 4, fh, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmp);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);

        // Frame 0: pelota blanca
        p.setColor(Color.WHITE);
        c.drawCircle(fw * 0 + fw / 2f, fh / 2f, 20, p);

        // Frame 1: explosión pequeña (naranja)
        p.setColor(Color.parseColor("#FF6600"));
        c.drawCircle(fw * 1 + fw / 2f, fh / 2f, 24, p);
        p.setColor(Color.YELLOW);
        c.drawCircle(fw * 1 + fw / 2f, fh / 2f, 14, p);

        // Frame 2: explosión media
        p.setColor(Color.parseColor("#FF4400"));
        c.drawCircle(fw * 2 + fw / 2f, fh / 2f, 30, p);
        p.setColor(Color.parseColor("#FF8800"));
        c.drawCircle(fw * 2 + fw / 2f, fh / 2f, 18, p);

        // Frame 3: explosión grande (se desvanece)
        p.setColor(Color.parseColor("#44FF2200")); // muy transparente
        c.drawCircle(fw * 3 + fw / 2f, fh / 2f, 40, p);

        return bmp;
    }

    /** Registra una nueva animación de explosión en las coordenadas dadas */
    public void startExplosion(float cx, float cy) {
        activeExplosions.add(new Explosion(cx, cy));
    }

    /** Actualiza el estado de todas las explosiones activas */
    public void update() {
        List<Explosion> toRemove = new ArrayList<>();
        for (Explosion exp : activeExplosions) {
            exp.frameCounter++;
            // Cambiar frame cada 4 actualizaciones (~15 fps de animación)
            if (exp.frameCounter >= 4) {
                exp.frameCounter = 0;
                exp.currentFrame++;
                if (exp.currentFrame >= 3) {
                    exp.finished = true;
                    toRemove.add(exp);
                }
            }
        }
        activeExplosions.removeAll(toRemove);
    }

    /** Dibuja todas las explosiones activas */
    public void draw(Canvas canvas) {
        if (sheet == null) return;

        for (Explosion exp : activeExplosions) {
            // ── Rect SRC: recorta el fotograma correcto del sprite sheet ──
            // frame 0 del sprite sheet = primer fotograma de explosión (índice 1)
            int sheetFrame = exp.currentFrame + 1; // frames 1, 2, 3 son la explosión
            Rect src = new Rect(
                    sheetFrame * frameWidth,    // left
                    0,                          // top
                    (sheetFrame + 1) * frameWidth, // right
                    frameHeight                 // bottom
            );

            // ── RectF DST: dónde dibujar en pantalla ──────────────────
            float size = 80;
            RectF dst = new RectF(
                    exp.x - size / 2,
                    exp.y - size / 2,
                    exp.x + size / 2,
                    exp.y + size / 2
            );

            // drawBitmap con src y dst realiza el recorte y escalado automáticamente
            canvas.drawBitmap(sheet, src, dst, null);
        }
    }

}
