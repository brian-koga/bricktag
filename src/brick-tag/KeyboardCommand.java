import java.io.Serializable;

public class KeyboardCommand implements Serializable {
	public int index;
	public String command;

	public KeyboardCommand(int index,String command){
		this.index = index;
		this.command = command;
	}

	public KeyboardCommand(){
		index = -1;
		command = "";
	}
}
