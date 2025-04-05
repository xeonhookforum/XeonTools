package xeonleon.xeontools;

import android.app.Activity;
import android.app.ActivityManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Build;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;
import android.content.Intent;

public class MainActivity extends Activity {

    private TextView phoneInfoText;
    private TextView networkInfoText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        if (!PermissionsUtils.checkAndRequestPermissions(this)) {
            Toast.makeText(this, "Without some permissions, the app will not work. Please grant them in the settings.",
                    Toast.LENGTH_LONG).show();
        }

        // Initialize views
        phoneInfoText = (TextView) findViewById(R.id.textview1);
        networkInfoText = (TextView) findViewById(R.id.textview2);
        Button refreshButton = (Button) findViewById(R.id.refreshButton);
        Button aboutButton = (Button) findViewById(R.id.aboutButton);
        Button toolsButton = (Button) findViewById(R.id.toolsButton);
        // First load
        refreshAllInfo();

        // Refresh button click
        refreshButton.setOnClickListener(new View.OnClickListener() {
                                             @Override
                                             public void onClick(View v) {
                                                 refreshAllInfo();
                                             }
                                         });
        aboutButton.setOnClickListener(new View.OnClickListener() {
                                           @Override
                                           public void onClick(View v) {
                                               startActivity(new Intent(MainActivity.this, AboutActivity.class));
                                           }
                                       });
        toolsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(MainActivity.this, ToolsActivity.class));
                }
        });

    }

    private void refreshAllInfo() {
        // Show loading state
        phoneInfoText.setText("Loading phone info...");
        networkInfoText.setText("Loading network info...");

        // Delay slightly for better UX
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                phoneInfoText.setText(getPhoneInfo());
                networkInfoText.setText(getNetworkInfo());
            }
        }, 300);
    }

    private String getPhoneInfo() {
        // Phone information
        String model = Build.MODEL;
        String manufacturer = Build.MANUFACTURER;
        String androidVersion = Build.VERSION.RELEASE;
        int sdkVersion = Build.VERSION.SDK_INT;

        // RAM information in MB
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        ((ActivityManager) getSystemService(ACTIVITY_SERVICE)).getMemoryInfo(mi);
        long totalRamMB = mi.totalMem / (1024 * 1024);
        long freeRamMB = mi.availMem / (1024 * 1024);

        // SIM information
        String sim1 = "N/A";
        try {
            TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
            sim1 = tm.getSimOperatorName();
        } catch (Exception e) {
            sim1 = "Permission needed";
        }

        return "------PHONE------\n" +
                "ID: " + Build.ID + "\n" +
                "MODEL: " + model + "\n" +
                "PRODUCER: " + manufacturer + "\n" +
                "SIM: " + sim1 + "\n" +
                "RAM: " + totalRamMB + " MB\n" +
                "RAM FREE: " + freeRamMB + " MB\n" +
                "------ANDROID------\n" +
                "VER: " + androidVersion + "\n" +
                "SDK: " + sdkVersion;
    }

    private String getNetworkInfo() {
        // Network information
        String ipv4 = getIPAddress(true);
        String ipv6 = getIPAddress(false);
        String ssid = "N/A";
        String wifiStrength = "N/A";
        String cellStrength = "N/A";

        try {
            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            ssid = wifiInfo.getSSID().replace("\"", "");
            wifiStrength = wifiInfo.getRssi() + " dBm (" + getSignalQuality(wifiInfo.getRssi()) + ")";

            TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                cellStrength = tm.getSignalStrength().getCellSignalStrengths().get(0).getDbm() + " dBm";
            }
        } catch (Exception e) {
            // Ignore errors
        }

        return "------NETWORK------\n" +
                "IPV4: " + ipv4 + "\n" +
                "IPV6: " + ipv6 + "\n" +
                "WIFI SSID: " + ssid + "\n" +
                "WIFI Power: " + wifiStrength + "\n" +
                "CELL POWER: " + cellStrength;
    }

    private String getSignalQuality(int rssi) {
        if (rssi >= -50) return "Excellent";
        if (rssi >= -60) return "Very Good";
        if (rssi >= -70) return "Good";
        if (rssi >= -80) return "Weak";
        return "Poor";
    }

    private String getIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        boolean isIPv4 = sAddr.indexOf(':') < 0;
                        if (useIPv4) {
                            if (isIPv4) return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%');
                                return delim < 0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Ignore
        }
        return "N/A";
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (!PermissionsUtils.onRequestPermissionsResult(requestCode, permissions, grantResults)) {
            Toast.makeText(this, "Some permissions was denied. App will have some functionality disabled.",
                    Toast.LENGTH_SHORT).show();
        } else {
            refreshAllInfo(); // Odśwież dane po uzyskaniu uprawnień
        }
    }



}