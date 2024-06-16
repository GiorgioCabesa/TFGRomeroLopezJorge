package com.example.tensorcube;

// Importaciones necesarias para la actividad
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ModelActivity extends AppCompatActivity {

    // Declaración de variables
    RecyclerView recyclerView; // RecyclerView para mostrar una lista de elementos
    ArrayList<User> list; // Lista que contendrá objetos User
    DatabaseReference databaseReference; // Referencia a la base de datos de Firebase
    MyAdapter adapter; // Adaptador para el RecyclerView

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_models); // Establece el layout de la actividad

        // Inicializa el RecyclerView
        recyclerView = findViewById(R.id.recycleview);

        // Obtiene el UID del usuario desde el Intent que lanzó esta actividad
        String userUid = getIntent().getStringExtra("user_uid");

        // Establece la referencia a la base de datos Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("usuarios").child(userUid).child("modelos");

        // Inicializa la lista de usuarios
        list = new ArrayList<>();

        // Configura el RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Inicializa el adaptador con el contexto, la lista y el UID del usuario
        adapter = new MyAdapter(this, list, userUid);

        // Asigna el adaptador al RecyclerView
        recyclerView.setAdapter(adapter);

        // Añade un ValueEventListener para escuchar cambios en la base de datos
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Itera sobre todos los hijos del DataSnapshot
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    // Convierte cada DataSnapshot a un objeto User
                    User user = dataSnapshot1.getValue(User.class);
                    if (user != null) {
                        // Establece la clave del modelo en el objeto User
                        String modelKey = dataSnapshot1.getKey();
                        user.setKey(modelKey);

                        // Añade el objeto User a la lista
                        list.add(user);
                    }
                }
                // Notifica al adaptador que los datos han cambiado para refrescar la vista
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Manejo de posibles errores al acceder a la base de datos
            }
        });
    }

    @Override
    public void onBackPressed() {
        // Obtiene el adaptador del RecyclerView
        MyAdapter adapter = (MyAdapter) recyclerView.getAdapter();
        // Si el adaptador no es nulo y no se está descargando, permite el retroceso
        if (adapter != null && !adapter.isDownloading()) {
            super.onBackPressed();
        }
    }
}
