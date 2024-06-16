package com.example.tensorcube;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;



public class LoginActivity extends AppCompatActivity {


    // Declaración de variables para los elementos de la interfaz de usuario
    Button btn_login;
    EditText email, password;

    // Declaración de la instancia de autenticación de Firebase
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        // Obtenemos los elementos xml del layout
        email = findViewById(R.id.correo);
        password = findViewById(R.id.contrasena);
        btn_login = findViewById(R.id.btn_inicio_sesion);
        mAuth = FirebaseAuth.getInstance();

        // Establece un listener para el botón de inicio de sesión
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Obtenemos el email y la contraseña ingresados por el usuario
                String emailUser = email.getText().toString().trim();
                String passUser = password.getText().toString().trim();

                // Verifica que los campos no estén vacíos
                if(emailUser.isEmpty() && passUser.isEmpty()){
                    Toast.makeText(LoginActivity.this,"Rellena los datos", Toast.LENGTH_SHORT).show();
                }else{
                    // Llama al método loginUser para autenticar al usuario
                    loginUser(emailUser, passUser);
                }


            }
        });

    }


    // Método para autenticar al usuario con Firebase
    private void loginUser(String emailUser, String passUser) {

        // Intenta iniciar sesión con el email y la contraseña proporcionado
        mAuth.signInWithEmailAndPassword(emailUser, passUser).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if(task.isSuccessful()){
                    // Si la autenticación es exitosa, obtenemos el usuario actual de Firebase
                    FirebaseUser user = mAuth.getCurrentUser();

                    assert user != null;
                    String userUid = user.getUid();

                    // Finaliza la actividad actual y abre la actividad MenuActivity
                    finish();
                    startActivity(new Intent(LoginActivity.this, MenuActivity.class)
                            .putExtra("user_uid", userUid));

                    // Mostramos mensajes
                    Toast.makeText(LoginActivity.this,"Bienvenido", Toast.LENGTH_SHORT).show();


                }else{
                    Toast.makeText(LoginActivity.this,"Error al iniciar sesión", Toast.LENGTH_SHORT).show();

                }

            }
            // Si hay algún error mostramos un mensaje de error
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(LoginActivity.this,"Error al iniciar sesión", Toast.LENGTH_SHORT).show();


            }
        });


    }
}
