package com.example.coflash;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;


public class Setting extends Activity {
    private Button noticeSetting, termOfUse, accountSafety;
    private ImageButton back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        noticeSetting = findViewById(R.id.noticeSetting);
        termOfUse = findViewById(R.id.termOfUse);
        accountSafety = findViewById(R.id.accountSafety);
        back = findViewById(R.id.back);

        noticeSetting.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                opennoticeSetting();
            }
        });
        termOfUse.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                openTermOfUse();
            }
        });
        accountSafety.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                openAccountSafety();
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });
    }
        private void opennoticeSetting () {
            Intent intent = new Intent(this, NoticeSetting.class);
            startActivity(intent);
        }
        private void openTermOfUse () {
            Intent intent = new Intent(this, TermOfUse.class);
            startActivity(intent);
        }
        private void openAccountSafety () {
            Intent intent = new Intent(this, AccountSafety.class);
            startActivity(intent);
        }
        private void openback () {
            Intent intent = new Intent(this, Myinfo.class);
            startActivity(intent);

    }
}

