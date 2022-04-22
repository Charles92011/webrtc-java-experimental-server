package utility;

public class Log 
{
	
	public static void log(String message)
	{
		System.out.println(message);
	}
	
	public static void log(String message, Object... args)
	{
		final String output = String.format(message, args);
		Log.log(output);
	}

	public static void lo(String message)
	{
		System.out.print(message);
	}
	
	public static void lo(String message, Object... args)
	{
		final String output = String.format(message, args);
		System.out.print(output);
	}
	
}
