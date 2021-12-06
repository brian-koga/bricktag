import org.lwjgl.Sys;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


@SuppressWarnings("InfiniteLoopStatement")
public class Server {
	static Vector<ClientHandler> playerList = new Vector<>();
	static int numberOfActivePlayers = 0;
	static BrickTagGameVariables BTGV = new BrickTagGameVariables(720,1280);

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
				ClientHandler clientHandler = new ClientHandler(socket,input,output,objectOutputStream,objectInputStream,new BrickTagGameVariables(720,1280),numberOfActivePlayers);
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
	PlayerVariables PV;
	final int playerIndex;

	ClientHandler(Socket socket, DataInputStream inputStream, DataOutputStream outputStream,ObjectOutputStream objectOutputStream,ObjectInputStream objectInputStream,BrickTagGameVariables btg, int i) throws IOException {
		this.socket = socket;
		this.inputStream = inputStream;
		this.outputStream = outputStream;
		this.objectOutputStream = objectOutputStream;
		this.objectInputStream = objectInputStream;
		this.playerIndex = i;
	}

	@Override
	public void run(){
		//System.out.println("run");

		this.writeIndex(this.playerIndex);
		Server.BTGV.playerList.add(new PlayerVariables(240, 352, 0, 0));
		while (true){
			if (clientHandlerLoop()){
				break;
			}
		}
		logoutClient();
	}

	private boolean clientHandlerLoop() {
		//System.out.println("clientHandlerLoop");

		//System.out.println("START");
		KeyboardCommand kc = receiveKeyboardCommand();
		//System.out.println("FINISH -----------");

		if(kc==null){
			//Acts as a continue
			return false;
		}
		String received = kc.command;
		//System.out.println("String Recieved: " + received);


		boolean didSendMessage = false;

		if(received.equals("NEW")){
			receiveGameState();
			System.out.println("NEW GAME STATE");
			return false;
		}else if(received.equals("PV")){
			System.out.println("SET PV");
			PlayerVariables newPV = receivePlayerVariables();
			Server.BTGV.playerList.set(this.playerIndex,newPV);
		}else if(received.equals("logout")){
			Server.BTGV.playerList.remove(this.playerIndex);
			return true;
		}else if(Server.BTGV.currentState==BrickTagGame.PLAYINGSTATE) {
			this.PV = Server.BTGV.playerList.get(playerIndex);
			didSendMessage = checkPlayingControls(received);
			Server.BTGV.playerList.set(this.playerIndex, this.PV);
		}else if(Server.BTGV.currentState==BrickTagGame.STARTUPSTATE) {
			didSendMessage = checkStartControls(received);
		}

		if(!didSendMessage) {
			writeToClient("", this.outputStream);
		}
		return false;
	}

