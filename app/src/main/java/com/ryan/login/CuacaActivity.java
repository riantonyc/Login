package com.ryan.login;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import org.json.JSONArray;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import cz.msebera.android.httpclient.Header;

public class CuacaActivity extends AppCompatActivity {
    private EditText etKota;
    private Button btnTampilkan, btnBack;
    private LinearLayout headerLayout;
    private TextView tvInfoKota, tvTotalRecord;
    private RecyclerView rvCuaca;
    private WebView webViewMaps;
    private SwipeRefreshLayout swipeRefresh;
    private double lat, lon;

    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_CODE = 100;
    private static final String API_KEY = "2fc1bfc247e80a7678b8b6b0f2e00aaa";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cuaca);

        etKota = findViewById(R.id.etKota);
        btnTampilkan = findViewById(R.id.btnTampilkan);
        btnBack = findViewById(R.id.btnBack);
        headerLayout = findViewById(R.id.headerLayout);
        tvInfoKota = findViewById(R.id.tvInfoKota);
        tvTotalRecord = findViewById(R.id.tvTotalRecord);
        rvCuaca = findViewById(R.id.rvCuaca);
        webViewMaps = findViewById(R.id.webViewMaps);
        swipeRefresh = findViewById(R.id.swipeRefresh);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        rvCuaca.setLayoutManager(new LinearLayoutManager(this));

        webViewMaps.getSettings().setJavaScriptEnabled(true);
        webViewMaps.getSettings().setDomStorageEnabled(true);
        webViewMaps.setWebViewClient(new WebViewClient());

        checkLocationPermissionAndFetch();

        swipeRefresh.setOnRefreshListener(() -> {
            String kotaSaatIni = etKota.getText().toString();
            if (!kotaSaatIni.isEmpty()) {
                getCuacaByCity(kotaSaatIni);
            } else if (lat != 0 && lon != 0) {
                getCuacaByCoord(lat, lon);
            } else {
                checkLocationPermissionAndFetch();
            }
        });

        btnTampilkan.setOnClickListener(v -> {
            String kota = etKota.getText().toString();
            if(!kota.isEmpty()) {
                swipeRefresh.setRefreshing(true);
                getCuacaByCity(kota);
            } else {
                Toast.makeText(this, "Masukkan nama kota", Toast.LENGTH_SHORT).show();
            }
        });

        headerLayout.setOnClickListener(v -> {
            if (lat != 0 && lon != 0) {
                showMaps();
            } else {
                Toast.makeText(this, "Tunggu hingga data kota tersedia", Toast.LENGTH_SHORT).show();
            }
        });

        btnBack.setOnClickListener(v -> showListCuaca());
    }

    private void showMaps() {
        swipeRefresh.setVisibility(View.GONE);
        tvTotalRecord.setVisibility(View.GONE);

        webViewMaps.setVisibility(View.VISIBLE);
        btnBack.setVisibility(View.VISIBLE);

        String iframeHtml = "<html><body style='margin:0;padding:0;'>" +
                "<iframe width=\"100%\" height=\"100%\" " +
                "src=\"https://maps.google.com/maps?q=" + lat + "," + lon + "&z=15&output=embed\" " +
                "frameborder=\"0\" scrolling=\"no\" marginheight=\"0\" marginwidth=\"0\"></iframe>" +
                "</body></html>";
        webViewMaps.loadData(iframeHtml, "text/html", "utf-8");
    }

    private void showListCuaca() {
        webViewMaps.setVisibility(View.GONE);
        btnBack.setVisibility(View.GONE);

        swipeRefresh.setVisibility(View.VISIBLE);
        tvTotalRecord.setVisibility(View.VISIBLE);
    }

    private void checkLocationPermissionAndFetch() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CODE);
        } else {
            swipeRefresh.setRefreshing(true);
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    getCuacaByCoord(location.getLatitude(), location.getLongitude());
                } else {
                    Toast.makeText(this, "GPS Emulator belum terkunci. Menampilkan kota default.", Toast.LENGTH_LONG).show();
                    getCuacaByCity("Pontianak");
                }
            }).addOnFailureListener(e -> {
                getCuacaByCity("Pontianak");
                Toast.makeText(this, "Error akses lokasi", Toast.LENGTH_SHORT).show();
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkLocationPermissionAndFetch();
            } else {
                Toast.makeText(this, "Izin lokasi ditolak", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void getCuacaByCity(String kota) {
        String url = "https://api.openweathermap.org/data/2.5/forecast?q=" + kota + "&appid=" + API_KEY;
        fetchWeatherData(url);
    }

    private void getCuacaByCoord(double latitude, double longitude) {
        String url = "https://api.openweathermap.org/data/2.5/forecast?lat=" + latitude + "&lon=" + longitude + "&appid=" + API_KEY;
        fetchWeatherData(url);
    }

    private void fetchWeatherData(String url) {
        AsyncHttpClient client = new AsyncHttpClient();

        client.get(url, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    JSONObject city = response.getJSONObject("city");
                    lat = city.getJSONObject("coord").getDouble("lat");
                    lon = city.getJSONObject("coord").getDouble("lon");

                    String name = city.getString("name");
                    long sunrise = city.getLong("sunrise");
                    long sunset = city.getLong("sunset");

                    // ==========================================================
                    // MENYIMPAN DATA UNTUK WIDGET
                    // ==========================================================
                    android.content.SharedPreferences prefs = getSharedPreferences("WeatherPrefs", MODE_PRIVATE);
                    prefs.edit().putString("LAST_LAT", String.valueOf(lat))
                            .putString("LAST_LON", String.valueOf(lon))
                            .putString("LAST_CITY", name)
                            .apply();

                    // Memicu Widget untuk langsung memperbarui tampilannya
                    android.content.Intent intent = new android.content.Intent(CuacaActivity.this, WeatherWidgetProvider.class);
                    intent.setAction(android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE);
                    int[] ids = android.appwidget.AppWidgetManager.getInstance(getApplication())
                            .getAppWidgetIds(new android.content.ComponentName(getApplication(), WeatherWidgetProvider.class));
                    intent.putExtra(android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
                    sendBroadcast(intent);
                    // ==========================================================

                    // ---> MEMICU POP-UP PEMASANGAN WIDGET OTOMATIS <---
                    mintaPasangWidgetOtomatis();

                    etKota.setText(name);
                    tvInfoKota.setText("KOTA: " + name.toUpperCase() + "\nMATAHARI TERBIT: " + formatWaktu(sunrise) + " (LOKAL)\nMATAHARI TERBENAM: " + formatWaktu(sunset) + " (LOKAL)");

                    List<WeatherItem> listCuaca = new ArrayList<>();
                    JSONArray list = response.getJSONArray("list");

                    for (int i = 0; i < list.length(); i++) {
                        JSONObject item = list.getJSONObject(i);

                        String rawWaktu = item.getString("dt_txt");
                        String waktu = rawWaktu.substring(0, rawWaktu.length() - 3) + " WIB";

                        JSONObject mainObj = item.getJSONObject("main");
                        double tempMin = mainObj.getDouble("temp_min") - 273.15;
                        double tempMax = mainObj.getDouble("temp_max") - 273.15;
                        String suhu = String.format(Locale.US, "%.2f°C - %.2f°C", tempMin, tempMax);

                        JSONObject weather = item.getJSONArray("weather").getJSONObject(0);
                        String mainDesc = weather.getString("main");
                        String desc = weather.getString("description");
                        String icon = weather.getString("icon");

                        listCuaca.add(new WeatherItem(waktu, suhu, mainDesc, desc, icon));
                    }

                    WeatherAdapter adapter = new WeatherAdapter(CuacaActivity.this, listCuaca);
                    rvCuaca.setAdapter(adapter);
                    tvTotalRecord.setText("Total Record : " + listCuaca.size());
                    showListCuaca();

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    swipeRefresh.setRefreshing(false);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                swipeRefresh.setRefreshing(false);
                Toast.makeText(CuacaActivity.this, "Gagal mengambil data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String formatWaktu(long timestamp) {
        return new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date(timestamp * 1000));
    }

    // ==========================================================
    // FITUR AUTO-PIN WIDGET UNTUK ANDROID 8.0 KE ATAS
    // ==========================================================
    private void mintaPasangWidgetOtomatis() {
        android.content.SharedPreferences prefs = getSharedPreferences("WeatherPrefs", MODE_PRIVATE);
        boolean sudahPernahDitanya = prefs.getBoolean("SUDAH_DITAWARKAN_WIDGET", false);

        // Jika sudah pernah ditawarkan, jangan munculkan pop-up lagi
        if (sudahPernahDitanya) return;

        // Memeriksa versi Android (hanya berlaku untuk Android 8.0 Oreo ke atas)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            android.appwidget.AppWidgetManager appWidgetManager = getSystemService(android.appwidget.AppWidgetManager.class);
            android.content.ComponentName myProvider = new android.content.ComponentName(this, WeatherWidgetProvider.class);

            // Jika perangkat mendukung fitur ini, tampilkan dialog pemasangan otomatis
            if (appWidgetManager != null && appWidgetManager.isRequestPinAppWidgetSupported()) {
                appWidgetManager.requestPinAppWidget(myProvider, null, null);

                // Tandai bahwa user sudah mendapat pop-up ini
                prefs.edit().putBoolean("SUDAH_DITAWARKAN_WIDGET", true).apply();
            }
        }
    }
}