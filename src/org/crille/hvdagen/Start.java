package org.crille.hvdagen;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


public class Start extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start);

        Button btnMindag = (Button) findViewById(R.id.start_btn_mindag);
        Button btnSchema = (Button) findViewById(R.id.start_btn_schema);
        Button btnTwitter = (Button) findViewById(R.id.start_btn_twitter);
        Button btnLogin = (Button) findViewById(R.id.start_btn_login);
        Button btnPickschema = (Button) findViewById(R.id.start_btn_pickschema);

        btnMindag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startMindag = new Intent(getApplicationContext(), MinDag.class);
                startActivity(startMindag);
            }
        });
        btnSchema.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startSchema = new Intent(getApplicationContext(), Schema.class);
                startActivity(startSchema);
            }
        });
        btnTwitter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startTwitter = new Intent(getApplicationContext(), Twitter.class);
                startActivity(startTwitter);
            }
        });
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startLogin = new Intent(getApplicationContext(), Login.class);
                startActivity(startLogin);
            }
        });
        btnPickschema.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startPick = new Intent(getApplicationContext(), PickSchema.class);
                startActivity(startPick);
            }
        });
    }
}
