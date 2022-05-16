package server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.json.simple.JSONObject;
import dev.onvoid.webrtc.CreateSessionDescriptionObserver;
import dev.onvoid.webrtc.PeerConnectionFactory;
import dev.onvoid.webrtc.PeerConnectionObserver;
import dev.onvoid.webrtc.RTCAnswerOptions;
import dev.onvoid.webrtc.RTCConfiguration;
import dev.onvoid.webrtc.RTCIceCandidate;
import dev.onvoid.webrtc.RTCIceServer;
import dev.onvoid.webrtc.RTCPeerConnection;
import dev.onvoid.webrtc.RTCRtpReceiver;
import dev.onvoid.webrtc.RTCRtpSender;
import dev.onvoid.webrtc.RTCSdpType;
import dev.onvoid.webrtc.RTCSessionDescription;
import dev.onvoid.webrtc.SetSessionDescriptionObserver;
import dev.onvoid.webrtc.media.MediaStream;
import dev.onvoid.webrtc.media.MediaStreamTrack;
import utility.Log;

/*
 * 
 * This implements a weird idea to combine the web socket connection and the RTC peer connection
 * into a single class.
 * Why? the two parts often work together.
 */

@ServerEndpoint("/")
public class Client implements PeerConnectionObserver {

	private final RTCPeerConnection peerConnection;
	
	private String sessionId;
	private Session session;
	
	private Recorder recorder = new Recorder();
	private Integer trackCounter = 0;
	
	public Client() {
		
		RTCConfiguration rtcConfiguration = new RTCConfiguration();

		RTCIceServer stunServer = new RTCIceServer();
		stunServer.urls.add("stun:stun.l.google.com:19302");
		rtcConfiguration.iceServers.add(stunServer);

		PeerConnectionFactory peerConnectionFactory = new PeerConnectionFactory();
		
		Log.log("creating peer connection");
		
		peerConnection = peerConnectionFactory.createPeerConnection(rtcConfiguration, this);
		
	}
	
	public RTCPeerConnection getPeerConnection() {
		return peerConnection;
	}

	@OnOpen
    public void onOpen(Session session) {
		
		this.session = session;
		this.sessionId =  session.getId();
		
		Log.log("onOpen:: %s", sessionId);        

    	ConnectionManager.put(sessionId, this);
        
        
        final String payload = String.format("{\"message\":\"greeting\",\"sessionid\":\"%s\"}", sessionId);
    	sendMessage(payload);
	
	}
    @OnClose
    public void onClose(Session session) {
    	
		this.session = session;
		this.sessionId =  session.getId();

		ConnectionManager.remove(this);
		
		recorder.stop();
		
		Log.log("onClose:: %s", sessionId);        
    }
    
    @OnMessage
    public void onMessage(String message, Session session) {

		this.session = session;
		this.sessionId =  session.getId();

		@SuppressWarnings("unused")
		final MessageTask messageTask = MessageTask.Go(this, message);
		
    }
    
