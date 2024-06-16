package com.example.tensorcube;

import android.app.ProgressDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyviewHolder> {

    Context context;
    ArrayList<User> list;
    String userUid;
    private ProgressDialog progressDialog;
    private boolean isDownloading = false;

    public MyAdapter(Context context, ArrayList<User> list, String userUid) {
        this.context = context;
        this.list = list;
        this.userUid = userUid;
        this.progressDialog = new ProgressDialog(context);
        this.progressDialog.setMessage("Descargando...");
        this.progressDialog.setCancelable(false);
    }

    @NonNull
    @Override
    public MyviewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.card_view, parent, false);
        return new MyviewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyAdapter.MyviewHolder myviewHolder, int position) {
        User user = list.get(position);
        myviewHolder.modelo.setText(user.getModelo());
        myviewHolder.name.setText(user.getNombre());

        // Concatenar todas las etiquetas en una cadena
        StringBuilder labels = new StringBuilder();
        for (String label : user.getLabel()) {
            labels.append(label).append(", ");
        }

        // Eliminar la última coma y espacio
        if (labels.length() > 0) {
            labels.setLength(labels.length() - 2);
        }

        myviewHolder.label.setText(labels.toString());

        // Agregar oyente de clics a la tarjeta
        myviewHolder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isDownloading) {
                    isDownloading = true;
                    progressDialog.show();
                    // Obtener el modelo seleccionado
                    User selectedUser = list.get(myviewHolder.getAdapterPosition());

                    // Llamar al método para descargar y guardar el archivo y etiquetas
                    downloadAndSaveFile(selectedUser);
                    saveLabelsToFile(selectedUser.getLabel());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    // Método para obtener los datos del Model
    public static class MyviewHolder extends RecyclerView.ViewHolder {
        TextView name, modelo, label;
        CardView cardView;

        public MyviewHolder(@NonNull View itemView) {
            // Obtenemos cada dato necesario para mostrarlo en el ModelActivity
            super(itemView);
            name = itemView.findViewById(R.id.textname);
            modelo = itemView.findViewById(R.id.textmodelo);
            label = itemView.findViewById(R.id.textlabel);
            cardView = itemView.findViewById(R.id.cardView);
        }
    }

    private void downloadAndSaveFile(User user) {
        String filePath = "usuarios/" + userUid + "/" + user.getKey() + "/" + user.getModelo();
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(filePath);

        // Preparar la ruta local para guardar el archivo descargado
        File localFile = new File(context.getFilesDir(), user.getModelo());

        storageRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                // Archivo descargado con éxito
                Toast.makeText(context, "Archivo descargado: " + localFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                listFilesInDir(context.getFilesDir());  // Método para listar archivos
                progressDialog.dismiss();
                isDownloading = false;
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Error durante la descarga
                Toast.makeText(context, "Error al descargar el archivo", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
                isDownloading = false;
            }
        });
    }

    private void saveLabelsToFile(ArrayList<String> labels) {
        File labelFile = new File(context.getFilesDir(), "labels.txt");
        try (FileWriter writer = new FileWriter(labelFile)) {
            for (String label : labels) {
                writer.write(label + "\n");
            }
            Toast.makeText(context, "Etiquetas guardadas en: " + labelFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(context, "Error al guardar las etiquetas", Toast.LENGTH_SHORT).show();
        }
    }

    private void listFilesInDir(File directory) {
        File[] files = directory.listFiles();
        for (File file : files) {
            System.out.println("File List " + "Archivo: " + file.getName());
        }
    }

    public boolean isDownloading() {
        return isDownloading;
    }
}
