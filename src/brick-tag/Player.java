import jig.ResourceManager;
import jig.Vector;
import jig.Entity;


/**
 * The Ball class is an Entity that has a velocity (since it's moving). When
 * the Ball bounces off a surface, it temporarily displays a image with
 * cracks for a nice visual effect.
 *
 */
class Player extends Entity {

    private Vector velocity;
    private int countdown;

    public Player(final float x, final float y, final float vx, final float vy) {
        super(x, y);
        addImageWithBoundingBox(ResourceManager.getImage(BrickTagGame.PLAYER_RSC));
        velocity = new Vector(vx, vy);
        countdown = 0;
    }

    public void setVelocity(final Vector v) {
        velocity = v;
    }

    public Vector getVelocity() {
        return velocity;
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




    public void update(final int delta) {
        translate(velocity.scale(delta));
        if (countdown > 0) {
            countdown -= delta;
            if (countdown <= 0) {
                addImageWithBoundingBox(ResourceManager.getImage(BrickTagGame.PLAYER_RSC));
                removeImage(ResourceManager.getImage(BrickTagGame.PLAYER_RSC));
            }
        }
    }
}

