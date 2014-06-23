// Package
package com.example.test1001;

// Imports
import java.io.IOException;
import java.util.Calendar;

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

	/*
	 * Define a global instance of a Youtube object, which will be used
	 * to make YouTube Data API requests.
	 */
	private static YouTube youtube;

	
	
	
	// -------------------------------------Constructor ----------------------------------------------//
	CreateBroadcastEvent(MainActivity activity, String name, String scope) {
		this.mActivity = activity;
		this.mScope = scope;
		this.mEmail = name;
	}
	//---------------------------------- End-of-Constructor-------------------------------------------//


	// ------------------------------------ doInBackground() --------------------------------------------//


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
				// calls the method to construct the Youtube object. 
				this.createYoutubeBroadcast(new Auth(token));
			}
		}catch(IOException ex){
			// The fetchToken() method handles Google-specific exceptions,
			// so this indicates something went wrong at a higher level.
			// TIP: Check for network connectivity before starting the AsyncTask.
			mActivity.show("IOException"  + ex.getLocalizedMessage());
		}catch(Exception ex){
			mActivity.show("IOException"  + ex.getMessage());
		}
		return null;
	}
	// ------------------------------------ Begin Method --------------------------------------------//

	/*
	 * This method creates the Youtube Object and of all its components (i.e. title, time,)
	 * However, this is bad coding. The method needs to be spit into classes. 
	 */
	
	private void createYoutubeBroadcast(Auth auth){
		
		
		Calendar cal = Calendar.getInstance();      // set the current Date, time, and TimeZone
//		cal.add(Calendar.HOUR, +1);   				// Adds one more hour because the YouTube Servers are in a pacific zone. 
		
		try{ 

			// This object is used to make YouTube Data API requests.
			youtube = new YouTube.Builder(
					Auth.HTTP_TRANSPORT
					, Auth.JSON_FACTORY
					, auth.getCredential()).setApplicationName("Martin").build();
			mActivity.show("Authentication passed." + '\n' + "Youtube Object created");     	 // debug code							
 
								
			// Creates a Broadcast snippet object
			LiveBroadcastSnippet broadcastSnippet = new LiveBroadcastSnippet();
					// Get the title for the broadcast event
					broadcastSnippet.setTitle(mActivity.getTitleName());

					
			broadcastSnippet.setScheduledStartTime(new DateTime(cal.getTime()));
			mActivity.show("Time: " + cal.getTime());
//			broadcastSnippet.setScheduledStartTime(new DateTime("2014-06-19T20:23:00-06:00")); 
//			broadcastSnippet.setScheduledStartTime(new DateTime(mActivity.getdater() + "T20:" + mActivity.getwatch()));


			// Creates a Broadcast status object
			LiveBroadcastStatus status = new LiveBroadcastStatus();
					// Set attributes 
					status.setPrivacyStatus("public");                      
			
			// Creates a broadcast object (a mix of Snippet and Status)
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
			mActivity.show("Broadcast event object created Successfully!");
			
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
			// Fetches the token using the context of the Activity, email, and scope. 
			return GoogleAuthUtil.getToken(mActivity, mEmail, mScope);
			
		} catch (UserRecoverableAuthException userRecoverableException) {
			/*
			 * Unable to authenticate because of the following reasons:
			 * - the user needs to grant the permissions to the app so it can 
			 *   access private data of the account (user consent)
			 * - the user needs to re-enter the password 
			 */
			
			//Publish the error on the UI threat text field area
			mActivity.show("UserRecoverableAuthException has occurred."); 
			
			// Runs the handleException() method found in the MainActivity to handle the error
			mActivity.handleException(userRecoverableException);

		
		} catch (GoogleAuthException fatalException) {
			mActivity.show(fatalException.getMessage() + '\n' + fatalException.getLocalizedMessage());
		}
		return null;
	}









	// ---------------------------------------- End Methods --------------------------------------------//


}
//----------------------------------------------------End Class -------------------------------------------------------//






