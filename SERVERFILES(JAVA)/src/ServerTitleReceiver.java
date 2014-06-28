import java.awt.BorderLayout;
import java.awt.TextArea;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.*;

import java.util.*;

import javax.swing.JScrollPane;

import org.quickconnectfamily.json.JSONInputStream;
import org.quickconnectfamily.json.JSONOutputStream;
import org.quickconnectfamily.json.JSONUtilities;

public class ServerTitleReceiver extends JFrame {

	//text area for displaying contents
	private JTextArea jta = new JTextArea();
	private InetAddress inetAddress;
	

	public static void main(String[] args){
		new ServerTitleReceiver();
	}

	
	public ServerTitleReceiver() {
		//Place text area on the frame
		setLayout(new BorderLayout());
		add(new JScrollPane(jta), BorderLayout.CENTER);
		setTitle("ServerTitleReceiver");
		setSize(500, 300);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true); //it is necessary to show the frame
		
		
		try {
			//create a server socket
			ServerSocket serverSocket = new ServerSocket(9292);
			jta.append("Server started at " + new Date() + '\n');

			while(true){
				// listen for a connection request
				Socket socket = serverSocket.accept();
				
				//Find the client's IpAddress
				inetAddress = socket.getInetAddress();
				jta.append(" " + inetAddress.getHostName() + "has joined" + "\n");
				
				
				// Creates a task for each connection 
				HandleClientTask task = new HandleClientTask(socket);
				
				// Wrap the task with a Thread and starts it
				new Thread(task).start();
				
				
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}


	// Creates a new Thread for each client. On each thread, a while
	// loop will be active to listen for incoming request from a client.
	class HandleClientTask implements Runnable{
		private JSONInputStream in;
		private JSONOutputStream out;
		private Socket socket;

		public HandleClientTask(Socket socket){
			this.socket = socket;
		}

		public void run() {
			try{
				// Create data input and output streams
				in = new JSONInputStream(socket.getInputStream());
				out = new JSONOutputStream(socket.getOutputStream());


				while (true){
					// Converts the stream to an HasMap Object
					@SuppressWarnings("rawtypes")
					HashMap aMap = (HashMap)in.readObject();
					System.out.println("Server recevied an object" + aMap);  // print out the object
					System.out.println(new JSONUtilities().stringify(aMap));
					
					// It is time to open the object body and examine its organs! lol.
					ArrayList<String> aData = (ArrayList) aMap.get("data");
					String aCommand = (String) aMap.get("command");

					String theTitle = aData.get(0);
	
					// Store the title in the database
					// ...
					// ...
					
					// Send respone to client 
					// ... 
					// ...

				}
			}catch (Exception ex){
				ex.printStackTrace();
			}
		}

	}
}

