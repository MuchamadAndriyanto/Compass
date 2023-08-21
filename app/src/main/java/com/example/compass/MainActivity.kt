package com.example.compass

import android.content.Context
import android.graphics.Matrix
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import androidx.databinding.DataBindingUtil
import com.example.compass.databinding.ActivityMainBinding
import kotlin.math.roundToInt


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var sensorManager: SensorManager
    private lateinit var accelerometer: Sensor
    private lateinit var magnetometer: Sensor
    private val directions = arrayOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")
    private var currentDegree = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        if (accelerometer == null || magnetometer == null) {
            // Handle if the device doesn't have required sensors
        }
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(sensorListener, accelerometer, SensorManager.SENSOR_DELAY_GAME)
        sensorManager.registerListener(sensorListener, magnetometer, SensorManager.SENSOR_DELAY_GAME)
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(sensorListener)
    }

    // Inisialisasi array untuk membaca sensor
    private val accelerometerReading = FloatArray(3)
    private val magnetometerReading = FloatArray(3)
    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    private val sensorListener = object : SensorEventListener {
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            // Do nothing here
        }

        override fun onSensorChanged(event: SensorEvent) {
            if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                System.arraycopy(event.values, 0, accelerometerReading, 0, accelerometerReading.size)
                // Mengupdate array accelerometerReading dengan nilai baru dari sensor accelerometer
            } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                System.arraycopy(event.values, 0, magnetometerReading, 0, magnetometerReading.size)
                // Mengupdate array magnetometerReading dengan nilai baru dari sensor magnetometer
            }

            // Mendapatkan matriks rotasi dan sudut orientasi dari nilai sensor
            SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerReading, magnetometerReading)
            SensorManager.getOrientation(rotationMatrix, orientationAngles)

            val azimuth = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
            val rotation = -azimuth + currentDegree
            currentDegree = -azimuth

            //  Animasi rotasi dengan menggunakan RotateAnimation
            val rotateAnimation = RotateAnimation(
                currentDegree,
                rotation,
                Animation.RELATIVE_TO_SELF,
                0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f
            )
            rotateAnimation.duration = 200
            rotateAnimation.interpolator = LinearInterpolator()
            binding.imageView.startAnimation(rotateAnimation)

            // Menghitung indeks arah 
            val directionIndex = ((azimuth + 360) % 360 / 45).roundToInt() % 8
            val direction = directions[directionIndex]
            binding.tvValue.text = direction
        }
    }
}