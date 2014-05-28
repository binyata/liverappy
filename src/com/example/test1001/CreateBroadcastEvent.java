// Package
package com.example.test1001;

// Imports
import java.io.IOException;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;

import android.os.AsyncTask;
//----------------------------------------------------Begin Class -------------------------------------------------------//
public class CreateBroadcastEvent extends AsyncTask<String, Void, String>{
	
	//-------------------------------------- Class Variables ------------------------------------------//
	
	//Attributes to get the Token
	protected MainActivity mActivity;
	protected String mScope;
	protected String mEmail;
	protected String token; 

	
	
	// -------------------------------------Constructor ----------------------------------------------//
	CreateBroadcastEvent(MainActivity activity, String name, String scope) {
		this.mActivity = activity;
		this.mScope = scope;
		this.mEmail = name;
	}
	//---------------------------------- End-of-Constructor-------------------------------------------//

	
	// ------------------------------------ Begin Methods --------------------------------------------//
	/**
	 * Executes the asynchronous job. This runs when you call "execute()"
	 * on the AsynchTask instance
	 */
	@Override
	protected String doInBackground(String... params) {
		try{
			String token = fetchToken();
			mActivity.show("Token fetched");
			if( token != null){
				mActivity.show("Token :" + token);
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
	
	
	

	
	
