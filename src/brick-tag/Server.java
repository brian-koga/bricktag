import org.lwjgl.Sys;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;


@SuppressWarnings("InfiniteLoopStatement")
public class Server {
	static Vector<ClientHandler> playerList = new Vector<>();
	static int numberOfActivePlayers = 0;
	static BrickTagGameVariables BTGV = new BrickTagGameVariables(720,1280);
	static Tile[][] tileGrid;
	static int flagCountdown=0;

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
		if(Server.tileGrid == null) { setTileGrid(); } // prevent new clients from resetting map
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
			setPlayerFlag();
			setScore();
			Server.BTGV.playerList.set(this.playerIndex, this.PV);
			int whoWon = checkScores();
			if(whoWon>=0){
				System.out.println("GAME OVER!!!! PLAYER "+whoWon+" WINS!!!!!!");
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

	public void sendVariablesToClient(){
		//System.out.println(playerIndex + "SVTC: " + Server.BTGV.tileGrid);
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
			sendVariablesToClient();
			return true;
		}
		return false;
	}

	private boolean checkPlayingControls(String input){
		if(input.equals("DEBUG")){
			Server.BTGV.toggleShowGrid();
			this.PV.toggleFlag();
		}

		this.movePlayer(input);
		sendVariablesToClient();

		return true;
	}

