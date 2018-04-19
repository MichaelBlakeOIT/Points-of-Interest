package poi.michael.pointsofinterest;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommentActivity extends Activity {
    private RecyclerView mRecyclerView;

    private LinearLayoutManager mLinearLayoutManager;

    private List<Comment> list_comments = new ArrayList<>();
    private int mPoiId;
    private EditText mComment;
    private CommentAdapter mCommentAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        Intent intentExtras = getIntent();
        mPoiId = intentExtras.getIntExtra("poi_id", 0);
        mComment = (EditText) findViewById(R.id.comment_box);

        mLinearLayoutManager = new LinearLayoutManager(this);

        new GetCommentsTask().execute();
    }

    private class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {

        private List<Comment> comments;

        private CommentAdapter(List<Comment> comments) {
            super();
            this.comments = comments;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.comment_layout, parent, false));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            if (holder == null) {
                return;
            }
            holder.bindView(position);
        }

        @Override
        public int getItemCount() {
            return comments.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            TextView username;
            TextView text;
            View layout;

            private ViewHolder(View itemView) {
                super(itemView);
                layout = itemView;
                username = (TextView) layout.findViewById(R.id.comment_username);
                text = (TextView) layout.findViewById(R.id.comment_text);
            }

            private void bindView(int pos) {
                Comment item = comments.get(pos);
                layout.setTag(this);
                username.setText(item.getUsername());
                text.setText(item.getComment());
            }
        }
    }

    private class GetCommentsTask extends AsyncTask<Void, Void, Boolean> {
        private Context mContext;

        @Override
        protected Boolean doInBackground(Void... params) {
            String url = getResources().getString(R.string.base_url) + "/poi/" + mPoiId + "/comments";

            mContext = getApplicationContext();

            StringRequest saveRequest = new StringRequest(Request.Method.GET, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject JSONResponse = new JSONObject(response);

                                JSONArray comments = JSONResponse.getJSONArray("data");

                                for(int i = 0; i < comments.length(); i++) {
                                    JSONObject comment = comments.getJSONObject(i);
                                    String commentText = comment.getString("comment");
                                    int commentId = comment.getInt("comment_id");
                                    int userId = comment.getInt("user_id");
                                    int poiId = comment.getInt("point_of_interest_id");
                                    String username = comment.getString("username");

                                    list_comments.add(new Comment(commentText, userId, poiId, commentId, username));
                                }
                                mCommentAdapter = new CommentAdapter(list_comments);

                                mRecyclerView = (RecyclerView) findViewById(R.id.comments_recycler_view);
                                mRecyclerView.setHasFixedSize(true);
                                mRecyclerView.setLayoutManager(mLinearLayoutManager);
                                mRecyclerView.setAdapter(mCommentAdapter);
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
                public Map<String, String> getHeaders() throws AuthFailureError {
                    SharedPreferences sharedPref = getSharedPreferences(getString(R.string.user_token), Context.MODE_PRIVATE);

                    Map<String, String> params = new HashMap<String, String>();
                    params.put("Authorization", "Bearer " + sharedPref.getString("token", ""));

                    return params;
                }
            };
            volleySingleton.getInstance(mContext).getRequestQueue().add(saveRequest);
            return true;
        }
    }

    private class CreateCommentTask extends AsyncTask<Void, Void, Boolean> {
        private Context mContext;
        private String mCommentText;

        CreateCommentTask(String comment) {
            mCommentText = comment;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            String url = getResources().getString(R.string.base_url) + "/poi/" + mPoiId + "/comments";

            mContext = getApplicationContext();

            StringRequest saveRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            // response
                            try {
                                JSONObject JSONResponse = new JSONObject(response);
                                if (JSONResponse.getBoolean("success")) {
                                    //Toast.makeText(getApplicationContext(), "Successfully added comment", Toast.LENGTH_SHORT).show();
                                    SharedPreferences sharedPref = getSharedPreferences(getString(R.string.user_token), Context.MODE_PRIVATE);
                                    String username =  sharedPref.getString("username", "");
                                    list_comments.add(new Comment(mCommentText, 0, mPoiId, 0, username));
                                    mCommentAdapter.notifyDataSetChanged();
                                    mComment.getText().clear();
                                } else {
                                    Toast.makeText(getApplicationContext(), "An error occurred", Toast.LENGTH_SHORT).show();
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
                public Map<String, String> getHeaders() throws AuthFailureError {
                    SharedPreferences sharedPref = getSharedPreferences(getString(R.string.user_token), Context.MODE_PRIVATE);

                    Map<String, String> params = new HashMap<String, String>();
                    params.put("Authorization", "Bearer " + sharedPref.getString("token", ""));

                    return params;
                }

                @Override
                protected Map<String, String> getParams()
                {
                    Map<String, String>  params = new HashMap<String, String>();
                    params.put("comment", mCommentText);

                    return params;
                }
            };
            volleySingleton.getInstance(mContext).getRequestQueue().add(saveRequest);
            return true;
        }
    }

    public void clear(View v) {
        mComment.setText("");
    }

    public void submit(View v) {
        new CreateCommentTask(mComment.getText().toString()).execute();
    }
}
