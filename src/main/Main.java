package main;

import java.util.Scanner;
import javax.websocket.DeploymentException;
import org.glassfish.tyrus.server.Server;

import server.Client;

public class Main {
	
	public static void main(String[] args) throws DeploymentException {

		final Server server = new Server("localhost", 1969, null, null, Client.class);
		server.start();

		try(Scanner scanner = new Scanner(System.in))
		{
			Boolean done = false;
			
			while (!done)
			{
				if (scanner.hasNextLine())
				{
					final String commandLine = scanner.nextLine();
					done = commandLine.equalsIgnoreCase("quit");
				}
			}
		}

		
		server.stop();
	}
}