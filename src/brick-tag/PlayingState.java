import jig.ResourceManager;
import jig.Vector;
import org.lwjgl.Sys;
import org.newdawn.slick.*;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
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
	boolean airborn = true;

	
	@Override
	public void init(GameContainer container, StateBasedGame game)
			throws SlickException {
	}

	@Override
	public void enter(GameContainer container, StateBasedGame game) {

		container.setSoundOn(true);
		BrickTagGame btg = (BrickTagGame) game;
		BrickTagGameVariables btgV = btg.variables;

		// setup level
		levelNumber = btgV.level;

		if(levelNumber == 1) {
			setupLevel(btg, "Brick-Tag/src/brick-tag/resource/Level1.txt");
		} else if(levelNumber == 2) {
			setupLevel(btg, "Brick-Tag/src/brick-tag/resource/Level2.txt");
		}

		sendNewMap(btg, btgV);

		// ***** create elements in the world ****
		/*
		for(int i = 0; i < 28; i++) {
			for(int j = 0; j < 24; j++) {
			}
		}
		 */

	}

	private void sendNewMap(BrickTagGame btg, BrickTagGameVariables btgV) {
		btg.client.sendString("NEW_MAP");
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

			for (int i = 0; i < btgV.WorldTileWidth; i++) {
				g.drawLine(x, 0, x, btgV.ScreenHeight);
				x += btgV.tileSize;
			}

			for (int i = 0; i < btgV.WorldTileHeight + 1; i++) {
				g.drawLine(0, y, btgV.ScreenWidth, y);
				y += btgV.tileSize;
			}
		}

		// draw background

		// draw blocks
		//temporary, not an efficient way to do this, should only create them once
		for(int i = 0; i < btgV.WorldTileWidth; i++) {
			for(int j = 0; j < btgV.WorldTileHeight; j++) {
				if(btgV.tileGrid[i][j].designation == 1) {
					// should be a block
					g.drawImage(ResourceManager.getImage(BrickTagGame.Block_RSC), i*btgV.tileSize, j*btgV.tileSize);
				}
			}
		}

		// draw others

		// draw players
		btg.player.render(g);

	}






	/*

	//checks for collisions with a provided brick & the ball, return 0 on no collision, 1 on side, 2 on roof/floor
	public int collision(GameContainer container, StateBasedGame game, Tile object) throws SlickException {
		BrickTagGame btg = (BrickTagGame) game;

		if (btg.ball.getCoarseGrainedMaxX() > object.getCoarseGrainedMinX()){
			if(btg.ball.getCoarseGrainedMinX() < object.getCoarseGrainedMaxX()){
				if( btg.ball.getCoarseGrainedMaxY() > object.getCoarseGrainedMinY() &&
						btg.ball.getCoarseGrainedMinY() < object.getCoarseGrainedMaxY()) {

					//variables for each side, value closest to zero is collision side
					float west, east, north, south;

					west = (bg.ball.getCoarseGrainedMaxX() - object.getCoarseGrainedMinX());
					east = (bg.ball.getCoarseGrainedMinX() - object.getCoarseGrainedMaxX());
					north = (bg.ball.getCoarseGrainedMaxY() - object.getCoarseGrainedMinY());
					south = (bg.ball.getCoarseGrainedMinY() - object.getCoarseGrainedMaxY());

					if(west < 0) { west = west * (-1);}
					if(east < 0) { east = east * (-1);}
					if(north < 0) { north = north * (-1);}
					if(south < 0) { south = south * (-1);}

					//System.out.println("N: " + north + " E: " + east + " S: " + south + " W: " + west);

					if(((north < east) && (north < west)) || ((south < east) && (south < west))){
						//System.out.println("north/south");
						return 2;
					}

					return 1;
				}
			}
		}
		return 0;
	}


	*/



	@Override
	public void update(GameContainer container, StateBasedGame game,
			int delta) throws SlickException {

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
		}else{
			btg.client.sendString("");
		}






		// "infinite" value
		int xMax = 99;
		int yMax = 99;
		int xMin = -99;
		int yMin = -99; //used for checking "head bonks" on block above. not implemented yet

		//get player position in tile grid
		int playerX = (int)Math.floor(btg.player.getX() / 64);
		int playerY = (int)Math.floor(btg.player.getY() / 64);

		//East
		if( btgV.tileGrid[playerX + 1][playerY].designation != 0){ xMax = playerX + 1; }

		//West
		if( btgV.tileGrid[playerX - 1][playerY].designation != 0){ xMin = playerX - 1; }

		//South
		if( btgV.tileGrid[playerX][playerY + 1].designation != 0){
			yMax = playerY + 1;
		}else{
			airborn = true;
		}

		//Ground Check
		if(airborn) {
			if (btg.player.getY() > ((yMax) * 64) - 32) {

				System.out.println("Landed!");
				//btg.player.translate(5, 0);
				btg.player.setY(((yMax) * 64) - 32);
				btg.player.setVelocity(new Vector(0f, 0f));
				airborn = false;
			}
		}

		//JUMPING
		if(airborn) {
			btg.player.setVelocity(btg.player.getVelocity().add(btgV.gravity));
		}else {
			if (input.isKeyPressed(Input.KEY_SPACE)) {
				btg.player.setVelocity(btg.player.getVelocity().add(btgV.jump));
				airborn = true;
			}
		}

		//Movement - lots of seemingly random values here... had to adjust for smooth contact against tiles
		//Move Left
		if (input.isKeyDown(Input.KEY_A)) {
			btg.player.translate(-5, 0);
			if(btg.player.getX() < ((xMin)* 64) + 96){
				btg.player.setX((xMin + 1)* 64 + 32);
			}
		}

		//Move Right
		if (input.isKeyDown(Input.KEY_D)) {
			btg.player.translate(5, 0);
			if(btg.player.getX() > ((xMax)* 64) - 32){
				btg.player.setX((xMax)* 64 - 32);
			}
		}

		btg.player.update(delta);












		//Needs to be at bottom of method
		btg.setVariablesFromClient();

		if(btg.variables.currentState!=BrickTagGame.PLAYINGSTATE){
			btg.enterState(btg.variables.currentState);
		}
	}

	@Override
	public int getID() {
		return BrickTagGame.PLAYINGSTATE;
	}
	
}