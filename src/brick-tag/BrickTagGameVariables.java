import java.io.Serializable;
import java.util.ArrayList;
import java.util.Vector;

public class BrickTagGameVariables implements Serializable {
	public final float ScreenWidth;
	public final float ScreenHeight;

	// This will be the size of the world in pixels
	public final float WorldWidth;
	public final float WorldHeight;

	// size of tile in pixels
	float tileSize;

	// This will be the size of the world in tiles
	public final int WorldTileWidth;
	public final int WorldTileHeight;

	// This will be the size of the screen in tiles
	public final int ScreenTileWidth;
	public final int ScreenTileHeight;

	// what level it is (numbers for now, could change)
	int level;

	// holds the world, each is a tile object
	//Tile[][] tileGrid;
	int currentState;
	boolean showGrid;

	// gravity vectors
	float gravityValue;
	float jumpValue;

	int updateCount;

	ArrayList<PlayerVariables> playerList;
	Vector<Tile> placedTiles;

	public BrickTagGameVariables(int height,int width) {
		ScreenHeight = (float) height;
		ScreenWidth = (float) width;

		this.ScreenTileWidth = 20;
		this.ScreenTileHeight = 11;


		this.WorldTileWidth = ScreenTileWidth*3;
		this.WorldTileHeight = ScreenTileHeight*2;

		// do we want this to change based on the level?
		WorldWidth = ScreenWidth*3;
		WorldHeight = ScreenHeight*2;


		//tileGrid = new Tile[WorldTileWidth][WorldTileHeight];

		// adjust these values to change jumping behavior
		// .4 & -13.0 work quite nice
		gravityValue = .4f;
		jumpValue = -13.0f;


		this.currentState = BrickTagGame.STARTUPSTATE;
		this.tileSize = 64;
		this.level = 1;
		this.showGrid = false;
		this.playerList = new ArrayList<>(4);
		//setTileGrid();
		this.updateCount = 0;
		this.placedTiles = new Vector<>();
	}

	public void setPv(PlayerVariables pv,int index) {
		this.playerList.set(index,pv);
	}

	public void setLevel(int level) {this.level = level;}

	public void setCurrentState(int currentState) {this.currentState = currentState;}

	public void incrementUpdateCount() {this.updateCount++;}
	public int getUpdateCount() {return this.updateCount;}

	public void toggleShowGrid() {this.showGrid = !this.showGrid;}

	/*
	public void setTileGrid() {
		String path = getMapFile(this.level);
		tileGrid = PlayingState.setupLevel(this,path);
	}

	 */

	private String getMapFile(int levelNumber){
		if(levelNumber == 1) {
			return "Brick-Tag/src/brick-tag/resource/Level1.txt";
		} else if(levelNumber == 2) {
			return  "Brick-Tag/src/brick-tag/resource/Level2.txt";
		}
		else{
			return null;
		}
	}
}
