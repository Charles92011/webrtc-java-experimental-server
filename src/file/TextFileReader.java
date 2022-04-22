package file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class TextFileReader 
implements AutoCloseable
{

	public static TextFileReader open(String filename) 
		throws FileNotFoundException {

		final TextFileReader fileReader = new TextFileReader(filename);
		
		fileReader.open();
		
		return fileReader; 
	}
	
	public static TextFileReader read(String filename)
		throws IOException, FileNotFoundException {

		final TextFileReader fileReader = new TextFileReader(filename);
		
		fileReader.readAll();
		
		return fileReader; 
	}
	
	private final String filename;
	private File file = null;
	private FileReader fileReader = null;
	private BufferedReader bufferedReader = null;
	private String buffer = "";
	
	private TextFileReader(String filename) {

		this.filename = filename;
	}
	
	private void open() throws FileNotFoundException {

		file = new File(filename);
		fileReader = new FileReader(file);
		bufferedReader = new BufferedReader(fileReader);
	}
	
	public String readAll() throws IOException {

		final Integer fileLength = (int)file.length();
		final byte[] data = new byte[fileLength];
		
		buffer = "";
		
		try (final FileInputStream fileInputStream = new FileInputStream(file)) {

			fileInputStream.read(data);
			buffer = new String(data, "UTF-8");
		}
		
		return buffer;
	}
	
	public String readLine() throws IOException {

		buffer = bufferedReader.readLine();
		return buffer;
	}
	
	@Override
	public void close() throws IOException {

		if (bufferedReader != null)	bufferedReader.close();
		if (fileReader != null)		fileReader.close();

	}

	public String getBuffer() {
		return buffer;
	}
}
