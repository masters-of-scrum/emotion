package com.hackeps.mastersofscrum;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Date;

public class CameraActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2{
    private static final String TAG="MainActivity";

    private Mat mRgba;
    private Mat mGray;
    private String toShow = "Example string";
    private CameraBridgeViewBase mOpenCvCameraView;

    File caseFile;
    //private facialRecognition facialRecognition;

    private ModelService modelService;

    private BaseLoaderCallback mLoaderCallback =new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            if (status == LoaderCallbackInterface
                    .SUCCESS) {
                InputStream is = getResources().openRawResource(R.raw.haarcascade_frontalface_alt);
                File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                caseFile = new File(cascadeDir, "haarcascade_frontalface_alt.xml");

                try {
                    FileOutputStream fos = new FileOutputStream(caseFile);
                    byte[] buffer = new byte[4096];
                    int bytesRead;

                    while ((bytesRead = is.read(buffer)) != -1) {
                        fos.write(buffer, 0, bytesRead);
                    }
                    is.close();
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                CascadeClassifier faceDetector = new CascadeClassifier(caseFile.getAbsolutePath());
                if (faceDetector.empty()) {
                    faceDetector = null;
                } else {
                    cascadeDir.delete();
                }
                modelService = new ModelService(faceDetector);
                Log.i(TAG, "OpenCv Is loaded");
                mOpenCvCameraView.enableView();
            }
            super.onManagerConnected(status);
        }
    };

    public CameraActivity(){
        Log.i(TAG,"Instantiated new "+this.getClass());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        int MY_PERMISSIONS_REQUEST_CAMERA=0;
        // if camera permission is not given it will ask for it on device
        if (ContextCompat.checkSelfPermission(CameraActivity.this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(CameraActivity.this, new String[] {Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
        }

        setContentView(R.layout.activity_camera);

        mOpenCvCameraView=(CameraBridgeViewBase) findViewById(R.id.frame_Surface);
        mOpenCvCameraView.setCameraIndex(1);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        /*
        int inputSize=48;
        try {
            facialRecognition = new facialRecognition(getAssets(), CameraActivity.this,
                    "model.tflite", inputSize);
        } catch (IOException e) {
            e.printStackTrace();
        }
         */


    }

    @Override
    protected void onResume() {
        super.onResume();
        if (OpenCVLoader.initDebug()){
            //if load success
            Log.d(TAG,"Opencv initialization is done");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
        else{
            //if not loaded
            Log.d(TAG,"Opencv is not loaded. try again");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0,this,mLoaderCallback);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mOpenCvCameraView !=null){
            mOpenCvCameraView.disableView();
        }
    }

    public void onDestroy(){
        super.onDestroy();
        if(mOpenCvCameraView !=null){
            mOpenCvCameraView.disableView();
        }

    }

    public void onCameraViewStarted(int width ,int height){
        mRgba=new Mat(height,width, CvType.CV_8UC4);
        mGray =new Mat(height,width,CvType.CV_8UC1);
    }
    public void onCameraViewStopped(){
        mRgba.release();
    }
    @SuppressLint("SetTextI18n")
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba=inputFrame.rgba();
        mGray=inputFrame.gray();

        // FICAR EL TEXT QUE VOLGUEM MOSTRAR
        final TextView helloTextView = findViewById(R.id.toShowText);

        Rect largest_rect = modelService.detectBiggestFace(mGray);
        if (largest_rect == null){
            return mGray;
        } else {
            Mat croppedGray = modelService.cropToRect(mGray, largest_rect);
            Imgproc.rectangle(mRgba, largest_rect.tl(), largest_rect.br(), new Scalar(255,0,0), 4);
            Imgproc.rectangle(mGray, largest_rect.tl(), largest_rect.br(), new Scalar(255,0,0), 4);
            //Mat croppedRgba = modelService.cropToRect(mRgba, largest_rect);
            try {
                String result = modelService.sendCropped(croppedGray);
                result = result.replace("\n","");
                result = result.replace(" ","");
                helloTextView.setText(result);
            } catch (IOException e) {
                helloTextView.setText("REQUEST FAILED");
                e.printStackTrace();
                return mGray;
            }
            return mRgba;
        }
    }

}