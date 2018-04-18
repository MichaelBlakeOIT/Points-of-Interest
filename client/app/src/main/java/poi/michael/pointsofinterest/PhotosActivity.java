package poi.michael.pointsofinterest;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PhotosActivity extends Activity {

    int mId;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private String mCurrentPhotoPath;
    private File photoFile;
    private String mImageFileName;
    ImageAdapter adapter;
    private ArrayList<Bitmap> photos;
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
        new GetPhotosTask().execute();
    }

    public void dispatchTakePictureIntent(View v) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            //File photoFile = null;
            try {
                photoFile = createImageFile();

            } catch (IOException ex) {
                Log.e("PhotosActivity", ex.toString());
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            //URI photo = (URI) data.getExtras().get("data");
            if (photoFile != null) {
                String filename = UUID.randomUUID().toString();
                ImageTools.uploadData(photoFile, "pois/" + filename, getApplicationContext());
                new RegisterPhotoTask(filename).execute();
                new GetPhotosTask().execute();
            }
            //uploadData(mCurrentPhotoPath, mImageFileName);
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        //String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        mImageFileName = "JPEG_" + UUID.randomUUID().toString() + "_";
        //mImageFileName = "JPEG_" + timeStamp + "_";
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
                            try {
                                JSONObject JSONResponse = new JSONObject(response);
                                if (JSONResponse.getBoolean("success")) {
                                    Toast.makeText(getApplicationContext(), "Success", Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d("Error.Response", error.toString());
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
                                    JSONArray photos;
                                    photos = JSONResponse.getJSONArray("data");
                                    if (photos.length() > 0) {
                                        new GetImagesTask().execute(photos);
                                    }
                                }
                            }
                            catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d("Error.Response", error.toString());
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

        @Override
        protected void onPostExecute(Boolean x) {

        }
    }

    private class GetImagesTask extends AsyncTask<JSONArray, Void, Boolean> {

        ProgressDialog progDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progDialog = new ProgressDialog(PhotosActivity.this);
            progDialog.setMessage("Loading...");
            progDialog.setIndeterminate(false);
            progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progDialog.setCancelable(true);
            progDialog.show();
        }

        @Override
        protected Boolean doInBackground(JSONArray... photos_json) {
            JSONObject photo;

            for(int i = 0; i < photos_json[0].length(); i++) {
                try {
                    photo = photos_json[0].getJSONObject(i);
                    String name = photo.getString("filename");
                    InputStream in = new java.net.URL("https://s3.us-east-2.amazonaws.com/points-of-interest/pois/" + name).openStream();
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = 4;
                    photos.add(BitmapFactory.decodeStream(in, null, options));
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean x) {
            adapter.notifyDataSetChanged();
            progDialog.dismiss();
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
