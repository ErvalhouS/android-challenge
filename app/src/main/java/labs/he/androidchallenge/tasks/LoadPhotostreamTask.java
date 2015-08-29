/**
 * 
 */
package labs.he.androidchallenge.tasks;

import java.util.HashSet;
import java.util.Set;

import android.app.Activity;
import android.os.AsyncTask;
import android.widget.ListView;

import com.googlecode.flickrjandroid.Flickr;
import com.googlecode.flickrjandroid.oauth.OAuth;
import com.googlecode.flickrjandroid.oauth.OAuthToken;
import com.googlecode.flickrjandroid.people.User;
import com.googlecode.flickrjandroid.photos.PhotoList;

import labs.he.androidchallenge.AndroidChallengeActivity;
import labs.he.androidchallenge.FlickrHelper;
import labs.he.androidchallenge.R;
import labs.he.androidchallenge.images.LazyAdapter;

public class LoadPhotostreamTask extends AsyncTask<OAuth, Void, PhotoList> {

	/**
	 * 
	 */
	private ListView listView;
	private Activity activity;
	private int page;

	public LoadPhotostreamTask(Activity activity,
			ListView listView) {
		this.activity = activity;
		this.listView = listView;
		this.page = 1;
	}
	public LoadPhotostreamTask(Activity activity,
							   ListView listView, int page) {
		this.activity = activity;
		this.listView = listView;
		this.page = page;
		if (page<1)
			this.page=1;
	}

	@Override
	protected PhotoList doInBackground(OAuth... arg0) {
		OAuthToken token = arg0[0].getToken();
		Flickr f = FlickrHelper.getInstance().getFlickrAuthed(token.getOauthToken(),
				token.getOauthTokenSecret());
		Set<String> extras = new HashSet<String>();
		extras.add("url_sq"); //$NON-NLS-1$
		extras.add("url_l"); //$NON-NLS-1$
		extras.add("views"); //$NON-NLS-1$
		User user = arg0[0].getUser();
		try {
			return f.getPhotosInterface().getRecent(extras, 20 , page+200);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	@Override
	protected void onPostExecute(PhotoList result) {
		if (result != null) {
			if (this.listView.getAdapter()==null) {
				LazyAdapter adapter =
						new LazyAdapter(this.activity, result);
				this.listView.setAdapter(adapter);
			} else {
				((LazyAdapter)this.listView.getAdapter()).add(result);
				((LazyAdapter)this.listView.getAdapter()).notifyDataSetChanged();
				((AndroidChallengeActivity)activity).isLoading = false;
			}
		}
	}
	
}