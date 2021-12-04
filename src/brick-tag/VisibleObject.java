import java.io.Serializable;
import java.util.ArrayList;

public class VisibleObject implements Serializable {

    float x;
    float y;
    final char objectType;
	int playersIndexOnScreen;

    public VisibleObject(final float x, final float y, final char objectType) {
        this.x = x;
        this.y = y;
        this.objectType = objectType;
    }

	//This is for rendering players
	public VisibleObject(int playersIndexOnScreen, final char objectType){
		this.playersIndexOnScreen = playersIndexOnScreen;
		this.objectType = objectType;
	}
}
