package poi.michael.pointsofinterest.activities;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import poi.michael.pointsofinterest.utils.ImageTools;
import poi.michael.pointsofinterest.R;
import poi.michael.pointsofinterest.utils.volleySingleton;

public class SettingsActivity extends Activity {

    public static final int RESULT_LOAD_IMG = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Button saveButton = (Button) findViewById(R.id.save_settings_button);
        saveButton.setOnClickListener(mSaveSettingsListener);

        Button pictureButton = (Button) findViewById(R.id.settings_picture);
        pictureButton.setOnClickListener(mUploadPictureListener);
    }

    private View.OnClickListener mSaveSettingsListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v) {
            EditText password = (EditText) findViewById(R.id.change_password_text);
            EditText repeat = (EditText) findViewById(R.id.change_password_repeat_text);
            EditText bio = (EditText) findViewById(R.id.bio_text);
            if(password.getText().toString().equals(repeat.getText().toString())) {
                new ChangeSettingsTask(bio.getText().toString(), password.getText().toString()).execute();
            }
            else {
                Toast.makeText(getApplicationContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
            }
        }
    };

    private View.OnClickListener mUploadPictureListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v) {
            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
            photoPickerIntent.setType("image/*");
            startActivityForResult(photoPickerIntent, RESULT_LOAD_IMG);
        }
    };

    private class ChangeSettingsTask extends AsyncTask<Void, Void, Boolean> {

        private final String mBio;
        private final String mPassword;
        private Context mContext;

        ChangeSettingsTask(String bio, String password) {
            mBio = bio;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            String url = getResources().getString(R.string.base_url) + "/users";
            mContext = getApplicationContext();

            StringRequest postRequest = new StringRequest(Request.Method.PUT, url,
                    new Response.Listener<String>()
                    {
                        @Override
                        public void onResponse(String response) {
                            // response
                            try {
                                JSONObject JSONResponse = new JSONObject(response);
                                if (JSONResponse.getBoolean("success")) {
                                    Toast.makeText(getApplicationContext(), "Success", Toast.LENGTH_SHORT).show();
                                    SettingsActivity.this.finish();
                                } else {
                                    Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener()
                    {
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
                    params.put("bio", mBio);
                    if(!mPassword.equals("")) {
                        params.put("password", mPassword);
                    }
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
            volleySingleton.getInstance(mContext).getRequestQueue().add(postRequest);
            return true;
        }
    }

    private String getPath(Uri uri) throws URISyntaxException {
        final boolean needToCheckUri = Build.VERSION.SDK_INT >= 19;
        String selection = null;
        String[] selectionArgs = null;
        // Uri is different in versions after KITKAT (Android 4.4), we need to
        // deal with different Uris.
        if (needToCheckUri && DocumentsContract.isDocumentUri(getApplicationContext(), uri)) {
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                return Environment.getExternalStorageDirectory() + "/" + split[1];
            } else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                uri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
            } else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("image".equals(type)) {
                    uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                selection = "_id=?";
                selectionArgs = new String[] {
                        split[1]
                };
            }
        }
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = {
                    MediaStore.Images.Media.DATA
            };
            Cursor cursor = null;
            try {
                cursor = getContentResolver()
                        .query(uri, projection, selection, selectionArgs, null);
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    @Override
    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            try {
                SharedPreferences sharedPref = getSharedPreferences(getString(R.string.user_token), Context.MODE_PRIVATE);
                String username = sharedPref.getString("username", "");
                Uri uri = data.getData();
                String path = getPath(uri);
                ImageTools.uploadData(new File(path), "profile_photos/" + username + ".jpg", getApplicationContext());
            } catch (URISyntaxException e) {
                Log.e("SettingsActivity", "Unable to upload file from the given uri", e);
            }
        }
        else {
            Toast.makeText(getApplicationContext(), "You haven't picked Image",Toast.LENGTH_LONG).show();
        }
    }
}
