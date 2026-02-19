package com.example.arkanoidgame;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private GameView gameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Mantener la pantalla encendida durante el juego
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Inflar el layout XML (obligatorio según el enunciado)
        setContentView(R.layout.activity_main);

        // Obtener dimensiones reales de la pantalla
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int screenWidth  = metrics.widthPixels;
        int screenHeight = metrics.heightPixels;

        // Crear GameView con las dimensiones conocidas
        gameView = new GameView(this, screenWidth, screenHeight);

        // Añadir GameView al contenedor del layout XML
        FrameLayout container = findViewById(R.id.gameContainer);
        container.addView(gameView);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Pausar el juego cuando la Activity pierde el foco
        if (gameView != null) {
            gameView.stopGame();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reanudar el juego cuando la Activity recupera el foco
        if (gameView != null) {
            gameView.resumeGame();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Garantizar limpieza final
        if (gameView != null) {
            gameView.stopGame();
        }
    }
}