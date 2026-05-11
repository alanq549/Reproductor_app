package com.alan.reproductor_de_audio;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private MediaPlayer mediaPlayer;
    private Ringtone ringtone;

    private Uri externalSongUri;

    // NUEVO
    private TextView txtSelectedSong;

    // Panels
    private LinearLayout panelAlarmas;
    private LinearLayout panelCancion;
    private LinearLayout panelPropia;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // =========================
        // TABS
        // =========================

        LinearLayout tabAlarmas = findViewById(R.id.tabAlarmas);
        LinearLayout tabCancion = findViewById(R.id.tabCancion);
        LinearLayout tabPropia = findViewById(R.id.tabPropia);

        panelAlarmas = findViewById(R.id.panelAlarmas);
        panelCancion = findViewById(R.id.panelCancion);
        panelPropia = findViewById(R.id.panelPropia);

        tabAlarmas.setOnClickListener(v -> showPanel(panelAlarmas));
        tabCancion.setOnClickListener(v -> showPanel(panelCancion));
        tabPropia.setOnClickListener(v -> showPanel(panelPropia));

        // =========================
        // BOTONES ALARMAS
        // =========================

        Button btnSystem = findViewById(R.id.btnSystem);
        Button btnSystem2 = findViewById(R.id.btnSystem2);
        Button btnSystem3 = findViewById(R.id.btnSystem3);

        btnSystem.setOnClickListener(v ->
                playSystemSound(RingtoneManager.TYPE_NOTIFICATION));

        btnSystem2.setOnClickListener(v ->
                playSystemSound(RingtoneManager.TYPE_ALARM));

        btnSystem3.setOnClickListener(v ->
                playSystemSound(RingtoneManager.TYPE_RINGTONE));

        // =========================
        // CANCIÓN LOCAL (res/raw)
        // =========================

        Button btnLocal = findViewById(R.id.btnLocal);

        btnLocal.setOnClickListener(v -> playLocalSong());

        // =========================
        // ARCHIVO EXTERNO
        // =========================

        Button btnSelect = findViewById(R.id.btnSelect);
        Button btnExternal = findViewById(R.id.btnExternal);

        // NUEVO
        txtSelectedSong =
                findViewById(R.id.txtSelectedSong);

        ActivityResultLauncher<Intent> picker =
                registerForActivityResult(
                        new ActivityResultContracts.StartActivityForResult(),
                        result -> {

                            if (result.getResultCode() == Activity.RESULT_OK
                                    && result.getData() != null) {

                                externalSongUri =
                                        result.getData().getData();

                                // NUEVO
                                String fileName =
                                        externalSongUri.getLastPathSegment();

                                txtSelectedSong.setText(fileName);

                                Toast.makeText(
                                        this,
                                        "Audio seleccionado",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        });

        btnSelect.setOnClickListener(v ->
                openFilePicker(picker));

        btnExternal.setOnClickListener(v ->
                playExternalSong());

        // =========================
        // PAUSA
        // =========================

        Button btnPause = findViewById(R.id.btnPause);

        btnPause.setOnClickListener(v ->
                pauseAudio());
    }

    // =========================
    // CAMBIAR PANELES
    // =========================

    private void showPanel(View panel) {

        panelAlarmas.setVisibility(View.GONE);
        panelCancion.setVisibility(View.GONE);
        panelPropia.setVisibility(View.GONE);

        panel.setVisibility(View.VISIBLE);
    }

    // =========================
    // SONIDOS DEL SISTEMA
    // =========================

    private void playSystemSound(int type) {

        stopAudio();

        Uri uri = RingtoneManager.getDefaultUri(type);

        ringtone = RingtoneManager.getRingtone(
                getApplicationContext(),
                uri
        );

        ringtone.play();

        Toast.makeText(
                this,
                "Sonido del sistema",
                Toast.LENGTH_SHORT
        ).show();
    }

    // =========================
    // CANCIÓN LOCAL RES/RAW
    // =========================

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

    // =========================
    // SELECTOR DE ARCHIVOS
    // =========================

    private void openFilePicker(
            ActivityResultLauncher<Intent> launcher
    ) {

        Intent intent =
                new Intent(Intent.ACTION_OPEN_DOCUMENT);

        intent.addCategory(Intent.CATEGORY_OPENABLE);

        intent.setType("audio/*");

        launcher.launch(intent);
    }

    // =========================
    // REPRODUCIR EXTERNA
    // =========================

    private void playExternalSong() {

        if (externalSongUri == null) {

            Toast.makeText(
                    this,
                    "Selecciona un audio primero",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

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
                    "Audio externo",
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

    // =========================
    // PAUSAR
    // =========================

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

    // =========================
    // DETENER AUDIO
    // =========================

    private void stopAudio() {

        if (mediaPlayer != null) {

            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }

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