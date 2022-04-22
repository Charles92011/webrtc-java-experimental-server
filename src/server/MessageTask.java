package server;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import utility.Log;


public class MessageTask implements Runnable {
	
	
	private final Client client;
	private final String payload;
	
	private String message;
	JSONObject payloadObject;
	
	public MessageTask(Client client, String payload) {

		this.client = client;
		this.payload = payload;
	}
	
	public void go() {

		Thread thread = new Thread(this);
		thread.run();
	}

	@Override
	public void run() {
		
		try {

			if (!parseMessage()) return;

		} catch (ParseException ex) {
			
			Log.log("message Task, Parse Exception: %s", ex.toString());
			return;
		}
		
		Log.log("message task: %s", message);
		
		processMessage();
		
	}

	private Boolean parseMessage() throws ParseException {
		
		final JSONParser jsonParser = new JSONParser();
		
		payloadObject = (JSONObject)jsonParser.parse(payload);
		
		if (!payloadObject.containsKey("message")) return false;
		
		message = payloadObject.get("message").toString(); 
		
		return true;
	}
	
	
	private void processMessage() {
		
		if (message.equals("offer")) {
			
			if (payloadObject.containsKey("sdp")) {
				
				JSONObject sdp = (JSONObject) payloadObject.get("sdp");
				client.setRemoteDescrption(sdp);
			}
		}
		

		if (message.equals("icecandidate")) {
			
			if (payloadObject.containsKey("candidate")) {
				
				JSONObject candidate = (JSONObject) payloadObject.get("candidate");
				client.addIceCandidate(candidate);
			}
		}
		
		if (message.equals("mirror")) {
			
			client.mirror();
		}

		if (message.equals("connect")) {
			
			if (!payloadObject.containsKey("sessionid")) return;
			
			final String sessionToConnect = payloadObject.get("sessionid").toString();
			
			try {
				
				Client clientToConnect = ConnectionManager.get(sessionToConnect);
				
				Log.log("Connecting: %s", sessionToConnect);
				
				client.connect(clientToConnect);
				
			} catch (ClientNotFoundException ex) {

				Log.log("Connection not found: %s", sessionToConnect);
				//ex.printStackTrace();
			}

		}
		
		if (message.equals("record")) {
			
			client.toggleRecording();
		}
	}
	
}
