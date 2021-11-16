import org.lwjgl.Sys;
import java.io.*;
import java.net.*;
import java.util.*;

@SuppressWarnings("InfiniteLoopStatement")
public class Server {
	static Vector<ClientHandler> playerList = new Vector<>();
	static int numberOfActivePlayers = 0;

	public static void main(String[] args) throws IOException {
		//Start a new server listening on port 5000
		ServerSocket server = new ServerSocket(5000);
		Socket socket;

		while(true){
			try{
				//accepts a new client
				socket = server.accept();
				System.out.println("NEW CLIENT");

				DataInputStream input = new DataInputStream(socket.getInputStream());
				DataOutputStream output = new DataOutputStream(socket.getOutputStream());

				//Assigning a new thread for the current client
				ClientHandler clientHandler = new ClientHandler(socket,input,output);
				Thread thread = new Thread(clientHandler);
				playerList.add(clientHandler);
				thread.start();
				numberOfActivePlayers++;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}

class ClientHandler implements Runnable{
	final DataInputStream inputStream;
	final DataOutputStream outputStream;
	final Socket socket;

	ClientHandler(Socket socket, DataInputStream inputStream, DataOutputStream outputStream) {
		this.socket = socket;
		this.inputStream = inputStream;
		this.outputStream = outputStream;
	}

	@Override
	public void run(){
		String received;
		while (true){
			try {
				received = inputStream.readUTF();
//				System.out.println(received);

				if(received.equals("logout")){
					Server.numberOfActivePlayers--;
					this.socket.close();
					break;
				}

			} catch (IOException ignored) {}
		}
		try {
			this.inputStream.close();
			this.outputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
