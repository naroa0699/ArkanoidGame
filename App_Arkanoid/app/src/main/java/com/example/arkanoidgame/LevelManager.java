package com.example.arkanoidgame;

public class LevelManager {

    // Cada nivel es una matriz de enteros:
    // 0 = sin bloque
    // 1 = bloque azul    (1 punto)
    // 2 = bloque verde   (1 punto)
    // 3 = bloque amarillo(2 puntos)
    // 4 = bloque naranja (2 puntos)
    // 5 = bloque rojo    (3 puntos, 2 golpes)
    // 9 = bloque acero   (indestructible)

    private static final int[][][] LEVELS = {

            // ── NIVEL 1: Clásico ────────────────────────────────────────
            {
                    { 1, 1, 1, 1, 1, 1, 1, 1 },
                    { 2, 2, 2, 2, 2, 2, 2, 2 },
                    { 3, 3, 3, 3, 3, 3, 3, 3 },
                    { 4, 4, 4, 4, 4, 4, 4, 4 },
                    { 5, 5, 5, 5, 5, 5, 5, 5 },
            },

            // ── NIVEL 2: Pirámide ────────────────────────────────────────
            {
                    { 0, 0, 0, 5, 5, 0, 0, 0 },
                    { 0, 0, 4, 4, 4, 4, 0, 0 },
                    { 0, 3, 3, 3, 3, 3, 3, 0 },
                    { 2, 2, 2, 2, 2, 2, 2, 2 },
                    { 1, 1, 1, 1, 1, 1, 1, 1 },
            },

            // ── NIVEL 3: Fortaleza de acero ──────────────────────────────
            {
                    { 9, 1, 1, 1, 1, 1, 1, 9 },
                    { 1, 9, 3, 3, 3, 3, 9, 1 },
                    { 1, 3, 9, 5, 5, 9, 3, 1 },
                    { 1, 3, 9, 5, 5, 9, 3, 1 },
                    { 9, 1, 1, 1, 1, 1, 1, 9 },
            },

            // ── NIVEL 4: Tablero de ajedrez ──────────────────────────────
            {
                    { 5, 0, 5, 0, 5, 0, 5, 0 },
                    { 0, 4, 0, 4, 0, 4, 0, 4 },
                    { 3, 0, 9, 0, 9, 0, 3, 0 },
                    { 0, 2, 0, 4, 0, 4, 0, 2 },
                    { 1, 0, 1, 0, 1, 0, 1, 0 },
            },

            // ── NIVEL 5: Caos total ──────────────────────────────────────
            {
                    { 9, 5, 9, 5, 9, 5, 9, 5 },
                    { 5, 4, 4, 4, 4, 4, 4, 9 },
                    { 9, 4, 9, 3, 3, 9, 4, 5 },
                    { 5, 4, 3, 9, 9, 3, 4, 9 },
                    { 9, 5, 9, 5, 9, 5, 9, 5 },
            },
    };

    private int currentLevel = 0;

    public int[][] getCurrentLayout() {
        return LEVELS[currentLevel];
    }

    public void nextLevel() {
        currentLevel++;
        if (currentLevel >= LEVELS.length) {
            currentLevel = 0; // vuelve al nivel 1 si completa todos
        }
    }

    public int getCurrentLevelNumber() {
        return currentLevel + 1; // mostrar al usuario empezando por 1
    }

    public int getTotalLevels() {
        return LEVELS.length;
    }

    public boolean isLastLevel() {
        return currentLevel == LEVELS.length - 1;
    }

    public void reset() {
        currentLevel = 0;
    }
}
