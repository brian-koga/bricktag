import jig.ResourceManager;
import jig.Vector;
import org.newdawn.slick.*;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
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
	int playerIndex;

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
			setupLevel(btgV, "Brick-Tag/src/brick-tag/resource/Level1.txt");
		} else if(levelNumber == 2) {
			setupLevel(btgV, "Brick-Tag/src/brick-tag/resource/Level2.txt");
		}

		playerIndex = btg.player.getIndex();

		btg.player.setTileGrid(btgV.tileGrid);
//		sendNewGameState(btg, btgV);

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

	private void sendNewGameState(BrickTagGame btg, BrickTagGameVariables btgV) {
		KeyboardCommand kc = new KeyboardCommand(-1,"NEW");
		sendKeyboardCommands(kc,btg);
		try {
			btg.client.objectOutputStream.reset();
			btg.client.objectOutputStream.writeObject(btgV);
			btg.client.objectOutputStream.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	static void sendKeyboardCommands(KeyboardCommand kc,BrickTagGame btg) {
//		btg.client.sendString("NEW");
		try {
			btg.client.objectOutputStream.reset();
			btg.client.objectOutputStream.writeObject(kc);
			btg.client.objectOutputStream.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void setupLevel(BrickTagGameVariables btgV, String path) {
//		BrickTagGameVariables btgV = btg.variables;
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
//		btg.player.render(g);
//		for(Player p : btg.allPlayers){
//			p.render(g);
//		}
		for(int i = 0; i<btg.allPlayers.size(); i++){
			btg.allPlayers.get(i).render(g);
		}
	}

	@Override
	public void update(GameContainer container, StateBasedGame game,
					   int delta) throws SlickException {

		//System.out.println("update start");
		Input input = container.getInput();
		BrickTagGame btg = (BrickTagGame) game;
		BrickTagGameVariables btgV = btg.variables;
//		PlayerVariables PV = btgV.playerList.get(playerIndex);

//		String index = String.valueOf(this.playerIndex);
//		System.out.println(index);
//		btg.client.sendString(index);

		KeyboardCommand kc = new KeyboardCommand();
		kc.index = playerIndex;

		if (input.isKeyPressed(Input.KEY_0)) {
//			btg.client.sendString("DEBUG");
			kc.command = "DEBUG";
		}
		//Temporary
		else if(input.isKeyPressed(Input.KEY_ESCAPE)){
//			btg.client.sendString("logout");
			kc.command = "logout";
			System.exit(0);
		}else if(input.isKeyPressed(Input.KEY_SPACE)){
//			btg.client.sendString("SPACE");
			kc.command = "SPACE";
		}else if(input.isKeyDown(Input.KEY_A)){
//			btg.client.sendString("A");
			kc.command = "A";
		}else if(input.isKeyDown(Input.KEY_D)){
//			btg.client.sendString("D");
			kc.command = "D";
		}else{
//			btg.client.sendString("");
			kc.command = "";
		}

		sendKeyboardCommands(kc,btg);
		btg.setVariablesFromClient();

		setPlayerPositions(btg, btgV);

		if(btg.variables.currentState!=BrickTagGame.PLAYINGSTATE){ btg.enterState(btg.variables.currentState); }

	}

	private void setPlayerPositions(BrickTagGame btg, BrickTagGameVariables btgV) {
		for(int i = 0; i<btgV.playerList.size(); i++){
			PlayerVariables currPV = btgV.playerList.get(i);
			btg.allPlayers.get(i).setPosition(currPV.getX(), currPV.getY());
		}
	}

	@Override
	public int getID() {
		return BrickTagGame.PLAYINGSTATE;
	}

}