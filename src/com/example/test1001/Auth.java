package com.example.test1001;


import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;

public class Auth {
	//YouTUbe stuff
	protected MainActivity mActivity;
	public static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
	public static final JsonFactory JSON_FACTORY = new JacksonFactory();
	protected String token;
	private TokenResponse tokenRes;
		
		
	public Auth(String token){
		this.token = token;
		this.tokenRes = new TokenResponse();
		this.tokenRes.setAccessToken(token);
	}
	
	// Gets the Credential object
	public Credential getCredential(){
		// calls the private method to create a credential object
		return createCredentialWithAccessTokenOnly(HTTP_TRANSPORT, JSON_FACTORY, this.tokenRes);
	}
	
	// Creates a Credential object .
	private Credential createCredentialWithAccessTokenOnly(
			HttpTransport transport
			, JsonFactory jsonFactory
			, TokenResponse tokenResponse) {

		return new Credential(BearerToken.authorizationHeaderAccessMethod()).setFromTokenResponse(
				tokenResponse);
	}
	
}
