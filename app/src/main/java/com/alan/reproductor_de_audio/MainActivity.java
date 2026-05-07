package com.alan.reproductor_de_audio;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    // El objeto MediaPlayer es el encargado de reproducir el audio
    private MediaPlayer mediaPlayer;

    // URIs para guardar la dirección de las canciones seleccionadas del teléfono
    private Uri uriSong1, uriSong2;

    // Botones de reproducción (se activan solo cuando hay una canción seleccionada)
    private Button btnPlay1, btnPlay2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializar componentes de la UI
        Button btnInternal = findViewById(R.id.btnInternal);
        Button btnSelect1 = findViewById(R.id.btnSelectSong1);
        Button btnSelect2 = findViewById(R.id.btnSelectSong2);
        btnPlay1 = findViewById(R.id.btnPlaySong1);
        btnPlay2 = findViewById(R.id.btnPlaySong2);
        Button btnPause = findViewById(R.id.btnPause);

        // --- 1. CONFIGURAR SELECCIÓN DE ARCHIVOS ---
        // Launcher para abrir el selector de documentos y obtener la URI de la canción 1
        ActivityResultLauncher<Intent> picker1 = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        uriSong1 = result.getData().getData();
                        btnPlay1.setEnabled(true); // Habilitamos el botón de Play
                        Toast.makeText(this, "Canción 1 cargada", Toast.LENGTH_SHORT).show();
                    }
                });

        // Launcher para la canción 2
        ActivityResultLauncher<Intent> picker2 = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        uriSong2 = result.getData().getData();
                        btnPlay2.setEnabled(true);
                        Toast.makeText(this, "Canción 2 cargada", Toast.LENGTH_SHORT).show();
                    }
                });

        // --- 2. ASIGNAR EVENTOS A LOS BOTONES ---

        // Audio Interno (Requiere que crees res/raw/audio_interno.mp3)
        btnInternal.setOnClickListener(v -> playInternal());

        // Selectores
        btnSelect1.setOnClickListener(v -> openFilePicker(picker1));
        btnSelect2.setOnClickListener(v -> openFilePicker(picker2));

        // Reproductores de canciones externas
        btnPlay1.setOnClickListener(v -> playExternal(uriSong1, "Canción 1"));
        btnPlay2.setOnClickListener(v -> playExternal(uriSong2, "Canción 2"));

        // Botón Pausa
        btnPause.setOnClickListener(v -> {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                Toast.makeText(this, "Pausado", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Método para abrir el explorador de archivos del sistema
    private void openFilePicker(ActivityResultLauncher<Intent> launcher) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("audio/*"); // Filtrar solo archivos de audio
        launcher.launch(intent);
    }

    // Reproduce el audio que está dentro de la carpeta res/raw
    private void playInternal() {
        stopAndRelease(); // Detener cualquier audio previo
        // R.raw.audio_interno es el ID del recurso
        mediaPlayer = MediaPlayer.create(this, R.raw.audio_interno);
        if (mediaPlayer != null) {
            mediaPlayer.start();
            Toast.makeText(this, "Reproduciendo Interno", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Error: ¿Olvidaste añadir audio_interno.mp3 a res/raw?", Toast.LENGTH_LONG).show();
        }
    }

    // Reproduce un audio desde una URI (almacenamiento externo)
    private void playExternal(Uri uri, String nombre) {
        if (uri == null) return;
        stopAndRelease();
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(this, uri);
            mediaPlayer.prepare(); // Importante para archivos externos
            mediaPlayer.start();
            Toast.makeText(this, "Reproduciendo: " + nombre, Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, "Error al cargar el archivo", Toast.LENGTH_SHORT).show();
        }
    }

    // Libera los recursos del MediaPlayer para no gastar memoria
    private void stopAndRelease() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAndRelease(); // Limpiar al cerrar la app
    }
}
