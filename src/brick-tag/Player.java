import jig.ResourceManager;
import jig.Entity;
import jig.Vector;
import org.lwjgl.Sys;

class Player extends Entity {
	private PlayerVariables variables;
	private Tile[][] tileGrid;

	public Player(final float x, final float y, final float vx, final float vy) {
		super(x, y);
		addImageWithBoundingBox(ResourceManager.getImage(BrickTagGame.PLAYER_RSC));
		this.variables = new PlayerVariables(x, y, vx, vy);
	}


	public void setTileGrid(Tile[][] tileGrid) {
		this.tileGrid = tileGrid;
	}

	public void setVariables(PlayerVariables variables) {
		this.variables = variables;
	}

	public PlayerVariables getVariables() {
		return variables;
	}

}

