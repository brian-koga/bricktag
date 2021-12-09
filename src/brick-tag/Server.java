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
	Tile[][] tileMap;
	int thisThreadUpdateCount = 0;

	ClientHandler(Socket socket, DataInputStream inputStream, DataOutputStream outputStream,ObjectOutputStream objectOutputStream,ObjectInputStream objectInputStream,BrickTagGameVariables btg, int i) throws IOException {
		this.socket = socket;
		this.inputStream = inputStream;
		this.outputStream = outputStream;
		this.objectOutputStream = objectOutputStream;
		this.objectInputStream = objectInputStream;
		this.playerIndex = i;
		if(Server.BTGV.tileGrid == null) { setTileGrid(); } // prevent new clients from resetting map
		this.tileMap = Server.BTGV.tileGrid;
	}

	@Override
	public void run(){
		this.writeIndex(this.playerIndex);
		Server.BTGV.playerList.add(new PlayerVariables(240, 352, 0, 0));
		Server.BTGV.scoreList.add(0);
		while (true){
			if (clientHandlerLoop()){
				break;
			}
		}
		logoutClient();
	}

	private boolean clientHandlerLoop() {
		KeyboardCommand kc = receiveKeyboardCommand();
		if(kc==null){
			//Acts as a continue
			return false;
		}
		String received = kc.command;

		boolean didSendMessage = false;

		if(received.equals("NEW")){
			receiveGameState();
			System.out.println("NEW GAME STATE");
			return false;
		}else if(received.equals("PV")){
			PlayerVariables newPV = receivePlayerVariables();
			Server.BTGV.playerList.set(this.playerIndex,newPV);
		}else if(received.equals("logout")){
			Server.BTGV.playerList.remove(this.playerIndex);
			return true;
		}else if(Server.BTGV.currentState==BrickTagGame.PLAYINGSTATE) {
			this.PV = Server.BTGV.playerList.get(playerIndex);
			didSendMessage = checkPlayingControls(received);
			setScore();
			Server.BTGV.playerList.set(this.playerIndex, this.PV);
			int whoWon = checkScores();
			if(whoWon>=0){
				System.out.println("_________");
				//TODO Go to Gameover State
			}
		}else if(Server.BTGV.currentState==BrickTagGame.STARTUPSTATE) {
			didSendMessage = checkStartControls(received);
		}

		if(!didSendMessage) {
			writeToClient("", this.outputStream);
		}
		return false;
	}

	private KeyboardCommand receiveKeyboardCommand(){
		try {
			return (KeyboardCommand) this.objectInputStream.readObject();
		} catch (IOException | ClassNotFoundException e) {
//			e.printStackTrace();
			return null;
		}
	}

	private PlayerVariables receivePlayerVariables(){
		try {
			return (PlayerVariables) this.objectInputStream.readObject();

		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
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

	public void sendVariablesToClient(String message){
		//System.out.println(playerIndex + "SVTC: " + Server.BTGV.tileGrid);
		try {
			if(message.equals("CHANGE")){
				this.tileMap = Server.BTGV.tileGrid;
				Server.BTGV.tileGrid = null;
			}
			writeToClient(message, this.outputStream);
			this.objectOutputStream.reset();
			this.objectOutputStream.writeObject(Server.BTGV);
			this.objectOutputStream.flush();
			Server.BTGV.tileGrid = this.tileMap;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void writeToClient(String s,DataOutputStream localOutputStream){
		try {
			localOutputStream.writeUTF(s);
			localOutputStream.flush();
		} catch (IOException ignored) {}
	}

	private void writeIndex(int playerIndex){
		try {
			outputStream.writeInt(playerIndex);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private boolean checkStartControls(String input){
		if(input.equals("SPACE")){
			Server.BTGV.currentState = BrickTagGame.PLAYINGSTATE;
			sendVariablesToClient("CHANGE");
			return true;
		}
		return false;
	}

	private boolean checkPlayingControls(String input){
		if(input.equals("DEBUG")){
			Server.BTGV.toggleShowGrid();
		}

		String message = this.movePlayer(input);
		sendVariablesToClient(message);

		return true;
	}

	private String movePlayer(String input){
		PlayerVariables newLocation;
		String message = "CHANGE";
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

		System.out.println(this.playerIndex + "MOVE: " + Server.BTGV.tileGrid);

		Tile[][] tempMap;
		//MAKE SURE that the tilegrid isn't null from another thread at this moment
		while(true){
			if(Server.BTGV.tileGrid != null){
				tempMap = Server.BTGV.tileGrid;
				break;
			}
		}

		//East
		if(playerX == (Server.BTGV.WorldTileWidth - 1)){
			xMax = (Server.BTGV.WorldTileWidth);
		}else if (tempMap[playerX + 1][playerY].designation != 0) {
			xMax = playerX + 1;
		}else{}

		//West
		if(playerX == 0){ xMin = -1; }
		else if (tempMap[playerX - 1][playerY].designation != 0) {
			xMin = playerX - 1;
		}else{}

		//South
		if( tempMap[playerX][playerY + 1].designation != 0){
			yMax = playerY + 1;
		}else{
			this.PV.setAirborne(true);
		}

		//North
		if(playerY == 0){ yMin = -1; }
		else if (tempMap[playerX][playerY - 1].designation != 0) {
			yMin = playerY - 1;
		}else{}

		//Roof Check
		if(this.PV.isAirborne()) {
			if (this.PV.getY() < ((yMin) * 64) + 96) {
				if((tempMap[playerX][playerY - 1].designation == 2) ||
				   (tempMap[playerX][playerY - 1].designation == 3)){

					tempMap[playerX][playerY - 1].designation = 0;
					//TODO INCREMENT PLAYER BLOCK COUNT BY 1

					message = "NEW_MAP";
				}
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
		if(input.equals("Q")){
			message = placeWest(xMin, playerX, playerY, tempMap);
		}
		if(input.equals("E")){
			message = placeEast(xMax, playerX, playerY, tempMap);
		}

		//Combination of movement & placement
		if(input.equals("AE")){
			moveWest(xMin);
			message = placeEast(xMax, playerX, playerY, tempMap);
		}
		if(input.equals("AQ")){
			moveWest(xMin);
			message = placeWest(xMin, playerX, playerY, tempMap);
		}
		if(input.equals("DE")){
			moveEast(xMax);
			message = placeEast(xMax, playerX, playerY, tempMap);
		}
		if(input.equals("DQ")){
			moveEast(xMax);
			message = placeWest(xMin, playerX, playerY, tempMap);
		}

		if(input.equals("") && this.PV.getVelocity().getY()==0){
			this.PV.setVelocity(0,0);
		}

		Server.BTGV.tileGrid = tempMap;

		//check if an update is made by another client
		if(Server.BTGV.getUpdateCount() > thisThreadUpdateCount){
			message = "NEW_MAP";
			thisThreadUpdateCount = Server.BTGV.getUpdateCount();
		}else if(message.equals("NEW_MAP")){
			Server.BTGV.incrementUpdateCount();
		}
		return message;
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

	private String placeEast(int xMax, int playerX, int playerY, Tile[][] tempMap){
		if (xMax != (Server.BTGV.WorldTileWidth)) {
			if (tempMap[playerX + 1][playerY].designation == 0) {
				if(this.playerIndex == 0) { tempMap[playerX + 1][playerY].designation = 2; }
				if(this.playerIndex == 1) { tempMap[playerX + 1][playerY].designation = 3; }
				return "NEW_MAP";
			}
		}
		return "CHANGE";
	}

	private String placeWest(int xMin, int playerX, int playerY, Tile[][] tempMap){
		if (xMin != -1) {
			if (tempMap[playerX - 1][playerY].designation == 0) {
				if(this.playerIndex == 0) { tempMap[playerX - 1][playerY].designation = 2; }
				if(this.playerIndex == 1) { tempMap[playerX - 1][playerY].designation = 3; }
				return "NEW_MAP";
			}
		}
		return "CHANGE";
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
		boolean flag = this.PV.hasFlag();
		int score = this.PV.getScore();
		int tempScore = this.PV.tempScore;
		this.PV = null;

		newLocation = new PlayerVariables(tempX + (x), tempY + (y),tempVX + (vx), tempVY  + (vy));
		this.PV = newLocation;
		this.PV.setAirborne(airborne);
		if(flag){
			this.PV.toggleFlag();
		}
		this.PV.tempScore = tempScore;
		this.PV.score = score;
		Server.BTGV.setPv(this.PV,this.playerIndex);
	}

	public void receiveGameState(){
		//System.out.println("receiveGameState");

		try {
			Server.BTGV = (BrickTagGameVariables) this.objectInputStream.readObject();
			sendVariablesToClient("NEW_MAP");
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void setTileGrid() {
		String path = getMapFile(Server.BTGV.level);
		PlayingState.setupLevel(Server.BTGV,path);
	}

	private String getMapFile(int levelNumber){
		if(levelNumber == 1) {
			return "Brick-Tag/src/brick-tag/resource/Level1.txt";
		} else if(levelNumber == 2) {
			return  "Brick-Tag/src/brick-tag/resource/Level2.txt";
		}
		else{
			return null;
		}
	}

	private void setScore(){
		if(this.PV.hasFlag()){
			this.PV.addScore(1);
			Server.BTGV.scoreList.set(this.playerIndex,this.PV.getScore());
		}
	}

	private int checkScores(){
		for(int i = 0; i < Server.BTGV.scoreList.size();i++){
			//Currently, the score is set at 25 but that can be played with
			if(Server.BTGV.scoreList.get(i)>25){
				System.out.println("GAME OVER!!!! PLAYER "+i+" WINS!!!!!!");
				return i;
			}
		}
		return -1;
	}
}
