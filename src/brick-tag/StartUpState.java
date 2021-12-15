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
//		int index = btg.client.receiveIndex();
//		if(index>3){
//			System.out.println("The lobby is currently full please try again later");
//			System.exit(0);
//		}else if(index!=-1) {
//			btg.player.setIndex(index);
//		}
	}

	@Override
	public void render(GameContainer container, StateBasedGame game,Graphics g) throws SlickException {
		BrickTagGame btg = (BrickTagGame) game;
		BrickTagGameVariables btgV = btg.variables;

		g.drawString("< Start >", btgV.ScreenWidth/2 -30, btgV.ScreenHeight/2 -20);
	}

	@Override
	public void update(GameContainer container, StateBasedGame game, int delta) throws SlickException {
		Input input = container.getInput();
		BrickTagGame btg = (BrickTagGame) game;

		KeyboardCommand kc = new KeyboardCommand();
		kc.index = btg.player.getIndex();

		if(input.isKeyPressed(Input.KEY_SPACE)) {
			kc.command = "SPACE";
		} else {
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
