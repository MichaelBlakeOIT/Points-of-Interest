package poi.michael.pointsofinterest.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import poi.michael.pointsofinterest.interfaces.APIInterface;
import poi.michael.pointsofinterest.models.Response;
import poi.michael.pointsofinterest.utils.APIRequests;
import poi.michael.pointsofinterest.models.Comment;
import poi.michael.pointsofinterest.R;
import retrofit2.Call;
import retrofit2.Callback;

public class CommentActivity extends Activity {
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private List<Comment> mListComments = new ArrayList<>();
    private int mPoiId;
    private EditText mComment;
    private CommentAdapter mCommentAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        Intent intentExtras = getIntent();
        mPoiId = intentExtras.getIntExtra("poi_id", 0);
        mComment = findViewById(R.id.comment_box);

        mLinearLayoutManager = new LinearLayoutManager(this);

        loadComments();
    }

    private void loadComments() {
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.user_token), Context.MODE_PRIVATE);
        String token = "Bearer " + sharedPref.getString("token", "");

        APIInterface APIInterface = new APIRequests(getApplicationContext()).getInterface();
        APIInterface.getCommentList(mPoiId, token).enqueue(new Callback<Response<List<Comment>>>() {
            @Override
            public void onResponse(Call<Response<List<Comment>>> call, retrofit2.Response<Response<List<Comment>>> response) {
                if (response.isSuccessful()) {
                    mCommentAdapter = new CommentAdapter(response.body().getData());
                    //TODO: LIVEDATA
                    mRecyclerView = findViewById(R.id.comments_recycler_view);
                    mRecyclerView.setHasFixedSize(true);
                    mRecyclerView.setLayoutManager(mLinearLayoutManager);
                    mRecyclerView.setAdapter(mCommentAdapter);
                }
                else {
                    //error
                }
            }

            @Override
            public void onFailure(Call<Response<List<Comment>>> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "An error occurred", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createComment(String comment) {
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.user_token), Context.MODE_PRIVATE);
        String token = "Bearer " + sharedPref.getString("token", "");

        APIInterface APIInterface = new APIRequests(getApplicationContext()).getInterface();
        APIInterface.createComment(mPoiId, comment, token).enqueue(new Callback<Response<Comment>>() {
            @Override
            public void onResponse(Call<Response<Comment>> call, retrofit2.Response<Response<Comment>> response) {
                if (response.isSuccessful()) {
                    SharedPreferences sharedPref = getSharedPreferences(getString(R.string.user_token), Context.MODE_PRIVATE);
                    String username =  sharedPref.getString("username", "");
                    mListComments.add(new Comment(response.body().getData().getComment(), 0, mPoiId, 0, username));
                    mCommentAdapter.notifyDataSetChanged();
                    mComment.getText().clear();
                }
            }

            @Override
            public void onFailure(Call<Response<Comment>> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "An error occurred", Toast.LENGTH_SHORT).show();
            }
        });
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
                username = layout.findViewById(R.id.comment_username);
                text = layout.findViewById(R.id.comment_text);
            }

            private void bindView(int pos) {
                Comment item = comments.get(pos);
                layout.setTag(this);
                username.setText(item.getUsername());
                text.setText(item.getComment());
            }
        }
    }

    public void clear(View v) {
        mComment.setText("");
    }

    public void submit(View v) {
        /*new CreateCommentTask(mComment.getText().toString()).execute();*/
        createComment(mComment.getText().toString());
    }
}
