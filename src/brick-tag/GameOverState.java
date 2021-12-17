import jig.ResourceManager;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;


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

	@Override
	public void init(GameContainer container, StateBasedGame game) {
	}
	
	@Override
	public void enter(GameContainer container, StateBasedGame game) {
		BrickTagGame btg = (BrickTagGame) game;
		BrickTagGameVariables btgV = btg.variables;
		this.whoWon = btgV.scoreList.indexOf(25);
		this.countdown = 10000;
	}
	
	@Override
	public void render(GameContainer container, StateBasedGame game,Graphics g) {
		g.drawImage(ResourceManager.getImage(BrickTagGame.GAMEOVER_SCREEN_RSC),0,0);
		String winner = getWinnerImage();
		g.drawImage(ResourceManager.getImage(winner).getScaledCopy(2),750,485);
	}

	public String getWinnerImage(){
		if(whoWon == 0){
			return BrickTagGame.BLUE_MINI_RSC;
		}else if(whoWon == 1){
			return BrickTagGame.GREEN_MINI_RSC;
		}else if(whoWon==2){
			return BrickTagGame.RED_MINI_RSC;
		}else if(whoWon==3){
			return BrickTagGame.YELLOW_MINI_RSC;
		}
		return "";
	}

	@Override
	public void update(GameContainer container, StateBasedGame game,
			int delta) {
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