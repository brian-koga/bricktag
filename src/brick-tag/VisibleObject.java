import java.io.Serializable;

public class VisibleObject implements Serializable {

    final float x;
    final float y;
    final char objectType;

    public VisibleObject(final float x, final float y, final char objectType) {
        this.x = x;
        this.y = y;
        this.objectType = objectType;
    }
}
