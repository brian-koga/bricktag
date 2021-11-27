import jig.ResourceManager;
import jig.Vector;

import java.io.Serializable;

public class PlayerVariables implements Serializable {

	private Vector velocity;
	private int countdown;
	private float x, y, vx, vy;
	private boolean airborne;
	private int playerX;
	private int playerY;


	PlayerVariables(final float x, final float y, final float vx, final float vy){
		this.x=x;
		this.y=y;
		this.vx=vx;
		this.vy=vy;
		this.velocity = new Vector(vx, vy);
		this.countdown = 0;
	}

	public void setVelocity(final Vector v) {
		velocity = v;
	}

	public void setVelocity(float x, float y) {
		velocity = new Vector(x,y);
	}

	public Vector getVelocity() {
		return velocity;
	}

	public int getCountdown() {
		return countdown;
	}

	public void setCountdown(int countdown) {
		this.countdown = countdown;
	}

	public float getX() {return x;}
	public float getVX() {return vx;}

	public void setVariableX(float x) {
		this.x = x;
	}

	public float getY() {return y;}
	public float getVY() {return vy;}

	public void resetVelocity() {this.vx = 0; this.vy = 0; velocity = new Vector(0,0);}

	public void setVariableY(float y) {
		this.y = y;
	}

	public boolean isAirborne() {
		return airborne;
	}

	public void setAirborne(boolean airborne) {
		this.airborne = airborne;
	}

	public int getPlayerX() {
		setPlayerX();
		return this.playerX;
	}

	public int getPlayerY() {
		setPlayerY();
		return this.playerY;
	}

	private void setPlayerY() {
		this.y = (int)Math.floor(this.getY() / 64);
	}

	public void setPlayerX() {
		this.x = (int)Math.floor(this.getX() / 64);
	}
}
