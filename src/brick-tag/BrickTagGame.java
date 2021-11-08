import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.StateBasedGame;

public class BrickTagGame extends StateBasedGame {

	public final int ScreenWidth;
	public final int ScreenHeight;

	public BrickTagGame(String name, int width, int height) {
		super(name);
		ScreenHeight = height;
		ScreenWidth = width;
	}

	@Override
	public void initStatesList(GameContainer gameContainer) throws SlickException {

	}

	public static void main(String[] args){
		AppGameContainer app;
		try {
			app = new AppGameContainer(new BrickTagGame("Brick Tag!", 1280, 720));
			app.setDisplayMode(1280, 720, false);
			app.setVSync(true);
			app.start();
		} catch (SlickException e) {
			e.printStackTrace();
		}
	}
}