    public void sendMessage(String payload) {
    	
        try {
        	
        	session.getBasicRemote().sendText(payload);
        	
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    @OnError
    public void onError(Throwable t) {
    	Log.log("onError:: %s", t.getMessage());
    }

    public Session getSession() {
    	return this.session;
    }
    
    public String getSessionId() {
    	return this.sessionId;
    }
    
    // PeerConnectionObserver
	public void setRemoteDescrption(JSONObject sdp) { 
		
		//final JSONParser jsonParser = new JSONParser();
		//final JSONObject jsonObject = (JSONObject)jsonParser.parse(jsep);
		
		final String sdpType = (String)sdp.get("type");
		final RTCSdpType remoteSdpType = (sdpType.equalsIgnoreCase("offer")) ? RTCSdpType.OFFER : RTCSdpType.ANSWER;
		final RTCSessionDescription remoteDescription = new RTCSessionDescription(remoteSdpType, (String)sdp.get("sdp"));

		if (remoteSdpType == RTCSdpType.OFFER) {
			Log.log("sdp is an offer");
		} else {
			Log.log("sdp is an answer");
		}
		
		final SetSessionDescriptionObserver setLocalDescriptionObserver =
			new SetSessionDescriptionObserver() {

				@Override
				public void onSuccess() {
					Log.log("Local Description Set");
					
				}

				@Override
				public void onFailure(String error) {
					Log.log("Error setting local description");
					
				} 
			};

		final CreateSessionDescriptionObserver createSessionDescriptionObserver = 
			new CreateSessionDescriptionObserver() {

				@Override
				public void onSuccess(RTCSessionDescription description) {
					
					peerConnection.setLocalDescription(description, setLocalDescriptionObserver);
					
					
					final String payload = 
						String.format(
							"{\"message\":\"answer\",\"sdp\":{\"type\":\"%s\",\"sdp\":\"%s\"}}",
							description.sdpType.toString().toLowerCase(), 
							description.sdp.toString().replace("\r\n", "\\r\\n"));
					
					sendMessage(payload);
					
					Log.log("Answer created");
					
				}

				@Override
				public void onFailure(String error) {
					Log.log("Could not create Answer");
					
				}
		
			};		
				
		final SetSessionDescriptionObserver setRemoteDescriptionObserver =
			new SetSessionDescriptionObserver() {

				@Override
				public void onSuccess() {

					Log.log("Remote Description Set");
					
					if (remoteSdpType == RTCSdpType.OFFER) {
						Log.log("Creating Answer");
						
						final RTCAnswerOptions rtcAnswerOptions = new RTCAnswerOptions();
						rtcAnswerOptions.voiceActivityDetection = false;
						
						peerConnection.createAnswer(rtcAnswerOptions, createSessionDescriptionObserver);

					} else {
						Log.log("Remote %s set", sdpType);

					}
				}

				@Override
				public void onFailure(String error) {
					
					Log.log("Could not set Remote Description %s", sdpType);
					
				} 
			};
		
		peerConnection.setRemoteDescription(remoteDescription, setRemoteDescriptionObserver);
		
		Log.log("Receivers: %d", peerConnection.getReceivers().length);
		Log.log("Senders: %d", peerConnection.getSenders().length);
		Log.log("Transceivers: %d", peerConnection.getTransceivers().length);

	}
	
	@Override
	public void onIceCandidate(RTCIceCandidate iceCandidate) {
		
		if (iceCandidate == null) return;
		
		final String candidate = String.format(
				"{\"sdpMid\":\"%s\", \"sdpMLineIndex\":%d, \"candidate\":\"%s\"}",
				iceCandidate.sdpMid,
				iceCandidate.sdpMLineIndex,
				iceCandidate.sdp
			);
		
		
		final String payload = String.format("{\"message\":\"icecandidate\",\"candidate\":%s}", candidate);
		
		sendMessage(payload);
		
	}
	
	public void addIceCandidate(JSONObject candidate) {
		
		final String sdp = candidate.get("candidate").toString();
		final String sdpMid = candidate.get("sdpMid").toString();
		final int sdpMLineIndex = Integer.parseInt(candidate.get("sdpMLineIndex").toString());
		
		RTCIceCandidate rtcCandidate = new RTCIceCandidate(sdpMid, sdpMLineIndex, sdp);
		
		peerConnection.addIceCandidate(rtcCandidate);
	}
	
	@Override
	public void onAddTrack(RTCRtpReceiver receiver, MediaStream[] mediaStreams) {
		
		final MediaStreamTrack track = receiver.getTrack();
		Log.log("onAddTrack %s", track.getKind());
		
		final String fileName = String.format("%s-%s-%s", track.getKind(), sessionId, (++trackCounter).toString().trim());
		recorder.addTrack(fileName, receiver.getTrack());
		
	}

	public void connect(Client clientToConnect) {
		
		final RTCPeerConnection connectionToConnect = clientToConnect.peerConnection; 
		
		final int receivers = connectionToConnect.getReceivers().length;
		
		if (receivers == 0) return;
		
		for (int index = 0; index < receivers; index++) {
			
			final RTCRtpReceiver receiver = connectionToConnect.getReceivers()[index];
			if (receiver != null) {
				
				final MediaStreamTrack track = receiver.getTrack();
				if (track != null) {
					
					Log.log("Adding track: %s", track.getKind());
					
					List<String> streamIds = new ArrayList<String>();
					streamIds.add(receiver.getTrack().getId());
					
					@SuppressWarnings("unused")
					RTCRtpSender sender = this.peerConnection.addTrack(track, streamIds);
				}
			}
		}
		
        final String payload = String.format("{\"message\":\"negotiate\"}");
    	sendMessage(payload);
    	
	}
	
	public void toggleRecording() {
		
		if (recorder.getRecording()) {
			
			recorder.stop();
		} else {
			
			recorder.start();
		}

        final String payload = String.format("{\"message\":\"recording\",\"recording\":\"%s\"}", recorder.getRecording());
    	sendMessage(payload);
	}
	
}
