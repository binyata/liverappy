package com.example.test1001;

import java.io.IOException;


import com.google.api.client.util.DateTime;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.CdnSettings;
import com.google.api.services.youtube.model.LiveBroadcast;
import com.google.api.services.youtube.model.LiveBroadcastSnippet;
import com.google.api.services.youtube.model.LiveBroadcastStatus;
import com.google.api.services.youtube.model.LiveStream;
import com.google.api.services.youtube.model.LiveStreamSnippet;

public class YoutubeBroadcast{
	private static YouTube youtube;
	protected MainActivity mActivity;
	private Auth auth;

	public YoutubeBroadcast(String token) {
		try{
			mActivity.show("Getting Authenticate");
			auth = new Auth(token);

			// Builds the Youtube Object 
			youtube = new YouTube.Builder(
					  Auth.HTTP_TRANSPORT
					, Auth.JSON_FACTORY
					, auth.getCredential()).setApplicationName("Martin").build();
			mActivity.show("youtube object created");
			// Prompt the user to enter a title for the broadcast.
			LiveBroadcastSnippet broadcastSnippet = new LiveBroadcastSnippet();
			broadcastSnippet.setTitle(mActivity.getTitleName());
			broadcastSnippet.setScheduledStartTime(new DateTime("2014-05-27T20:23:00-06:00"));


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




}
