package cse.cuhk.smartalbum;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.database.MergeCursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Process;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import com.google.gson.Gson;
import com.microsoft.projectoxford.vision.VisionServiceClient;
import com.microsoft.projectoxford.vision.VisionServiceRestClient;
import com.microsoft.projectoxford.vision.contract.AnalysisResult;
import com.microsoft.projectoxford.vision.contract.Caption;
import com.microsoft.projectoxford.vision.rest.VisionServiceException;

import cse.cuhk.smartalbum.utils.Photo;
import cse.cuhk.smartalbum.utils.database.DBHelper;

public class InitUpdateService extends Service {
    public static final String GET_IMAGE_PATH_KEY = "image_path_key";
    private HashSet<String> imagePathSet;
    private Looper mServiceLooper;
    private Handler mServiceHandler;
    private DBHelper db;
    // update the database every 30 sec
    private int ALARM_INTERVAL = 30*1000;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private Runnable yourRunnable = new Runnable() {
        @Override
        public void run() {
            //DB work here
            ArrayList<String> allImages = getAllShownImagesPath(InitUpdateService.this);

            for(String imagePath: allImages){
                if(!imagePathSet.contains(imagePath)){
                    imagePathSet.add(imagePath);
                    db.insertPhoto(imagePath, imagePath, "Nothing");
                    try {
                       AsyncTask temp = new doRequest(imagePath).execute();
                    } catch (Exception e) {
                        Log.d("VisionAPI - Exception", e.toString());
                    }
                }
            }
            if (mServiceHandler != null)
                mServiceHandler.postDelayed(this, ALARM_INTERVAL);
        }
    };


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // get all images from user data
        ArrayList<String> allImages = getAllShownImagesPath(InitUpdateService.this);
        // save all in database
        for(String imagePath: allImages){
            if(!imagePathSet.contains(imagePath)){
                imagePathSet.add(imagePath);
                db.insertPhoto(imagePath, imagePath, "Nothing");
            }
        }
        mServiceHandler.removeCallbacks(yourRunnable);
        mServiceHandler.post(yourRunnable);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {

        // get all imagesPath from database
        imagePathSet = new HashSet<>();

        db = new DBHelper(this);
        ArrayList<Photo> photos = db.getAllPhotos();
        for(Photo photo: photos){
            imagePathSet.add(photo.path);
        }
        Toast.makeText(this, "Service Created", Toast.LENGTH_LONG).show();
        // Start up the thread running the service.  Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.  We also make it
        // background priority so CPU-intensive work will not disrupt our UI.
        HandlerThread thread = new HandlerThread("ServiceStartArguments",
                Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        // Get the HandlerThread's Looper and use it for our Handler
        mServiceLooper = thread.getLooper();
        mServiceHandler = new Handler(mServiceLooper);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();
        mServiceHandler.removeCallbacks(yourRunnable);
        mServiceLooper.quit();
    }

    private ArrayList<String> getAllShownImagesPath(Service service) {

        Uri uri;
        Cursor[] cursors = new Cursor[2];

        int column_index_data, column_index_folder_name;
        ArrayList<String> listOfAllImages = new ArrayList<String>();
        String absolutePathOfImage = null;
        uri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String[] projection = { MediaStore.MediaColumns.DATA,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME };

        cursors[0] = service.getContentResolver().query(uri, projection, null,
                null,MediaStore.Images.ImageColumns.DATE_TAKEN + " ASC");
        cursors[1] = service.getContentResolver().query(
                MediaStore.Images.Media.INTERNAL_CONTENT_URI,
                new String[]{
                        MediaStore.Images.Media.DATA,
                },
                null,
                null,
                MediaStore.Images.ImageColumns.DATE_TAKEN + " ASC"
        );

        Cursor cursor = new MergeCursor(cursors);

        column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        column_index_folder_name = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
        while (cursor.moveToNext()) {
            absolutePathOfImage = cursor.getString(column_index_data);

            listOfAllImages.add(absolutePathOfImage);
        }
        return listOfAllImages;

    }


    private class doRequest extends AsyncTask<String, String, String> {
        // Store error message
        private Exception e = null;
        private VisionServiceClient client = null;
        private Bitmap imageToAnalyze = null;

        public doRequest(String imagePath) {

            if (client == null) {
                client = new VisionServiceRestClient(getString(R.string.subscription_key), getString(R.string.subscription_apiroot));
            }

            File file = new File(imagePath);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;

            if (file.exists()) {
                BitmapFactory.decodeFile(imagePath, options);
                int maxSideLength =
                        options.outWidth > options.outHeight ? options.outWidth : options.outHeight;
                options.inSampleSize = 1;

                int inSampleSize2 = 1;

                while (maxSideLength > 2 * 1280) {
                    maxSideLength /= 2;
                    inSampleSize2 *= 2;
                }

                options.inSampleSize = inSampleSize2;
                options.inJustDecodeBounds = false;
                Bitmap bitmap = BitmapFactory.decodeFile(imagePath, options);
                maxSideLength = bitmap.getWidth() > bitmap.getHeight()
                        ? bitmap.getWidth(): bitmap.getHeight();
                double ratio = 1280 / (double) maxSideLength;
                if (ratio < 1) {
                    imageToAnalyze = Bitmap.createScaledBitmap(
                            bitmap,
                            (int)(bitmap.getWidth() * ratio),
                            (int)(bitmap.getHeight() * ratio),
                            false);
                }
                else {
                    imageToAnalyze = bitmap;
                }
            }

        }

        @Override
        protected String doInBackground(String... args) {
            try {
                return process();
            } catch (Exception e) {
                this.e = e;    // Store error
            }

            return null;
        }

        private String process() throws VisionServiceException, IOException {
            Gson gson = new Gson();
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            if (imageToAnalyze != null) {
                imageToAnalyze.compress(Bitmap.CompressFormat.JPEG, 90, output);
                ByteArrayInputStream input = new ByteArrayInputStream(output.toByteArray());

                AnalysisResult result = this.client.describe(input, 1);
                String resultStr = gson.toJson(result);
                Log.d("VisionAPI - result", resultStr);
                return resultStr;
            }
            else {
                Log.d("VisionAPI", "imageToAnalyze is null");
                return "NULL";
            }
        }

        @Override
        protected void onPostExecute(String data) {
            super.onPostExecute(data);
            // Display based on error existence

            if (e != null) {
                this.e = null;
            } else {
                Gson gson = new Gson();
                AnalysisResult result = gson.fromJson(data, AnalysisResult.class);

                //for (Caption caption: result.description.captions) {
                //}

                for (String tag: result.description.tags) {
                    Log.d("VisionAPI - tags", tag);
                }

            }
        }
    }




}