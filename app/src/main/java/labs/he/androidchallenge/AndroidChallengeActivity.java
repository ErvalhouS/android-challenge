package labs.he.androidchallenge;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.googlecode.flickrjandroid.Flickr;
import com.googlecode.flickrjandroid.oauth.OAuth;
import com.googlecode.flickrjandroid.oauth.OAuthToken;
import com.googlecode.flickrjandroid.people.User;
import com.googlecode.flickrjandroid.photos.Photo;

import labs.he.androidchallenge.images.LazyAdapter;
import labs.he.androidchallenge.tasks.GetOAuthTokenTask;
import labs.he.androidchallenge.tasks.LoadPhotostreamTask;
import labs.he.androidchallenge.tasks.LoadUserTask;
import labs.he.androidchallenge.tasks.OAuthTask;

public class AndroidChallengeActivity extends SherlockActivity {
	public static final String CALLBACK_SCHEME = "androidchallenge-oauth"; //$NON-NLS-1$
	public static final String PREFS_NAME = "androidchallenge-pref"; //$NON-NLS-1$
	public static final String KEY_OAUTH_TOKEN = "flickrj-android-oauthToken"; //$NON-NLS-1$
	public static final String KEY_TOKEN_SECRET = "flickrj-android-tokenSecret"; //$NON-NLS-1$
	public static final String KEY_USER_NAME = "flickrj-android-userName"; //$NON-NLS-1$
	public static final String KEY_USER_ID = "flickrj-android-userId"; //$NON-NLS-1$
	
	private static final Logger logger = LoggerFactory.getLogger(AndroidChallengeActivity.class);


