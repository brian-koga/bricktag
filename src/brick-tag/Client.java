import org.lwjgl.Sys;

import java.io.*;
import java.net.*;

public class Client {
	DataInputStream inputStream;
	DataOutputStream outputStream;
	Socket socket;

	public Client(){
		try {
			socket = new Socket("127.0.0.1",5000);

			this.inputStream = new DataInputStream(socket.getInputStream());
			this.outputStream = new DataOutputStream(socket.getOutputStream());
		} catch (IOException ignored) {}
	}

	public void sendString(String s){
		try{
			outputStream.writeUTF(s);
			if(s.equals("logout")){
				socket.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
