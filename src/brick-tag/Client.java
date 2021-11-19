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
			this.inputStream = new DataInputStream(socket.getInputStream());
			this.outputStream = new DataOutputStream(socket.getOutputStream());
			objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
			objectOutputStream.flush();
			objectInputStream = new ObjectInputStream(socket.getInputStream());

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void receiveGameState(){
		try {
			System.out.println("RGS");
			this.brickTagGameVariables = (BrickTagGameVariables) objectInputStream.readObject();
			System.out.println(this.brickTagGameVariables.currentState);
			System.out.println("RGSR");
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
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

	public void checkIfNeedToGetNewGameState(){
		try {
			String message = inputStream.readUTF();
			System.out.println(message);
			if(message.equals("CHANGE")){
				receiveGameState();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
