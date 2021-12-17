import jig.ResourceManager;
import org.newdawn.slick.*;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Vector;


/**
 * This state is active when the Game is being played.
 *
 * Transitions From StartUpState, LevelOverState
 *
 * Transitions To GameOverState, LevelOverState
 */
class PlayingState extends BasicGameState {

	int levelNumber;
	int playerIndex;

	@Override
	public void init(GameContainer container, StateBasedGame game)
			throws SlickException {
	}

	@Override
	public void enter(GameContainer container, StateBasedGame game) {
		//System.out.println("enter");

		container.setSoundOn(true);
		BrickTagGame btg = (BrickTagGame) game;
		BrickTagGameVariables btgV = btg.variables;

		// setup level
		levelNumber = btgV.level;
		btg.tileGrid = new Tile[btgV.WorldTileWidth][btgV.WorldTileHeight];

		if(levelNumber == 1) {
			btg.tileGrid = setupLevel(btgV, "Brick-Tag/src/brick-tag/resource/Level1.txt");
		} else if(levelNumber == 2) {
			btg.tileGrid = setupLevel(btgV, "Brick-Tag/src/brick-tag/resource/Level2.txt");
		}

		playerIndex = btg.player.getIndex();
		//btg.player.setTileGrid(btgV.tileGrid);

		sendNewPlayerVariables(btg);
		btg.setVariablesFromClient();

		// ***** create elements in the world ****
		/*
		for(int i = 0; i < 28; i++) {
			for(int j = 0; j < 24; j++) {
			}
		}
		 */

	}

