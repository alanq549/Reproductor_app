package com.alan.reproductor_de_audio;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private MediaPlayer mediaPlayer;

    // URI de la canción externa
    private Uri externalSongUri;

    // Sonido del sistema
    private Ringtone ringtone;

    private Button btnExternal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnSystem = findViewById(R.id.btnSystem);
        Button btnLocal = findViewById(R.id.btnLocal);
        Button btnSelect = findViewById(R.id.btnSelect);
        btnExternal = findViewById(R.id.btnExternal);
        Button btnPause = findViewById(R.id.btnPause);

        // Selector de archivo
        ActivityResultLauncher<Intent> picker =
                registerForActivityResult(
                        new ActivityResultContracts.StartActivityForResult(),
                        result -> {
                            if (result.getResultCode() == Activity.RESULT_OK
                                    && result.getData() != null) {

                                externalSongUri = result.getData().getData();

                                btnExternal.setEnabled(true);

                                Toast.makeText(
                                        this,
                                        "Canción seleccionada",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        });

        // SONIDO DEL SISTEMA
        btnSystem.setOnClickListener(v -> playSystemSound());

        // CANCIÓN LOCAL
        btnLocal.setOnClickListener(v -> playLocalSong());

        // SELECCIONAR EXTERNA
        btnSelect.setOnClickListener(v -> openFilePicker(picker));

        // REPRODUCIR EXTERNA
        btnExternal.setOnClickListener(v -> playExternalSong());

        // PAUSA
        btnPause.setOnClickListener(v -> pauseAudio());
    }

    // SONIDO DEL SISTEMA
    private void playSystemSound() {

        stopAudio();

        Uri notificationUri = RingtoneManager.getDefaultUri(
                RingtoneManager.TYPE_NOTIFICATION
        );

        ringtone = RingtoneManager.getRingtone(
                getApplicationContext(),
                notificationUri
        );

        ringtone.play();

        Toast.makeText(
                this,
                "Sonido del sistema",
                Toast.LENGTH_SHORT
        ).show();
    }

    // CANCIÓN LOCAL EN res/raw
    private void playLocalSong() {

        stopAudio();

        mediaPlayer = MediaPlayer.create(
                this,
                R.raw.radiohead_karma_police
        );

        mediaPlayer.start();

        Toast.makeText(
                this,
                "Canción local",
                Toast.LENGTH_SHORT
        ).show();
    }

    // ABRIR SELECTOR
    private void openFilePicker(
            ActivityResultLauncher<Intent> launcher
    ) {

        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

        intent.addCategory(Intent.CATEGORY_OPENABLE);

        intent.setType("audio/*");

        launcher.launch(intent);
    }

    // CANCIÓN EXTERNA
    private void playExternalSong() {

        if (externalSongUri == null) return;

        stopAudio();

        try {

            mediaPlayer = new MediaPlayer();

            mediaPlayer.setDataSource(
                    this,
                    externalSongUri
            );

            mediaPlayer.prepare();

            mediaPlayer.start();

            Toast.makeText(
                    this,
                    "Canción externa",
                    Toast.LENGTH_SHORT
            ).show();

        } catch (IOException e) {

            Toast.makeText(
                    this,
                    "Error al reproducir",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    // PAUSAR
    private void pauseAudio() {

        if (mediaPlayer != null
                && mediaPlayer.isPlaying()) {

            mediaPlayer.pause();
        }

        if (ringtone != null
                && ringtone.isPlaying()) {

            ringtone.stop();
        }
    }

    // DETENER Y LIBERAR
    private void stopAudio() {

        if (mediaPlayer != null) {

            mediaPlayer.stop();

            mediaPlayer.release();

            mediaPlayer = null;
        }

        if (ringtone != null
                && ringtone.isPlaying()) {

            ringtone.stop();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        stopAudio();
    }

}
