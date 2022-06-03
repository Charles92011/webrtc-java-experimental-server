package recording;

import java.util.ArrayList;
import java.util.List;

import dev.onvoid.webrtc.media.MediaStreamTrack;

public class Recorder {

/*
 *  ffmpeg -sample_rate 48000 -f s16le -ac 1 -i .\audio-468d58c6-4b3a-440c-b6a9-2b673e64739d-1 -i .\video-468d58c6-4b3a-440c-b6a9-2b673e64739d-2 x.mp4
 */
	
	
	final private List<TrackRecorder> trackRecorders = new ArrayList<TrackRecorder>();

	Boolean recording = false;
	
	public Recorder() {
		
	}
	
	
	public Boolean getRecording() {
		return recording;
	}


	public void addTrack(String filename, MediaStreamTrack mediaStreamTrack) {
		
		TrackRecorder trackRecorder = new TrackRecorder(filename, mediaStreamTrack);
		trackRecorders.add(trackRecorder);
		
		if(recording) {

			trackRecorder.start();
		}
	}
	
	public void start() {

		for (TrackRecorder trackRecorder : trackRecorders) {
			
			trackRecorder.start();
		}
		
		this.recording = true;
	}
	
	public void stop() {

		for (TrackRecorder trackRecorder : trackRecorders) {
			
			trackRecorder.stop();
		}
				
		this.recording = false;
	}

}
