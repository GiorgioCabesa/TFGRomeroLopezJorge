package com.example.tensorcube;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MenuActivity extends AppCompatActivity {

    Button btn_modelos;

    Button btn_inference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        // Botones XML
        btn_modelos = findViewById(R.id.id_boton_modelos);
        btn_inference = findViewById(R.id.idbtninference);


        String userUid =  getIntent().getStringExtra("user_uid");

        // Listener de botón para ver los modelos
        btn_modelos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(MenuActivity.this, ModelActivity.class)
                        .putExtra("user_uid", userUid));
                Toast.makeText(MenuActivity.this,"Viendo sus modelos", Toast.LENGTH_SHORT).show();

            }
        });

        // Listener de botón para clasifica Modelos
        btn_inference.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(MenuActivity.this, ClassiffierActivity.class));
                Toast.makeText(MenuActivity.this,"Usando su modelo", Toast.LENGTH_SHORT).show();

            }
        });



    }


}