import java.io.Serializable;
import jig.Vector;

public class BrickTagGameVariables implements Serializable {
	public final float ScreenWidth;
	public final float ScreenHeight;

	// This will be the size of the world in pixels
	public final float WorldWidth;
	public final float WorldHeight;

	// size of tile in pixels
	float tileSize;

	// This will be the size of the world in tiles (set for the screen size for now)
	public final int WorldTileWidth;
	public final int WorldTileHeight;

	// what level it is (numbers for now, could change)
	int level;

	// holds the world, each is a tile object
	Tile[][] tileGrid;
	int currentState;
	boolean showGrid;

	// gravity vectors
	float gravityValue;
	float jumpValue;

	PlayerVariables PV;

	public BrickTagGameVariables(int height,int width) {
		ScreenHeight = (float) height;
		ScreenWidth = (float) width;

		// For now set these to be the same, when scrolling is added, the world size will be fixed as a
		// class variable, or change based on the level, in which case these won't be finals
		WorldHeight = ScreenHeight;
		WorldWidth = ScreenWidth;

		this.WorldTileWidth = 20;
		this.WorldTileHeight = 11;

		tileGrid = new Tile[WorldTileWidth][WorldTileHeight];

		// adjust these values to change jumping behavior
		// .4 & -13.0 work quite nice
		gravityValue = .4f;
		jumpValue = -13.0f;

		this.currentState = BrickTagGame.STARTUPSTATE;
		this.tileSize = 64;
		this.level = 1;
		this.showGrid = true;

	}

	public void setPv(PlayerVariables pv) {this.PV = pv;}

	public void setLevel(int level) {this.level = level;}

	public void setCurrentState(int currentState) {this.currentState = currentState;}

	public void toggleShowGrid() {this.showGrid = !this.showGrid;}
}
