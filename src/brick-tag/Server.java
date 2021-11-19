import org.lwjgl.Sys;
import java.io.*;
import java.net.*;
import java.util.*;

@SuppressWarnings("InfiniteLoopStatement")
public class Server {
	static Vector<ClientHandler> playerList = new Vector<>();
	static int numberOfActivePlayers = 0;
//	static BrickTagGameServer btg;

	public static void main(String[] args) throws IOException {
		//Start a new server listening on port 5000
		ServerSocket server = new ServerSocket(5000);
		Socket socket;

//		Server.btg = new BrickTagGameServer("Brick Tag!", 1280, 720);

		while(true){
			try{
				//accepts a new client
				socket = server.accept();
				System.out.println("NEW CLIENT");

				DataInputStream input = new DataInputStream(socket.getInputStream());
				DataOutputStream output = new DataOutputStream(socket.getOutputStream());

				ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
				objectOutputStream.flush();
				ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());

				//Assigning a new thread for the current client
				ClientHandler clientHandler = new ClientHandler(socket,input,output,objectOutputStream,objectInputStream,new BrickTagGameVariables(720,1280));
				Thread thread = new Thread(clientHandler);
				playerList.add(clientHandler);
				thread.start();
				numberOfActivePlayers++;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}

class ClientHandler implements Runnable{
	final DataInputStream inputStream;
	final DataOutputStream outputStream;
	final Socket socket;
	ObjectInputStream objectInputStream;
	ObjectOutputStream objectOutputStream;
	BrickTagGameVariables btgVariables;
	String received;

	ClientHandler(Socket socket, DataInputStream inputStream, DataOutputStream outputStream,ObjectOutputStream objectOutputStream,ObjectInputStream objectInputStream,BrickTagGameVariables btg) throws IOException {
		this.socket = socket;
		this.inputStream = inputStream;
		this.outputStream = outputStream;
		this.objectOutputStream = objectOutputStream;
		this.objectInputStream = objectInputStream;
		this.btgVariables = btg;
	}

	@Override
	public void run(){
		while (true){
			try {
				received = inputStream.readUTF();
//				System.out.println(received);
				if(received.equals("SPACE")) {
					this.btgVariables.currentState = BrickTagGame.PLAYINGSTATE;
					System.out.println("PLAYING");
					sendVariables();
				} else if(received.equals("logout")){
					Server.numberOfActivePlayers--;
					this.socket.close();
					break;
				}
//				else if(received.equals("")){
//					sendVariables();
//				}
//
				this.outputStream.writeUTF("");
			} catch (IOException ignored) {}
		}
//		try {
//			this.inputStream.close();
//			this.outputStream.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
	}

	public void sendVariables(){
		for(ClientHandler ch : Server.playerList) {
			try {
				System.out.println(btgVariables.currentState);
				ch.outputStream.writeUTF("CHANGE");
				ch.objectOutputStream.writeObject(this.btgVariables);
				ch.objectOutputStream.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public String receiveString(){
		try {
			return inputStream.readUTF();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}
}
