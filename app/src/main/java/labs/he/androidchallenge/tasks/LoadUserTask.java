/**
 * 
 */
package labs.he.androidchallenge.tasks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.widget.ImageView;
import android.widget.Toast;

import com.googlecode.flickrjandroid.Flickr;
import com.googlecode.flickrjandroid.oauth.OAuth;
import com.googlecode.flickrjandroid.oauth.OAuthToken;
import com.googlecode.flickrjandroid.people.User;
import labs.he.androidchallenge.FlickrHelper;
import labs.he.androidchallenge.AndroidChallengeActivity;
import labs.he.androidchallenge.R;
import labs.he.androidchallenge.images.ImageUtils.DownloadedDrawable;

public class LoadUserTask extends AsyncTask<OAuth, Void, User> {
	/**
	 * 
	 */
	private final AndroidChallengeActivity AndroidChallengeActivity;
	private ImageView userIconImage;
	private final Logger logger = LoggerFactory.getLogger(LoadUserTask.class);
	
	public LoadUserTask(AndroidChallengeActivity AndroidChallengeActivity, 
			ImageView userIconImage) {
		this.AndroidChallengeActivity = AndroidChallengeActivity;
		this.userIconImage = userIconImage;
	}
	
	/**
	 * The progress dialog before going to the browser.
	 */
	private ProgressDialog mProgressDialog;
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		mProgressDialog = ProgressDialog.show(AndroidChallengeActivity,
				"", AndroidChallengeActivity.getString(R.string.loading_your_information)); //$NON-NLS-1$ //$NON-NLS-2$
		mProgressDialog.setCanceledOnTouchOutside(true);
		mProgressDialog.setCancelable(true);
		mProgressDialog.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dlg) {
				LoadUserTask.this.cancel(true);
			}
		});
	}

	/* (non-Javadoc)
	 * @see android.os.AsyncTask#doInBackground(Params[])
	 */
	@Override
	protected User doInBackground(OAuth... params) {
		OAuth oauth = params[0];
		User user = oauth.getUser();
		OAuthToken token = oauth.getToken();
		try {
			Flickr f = FlickrHelper.getInstance()
					.getFlickrAuthed(token.getOauthToken(), token.getOauthTokenSecret());
			return f.getPeopleInterface().getInfo(user.getId());
		} catch (Exception e) {
			Toast.makeText(AndroidChallengeActivity, e.toString(), Toast.LENGTH_LONG).show();
			logger.error(e.getLocalizedMessage(), e);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
	 */
	@Override
	protected void onPostExecute(User user) {
		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
		}
		if (user == null) {
			return;
		}
		AndroidChallengeActivity.setUser(user);
		if (user.getBuddyIconUrl() != null) {
			String buddyIconUrl = user.getBuddyIconUrl();
	        if (userIconImage != null) {
	        	ImageDownloadTask task = new ImageDownloadTask(userIconImage);
	            Drawable drawable = new DownloadedDrawable(task);
	            userIconImage.setImageDrawable(drawable);
	            task.execute(buddyIconUrl);
	        }
		}
	}
	
	
}