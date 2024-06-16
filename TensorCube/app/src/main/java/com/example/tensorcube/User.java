package com.example.tensorcube;

import com.google.firebase.database.IgnoreExtraProperties;
import java.util.ArrayList;

@IgnoreExtraProperties
public class User {
    private ArrayList<String> label;
    private String modelo;
    private String nombre;
    private String key;

    public User() {
        // Constructor vacío requerido por Firebase
        this.label = new ArrayList<>();
    }

    public User(ArrayList<String> label, String modelo, String nombre, String key) {
        this.label = label;
        this.modelo = modelo;
        this.nombre = nombre;
        this.key = key;
    }

    public ArrayList<String> getLabel() {
        return label;
    }

    public void setLabel(ArrayList<String> label) {
        this.label = label;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
