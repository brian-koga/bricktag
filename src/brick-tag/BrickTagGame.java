import jig.ResourceManager;
import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.StateBasedGame;

public class BrickTagGame extends StateBasedGame {

	public static final int STARTUPSTATE = 0;
	public static final int PLAYINGSTATE = 1;
	public static final int GAMEOVERSTATE = 2;

	public static final String Block_RSC = "resource/red_outlet_tile_64px.png";

	// These values are floats to make things easier later?

	// This will be the size of the display screen in pixels
	public final float ScreenWidth;
	public final float ScreenHeight;

	// This will be the size of the world in pixels
	public final float WorldWidth;
	public final float WorldHeight;
	// size of tile in pixels
	float tileSize = 64;

	// This will be the size of the world in tiles (set for the screen size for now)
	public final int WorldTileWidth = 20;
	public final int WorldTileHeight = 11;

	// what level it is (numbers for now, could change)
	int level = 1;

	// holds the world, each is a tile object
	Tile[][] tileGrid;


	boolean showGrid = true;
	Client client = new Client();


	public BrickTagGame(String name, int width, int height) {
		super(name);
		ScreenHeight = (float)height;
		ScreenWidth = (float)width;

		// For now set these to be the same, when scrolling is added, the world size will be fixed as a
		// class variable, or change based on the level, in which case these won't be finals
		WorldHeight = ScreenHeight;
		WorldWidth = ScreenWidth;

		tileGrid = new Tile[WorldTileWidth][WorldTileHeight];
	}

	@Override
	public void initStatesList(GameContainer gameContainer) throws SlickException {
		addState(new StartUpState());
		addState(new GameOverState());
		addState(new PlayingState());
		//addState(new LevelOverState());

		// preload all the resources to avoid warnings & minimize latency...
		ResourceManager.loadImage(Block_RSC);

	}

	public static void main(String[] args){
		AppGameContainer app;
		try {
			app = new AppGameContainer(new BrickTagGame("Brick Tag!", 1280, 720));
			app.setDisplayMode(1280, 720, false);
			app.setVSync(true);
			app.setTargetFrameRate(60);
			app.start();
		} catch (SlickException e) {
			e.printStackTrace();
		}
	}
}
