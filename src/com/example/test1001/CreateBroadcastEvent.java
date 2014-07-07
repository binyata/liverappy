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
import com.google.api.services.youtube.YouTube.LiveBroadcasts.Bind;
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

	//Attributes to get the Token!
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

			
			//----------------------------------------------------------Youtube object ------------------------------------------------
			// This object is used to make YouTube Data API requests.
			youtube = new YouTube.Builder(
					  Auth.HTTP_TRANSPORT
					, Auth.JSON_FACTORY
					, auth.getCredential()).setApplicationName("Martin").build();
			mActivity.show("Authentication passed." + '\n' 
					     + "Youtube Object created by the Youtube.Builder");     	 
 
			//-----------------------------------------------------Broadcast --------------------------------------------------------
			// Creates a liveBroadcast-snippet object
			LiveBroadcastSnippet broadcastSnippet = new LiveBroadcastSnippet();
					// Get the title for the broadcast event
					broadcastSnippet.setTitle(mActivity.getTitleName());
					broadcastSnippet.setScheduledStartTime(new DateTime(cal.getTime()));  //Sets current time
//					broadcastSnippet.setScheduledStartTime(new DateTime("2014-06-19T20:23:00-06:00")); 

			
			// Creates a liveBroadcast-status object
			LiveBroadcastStatus status = new LiveBroadcastStatus();
					// Set status
					status.setPrivacyStatus("public");                      
			
			// Creates a broadcast object or resource (status + snippet) 
			// The broadcast resource contains the basic data about the event (title, start time, end time, privacy.)
			LiveBroadcast broadcast = new LiveBroadcast();
					broadcast.setKind("youtube#liveBroadcast");
					broadcast.setSnippet(broadcastSnippet);
					broadcast.setStatus(status);
					
			// Constructs or prepares the API request to insert the broadcast-resource in the YouTube servers 
			YouTube.LiveBroadcasts.Insert liveBroadcastInsert =
					youtube.liveBroadcasts().insert("snippet,status", broadcast);    
			
			// Execute the API request (sends it to the YouTube service) and catch the return result
			LiveBroadcast returnedBroadcast = liveBroadcastInsert.execute();      
		
			
			//--------------------------------------------------Stream-----------------------------------------------------------
			
			// Create a liveStream-Snippet object
			LiveStreamSnippet streamSnippet = new LiveStreamSnippet();
				streamSnippet.setTitle(mActivity.getTitleName() + "IO");
			
			
			// Creates a cdn object (Brief description of the live stream cdn settings) 
			CdnSettings cdnSettings = new CdnSettings();
				cdnSettings.setFormat("240p");
				cdnSettings.setIngestionType("rtmp");
				
			// Creates a liveStream object or resource (snippet + cdn)
			// The liveStream resource contains information about the video stream that you are transmitting to Youtube. 
			LiveStream stream = new LiveStream();
				stream.setKind("youtube#liveStream");
				stream.setSnippet(streamSnippet);
				stream.setCdn(cdnSettings);
				

			// Construct or prepares the API request to insert the stream-resource in the YouTube Servers
			YouTube.LiveStreams.Insert liveStreamInsert =
					youtube.liveStreams().insert("snippet, cdn", stream);  
			
			// Execute the API request (sends it to the YouTube service) and catch the return result
			LiveStream returnedStream = liveStreamInsert.execute();         
			
			
			mActivity.show("Stream Name: " + returnedStream.getCdn().getIngestionInfo().getStreamName());
			mActivity.show("Primary-IngestionAddress: " + returnedStream.getCdn().getIngestionInfo().getIngestionAddress());
			mActivity.show("Backup-IngestionAddress: " + returnedStream.getCdn().getIngestionInfo().getBackupIngestionAddress());
			
			
			//---------------------------------------------------Bind----------------------------------------------------------
			// Having created your liveBroadcast and liveStream resources, you now need to associate the two using the liveBroadcasts.bind method. 
			// This action links the video that you will transmit to YouTube to the broadcast that the video is for.
			YouTube.LiveBroadcasts.Bind liveBroadcastBind =
					youtube.liveBroadcasts().bind(returnedBroadcast.getId(), "id,contentDetails"); // provide the broadcast object  
					liveBroadcastBind.setStreamId(returnedStream.getId()); 						   // associate the stream object
					
			returnedBroadcast = liveBroadcastBind.execute(); 
			
			
			mActivity.show("https://www.youtube.com/watch?v=" + returnedBroadcast.getId());
			mActivity.show("Broadcast-Event created Successfully!");
			
			//-------------------------------------------------------------------------------------------------------------
			
			

			
		
			
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






