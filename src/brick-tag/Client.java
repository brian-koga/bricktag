import java.io.*;
import java.net.*;

public class Client {
	DataInputStream inputStream;
	DataOutputStream outputStream;
	Socket socket;
	ObjectOutputStream objectOutputStream;
	ObjectInputStream objectInputStream;
	BrickTagGameVariables brickTagGameVariables;

	public Client(BrickTagGameVariables btgV){
		try {
			socket = new Socket("127.0.0.1",5000);
//			socket = new Socket("192.168.1.2",5000);

			this.brickTagGameVariables = btgV;
			createStreams();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void createStreams(){
		try {
			//Creates the sockets that read and write strings
			this.inputStream = new DataInputStream(socket.getInputStream());
			this.outputStream = new DataOutputStream(socket.getOutputStream());

			//Creates the sockets that read and write objects
			objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
			objectOutputStream.flush();
			objectInputStream = new ObjectInputStream(socket.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void receiveGameState(){
		try {
			this.brickTagGameVariables = null;
			this.setBrickTagGameVariables((BrickTagGameVariables) objectInputStream.readObject());
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void sendString(String s){
		try{
			outputStream.writeUTF(s);
			outputStream.flush();
			if(s.equals("logout")){
				socket.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendPos(float pos){
		try {
			outputStream.writeFloat(pos);
			outputStream.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void checkIfNeedToGetNewGameState(){
		try {
			String message = inputStream.readUTF();
			if(message.equals("CHANGE")){
				receiveGameState();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setBrickTagGameVariables(BrickTagGameVariables brickTagGameVariables) {
		this.brickTagGameVariables = brickTagGameVariables;
	}
}
