package utility;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

import file.TextFileReader;

public enum Properties 
{

	INSTANCE;
	
	private final String filename = "properties.txt";
	
	private final HashMap<String, String>properties = new HashMap<String, String>();

	
	public static String getPropertyS(String property)
	{
		return INSTANCE.getProperty(property);
	}
	
	public static String getPropertyS(String property, String theDefault)
	{
		return INSTANCE.getProperty(property, theDefault);
		
	}
	
	public String getProperty(String property)
	{
		return getProperty(property, "");
	}
	
	public String getProperty(String property, String theDefault)
	{
		String value = theDefault;
		final String key = property.toUpperCase();
		
		if (properties.containsKey(key)) 
		{
			value= properties.get(key);
		}
		
		return value;
		
	}
	
	public Integer getPropertyInt(String property)
	{
		return getPropertyInt(property, 0);
		
	}
	
	public Integer getPropertyInt(String property, Integer theDefault)
	{
		Integer value = theDefault;
		final String key = property.toUpperCase();
		
		if (properties.containsKey(key)) 
		{
			String strValue = properties.get(key);
			value = Integer.parseInt(strValue);
		}
		
		return value;
	}
	

	private Properties() 
	{
		try (final TextFileReader textFileReader = TextFileReader.open(filename))
		{
			
			String readLine;
			
			while ((readLine = textFileReader.readLine()) != null)
			{
				if (readLine.contains("="))
				{
					int equalsIndex = readLine.indexOf('=');
					
					if (equalsIndex > -1)
					{
						final String key = readLine.substring(0,  equalsIndex).toUpperCase().trim();
						final String value = readLine.substring(equalsIndex + 1).trim();
						
						properties.put(key, value);
					}
				}
			}

		} catch (FileNotFoundException ex) {

			ex.printStackTrace();
			
		} catch (IOException ex) {
			
			ex.printStackTrace();
		}
	}	

}
