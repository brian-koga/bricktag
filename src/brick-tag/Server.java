import org.lwjgl.Sys;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Vector;
import java.util.function.Predicate;
import java.math.*;


@SuppressWarnings("InfiniteLoopStatement")
public class Server {
	static int numberOfActivePlayers = 0;
	static Vector<Integer> removedPlayers;
	static BrickTagGameVariables BTGV = new BrickTagGameVariables(720,1280);
	static Tile[][] tileGrid;
	static int flagCountdown=0;

	public static void main(String[] args) throws IOException {
		//Start a new server listening on port 5000
		ServerSocket server = new ServerSocket(5000);
		Socket socket;

		removedPlayers = new Vector<>();


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
				ClientHandler clientHandler = new ClientHandler(socket,input,output,objectOutputStream,objectInputStream,new BrickTagGameVariables(720,1280),Server.BTGV.playerList.size());
				Thread thread = new Thread(clientHandler);
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
	int playerIndex;

	ClientHandler(Socket socket, DataInputStream inputStream, DataOutputStream outputStream,ObjectOutputStream objectOutputStream,ObjectInputStream objectInputStream,BrickTagGameVariables btg, int i) throws IOException {
		this.socket = socket;
		this.inputStream = inputStream;
		this.outputStream = outputStream;
		this.objectOutputStream = objectOutputStream;
		this.objectInputStream = objectInputStream;
		this.playerIndex = i;
		if(Server.tileGrid == null) { setTileGrid(); } // prevent new clients from resetting map
		// add the power ups to the tileGrid
		for (Tile temp : Server.BTGV.powerUpTiles) {
			Server.tileGrid[temp.x][temp.y] = temp;
		}
	}

	@Override
	public void run(){
		if(Server.removedPlayers.size()>0){
			this.playerIndex = Server.removedPlayers.remove(0);
			System.out.println(this.playerIndex);
			Server.BTGV.playerList.set(this.playerIndex,new PlayerVariables(240, 352, 0, 0));
			Server.BTGV.scoreList.set(this.playerIndex,0);
		}else if(this.playerIndex>3) {
			this.writeIndex(this.playerIndex);
			logoutClient();
			return;
		}
		else {
			Server.BTGV.playerList.add(new PlayerVariables(240, 352, 0, 0));
			Server.BTGV.scoreList.add(0);
		}
		this.writeIndex(this.playerIndex);
		sendVariablesToClient();
		while (true) {
			if (clientHandlerLoop()) {
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
			writeToClient("logout",this.outputStream);
			Server.BTGV.playerList.get(this.playerIndex).isLoggedIn=false;
			if(Server.BTGV.playerList.get(this.playerIndex).hasFlag()) {
				setFlagPosition();
			}
			return true;
		}else if(Server.BTGV.currentState==BrickTagGame.PLAYINGSTATE) {
			this.PV = Server.BTGV.playerList.get(this.playerIndex);
			didSendMessage = checkPlayingControls(received);
			setPlayerFlag();
			setScore();
			checkPowerUp();
			Server.BTGV.playerList.set(this.playerIndex, this.PV);
			int whoWon = checkScores();
			if(whoWon>=0){
				Server.BTGV.currentState = BrickTagGame.GAMEOVERSTATE;
				sendVariablesToClient();
				didSendMessage=true;
			}
		}else if(Server.BTGV.currentState==BrickTagGame.STARTUPSTATE || Server.BTGV.currentState==BrickTagGame.GAMEOVERSTATE) {
			didSendMessage = checkStartEndControls(received);
		}

		if(!didSendMessage) {
			writeToClient("", this.outputStream);
		}
		return false;
	}

	private KeyboardCommand receiveKeyboardCommand(){
		try {
			return (KeyboardCommand) this.objectInputStream.readObject();
		}catch(SocketException s){
			KeyboardCommand kc = new KeyboardCommand();
			kc.command = "logout";
			return kc;
		}catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
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
			System.out.println("LOGGING OUT: "+this.playerIndex);
			Server.removedPlayers.add(this.playerIndex);
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
		} catch (SocketException ignored){
		}catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void writeIndex(int playerIndex){
		try {
			outputStream.writeInt(playerIndex);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private boolean checkStartEndControls(String input){
		if(this.playerIndex<Server.BTGV.playerList.size()-1){
			sendVariablesToClient();
			return true;
		}
		if(input.equals("SPACE") && Server.BTGV.currentState == BrickTagGame.STARTUPSTATE){
			Server.BTGV.currentState = BrickTagGame.PLAYINGSTATE;
			sendVariablesToClient();
			return true;
		}else if(Server.BTGV.currentState == BrickTagGame.GAMEOVERSTATE){
			if(input.equals("GO_START")) {
				resetGame();
			}
			sendVariablesToClient();
			return true;
		}else if(input.equals("END")){
			sendVariablesToClient();
			return true;
		}
		return false;
	}

	private void resetGame() {
		Server.BTGV.currentState = BrickTagGame.STARTUPSTATE;
		Server.BTGV.placedTiles = new Vector<>();
		for(int i = 0; i<Server.BTGV.playerList.size();i++){
			Server.BTGV.playerList.set(i,Server.BTGV.playerList.set(this.playerIndex,new PlayerVariables(240, 352, 0, 0)));
			Server.BTGV.scoreList.set(i,0);
			Server.BTGV.playerList.get(i).score=0;
			Server.BTGV.playerList.get(i).tempScore=0;
			if(Server.BTGV.playerList.get(i).hasFlag()){
				Server.BTGV.playerList.get(i).toggleFlag();
			}
			Server.BTGV.flagHolder = -1;
		}
		for(int i : Server.removedPlayers){
			Server.BTGV.playerList.get(i).isLoggedIn = false;
		}
		setTileGrid();

		// add the power ups to the tileGrid
		for (Tile temp : Server.BTGV.powerUpTiles) {
			Server.tileGrid[temp.x][temp.y] = temp;
		}
	}

	private boolean checkPlayingControls(String input){
		if(input.equals("DEBUG")){
			Server.BTGV.toggleShowGrid();
			//this.PV.toggleFlag();
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

		//send current flag bearer to BTGV as an int for rendering the mini flag next to mini heads
		if(this.PV.hasFlag()){ Server.BTGV.setFlagHolder(this.playerIndex); }

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
			// can be not 0 if its greater than 20
		}else if ((tempMap[playerX + 1][playerNorth].designation != 0 && tempMap[playerX + 1][playerNorth].designation <= 20)
			|| (tempMap[playerX + 1][playerSouth].designation != 0 && tempMap[playerX + 1][playerNorth].designation <= 20)){
			xMax = playerX + 1;
		}

		//West
		if(playerX == 0){ xMin = -1; }
		else if ((tempMap[playerX - 1][playerNorth].designation != 0 && tempMap[playerX - 1][playerNorth].designation <= 20)
			|| (tempMap[playerX - 1][playerSouth].designation != 0 && tempMap[playerX - 1][playerSouth].designation <= 20)) {
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
			} else if ((tempMap[playerEast][playerY - 1].designation != 0 && tempMap[playerEast][playerY - 1].designation <= 20)
				|| (tempMap[playerWest][playerY - 1].designation != 0 && tempMap[playerWest][playerY - 1].designation <= 0)) {
				yMin = playerY - 1;
			}
		}else {
			if (playerY == 0) {
				yMin = -1;
			} else if ((tempMap[playerX][playerY - 1].designation != 0)) {
				yMin = playerY - 1;
			}
		}

		// check current tile
		if(tempMap[playerX][playerY].designation > 20) {
			checkIfGotPowerUp(playerX, playerY, tempMap[playerX][playerY],tempMap);
		}

		//Roof Check
		if(this.PV.isAirborne()) {
			if (this.PV.getY() < ((yMin) * 64) + 96 && playerY>0) {
				// checks if this is a player placed block
				breakBlock(playerY-1,tempMap,playerX);
				// checks if this is a power up block
				if((tempMap[playerX][playerY - 1].designation > 20)){
					checkIfGotPowerUp(playerX, playerY - 1, tempMap[playerX][playerY - 1],tempMap);
				}
//				System.out.println("Bonk!");
				this.PV.setVariableY(((yMin + 1) * 64) + 32);
				this.PV.resetVelocity();
			}
		}

		//Ground Check
		if(tempMap[playerEast][playerY + 1].designation > 20) {
			checkIfGotPowerUp(playerEast, playerY + 1, tempMap[playerEast][playerY + 1],tempMap);
		} else if(tempMap[playerWest][playerY + 1].designation > 20) {
			// is a power up
			checkIfGotPowerUp(playerWest, playerY + 1, tempMap[playerWest][playerY + 1],tempMap);
			// regular ground check
		} else if(this.PV.isAirborne()) {
			if (this.PV.getY() > ((yMax) * 64) - 32) {
//				System.out.println("Landed!");
				this.PV.setVariableY(((yMax) * 64) - 32);
				this.PV.resetVelocity();
				this.PV.setAirborne(false);
			}
			// check if the tile below is a power up
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

		if(input.equals("ALEFT") || input.equals("ARIGHT")){
			moveWest(xMin);
		}
		if(input.equals("DLEFT") || input.equals("DRIGHT")){
			moveEast(xMax);
		}

		if(this.PV.powerUp==2) {
			if(input.length()>2){
				input = input.substring(1);
			}
			if (input.equals("LEFT")) {
				breakBlock(playerY, tempMap, playerX - 1);
			}
			if (input.equals("RIGHT")) {
				breakBlock(playerY, tempMap, playerX+1);
			}
		}


		if(input.equals("") && this.PV.getVelocity().getY()==0){
			String orientation = Server.BTGV.getOrientation(this.playerIndex);
			if(orientation.equals("RR")){
				Server.BTGV.setOrientation(this.playerIndex, "SR");
			}
			if(orientation.equals("RL")){
				Server.BTGV.setOrientation(this.playerIndex, "SL");
			}

			this.PV.setVelocity(0,0);
		}

		Server.tileGrid = tempMap;
	}

	private void breakBlock(int playerY, Tile[][] tempMap, int i) {
		if ((tempMap[i][playerY].designation > 1) && (tempMap[i][playerY].designation < 6)) {
			tempMap[i][playerY].designation = 0;
			Predicate<Tile> condition = tile -> tile.getX() == (i) && tile.getY() == playerY;
			Server.BTGV.placedTiles.removeIf(condition);
			this.PV.addBrick();
		}
	}

	private void checkIfGotPowerUp(int playerX, int playerY, Tile tile1,Tile[][] tempMap) {
		int powerUpType = tile1.designation;

		//If we want to remove the comments and take out this section in the if statement
//		tempMap[playerX][playerY].designation = 0;
//		Predicate<Tile> condition = tile -> tile.getX() == playerX && tile.getY() == playerY;
//		Server.BTGV.powerUpTiles.removeIf(condition);

		//99 is the flag and technically not a power up
		if(powerUpType == 99){
			tempMap[playerX][playerY].designation = 0;
			Predicate<Tile> condition = tile -> tile.getX() == playerX && tile.getY() == playerY;
			Server.BTGV.powerUpTiles.removeIf(condition);
			this.PV.toggleFlag();
		}else {
			// the designation of power up tiles start after 20, so send that minus 20 to get the
			// correct power up int for the PlayerVariables
			this.PV.givePowerUp(powerUpType - 20);
			System.out.println("Power up " + powerUpType + " given.");
		}
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
		setFlagPosition();
	}

	private String getMapFile(int levelNumber){
		if(levelNumber == 1) {
			return "Brick-Tag/src/brick-tag/resource/Level1.txt";
		} else if(levelNumber == 2) {
			return  "Brick-Tag/src/brick-tag/resource/Level2.txt";
		} else{
			return null;
		}
	}

	private void setScore(){
		if(this.PV.hasFlag()){
			this.PV.addScore(1);
			Server.BTGV.scoreList.set(this.playerIndex,this.PV.getScore());
		}
	}

	private void checkPowerUp() {
		if(this.PV.powerUp > 0) {
			this.PV.powerUpCountdown--;
		}
		if(this.PV.powerUpCountdown == 0) {
			// lose power up
			System.out.println("Power up " + this.PV.powerUp + " taken.");
			this.PV.powerUp = 0;
			this.PV.powerUpCountdown = -1;
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
						if(!checkPlayer.isLoggedIn){
							return;
						}
						if (Math.floor(checkPlayer.getX()/64) == Math.floor(currPlayer.getX()/64) && Math.floor(checkPlayer.getY()/64) == Math.floor(currPlayer.getY()/64)) {
							checkPlayer.toggleFlag();
							currPlayer.toggleFlag();
							Server.flagCountdown = 50;
//							System.out.println("Player " + this.playerIndex + " lost the flag to Player " + i);
							break;
						}
					}
				}
				System.out.println("\n");
			}else{
				Server.flagCountdown-=1;
//				System.out.println(Server.flagCountdown);
			}
		}
	}

	private void setFlagPosition(){
		Server.BTGV.powerUpTiles.add(new Tile(Server.BTGV.WorldTileWidth/2,Server.BTGV.WorldTileHeight/2,99,true));
		Server.tileGrid[Server.BTGV.WorldTileWidth/2][Server.BTGV.WorldTileHeight/2].designation = 99;
	}
}
