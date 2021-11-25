import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Locale;
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

		if(received.equals("NEW")){
			receiveGameState();
		}

		if(received.equals("logout")){
			return true;
		}else if(btgVariables.currentState==BrickTagGame.STARTUPSTATE) {
			didSendMessage = checkStartControls(received);
		}else if(btgVariables.currentState==BrickTagGame.PLAYINGSTATE) {
			didSendMessage = checkPlayingControls(received);
//			setPlayerPosition();
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
//				e.printStackTrace();
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

		this.movePlayer(input);

		sendVariablesToClient();
		return true;
	}

	private void movePlayer(String input){
		// "infinite" value
		int xMax = 99;
		int yMax = 99;
		int xMin = -99;
		int yMin = -99; //used for checking "head bonks" on block above. not implemented yet

		//get player position in tile grid
		int playerX = (int)Math.floor(this.btgVariables.playerVariables.getX() / 64);
		int playerY = (int)Math.floor(this.btgVariables.playerVariables.getY() / 64);

//		int playerX = this.btgVariables.playerVariables.getPlayerX();
//		int playerY = this.btgVariables.playerVariables.getPlayerY();

		//East
		if( this.btgVariables.tileGrid[playerX + 1][playerY].designation != 0){ xMax = playerX + 1; }

		//West
		if( this.btgVariables.tileGrid[playerX - 1][playerY].designation != 0){ xMin = playerX - 1; }

		//South
		if( this.btgVariables.tileGrid[playerX][playerY + 1].designation != 0){
			yMax = playerY + 1;
		}else{
			this.btgVariables.playerVariables.setAirborne(true);
		}

//		Ground Check
		if(this.btgVariables.playerVariables.isAirborne()) {
			if (this.btgVariables.playerVariables.getY() > ((yMax) * 64) - 32) {

				System.out.println("Landed!");
//				btg.player.translate(5, 0);
				this.btgVariables.playerVariables.setVariableY(((yMax) * 64) - 32);
				this.btgVariables.playerVariables.setVelocity(0,0);
				this.btgVariables.playerVariables.setAirborne(false);
			}
		}

		if(this.btgVariables.playerVariables.isAirborne()){
			this.btgVariables.playerVariables.setVelocity(this.btgVariables.playerVariables.getVelocity().add(this.btgVariables.gravity));
		}else{
			if(input.equals("SPACE")){
				this.btgVariables.playerVariables.setVelocity(this.btgVariables.playerVariables.getVelocity().add(this.btgVariables.jump));
				this.btgVariables.playerVariables.setAirborne(true);
			}
		}

		if(input.equals("A")){
			jig.Vector v = this.btgVariables.playerVariables.getVelocity();
			if(this.btgVariables.playerVariables.getVelocity().getX()>0) {
				v = this.btgVariables.playerVariables.getVelocity().add(new jig.Vector(-.1f, 0));
			}
			this.btgVariables.playerVariables.setVelocity(v);
			if(this.btgVariables.playerVariables.getX() < ((xMin)* 64) + 96){
				this.btgVariables.playerVariables.setVariableX((xMin + 1)* 64 + 32);
			}
		}else if(input.equals("D")){
			jig.Vector v = this.btgVariables.playerVariables.getVelocity();
			if(this.btgVariables.playerVariables.getVelocity().getX()<0) {
				v = this.btgVariables.playerVariables.getVelocity().add(new jig.Vector(.1f, 0));
			}
			this.btgVariables.playerVariables.setVelocity(v);
			if(this.btgVariables.playerVariables.getX() > ((xMax)* 64) - 32){
				this.btgVariables.playerVariables.setVariableX((xMax)* 64 - 32);
			}
		}

		if(input.equals("") && this.btgVariables.playerVariables.getVelocity().getY()==0){
			this.btgVariables.playerVariables.setVelocity(0,0);
		}
	}

	private void setPlayerPosition(){
		float x = 0;
		float y = 0;
		try {
			x = inputStream.readFloat();
			y = inputStream.readFloat();
			this.btgVariables.playerVariables.setVariableX(x);
			this.btgVariables.playerVariables.setVariableY(y);
		} catch (IOException e) {
//			e.printStackTrace();
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
