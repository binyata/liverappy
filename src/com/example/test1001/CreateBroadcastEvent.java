// Package
package com.example.test1001;

// Imports
import java.io.IOException;





import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.api.client.util.DateTime;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.CdnSettings;
import com.google.api.services.youtube.model.LiveBroadcast;
import com.google.api.services.youtube.model.LiveBroadcastSnippet;
import com.google.api.services.youtube.model.LiveBroadcastStatus;
import com.google.api.services.youtube.model.LiveStream;
import com.google.api.services.youtube.model.LiveStreamSnippet;

import android.os.AsyncTask;
//----------------------------------------------------Begin Class -------------------------------------------------------//
public class CreateBroadcastEvent extends AsyncTask<Void, Void, Void>{
	
	//-------------------------------------- Class Variables ------------------------------------------//
	
	//Attributes to get the Token
	protected MainActivity mActivity;
	protected String mScope;
	protected String mEmail;

	
	//Attributes to do the YouTube API request (post)
	private static YouTube youtube;
	
	// -------------------------------------Constructor ----------------------------------------------//
	CreateBroadcastEvent(MainActivity activity, String name, String scope) {
		this.mActivity = activity;
		this.mScope = scope;
		this.mEmail = name;
	}
	//---------------------------------- End-of-Constructor-------------------------------------------//

	
	// ------------------------------------ Begin Method --------------------------------------------//
	

	/*
	 * Executes the asynchronous job. This runs when you call "execute()"
	 * on the AsynchTask instance
	 * 
	 * AsynchTask: When this class is instantiated (which is in the MainActivity as "new CreateBroadcastEvent(MainActivity.this, mEmail, SCOPE).execute()") 
	 * 			   a threat is created. And when the .execute() is used, the doInBackground(...)method is CALL!
	 * 
	 * What it does:
	 * 1. Fetches the token
	 * 2. Creates the broadcast. It does this by calling the createYoutubeBroadcast(Auth auth) method and
	 *    requires an auth object as the parameter. The auth object needs to be created with a token as a parameter.
	 * 3. For now, lets leave the exceptions alone.
	 * 
	 * Please read this to be more informed how AsynTask works:
	 * http://developer.android.com/guide/components/processes-and-threads.html
	 */
	@Override
	protected Void doInBackground(Void... params) {
		try{
			String token = fetchToken();
			if( token != null){
				
				this.createYoutubeBroadcast(new Auth(token));
			}
		}catch(IOException ex){
			// The fetchToken() method handles Google-specific exceptions,
			// so this indicates something went wrong at a higher level.
			// TIP: Check for network connectivity before starting the AsyncTask.
			mActivity.show(ex.getLocalizedMessage());
		}catch(Exception ex){
			mActivity.show(ex.getMessage());
		}

		return null;
	}
	
	
	
	
	/*
	 * This method creates the Youtube Object and of all its components (i.e. title, time,)
	 * THIS NEEDS TO BE DEELPLY ANALYZED. This method needs to be split up in other methods or classes.
	 */
	private void createYoutubeBroadcast(Auth auth){
		try{
		// Builds the YouTube Object 
			
					// This method needs to be broken down
					youtube = new YouTube.Builder(
							Auth.HTTP_TRANSPORT
							, Auth.JSON_FACTORY
							, auth.getCredential()).setApplicationName("Martin").build();
					mActivity.show("youtube object created");
					// Prompt the user to enter a title for the broadcast.
					LiveBroadcastSnippet broadcastSnippet = new LiveBroadcastSnippet();
					broadcastSnippet.setTitle(mActivity.getTitleName());
					broadcastSnippet.setScheduledStartTime(new DateTime("2014-06-15T20:23:00-06:00"));
					


					// Create a snippet with the title and scheduled start and end
					// times for the broadcast. Currently, those times are hard-coded.
					LiveBroadcastStatus status = new LiveBroadcastStatus();
					status.setPrivacyStatus("private");
					LiveBroadcast broadcast = new LiveBroadcast();
					broadcast.setKind("youtube#liveBroadcast");
					broadcast.setSnippet(broadcastSnippet);
					broadcast.setStatus(status);

					// Construct and execute the API request to insert the broadcast.
					YouTube.LiveBroadcasts.Insert liveBroadcastInsert =
							youtube.liveBroadcasts().insert("snippet,status", broadcast);
					LiveBroadcast returnedBroadcast = liveBroadcastInsert.execute();

					// Create a snippet with the video stream's title.
					LiveStreamSnippet streamSnippet = new LiveStreamSnippet();
					streamSnippet.setTitle(mActivity.getTitleName() + "IO");
					CdnSettings cdnSettings = new CdnSettings();
					cdnSettings.setFormat("240p");
					cdnSettings.setIngestionType("rtmp");
					LiveStream stream = new LiveStream();
					stream.setKind("youtube#liveStream");
					stream.setSnippet(streamSnippet);
					stream.setCdn(cdnSettings);

					// Construct and execute the API request to insert the stream.
					YouTube.LiveStreams.Insert liveStreamInsert =
							youtube.liveStreams().insert("snippet,cdn", stream);
					LiveStream returnedStream = liveStreamInsert.execute();

					YouTube.LiveBroadcasts.Bind liveBroadcastBind =
							youtube.liveBroadcasts().bind(returnedBroadcast.getId(), "id,contentDetails");
					liveBroadcastBind.setStreamId(returnedStream.getId());
					returnedBroadcast = liveBroadcastBind.execute();  
					mActivity.show("BroadCast and LiveStream created Successfully!");
		}catch(IOException ex){
			// The fetchToken() method handles Google-specific exceptions,
			// so this indicates something went wrong at a higher level.
			// TIP: Check for network connectivity before starting the AsyncTask.
			mActivity.show(ex.getLocalizedMessage());
		}catch(Exception ex){
			mActivity.show(ex.getMessage());
		}

	}
	
	
	
	/**
	 * Get a authentication token if one is not available. If the error is not recoverable then
	 * it displays the error message on parent activity right away.
	 */

	protected String fetchToken() throws IOException {
		try {
			return GoogleAuthUtil.getToken(mActivity, mEmail, mScope);
		} catch (UserRecoverableAuthException userRecoverableException) {
			// Unable to authenticate, such as when the user has not yet granted
			// the app access to the account, but the user can fix this.
			// Forward the user to an activity in Google Play services.

			mActivity.show("UserRecoverableAuthException");
			mActivity.handleException(userRecoverableException);

		} catch (GoogleAuthException fatalException) {
			mActivity.show(fatalException.getMessage() + '\n' + fatalException.getLocalizedMessage());
		}
		return null;
	}
	
	
	
	
	
	
	

	
	// ---------------------------------------- End Methods --------------------------------------------//
	
	
}
//----------------------------------------------------End Class -------------------------------------------------------//
	
	
	

	
	
