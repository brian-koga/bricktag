import java.io.Serializable;

public class BrickTagGameVariables implements Serializable {
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

	int currentState;


	boolean showGrid = true;
	public BrickTagGameVariables(int height,int width){
		ScreenHeight = (float)height;
		ScreenWidth = (float)width;

		// For now set these to be the same, when scrolling is added, the world size will be fixed as a
		// class variable, or change based on the level, in which case these won't be finals
		WorldHeight = ScreenHeight;
		WorldWidth = ScreenWidth;

		tileGrid = new Tile[WorldTileWidth][WorldTileHeight];
		this.currentState = BrickTagGame.STARTUPSTATE;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public void setCurrentState(int currentState) {
		this.currentState = currentState;
	}
}
