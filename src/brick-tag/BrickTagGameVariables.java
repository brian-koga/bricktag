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

	// misc variables
	ArrayList<PlayerVariables> playerList;
	ArrayList<Integer> scoreList;
	Vector<Tile> placedTiles;
	Vector<Tile> powerUpTiles;

	String p1_orientation;
	String p2_orientation;
	String p3_orientation;
	String p4_orientation;

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
		this.scoreList = new ArrayList<>(4);
		this.placedTiles = new Vector<>();
		this.powerUpTiles = new Vector<>();

		// populate powerUpTiles
		this.powerUpTiles.add(new Tile(9, 12, 21, true));
		this.powerUpTiles.add(new Tile(54, 1, 21, true));
		this.powerUpTiles.add(new Tile(55, 19, 21, true));

		//SL = standing left. SL SR RL RR
		this.p1_orientation = "SL";
		this.p2_orientation = "SL";
		this.p3_orientation = "SL";
		this.p4_orientation = "SL";
	}

	public void setPv(PlayerVariables pv,int index) {
		this.playerList.set(index,pv);
	}

	public void setOrientation(int index, String orientation) {
		if(index == 0){ this.p1_orientation = orientation; }
		if(index == 1){ this.p2_orientation = orientation; }
		if(index == 2){ this.p3_orientation = orientation; }
		if(index == 3){ this.p4_orientation = orientation; }
	}

	public String getOrientation(int index) {
		if(index == 0){ return this.p1_orientation; }
		if(index == 1){ return this.p2_orientation; }
		if(index == 2){ return this.p3_orientation; }
		if(index == 3){ return this.p4_orientation; }
		return "";
	}

	public void setLevel(int level) {this.level = level;}

	public void setCurrentState(int currentState) {this.currentState = currentState;}

	public void toggleShowGrid() {this.showGrid = !this.showGrid;}
}
