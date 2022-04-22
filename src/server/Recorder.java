package server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import dev.onvoid.webrtc.media.MediaStreamTrack;
import dev.onvoid.webrtc.media.audio.AudioTrack;
import dev.onvoid.webrtc.media.audio.AudioTrackSink;
import dev.onvoid.webrtc.media.video.NativeI420Buffer;
import dev.onvoid.webrtc.media.video.VideoFrame;
import dev.onvoid.webrtc.media.video.VideoFrameBuffer;
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

			try {
			
			VideoFrameBuffer buffer = frame.buffer;
			NativeI420Buffer fourTwentyBuffer = (NativeI420Buffer)buffer.toI420();
			final ByteBuffer y = fourTwentyBuffer.getDataY();
			final ByteBuffer u = fourTwentyBuffer.getDataY();
			final ByteBuffer v = fourTwentyBuffer.getDataY();			

			Log.log(
				"w:%d, h:%d, sY:%d, sU:%d, sV:%d", //, Y:%d, U:%d, V:%d",
					fourTwentyBuffer.getWidth(),
					fourTwentyBuffer.getHeight(),
					fourTwentyBuffer.getStrideY(),
					fourTwentyBuffer.getStrideU(),
					fourTwentyBuffer.getStrideV());/*,
					y.array().length,
					u.array().length,
					v.array().length);*/
			} catch(Exception ex) {
				ex.printStackTrace();
			}
			
			/*
			if (!infoWritten)
				writeInfo("width: %d, height: %d", frame.buffer.getWidth(), frame.buffer.getHeight());
			
			try (FileOutputStream fos = new FileOutputStream(file, true)) {

				final I420Buffer i420Buffer = frame.buffer.toI420();

				final ByteBuffer sizeData = ByteBuffer.allocate(Integer.BYTES * 5);
				sizeData.putInt(Integer.BYTES * 0, frame.buffer.getHeight());
				sizeData.putInt(Integer.BYTES * 1, frame.buffer.getWidth());
				sizeData.putInt(Integer.BYTES * 2, i420Buffer.getStrideY());
				sizeData.putInt(Integer.BYTES * 3, i420Buffer.getStrideU());
				sizeData.putInt(Integer.BYTES * 4, i420Buffer.getStrideV());
				
				final ByteBuffer y = i420Buffer.getDataY();
				final ByteBuffer u = i420Buffer.getDataY();
				final ByteBuffer v = i420Buffer.getDataY();
				
				
				Log.log("sizeData:%d, Y:%d, U:%d, v:%d", 
						sizeData.array().length,
						y.array().length,
						u.array().length,
						v.array().length);
				fos.write(sizeData.array());
				fos.write(y.array());
				fos.write(u.array());
				fos.write(v.array());
		            
	        } catch (Exception ex) {
	        	ex.printStackTrace();
	        }
			*/
			this.stop();
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
