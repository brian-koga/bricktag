import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

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
	public void render(GameContainer container, StateBasedGame game,
			Graphics g) throws SlickException {

		BrickTagGame btg = (BrickTagGame) game;
		BrickTagGameVariables btgV = btg.variables;

		g.drawString("< Start >", btgV.ScreenWidth/2 -30, btgV.ScreenHeight/2 -20);
	}

	@Override
	public void update(GameContainer container, StateBasedGame game,
			int delta) throws SlickException {

		Input input = container.getInput();
		BrickTagGame btg = (BrickTagGame) game;

		if(input.isKeyPressed(Input.KEY_SPACE)) {
			btg.client.sendString("SPACE");
		}else{
			btg.client.sendString("");
		}

		btg.client.checkIfNeedToGetNewGameState();

		//Needs to be at bottom of method
		btg.variables = btg.client.brickTagGameVariables;
		System.out.println(btg.client.brickTagGameVariables.currentState);

		if(btg.variables.currentState!=BrickTagGame.STARTUPSTATE){
			btg.enterState(btg.variables.currentState);
		}

	}

	@Override
	public int getID() {
		return BrickTagGame.STARTUPSTATE;
	}
	
}