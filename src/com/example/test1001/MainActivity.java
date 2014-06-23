//Package
package com.example.test1001;


// Imports 
import com.google.android.gms.auth.GooglePlayServicesAvailabilityException;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.ConnectionResult;
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
	static final int REQUEST_CODE_GOOGLE_PLAY_SERVICES = 1001;	// Use for error purposes
    static final int REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR = 1002;	// Use for error purposes

	
    // Android Display Components 
	public TextView tapLog; 			    // Create a text area panel
	private EditText txtTitle;		// Create a text field for input
	private Button btnCreateBroadCast;
	
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

		this.btnCreateBroadCast = (Button) findViewById(R.id.btn_createBroadcast_id);
		
		//Build buttons
		this.addButtons();
		
	}
	
	//-------------------------------------- End-onCreate() ------------------------------------------//
	
	
	
	
	
	
	// ------------------------------------- Begin Methods -------------------------------------------//
	
	/**
	 * Makes the "CreateBroadcast" button responsive with the respective textField
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
		// Check if GooglePlayService library is installed
		int statusCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);  // The statuscode contains the code if it exists 
		if (statusCode == ConnectionResult.SUCCESS){				// If is a success then proceed the normal operation
			createBroadcast();
		} else if (GooglePlayServicesUtil.isUserRecoverableError(statusCode)) {
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(
                    statusCode, this, 0 /* request code not used */);
            dialog.show();
        } else {
            Toast.makeText(this, "Unrecoverable Error", Toast.LENGTH_SHORT).show();
        }

	}
		

	
	
	/**
	 * Launches the Activity "Advance Settings"
	 */
	public void getAdvanceSettings(View view){
		Intent intent = new Intent(this, AdvancedSettings.class);
		startActivity(intent);
	}
	
	
	
	
	
	
	/** 
	 * Creates the broadcast event
	 */
	private void createBroadcast(){
		// If the app doesn't know the email of the user, then it will prompt the user to select his/her account to get the email
		if (mEmail == null) {
			show("Open dialog to get the email(username");
            pickUserAccount();
            // Once the app knows the email of end-user, it will execute a threat which will get the token 
        } else {
				this.show("CreateBroadcastEvent class is instantiated");
				new CreateBroadcastEvent(MainActivity.this, mEmail, SCOPE).execute();
		}
		
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
		// REQUEST_CODE_PICK_ACCOUNT
		startActivityForResult(intent, REQUEST_CODE_PICK_ACCOUNT);
		this.show("Pick Acoount Dialog pop up");
	}

	
	
	/**
	 * Listener. It listens for incoming results from other activities
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		// The "requestCode" is used by the system to identify the request. The callback(subsequent activity)
		// provides the same request code so that your App can properly identify the result and determine 
		// how to handle it. The Result_OK is there to identify if the operation was successful or not. 
		if(requestCode == REQUEST_CODE_PICK_ACCOUNT){ 
			if(resultCode == RESULT_OK){
				// Handling the result. It reads the data by getting the email.  
				mEmail = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
				this.show("Result received from pickUserAccount dialog: " + mEmail);
				// With the account name acquired, go get the auth token
				createBroadcast();
			} else if (resultCode == RESULT_CANCELED){
				//The account picker dialog closed without selecting an account
				Toast.makeText(MainActivity.this, R.string.pick_account, Toast.LENGTH_LONG).show();
			}
		// Check if the result is coming from UserRecoverableAuthoExpcetion/user consent component
		}else if (requestCode == REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR && 
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
					
					// Get the intent received from the exception object and run the intent.
					// If the problem is that the the user needs to do the user consent, then
					// the intent will display a dialog that prompts the user to grant permissions 
					// to the app. After that is done, a result will come and be handled by the 
					// onActivityResult(). 
					
					// Also notices the "request code" that is passed with the request.
					// This way, when the user completes the appropriate action to resolve
					// the exception, your onActivityResult() method receives an intent that 
					// includes this request code and you can try to acquire the auth token again.
					Intent intent = ((UserRecoverableAuthException)e).getIntent();
					startActivityForResult(intent,
							REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR);
				}
			}
		});
	}

    
    /**
     * Access the UI thread from other threats. You can use this as a debuging tool.
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
