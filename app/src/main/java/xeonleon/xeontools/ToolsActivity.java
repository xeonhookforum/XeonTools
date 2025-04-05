package xeonleon.xeontools;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

    public class ToolsActivity extends AppCompatActivity {
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.tools);

            Button backAbout = findViewById(R.id.backTools);
            backAbout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish(); // ZAMKNIJ AboutActivity i wróć do MainActivity
                }
            });
            Button devicetest = findViewById(R.id.device_test_button);
            devicetest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ToolsActivity.this, DeviceTestActivity.class);
                    startActivity(intent);
                    finish();
                }
            });
        }
    }

