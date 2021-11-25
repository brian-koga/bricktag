import jig.Entity;
import jig.ResourceManager;
import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.StateBasedGame;


import java.io.Serializable;

public class BrickTagGame extends StateBasedGame implements Serializable {

	public static final int STARTUPSTATE = 0;
	public static final int PLAYINGSTATE = 1;
	public static final int GAMEOVERSTATE = 2;

	public static final String Block_RSC = "resource/red_outlet_tile_64px.png";
	//public static final String PLAYER_RSC = "resource/player.png";
	public static final String PLAYER_RSC = "resource/red_outlet_tile_64px.png";

	BrickTagGameVariables variables;
	Client client;
	Player player;

	public BrickTagGame(String name, int width, int height) {
		super(name);
		Entity.setCoarseGrainedCollisionBoundary(Entity.AABB);
		this.variables = new BrickTagGameVariables(height,width);
	}

	public void setVariablesFromClient() {
		this.client.checkIfNeedToGetNewGameState();
		this.variables = this.client.brickTagGameVariables;
		this.player.setVariables(this.client.brickTagGameVariables.playerVariables);
	}

	@Override
	public void initStatesList(GameContainer gameContainer) {
		addState(new StartUpState());
		addState(new GameOverState());
		addState(new PlayingState());

		// preload all the resources to avoid warnings & minimize latency...
		ResourceManager.loadImage(Block_RSC);
		ResourceManager.loadImage(PLAYER_RSC);

		player = new Player(640, 352, 0, 0);
	}

	public static void main(String[] args){
		AppGameContainer app;
		try {
			BrickTagGame btg = new BrickTagGame("Brick Tag!", 1280, 720);
			btg.client = new Client(btg.variables);
			app = new AppGameContainer(btg);
			app.setDisplayMode(1280, 720, false);
			app.setVSync(true);
			app.setTargetFrameRate(60);
			app.start();
		} catch (SlickException e) {
			e.printStackTrace();
		}
	}
}
