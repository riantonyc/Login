package com.ryan.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class DashboardActivity extends AppCompatActivity {

    private LinearLayout menuForex, menuMahasiswa, menuCuaca, menuPpv; // Tambahan menuPpv
    private TextView tvWelcomeDashboard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);

        // Menata posisi padding agar presisi di layar HP
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.dashboard_main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Hubungkan teks sambutan nama user real
        tvWelcomeDashboard = findViewById(R.id.tvWelcomeDashboard); // Diperbaiki agar tidak variable shadowing
        String namaUserReal = getIntent().getStringExtra("KEY_USER");
        if (namaUserReal != null && tvWelcomeDashboard != null) {
            tvWelcomeDashboard.setText("Selamat Datang, " + namaUserReal);
        }

        // Inisialisasi komponen Layout Menu berdasarkan ID XML Anda
        menuForex = findViewById(R.id.menuForex);
        menuMahasiswa = findViewById(R.id.menuMahasiswa);
        menuCuaca = findViewById(R.id.menuCuaca);
        menuPpv = findViewById(R.id.menuPpv); // Inisialisasi menu PPV

        // =========================================================================
        // AKSI KLIK MENU FOREX
        // =========================================================================
        if (menuForex != null) {
            menuForex.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(DashboardActivity.this, ForexActivity.class);
                    startActivity(intent);
                }
            });
        }

        // 2. Aksi Klik Menu Master Mahasiswa
        if (menuMahasiswa != null) {
            menuMahasiswa.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(DashboardActivity.this, MahasiswaActivity.class);
                    startActivity(intent);
                }
            });
        }

        // 3. Aksi Klik Menu Cuaca
        if (menuCuaca != null) {
            menuCuaca.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(DashboardActivity.this, CuacaActivity.class);
                    startActivity(intent);
                }
            });
        }

        // 4. Aksi Klik Menu PPV Boxing (BARU)
        if (menuPpv != null) {
            menuPpv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Berpindah ke halaman PpvActivity
                    Intent intent = new Intent(DashboardActivity.this, PpvActivity.class);
                    startActivity(intent);
                }
            });
        }
    }
}