import jig.ResourceManager;
import jig.Entity;
import jig.Vector;
import org.lwjgl.Sys;
import org.newdawn.slick.Animation;

class Player extends Entity {
	private PlayerVariables variables;
	private Tile[][] tileGrid;
	private int index;
	private float worldX;
	private float worldY;
	private float screenX;
	private float screenY;

	private Animation player;

	public Player(final float x, final float y, final float vx, final float vy, int playerid, String orientation) {
		super(x, y);

		if((playerid == 0) && (orientation.equals("RL"))) {
			player = new Animation(ResourceManager.getSpriteSheet(BrickTagGame.RED_RL_RSC, 64, 64), 0, 0, 5, 0, true, 50, true);
		}
		if((playerid == 0) && (orientation.equals("RR"))) {
			player = new Animation(ResourceManager.getSpriteSheet(BrickTagGame.RED_RR_RSC, 64, 64), 0, 0, 5, 0, true, 50, true);
		}
		if((playerid == 1) && (orientation.equals("RL"))) {
			player = new Animation(ResourceManager.getSpriteSheet(BrickTagGame.GREEN_RL_RSC, 64, 64), 0, 0, 5, 0, true, 50, true);
		}
		if((playerid == 1) && (orientation.equals("RR"))) {
			player = new Animation(ResourceManager.getSpriteSheet(BrickTagGame.GREEN_RR_RSC, 64, 64), 0, 0, 5, 0, true, 50, true);
		}

		addAnimation(player);
		player.setLooping(true);

		//addImageWithBoundingBox(ResourceManager.getImage(BrickTagGame.PLAYER_RSC));
		this.variables = new PlayerVariables(x, y, vx, vy);
	}

	public void setWorldPosition(float worldX,float worldY) {
		this.worldX = worldX;
		this.worldY = worldY;
	}

	public void setWorldPosition() {
		this.worldX = this.variables.getX();
		this.worldY = this.variables.getY();
	}

	public void setScreenPosition(float screenX,float screenY) {
		this.screenX = screenX;
		this.screenY = screenY;
	}

	public void setScreenPosition() {
		this.screenX = this.variables.x_SC;
		this.screenY = this.variables.y_SC;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
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

	public float getScreenY() {
		return screenY;
	}

	public float getScreenX() {
		return screenX;
	}

	public float getWorldX() {
		return worldX;
	}

	public float getWorldY() {
		return worldY;
	}
}

