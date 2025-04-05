package xeonleon.xeontools;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Process;
import android.os.SystemClock;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;

public class DeviceTestActivity extends AppCompatActivity {

    private TextView cpuInfoText, gpuInfoText, batteryInfoText;
    private Handler handler = new Handler();
    private Runnable updateRunnable;
    private long lastCpuTime;
    private long lastAppCpuTime;
    private int cpuUsage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_test);

        // Ukryj ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        cpuInfoText = findViewById(R.id.cpu_info);
        gpuInfoText = findViewById(R.id.gpu_info);
        batteryInfoText = findViewById(R.id.battery_info);

        // Inicjalizacja pierwszych wartości
        lastCpuTime = getTotalCpuTime();
        lastAppCpuTime = getAppCpuTime();

        // Rozpocznij monitoring
        startMonitoring();
    }

    private void startMonitoring() {
        updateRunnable = new Runnable() {
            @Override
            public void run() {
                updateCpuUsage();
                updateBatteryInfo();
                updateGpuInfo();
                handler.postDelayed(this, 1000); // Odświeżaj co 1 sekundę
            }
        };
        handler.post(updateRunnable);
    }

    @SuppressLint("DefaultLocale")
    private void updateCpuUsage() {
        long currentCpuTime = getTotalCpuTime();
        long currentAppCpuTime = getAppCpuTime();

        if (currentCpuTime > lastCpuTime && currentAppCpuTime > lastAppCpuTime) {
            cpuUsage = (int) ((currentAppCpuTime - lastAppCpuTime) * 100L / (currentCpuTime - lastCpuTime));
        }

        lastCpuTime = currentCpuTime;
        lastAppCpuTime = currentAppCpuTime;

        String cpuInfo = String.format("🖥️ CPU Usage: %d%%\nCores: %d\nMax Freq: %.2f GHz",
                cpuUsage,
                Runtime.getRuntime().availableProcessors(),
                getMaxCpuFreq() / 1000000.0);

        cpuInfoText.setText(cpuInfo);
    }

    private long getTotalCpuTime() {
        try (RandomAccessFile reader = new RandomAccessFile("/proc/stat", "r")) {
            String line = reader.readLine();
            String[] values = line.split(" +");

            long total = 0;
            for (int i = 1; i < values.length; i++) {
                total += Long.parseLong(values[i]);
            }
            return total;
        } catch (IOException e) {
            return 0;
        }
    }

    private long getAppCpuTime() {
        return Process.getElapsedCpuTime();
    }

    private long getMaxCpuFreq() {
        try (BufferedReader reader = new BufferedReader(new FileReader("/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq"))) {
            return Long.parseLong(reader.readLine());
        } catch (IOException | NumberFormatException e) {
            return 0;
        }
    }

    private void updateBatteryInfo() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                float batteryPct = level * 100 / (float) scale;

                int temp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) / 10;
                int voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1);

                String health;
                switch (intent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)) {
                    case BatteryManager.BATTERY_HEALTH_GOOD: health = "Good"; break;
                    case BatteryManager.BATTERY_HEALTH_OVERHEAT: health = "Overheat"; break;
                    default: health = "Unknown";
                }

                String info = String.format("🔋 Battery: %.1f%%\nTemp: %d°C\nVoltage: %dmV\nHealth: %s",
                        batteryPct, temp, voltage, health);

                batteryInfoText.setText(info);
            }
        }, filter);
    }

    private void updateGpuInfo() {
        try {
            // Prosta implementacja - na niektórych urządzeniach wymaga OpenGL
            String gpuInfo = "🎮 GPU: " + System.getProperty("ro.hardware", "Unknown") +
                    "\nOpenGL: " + System.getProperty("ro.opengles.version", "Unknown");
            gpuInfoText.setText(gpuInfo);
        } catch (Exception e) {
            gpuInfoText.setText("GPU info unavailable");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(updateRunnable);
    }
}