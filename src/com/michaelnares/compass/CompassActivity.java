package com.michaelnares.compass;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

public class CompassActivity extends Activity
{
    private float[] aValues = new float[3];
    private float[] mValues = new float[3];
    private CompassView compassView;
    private SensorManager sensorManager;
    private int rotation;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        compassView = (CompassView) findViewById(R.id.compassView);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        final String windowService = Context.WINDOW_SERVICE;
        final WindowManager wm = (WindowManager) getSystemService(windowService);
        final Display display = wm.getDefaultDisplay();
        rotation = display.getRotation();

        updateOrientation(new float[]{0, 0, 0});
    }

    @Override
    public void onResume()
    {
        super.onResume();
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor magFieldSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        sensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(sensorEventListener, magFieldSensor, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        sensorManager.unregisterListener(sensorEventListener);
    }

    private void updateOrientation(float[] values)
    {
        if (compassView != null) {
            compassView.setBearing(values[0]);
            compassView.setPitch(values[1]);
            compassView.setRoll(-values[2]);
            compassView.invalidate();
        }
    }

    private float[] calculateOrientation()
    {
        float[] values = new float[3];
        float[] inR = new float[9];
        float[] outR = new float[9];

        //Determine the rotation matrix
        SensorManager.getRotationMatrix(inR, null, aValues, mValues);

        //Remap the co-ordinates based on the natural device orientation.
        int xAxis = SensorManager.AXIS_X;
        int yAxis = SensorManager.AXIS_Y;

        switch(rotation)
        {
            case (Surface.ROTATION_90):
                xAxis = SensorManager.AXIS_Y;
                yAxis = SensorManager.AXIS_MINUS_X;
                break;
            case (Surface.ROTATION_180):
                yAxis = SensorManager.AXIS_MINUS_Y;
                break;
            case (Surface.ROTATION_270):
                xAxis = SensorManager.AXIS_MINUS_Y;
                yAxis = SensorManager.AXIS_X;
                break;
            default:break;
        } // ends switch statement
        SensorManager.remapCoordinateSystem(inR, xAxis, yAxis, outR);
        SensorManager.getOrientation(outR, values); // current, corrected orientation

        //Convert from radians to degrees.
        values[0] = (float)Math.toDegrees(values[0]);
        values[1] = (float)Math.toDegrees(values[1]);
        values[2] = (float)Math.toDegrees(values[2]);
        return values;
    }

    private final SensorEventListener sensorEventListener = new SensorEventListener()
    {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent)
        {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            {
                aValues = sensorEvent.values;
            }

        if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            {
                mValues = sensorEvent.values;

                updateOrientation(calculateOrientation());
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy)
        {

        }
    };
}


