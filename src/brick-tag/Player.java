import jig.ResourceManager;
import jig.Entity;
import jig.Vector;
import org.lwjgl.Sys;


/**
 * The Ball class is an Entity that has a velocity (since it's moving). When
 * the Ball bounces off a surface, it temporarily displays a image with
 * cracks for a nice visual effect.
 *
 */
class Player extends Entity {
	private PlayerVariables variables;
	private Tile[][] tileGrid;

	public Player(final float x, final float y, final float vx, final float vy) {
		super(x, y);
		addImageWithBoundingBox(ResourceManager.getImage(BrickTagGame.PLAYER_RSC));
		this.variables = new PlayerVariables(x, y, vx, vy);
	}

	/*

	public void bounce(float surfaceTangent) {
		removeImage(ResourceManager.getImage(BounceGame.BALL_BALLIMG_RSC));
		addImageWithBoundingBox(ResourceManager
				.getImage(BounceGame.BALL_BROKENIMG_RSC));
		countdown = 500;
		velocity = velocity.bounce(surfaceTangent);
	}

	*/

	public void setTileGrid(Tile[][] tileGrid) {
		this.tileGrid = tileGrid;
	}

	public void setVariables(PlayerVariables variables) {
		this.variables = variables;
	}

	public PlayerVariables getVariables() {
		return variables;
	}

	public void update(final int delta) {
//		setPosition(variables.getX(),variables.getY());

//		// "infinite" value
//		int xMax = 99;
//		int yMax = 99;
//		int xMin = -99;
//		int yMin = -99; //used for checking "head bonks" on block above. not implemented yet
//
//		//get player position in tile grid
//		int playerX = (int) Math.floor(this.getX() / 64);
//		int playerY = (int) Math.floor(this.getY() / 64);
//
//
//		//East
//		if (this.tileGrid[playerX + 1][playerY].designation != 0) {
//			xMax = playerX + 1;
//		}
//
//		//West
//		if (this.tileGrid[playerX - 1][playerY].designation != 0) {
//			xMin = playerX - 1;
//		}
//
//		//South
//		if (this.tileGrid[playerX][playerY + 1].designation != 0) {
//			yMax = playerY + 1;
//		} else {
//			this.variables.setAirborne(true);
//		}
//
//		//Ground Check
//		if (this.variables.isAirborne()) {
//			if (this.getY() > ((yMax) * 64) - 32) {
////				System.out.println("Landed!");
//				this.setY(((yMax) * 64) - 32);
//				this.variables.setAirborne(false);
//			}
//		}
//
//		if (this.getX() < ((xMin) * 64) + 96) {
//			this.setX((xMin + 1) * 64 + 32);
//		}
//
//		if (this.getX() > ((xMax) * 64) - 32) {
//			this.setX((xMax) * 64 - 32);
//		}

		translate(variables.getVelocity().scale(delta));
	}

//		// "infinite" value
//		int xMax = 99;
//		int yMax = 99;
//		int xMin = -99;
//		int yMin = -99;
//		int playerX = (int)Math.floor(this.getX() / 64);
//		int playerY = (int)Math.floor(this.getY() / 64);
//		if( btgV.tileGrid[playerX + 1][playerY].designation != 0){ xMax = playerX + 1; }
//		if( btgV.tileGrid[playerX - 1][playerY].designation != 0){ xMin = playerX - 1; }
//		if( btgV.tileGrid[playerX][playerY + 1].designation != 0){
//			yMax = playerY + 1;
//		}else{
//			airborn = true;
//		}
//		if(airborn) {
//			if (this.getY() > ((yMax) * 64) - 32) {
//				this.setY(((yMax) * 64) - 32);
//				airborn = false;
//			}
//		}
//		if(this.getX() < ((xMin)* 64) + 96){
//			this.setX((xMin + 1)* 64 + 32);
//		}
//		if(this.getX() > ((xMax)* 64) - 32){
//			this.setX((xMax)* 64 - 32);
//		}
//
//		this.variables.setVariableX(this.getX());
//		this.variables.setVariableY(this.getY());
//
//	}
//
//
//	{
//		// "infinite" value
//		int xMax = 99;
//		int yMax = 99;
//		int xMin = -99;
//		int yMin = -99;
//		int playerX = (int)Math.floor(this.getX() / 64);
//		int playerY = (int)Math.floor(this.getY() / 64);
//		if( btgV.tileGrid[playerX + 1][playerY].designation != 0){ xMax = playerX + 1; }
//		if( btgV.tileGrid[playerX - 1][playerY].designation != 0){ xMin = playerX - 1; }
//		if( btgV.tileGrid[playerX][playerY + 1].designation != 0){
//			yMax = playerY + 1;
//		}else{
//			airborn = true;
//		}
//		if(airborn) {
//			if (this.getY() > ((yMax) * 64) - 32) {
//				this.setY(((yMax) * 64) - 32);
//				airborn = false;
//			}
//		}
//		if(this.getX() < ((xMin)* 64) + 96){
//			this.setX((xMin + 1)* 64 + 32);
//		}
//		if(this.getX() > ((xMax)* 64) - 32){
//			this.setX((xMax)* 64 - 32);
//		}
//	}

}

