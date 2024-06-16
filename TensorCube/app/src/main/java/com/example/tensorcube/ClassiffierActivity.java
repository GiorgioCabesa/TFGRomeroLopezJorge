package com.example.tensorcube;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ImageView;
import android.widget.TextView;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class ClassiffierActivity extends Activity {
    // Declaración de variables
    private Interpreter tflite;
    private TensorImage inputImageBuffer;
    private TensorBuffer outputProbabilityBuffer;

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_GALLERY = 2;
    private static final DataType DATA_TYPE = DataType.FLOAT32;

    private String[] classNames = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            // Cargar los nombres de las clases desde un archivo
            classNames = readLabelsFromFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Inicialización del modelo y la interfaz de usuario
        initializeModel();
        initializeUI();
    }

    private void initializeModel() {
        try {
            // Cargar el modelo y preparar los buffers de entrada y salida
            tflite = new Interpreter(loadModelFile());
            inputImageBuffer = new TensorImage(DATA_TYPE);
            outputProbabilityBuffer = TensorBuffer.createFixedSize(new int[]{1, classNames.length}, DATA_TYPE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initializeUI() {
        // Configurar los botones para capturar imagen y abrir la galería
        findViewById(R.id.button).setOnClickListener(v -> takePicture());
        findViewById(R.id.button2).setOnClickListener(v -> openGallery());
    }

    private void takePicture() {
        // Crear un Intent para capturar una imagen con la cámara
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void openGallery() {
        // Crear un Intent para seleccionar una imagen de la galería
        Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhoto, REQUEST_IMAGE_GALLERY);
    }

    // Se ejecuta la clasificación de imágenes ya sea en una foto con la cámara del móvil o la galería
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Bitmap imageBitmap = null;
            if (requestCode == REQUEST_IMAGE_CAPTURE && data != null && data.getExtras() != null) {
                // Obtener la imagen capturada
                imageBitmap = (Bitmap) data.getExtras().get("data");
            } else if (requestCode == REQUEST_IMAGE_GALLERY && data != null) {
                // Obtener la imagen seleccionada de la galería
                Uri selectedImage = data.getData();
                try {
                    imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (imageBitmap != null) {
                // Mostrar la imagen y comenzar la clasificación
                displayImage(imageBitmap);
                classifyImage(imageBitmap);
            }
        }
    }

    // Mostrar la imagen en un ImageView
    private void displayImage(Bitmap imageBitmap) {
        ((ImageView) findViewById(R.id.imageView)).setImageBitmap(imageBitmap);
    }

    // Escalar la imagen al tamaño esperado por el modelo
    private Bitmap scaleImage(Bitmap image) {
        return Bitmap.createScaledBitmap(image, 224, 224, false);
    }

    private void classifyImage(Bitmap imageBitmap) {
        // Escalar la imagen y clasificarla
        Bitmap scaledImage = scaleImage(imageBitmap);
        String resultText = classifyImageInternal(scaledImage);
        // Mostrar el resultado de la clasificación
        ((TextView) findViewById(R.id.result)).setText(resultText);
    }

    // Método para clasificar la imagen
    private String classifyImageInternal(Bitmap image) {
        // Escalar y normalizar la imagen
        inputImageBuffer.load(image);

        // Ejecutar el modelo
        tflite.run(inputImageBuffer.getBuffer(), outputProbabilityBuffer.getBuffer().rewind());

        // Obtener los resultados
        float[] results = outputProbabilityBuffer.getFloatArray();
        return generateResultText(results);
    }

    // Genera el resultado de la predicción
    private String generateResultText(float[] results) {
        return formatMulticlassClassification(results);
    }

    // Clasificación softmax para clasificación de más de 2 clases (más de una neurona de salida)
    private String formatMulticlassClassification(float[] results) {
        float maxProbability = -1.0f;
        int maxIndex = -1;
        for (int i = 0; i < results.length; i++) {
            float expValue = (float) Math.exp(results[i]);
            if (expValue > maxProbability) {
                maxProbability = expValue;
                maxIndex = i;
            }
        }
        return classNames[maxIndex];
    }

    // Se abre el archivo model.tflite para su uso
    private MappedByteBuffer loadModelFile() throws IOException {
        // Cargar el archivo del modelo
        File modelFile = new File(getFilesDir(), "model.tflite");
        try (RandomAccessFile raf = new RandomAccessFile(modelFile, "r");
             FileChannel fileChannel = raf.getChannel()) {
            long startOffset = 0;
            long declaredLength = fileChannel.size();
            return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
        }
    }

    // Leemos las etiquetas del archivo txt, es decir, de los diferentes tipos de imágenes que vamos a clasificar
    private String[] readLabelsFromFile() throws IOException {
        // Leer las etiquetas de clasificación
        File labelFile = new File(getFilesDir(), "labels.txt");
        List<String> labels = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(labelFile)) {
            int content;
            StringBuilder sb = new StringBuilder();
            while ((content = fis.read()) != -1) {
                if (content == '\n') {
                    labels.add(sb.toString());
                    sb.setLength(0);
                } else {
                    sb.append((char) content);
                }
            }

            if (sb.length() > 0) {
                labels.add(sb.toString());
            }
        }
        return labels.toArray(new String[0]);
    }
}
