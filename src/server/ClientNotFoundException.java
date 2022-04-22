package server;

public class ClientNotFoundException extends Exception
{
	private static final long serialVersionUID = 1L;

	public ClientNotFoundException(final String sessionId)
	{
		super(String.format("Client %s not found", sessionId));
	}
}
