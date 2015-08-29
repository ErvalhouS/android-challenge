/**
 * 
 */
package labs.he.androidchallenge.images;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.googlecode.flickrjandroid.photos.Photo;
import com.googlecode.flickrjandroid.photos.PhotoList;
import labs.he.androidchallenge.R;
import labs.he.androidchallenge.images.ImageUtils.DownloadedDrawable;
import labs.he.androidchallenge.tasks.ImageDownloadTask;

public class LazyAdapter extends BaseAdapter {
    
    private Activity activity;
    private PhotoList photos;
    private static LayoutInflater inflater=null;
    
    public LazyAdapter(Activity a, PhotoList d) {
        activity = a;
        photos = d;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
        return photos.size();
    }

    public Photo getItem(int position) {
        return photos.get(position);
    }

    public long getItemId(int position) {
        return position;
    }
    
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        if(convertView == null)
            vi = inflater.inflate(R.layout.row, null);
        TextView title=(TextView)vi.findViewById(R.id.imageTitle);
        TextView owner=(TextView)vi.findViewById(R.id.ownerText);
        TextView comments=(TextView)vi.findViewById(R.id.commentsText);
        ImageView image=(ImageView)vi.findViewById(R.id.imageIcon);
        Photo photo = photos.get(position);
        title.setText(photo.getTitle());
        owner.setText(photo.getOwner().getUsername());
        if (photo.getComments()>0)
        comments.setText(Integer.toString(photo.getComments()));
        if (image != null) {
        	ImageDownloadTask task = new ImageDownloadTask(image);
            Drawable drawable = new DownloadedDrawable(task);
            image.setImageDrawable(drawable);
            task.execute(photo.getSmallSquareUrl());
        }
        ImageView viewIcon = (ImageView)vi.findViewById(R.id.viewIcon);
        if (photo.getViews() >= 0) {
        	viewIcon.setImageResource(R.drawable.views);
        	TextView viewsText = (TextView)vi.findViewById(R.id.viewsText);
        	viewsText.setText(String.valueOf(photo.getViews()));
        } else {
        	viewIcon.setImageBitmap(null);
        }
        
        return vi;
    }

    public void add(PhotoList result) {
        photos.addAll(result);
    }
}
