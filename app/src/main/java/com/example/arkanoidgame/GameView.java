package com.example.arkanoidgame;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import java.util.Random;

/**
 * GameView — Clase principal del juego.
 *
 * Extiende SurfaceView:  Proporciona un Canvas dedicado para dibujar
 *                         en un hilo separado al UI thread.
 * Implementa Runnable:   El método run() contiene el Game Loop.
 * Implementa Callback:   Recibe eventos del ciclo de vida de la Surface.
 */
public class GameView extends SurfaceView
        implements Runnable, SurfaceHolder.Callback, View.OnTouchListener {

    // ─── Hilo y control del bucle ───────────────────────────────────────
    private Thread gameThread;
    private volatile boolean isRunning = false;
    // volatile garantiza visibilidad entre hilos sin necesidad de synchronized

    private float[] stars;
    private SoundManager soundManager;
    private LevelManager levelManager = new LevelManager();
    private int blocksDestroyed = 0; // ← contador de bloques destruidos

    // ─── Dimensiones de pantalla ─────────────────────────────────────────
    private int screenWidth;
    private int screenHeight;

    // ─── Entidades del juego ─────────────────────────────────────────────
    private Ball ball;
    private Paddle paddle;
    private Block[][] blocks;
    private SpriteSheet spriteSheet;

    // ─── Estado del juego ────────────────────────────────────────────────
    private enum GameState { WAITING, PLAYING, GAME_OVER, WIN }
    private GameState state = GameState.WAITING;
    private int score = 0;
    private int lives = 3;

    // ─── Paint reutilizable ──────────────────────────────────────────────
    private Paint textPaint;
    private Paint hudPaint;

    // ─── Control táctil ──────────────────────────────────────────────────
    private float touchX = -1;

    // ════════════════════════════════════════════════════════════════════
    // CONSTRUCTOR
    // ════════════════════════════════════════════════════════════════════
    public GameView(Context context, int width, int height) {
        super(context);
        this.screenWidth  = width;
        this.screenHeight = height;

        // Registrar callbacks del ciclo de vida de la Surface
        getHolder().addCallback(this);

        // Habilitar eventos táctiles
        setOnTouchListener(this);
        setFocusable(true);

        // Inicializar Paint para textos HUD
        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(60);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setAntiAlias(true);

        hudPaint = new Paint();
        hudPaint.setColor(Color.YELLOW);
        hudPaint.setTextSize(40);
        hudPaint.setAntiAlias(true);

        soundManager = new SoundManager(context);
        // Inicializar entidades
        initGame();
    }

    // ════════════════════════════════════════════════════════════════════
    // INICIALIZACIÓN DEL JUEGO
    // ════════════════════════════════════════════════════════════════════
    private void initGame() {
        // Cargamos el sprite sheet desde res/drawable
        spriteSheet = new SpriteSheet(getContext());

        // Pelota: centrada en pantalla, tamaño proporcional
        int ballSize = screenWidth / 30;
        ball = new Ball(screenWidth / 2f, screenHeight * 0.65f, ballSize, screenWidth, screenHeight);

        // Paleta: centrada horizontalmente en la parte inferior
        int paddleWidth  = screenWidth / 5;
        int paddleHeight = screenHeight / 35;
        paddle = new Paddle(
                screenWidth / 2f - paddleWidth / 2f,
                screenHeight * 0.85f,
                paddleWidth, paddleHeight,
                screenWidth
        );

        // Bloques: cuadrícula de 8 columnas × 5 filas
        initBlocks();
        initStars();

        levelManager.reset(); // ← resetear niveles al reiniciar
        initLevel();
        blocksDestroyed = 0;
        score = 0;
        lives = 3;
        state = GameState.WAITING;
    }

    private void initLevel() {
        spriteSheet     = new SpriteSheet(getContext());
        blocksDestroyed = 0;
        int ballSize    = screenWidth / 30;
        ball   = new Ball(screenWidth / 2f, screenHeight * 0.65f, ballSize, screenWidth, screenHeight);
        paddle = new Paddle(screenWidth / 2f - (screenWidth / 5) / 2f,
                screenHeight * 0.85f,
                screenWidth / 5, screenHeight / 35, screenWidth);
        initBlocks();
        initStars();
    }

    private void initStars() {
        stars = new float[200]; // 100 estrellas (x,y por cada una)
        Random rnd = new Random();
        for (int i = 0; i < stars.length; i++) {
            stars[i] = (i % 2 == 0)
                    ? rnd.nextInt(screenWidth)
                    : rnd.nextInt(screenHeight);
        }
    }


    private void initBlocks() {
        int cols      = 8;
        int rows      = 5;
        int margin    = screenWidth / 40;
        int topOffset = screenHeight / 8;
        int blockW    = (screenWidth - margin * 2) / cols - margin / cols;
        int blockH    = screenHeight / 20;
        int gapX      = (screenWidth - margin * 2 - blockW * cols) / (cols - 1);
        int gapY      = blockH / 3;

        // Leer la configuración del nivel actual
        int[][] layout = levelManager.getCurrentLayout();

        blocks = new Block[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                float x   = margin + c * (blockW + gapX);
                float y   = topOffset + r * (blockH + gapY);
                int   tipo = layout[r][c];

                if (tipo == 0) {
                    blocks[r][c] = new Block(x, y, blockW, blockH,
                            android.graphics.Color.TRANSPARENT, 0, false);
                    blocks[r][c].setInvisible(); // ver paso 3
                } else {
                    blocks[r][c] = new Block(x, y, blockW, blockH,
                            getColorForType(tipo),
                            getPointsForType(tipo),
                            tipo == 9
                    );
                }
            }
        }
    }

    private int getColorForType(int tipo) {
        switch (tipo) {
            case 1: return android.graphics.Color.parseColor("#4488FF"); // azul
            case 2: return android.graphics.Color.parseColor("#44CC44"); // verde
            case 3: return android.graphics.Color.parseColor("#FFDD00"); // amarillo
            case 4: return android.graphics.Color.parseColor("#FF8800"); // naranja
            case 5: return android.graphics.Color.parseColor("#FF4444"); // rojo
            case 9: return android.graphics.Color.parseColor("#888888"); // acero
            default: return android.graphics.Color.TRANSPARENT;
        }
    }

    private int getPointsForType(int tipo) {
        switch (tipo) {
            case 1: case 2: return 1;
            case 3: case 4: return 2;
            case 5:         return 3;
            default:        return 0;
        }
    }

    // ════════════════════════════════════════════════════════════════════
    // GAME LOOP — run() se ejecuta en el gameThread
    // ════════════════════════════════════════════════════════════════════
    @Override
    public void run() {
        // BUCLE PRINCIPAL DEL JUEGO (Game Loop)
        // Corre en un hilo separado al UI Thread para no bloquearlo.
        // Patrón: update (lógica/física) → draw (renderizado) → sleep (control FPS)
        // FPS objetivo: 60 fotogramas por segundo
        final long TARGET_FPS  = 60;
        final long TARGET_TIME = 1_000_000_000L / TARGET_FPS; // en nanosegundos

        while (isRunning) {
            long startTime = System.nanoTime();

            // ── 1. Actualizar lógica ──────────────────────────────────
            update();

            // ── 2. Renderizar ─────────────────────────────────────────
            draw();

            // ── 3. Control de FPS (limitar velocidad del bucle) ───────
            long elapsed   = System.nanoTime() - startTime;
            long sleepTime = (TARGET_TIME - elapsed) / 1_000_000L; // a ms

            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    // ════════════════════════════════════════════════════════════════════
    // ACTUALIZACIÓN DE LÓGICA
    // ════════════════════════════════════════════════════════════════════
    private void update() {
        if (state != GameState.PLAYING) return;

        ball.update();

        // Colisión con paleta
        if (Rect.intersects(ball.getBounds(), paddle.getBounds())) {
            ball.bounceY();
            ball.setAngle((ball.getCenterX() - paddle.getX()) / paddle.getWidth());
            soundManager.playBouncePaddle(); // ← SONIDO paleta
        }

        // Colisión con paredes (detectada en Ball.update, pero sonido aquí)
        if (ball.justBouncedWall()) {
            soundManager.playBounceWall(); // ← SONIDO pared
        }

        // Colisión con bloques
        boolean allDestroyed = true;
        for (int r = 0; r < blocks.length; r++) {
            for (int c = 0; c < blocks[r].length; c++) {
                Block block = blocks[r][c];
                if (block.isAlive()) {
                    if (!block.isSteel()) allDestroyed = false;
                    if (Rect.intersects(ball.getBounds(), block.getBounds())) {
                        boolean wasAlive = block.isAlive();
                        block.hit();
                        ball.bounceY();

                        if (block.isSteel()) {
                            soundManager.playSteelHit(); // ← SONIDO acero
                        } else if (!block.isAlive() && wasAlive) {
                            // Bloque destruido
                            blocksDestroyed++;
                            score += block.getPoints();
                            soundManager.playBlockBreak(); // ← SONIDO destrucción
                            spriteSheet.startExplosion(block.getCenterX(), block.getCenterY());

                            // ── VELOCIDAD PROGRESIVA ──────────────────────
                            // Cada 10 bloques destruidos, aumenta la velocidad
                            if (blocksDestroyed % 10 == 0) {
                                ball.increaseSpeed(1.5f); // +1.5 px por frame
                            }
                        } else {
                            soundManager.playBlockHit(); // ← SONIDO golpe sin destruir
                        }
                    }
                }
            }
        }

        // Pelota perdida
        if (ball.getY() > screenHeight + 50) {
            lives--;
            if (lives <= 0) {
                saveHighScore(score);
                state = GameState.GAME_OVER;
            } else {
                ball.reset(screenWidth / 2f, screenHeight * 0.65f);
                state = GameState.WAITING;
            }
        }

        if (allDestroyed) {
            saveHighScore(score);
            if (levelManager.isLastLevel()) {
                state = GameState.WIN; // completó todos los niveles
            } else {
                levelManager.nextLevel();
                initLevel();           // cargar siguiente nivel sin resetear puntos ni vidas
                state = GameState.WAITING;
            }
        }

        spriteSheet.update();
    }

    // ════════════════════════════════════════════════════════════════════
    // RENDERIZADO
    // ════════════════════════════════════════════════════════════════════
    private void draw() {
        if (getHolder().getSurface().isValid()) {
            Canvas canvas = null;
            try {
                canvas = getHolder().lockCanvas();
                if (canvas == null) return;

                // 1. Fondo con estrellas (siempre lo primero)
                drawBackground(canvas);

                // 2. Bloques
                for (Block[] row : blocks)
                    for (Block b : row)
                        if (b.isAlive()) b.draw(canvas);

                // 3. Paleta
                paddle.draw(canvas);

                // 4. Pelota
                ball.draw(canvas);

                // 5. Explosiones del sprite sheet
                spriteSheet.draw(canvas);

                // 6. HUD (puntuación y vidas, siempre encima de todo)
                drawHUD(canvas);

                // 7. Pantallas de estado
                if (state == GameState.WAITING)   drawMessage(canvas, "Toca para lanzar");
                if (state == GameState.GAME_OVER) drawMessage(canvas, "GAME OVER\nToca para reiniciar");
                if (state == GameState.WIN)       drawMessage(canvas, "¡GANASTE!\nToca para reiniciar");

            } finally {
                if (canvas != null) {
                    getHolder().unlockCanvasAndPost(canvas);
                }
            }
        }
    }
    private void drawBackground(Canvas canvas) {
        canvas.drawColor(Color.parseColor("#0A0A1A"));
        if (stars == null) return;
        Paint starPaint = new Paint();
        starPaint.setColor(Color.parseColor("#AAFFFFFF"));
        starPaint.setStrokeWidth(2);
        for (int i = 0; i < stars.length - 1; i += 2) {
            canvas.drawPoint(stars[i], stars[i+1], starPaint);
        }
    }

    private void drawHUD(Canvas canvas) {
        canvas.drawText("Puntos: " + score, 20, 90, hudPaint);
        canvas.drawText("Récord: " + loadHighScore(), 20, 135, hudPaint);
        canvas.drawText("Nivel: " + levelManager.getCurrentLevelNumber()
                        + "/" + levelManager.getTotalLevels(),
                screenWidth / 2f, 90, hudPaint); // ← centrado
        canvas.drawText("Vidas: " + lives, screenWidth - 200f, 90, hudPaint);
    }

    private void drawMessage(Canvas canvas, String message) {
        Paint overlay = new Paint();
        overlay.setColor(Color.parseColor("#AA000000"));
        canvas.drawRect(0, screenHeight * 0.35f, screenWidth, screenHeight * 0.65f, overlay);

        String[] lines = message.split("\n");
        float y = screenHeight / 2f - (lines.length - 1) * 35f;
        for (String line : lines) {
            canvas.drawText(line, screenWidth / 2f, y, textPaint);
            y += 75;
        }
    }

    // ════════════════════════════════════════════════════════════════════
    // EVENTOS TÁCTILES
    // ════════════════════════════════════════════════════════════════════
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        touchX = event.getX();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (state == GameState.WAITING) {
                    state = GameState.PLAYING;
                } else if (state == GameState.GAME_OVER || state == GameState.WIN) {
                    initGame();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                // Mover paleta siguiendo el dedo
                paddle.moveTo(touchX - paddle.getWidth() / 2f);
                break;
        }
        return true;
    }

    // ════════════════════════════════════════════════════════════════════
    // CICLO DE VIDA DE LA SURFACE
    // ════════════════════════════════════════════════════════════════════

    /**
     * surfaceCreated — La Surface está lista para dibujar.
     * Aquí iniciamos el hilo del juego.
     */
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // OPCIÓN B: Creación del hilo de juego separado del UI Thread.
        // Se instancia pasando 'this' porque GameView implementa Runnable.
        isRunning = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    /**
     * surfaceChanged — Cambio de tamaño/orientación.
     * En este proyecto bloqueamos la orientación, pero se incluye
     * por buenas prácticas.
     */
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // No necesario para este proyecto (orientación fija)
    }

    /**
     * surfaceDestroyed — La Surface está siendo destruida.
     * CRÍTICO: Debemos detener el hilo y esperar a que termine
     * para evitar fugas de memoria y excepciones de Canvas nulo.
     */
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        stopGame();
    }

    /**
     * Detiene el hilo de juego de forma segura.
     * Llamado tanto desde surfaceDestroyed como desde onPause de la Activity.
     */
    public void stopGame() {
        isRunning = false;
        // join() es CRÍTICO: espera a que el hilo termine completamente
        // antes de que Android destruya la Surface. Sin esto → posible crash
        if (gameThread != null) {
            boolean retry = true;
            while (retry) {
                try {
                    gameThread.join(); // Esperar a que el hilo termine completamente
                    retry = false;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            gameThread = null;
        }
    }

    /**
     * Reanuda el juego (llamado desde onResume de la Activity).
     */
    public void resumeGame() {
        if (!isRunning && getHolder().getSurface().isValid()) {
            isRunning = true;
            gameThread = new Thread(this);
            gameThread.start();
        }
    }

    private int loadHighScore() {
        return getContext()
                .getSharedPreferences("arkanoid_prefs", Context.MODE_PRIVATE)
                .getInt("high_score", 0);
    }

    private void saveHighScore(int newScore) {
        int current = loadHighScore();
        if (newScore > current) {
            getContext()
                    .getSharedPreferences("arkanoid_prefs", Context.MODE_PRIVATE)
                    .edit()
                    .putInt("high_score", newScore)
                    .apply();
        }
    }
}
