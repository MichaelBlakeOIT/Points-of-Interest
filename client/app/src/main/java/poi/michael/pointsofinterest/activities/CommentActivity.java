package poi.michael.pointsofinterest.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
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

import poi.michael.pointsofinterest.utils.APIRequests;
import poi.michael.pointsofinterest.models.Comment;
import poi.michael.pointsofinterest.R;

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

    private class GetCommentsTask extends AsyncTask<Void, Void, List<Comment>> {

        @Override
        protected List<Comment> doInBackground(Void... params) {
            mListComments = new APIRequests(getApplicationContext()).getComments(mPoiId);
            return mListComments;
        }

        @Override
        protected void onPostExecute(final List<Comment> comments) {
            if (comments != null) {
                mCommentAdapter = new CommentAdapter(comments);

                mRecyclerView = (RecyclerView) findViewById(R.id.comments_recycler_view);
                mRecyclerView.setHasFixedSize(true);
                mRecyclerView.setLayoutManager(mLinearLayoutManager);
                mRecyclerView.setAdapter(mCommentAdapter);
            } else {
                Toast.makeText(getApplicationContext(), "An error occurred", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class CreateCommentTask extends AsyncTask<String, Void, Boolean> {

        String mCommentText;

        CreateCommentTask(String comment) {
            mCommentText = comment;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            return new APIRequests(getApplicationContext()).makeComment(mPoiId, mCommentText);
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (success) {
                SharedPreferences sharedPref = getSharedPreferences(getString(R.string.user_token), Context.MODE_PRIVATE);
                String username =  sharedPref.getString("username", "");
                mListComments.add(new Comment(mCommentText, 0, mPoiId, 0, username));
                mCommentAdapter.notifyDataSetChanged();
                mComment.getText().clear();
            } else {
                Toast.makeText(getApplicationContext(), "An error occurred", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void clear(View v) {
        mComment.setText("");
    }

    public void submit(View v) {
        new CreateCommentTask(mComment.getText().toString()).execute();
    }
}
