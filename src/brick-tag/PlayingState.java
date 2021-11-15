import jig.ResourceManager;
import jig.Vector;
import org.newdawn.slick.*;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;
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

	
	@Override
	public void init(GameContainer container, StateBasedGame game)
			throws SlickException {
	}

	@Override
	public void enter(GameContainer container, StateBasedGame game) {

		container.setSoundOn(true);
		BrickTagGame btg = (BrickTagGame) game;

		// setup level
		levelNumber = btg.level;

		if(levelNumber == 1) {
			setupLevel(btg, "Brick-Tag/src/brick-tag/resource/Level1.txt");
		} else if(levelNumber == 2) {
			setupLevel(btg, "Brick-Tag/src/brick-tag/resource/Level2.txt");
		}

		// ***** create elements in the world ****
		/*
		for(int i = 0; i < 28; i++) {
			for(int j = 0; j < 24; j++) {
			}
		}
		 */

	}

	public void setupLevel(BrickTagGame btg, String path) {
		try {
			File f = new File(path);
			Scanner scan = new Scanner(f);
			int j = 0;
			while (scan.hasNextLine()) {
				String data = scan.nextLine();
				for(int i = 0; i < data.length(); i++) {
					if(data.charAt(i) == '-') {
						// something else, can't occupy
						btg.tileGrid[i][j] = new Tile(i, j, -1, false);
					} else if(data.charAt(i) == '0') {
						// signify air tile
						btg.tileGrid[i][j] = new Tile(i, j, 0, true);
					} else if(data.charAt(i) == '1') {
						// tiles that are occupied by a world block
						btg.tileGrid[i][j] = new Tile(i, j, 1, false);
					} else if(data.charAt(i) == '2') {
						// tiles that are occupied by a player placed block
						btg.tileGrid[i][j] = new Tile(i, j, 2, false);
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
		g.setColor(Color.white);

		//g.drawString("Lives: " + btg.lives, 10, 30);
		//g.drawString("Score: " + btg.score, 110, 10);
		g.drawString("Level: " + btg.level, 110, 30);



		// draw grid
		if(btg.showGrid) {
			float x = 0;
			float y = 0;

			for (int i = 0; i < btg.WorldTileWidth; i++) {
				g.drawLine(x, 0, x, btg.ScreenHeight);
				x += btg.tileSize;
			}


			for (int i = 0; i < btg.WorldTileHeight + 1; i++) {
				g.drawLine(0, y, btg.ScreenWidth, y);
				y += btg.tileSize;
			}
		}

		// draw background

		// draw blocks
		//temporary, not an efficient way to do this, should only create them once
		for(int i = 0; i < btg.WorldTileWidth; i++) {
			for(int j = 0; j < btg.WorldTileHeight; j++) {
				if(btg.tileGrid[i][j].designation == 1) {
					// should be a block
					g.drawImage(ResourceManager.getImage(BrickTagGame.Block_RSC), i*btg.tileSize, j*btg.tileSize);
				}
			}
		}

		// draw others

		// draw players

	}

	@Override
	public void update(GameContainer container, StateBasedGame game,
			int delta) throws SlickException {

		Input input = container.getInput();
		BrickTagGame btg = (BrickTagGame) game;


		if (input.isKeyPressed(Input.KEY_0)) {
			btg.showGrid = !btg.showGrid;
		}

	}

	@Override
	public int getID() {
		return BrickTagGame.PLAYINGSTATE;
	}
	
}