	public boolean isLoading;
	private ListView listView;
	private TextView textUserTitle;
	private TextView textUserName;
	private TextView textUserId;
	private ImageView userIcon;
	private ImageButton refreshButton;
	private ImageView image;
	public static AndroidChallengeActivity activity;
	public static Flickr flickr;
	private int counter;


	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		final OAuth oauth = getOAuthToken();
		this.textUserTitle = (TextView) this.findViewById(R.id.profilePageTitle);
		this.textUserName = (TextView) this.findViewById(R.id.userScreenName);
		this.textUserId = (TextView) this.findViewById(R.id.userId);
		this.userIcon = (ImageView) this.findViewById(R.id.userImage);
		this.listView = (ListView) this.findViewById(R.id.imageList);
		this.refreshButton = (ImageButton) this.findViewById(R.id.btnRefreshUserProfile);
		counter = 1;
		activity = this;
		if (oauth == null || oauth.getUser() == null) {
			OAuthTask task = new OAuthTask(this);
			task.execute();
		} else {
			load(oauth);
		}
		assert oauth != null;
		OAuthToken token = oauth.getToken();
		flickr = FlickrHelper.getInstance().getFlickrAuthed(token.getOauthToken(),
				token.getOauthTokenSecret());
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
				Photo pSelected = ((LazyAdapter)listView.getAdapter()).getItem(i);
				String[] tags = { pSelected.getTitle() };
				Intent intent = new Intent(activity, PagerActivity.class);
				intent.putExtra("tags", tags);
				intent.putExtra("Position", pSelected.getId());
				startActivity(intent);

			}
		});

		this.refreshButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				load(getOAuthToken());
			}
		});
		listView.setOnScrollListener(new AbsListView.OnScrollListener() {
			public int totalItemCount;
			public int currentFirstVisibleItem;
			public int currentScrollState;
			public int currentVisibleItemCount;

			@Override
			public void onScrollStateChanged(AbsListView absListView, int i) {
				this.currentScrollState = i;
			}

			@Override
			public void onScroll(AbsListView absListView, int i, int i1, int i2) {
				this.currentFirstVisibleItem = i;
				this.currentVisibleItemCount = i1;
				this.totalItemCount = i2;
				this.isScrollCompleted();
			}
			private void isScrollCompleted() {
				if (currentFirstVisibleItem > totalItemCount-6 && totalItemCount != 0) {
					/*** In this way I detect if there's been a scroll which has completed ***/
					/*** do the work for load more date! ***/
					if(!isLoading){
						isLoading = true;
						counter++;
						new LoadPhotostreamTask(activity, listView,counter).execute(oauth);
					}
				}
			}
		});


	}
	private void load(OAuth oauth) {
		if (oauth != null) {
			new LoadUserTask(this, userIcon).execute(oauth);
			new LoadPhotostreamTask(this, listView).execute(oauth);
		}
	}
    @Override
    public void onDestroy() {
    	listView.setAdapter(null);
        super.onDestroy();
    }
    
	/* (non-Javadoc)
	 * @see android.app.Activity#onNewIntent(android.content.Intent)
	 */
	@Override
	protected void onNewIntent(Intent intent) {
		//this is very important, otherwise you would get a null Scheme in the onResume later on.
		setIntent(intent);
	}
	
	public void setUser(User user) {
		textUserTitle.setText(user.getUsername());
		textUserName.setText(user.getRealName());
		textUserId.setText(user.getId());
	}
	
	public ImageView getUserIconImageView() {
		return this.userIcon;
	}

	@Override
	public void onResume() {
		super.onResume();
		Intent intent = getIntent();
		String scheme = intent.getScheme();
		OAuth savedToken = getOAuthToken();
		if (CALLBACK_SCHEME.equals(scheme) && (savedToken == null || savedToken.getUser() == null)) {
			Uri uri = intent.getData();
			String query = uri.getQuery();
			logger.debug("Returned Query: {}", query); //$NON-NLS-1$
			String[] data = query.split("&"); //$NON-NLS-1$
			if (data != null && data.length == 2) {
				String oauthToken = data[0].substring(data[0].indexOf("=") + 1); //$NON-NLS-1$
				String oauthVerifier = data[1]
						.substring(data[1].indexOf("=") + 1); //$NON-NLS-1$
				logger.debug("OAuth Token: {}; OAuth Verifier: {}", oauthToken, oauthVerifier); //$NON-NLS-1$

				OAuth oauth = getOAuthToken();
				if (oauth != null && oauth.getToken() != null && oauth.getToken().getOauthTokenSecret() != null) {
					GetOAuthTokenTask task = new GetOAuthTokenTask(this);
					task.execute(oauthToken, oauth.getToken().getOauthTokenSecret(), oauthVerifier);
				}
			}
		}

	}
    
    public void onOAuthDone(OAuth result) {
		if (result == null) {
			Toast.makeText(this,
					"Não autorizado!", //$NON-NLS-1$
					Toast.LENGTH_LONG).show();
		} else {
			User user = result.getUser();
			OAuthToken token = result.getToken();
			if (user == null || user.getId() == null || token == null
					|| token.getOauthToken() == null
					|| token.getOauthTokenSecret() == null) {
				Toast.makeText(this,
						"Não autorizado!", //$NON-NLS-1$
						Toast.LENGTH_LONG).show();
				return;
			}
			String message = "Login realizado com sucesso!";
			Toast.makeText(this,
					message,
					Toast.LENGTH_LONG).show();
			saveOAuthToken(user.getUsername(), user.getId(), token.getOauthToken(), token.getOauthTokenSecret());
			load(result);
		}
	}
    
    
    public OAuth getOAuthToken() {
    	 //Restore preferences
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String oauthTokenString = settings.getString(KEY_OAUTH_TOKEN, null);
        String tokenSecret = settings.getString(KEY_TOKEN_SECRET, null);
        if (oauthTokenString == null && tokenSecret == null) {
        	logger.warn("No oauth token retrieved"); //$NON-NLS-1$
        	return null;
        }
        OAuth oauth = new OAuth();
        String userName = settings.getString(KEY_USER_NAME, null);
        String userId = settings.getString(KEY_USER_ID, null);
        if (userId != null) {
        	User user = new User();
        	user.setUsername(userName);
        	user.setId(userId);
        	oauth.setUser(user);
        }
        OAuthToken oauthToken = new OAuthToken();
        oauth.setToken(oauthToken);
        oauthToken.setOauthToken(oauthTokenString);
        oauthToken.setOauthTokenSecret(tokenSecret);
        logger.debug("Retrieved token from preference store: oauth token={}, and token secret={}", oauthTokenString, tokenSecret); //$NON-NLS-1$
        return oauth;
    }
    
    public void saveOAuthToken(String userName, String userId, String token, String tokenSecret) {
    	logger.debug("Saving userName=%s, userId=%s, oauth token={}, and token secret={}", new String[]{userName, userId, token, tokenSecret}); //$NON-NLS-1$
    	SharedPreferences sp = getSharedPreferences(PREFS_NAME,
				Context.MODE_PRIVATE);
		Editor editor = sp.edit();
		editor.putString(KEY_OAUTH_TOKEN, token);
		editor.putString(KEY_TOKEN_SECRET, tokenSecret);
		editor.putString(KEY_USER_NAME, userName);
		editor.putString(KEY_USER_ID, userId);
		editor.commit();
    }

}