	private KeyboardCommand receiveKeyboardCommand(){
		//System.out.println("recieveKeyboardCommand");

		try {
			return (KeyboardCommand) this.objectInputStream.readObject();
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

	private PlayerVariables receivePlayerVariables(){
		//System.out.println("recievePlayerVariables");

		try {
			return (PlayerVariables) this.objectInputStream.readObject();

		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

	private void logoutClient(){
		//System.out.println("logoutClient");

		try {
			Server.numberOfActivePlayers--;
			Server.playerList.remove(this);
			this.socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendVariablesToClient(){
		//System.out.println("sendVariablesToClient");

		try {
			writeToClient("CHANGE", this.outputStream);
			this.objectOutputStream.reset();
			this.objectOutputStream.writeObject(Server.BTGV);
			this.objectOutputStream.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void writeToClient(String s,DataOutputStream localOutputStream){
		//System.out.println("writeToClient");

		try {
			localOutputStream.writeUTF(s);
			localOutputStream.flush();
		} catch (IOException ignored) {}
	}

	private void writeIndex(int playerIndex){
		//System.out.println("writeIndex");

		try {
			outputStream.writeInt(playerIndex);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private boolean checkStartControls(String input){
		//System.out.println("checkStartControls");

		if(input.equals("SPACE")){
			Server.BTGV.currentState = BrickTagGame.PLAYINGSTATE;
			sendVariablesToClient();
			return true;
		}
		return false;
	}

	private boolean checkPlayingControls(String input){
		//System.out.println("checkPlayingControls");

		if(input.equals("DEBUG")){
			Server.BTGV.toggleShowGrid();
		}

		this.movePlayer(input);
		sendVariablesToClient();

		return true;
	}

	private void movePlayer(String input){
		//System.out.println("movePlayer");

		PlayerVariables newLocation;

		//Leave at top
		if(this.PV == null){
			System.out.println("position currently null - this is expected - setting to default");
			newLocation = new PlayerVariables(280, 352, 0, 0);
			Server.BTGV.setPv(newLocation,this.playerIndex);
			this.PV = newLocation;
			this.PV.setAirborne(true);
		}

		update();

		// "infinite" value
		int xMax = 99;
		int yMax = 99;
		int xMin = -99;
		int yMin = -99;

		//get player position in tile grid
		int playerX = (int)Math.floor(this.PV.getX() / 64);
		int playerY = (int)Math.floor(this.PV.getY() / 64);

		//East
		if(playerX == (Server.BTGV.WorldTileWidth - 1)){
			xMax = (Server.BTGV.WorldTileWidth);
		}else if (Server.BTGV.tileGrid[playerX + 1][playerY].designation != 0) {
			xMax = playerX + 1;
		}else{}

		//West
		if(playerX == 0){ xMin = -1; }
		else if (Server.BTGV.tileGrid[playerX - 1][playerY].designation != 0) {
			xMin = playerX - 1;
		}else{}

		//South
		if( Server.BTGV.tileGrid[playerX][playerY + 1].designation != 0){
			yMax = playerY + 1;
		}else{
			this.PV.setAirborne(true);
		}

		//North
		if(playerY == 0){ yMin = -1; }
		else if (Server.BTGV.tileGrid[playerX][playerY - 1].designation != 0) {
			yMin = playerY - 1;
		}else{}

		//Roof Check
		if(this.PV.isAirborne()) {
			if (this.PV.getY() < ((yMin) * 64) + 96) {
//				System.out.println("Bonk!");
				this.PV.setVariableY(((yMin + 1) * 64) + 32);
				this.PV.resetVelocity();
			}
		}

		//Ground Check
		if(this.PV.isAirborne()) {
			if (this.PV.getY() > ((yMax) * 64) - 32) {
//				System.out.println("Landed!");
				this.PV.setVariableY(((yMax) * 64) - 32);
				this.PV.resetVelocity();
				this.PV.setAirborne(false);
			}
		}

		//Gravity
		if(this.PV.isAirborne()){
//			System.out.println("add gravity!");
			translateMoveHelper(0f,0f,0f,Server.BTGV.gravityValue);
		}else{
			if(input.equals("SPACE")){
				translateMoveHelper(0f,0f,0f,Server.BTGV.jumpValue);
				this.PV.setAirborne(true);
			}
		}

		//Move West / East
		if(input.equals("A")){ moveWest(xMin); }
		if(input.equals("D")){ moveEast(xMax); }

		//Place West / East
		if(input.equals("Q")){ placeWest(xMin, playerX, playerY); }
		if(input.equals("E")){ placeEast(xMax, playerX, playerY); }

		//Combination of movement & placement
		if(input.equals("AE")){ moveWest(xMin); placeEast(xMax, playerX, playerY); }
		if(input.equals("AQ")){ moveWest(xMin); placeWest(xMin, playerX, playerY); }
		if(input.equals("DE")){ moveEast(xMax); placeEast(xMax, playerX, playerY); }
		if(input.equals("DQ")){ moveEast(xMax); placeWest(xMin, playerX, playerY); }

		if(input.equals("") && this.PV.getVelocity().getY()==0){
			this.PV.setVelocity(0,0);
		}
	}


	//Movement & Placement
	private void moveEast(int xMax){
		translateMoveHelper(5f,0f,0f,0f);
		if(this.PV.getX() > ((xMax)* 64) - 32){
			this.PV.setVariableX((xMax)* 64 - 32);
		}
	}

	private void moveWest(int xMin){
		translateMoveHelper(-5f,0f,0f,0f);
		if(this.PV.getX() < ((xMin)* 64) + 96 || (this.PV.getX() < 32)){
			this.PV.setVariableX((xMin + 1)* 64 + 32);
		}
	}

	private void placeEast(int xMax, int playerX, int playerY){
		if (xMax != (Server.BTGV.WorldTileWidth)) {
			if (Server.BTGV.tileGrid[playerX + 1][playerY].designation == 0) {
				Server.BTGV.tileGrid[playerX + 1][playerY].designation = 1;
			}
		}
	}

	private void placeWest(int xMin, int playerX, int playerY){
		if (xMin != -1) {
			if (Server.BTGV.tileGrid[playerX - 1][playerY].designation == 0) {
				Server.BTGV.tileGrid[playerX - 1][playerY].designation = 1;
			}
		}
	}

	//Adds velocity to position
	private void update(){
		//System.out.println("update");
		float tempVX = this.PV.getVX();
		float tempVY = this.PV.getVY();
		translateMoveHelper(tempVX, tempVY, 0f, 0f);
	}

	//Adds given value to pre-existing value - pretty much translate();
	private void translateMoveHelper(float x, float y, float vx, float vy){
		//System.out.println("translateMoveHelper");

		PlayerVariables newLocation;

		float tempX = this.PV.getX();
		float tempY = this.PV.getY();
		float tempVX = this.PV.getVX();
		float tempVY = this.PV.getVY();
		boolean airborne = this.PV.isAirborne();
		this.PV = null;

		newLocation = new PlayerVariables(tempX + (x), tempY + (y),tempVX + (vx), tempVY  + (vy));
		Server.BTGV.setPv(newLocation,this.playerIndex);
		this.PV = newLocation;
		this.PV.setAirborne(airborne);
	}

	public void receiveGameState(){
		//System.out.println("receiveGameState");

		try {
			Server.BTGV = (BrickTagGameVariables) this.objectInputStream.readObject();
			sendVariablesToClient();
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
}
