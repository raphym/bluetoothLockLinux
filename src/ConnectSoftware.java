import java.io.IOException;
import java.io.PrintWriter;

import ui.MyDialog;

public class ConnectSoftware 
{
	public static String deviceDetails="";

	//register your device into our database
	public static boolean  register(String name,String deviceBTURL, String message, MyDialog dialog)
	{
		deviceDetails=name;
		try
		{
			//write the device into the file paired_device
			PrintWriter writer = new PrintWriter("paired_device.txt", "UTF-8");
			writer.println(deviceDetails);
			writer.close();
			System.out.println("Ok your phone is registered please restart the application");
			dialog.setStatus("Ok your phone is registered please restart the application");
		} 
		catch (IOException e) 
		{
			System.out.println("Problem to register your phone ,  please restart the application");
			dialog.setStatus("Problem to register your phone ,  please restart the application");
			return false;
		}
		return true;
	}
}
