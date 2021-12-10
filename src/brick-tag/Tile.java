import java.io.Serializable;
import java.util.ArrayList;

public class Tile  implements Serializable {
	int x;
	int y;
	int designation;

	ArrayList<Tile> validNeighbors;
	int d;
	boolean valid;

	public Tile(int x, int y, int designation, boolean valid) {
		this.x = x;
		this.y = y;
		this.designation = designation;
		this.valid = valid;
	}

	public void setNeighbors(ArrayList<Tile> validNeighbors) {
		this.validNeighbors = validNeighbors;
	}

	public void setD(int d) {
		this.d = d;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}
}
