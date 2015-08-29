package labs.he.androidchallenge;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.googlecode.flickrjandroid.photos.Photo;
import com.googlecode.flickrjandroid.photos.comments.Comment;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import labs.he.androidchallenge.images.ImageUtils;
import labs.he.androidchallenge.tasks.ImageDownloadTask;

import static labs.he.androidchallenge.AndroidChallengeActivity.flickr;

/**
 * Created by FernandoHenrique on 28/08/2015.
 */
public class PagerActivity extends SherlockActivity {
    String position;
    Photo photo;
    public ImageView image;
    public List<Comment> comments;
    public Comment comment;
    public ImageView icon;
    public LoadPhotoTask photoInitializer;
    public Date min;
    public Date max;
    public LoadCommentsTask commentsInitializer;

    @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.pager_activity);
            String[] tags = getIntent().getStringArrayExtra("tags");
            position = getIntent().getStringExtra("Position");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(tags[0]);
        }
    @Override
    protected void onResume(){
        super.onResume();
        Calendar c = Calendar.getInstance();
        min = c.getTime();
        c.set(Calendar.MONTH, 3);
        max = c.getTime();
        photoInitializer = new LoadPhotoTask(this);
        photoInitializer.execute();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public String timeDifference(Date startDate, Date endDate){
        long millis = endDate.getTime() - startDate.getTime();
        int Hours = (int) (millis/(1000 * 60 * 60));
        int Mins = (int) (millis % (1000*60*60));

        return (Hours>0 ? Hours + " h" : "") + Mins + " m";
    }

    public class LoadPhotoTask extends AsyncTask<Void, Void, Photo> {

        /**
         *
         */
        private Activity activity;

        public LoadPhotoTask(Activity activity) {
            this.activity = activity;
        }

        @Override
        protected Photo doInBackground(Void ...args) {
            try {
                return flickr.getPhotosInterface().getPhoto(position);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Photo result) {
            if (result != null) {
                ((TextView)activity.findViewById(R.id.author)).setText(result.getOwner().getUsername());
                ((TextView)activity.findViewById(R.id.desc)).setText(Html.fromHtml(result.getDescription()));
                icon = (ImageView)findViewById(R.id.icon);
                if (icon != null) {
                    ImageDownloadTask task = new ImageDownloadTask(icon);
                    Drawable drawable = new ImageUtils.DownloadedDrawable(task);
                    icon.setImageDrawable(drawable);
                    task.execute(result.getOwner().getBuddyIconUrl());
                }
                ((TextView)activity.findViewById(R.id.views)).setText(result.getViews()+" ");
                image = (ImageView)activity.findViewById(R.id.pager);
                if (image != null) {
                    ImageDownloadTask task = new ImageDownloadTask(image);
                    Drawable drawable = new ImageUtils.DownloadedDrawable(task);
                    image.setImageDrawable(drawable);
                    image.setAlpha(1f);
                    task.execute(result.getLargeUrl());
                }
                commentsInitializer = new LoadCommentsTask(activity);
                commentsInitializer.execute(result);
            }
        }

    }
    public class LoadCommentsTask extends AsyncTask<Photo, Void, List<Comment>> {

        /**
         *
         */
        private Activity activity;

        public LoadCommentsTask(Activity activity) {
            this.activity = activity;
        }

        @Override
        protected List<Comment> doInBackground(Photo ...args) {
            try {
                return flickr.getCommentsInterface().getList(args[0].getId(),min,max);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Comment> result) {
            if (result != null) {
                LinearLayout list = (LinearLayout)activity.findViewById(R.id.comments);
                for (int i=0; i<result.size(); i++) {
                    comment = result.get(i);
                    View vi = getLayoutInflater().inflate(R.layout.comment_item, null);
                    ((TextView)vi.findViewById(R.id.comment)).setText(comment.getText());
                    ((TextView)vi.findViewById(R.id.author)).setText(comment.getAuthor());
                    ((TextView)vi.findViewById(R.id.time)).setText(timeDifference(comment.getDateCreate(), min));
                    list.addView(vi);
                }
            }
        }

    }
}
