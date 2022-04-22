package file;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

public class TextFileWriter implements AutoCloseable
{
	
	public static final Boolean APPEND = true;
	public static final Boolean OVERWRITE = false;
	
	public static TextFileWriter writeFile(String filename, String buffer) 
		throws UnsupportedEncodingException, IOException, FileNotFoundException {
		
		final TextFileWriter textFileWriter = TextFileWriter.open(filename, OVERWRITE);
		textFileWriter.write(buffer);
		
		return textFileWriter;
	}
	
	public static TextFileWriter open(String filename) 
		throws FileNotFoundException, UnsupportedEncodingException {

		return TextFileWriter.open(filename, OVERWRITE);
	}
	
	public static TextFileWriter open(String filename, Boolean append) 
		throws FileNotFoundException, UnsupportedEncodingException {

		final TextFileWriter textFileWriter = new TextFileWriter(filename);
		
		textFileWriter.open(append);
		
		return textFileWriter;
	}
	

	private final String filename;
	private FileOutputStream fileOutputStream = null;
	private OutputStreamWriter outputStreamWriter = null;
	private BufferedWriter bufferedWriter;
	
	private TextFileWriter(String filename) {
		
		this.filename = filename;
	}
	
	private void open(Boolean append)
		throws FileNotFoundException, UnsupportedEncodingException {

		fileOutputStream = new FileOutputStream(filename, append);
		
		outputStreamWriter = new OutputStreamWriter(fileOutputStream, "utf-8");
		bufferedWriter = new BufferedWriter(outputStreamWriter);
	}

	@Override
	public void close() throws Exception {

		if (bufferedWriter != null) bufferedWriter.close();
		if (outputStreamWriter != null) outputStreamWriter.close();
		if (fileOutputStream != null) fileOutputStream.close();
	}
	
	public void write(final String buffer) throws IOException {

		bufferedWriter.write(buffer);
	}
	
	public void write(final String message, Object... args) throws IOException {

		final String buffer = String.format(message, args);
		this.write(buffer);
	}
	
	public void writeLine(final String message) throws IOException {

		final String buffer = String.format("%s\n", message);
		bufferedWriter.write(buffer);
	}
	
	public void writeLine(final String message, Object... args) throws IOException {

		final String buffer = String.format(message, args);
		this.writeLine(buffer);
	}
}
