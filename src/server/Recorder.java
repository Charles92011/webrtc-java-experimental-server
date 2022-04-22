package server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import dev.onvoid.webrtc.media.MediaStreamTrack;
import dev.onvoid.webrtc.media.audio.AudioTrack;
import dev.onvoid.webrtc.media.audio.AudioTrackSink;
import dev.onvoid.webrtc.media.video.VideoFrame;
import dev.onvoid.webrtc.media.video.VideoTrack;
import dev.onvoid.webrtc.media.video.VideoTrackSink;
import file.TextFileWriter;
import utility.Log;
import utility.Properties;

public class Recorder {

	private class RecordingTrack  implements AudioTrackSink, VideoTrackSink {
		
		final File file;
		final MediaStreamTrack mediaStreamTrack;

		private Boolean recording = false;
		private Boolean infoWritten = false;
		
		RecordingTrack(String fileName, MediaStreamTrack mediaStreamTrack) {
			
			this.file = new File(recordingPath, fileName);
			this.mediaStreamTrack = mediaStreamTrack;
			
		}
		
		void start() {

			if (mediaStreamTrack.getKind().equals(MediaStreamTrack.AUDIO_TRACK_KIND))
				((AudioTrack)mediaStreamTrack).addSink(this);
			
			else if (mediaStreamTrack.getKind().equals(MediaStreamTrack.VIDEO_TRACK_KIND))
				((VideoTrack)mediaStreamTrack).addSink(this);
			
			else return;	// unknown type, recording didn't start
			
			this.recording = true;

		}
		
		void stop() {

			if (!this.recording) return;
			
			if (mediaStreamTrack.getKind().equals(MediaStreamTrack.AUDIO_TRACK_KIND))
				((AudioTrack)mediaStreamTrack).removeSink(this);
			
			if (mediaStreamTrack.getKind().equals(MediaStreamTrack.VIDEO_TRACK_KIND))
				((VideoTrack)mediaStreamTrack).removeSink(this);
			
			this.recording = false;
		}
		
		@Override
		public void onVideoFrame(VideoFrame frame) {

			if (!infoWritten)
				writeInfo("width: %d, height: %d", frame.buffer.getWidth(), frame.buffer.getHeight());
			
			try (FileOutputStream fos = new FileOutputStream(file, true)) {

				// this is the big mystery
		            
	        } catch (Exception ex) {
	        	ex.printStackTrace();
	        }
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
	
	final private String recordingPath;
	final private List<RecordingTrack> recordingTracks = new ArrayList<RecordingTrack>();

	Boolean recording = false;
	
	public Recorder() {
		
		 recordingPath = Properties.getPropertyS("RecordingPath", ".");
	}
	
	
	public Boolean getRecording() {
		return recording;
	}


	public void addTrack(String filename, MediaStreamTrack mediaStreamTrack) {
		
		RecordingTrack recordingTrack = new RecordingTrack(filename, mediaStreamTrack);
		recordingTracks.add(recordingTrack);
		
		if(recording) {

			recordingTrack.start();
		}
	}
	
	public void start() {

		for (RecordingTrack recordingTrack : recordingTracks) {
			
			recordingTrack.start();
		}
		
		this.recording = true;
	}
	
	public void stop() {

		for (RecordingTrack recordingTrack : recordingTracks) {
			
			recordingTrack.stop();
		}
				
		this.recording = false;
	}

	
}
