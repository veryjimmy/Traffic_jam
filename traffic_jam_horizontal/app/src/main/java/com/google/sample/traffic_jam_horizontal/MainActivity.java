/*
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.sample.traffic_jam_horizontal;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequest;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements Camera.AutoFocusCallback
{
    private static final String CLOUD_VISION_API_KEY = "AIzaSyDv3qmz9FJsp4YZjN4EM932RFJoyyiwt6I";
    public static final String FILE_NAME = "temp.jpg";
    private static final String ANDROID_CERT_HEADER = "X-Android-Cert";
    private static final String ANDROID_PACKAGE_HEADER = "X-Android-Package";
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int GALLERY_PERMISSIONS_REQUEST = 0;
    private static final int GALLERY_IMAGE_REQUEST = 1;
    public static final int CAMERA_PERMISSIONS_REQUEST = 2;
    public static final int CAMERA_IMAGE_REQUEST = 3;
    public static final String TRAFFIC_COLLISION = "traffic collision";
    static float a=0;
    private TextView mImageDetails;
    private ImageView mMainImage;
    private Camera camera;
    private List<String> number;
    private GpsLocationListener gpsLocationListener;//定位监听器
    private MyMongo mongo;                        //云端伺服器
    private Button sStart;
    boolean RUN=true;
    private SurfaceView mSurfaceView;
    private Camera mCamera;
    private SurfaceHolder mSurfaceHolder;
    private Camera.Parameters parameters = null;
    private Button mStart;
    private Button mStop;
    private Switch Open;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        /////////////////////////////////////////////////////////camera///////////////////////////////////////////////////////////////////////////////////
        mSurfaceView = (SurfaceView) findViewById(R.id.surface);
        mStart = (Button) findViewById(R.id.start);
        mStop = (Button) findViewById(R.id.stop);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mSurfaceHolder.setFixedSize(176, 144); //设置Surface分辨率
        mSurfaceHolder.addCallback(new MainActivity.SurfaceCallback());
        mSurfaceView.setVisibility(View.INVISIBLE);
        Open = (Switch) findViewById(R.id.open);
        mStart.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                RUN=true;
                try
                {
                    new Thread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            while(RUN)
                            {
                                try
                                {
                                    mCamera.takePicture(null, null, new MyPictureCallback());
                                    Thread.sleep(3000);
                                }
                                catch(Exception e)
                                {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }).start();
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
            }
        });
        mStop.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                    RUN=false;
            }
        });

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        sStart = (Button) findViewById(R.id.camera);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        configure_button();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(View.INVISIBLE);
        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                startGalleryChooser();
            }
        });

        sStart.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //mSurfaceView.setVisibility(View.VISIBLE);
                mCamera.takePicture(null, null, new MyPictureCallback());

            }
        });

        Open.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                // TODO Auto-generated method stub
                if (isChecked)
                {
                    mSurfaceView.setVisibility(View.VISIBLE);
                    mStart.setVisibility(View.VISIBLE);
                    sStart.setVisibility(View.VISIBLE);
                    mStop.setVisibility(View.VISIBLE);
                }
                else
                {
                    mSurfaceView.setVisibility(View.GONE);
                    mStart.setVisibility(View.GONE);
                    sStart.setVisibility(View.GONE);
                    mStop.setVisibility(View.GONE);
                }
            }
        });

        Log.i(TAG, "Create Service");
        mongo = new MyMongo();
        gpsLocationListener = new GpsLocationListener(this);
        Log.i(TAG, "Service start!!");

        mImageDetails = (TextView) findViewById(R.id.image_details);
        mMainImage = (ImageView) findViewById(R.id.main_image);
        number = new ArrayList<>();
        String s = Float.toString(a);
        number.add(s);
    }

    ///////////////////////////////////////////////////////////////////////MyPictureCallback/////////////////////////////////////////////////////////////////////////////////
    private final class MyPictureCallback implements Camera.PictureCallback
    {
        @Override
        public void onPictureTaken(byte[] data, Camera camera)
        {
            try
            {
                ///////////////////////////////////////////////////////////////////////儲存//////////////////////////////////////////////////////////////////////////////////////
                FileOutputStream outStream = null;
                try
                {
                    //儲存在內部記憶體
                    outStream = new FileOutputStream("/sdcard/photo.jpg");
                    outStream.write(data);
                    outStream.close();
                    Toast.makeText(MainActivity.this, "上傳成功！", Toast.LENGTH_SHORT).show();

                    ////////////////////////////////////////////////////////////////////////////////取得URI////////////////////////////////////////////////////////////////////////////////
                    String TakePicFileName = "photo.jpg";
                    String TakePicFilePath = Environment.getExternalStorageDirectory().toString();
                    File tmpFile = new File(TakePicFilePath, TakePicFileName);
                    Uri photoUri = Uri.fromFile(tmpFile);
                    ////////////////////////////////////////////////////////////////////////////////取得URI////////////////////////////////////////////////////////////////////////////////
                    String stringUri = photoUri.toString();
                    Log.i("URI", stringUri);
                    CameraUploadImage(photoUri);
                    camera.startPreview(); // 拍完照后，重新开始预览
                }
                catch (IOException e)
                {
                    Toast.makeText(MainActivity.this, "影像檔儲存錯誤！", Toast.LENGTH_SHORT).show();
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
    ///////////////////////////////////////////////////////////////////////Surface的生命週期//////////////////////////////////////////////////////////////////////////////////////
    private final class SurfaceCallback implements SurfaceHolder.Callback
    {
        @Override
        public void surfaceCreated(SurfaceHolder holder)
        {
            mCamera = Camera.open();
            //mCamera.setDisplayOrientation(90); //摄像头进行旋转90°
            try
            {
                mCamera.setPreviewDisplay(holder);
            }
            catch (IOException e)
            {
                mCamera.release();
                mCamera = null;
            }
            Log.d("MYLOG", "SurfaceView is Creating!");
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) //進行camera的preview動作
        {
            Log.d("MYLOG", "SurfaceView is Change!");
            mCamera.startPreview();
            parameters = mCamera.getParameters(); // 获取各项参数
            parameters.setPictureFormat(PixelFormat.JPEG); // 设置图片格式
            parameters.setPreviewSize(width, height); // 设置预览大小
            parameters.setPreviewFrameRate(5);  //设置每秒显示4帧
            parameters.setPictureSize(width, height); // 设置保存的图片尺寸
            parameters.setJpegQuality(80); // 设置照片质量

            mCamera.autoFocus(new Camera.AutoFocusCallback()
            {
                @Override
                public void onAutoFocus(boolean success, Camera camera)
                {
                    if (success)
                    {
                        parameters = camera.getParameters();
                        parameters.setPictureFormat(PixelFormat.JPEG);
                        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);//1连续对焦
                        camera.setParameters(parameters);
                        camera.startPreview();
                    }
                }
            });
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder)
        {
            Log.d("MYLOG", "SurfaceView is Destroyed!");
            mCamera.release();
            mCamera = null;
        }
    }
    ///////////////////////////////////////////////////////////////////////Surface的生命週期//////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onAutoFocus(boolean success, Camera camera)
    {
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    void configure_button()
    {
        // first check for permissions
        if (
                //ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        //ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        PermissionUtils.requestPermission(this, CAMERA_PERMISSIONS_REQUEST,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION))
        {
            mSurfaceView.setVisibility(View.VISIBLE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.INTERNET},10);
            }
            return;
        }
    }


    public void startGalleryChooser()
    {
        if (PermissionUtils.requestPermission(this, GALLERY_PERMISSIONS_REQUEST, Manifest.permission.READ_EXTERNAL_STORAGE))
        {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select a photo"), GALLERY_IMAGE_REQUEST);
        }
    }

    public void startCamera()
    {/*
        if (PermissionUtils.requestPermission(this, CAMERA_PERMISSIONS_REQUEST, Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE))
        {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            Uri photoUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", getCameraFile());
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivityForResult(intent, CAMERA_IMAGE_REQUEST);
        }*/
    }
    public File getCameraFile()
    {
        //handler.postDelayed(runnable, 2000);//每两秒执行一次runnable
        File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return new File(dir, FILE_NAME);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_IMAGE_REQUEST && resultCode == RESULT_OK && data != null)
        {
            GalleryUploadImage(data.getData());
        }
        else
        if (requestCode == CAMERA_IMAGE_REQUEST && resultCode == RESULT_OK)
        {

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode)
        {
            case CAMERA_PERMISSIONS_REQUEST:
                if (PermissionUtils.permissionGranted(requestCode, CAMERA_PERMISSIONS_REQUEST, grantResults))
                {
                    startCamera();
                }
                break;
            case GALLERY_PERMISSIONS_REQUEST:
                if (PermissionUtils.permissionGranted(requestCode, GALLERY_PERMISSIONS_REQUEST, grantResults))
                {
                    startGalleryChooser();
                }
                break;
        }
    }

    public void CameraUploadImage(Uri uri)
    {
        if (uri != null)
        {
            try {
                // scale the image to save on bandwidth
                Bitmap bitmap = scaleBitmapDown(MediaStore.Images.Media.getBitmap(getContentResolver(), uri), 1200);

//////////////////////////////////////////////////////////////////////////////////////旋轉90度//////////////////////////////////////////////////////////////////////////////////////////////////
                Matrix matrix = new Matrix();
                //matrix.postRotate(90);
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                callCloudVision(bitmap);

                mMainImage.setImageBitmap(bitmap);

            } catch (IOException e)
            {
                Log.d(TAG, "Image picking failed because " + e.getMessage());
                Toast.makeText(this, R.string.image_picker_error, Toast.LENGTH_LONG).show();
            }
        }
        else
        {
            Log.d(TAG, "Image picker gave us a null image.");
            Toast.makeText(this, R.string.image_picker_error, Toast.LENGTH_LONG).show();
        }
    }

    public void GalleryUploadImage(Uri uri)
    {
        if (uri != null)
        {
            try {
                // scale the image to save on bandwidth
                Bitmap bitmap = scaleBitmapDown(MediaStore.Images.Media.getBitmap(getContentResolver(), uri), 1200);

                callCloudVision(bitmap);

                mMainImage.setImageBitmap(bitmap);

            } catch (IOException e)
            {
                Log.d(TAG, "Image picking failed because " + e.getMessage());
                Toast.makeText(this, R.string.image_picker_error, Toast.LENGTH_LONG).show();
            }
        }
        else
        {
            Log.d(TAG, "Image picker gave us a null image.");
            Toast.makeText(this, R.string.image_picker_error, Toast.LENGTH_LONG).show();
        }
    }

    private void callCloudVision(final Bitmap bitmap) throws IOException
    {
        // Switch text to loading
        mImageDetails.setText(R.string.loading_message);

        // Do the real work in an async task, because we need to use the network anyway
        new AsyncTask<Object, Void, String>()
        {
            @Override
            protected String doInBackground(Object... params) {
                try {
                    HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
                    JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

                    VisionRequestInitializer requestInitializer =
                            new VisionRequestInitializer(CLOUD_VISION_API_KEY) {
                                /**
                                 * We override this so we can inject important identifying fields into the HTTP
                                 * headers. This enables use of a restricted cloud platform API key.
                                 */
                                @Override
                                protected void initializeVisionRequest(VisionRequest<?> visionRequest)
                                        throws IOException {
                                    super.initializeVisionRequest(visionRequest);

                                    String packageName = getPackageName();
                                    visionRequest.getRequestHeaders().set(ANDROID_PACKAGE_HEADER, packageName);

                                    String sig = PackageManagerUtils.getSignature(getPackageManager(), packageName);

                                    visionRequest.getRequestHeaders().set(ANDROID_CERT_HEADER, sig);
                                }
                            };

                    Vision.Builder builder = new Vision.Builder(httpTransport, jsonFactory, null);
                    builder.setVisionRequestInitializer(requestInitializer);

                    Vision vision = builder.build();

                    BatchAnnotateImagesRequest batchAnnotateImagesRequest =
                            new BatchAnnotateImagesRequest();
                    batchAnnotateImagesRequest.setRequests(new ArrayList<AnnotateImageRequest>() {{
                        AnnotateImageRequest annotateImageRequest = new AnnotateImageRequest();

                        // Add the image
                        Image base64EncodedImage = new Image();
                        // Convert the bitmap to a JPEG
                        // Just in case it's a format that Android understands but Cloud Vision
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
                        byte[] imageBytes = byteArrayOutputStream.toByteArray();

                        // Base64 encode the JPEG
                        base64EncodedImage.encodeContent(imageBytes);
                        annotateImageRequest.setImage(base64EncodedImage);

                        // add the features we want
                        // add the features we want
                        annotateImageRequest.setFeatures(new ArrayList<Feature>() {{
                            Feature labelDetection = new Feature();
                            labelDetection.setType("LABEL_DETECTION");
                            labelDetection.setMaxResults(100);
                            add(labelDetection);
                        }});

                        // Add the list of one thing to the request
                        add(annotateImageRequest);
                    }});
                    Vision.Images.Annotate annotateRequest = vision.images().annotate(batchAnnotateImagesRequest);
                    // Due to a bug: requests to Vision API containing large images fail when GZipped.
                    annotateRequest.setDisableGZipContent(true);
                    Log.d(TAG, "created Cloud Vision request object, sending request");
                    BatchAnnotateImagesResponse response = annotateRequest.execute();
                    return convertResponseToString(response);
                }
                catch (GoogleJsonResponseException e)
                {
                    Log.d(TAG, "failed to make API request because " + e.getContent());
                }
                catch (IOException e)
                {
                    Log.d(TAG, "failed to make API request because of other IOException " +
                            e.getMessage());
                }
                return "Cloud Vision API request failed. Check logs for details.";
            }

            protected void onPostExecute(String result)
            {
                mImageDetails.setText(result);
            }

        }.execute();
    }

    //縮放圖片
    public Bitmap scaleBitmapDown(Bitmap bitmap, int maxDimension)
    {

        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        int resizedWidth = maxDimension;
        int resizedHeight = maxDimension;

        if (originalHeight > originalWidth)
        {
            resizedHeight = maxDimension;
            resizedWidth = (int) (resizedHeight * (float) originalWidth / (float) originalHeight);
        } else if (originalWidth > originalHeight)
        {
            resizedWidth = maxDimension;
            resizedHeight = (int) (resizedWidth * (float) originalHeight / (float) originalWidth);
        } else if (originalHeight == originalWidth)
        {
            resizedHeight = maxDimension;
            resizedWidth = maxDimension;
        }
        return Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, false);
    }

    private String convertResponseToString(BatchAnnotateImagesResponse response)
    {
        //String message = "I found these things:\n\n\n";
        String message = "Probability:\n\n\n";
        List<EntityAnnotation> labels = response.getResponses().get(0).getLabelAnnotations();
        if (labels != null)
        {
            for (EntityAnnotation label : labels)
            {
                if(TRAFFIC_COLLISION.equals(label.getDescription())) // find traffic collision
                {
                    message += String.format(Locale.US, "%s: %.3f", label.getDescription(), label.getScore());
                    a = label.getScore();
                    message += "\n";
                    new Thread() {
                        @Override
                        public void run() {
                            try {
                                Looper.prepare();               //important!!!!!!!!!! if you dont konw this is what please google!!!!!!!!!!!!!!!
                                gpsLocationListener.getLocation();
                                // mongodb.remove("service", gpsLocationListener.getId());
                                List<String> key = new ArrayList<String>(gpsLocationListener.getGpsDataName());
                                key.add("Score");
                                List<String> value = new ArrayList<String>(gpsLocationListener.getGpsData());
                                value.add(String.valueOf(a));
                                mongo.insert("Vision", key, value);
                                Looper.loop();                  //important!!!!!!!!!! if you dont konw this is what please google!!!!!!!!!!!!!!!
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            super.run();
                        }
                    }.start();
                }
            }
        }
        else
        {
            message += "nothing";
        }
        return message;
    }
}
