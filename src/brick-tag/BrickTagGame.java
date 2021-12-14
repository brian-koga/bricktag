import jig.Entity;
import jig.ResourceManager;
import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.StateBasedGame;


import java.io.Serializable;
import java.util.ArrayList;

public class BrickTagGame extends StateBasedGame implements Serializable {

	public static final int STARTUPSTATE = 0;
	public static final int PLAYINGSTATE = 1;
	public static final int GAMEOVERSTATE = 2;

	public static final String Block_RSC = "resource/red_outlet_tile_64px.png";
	public static final String PLAYER_RSC = "resource/player.png";
	public static final String FLAG_RSC = "resource/flag.png";
	public static final String FLAG_MINI_RSC = "resource/flag_mini.png";

	public static final String BLUE_GLASS_RSC = "resource/BLUE/blue_glass.png";
	public static final String BLUE_RL_RSC = "resource/BLUE/blue_running_left.png";
	public static final String BLUE_RR_RSC = "resource/BLUE/blue_running_right.png";
	public static final String BLUE_SL_RSC = "resource/BLUE/blue_standing_left.png";
	public static final String BLUE_SR_RSC = "resource/BLUE/blue_standing_right.png";
	public static final String BLUE_MINI_RSC = "resource/BLUE/blue_mini.png";

	public static final String GREEN_GLASS_RSC = "resource/GREEN/green_glass.png";
	public static final String GREEN_RL_RSC = "resource/GREEN/green_running_left.png";
	public static final String GREEN_RR_RSC = "resource/GREEN/green_running_right.png";
	public static final String GREEN_SL_RSC = "resource/GREEN/green_standing_left.png";
	public static final String GREEN_SR_RSC = "resource/GREEN/green_standing_right.png";
	public static final String GREEN_MINI_RSC = "resource/GREEN/green_mini.png";

	public static final String RED_GLASS_RSC = "resource/RED/red_glass.png";
	public static final String RED_RL_RSC = "resource/RED/red_running_left.png";
	public static final String RED_RR_RSC = "resource/RED/red_running_right.png";
	public static final String RED_SL_RSC = "resource/RED/red_standing_left.png";
	public static final String RED_SR_RSC = "resource/RED/red_standing_right.png";
	public static final String RED_MINI_RSC = "resource/RED/red_mini.png";

	public static final String YELLOW_GLASS_RSC = "resource/YELLOW/yellow_glass.png";
	public static final String YELLOW_RL_RSC = "resource/YELLOW/yellow_running_left.png";
	public static final String YELLOW_RR_RSC = "resource/YELLOW/yellow_running_right.png";
	public static final String YELLOW_SL_RSC = "resource/YELLOW/yellow_standing_left.png";
	public static final String YELLOW_SR_RSC = "resource/YELLOW/yellow_standing_right.png";
	public static final String YELLOW_MINI_RSC = "resource/YELLOW/yellow_mini.png";



	BrickTagGameVariables variables;
	Client client;
	Player player;
	ArrayList<Player> allPlayers;
	Tile[][] tileGrid;

	public BrickTagGame(String name, int width, int height) {
		super(name);
		Entity.setCoarseGrainedCollisionBoundary(Entity.AABB);
		this.variables = new BrickTagGameVariables(height,width);
		allPlayers = new ArrayList<>(16);
	}

	public void setVariablesFromClient() {
		this.client.checkIfNeedToGetNewGameState();
		this.variables = this.client.brickTagGameVariables;
		for(int i = 0;i<this.client.brickTagGameVariables.playerList.size();i++){
			PlayerVariables newPV = this.client.brickTagGameVariables.playerList.get(i);
			if(i > ((this.allPlayers.size() / 4) - 1)){
				//System.out.println("PLAYER!" + i);
				this.allPlayers.add(new Player(0,0,0,0, i, "RL"));
				this.allPlayers.add(new Player(0,0,0,0, i, "RR"));
				this.allPlayers.add(new Player(0,0,0,0, i, "SL"));
				this.allPlayers.add(new Player(0,0,0,0, i, "SR"));
			}
			this.allPlayers.get(i).setVariables(newPV);
		}
	}

	@Override
	public void initStatesList(GameContainer gameContainer) {
		addState(new StartUpState());
		addState(new GameOverState());
		addState(new PlayingState());

		// preload all the resources to avoid warnings & minimize latency...
		ResourceManager.loadImage(Block_RSC);
		ResourceManager.loadImage(PLAYER_RSC);
		ResourceManager.loadImage(FLAG_RSC);
		ResourceManager.loadImage(FLAG_MINI_RSC);

		ResourceManager.loadImage(BLUE_GLASS_RSC);
		ResourceManager.loadImage(BLUE_RL_RSC);
		ResourceManager.loadImage(BLUE_RR_RSC);
		ResourceManager.loadImage(BLUE_SL_RSC);
		ResourceManager.loadImage(BLUE_SR_RSC);
		ResourceManager.loadImage(BLUE_MINI_RSC);

		ResourceManager.loadImage(GREEN_GLASS_RSC);
		ResourceManager.loadImage(GREEN_RL_RSC);
		ResourceManager.loadImage(GREEN_RR_RSC);
		ResourceManager.loadImage(GREEN_SL_RSC);
		ResourceManager.loadImage(GREEN_SR_RSC);
		ResourceManager.loadImage(GREEN_MINI_RSC);

		ResourceManager.loadImage(RED_GLASS_RSC);
		ResourceManager.loadImage(RED_RL_RSC);
		ResourceManager.loadImage(RED_RR_RSC);
		ResourceManager.loadImage(RED_SL_RSC);
		ResourceManager.loadImage(RED_SR_RSC);
		ResourceManager.loadImage(RED_MINI_RSC);

		ResourceManager.loadImage(YELLOW_GLASS_RSC);
		ResourceManager.loadImage(YELLOW_RL_RSC);
		ResourceManager.loadImage(YELLOW_RR_RSC);
		ResourceManager.loadImage(YELLOW_SL_RSC);
		ResourceManager.loadImage(YELLOW_SR_RSC);
		ResourceManager.loadImage(YELLOW_MINI_RSC);

		//creates player, position here is kinda irrelevant as its changed instantly.
		player = new Player(240, 352, 0, 0, 1, "RL");

		int index = this.client.receiveIndex();
		if(index>3){
			System.out.println("The lobby is currently full please try again later");
			System.exit(0);
		}else if(index!=-1) {
			this.player.setIndex(index);
		}
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
