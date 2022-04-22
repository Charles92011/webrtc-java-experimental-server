package server;

import java.util.concurrent.ConcurrentHashMap;

public enum ConnectionManager 
{
	INSTANCE;

	private final ConcurrentHashMap<String, Client>clients = new ConcurrentHashMap<String, Client>();
	
	private ConnectionManager() {

	}
	
	public static Client put(String sessionId, Client client)
	{
		INSTANCE.clients.put(sessionId, client);
		return client;
	}
	
	public static Client get(String sessionId) throws ClientNotFoundException
	{
		final Client client = INSTANCE.clients.get(sessionId);
		if (client == null) throw new ClientNotFoundException(sessionId);
			
		return client;
	}
	
	public static Client remove(Client client)
	{
		final String sessionId = client.getSessionId();
		return remove(sessionId);
	}
	
	public static Client remove(String sessionId)
	{
		return INSTANCE.clients.remove(sessionId);
	}
	
}