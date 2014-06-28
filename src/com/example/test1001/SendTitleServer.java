package com.example.test1001;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.quickconnectfamily.json.JSONException;
import org.quickconnectfamily.json.JSONInputStream;
import org.quickconnectfamily.json.JSONOutputStream;


public class SendTitleServer {
	private static MainActivity mActivity;   //Reference to the main Activity component
	private static JSONOutputStream out;	 // Stream data OUT (PUSH)
	private static JSONInputStream in;		 // Stream data IN (PULL)!
	private static ToServer con = new ToServer();   // Object to connect to the Server
	
	
	//------------------------------------------------Constructor -------------------------------------------------------------------//
	public SendTitleServer(MainActivity ma){
		mActivity = ma;
	}
	
	
	//------------------------------------------------Local Methods -------------------------------------------------------------------//
	// Connects to Server <-- this is a task!
	public void connectToServer(){
		// Run the thread to do this task!
		new Thread(new ConnectToServerTask()).start();  
	}


	// Send the title to the Server  <-- this is a task!
	public void sendTitle(String title){
		// Run the thread to do this task!
		new Thread(new SendTitleTask(mActivity, title)).start();

	}
	
	
	// Task to connect to the Server--------------------------------INNER CLASS--------------------------------------------------------//
	private class ConnectToServerTask implements Runnable{
		@Override
		public void run() {
			con.connect();
		}
	}
	
		
	// Task to send title to the Server------------------------------INNER CLASS-------------------------------------------------------//
	private class SendTitleTask implements Runnable{
		private MainActivity mActivity;
		private String mTitle;

		public SendTitleTask(MainActivity mA, String title) {
			this.mActivity = mA;
			this.mTitle = title;
		}

		@Override
		public void run() {
			// Creates a CircleBean
			try{
				// Prepares Mr.bean!	
				TitleBean bean = new TitleBean();
				// Create Array-list for the data 
				ArrayList<String> aDataList = new ArrayList<>();
				aDataList.add(mTitle);

				bean.setDataCollection(aDataList);   //Sets the data that the Server needs to accomplish the command
				bean.setServerCommand("TITLE_IS_SENT"); // Sets the command for the server

				con.checkSocket();
			
				out.writeObject(bean);			
				mActivity.show("Title string was sent to server"); // Update the UI (user interface) 

			}catch (JSONException ex){
				ex.printStackTrace();
			}catch (Exception ex){
				ex.printStackTrace();
			}
		}

	}
	
	
	// This handles the connection to the Server----------------------------INNER CLASS------------------------------------------//
	public static class ToServer{
		private static Lock lock = new ReentrantLock(); 						// Create a lock
		private static Condition socketIsOpenCondition = lock.newCondition();   // Create a condition
		private static Socket SOCKET;			 								// Creates a socket
		private boolean stateOfObject= false;	                 				// Manages the STATE OF THE SOCKET
		

		//----------------------------------Local Methods --------------------------------------//
		
		//This will attempt to connect to the Server.
		public void connect(){
			lock.lock();
			try{
				// Connects to the Server
				SOCKET = new Socket("192.168.4.186", 9292);

				// update socket state
				stateOfObject = true;
				
				mActivity.show("Connected to the Server!");
				//create an input stream to receive data from the server (pull)
				in = new JSONInputStream(SOCKET.getInputStream());
				//create and output stream to send data to the server (push).
				out = new JSONOutputStream(SOCKET.getOutputStream());
				
				// If are other threads sleep, this command wakes them up. 
				socketIsOpenCondition.signalAll();
				
			}catch(IOException ex){
				mActivity.show("Class: connectionToServer, method: createSocket" + ex.getMessage());
			}catch(Exception ex){
				mActivity.show("Class: connectionToServer, method: createSocket" + ex.getMessage());
			}finally{
				lock.unlock();   //release the lock
			}
		}
		
		// This servers to help the threads to see if the Socket was created
		public void checkSocket(){
			lock.lock(); // thread acquires the lock 
			try{
				while(stateOfObject==false){
					socketIsOpenCondition.await();
				}
			}catch(InterruptedException ex){
				mActivity.show("Class: connectionToServer, method: checkSocket()" + ex.getMessage());
				
			}finally{
				lock.unlock();
			}
		}
	}
}