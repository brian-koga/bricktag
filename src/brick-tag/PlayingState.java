import jig.ResourceManager;
import org.newdawn.slick.*;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;


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

		//g.drawString("Lives: " + btg.lives, 10, 30);
		//g.drawString("Score: " + btg.score, 110, 10);
		g.drawString("Level: " + btgV.level, 110, 30);

		// draw grid
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
		}

		// draw background

		// draw blocks
		//temporary, not an efficient way to do this, should only create them once
	/*
		for(int i = 0; i < btgV.ScreenTileWidth; i++) {
			for(int j = 0; j < btgV.ScreenTileHeight; j++) {
				if(btgV.tileGrid[i][j].designation == 1) {
					// should be a block
					g.drawImage(ResourceManager.getImage(BrickTagGame.Block_RSC), i*btgV.tileSize, j*btgV.tileSize);
				}
			}
		}

	 */

		// draw others
		PlayerVariables PV = btg.allPlayers.get(this.playerIndex).getVariables();
		for (VisibleObject objectToRender : PV.objectsToRender) {
			if(objectToRender.objectType == 'b') {
				g.drawImage(ResourceManager.getImage(BrickTagGame.Block_RSC), objectToRender.x, objectToRender.y);
			}else if(objectToRender.objectType == 'x'){
				g.drawImage(ResourceManager.getImage(BrickTagGame.RED_GLASS_RSC), objectToRender.x, objectToRender.y);
			}else if(objectToRender.objectType == 'y'){
				g.drawImage(ResourceManager.getImage(BrickTagGame.BLUE_GLASS_RSC), objectToRender.x, objectToRender.y);
			}

			else if(objectToRender.objectType == 'p'){
				Player tempPlayerVariables = btg.allPlayers.get(objectToRender.playersIndexOnScreen);
				if(objectToRender.playersIndexOnScreen != this.playerIndex){
					getScreenCoords(objectToRender.playersIndexOnScreen,btg);
				}
				btg.allPlayers.get(objectToRender.playersIndexOnScreen).setPosition(tempPlayerVariables.getScreenX(), tempPlayerVariables.getScreenY());
				btg.allPlayers.get(objectToRender.playersIndexOnScreen).render(g);
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
		}else if(input.isKeyDown(Input.KEY_A)){
			kc.command = "A";
		}else if(input.isKeyDown(Input.KEY_D)){
			kc.command = "D";
		}else if(input.isKeyDown(Input.KEY_Q)){
			kc.command = "Q";
		}else if(input.isKeyDown(Input.KEY_E)){
			kc.command = "E";
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

		ArrayList<Tile> temp = btgV.placedTiles;

		// check the placed tile array
		for (Tile t: temp) {
			if(t.x >= leftTile && t.x <= rightTile && t.y >= topTile && t.y <= bottomTile) {
				if(t.designation == 2) {
					PV.objectsToRender.add(new VisibleObject(t.x*btgV.tileSize - xDiff, t.y*btgV.tileSize - yDiff, 'x'));
				} else if(t.designation == 3) {
					PV.objectsToRender.add(new VisibleObject(t.x*btgV.tileSize - xDiff, t.y*btgV.tileSize - yDiff, 'y'));
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
