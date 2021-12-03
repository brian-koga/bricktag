import jig.Entity;
import jig.ResourceManager;
import jig.Vector;
import org.newdawn.slick.*;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.lang.Math;


/**
 * This state is active when the Game is being played.
 * 
 * Transitions From StartUpState, LevelOverState
 * 
 * Transitions To GameOverState, LevelOverState
 */
class PlayingState extends BasicGameState {

	int levelNumber;
	
	@Override
	public void init(GameContainer container, StateBasedGame game)
			throws SlickException {
	}

	@Override
	public void enter(GameContainer container, StateBasedGame game) {

		container.setSoundOn(true);
		BrickTagGame btg = (BrickTagGame) game;
		BrickTagGameVariables btgV = btg.variables;

		//System.out.println(btg.player);

		btgV.PV = btg.player.getVariables();

		// setup level
		levelNumber = btgV.level;

		if(levelNumber == 1) {
			setupLevel(btg, "Brick-Tag/src/brick-tag/resource/Level1.txt");
		} else if(levelNumber == 2) {
			setupLevel(btg, "Brick-Tag/src/brick-tag/resource/Level2.txt");
		}

		btg.player.setTileGrid(btgV.tileGrid);
		sendNewGameState(btg, btgV);

		// ***** create elements in the world ****
		/*
		for(int i = 0; i < 28; i++) {
			for(int j = 0; j < 24; j++) {
			}
		}
		 */

	}

	private void sendNewGameState(BrickTagGame btg, BrickTagGameVariables btgV) {
		btg.client.sendString("NEW");
		try {
			btg.client.objectOutputStream.reset();
			btg.client.objectOutputStream.writeObject(btgV);
			btg.client.objectOutputStream.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setupLevel(BrickTagGame btg, String path) {
		BrickTagGameVariables btgV = btg.variables;
		try {
			File f = new File(path);
			Scanner scan = new Scanner(f);
			int j = 0;
			while (scan.hasNextLine()) {
				String data = scan.nextLine();
				for(int i = 0; i < data.length(); i++) {
					if(data.charAt(i) == '-') {
						// something else, can't occupy
						btgV.tileGrid[i][j] = new Tile(i, j, -1, false);
					} else if(data.charAt(i) == '0') {
						// signify air tile
						btgV.tileGrid[i][j] = new Tile(i, j, 0, true);
					} else if(data.charAt(i) == '1') {
						// tiles that are occupied by a world block
						btgV.tileGrid[i][j] = new Tile(i, j, 1, false);
					} else if(data.charAt(i) == '2') {
						// tiles that are occupied by a player placed block
						btgV.tileGrid[i][j] = new Tile(i, j, 2, false);
					} else {
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
	}


	@Override
	public void render(GameContainer container, StateBasedGame game,
			Graphics g) throws SlickException {
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
		for (VisibleObject objectToRender : btgV.PV.objectsToRender) {
			if(objectToRender.objectType == 'b')
				g.drawImage(ResourceManager.getImage(BrickTagGame.Block_RSC), objectToRender.x, objectToRender.y);
		}

		// draw players
		btg.player.render(g);
	}

	@Override
	public void update(GameContainer container, StateBasedGame game,
			int delta) throws SlickException {

		//System.out.println("update start");
		Input input = container.getInput();
		BrickTagGame btg = (BrickTagGame) game;
		BrickTagGameVariables btgV = btg.variables;

		if (input.isKeyPressed(Input.KEY_0)) {
			btg.client.sendString("0");
		}
		//Temporary
		else if(input.isKeyPressed(Input.KEY_ESCAPE)){
			btg.client.sendString("logout");
			System.exit(0);
		}else if(input.isKeyPressed(Input.KEY_SPACE)){
			btg.client.sendString("SPACE");
		}else if(input.isKeyDown(Input.KEY_A)){
			btg.client.sendString("A");
		}else if(input.isKeyDown(Input.KEY_D)){
			btg.client.sendString("D");
		}else{
			btg.client.sendString("");
		}


		if(btgV.PV != null) {
			//System.out.println("Client Coords: " + btgV.PV.getX() + " " + btgV.PV.getY());
			btg.player.setPosition(btgV.PV.getX(), btgV.PV.getY());
		}


		btg.setVariablesFromClient();
		btgV.setPv(btg.player.getVariables());

		if(btg.variables.currentState!=BrickTagGame.PLAYINGSTATE){ btg.enterState(btg.variables.currentState); }

		// change the player coordinates to screen coordinates
		calculateObjects(btgV);
		// this is fine since a new game state will be sent on the next update
		btg.player.setPosition(btgV.PV.x_SC, btgV.PV.y_SC);

	}

	public void calculateObjects(BrickTagGameVariables btgV) {
		// find where the player is, this will be the center (usually)
		float centerX = btgV.PV.getX();
		float centerY = btgV.PV.getY();

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
		int bottomTile = topTile + btgV.ScreenTileHeight;

		//System.out.println("calculateObjects: leftTile, rightTile is (" + leftTile + "," + rightTile + ")");
		//System.out.println("calculateObjects: topTile, bottomTile is (" + topTile + "," + bottomTile + ")");

		btgV.PV.objectsToRender.clear();


		for(int i = leftTile; i < rightTile; i++) {
			for(int j = topTile; j < bottomTile; j++) {
				if(btgV.tileGrid[i][j].designation == 1) {
					// should be a block
					btgV.PV.objectsToRender.add(new VisibleObject(i*btgV.tileSize - xDiff, j*btgV.tileSize - yDiff, 'b'));
				}
				// other players?
				// other objects?
			}
		}

		// change the player screen coordinates
		btgV.PV.x_SC = btgV.PV.getX() - xDiff;
		btgV.PV.y_SC = btgV.PV.getY() - yDiff;
	}

	private void sendPlayerPos(BrickTagGame btg){
		btg.client.sendPos(btg.player.getX());
		btg.client.sendPos(btg.player.getY());
	}

	@Override
	public int getID() {
		return BrickTagGame.PLAYINGSTATE;
	}
	
}