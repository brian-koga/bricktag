import jig.ResourceManager;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;
import org.newdawn.slick.state.transition.EmptyTransition;
import org.newdawn.slick.state.transition.HorizontalSplitTransition;


/**
 * This state is active when the Game is over.
 * 
 * Transitions From PlayingState
 * 
 * Transitions To StartUpState, PLayingState
 */
class GameOverState extends BasicGameState {

	private int whoWon;
	private int countdown;
	private String[] players;

	@Override
	public void init(GameContainer container, StateBasedGame game)
			throws SlickException {
	}
	
	@Override
	public void enter(GameContainer container, StateBasedGame game) {
		BrickTagGame btg = (BrickTagGame) game;
		BrickTagGameVariables btgV = btg.variables;
		this.whoWon = btgV.scoreList.indexOf(25);
		this.countdown = 10000;
		players = new String[]{"Blue", "Green", "Red", "Yellow"};
		System.out.println("GAME OVER");
	}
	
	@Override
	public void render(GameContainer container, StateBasedGame game,
			Graphics g) throws SlickException {

		BrickTagGame btg = (BrickTagGame) game;
		BrickTagGameVariables btgV = btg.variables;

		g.drawString(players[whoWon] +" Player Wins", btgV.ScreenWidth/2 -30, btgV.ScreenHeight/2 -20);

	}

	@Override
	public void update(GameContainer container, StateBasedGame game,
			int delta) throws SlickException {
		BrickTagGame btg = (BrickTagGame) game;

		KeyboardCommand kc = new KeyboardCommand();
		kc.index = btg.player.getIndex();
		kc.command = "END";

		if(this.countdown <=0){
			kc.command = "GO_START";
		}
		PlayingState.sendKeyboardCommands(kc,btg);
		this.countdown-=delta;

		btg.setVariablesFromClient();

		if(btg.variables.currentState!=BrickTagGame.GAMEOVERSTATE){
			btg.enterState(btg.variables.currentState);
		}
	}

	@Override
	public int getID() {
		return BrickTagGame.GAMEOVERSTATE;
	}
	
}