package com.sw.touristapp;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import static java.lang.Math.*;

/**
 * Klasa odpowiedzialna za funkcjonalność kompasu.
 */
public class Compass implements SensorEventListener {
    AppCompatActivity app;
    private SensorManager sensorManager;
    private Sensor accelerometerSensor;
    private Sensor geoMagneticSensor;
    private TextView angleView;
    private ImageView pointerView;
    private float azimuth;

    private float[] gravity = new float[3];
    private float[] geoMagnetic = new float[3];
    private float[] rotationMatrix = new float[9];

    /**
     * Konstruktor parametrowy kompasu.
     * @param context kontekst aplikacji
     */
    public Compass(Context context) {
        app = (AppCompatActivity) context;
        angleView = app.findViewById(R.id.angleView);
        pointerView = app.findViewById(R.id.compassPointer);
        sensorManager= (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        geoMagneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }

    /**
     * Rejestracja wymaganych czujników (akceleremoter i magnetometr).
     */
    public void start() {
        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, geoMagneticSensor, SensorManager.SENSOR_DELAY_GAME);
    }

    /**
     * Wyrejestrowanie wszystkich zarejestrowanych czujników.
     */
    public void stop() {
        sensorManager.unregisterListener(this);
    }

    /**
     * Aktualizacja azymutu w UI.
     * @param newAzimuth nowy wartość azymutu do aktualizacji
     */
    private void changeUI(float newAzimuth){
        angleView.setText(round(normalizeDegree(-newAzimuth)) + "°");
        Animation compassImageRotate = new RotateAnimation(azimuth, newAzimuth,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        azimuth = newAzimuth;

        compassImageRotate.setDuration(500);
        compassImageRotate.setRepeatCount(0);
        compassImageRotate.setFillAfter(true);
        pointerView.setAnimation(compassImageRotate);
    }

    /**
     * Metoda zostaje wywołana gdy zmieni się wartość czujnika.
     * @param event zdarzenie sensora
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        final float alpha = 0.96f;
        synchronized (this) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
                gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
                gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];
            }

            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                geoMagnetic[0] = alpha * geoMagnetic[0] + (1 - alpha) * event.values[0];
                geoMagnetic[1] = alpha * geoMagnetic[1] + (1 - alpha) * event.values[1];
                geoMagnetic[2] = alpha * geoMagnetic[2] + (1 - alpha) * event.values[2];
            }

            if (SensorManager.getRotationMatrix(rotationMatrix, null, gravity, geoMagnetic)) {
                final float[] orientation = new float[3];
                SensorManager.getOrientation(rotationMatrix, orientation);
                app.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        changeUI((float) -toDegrees(orientation[0]));
                    }
                });
            }
        }
    }

    /**
     * Normalizacja azymutu (0-360 stopni).
     * @param value azymut do normalizacji
     * @return azymut po normalizacji
     */
    private float normalizeDegree(float value) {
        if (value >= 0.0f && value <= 180.0f) {
            return value;
        } else {
            return 180 + (180 + value);
        }
    }

    /**
     * Metoda zostaje wywołana gdy zmieni się dokładność zarejestrowanego czujnika.
     * @param sensor obiekt sensora
     * @param accuracy nowa dokładność czujnika
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}