	private void sendNewPlayerVariables(BrickTagGame btg) {
		//System.out.println("sendNewPlayerVariables");

		KeyboardCommand kc = new KeyboardCommand(this.playerIndex,"PV");
		PlayingState.sendKeyboardCommands(kc,btg);
		try {
			btg.client.objectOutputStream.reset();
			btg.client.objectOutputStream.writeObject(btg.player.getVariables());
			btg.client.objectOutputStream.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	static void sendKeyboardCommands(KeyboardCommand kc,BrickTagGame btg) {
		try {
			btg.client.objectOutputStream.reset();
			btg.client.objectOutputStream.writeObject(kc);
			btg.client.objectOutputStream.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static Tile[][] setupLevel(BrickTagGameVariables btgV, String path) {
		Tile[][] tileGrid = new Tile[btgV.WorldTileWidth][btgV.WorldTileHeight];
		try {
			File f = new File(path);
			Scanner scan = new Scanner(f);
			int j = 0;
			while (scan.hasNextLine()) {
				String data = scan.nextLine();
				for(int i = 0; i < data.length(); i++) {
					if(data.charAt(i) == '-') {
						// something else, can't occupy
						tileGrid[i][j] = new Tile(i, j, -1, false);
					} else if(data.charAt(i) == '0') {
						// signify air tile
						tileGrid[i][j] = new Tile(i, j, 0, true);
					} else if(data.charAt(i) == '1') {
						// tiles that are occupied by a world block
						tileGrid[i][j] = new Tile(i, j, 1, false);
					} else if(data.charAt(i) == '2') {
						// tiles that are occupied by a player placed block
						tileGrid[i][j] = new Tile(i, j, 2, false);
					} else if(data.charAt(i) == '3') {
						// tiles that are occupied by a player placed block
						tileGrid[i][j] = new Tile(i, j, 3, false);
					} else if(data.charAt(i) == '7') {
						//metal block
						tileGrid[i][j] = new Tile(i, j, 7, false);
					} else if(data.charAt(i) == '8') {
						//ground1 block
						tileGrid[i][j] = new Tile(i, j, 8, false);
					} else if(data.charAt(i) == '9') {
						//ground2 block
						tileGrid[i][j] = new Tile(i, j, 9, false);
					}else if(data.charAt(i) == 'e') {
						//metal right block
						tileGrid[i][j] = new Tile(i, j, 10, false);
					}else if(data.charAt(i) == 'w') {
						//metal left block
						tileGrid[i][j] = new Tile(i, j, 11, false);
					}else if(data.charAt(i) == 'c') {
						//metal center block
						tileGrid[i][j] = new Tile(i, j, 12, false);
					}else if(data.charAt(i) == 'a') {
						//concrete1 block
						tileGrid[i][j] = new Tile(i, j, 13, false);
					}else if(data.charAt(i) == 'b') {
						//concrete2 block
						tileGrid[i][j] = new Tile(i, j, 14, false);
					}else if(data.charAt(i) == 'x') {
						//box block
						tileGrid[i][j] = new Tile(i, j, 15, false);
					}else if(data.charAt(i) == 'q') {
						//box block
						tileGrid[i][j] = new Tile(i, j, 16, false);
					}else {
						// something has gone wrong
						System.out.println("Unknown character encountered in level file.");
					}
				}
				j++;
			}
			scan.close();
		} catch (FileNotFoundException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		}
		return tileGrid;
	}

	@Override
	public void render(GameContainer container, StateBasedGame game, Graphics g) {
		//System.out.println("render");
		BrickTagGame btg = (BrickTagGame) game;
		BrickTagGameVariables btgV = btg.variables;
		g.setColor(Color.white);

		g.drawString("Level: " + btgV.level, 10, 30);

		// draw grid
		/*
		if(btgV.showGrid) {
			float x = 0;
			float y = 0;

			for (int i = 0; i < btgV.ScreenTileWidth; i++) {
				g.drawLine(x, 0, x, btgV.ScreenHeight);
				x += btgV.tileSize;
			}

			for (int i = 0; i < btgV.ScreenTileHeight + 1; i++) {
				g.drawLine(0, y, btgV.ScreenWidth, y);
				y += btgV.tileSize;
			}
		}*/





		// draw background
		float backgroundX = getBackgroundCoords(btg, 0);
		float backgroundY = getBackgroundCoords(btg, 1);

		if(btgV.showGrid) {
			g.drawImage(ResourceManager.getImage(BrickTagGame.NIGHT_1_RSC), 0 - (backgroundX / 5), -220 - (backgroundY / 5));
			g.drawImage(ResourceManager.getImage(BrickTagGame.NIGHT_2_RSC), 0 - (backgroundX / 4), -220 - (backgroundY / 4));
			g.drawImage(ResourceManager.getImage(BrickTagGame.NIGHT_3_RSC), 0 - (backgroundX / 3), -220 - (backgroundY / 3));
			g.drawImage(ResourceManager.getImage(BrickTagGame.NIGHT_4_RSC), 0 - (backgroundX / 2), -220 - (backgroundY / 2));
			g.drawImage(ResourceManager.getImage(BrickTagGame.NIGHT_5_RSC), 0 - (backgroundX), -220 - (backgroundY));
		}else {
			g.drawImage(ResourceManager.getImage(BrickTagGame.DAY_1_RSC), 0 - (backgroundX / 5), -220 - (backgroundY / 5));
			g.drawImage(ResourceManager.getImage(BrickTagGame.DAY_2_RSC), 0 - (backgroundX / 4), -220 - (backgroundY / 4));
			g.drawImage(ResourceManager.getImage(BrickTagGame.DAY_3_RSC), 0 - (backgroundX / 3), -220 - (backgroundY / 3));
			g.drawImage(ResourceManager.getImage(BrickTagGame.DAY_4_RSC), 0 - (backgroundX / 2), -220 - (backgroundY / 2));
			g.drawImage(ResourceManager.getImage(BrickTagGame.DAY_5_RSC), 0 - (backgroundX), -220 - (backgroundY));
		}

		// draw others
		PlayerVariables PV = btg.allPlayers.get(this.playerIndex).getVariables();
		for (VisibleObject objectToRender : PV.objectsToRender) {
			if(objectToRender.objectType == 'b') {
				g.drawImage(ResourceManager.getImage(BrickTagGame.Block_RSC), objectToRender.x, objectToRender.y);
			}else if(objectToRender.objectType == 'w'){
				g.drawImage(ResourceManager.getImage(BrickTagGame.BLUE_GLASS_RSC), objectToRender.x, objectToRender.y);
			}else if(objectToRender.objectType == 'x'){
				g.drawImage(ResourceManager.getImage(BrickTagGame.GREEN_GLASS_RSC), objectToRender.x, objectToRender.y);
			}else if(objectToRender.objectType == 'y'){
				g.drawImage(ResourceManager.getImage(BrickTagGame.RED_GLASS_RSC), objectToRender.x, objectToRender.y);
			}else if(objectToRender.objectType == 'z'){
				g.drawImage(ResourceManager.getImage(BrickTagGame.YELLOW_GLASS_RSC), objectToRender.x, objectToRender.y);
			}else if(objectToRender.objectType == 's') {
				g.drawImage(ResourceManager.getImage(BrickTagGame.BOOTS_RSC), objectToRender.x, objectToRender.y);
			}else if(objectToRender.objectType == 'a') {
				g.drawImage(ResourceManager.getImage(BrickTagGame.PIC_RSC), objectToRender.x, objectToRender.y);
			}else if(objectToRender.objectType == 'f'){
				g.drawImage(ResourceManager.getImage(BrickTagGame.FLAG_RSC),objectToRender.x,objectToRender.y);
			}else if(objectToRender.objectType == 'm'){
				g.drawImage(ResourceManager.getImage(BrickTagGame.METAL_RSC),objectToRender.x,objectToRender.y);
			}else if(objectToRender.objectType == 'g'){
				g.drawImage(ResourceManager.getImage(BrickTagGame.GROUND1_RSC),objectToRender.x,objectToRender.y);
			}else if(objectToRender.objectType == 'i'){
				g.drawImage(ResourceManager.getImage(BrickTagGame.METAL_RIGHT_RSC),objectToRender.x,objectToRender.y);
			}else if(objectToRender.objectType == 'j'){
				g.drawImage(ResourceManager.getImage(BrickTagGame.METAL_LEFT_RSC),objectToRender.x,objectToRender.y);
			}else if(objectToRender.objectType == 'k'){
				g.drawImage(ResourceManager.getImage(BrickTagGame.METAL_CENTER_RSC),objectToRender.x,objectToRender.y);
			}else if(objectToRender.objectType == 'l'){
				g.drawImage(ResourceManager.getImage(BrickTagGame.CONCRETE1_RSC),objectToRender.x,objectToRender.y);
			}else if(objectToRender.objectType == 'n'){
				g.drawImage(ResourceManager.getImage(BrickTagGame.CONCRETE2_RSC),objectToRender.x,objectToRender.y);
			}else if(objectToRender.objectType == 'o'){
				g.drawImage(ResourceManager.getImage(BrickTagGame.BOX_RSC),objectToRender.x,objectToRender.y);
			}else if(objectToRender.objectType == 'q'){
				g.drawImage(ResourceManager.getImage(BrickTagGame.METAL_Vertical_RSC),objectToRender.x,objectToRender.y);
			}else if(objectToRender.objectType == 'e'){
				g.drawImage(ResourceManager.getImage(BrickTagGame.MORE_BRICKS_RSC), objectToRender.x ,objectToRender.y);
			}else if(objectToRender.objectType == 'v'){
				g.drawImage(ResourceManager.getImage(BrickTagGame.SCORE_UP_RSC), objectToRender.x ,objectToRender.y);
			}

			else if(objectToRender.objectType == 'p'){
				int scale = 0;

				if(objectToRender.playersIndexOnScreen == 0) {
					if (btgV.p1_orientation.equals("RL")) { scale = 0; }
					if (btgV.p1_orientation.equals("RR")) { scale = 1; }
					if (btgV.p1_orientation.equals("SL")) { scale = 2; }
					if (btgV.p1_orientation.equals("SR")) { scale = 3; }
				}
				if(objectToRender.playersIndexOnScreen == 1) {
					if (btgV.p2_orientation.equals("RL")) { scale = 4; }
					if (btgV.p2_orientation.equals("RR")) { scale = 5; }
					if (btgV.p2_orientation.equals("SL")) { scale = 6; }
					if (btgV.p2_orientation.equals("SR")) { scale = 7; }
				}
				if(objectToRender.playersIndexOnScreen == 2) {
					if (btgV.p3_orientation.equals("RL")) { scale = 8; }
					if (btgV.p3_orientation.equals("RR")) { scale = 9; }
					if (btgV.p3_orientation.equals("SL")) { scale = 10; }
					if (btgV.p3_orientation.equals("SR")) { scale = 11; }
				}
				if(objectToRender.playersIndexOnScreen == 3) {
					if (btgV.p4_orientation.equals("RL")) { scale = 12; }
					if (btgV.p4_orientation.equals("RR")) { scale = 13; }
					if (btgV.p4_orientation.equals("SL")) { scale = 14; }
					if (btgV.p4_orientation.equals("SR")) { scale = 15; }
				}

				int orientation_index = scale;


				Player tempPlayerVariables = btg.allPlayers.get(objectToRender.playersIndexOnScreen);
				if(!tempPlayerVariables.getVariables().isLoggedIn){
					continue;
				}

				if((objectToRender.playersIndexOnScreen) != this.playerIndex){
					getScreenCoords((objectToRender.playersIndexOnScreen),btg);
				}

				//System.out.println("object to render index: " + objectToRender.playersIndexOnScreen);
				//System.out.println("orientation index: " + orientation_index);

				btg.allPlayers.get(orientation_index).setPosition(tempPlayerVariables.getScreenX(), tempPlayerVariables.getScreenY());
				btg.allPlayers.get(orientation_index).render(g);
			}
		}

		//System.out.println("All Players: " + btg.allPlayers);


		g.drawString( "Bricks: " + PV.getNumberOfBricks(),15,700);
		for(int i=0;i<btgV.playerList.size();i++){
			int score = btgV.scoreList.get(i);
			int power = btgV.playerList.get(i).powerUp;


			int flagHolder = btgV.getFlagHolder();
			if(flagHolder >= 0){ g.drawImage(ResourceManager.getImage(BrickTagGame.FLAG_MINI_RSC), 1180, 20 * (flagHolder)); }

			// this is so that the power up icon is drawn directly left of the head, unless that player has the flag
			// in which case it is drawn directly left of the flag
			int powerIconXLocation = 1180;
			if(flagHolder == i+1) {
				powerIconXLocation -= 20;
			}

			if(i>=0){
				g.drawImage(ResourceManager.getImage(BrickTagGame.BLUE_MINI_RSC), 1200, 20 * (i+1));
			}
			if(i>=1){
				g.drawImage(ResourceManager.getImage(BrickTagGame.GREEN_MINI_RSC), 1200, 20 * (i+1));
			}
			if(i>=2){
				g.drawImage(ResourceManager.getImage(BrickTagGame.RED_MINI_RSC), 1200, 20 * (i+1));
			}
			if(i>=3){
				g.drawImage(ResourceManager.getImage(BrickTagGame.YELLOW_MINI_RSC), 1200, 20 * (i+1));
			}

			if(score >= 20){
				g.drawImage(ResourceManager.getImage(BrickTagGame.BAR5_RSC), 1225,20*(i+1));
			}else if (score >= 15){
				g.drawImage(ResourceManager.getImage(BrickTagGame.BAR4_RSC), 1225,20*(i+1));
			}else if (score >= 10){
				g.drawImage(ResourceManager.getImage(BrickTagGame.BAR3_RSC), 1225,20*(i+1));
			}else if (score >= 5){
				g.drawImage(ResourceManager.getImage(BrickTagGame.BAR2_RSC), 1225,20*(i+1));
			}else{
				g.drawImage(ResourceManager.getImage(BrickTagGame.BAR1_RSC), 1225,20*(i+1));
			}

			//g.drawString(""+score,1225,20*(i+1));

			// draw power up
			if(power != 0) {
				if(power == 1) {
					// speed
					g.drawImage(ResourceManager.getImage(BrickTagGame.BOOTS_MINI_RSC), powerIconXLocation, 20 * (i+1));
				}else if(power == 2){
					g.drawImage(ResourceManager.getImage(BrickTagGame.PIC_MINI_RSC),powerIconXLocation,20*(i+1));
				}
			}
		}
	}

	private void getScreenCoords(int index,BrickTagGame btg){
		Player mainPlayer = btg.allPlayers.get(this.playerIndex);
		Player currPlayer = btg.allPlayers.get(index);

		//Lets figure out what the world coordinates are on screen
		float worldLeft = mainPlayer.getWorldX() - btg.variables.ScreenWidth/2;
		float worldTop = mainPlayer.getWorldY() - btg.variables.ScreenHeight/2;

		if(worldLeft<0){
			worldLeft=0;
		} else if(worldLeft > btg.variables.WorldWidth - btg.variables.ScreenWidth) {
			worldLeft = btg.variables.WorldWidth - btg.variables.ScreenWidth;
		}

		if(worldTop<0){
			worldTop=0;
		} else if(worldTop > btg.variables.WorldHeight - btg.variables.ScreenHeight) {
			worldTop = btg.variables.WorldHeight - btg.variables.ScreenHeight;
		}

		float newX = currPlayer.getWorldX() - worldLeft;
		float newY = currPlayer.getWorldY() - worldTop;

		currPlayer.setScreenPosition(newX,newY);
	}

	private float getBackgroundCoords(BrickTagGame btg, int xy){
		Player mainPlayer = btg.allPlayers.get(this.playerIndex);

		//Lets figure out what the world coordinates are on screen
		float worldLeft = mainPlayer.getWorldX() - btg.variables.ScreenWidth/2;
		float worldTop = mainPlayer.getWorldY() - btg.variables.ScreenHeight/2;

		if(worldLeft<0){
			worldLeft=0;
		} else if(worldLeft > btg.variables.WorldWidth - btg.variables.ScreenWidth) {
			worldLeft = btg.variables.WorldWidth - btg.variables.ScreenWidth;
		}

		if(worldTop<0){
			worldTop=0;
		} else if(worldTop > btg.variables.WorldHeight - btg.variables.ScreenHeight) {
			worldTop = btg.variables.WorldHeight - btg.variables.ScreenHeight;
		}

		if(xy == 0){ return worldLeft; }
		if(xy == 1){ return worldTop; }
		return -99999;
	}

	@Override
	public void update(GameContainer container, StateBasedGame game,int delta){
		//System.out.println("update");
		Input input = container.getInput();
		BrickTagGame btg = (BrickTagGame) game;
		BrickTagGameVariables btgV = btg.variables;

		KeyboardCommand kc = new KeyboardCommand();
		kc.index = playerIndex;

		//System.out.println("KC INDEX: " + kc.index);

		if (input.isKeyPressed(Input.KEY_0)) {
			kc.command = "DEBUG";
		}else if(input.isKeyPressed(Input.KEY_ESCAPE)){
			kc.command = "logout";
			System.exit(0);
		}else if(input.isKeyPressed(Input.KEY_SPACE)){
			kc.command = "SPACE";
		}else if(input.isKeyDown(Input.KEY_A) && input.isKeyDown(Input.KEY_E)){
			kc.command = "AE";
		}else if(input.isKeyDown(Input.KEY_A) && input.isKeyDown(Input.KEY_Q)){
			kc.command = "AQ";
		}else if(input.isKeyDown(Input.KEY_D) && input.isKeyDown(Input.KEY_E)){
			kc.command = "DE";
		}else if(input.isKeyDown(Input.KEY_D) && input.isKeyDown(Input.KEY_Q)){
			kc.command = "DQ";
		}else if(input.isKeyDown(Input.KEY_A) && input.isKeyDown(Input.KEY_W)){
			kc.command = "AW";
		}else if(input.isKeyDown(Input.KEY_A) && input.isKeyDown(Input.KEY_S)){
			kc.command = "AS";
		}else if(input.isKeyDown(Input.KEY_D) && input.isKeyDown(Input.KEY_W)){
			kc.command = "DW";
		}else if(input.isKeyDown(Input.KEY_D) && input.isKeyDown(Input.KEY_S)){
			kc.command = "DS";
		}else if(input.isKeyDown(Input.KEY_F) && input.isKeyDown(Input.KEY_A)){
			kc.command = "ARIGHT";
		}else if(input.isKeyDown(Input.KEY_LSHIFT) && input.isKeyDown(Input.KEY_A)){
			kc.command = "ALEFT";
		}else if(input.isKeyDown(Input.KEY_F) && input.isKeyDown(Input.KEY_D)){
			kc.command = "DRIGHT";
		}else if(input.isKeyDown(Input.KEY_LSHIFT) && input.isKeyDown(Input.KEY_D)){
			kc.command = "DLEFT";
		}else if(input.isKeyDown(Input.KEY_A)){
			kc.command = "A";
		}else if(input.isKeyDown(Input.KEY_D)){
			kc.command = "D";
		}else if(input.isKeyDown(Input.KEY_Q)){
			kc.command = "Q";
		}else if(input.isKeyDown(Input.KEY_E)){
			kc.command = "E";
		}else if(input.isKeyDown(Input.KEY_W)){
			kc.command = "W";
		}else if(input.isKeyDown(Input.KEY_S)){
			kc.command = "S";
		}else if(input.isKeyDown(Input.KEY_F)){
			//Leave the space there its there for a reason
			kc.command = "_RIGHT";
		}else if(input.isKeyDown(Input.KEY_LSHIFT)){
			//Leave the space there its there for a reason
			kc.command = "_LEFT";
		}else{
			kc.command = "";
		}

		sendKeyboardCommands(kc,btg);

		btg.setVariablesFromClient();

		// change the player coordinates to screen coordinates
		setPlayerPositions(btg, btgV);

		if(btg.variables.currentState!=BrickTagGame.PLAYINGSTATE){ btg.enterState(btg.variables.currentState); }
	}

	public PlayerVariables calculateObjects(BrickTagGame btg, BrickTagGameVariables btgV, PlayerVariables PV) {
		// find where the player is, this will be the center (usually)
		float centerX = PV.getX();
		float centerY = PV.getY();

		//System.out.println("calculateObjects, precheck: player is at (" + BTGV.PV.getX() + "," +BTGV.PV.getY() + ")");
		//System.out.println("calculateObjects, precheck: center is at (" + centerX + "," + centerY + ")");

		// calculate edges of screen (in world coordinates)

		// left and right
		float left = centerX - btgV.ScreenWidth/2;
		float right = centerX + btgV.ScreenWidth/2;
		if(left < 0) {
			left = 0;
			centerX = btgV.ScreenWidth/2;
			right = btgV.ScreenWidth;
		}

		if(right > btgV.WorldWidth) {
			right = btgV.WorldWidth;
			centerX = btgV.WorldWidth - btgV.ScreenWidth/2;
			left = btgV.WorldWidth - btgV.ScreenWidth;
		}

		// top and bottom
		float top = centerY - btgV.ScreenHeight/2;
		float bottom = centerY + btgV.ScreenHeight/2;
		if(top < 0) {
			top = 0;
			centerY = btgV.ScreenHeight / 2;
			bottom = btgV.ScreenHeight;
		}

		if(bottom > btgV.WorldHeight) {
			bottom = btgV.WorldHeight;
			centerY = btgV.WorldHeight - btgV.ScreenHeight/2;
			top = btgV.WorldHeight - btgV.ScreenHeight;
		}

		//System.out.println("calculateObjects: player is at (" + BTGV.PV.getX() + "," +BTGV.PV.getY() + ")");
		//System.out.println("calculateObjects: center is at (" + centerX + "," + centerY + ")");
		//System.out.println("calculateObjects: left, right is at (" + left + "," + right + ")");
		//System.out.println("calculateObjects: top, bottom is at (" + top + "," + bottom + ")");

		// define the center coordinates of screen (this is based on the screen size)
		float centerX_SC = btgV.ScreenWidth/2;
		float centerY_SC = btgV.ScreenHeight/2;

		float xDiff = centerX - centerX_SC;
		float yDiff = centerY - centerY_SC;

		int leftTile = (int) (left/64);
		int topTile = (int) (top/64);
		int rightTile = leftTile + btgV.ScreenTileWidth;

		// needed for the tile check later
		if(rightTile == btgV.WorldTileWidth) {
			rightTile = btgV.WorldTileWidth - 1;
		}
		int bottomTile = topTile + btgV.ScreenTileHeight;

		// needed for the tile check later
		if(bottomTile == btgV.WorldTileHeight) {
			bottomTile = btgV.WorldTileHeight - 1;
		}

		//System.out.println("calculateObjects: leftTile, rightTile is (" + leftTile + "," + rightTile + ")");
		//System.out.println("calculateObjects: topTile, bottomTile is (" + topTile + "," + bottomTile + ")");

		PV.objectsToRender.clear();


		for(int i = leftTile; i <= rightTile; i++) {
			for(int j = topTile; j <= bottomTile; j++) {
				if(btg.tileGrid[i][j].designation == 1) {
					// should be a block
					PV.objectsToRender.add(new VisibleObject(i*btgV.tileSize - xDiff, j*btgV.tileSize - yDiff, 'b'));
				}else if(btg.tileGrid[i][j].designation == 9) {
					PV.objectsToRender.add(new VisibleObject(i*btgV.tileSize - xDiff, j*btgV.tileSize - yDiff, 'm'));
				}else if(btg.tileGrid[i][j].designation == 8) {
					PV.objectsToRender.add(new VisibleObject(i*btgV.tileSize - xDiff, j*btgV.tileSize - yDiff, 'g'));
				}else if(btg.tileGrid[i][j].designation == 7) {
					PV.objectsToRender.add(new VisibleObject(i*btgV.tileSize - xDiff, j*btgV.tileSize - yDiff, 'h'));
				}else if(btg.tileGrid[i][j].designation == 10) {
					PV.objectsToRender.add(new VisibleObject(i*btgV.tileSize - xDiff, j*btgV.tileSize - yDiff, 'i'));
				}else if(btg.tileGrid[i][j].designation == 11) {
					PV.objectsToRender.add(new VisibleObject(i*btgV.tileSize - xDiff, j*btgV.tileSize - yDiff, 'j'));
				}else if(btg.tileGrid[i][j].designation == 12) {
					PV.objectsToRender.add(new VisibleObject(i*btgV.tileSize - xDiff, j*btgV.tileSize - yDiff, 'k'));
				}else if(btg.tileGrid[i][j].designation == 13) {
					PV.objectsToRender.add(new VisibleObject(i*btgV.tileSize - xDiff, j*btgV.tileSize - yDiff, 'l'));
				}else if(btg.tileGrid[i][j].designation == 14) {
					PV.objectsToRender.add(new VisibleObject(i*btgV.tileSize - xDiff, j*btgV.tileSize - yDiff, 'n'));
				}else if(btg.tileGrid[i][j].designation == 15) {
					PV.objectsToRender.add(new VisibleObject(i*btgV.tileSize - xDiff, j*btgV.tileSize - yDiff, 'o'));
				}else if(btg.tileGrid[i][j].designation == 16) {
					PV.objectsToRender.add(new VisibleObject(i*btgV.tileSize - xDiff, j*btgV.tileSize - yDiff, 'q'));
				}
				/*
				if(btg.tileGrid[i][j].designation == 2) {
					// should be a block
					PV.objectsToRender.add(new VisibleObject(i*btgV.tileSize - xDiff, j*btgV.tileSize - yDiff, 'x'));
				}
				if(btg.tileGrid[i][j].designation == 3) {
					// should be a block
					PV.objectsToRender.add(new VisibleObject(i*btgV.tileSize - xDiff, j*btgV.tileSize - yDiff, 'y'));
				}

				 */

				// other objects?
			}
		}

		Vector<Tile> temp = btgV.placedTiles;

		// check the placed tile array
		for (Tile t: temp) {
			if(t.x >= leftTile && t.x <= rightTile && t.y >= topTile && t.y <= bottomTile) {
				if(t.designation == 2) {
					PV.objectsToRender.add(new VisibleObject(t.x*btgV.tileSize - xDiff, t.y*btgV.tileSize - yDiff, 'w'));
				} else if(t.designation == 3) {
					PV.objectsToRender.add(new VisibleObject(t.x*btgV.tileSize - xDiff, t.y*btgV.tileSize - yDiff, 'x'));
				} else if(t.designation == 4) {
					PV.objectsToRender.add(new VisibleObject(t.x*btgV.tileSize - xDiff, t.y*btgV.tileSize - yDiff, 'y'));
				} else if(t.designation == 5) {
					PV.objectsToRender.add(new VisibleObject(t.x*btgV.tileSize - xDiff, t.y*btgV.tileSize - yDiff, 'z'));
				}
				//This is temporary
				else{
					PV.objectsToRender.add(new VisibleObject(t.x*btgV.tileSize - xDiff, t.y*btgV.tileSize - yDiff, 'w'));
				}
			}
		}

		temp = btgV.powerUpTiles;

		// check the power up tile array
		for (Tile t: temp) {
			if(t.x >= leftTile && t.x <= rightTile && t.y >= topTile && t.y <= bottomTile) {
				if(t.designation == 21) {
					PV.objectsToRender.add(new VisibleObject(t.x*btgV.tileSize - xDiff, t.y*btgV.tileSize - yDiff, 's'));
				}else if(t.designation == 22) {
					PV.objectsToRender.add(new VisibleObject(t.x*btgV.tileSize - xDiff, t.y*btgV.tileSize - yDiff, 'a'));
				}else if(t.designation == 23){
					PV.objectsToRender.add(new VisibleObject(t.x*btgV.tileSize - xDiff, t.y*btgV.tileSize - yDiff, 'e'));
				}else if(t.designation == 24){
					PV.objectsToRender.add(new VisibleObject(t.x*btgV.tileSize - xDiff, t.y*btgV.tileSize - yDiff, 'v'));
				}else if(t.designation == 99){
					PV.objectsToRender.add(new VisibleObject(t.x*btgV.tileSize - xDiff, t.y*btgV.tileSize - yDiff,'f'));
				}
			}
		}

		// change the player screen coordinates
		PV.x_SC = PV.getX() - xDiff;
		PV.y_SC = PV.getY() - yDiff;

		for(int i = 0; i< btgV.playerList.size(); i++){
			PlayerVariables playerVariables = btgV.playerList.get(i);
			if(playerVariables.getX()>=left && playerVariables.getX()<=right && playerVariables.getY()>=top && playerVariables.getY()<=bottom){
				PV.objectsToRender.add(new VisibleObject(i,'p'));
			}
		}
		return PV;
	}

	private void setPlayerPositions(BrickTagGame btg, BrickTagGameVariables btgV) {
		for(int i = 0; i<btgV.playerList.size(); i++){
			PlayerVariables currPV = btgV.playerList.get(i);
			currPV = calculateObjects(btg, btgV,currPV);
			btgV.playerList.set(i,currPV);
			btg.allPlayers.get(i).setVariables(currPV);
			btg.allPlayers.get(i).setScreenPosition();
			btg.allPlayers.get(i).setWorldPosition();
		}
	}

	@Override
	public int getID() {
		return BrickTagGame.PLAYINGSTATE;
	}
}
