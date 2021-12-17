import jig.Vector;
import org.lwjgl.Sys;

import java.io.Serializable;
import java.util.ArrayList;

public class PlayerVariables implements Serializable {
	private Vector velocity;
	private float x, y, vx, vy;
	private boolean airborne;
	float x_SC, y_SC;
	private boolean flag;
	int score;
	int tempScore;
	private int numberOfBricks;
	boolean isLoggedIn;

	// holds what power-up a player hase
	// 0 : none
	// 1 : speed
	int powerUp = 0;
	int powerUpCountdown = 0;

	ArrayList<VisibleObject> objectsToRender = new ArrayList<>();

	PlayerVariables(final float x, final float y, final float vx, final float vy){
		this.x=x;
		this.y=y;
		this.vx=vx;
		this.vy=vy;
		this.velocity = new Vector(vx, vy);
		this.flag = false;
		this.tempScore = 0;
		this.score = 0;
		this.numberOfBricks = 15;
		this.isLoggedIn = true;
	}

	public void toggleFlag(){
		this.flag = !this.flag;
	}

	public boolean hasFlag(){
		return this.flag;
	}

	public void setVelocity(float x, float y) {
		velocity = new Vector(x,y);
	}

	public Vector getVelocity() {
		return velocity;
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

	public int getScore() {
		return score;
	}

	public void addScore(int score) {
		this.tempScore += score;
		if(tempScore>=100){
			this.score += 1;
			this.tempScore -= 100;
		}
	}

	public void powerUpScore() {
		this.score+=2;
	}

	public int getNumberOfBricks() {
		return numberOfBricks;
	}

	public void addBrick(){
		this.numberOfBricks+=1;
	}

	public void powerUpBrick(){
		this.numberOfBricks+=5;
	}

	public void useBrick(){
		this.numberOfBricks-=1;
	}

	public void translateHelper(float x, float y ,float vx ,float vy){
		// if they have the speed power up, they move 1.5 times faster and jump 1.25 time higher
		if(this.powerUp == 1) {
			this.x += 1.5*x;
			this.y += 1.25*y;
		} else {
			this.x += x;
			this.y += y;
		}
		this.vx += vx;
		this.vy += vy;
	}

	public void givePowerUp(int powerUpType) {
		this.powerUp = powerUpType;
		this.powerUpCountdown = 500;
	}
}
