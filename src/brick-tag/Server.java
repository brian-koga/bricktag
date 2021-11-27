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
	BrickTagGameVariables BTGV;

	ClientHandler(Socket socket, DataInputStream inputStream, DataOutputStream outputStream,ObjectOutputStream objectOutputStream,ObjectInputStream objectInputStream,BrickTagGameVariables btg) throws IOException {
		this.socket = socket;
		this.inputStream = inputStream;
		this.outputStream = outputStream;
		this.objectOutputStream = objectOutputStream;
		this.objectInputStream = objectInputStream;
		this.BTGV = btg;
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
		}else if(BTGV.currentState==BrickTagGame.STARTUPSTATE) {
			didSendMessage = checkStartControls(received);
		}else if(BTGV.currentState==BrickTagGame.PLAYINGSTATE) {
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
				BrickTagGameVariables temp = this.BTGV;
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
			this.BTGV.currentState = BrickTagGame.PLAYINGSTATE;
			sendVariablesToClient();
			return true;
		}
		return false;
	}

	private boolean checkPlayingControls(String input){
		if(input.equals("0")){ this.BTGV.toggleShowGrid(); }

		this.movePlayer(input);
		sendVariablesToClient();

		return true;
	}

	private void movePlayer(String input){

		PlayerVariables newLocation;

		//Leave at top
		if(BTGV.PV == null){
			System.out.println("position currently null - this is expected - setting to default");
			newLocation = new PlayerVariables(280, 352, 0, 0);
			this.BTGV.setPv(newLocation);
			this.BTGV.PV.setAirborne(true);
		}

		update();

		// "infinite" value
		int xMax = 99;
		int yMax = 99;
		int xMin = -99;
		int yMin = -99;

		//get player position in tile grid
		int playerX = (int)Math.floor(this.BTGV.PV.getX() / 64);
		int playerY = (int)Math.floor(this.BTGV.PV.getY() / 64);

		//East
		if(playerX == (BTGV.WorldTileWidth - 1)){ xMax = (BTGV.WorldTileWidth); }
		else if (BTGV.tileGrid[playerX + 1][playerY].designation != 0) {
			xMax = playerX + 1;
		}else{}

		//West
		if(playerX == 0){ xMin = -1; }
		else if (BTGV.tileGrid[playerX - 1][playerY].designation != 0) {
			xMin = playerX - 1;
		}else{}

		//South
		if( this.BTGV.tileGrid[playerX][playerY + 1].designation != 0){
			yMax = playerY + 1;
		}else{
			this.BTGV.PV.setAirborne(true);
		}

		//North
		if(playerY == 0){ yMin = -1; }
		else if (BTGV.tileGrid[playerX][playerY - 1].designation != 0) {
			yMin = playerY - 1;
		}else{}




		//Roof Check
		if(this.BTGV.PV.isAirborne()) {
			if (this.BTGV.PV.getY() < ((yMin) * 64) + 96) {
				System.out.println("Bonk!");
				this.BTGV.PV.setVariableY(((yMin + 1) * 64) + 32);
				this.BTGV.PV.resetVelocity();
			}
		}

		//Ground Check
		if(this.BTGV.PV.isAirborne()) {
			if (this.BTGV.PV.getY() > ((yMax) * 64) - 32) {
				System.out.println("Landed!");
				this.BTGV.PV.setVariableY(((yMax) * 64) - 32);
				this.BTGV.PV.resetVelocity();
				this.BTGV.PV.setAirborne(false);
			}
		}

		//Gravity
		if(this.BTGV.PV.isAirborne()){
			System.out.println("add gravity!");
			translateMoveHelper(0f,0f,0f,this.BTGV.gravityValue);
		}else{
			if(input.equals("SPACE")){
				translateMoveHelper(0f,0f,0f,this.BTGV.jumpValue);
				this.BTGV.PV.setAirborne(true);
			}
		}

		//MOVE WEST
		if(input.equals("A")){
			translateMoveHelper(-5f,0f,0f,0f);

			if(this.BTGV.PV.getX() < ((xMin)* 64) + 96 || (this.BTGV.PV.getX() < 32)){
				this.BTGV.PV.setVariableX((xMin + 1)* 64 + 32);
			}
		}

		//MOVE EAST
		if(input.equals("D")){
			translateMoveHelper(5f,0f,0f,0f);

			if(this.BTGV.PV.getX() > ((xMax)* 64) - 32){
				this.BTGV.PV.setVariableX((xMax)* 64 - 32);
			}
		}


		if(input.equals("") && this.BTGV.PV.getVelocity().getY()==0){
			this.BTGV.PV.setVelocity(0,0);
		}
	}

	//Adds velocity to position
	private void update(){
		float tempVX = BTGV.PV.getVX();
		float tempVY = BTGV.PV.getVY();
		translateMoveHelper(tempVX, tempVY, 0f, 0f);
	}

	//Adds given value to pre-existing value - pretty much translate();
	private void translateMoveHelper(float x, float y, float vx, float vy){
		PlayerVariables newLocation;

		float tempX = BTGV.PV.getX();
		float tempY = BTGV.PV.getY();
		float tempVX = BTGV.PV.getVX();
		float tempVY = BTGV.PV.getVY();
		boolean airborne = this.BTGV.PV.isAirborne();
		BTGV.PV = null;

		newLocation = new PlayerVariables(tempX + (x), tempY + (y),tempVX + (vx), tempVY  + (vy));
		this.BTGV.setPv(newLocation);

		this.BTGV.PV.setAirborne(airborne);
	}


	private void setPlayerPosition(){
		float x = 0;
		float y = 0;
		try {
			x = inputStream.readFloat();
			y = inputStream.readFloat();
			this.BTGV.PV.setVariableX(x);
			this.BTGV.PV.setVariableY(y);
		} catch (IOException e) {
//			e.printStackTrace();
		}

	}

	public void receiveGameState(){
		PlayerVariables tempPvHolder = null;
		BrickTagGameVariables tempBTGVHolder = null;
		//System.out.println("receive game state");
		if(BTGV.PV != null){
			//System.out.println("receive GS print x before: " + BTGV.PV.getX() );
			tempPvHolder = BTGV.PV;
			tempBTGVHolder = BTGV;
		}

		try {
			for(ClientHandler ch : Server.playerList) {
				ch.BTGV = (BrickTagGameVariables) ch.objectInputStream.readObject();
			}
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}

		if(BTGV.PV != null){
			this.BTGV.setPv(tempPvHolder);
			this.BTGV = tempBTGVHolder;
		}
	}
}
