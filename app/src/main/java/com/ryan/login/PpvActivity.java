package com.ryan.login;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class PpvActivity extends AppCompatActivity {

    private Button btnBeliTiket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ppv);

        // Menata posisi padding agar presisi di layar HP
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.ppv_main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnBeliTiket = findViewById(R.id.btnBeliTiket);

        // Aksi ketika tombol "Beli Tiket" di pojok kanan bawah ditekan
        if (btnBeliTiket != null) {
            btnBeliTiket.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    tampilkanPopUpPembayaran();
                }
            });
        }
    }

    // Method untuk menampilkan custom dialog (pop-up) pembayaran
    private void tampilkanPopUpPembayaran() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_payment_ppv); // Mengambil layout pop-up kita

        // Membuat background bawaan dialog menjadi transparan agar sudut melengkung CardView terlihat rapi
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }

        // Inisialisasi komponen di dalam pop-up
        EditText etNoHp = dialog.findViewById(R.id.etNoHpPayment);
        Button btnProses = dialog.findViewById(R.id.btnProsesBayar);

        btnProses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String noHp = etNoHp.getText().toString().trim();

                if (noHp.isEmpty()) {
                    Toast.makeText(PpvActivity.this, "Mohon masukkan nomor HP Anda!", Toast.LENGTH_SHORT).show();
                } else {
                    // Tutup pop-up
                    dialog.dismiss();
                    // Tampilkan notifikasi simulasi sukses
                    Toast.makeText(PpvActivity.this, "Berhasil! Memproses tiket untuk nomor: " + noHp, Toast.LENGTH_LONG).show();
                }
            }
        });

        // Tampilkan dialog ke layar
        dialog.show();
    }
}