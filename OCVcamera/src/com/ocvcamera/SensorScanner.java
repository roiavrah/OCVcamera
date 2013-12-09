package com.ocvcamera;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

/**
 * This class is used to scan sensors (like pressure) and pass them to the controller
 *
 */

public class SensorScanner {

	// Hold an interface to notify the outside world
	// Context needed to get access to sensor service
	
	private final String TAG ="SensorScanner";
	private Context context;

	protected SensorManager sensorManager;
	protected Sensor sensor;	

	private float[] orintationMatrixR = new float[9];
	private float[] lastAcc = new float[3];
	private float[] lastMagnet = new float[3];	
	private float orientation[] = new float[3]; 
	
	private TextView textView;
	
	public SensorScanner(Context context, TextView textView){
		this.context = context;
		this.textView = textView;
		
		sensorManager = (SensorManager)context.getSystemService(context.SENSOR_SERVICE);		
		StartSensors();
				
	}

	public SensorEventListener mySensorEventListener = new SensorEventListener() {

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// Auto-generated method stub
		}

		@Override
		public void onSensorChanged(SensorEvent event) {

			//Log.d("sensor","changed");
			switch (event.sensor.getType()) {
			case Sensor.TYPE_ACCELEROMETER:

				lastAcc = event.values.clone();

				if (lastAcc[0] != 0 && lastMagnet[0] != 0) {

					SensorManager.getRotationMatrix(orintationMatrixR, null,lastAcc, lastMagnet);
					SensorManager.getOrientation(orintationMatrixR, orientation);
					//Update data:
					//sensors.Set((float)Math.toDegrees(orientation[0]), (float)Math.toDegrees(orientation[1]),(float)Math.toDegrees(orientation[2]));
					textView.setText("yaw:   "+(int)Math.toDegrees(orientation[0])+"\npitch: "+(int)Math.toDegrees(orientation[1])+"\nroll:    "+ (int)Math.toDegrees(orientation[2]));
				}

				break;
			case Sensor.TYPE_MAGNETIC_FIELD:

				lastMagnet = event.values.clone();

				if (lastAcc[0] != 0 && lastMagnet[0] != 0) {

					SensorManager.getRotationMatrix(orintationMatrixR, null,lastAcc, lastMagnet);
					SensorManager.getOrientation(orintationMatrixR, orientation);
					//Update data:
					//sensors.Set((float)Math.toDegrees(orientation[0]), (float)Math.toDegrees(orientation[1]),(float)Math.toDegrees(orientation[2]));
				}

				break;
				/*case Sensor.TYPE_ORIENTATION:

				lastMagnet = event.values.clone();

				if (lastAcc[0] != 0 && lastMagnet[0] != 0) {

					SensorManager.getRotationMatrix(orintationMatrixR, null,lastAcc, lastMagnet);
					SensorManager.getOrientation(orintationMatrixR, orientation);
					//Update data:
					sensors.Set((float)Math.toDegrees(orientation[0]), (float)Math.toDegrees(orientation[1]),(float)Math.toDegrees(orientation[2]));
				}

				break;*/
			}
		}		
	};

	public void StartSensors(){
		Log.i(TAG, "StartSensors");
		sensorManager.registerListener(mySensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_UI);	
		sensorManager.registerListener(mySensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_UI);
		//sensorManager.registerListener(mySensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_UI);
	}

	public void StopSensors(){
		sensorManager.unregisterListener(mySensorEventListener);
		Log.i(TAG, "StopSensors");
	}
	
	public void saveOrientationToTextFile(String fileName){
		Log.i(TAG, "Saving a txt to file");
		FileWriter fileWriter = null;			
		try {		
			File newTextFile = new File(fileName);
			fileWriter = new FileWriter(newTextFile);
			fileWriter.write((int)Math.toDegrees(orientation[0])+System.getProperty( "line.separator")+(int)Math.toDegrees(orientation[1])+System.getProperty( "line.separator")+(int)Math.toDegrees(orientation[2]));
			fileWriter.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			try {
				fileWriter.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

}
