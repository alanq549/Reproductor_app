package com.alan.reproductor_de_audio;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
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
import androidx.core.content.ContextCompat;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private MediaPlayer mediaPlayer;
    private Ringtone ringtone;

    private Uri externalSongUri;

    private TextView txtSelectedSong;

    // Panels
    private LinearLayout panelAlarmas;
    private LinearLayout panelCancion;
    private LinearLayout panelPropia;

    // Tabs
    private LinearLayout tabAlarmas;
    private LinearLayout tabCancion;
    private LinearLayout tabPropia;

    // System Sound Containers
    private LinearLayout containerSystem1;
    private LinearLayout containerSystem2;
    private LinearLayout containerSystem3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // =========================
        // TABS
        // =========================

        tabAlarmas = findViewById(R.id.tabAlarmas);
        tabCancion = findViewById(R.id.tabCancion);
        tabPropia = findViewById(R.id.tabPropia);

        panelAlarmas = findViewById(R.id.panelAlarmas);
        panelCancion = findViewById(R.id.panelCancion);
        panelPropia = findViewById(R.id.panelPropia);

        tabAlarmas.setOnClickListener(v -> showPanel(panelAlarmas, tabAlarmas));
        tabCancion.setOnClickListener(v -> showPanel(panelCancion, tabCancion));
        tabPropia.setOnClickListener(v -> showPanel(panelPropia, tabPropia));

        // Inicializar UI de tabs
        updateTabsUI(tabAlarmas);

        // =========================
        // BOTONES Y CONTENEDORES ALARMAS
        // =========================

        containerSystem1 = findViewById(R.id.containerSystem1);
        containerSystem2 = findViewById(R.id.containerSystem2);
        containerSystem3 = findViewById(R.id.containerSystem3);

        Button btnSystem = findViewById(R.id.btnSystem);
        Button btnSystem2 = findViewById(R.id.btnSystem2);
        Button btnSystem3 = findViewById(R.id.btnSystem3);

        btnSystem.setOnClickListener(v ->
                playSystemSound(RingtoneManager.TYPE_NOTIFICATION, containerSystem1));

        btnSystem2.setOnClickListener(v ->
                playSystemSound(RingtoneManager.TYPE_ALARM, containerSystem2));

        btnSystem3.setOnClickListener(v ->
                playSystemSound(RingtoneManager.TYPE_RINGTONE, containerSystem3));

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
        txtSelectedSong = findViewById(R.id.txtSelectedSong);

        ActivityResultLauncher<Intent> picker =
                registerForActivityResult(
                        new ActivityResultContracts.StartActivityForResult(),
                        result -> {
                            if (result.getResultCode() == Activity.RESULT_OK
                                    && result.getData() != null) {
                                externalSongUri = result.getData().getData();
                                String fileName = externalSongUri.getLastPathSegment();
                                if (fileName != null && fileName.contains(":")) {
                                    fileName = fileName.substring(fileName.lastIndexOf(":") + 1);
                                }
                                txtSelectedSong.setText(fileName != null ? fileName : "Audio seleccionado");
                            }
                        });

        btnSelect.setOnClickListener(v -> openFilePicker(picker));
        btnExternal.setOnClickListener(v -> playExternalSong());

        // =========================
        // PAUSA
        // =========================

        Button btnPause = findViewById(R.id.btnPause);
        btnPause.setOnClickListener(v -> pauseAudio());
    }

    // =========================
    // CAMBIAR PANELES Y TABS
    // =========================

    private void showPanel(View panel, LinearLayout activeTab) {
        panelAlarmas.setVisibility(View.GONE);
        panelCancion.setVisibility(View.GONE);
        panelPropia.setVisibility(View.GONE);

        panel.setVisibility(View.VISIBLE);
        updateTabsUI(activeTab);
    }

    private void updateTabsUI(LinearLayout activeTab) {
        setTabState(tabAlarmas, activeTab == tabAlarmas);
        setTabState(tabCancion, activeTab == tabCancion);
        setTabState(tabPropia, activeTab == tabPropia);
    }

    private void setTabState(LinearLayout tab, boolean isActive) {
        TextView label = (TextView) tab.getChildAt(1);
        if (isActive) {
            tab.setBackgroundResource(R.drawable.tab_active_bg);
            label.setTextColor(ContextCompat.getColor(this, R.color.pink_text));
            label.setTypeface(null, Typeface.BOLD);
        } else {
            tab.setBackground(null);
            label.setTextColor(ContextCompat.getColor(this, R.color.pink_muted));
            label.setTypeface(null, Typeface.NORMAL);
        }
    }

    // =========================
    // REPRODUCCIÓN Y OTROS
    // =========================

    private void playSystemSound(int type, View container) {
        stopAudio();
        updateSystemSoundsUI(container);
        Uri uri = RingtoneManager.getDefaultUri(type);
        ringtone = RingtoneManager.getRingtone(getApplicationContext(), uri);
        if (ringtone != null) {
            ringtone.play();
        }
    }

    private void updateSystemSoundsUI(View activeContainer) {
        // Solo aplica background si es el activo, de lo contrario null (0)
        containerSystem1.setBackgroundResource(activeContainer == containerSystem1 ? R.drawable.card_bg : 0);
        containerSystem2.setBackgroundResource(activeContainer == containerSystem2 ? R.drawable.card_bg : 0);
        containerSystem3.setBackgroundResource(activeContainer == containerSystem3 ? R.drawable.card_bg : 0);
    }

    private void playLocalSong() {
        stopAudio();
        mediaPlayer = MediaPlayer.create(this, R.raw.radiohead_karma_police);
        if (mediaPlayer != null) {
            mediaPlayer.start();
        }
    }

    private void openFilePicker(ActivityResultLauncher<Intent> launcher) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("audio/*");
        launcher.launch(intent);
    }

    private void playExternalSong() {
        if (externalSongUri == null) {
            Toast.makeText(this, "Selecciona un audio primero", Toast.LENGTH_SHORT).show();
            return;
        }
        stopAudio();
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(this, externalSongUri);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            Toast.makeText(this, "Error al reproducir", Toast.LENGTH_SHORT).show();
        }
    }

    private void pauseAudio() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
        if (ringtone != null && ringtone.isPlaying()) {
            ringtone.stop();
            updateSystemSoundsUI(null);
        }
    }

    private void stopAudio() {
        updateSystemSoundsUI(null);
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (ringtone != null && ringtone.isPlaying()) {
            ringtone.stop();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAudio();
    }
}
