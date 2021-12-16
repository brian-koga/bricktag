import jig.ResourceManager;
import org.lwjgl.Sys;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

import java.io.IOException;

/**
 * This state is active prior to the Game starting.
 *
 *
 * Transitions From (Initialization), GameOverState
 *
 * Transitions To PlayingState
 */

class StartUpState extends BasicGameState {
	int menuItem = 0;
	int levelMenuItem = 1;

	@Override
	public void init(GameContainer container, StateBasedGame game)
		throws SlickException {
	}

	@Override
	public void enter(GameContainer container, StateBasedGame game) {
		container.setSoundOn(false);
		BrickTagGame btg = (BrickTagGame) game;
	}

	@Override
	public void render(GameContainer container, StateBasedGame game,Graphics g) throws SlickException {
		BrickTagGame btg = (BrickTagGame) game;
		BrickTagGameVariables btgV = btg.variables;
		g.drawImage(ResourceManager.getImage(BrickTagGame.STARTUP_SCREEN_RSC),0,0);

		for(int i=0;i<btg.allPlayers.size()/4;i++) {
//			System.out.println(i);
			if (i >= 0 && btgV.playerList.get(0).isLoggedIn) {
				g.drawImage(ResourceManager.getImage(BrickTagGame.BLUE_MINI_RSC), 505, 580);
			}
			if (i >= 1 && btgV.playerList.get(0).isLoggedIn) {
				g.drawImage(ResourceManager.getImage(BrickTagGame.GREEN_MINI_RSC), 525, 580);
			}
			if (i >= 2 && btgV.playerList.get(0).isLoggedIn) {
				g.drawImage(ResourceManager.getImage(BrickTagGame.RED_MINI_RSC), 545, 580);
			}
			if (i >= 3 && btgV.playerList.get(0).isLoggedIn) {
				g.drawImage(ResourceManager.getImage(BrickTagGame.YELLOW_MINI_RSC), 565, 580);
			}
		}
//		System.out.println();
	}

	@Override
	public void update(GameContainer container, StateBasedGame game, int delta) throws SlickException {
		Input input = container.getInput();
		BrickTagGame btg = (BrickTagGame) game;

		KeyboardCommand kc = new KeyboardCommand();
		kc.index = btg.player.getIndex();

		if(input.isKeyPressed(Input.KEY_SPACE)) {
			kc.command = "SPACE";
		}else {
			kc.command = "";
		}

		PlayingState.sendKeyboardCommands(kc,btg);

		//Needs to be at bottom of method
		btg.setVariablesFromClient();

		if(btg.variables.currentState!=BrickTagGame.STARTUPSTATE){
			btg.enterState(btg.variables.currentState);
		}
	}

	@Override
	public int getID() {
		return BrickTagGame.STARTUPSTATE;
	}
}
