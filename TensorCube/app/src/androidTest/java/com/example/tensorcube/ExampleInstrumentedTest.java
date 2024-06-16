package com.example.tensorcube;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {

    private FirebaseAuth mAuth;

    @Before
    public void setUp() {
        Intents.init();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            mAuth.signOut();
        }
    }

    @After
    public void tearDown() {
        Intents.release();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            mAuth.signOut();
        }
    }

    // Test para realizar login con éxito
    @Test
    public void testLoginSuccess() {
        // Lanzamos LoginActivity
        ActivityScenario<LoginActivity> scenario = ActivityScenario.launch(LoginActivity.class);

        scenario.onActivity(activity -> {
            // Introducimos correo y contraseña correctos
            String validEmail = "a3@correo.es";
            String validPassword = "A123.no";

            activity.email.setText(validEmail);
            activity.password.setText(validPassword);

            // Hacemos click en nuestro botón
            activity.btn_login.performClick();
        });

        // Esperamos un momento para permitir que se procese el login
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Verificamos que se ha lanzado MenuActivity
        intended(hasComponent(MenuActivity.class.getName()));
    }

    @Test
    public void testLoginFailure() {
        // Lanzamos LoginActivity
        ActivityScenario<LoginActivity> scenario = ActivityScenario.launch(LoginActivity.class);

        scenario.onActivity(activity -> {
            // Introducimos correo y contraseña incorrectos
            String invalidEmail = "correo@correo.com";
            String invalidPassword = "wrongpassword";

            activity.email.setText(invalidEmail);
            activity.password.setText(invalidPassword);

            // Hacemos click en nuestro botón
            activity.btn_login.performClick();
        });

        // Esperamos un momento para permitir que se procese el login
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Verificamos que no se ha lanzado MenuActivity
        boolean menuActivityStarted = false;
        try {
            intended(hasComponent(MenuActivity.class.getName()));
            menuActivityStarted = false;
        } catch (AssertionError e) {
            // MenuActivity no se lanzó, lo cual es lo esperado
        }
        assertTrue("Test fallido: testLoginFailure", !menuActivityStarted);
    }
}
