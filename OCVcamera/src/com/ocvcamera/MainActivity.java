package com.ocvcamera;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.Rect;
import android.hardware.Camera.Area;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements CvCameraViewListener2, OnTouchListener {
	private static final String TAG = "OCVSample::Activity";
  
	private CameraView mOpenCvCameraView;
	private List<Size> mResolutionList;
	private MenuItem[] mEffectMenuItems;
	private SubMenu mColorEffectsMenu;
	private MenuItem[] mResolutionMenuItems;
	private SubMenu mResolutionMenu;
	private TextView orientationView,cameraParametersView;
	private SensorScanner sensorScaner;
	private Mat  mGray, mRgba;

	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS:
			{
				Log.i(TAG, "OpenCV loaded successfully");
				mOpenCvCameraView.enableView();
				mOpenCvCameraView.setOnTouchListener(MainActivity.this);
				mOpenCvCameraView.setupCamera();
			} break;
			default:
			{
				super.onManagerConnected(status);
			} break;
			}
		}
	};

	public MainActivity() {
		Log.i(TAG, "Instantiated new " + this.getClass());
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "called onCreate");
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.camera_surface_view);

		cameraParametersView = (TextView) findViewById(R.id.camera_parameters_view);

		mOpenCvCameraView = (CameraView) findViewById(R.id.camera_surface_view);
		mOpenCvCameraView.setParametersView(cameraParametersView);
		mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
		mOpenCvCameraView.setCvCameraViewListener(this);

		orientationView = (TextView) findViewById(R.id.orientation_view);
		orientationView.setTextColor(Color.RED);
		sensorScaner = new SensorScanner(this, orientationView);
	}

	@Override
	public void onPause()
	{
		super.onPause();
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
	}

	@Override
	public void onResume()
	{
		super.onResume();
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
	}

	public void onDestroy() {
		super.onDestroy();
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
		sensorScaner.StopSensors();
	}

	public void onCameraViewStarted(int width, int height) {
		mGray = new Mat();
		mRgba = new Mat();
	}

	public void onCameraViewStopped() {
	}

	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {		
		mRgba = inputFrame.rgba();
		//Imgproc.cvtColor(mRgba, mGray, Imgproc.COLOR_BGRA2GRAY);		
		return mRgba;
	}

	public void SaveImage (Mat mat, String fileName) {
		  Mat mIntermediateMat = mat.clone();// new Mat();
		  Imgproc.cvtColor(mRgba, mIntermediateMat, Imgproc.COLOR_RGBA2BGR, 3);

		  File path = new File(fileName); //.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);		  
		  Boolean bool = Highgui.imwrite(path.toString(), mIntermediateMat);
		  if (bool == true)
		    Log.d(TAG, "SUCCESS writing image to external storage");
		  else
		    Log.d(TAG, "Fail writing image to external storage");
		  }
	

	@SuppressLint("SimpleDateFormat")
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		Log.i(TAG,"onTouch event");
		setFocusOnTouch(event);
		return false;
	}
	
	private void setFocusOnTouch(MotionEvent event){

		if(event.getAction() == MotionEvent.ACTION_DOWN){
			float x = event.getX();
			float y = event.getY();
			float touchMajor = event.getTouchMajor();
			float touchMinor = event.getTouchMinor();

			Rect touchRect = new Rect(
					(int)(x - touchMajor/2), 
					(int)(y - touchMinor/2), 
					(int)(x + touchMajor/2), 
					(int)(y + touchMinor/2));
			mOpenCvCameraView.setFocusArea(touchRect);
		}
	}

	public void takePictureAndOrientation(View view){
		//prepare file path & name
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		String currentDateandTime = sdf.format(new Date());
		String fileName = "mnt/sdcard" + "/sample_picture_" + currentDateandTime ;

		//take picture
		SaveImage(mRgba,fileName + ".png");	
		//mOpenCvCameraView.takePicture(fileName + ".png");

		//take orientation
		sensorScaner.saveOrientationToTextFile(fileName + ".txt");

		Toast.makeText(this, fileName + " saved", Toast.LENGTH_SHORT).show(); 
	}
	

	/* @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        List<String> effects = mOpenCvCameraView.getEffectList();

        if (effects == null) {
            Log.e(TAG, "Color effects are not supported by device!");
            return true;
        }

        mColorEffectsMenu = menu.addSubMenu("Color Effect");
        mEffectMenuItems = new MenuItem[effects.size()];

        int idx = 0;
        ListIterator<String> effectItr = effects.listIterator();
        while(effectItr.hasNext()) {
           String element = effectItr.next();
           mEffectMenuItems[idx] = mColorEffectsMenu.add(1, idx, Menu.NONE, element);
           idx++;
        }

        mResolutionMenu = menu.addSubMenu("Resolution");
        mResolutionList = mOpenCvCameraView.getResolutionList();
        mResolutionMenuItems = new MenuItem[mResolutionList.size()];

        ListIterator<Size> resolutionItr = mResolutionList.listIterator();
        idx = 0;
        while(resolutionItr.hasNext()) {
            Size element = resolutionItr.next();
            mResolutionMenuItems[idx] = mResolutionMenu.add(2, idx, Menu.NONE,
                    Integer.valueOf(element.width).toString() + "x" + Integer.valueOf(element.height).toString());
            idx++;
         }

        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);
        if (item.getGroupId() == 1)
        {
            mOpenCvCameraView.setEffect((String) item.getTitle());
            Toast.makeText(this, mOpenCvCameraView.getEffect(), Toast.LENGTH_SHORT).show();
        }
        else if (item.getGroupId() == 2)
        {
            int id = item.getItemId();
            Size resolution = mResolutionList.get(id);
            mOpenCvCameraView.setResolution(resolution);
            resolution = mOpenCvCameraView.getResolution();
            String caption = Integer.valueOf(resolution.width).toString() + "x" + Integer.valueOf(resolution.height).toString();
            Toast.makeText(this, caption, Toast.LENGTH_SHORT).show();
        }

        return true;
    }*/

}
