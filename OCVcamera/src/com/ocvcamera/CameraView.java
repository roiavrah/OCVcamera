package com.ocvcamera;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.opencv.android.JavaCameraView;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.Area;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class CameraView extends JavaCameraView implements PictureCallback, AutoFocusCallback {

	private static final String TAG = "OCV::camera";
	private String mPictureFileName;
	private TextView cameraParam;
	Rect tfocusRect;
	public CameraView(Context context, AttributeSet attrs) {
		super(context, attrs);

	}

	public void  setParametersView(TextView tv){
		cameraParam = tv;
	}

	public void setupCamera(){
		Camera.Parameters parameters = mCamera.getParameters();
		parameters.setJpegQuality(100);
		parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO); 	
		mCamera.setParameters(parameters);    	
	}	

	public List<String> getEffectList() {
		return mCamera.getParameters().getSupportedColorEffects();
	}

	public boolean isEffectSupported() {
		return (mCamera.getParameters().getColorEffect() != null);
	}

	public String getEffect() {
		return mCamera.getParameters().getColorEffect();
	}

	public void setEffect(String effect) {
		Camera.Parameters params = mCamera.getParameters();
		params.setColorEffect(effect);
		mCamera.setParameters(params);
	}

	public List<Size> getResolutionList() {
		return mCamera.getParameters().getSupportedPreviewSizes();
	}

	public void setResolution(Size resolution) {
		disconnectCamera();
		mMaxHeight = resolution.height;
		mMaxWidth = resolution.width;
		connectCamera(getWidth(), getHeight());
	}

	public Size getResolution() {
		return mCamera.getParameters().getPreviewSize();
	}

	public void takePicture(final String fileName) {
		Log.i(TAG, "Taking picture");
		this.mPictureFileName = fileName;
		// Postview and jpeg are sent in the same buffers if the queue is not empty when performing a capture.
		// Clear up buffers to avoid mCamera.takePicture to be stuck because of a memory issue
		mCamera.setPreviewCallback(null);

		// PictureCallback is implemented by the current class
		mCamera.takePicture(null, null, this);
	}

	@Override
	public void onPictureTaken(byte[] data, Camera camera) {
		Log.i(TAG, "Saving a bitmap to file");
		// The camera preview was automatically stopped. Start it again.
		mCamera.startPreview();
		mCamera.setPreviewCallback(this);

		// Write the image in a file (in jpeg format)
		try {
			FileOutputStream fos = new FileOutputStream(mPictureFileName);
			fos.write(data);
			fos.close();

		} catch (java.io.IOException e) {
			Log.e("Picture", "Exception in photoCallback", e);
		}

	}

	@Override
	public void onAutoFocus(boolean success, Camera camera) {
		/*if(success){
			Toast.makeText(this.getContext(), "autoFocus"+"", Toast.LENGTH_SHORT).show(); 
		}*/	
	}
	
	public void setFocusArea(final Rect tfocusRect){
		this.tfocusRect = tfocusRect;
		Camera.Parameters parameters = mCamera.getParameters();
		
		final Rect targetFocusRect = new Rect(
				tfocusRect.left * 2000/this.getWidth() - 1000,
				tfocusRect.top * 2000/this.getHeight() - 1000,
				tfocusRect.right * 2000/this.getWidth() - 1000,
				tfocusRect.bottom * 2000/this.getHeight() - 1000);

		final List<Camera.Area> focusList = new ArrayList<Camera.Area>();
		Camera.Area focusArea = new Camera.Area(targetFocusRect, 1000);
		focusList.add(focusArea);

		parameters.setFocusAreas(focusList);
		parameters.setMeteringAreas(focusList);

		cameraParam.setText(targetFocusRect.width()+" "+targetFocusRect.height());  			
		
		mCamera.setParameters(parameters);   		
		mCamera.autoFocus(this);
	}
	
}
