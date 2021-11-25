import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;


@SuppressWarnings("InfiniteLoopStatement")
public class Server {
	static Vector<ClientHandler> playerList = new Vector<>();
	static int numberOfActivePlayers = 0;

	public static void main(String[] args) throws IOException {
		//Start a new server listening on port 5000
		ServerSocket server = new ServerSocket(5000);
		Socket socket;

		while(true){
			try{
				//accepts a new client
				socket = server.accept();
				System.out.println("NEW CLIENT");

				//Creates the sockets for reading and writing strings
				DataInputStream input = new DataInputStream(socket.getInputStream());
				DataOutputStream output = new DataOutputStream(socket.getOutputStream());

				//Creates the sockets for reading and writing the GameStateVariable objects
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
	DataInputStream inputStream;
	DataOutputStream outputStream;
	final Socket socket;
	ObjectInputStream objectInputStream;
	ObjectOutputStream objectOutputStream;
	BrickTagGameVariables btgVariables;

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
			if (clientHandlerLoop()){
				break;
			}
		}
		logoutClient();
	}

	private boolean clientHandlerLoop() {
		String received = receiveString();
		boolean didSendMessage = false;
		if(received.equals("logout")){
			return true;
		}else if(received.equals("NEW_MAP")){
			receiveGameState();
		}else if(btgVariables.currentState==BrickTagGame.STARTUPSTATE) {
			didSendMessage = checkStartControls(received);
		}else if(btgVariables.currentState==BrickTagGame.PLAYINGSTATE) {
			didSendMessage = checkPlayingControls(received);
		}

		if(!didSendMessage) {
			writeToClient("", this.outputStream);
		}
		return false;
	}

	private void logoutClient(){
		try {
			Server.numberOfActivePlayers--;
			Server.playerList.remove(this);
			this.socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendVariablesToClient(){
		for(ClientHandler ch : Server.playerList) {
			try {
				//Tells the client there is a change and then sends the updated btgVariable instance
				writeToClient("CHANGE", ch.outputStream);
				BrickTagGameVariables temp = this.btgVariables;
				ch.objectOutputStream.reset();
				ch.objectOutputStream.writeObject(temp);
				ch.objectOutputStream.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public String receiveString(){
		try {
			return inputStream.readUTF();
		} catch (IOException ignored) {
//			e.printStackTrace();
		}
		return "";
	}

	private void writeToClient(String s,DataOutputStream localOutputStream){
		try {
			localOutputStream.writeUTF(s);
			localOutputStream.flush();
		} catch (IOException ignored) {}
	}

	private boolean checkStartControls(String input){
		if(input.equals("SPACE")){
			this.btgVariables.currentState = BrickTagGame.PLAYINGSTATE;
			sendVariablesToClient();
			return true;
		}
		return false;
	}

	private boolean checkPlayingControls(String input){
		if(input.equals("0")){
			this.btgVariables.toggleShowGrid();
		}

		if(!input.equals("")){
			sendVariablesToClient();
			return true;
		}else{
			return false;
		}
	}

	public void receiveGameState(){
		try {
			for(ClientHandler ch : Server.playerList) {
				ch.btgVariables = (BrickTagGameVariables) ch.objectInputStream.readObject();
			}
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
}
