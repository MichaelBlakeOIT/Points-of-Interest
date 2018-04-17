package poi.michael.pointsofinterest;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.AmazonS3Client;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PhotosActivity extends Activity {

    int mId;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_TAKE_PHOTO = 1;
    private String mCurrentPhotoPath;
    private String mImageFileName;
    ImageAdapter adapter;
    private ArrayList<Bitmap> photos;
    //private ArrayList<ImageView> photos;
    private ListView mLv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photos);

        Intent intentExtras = getIntent();
        intentExtras.getExtras();
        mId = intentExtras.getIntExtra("poi_id", 0);
        mLv = (ListView) findViewById(R.id.photos_list);
        photos = new ArrayList<>();
        adapter = new ImageAdapter(this, photos);
        mLv.setAdapter(adapter);
        //adapter = new SimpleAdapter(getBaseContext(), , R.layout.listview_layout, from, to);
        //adapter = new ArrayAdapter<>(this, R.layout.single_image, photos);
        //setListAdapter(adapter);
        new GetPhotosTask().execute();
    }

    public void dispatchTakePictureIntent(View v) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            //Bundle extras = data.getExtras();
            //Bitmap imageBitmap = (Bitmap) extras.get("data");
            //mImageView.setImageBitmap(imageBitmap);
            uploadData(mCurrentPhotoPath, mImageFileName);
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        mImageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                mImageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    public void uploadData(String path, final String filename) {
        File image = new File(path);

        // Initialize AWSMobileClient if not initialized upon the app startup.
        //AWSMobileClient.getInstance().initialize(this).execute();

        TransferUtility transferUtility =
                TransferUtility.builder()
                        .defaultBucket("points-of-interest")
                        .context(getApplicationContext())
                        .s3Client(new AmazonS3Client( new BasicAWSCredentials( getResources().getString(R.string.aws_key),getResources().getString(R.string.aws_secret)) ))
                        .build();

        TransferObserver uploadObserver =
                transferUtility.upload(
                        "pois/" + filename,
                        image);

        uploadObserver.setTransferListener(new TransferListener() {

            @Override
            public void onStateChanged(int id, TransferState state) {
                if (TransferState.COMPLETED == state) {
                    Toast.makeText(getApplicationContext(), "Success",Toast.LENGTH_LONG).show();
                    new RegisterPhotoTask(filename).execute();
                    new GetPhotosTask().execute();
                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                float percentDonef = ((float)bytesCurrent/(float)bytesTotal) * 100;
                int percentDone = (int)percentDonef;

                Log.d("MainActivity", "   ID:" + id + "   bytesCurrent: " + bytesCurrent + "   bytesTotal: " + bytesTotal + " " + percentDone + "%");
            }

            @Override
            public void onError(int id, Exception ex) {
                Toast.makeText(getApplicationContext(), "An error occurred",Toast.LENGTH_LONG).show();
            }

        });

        // If your upload does not trigger the onStateChanged method inside your
        // TransferListener, you can directly check the transfer state as shown here.
        if (TransferState.COMPLETED == uploadObserver.getState()) {
            // Handle a completed upload.
        }
    }

    private class RegisterPhotoTask extends AsyncTask<Void, Void, Boolean> {
        private Context mContext;
        private String mName;

        RegisterPhotoTask(String name) {
            mName = name;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            String url = getResources().getString(R.string.base_url) + "/poi/" + mId + "/photos";

            mContext = getApplicationContext();

            StringRequest ratingRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            // response
                            try {
                                JSONObject JSONResponse = new JSONObject(response);
                                if (JSONResponse.getBoolean("success")) {
                                    Toast.makeText(getApplicationContext(), "Success", Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            //Log.d("Response", response);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // error
                            //Log.d("Error.Response", error.toString());
                        }
                    }
            ) {
                @Override
                protected Map<String, String> getParams()
                {
                    Map<String, String>  params = new HashMap<>();
                    params.put("filename", mName);
                    return params;
                }

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    SharedPreferences sharedPref = getSharedPreferences(getString(R.string.user_token), Context.MODE_PRIVATE);

                    Map<String, String> params = new HashMap<String, String>();
                    params.put("Authorization", "Bearer " + sharedPref.getString("token", ""));

                    return params;
                }
            };
            volleySingleton.getInstance(mContext).getRequestQueue().add(ratingRequest);
            return true;
        }
    }

    private class GetPhotosTask extends AsyncTask<Void, Void, Boolean> {
        private Context mContext;
        private String mName;

        @Override
        protected Boolean doInBackground(Void... params) {
            String url = getResources().getString(R.string.base_url) + "/poi/" + mId + "/photos";

            mContext = getApplicationContext();

            StringRequest ratingRequest = new StringRequest(Request.Method.GET, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            // response
                            try {
                                JSONObject JSONResponse = new JSONObject(response);

                                if (JSONResponse.getBoolean("success")) {
                                    JSONArray photos = JSONResponse.getJSONArray("data");
                                    new GetImagesTask().execute(photos);
                                }
                            }
                            catch (JSONException e) {
                                e.printStackTrace();
                            }
                            //Log.d("Response", response);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // error
                            //Log.d("Error.Response", error.toString());
                        }
                    }
            ) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    SharedPreferences sharedPref = getSharedPreferences(getString(R.string.user_token), Context.MODE_PRIVATE);

                    Map<String, String> params = new HashMap<String, String>();
                    params.put("Authorization", "Bearer " + sharedPref.getString("token", ""));

                    return params;
                }
            };
            volleySingleton.getInstance(mContext).getRequestQueue().add(ratingRequest);
            return true;
        }
    }

    private class GetImagesTask extends AsyncTask<JSONArray, Void, Boolean> {

        @Override
        protected Boolean doInBackground(JSONArray... photos_json) {
            JSONObject photo;
            //ArrayList<ImageView> ivs = new ArrayList<>();
            //ArrayList<Bitmap> bms = new ArrayList<>();

            for(int i = 0; i < photos_json[0].length(); i++) {
                try {
                    photo = photos_json[0].getJSONObject(i);
                    String name = photo.getString("filename");
                    InputStream in = new java.net.URL("https://s3.us-east-2.amazonaws.com/points-of-interest/pois/" + name).openStream();
                    photos.add(BitmapFactory.decodeStream(in));
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean x) {
            /*for(int i = 0; i < images.size(); i++) {
                photos.add(images.get(i));
            }*/
            adapter.notifyDataSetChanged();
        }
    }

    public class ImageAdapter extends BaseAdapter {

        private Context context;
        private ArrayList<Bitmap> imageArrayList;


        public ImageAdapter(Context context, ArrayList<Bitmap> imageModelArrayList) {

            this.context = context;
            this.imageArrayList = imageModelArrayList;

        }

        @Override
        public int getItemViewType(int position) {

            return position;
        }

        @Override
        public int getCount() {
            return imageArrayList.size();
        }

        @Override
        public Object getItem(int position) {
            return imageArrayList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(context);
                convertView = inflater.inflate(R.layout.single_image, null, false);
            }
            ImageView iv = (ImageView) convertView.findViewById(R.id.singleImage);
            iv.setImageBitmap(photos.get(position));

            return iv;
        }
    }
}
