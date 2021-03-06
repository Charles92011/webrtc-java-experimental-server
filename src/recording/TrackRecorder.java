package recording;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import dev.onvoid.webrtc.media.MediaStreamTrack;
import dev.onvoid.webrtc.media.audio.AudioTrack;
import dev.onvoid.webrtc.media.audio.AudioTrackSink;
import dev.onvoid.webrtc.media.video.VideoFrame;
import dev.onvoid.webrtc.media.video.VideoTrack;
import dev.onvoid.webrtc.media.video.VideoTrackSink;
import file.TextFileWriter;
import utility.Log;
import utility.Properties;

public class TrackRecorder implements AudioTrackSink, VideoTrackSink {
	
	private final File file;
	private final MediaStreamTrack mediaStreamTrack;

	private final String mediaKind;
	
	private Boolean recording = false;
	private Boolean infoWritten = false;
	
	private final VideoFileRenderer videoFileRenderer;
	
	TrackRecorder(String fileName, MediaStreamTrack mediaStreamTrack) {
		
		final String recordingPath = Properties.getPropertyS("RecordingPath", ".");
		
		this.file = new File(recordingPath, fileName);
		this.mediaStreamTrack = mediaStreamTrack;
		this.mediaKind = mediaStreamTrack.getKind();
		
		Log.log("Track Recorder: tracks is %s", mediaKind);
		
		if (mediaKind.equals(MediaStreamTrack.VIDEO_TRACK_KIND))
			videoFileRenderer = new VideoFileRenderer(file);
		
		else videoFileRenderer = null;
		
	}
	
	void start() {

		if (mediaKind.equals(MediaStreamTrack.AUDIO_TRACK_KIND))
			((AudioTrack)mediaStreamTrack).addSink(this);
		
		else if (mediaKind.equals(MediaStreamTrack.VIDEO_TRACK_KIND))
			((VideoTrack)mediaStreamTrack).addSink(this);
		
		else return;	// unknown type, recording didn't start
		
		this.recording = true;

	}
	
	void stop() {

		if (!this.recording) return;
		
		if (mediaKind.equals(MediaStreamTrack.AUDIO_TRACK_KIND))
			((AudioTrack)mediaStreamTrack).removeSink(this);
		
		if (mediaKind.equals(MediaStreamTrack.VIDEO_TRACK_KIND))
			((VideoTrack)mediaStreamTrack).removeSink(this);
		
		this.recording = false;
	}
	
	@Override
	public void onData(byte[] data, int bitsPerSample, int sampleRate, int channels, int frames) {
		
		if (!infoWritten)				
			writeInfo("bitsPerSample: %d, sampleRate: %d, channels %d", bitsPerSample, sampleRate, channels);

        try (FileOutputStream fos = new FileOutputStream(file, true)) {

        	fos.write(data);
	            
        } catch (IOException ex) {
        	ex.printStackTrace();
        }
	}
	
	@Override
	public void onVideoFrame(VideoFrame frame) {

		if (!infoWritten)				
			writeInfo("");

		try {
			videoFileRenderer.queue(frame);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	void writeInfo(final String message, Object... args) {
		
		if (infoWritten) return;
		
		final String infoFileName = file.getPath().concat(".info");
		
		try(final TextFileWriter textFileWriter = TextFileWriter.open(infoFileName)) {
			
			textFileWriter.writeLine(message, args);
			Log.lo("Recording: %s ", infoFileName);
			Log.log(message, args);				
			
		} catch (FileNotFoundException ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		} catch (UnsupportedEncodingException ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		} catch (Exception ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}
		
		infoWritten = true;
	}


}
