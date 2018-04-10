package com.thisway.xunfeicloud;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String TAG  = "OpenCV";

    private File  mCascadeFile;
    private CascadeClassifier      mJavaDetector;
    private boolean   mIsFrontCamera = false;
    private MenuItem mItemSwitchCamera = null;


    private Mat  mRgba;

    private CameraBridgeViewBase mOpenCvCameraView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.java_surface_view);
        mOpenCvCameraView.setVisibility(CameraBridgeViewBase.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        //mOpenCvCameraView.setCameraIndex(1);//为了使用前置摄像头  要不然一直是后置摄像头
    }


    private BaseLoaderCallback mOpenCVCallBack = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    //DO YOUR WORK/STUFF HERE
                {
                    Log.i(TAG, "OpenCV loaded successfully");

                    // Load native library after(!) OpenCV initialization
                    //System.loadLibrary("detection_based_tracker");

                    try {
                        // load cascade file from application resources
                        InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface);
                        File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                        mCascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
                        //File(File parent, String child) 根据 parent 抽象路径名和 child 路径名字符串创建一个新 File 实例。
                        FileOutputStream os = new FileOutputStream(mCascadeFile);
                        // FileOutputStream(File file)  创建一个向指定 File 对象表示的文件中写入数据的文件输出流

                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = is.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);
                        }

                        //inputStream.read() 从输入流中读取数据的下一个字节。返回 0 到 255 范围内的 int 字节值。
                        // 如果因为已经到达流末尾而没有可用的字节，则返回值 -1。
                        //write(byte[] b, int off, int len)  将指定 byte 数组中从偏移量 off 开始的 len 个字节写入此文件输出流。

                        is.close();
                        os.close();

                        mJavaDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());  //用于在摄像头中检测人
                        // String getAbsolutePath() 返回此抽象路径名的绝对路径名字符串
                        if (mJavaDetector.empty()) {
                            Log.e("cascade", "Failed to load cascade classifier");
                            mJavaDetector = null;
                        } else
                            Log.i("cascade", "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());

                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e("cascade", "Failed to load cascade. Exception thrown: " + e);
                    }

                    mOpenCvCameraView.enableView();
                }
                break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.memu_main, menu);
        Log.i(TAG, "called onCreateOptionsMenu");
        mItemSwitchCamera = menu.add("Toggle Front/Back camera");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        String toastMesage = "";

        if (item == mItemSwitchCamera) {
            mIsFrontCamera = !mIsFrontCamera;

            if (mIsFrontCamera) {
                mOpenCvCameraView.setCameraIndex(1);
                toastMesage = "Front Camera";
            } else {
                mOpenCvCameraView.setCameraIndex(-1);
                toastMesage = "Back Camera";
            }


            mOpenCvCameraView.enableView();
            Toast toast = Toast.makeText(this, toastMesage, Toast.LENGTH_LONG);
            toast.show();
        }
        return true;
    }

    @Override
    protected void onResume() {

        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, this, mOpenCVCallBack);
            //加载OpenCV以供使用  mLoaderCallback 回调函数  用于检查OpenCV是否已经安装好 也可在本地包含函数  否则跳到PLAY STORE去下载
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mOpenCVCallBack.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {

        mRgba = new Mat(height,width, CvType.CV_8UC4);
    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        //旋转输入帧
        Mat mGray = inputFrame.gray();
        mRgba = inputFrame.rgba();

        if (mIsFrontCamera){
            Core.flip(mRgba,mRgba,1);
        }

        //Detecting face in the frame  在帧中检测人脸
        MatOfRect faces = new MatOfRect();
        if(mJavaDetector!= null)
        {
            mJavaDetector.detectMultiScale(mGray, faces, 1.1, 2, 2, new Size(200,200), new Size());
        }

        Rect[] facesArray = faces.toArray();



        for (int i = 0; i < facesArray.length; i++)
        {

            Imgproc.rectangle(mRgba, facesArray[i].tl(), facesArray[i].br(),new Scalar(0, 255, 0, 255), 3);
            LogUtil.i(TAG, "有人脸");
            startActivity(new Intent(MainActivity.this,SpeechActivity.class));

            //rect.tl() rect的左上顶点   rect.br() 返回rect的右下顶点
        }


        return mRgba;


    }
}
