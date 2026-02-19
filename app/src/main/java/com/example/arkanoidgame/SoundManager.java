package com.example.arkanoidgame;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.SoundPool;
import android.media.MediaPlayer;

public class SoundManager {

    private SoundPool soundPool;
    private int idBouncePaddle;
    private int idBounceWall;
    private int idBlockHit;
    private int idBlockBreak;
    private int idSteel;
    private boolean loaded = false;

    public SoundManager(Context context) {
        AudioAttributes attrs = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(6)
                .setAudioAttributes(attrs)
                .build();

        // Cuando todos los sonidos estén cargados, marcar como listo
        soundPool.setOnLoadCompleteListener((sp, sampleId, status) -> {
            if (status == 0) loaded = true;
        });

        // Cargar sonidos desde res/raw/
        idBouncePaddle = soundPool.load(context, R.raw.bounce_paddle, 1);
        idBounceWall   = soundPool.load(context, R.raw.bounce_wall,   1);
        idBlockHit     = soundPool.load(context, R.raw.block_hit,     1);
        idBlockBreak   = soundPool.load(context, R.raw.block_break,   1);
        idSteel        = soundPool.load(context, R.raw.steel_hit,     1);
    }

    // leftVolume y rightVolume: 0.0 a 1.0
    // priority: 1 (normal)
    // loop: 0 (sin bucle)
    // rate: 1.0 (velocidad normal)
    public void playBouncePaddle() { play(idBouncePaddle, 1.0f); }
    public void playBounceWall()   { play(idBounceWall,   0.8f); }
    public void playBlockHit()     { play(idBlockHit,     0.9f); }
    public void playBlockBreak()   { play(idBlockBreak,   1.0f); }
    public void playSteelHit()     { play(idSteel,        1.0f); }

    private void play(int soundId, float volume) {
        if (loaded && soundId != 0) {
            soundPool.play(soundId, volume, volume, 1, 0, 1.0f);
        }
    }

    public void release() {
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }
}