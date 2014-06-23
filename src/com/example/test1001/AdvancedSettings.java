package com.example.test1001;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class AdvancedSettings extends Activity {
	
	private EditText watch;
	private EditText dater;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_advanced_settings);
		
		watch = (EditText)(findViewById(R.id.watch));
		dater = (EditText)(findViewById(R.id.dater));
		
		
	
	}
	
	
	// return the time
	public String getwatch(){
		return watch.getText().toString();
	}

	public String getdater(){
		return dater.getText().toString();
	}

	
	public void goToYoutubeAccount(View view){
		Uri webpage = Uri.parse("https://www.youtube.com/my_live_events");
		Intent webIntent = new Intent(Intent.ACTION_VIEW, webpage); //action , data
		startActivity(webIntent);
	}
	
	
}
