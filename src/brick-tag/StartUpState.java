import jig.ResourceManager;
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

		//g.drawString("Brian Koga", 10, mg.ScreenHeight -20);

		g.drawString("< Start >", btg.ScreenWidth/2 -30, btg.ScreenHeight/2 -20);
	}

	@Override
	public void update(GameContainer container, StateBasedGame game,
			int delta) throws SlickException {

		Input input = container.getInput();
		BrickTagGame btg = (BrickTagGame) game;

		if(input.isKeyPressed(Input.KEY_SPACE)) {
			btg.enterState(BrickTagGame.PLAYINGSTATE);
		}
	}

	@Override
	public int getID() {
		return BrickTagGame.STARTUPSTATE;
	}
	
}