	private void movePlayer(String input){
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

		int playerNorth = (int)Math.floor((this.PV.getY() - 32) / 64);
		int playerEast = (int)Math.floor((this.PV.getX() + 31) / 64);
		int playerSouth = (int)Math.floor((this.PV.getY() + 0) / 64);
		int playerWest = (int)Math.floor((this.PV.getX() - 31) / 64);
		if(playerY<0){ playerY=0; }
		if(playerNorth<0){ playerNorth = 0;}
		if(playerSouth<0){ playerSouth = 0;}

		Tile[][] tempMap = Server.tileGrid;

		//East
		if(playerX == (Server.BTGV.WorldTileWidth - 1)){
			xMax = (Server.BTGV.WorldTileWidth);
		}else if ((tempMap[playerX + 1][playerNorth].designation != 0) || (tempMap[playerX + 1][playerSouth].designation != 0)){
			xMax = playerX + 1;
		}

		//West
		if(playerX == 0){ xMin = -1; }
		else if ((tempMap[playerX - 1][playerNorth].designation != 0) || (tempMap[playerX - 1][playerSouth].designation != 0)) {
			xMin = playerX - 1;
		}

		//South
		if( (tempMap[playerEast][playerY + 1].designation != 0) || (tempMap[playerWest][playerY + 1].designation != 0)){
			yMax = playerY + 1;
		}else{
			this.PV.setAirborne(true);
		}

		//North
		if(	this.PV.getVY() < 0){
			if (playerY == 0) {
				yMin = -1;
			} else if ((tempMap[playerEast][playerY - 1].designation != 0) || (tempMap[playerWest][playerY - 1].designation != 0)) {
				yMin = playerY - 1;
			}
		}else {
			if (playerY == 0) {
				yMin = -1;
			} else if ((tempMap[playerX][playerY - 1].designation != 0)) {
				yMin = playerY - 1;
			}
		}

		//Roof Check
		if(this.PV.isAirborne()) {
			if (this.PV.getY() < ((yMin) * 64) + 96 && playerY>0) {
				if((tempMap[playerX][playerY - 1].designation > 1) &&
					(tempMap[playerX][playerY - 1].designation < 6)){

					tempMap[playerX][playerY - 1].designation = 0;

					int finalPlayerY = playerY-1;
					Predicate<Tile> condition = tile -> tile.getX() == playerX && tile.getY() == finalPlayerY;

					Server.BTGV.placedTiles.removeIf(condition);
					this.PV.addBrick();
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
			placeWest(xMin, playerX, playerY, tempMap);
		}
		if(input.equals("E")){
			placeEast(xMax, playerX, playerY, tempMap);
		}
		if(input.equals("W")){
			placeAbove(yMin,playerX,playerY,tempMap);
		}
		if(input.equals("S")){
			placeBelow(yMax,playerX,playerY,tempMap);
		}

		//Combination of movement & placement
		if(input.equals("AE")){
			moveWest(xMin);
			placeEast(xMax, playerX, playerY, tempMap);
		}
		if(input.equals("AQ")){
			moveWest(xMin);
			placeWest(xMin, playerX, playerY, tempMap);
		}
		if(input.equals("DE")){
			moveEast(xMax);
			placeEast(xMax, playerX, playerY, tempMap);
		}
		if(input.equals("DQ")){
			moveEast(xMax);
			placeWest(xMin, playerX, playerY, tempMap);
		}

		//Above and Below Combos
		if(input.equals("AW")){
			moveWest(xMin);
			placeAbove(yMin,playerX,playerY,tempMap);
		}
		if(input.equals("AS")){
			moveWest(xMin);
			placeBelow(yMax,playerX,playerY,tempMap);
		}
		if(input.equals("DW")){
			moveEast(xMax);
			placeAbove(yMin,playerX,playerY,tempMap);
		}
		if(input.equals("DS")){
			moveEast(xMax);
			placeBelow(yMax,playerX,playerY,tempMap);
		}


		if(input.equals("") && this.PV.getVelocity().getY()==0){
			this.PV.setVelocity(0,0);
		}

		Server.tileGrid = tempMap;
	}

	//Movement & Placement
	private void moveEast(int xMax){
		Server.BTGV.setOrientation(this.playerIndex, "RR");

		translateMoveHelper(5f,0f,0f,0f);
		if(this.PV.getX() > ((xMax)* 64) - 32){
			this.PV.setVariableX((xMax)* 64 - 32);
		}
	}

	private void moveWest(int xMin){
		Server.BTGV.setOrientation(this.playerIndex, "RL");

		translateMoveHelper(-5f,0f,0f,0f);
		if(this.PV.getX() < ((xMin)* 64) + 96 || (this.PV.getX() < 32)){
			this.PV.setVariableX((xMin + 1)* 64 + 32);
		}
	}

	private void placeEast(int xMax, int playerX, int playerY, Tile[][] tempMap){
		if(this.PV.getNumberOfBricks()>0) {
			if (xMax != (Server.BTGV.WorldTileWidth)) {
				placeBlock(playerX +1 ,playerY,tempMap);
					/*if (this.playerIndex == 0) {
						tempMap[playerX + 1][playerY].designation = 2;
						Server.BTGV.placedTiles.add(new Tile(playerX + 1, playerY, 2, true));
					}else if (this.playerIndex == 1) {
						tempMap[playerX + 1][playerY].designation = 3;
						Server.BTGV.placedTiles.add(new Tile(playerX + 1, playerY, 3, true));
					}
					this.PV.useBrick();*/
			}
		}
	}

	private void placeWest(int xMin, int playerX, int playerY, Tile[][] tempMap){
		if(this.PV.getNumberOfBricks()>0) {
			if (xMin != -1) {
/*					if (this.playerIndex == 0) {
						tempMap[playerX - 1][playerY].designation = 2;
						Server.BTGV.placedTiles.add(new Tile(playerX - 1, playerY, 2, true));
					}else if (this.playerIndex == 1) {
						tempMap[playerX - 1][playerY].designation = 3;
						Server.BTGV.placedTiles.add(new Tile(playerX - 1, playerY, 3, true));
					}*/
				placeBlock(playerX - 1, playerY, tempMap);
			}
		}
	}

	private void placeAbove(int yMin, int playerX, int playerY, Tile[][] tempMap){
		if(this.PV.getNumberOfBricks()>0) {
			if (yMin != -1) {
				placeBlock(playerX, playerY-1, tempMap);
			}
		}
	}

	private void placeBelow(int yMax, int playerX, int playerY, Tile[][] tempMap){
		if(this.PV.getNumberOfBricks()>0) {
			if (yMax != (Server.BTGV.WorldTileHeight)) {
				placeBlock(playerX, playerY+1, tempMap);
			}
		}
	}

	private void placeBlock(int playerX, int playerY, Tile[][] tempMap) {
		if (tempMap[playerX][playerY].designation == 0) {
			int des = this.playerIndex + 2;
			tempMap[playerX][playerY].designation = des;
			Server.BTGV.placedTiles.add(new Tile(playerX, playerY, des, true));
			this.PV.useBrick();
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
		this.PV.translateHelper(x,y,vx,vy);
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

	public void setTileGrid() {
		String path = getMapFile(Server.BTGV.level);
		Server.tileGrid = PlayingState.setupLevel(Server.BTGV,path);
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
			if(Server.BTGV.scoreList.get(i)>=25){
				return i;
			}
		}
		return -1;
	}

	private void setPlayerFlag(){
		if(this.PV.hasFlag()) {
			if (Server.flagCountdown <= 0) {
				PlayerVariables currPlayer = this.PV;
				for (int i = 0; i < Server.BTGV.playerList.size(); i++) {
					if (i != this.playerIndex) {
						PlayerVariables checkPlayer = Server.BTGV.playerList.get(i);
						if (checkPlayer.getX() == currPlayer.getX() && checkPlayer.getY() == currPlayer.getY()) {
							checkPlayer.toggleFlag();
							currPlayer.toggleFlag();
							Server.flagCountdown = 50;
//							System.out.println("Player " + this.playerIndex + " lost the flag to Player " + i);
							break;
						}
					}
				}
			}else{
				Server.flagCountdown-=1;
//				System.out.println(Server.flagCountdown);
			}
		}
	}
}
