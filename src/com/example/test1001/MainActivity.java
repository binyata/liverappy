//Package
package com.example.test1001;


// Imports 
import com.google.android.gms.auth.GooglePlayServicesAvailabilityException;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.GooglePlayServicesUtil;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

//----------------------------------------------------Begin Class -------------------------------------------------------//
public class MainActivity extends Activity{
	
	//-------------------------------------- Class Variables ------------------------------------------//
	
	// A list of Keys 
	static final int REQUEST_CODE_PICK_ACCOUNT = 1000; 						// For picking an account
    static final int REQUEST_CODE_RECOVER_FROM_AUTH_ERROR = 1001;			// Use for error purposes
    static final int REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR = 1002;	// Use for error purposes

	
    // Android Display Components 
	public TextView tapLog; 			    // Create a text area panel
	private EditText txtTitle;		// Create a text field for input
	private Button btnCreateBroadCast;
	private EditText watch;
	private EditText dater;
	
	// Variables needed to get token and able to do API request
	private String mEmail;																	// Received from newChooseAccountIntent(); passed to getToken() 
	private static final String SCOPE = "oauth2:https://www.googleapis.com/auth/youtube";   // URI for scope 
	public String token;
	
	
	// -------------------------------------Start-onCreate() -----------------------------------------//	
	/**
	 * The first method that runs. It sets a default layout (activity_main)...
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//Set the layout
		setContentView(R.layout.activity_main);
		
		//Assigns display component
		this.tapLog = (TextView)(findViewById(R.id.tap_log_id));
		txtTitle = (EditText)(findViewById(R.id.txt_title));
		watch = (EditText)(findViewById(R.id.watch));
		dater = (EditText)(findViewById(R.id.dater));
		this.btnCreateBroadCast = (Button) findViewById(R.id.btn_createBroadcast_id);
		
		//Build buttons
		this.addButtons();
		
		
	}
	
	//-------------------------------------- End-onCreate() ------------------------------------------//
	
	
	
	
	
	
	// ------------------------------------- Begin Methods -------------------------------------------//
	
	/**
	 * 
	 */
	private void addButtons(){
		
		/* If there is nothing in the edittext, the btnCreateBroadcast is grayed out.
		 * If there is something, then is not grayed out. 
		 */
		txtTitle.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count){}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {}
			
			@Override
			public void afterTextChanged(Editable s) {
				if(txtTitle.getText().toString().equals(""))
					btnCreateBroadCast.setEnabled(false);
				if(txtTitle.getText().toString().length() > 0){
					btnCreateBroadCast.setEnabled(true);
				}
			}
		});	
	}
	
	
	
	/**
	 * It launches when a the "CreateBroadcast button is pressed. This method launches
	 * another function to get the token
	 */
	public void getCreateBroadcastButton(View view){
		createBroadcast();
	}
	
	
	/** 
	 * Creates the broadcast event
	 */
	private void createBroadcast(){
		if (mEmail == null) {
			show("Open dialog to get the email(username");
            pickUserAccount();
        } else {
				this.show("CreateBroadcastEvent is executed");
				new CreateBroadcastEvent(MainActivity.this, mEmail, SCOPE).execute();
				
		}
		
	}
	// return the time
	public String getwatch(){
		
		return watch.getText().toString();
		
	}
	
	public String getdater(){
		
		
		return dater.getText().toString();
	}

	
	/**
	 * Gets the title name for the broadcast
	 */
	public String getTitleName(){
		return txtTitle.getText().toString();
	}
	
	
	/**
	 * Opens an account picker dialog 
	 * Returns the email of the account picked
	 */
	private void pickUserAccount(){
		String[] accountTypes = new String[]{"com.google"};
		Intent intent = AccountPicker.newChooseAccountIntent(
				null, null, accountTypes,false, null, null, null, null);
		startActivityForResult(intent, REQUEST_CODE_PICK_ACCOUNT);
		this.show("- Pick Acoount");
	}

	
	
	/**
	 * Listener. It listens for incoming results from other methods
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		if(requestCode == REQUEST_CODE_PICK_ACCOUNT){ 
			if(resultCode == RESULT_OK){
				mEmail = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
				this.show("- Email received: " + mEmail);
				// With the account name acquired, go get the auth token
				createBroadcast();
			} else if (resultCode == RESULT_CANCELED){
				//The account picker dialog closed without selecting an account
				Toast.makeText(MainActivity.this, R.string.pick_account, Toast.LENGTH_LONG).show();
			}
		}else if ((requestCode == REQUEST_CODE_RECOVER_FROM_AUTH_ERROR           ||
				   requestCode == REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR) && 
				   resultCode  == RESULT_OK){
				// Receiving a result that follows a GoogleAuthException, try auth again
			createBroadcast();
		}
		// Later, more code will go here to handle the result from some exceptions..
	}
	
    
    /**
     * This method is a hook for background threads and async tasks that need to
     * provide the user a response UI when an exception occurs.
     */
    public void handleException(final Exception e) {
        // Because this call comes from the AsyncTask, we must ensure that the following
        // code instead executes on the UI thread.
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (e instanceof GooglePlayServicesAvailabilityException) {
                    // The Google Play services APK is old, disabled, or not present.
                    // Show a dialog created by Google Play services that allows
                    // the user to update the APK
                    int statusCode = ((GooglePlayServicesAvailabilityException)e)
                            .getConnectionStatusCode();
                    Dialog dialog = GooglePlayServicesUtil.getErrorDialog(statusCode,
                            MainActivity.this,
                            REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR);
                    dialog.show();
                } else if (e instanceof UserRecoverableAuthException) {
                    // Unable to authenticate, such as when the user has not yet granted
                    // the app access to the account, but the user can fix this.
                    // Forward the user to an activity in Google Play services.
                    Intent intent = ((UserRecoverableAuthException)e).getIntent();
                    startActivityForResult(intent,
                            REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR);
                }
            }
        });
    }
     
    
    /**
     * This method is a hook for background threads and async tasks that need to update the UI.
     * It does this by launching a runnable under the UI thread.
     */
    public void show(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tapLog.append(message + '\n');
            }
        });
    }
    
    
    public void setToken(String value){
    	this.token = value;
    }
    
   
  
    

    
 // ---------------------------------------- End Methods --------------------------------------------//
    	
}
//----------------------------------------------------End Class -------------------------------------------------------//
