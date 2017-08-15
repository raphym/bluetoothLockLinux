import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

import javax.bluetooth.DataElement;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.UUID;

import com.ibm.oti.connection.btgoep.Connection;
import com.intel.bluetooth.RemoteDeviceHelper;

import ui.MyDialog;

public class BTServicesSearcher
{

	static String deviceDetails;//name + [address] to register or to check
	private static MyDialog dialog;
	private static RemoteDeviceDiscoverer remoteDeviceDiscoverer;

	static final UUID OBEX_OBJECT_PUSH = new UUID(0x1105);

	public static final Vector serviceFound = new Vector();

	private static final String LOCKING = "gnome-screensaver-command -l";
	private static final String UNLOCKING = "loginctl unlock-session";

	public static boolean isRange=false;
	public static boolean active=true;


	public static void main(String[] args) throws IOException, InterruptedException 
	{
		System.out.println("*******************************************************");
		System.out.println("Debugging log to debug the program if needed");
		System.out.println("*******************************************************");
		BufferedReader br = null;
		FileReader fr = null;
		String FILENAME = "paired_device.txt";
		String registeredDevice="test";//the registered device from the database

		try {

			fr = new FileReader(FILENAME);
			br = new BufferedReader(fr);

			String sCurrentLine;

			br = new BufferedReader(new FileReader(FILENAME));

			while ((sCurrentLine = br.readLine()) != null) 
			{
				registeredDevice=sCurrentLine;
			}

		} catch (IOException e) 
		{
			//The device is not registered, so run the main loop only once
			active=false;
		} finally 
		{

			try {

				if (br != null)
					br.close();

				if (fr != null)
					fr.close();

			} catch (IOException ex) 
			{

				ex.printStackTrace();

			}

		}

		dialog = new MyDialog("Lock & Unlock by @ Matan & Raphael",null, new DevicesDialogListener());
		remoteDeviceDiscoverer = new RemoteDeviceDiscoverer();

		//it it is the first time ==>> register the device
		if(active==false)
		{
			dialog.enableConnect();
			dialog.disableDeleteButton();
		}
		//The device is already registered
		else
		{
			dialog.disableConnect();
			dialog.enableDeleteButton();
		}


		// First run RemoteDeviceDiscovery and use discovered device
		dialog.setStatus("Searching devices, please wait..");


		//If the device is not registered so it run one time
		//And if the device is already registered it is running until you quit the application
		while(true)
		{
			//for the log
			System.out.println("--------------------------------------------------");

			//if reseted software (button reset software)
			if(dialog.getResetedBoolean())
			{
				dialog.setStatus("Software reset , please restart the application");
				break;
			}


			//Start the search*****
			remoteDeviceDiscoverer.searchDevices();
			serviceFound.clear();

			UUID serviceUUID = OBEX_OBJECT_PUSH;
			if ((args != null) && (args.length > 0))
			{
				serviceUUID = new UUID(args[0], false);
			}

			final Object serviceSearchCompletedEvent = new Object();

			DiscoveryListener listener = new DiscoveryListener() 
			{

				public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) 
				{
				}

				public void inquiryCompleted(int discType) 
				{
				}

				public void servicesDiscovered(int transID, ServiceRecord[] servRecord) 
				{

					for (int i = 0; i < servRecord.length; i++) {

						String url = servRecord[i].getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false);
						if (url == null) 
						{
							continue;
						}
						serviceFound.add(url);
						DataElement serviceName = servRecord[i].getAttributeValue(0x0100);
						dialog.addItem(new BTDevice(deviceDetails,url));
					}
				}

				public void serviceSearchCompleted(int transID, int respCode) 
				{
					System.out.println("service search completed!");
					synchronized(serviceSearchCompletedEvent)
					{
						serviceSearchCompletedEvent.notifyAll();
					}
				}

			};

			UUID[] searchUuidSet = new UUID[] { serviceUUID };
			int[] attrIDs =  new int[] 
					{
					0x0100 // Service name
					};


			int size = remoteDeviceDiscoverer.getDevicesDiscovered().size();
			isRange=false;
			dialog.setStatus(size + " devices found. Now checking which is supported.");

			for(Enumeration en = remoteDeviceDiscoverer.getDevicesDiscovered().elements(); en.hasMoreElements(); ) 
			{
				RemoteDevice btDevice = (RemoteDevice)en.nextElement();

				synchronized(serviceSearchCompletedEvent) 
				{
					String friendlyName = "";
					int counter = 0;

					try
					{
						friendlyName = btDevice.getFriendlyName(false);
						String deviceAddress = btDevice.getBluetoothAddress();
						deviceDetails = friendlyName + " ["+deviceAddress+"]" ;

						if(registeredDevice!=null && deviceDetails.equals(registeredDevice))
						{
							isRange=true;
							System.out.println("Registered device found : " + deviceDetails);
							break;
						}

					}
					catch (Exception e) 
					{
						System.out.println("(dont know the name)");
						deviceDetails = null;
					}

					LocalDevice.getLocalDevice().getDiscoveryAgent().searchServices(attrIDs, searchUuidSet, btDevice, listener);
					serviceSearchCompletedEvent.wait();	
				}
			}

			//END the search******
			//So here we will check is the device is in the range or not
			//Active = the device is registered 
			//IsRange = the device is in the range
			if(active==true && isRange == false)
			{
				System.out.println("The device is not in the range");
				dialog.setStatus("The device is not in the range");
				lockAndUnlock(true);//lock the computer
			}
			else if(active == false)
			{
				System.out.println("Search finished ; You must first register your device , Select your device and press Connect");
				dialog.setStatus("Search finished ; You must first register your device , Select your device and press Connect");
				break;
			}
			else
			{
				System.out.println("The device is in the range");
				dialog.setStatus("The device is in the range");
				lockAndUnlock(false);//unlock the computer
			}

			//reset the list of found device
			dialog.resetItems();

		}
	}

	public static void lockAndUnlock(boolean lock)
	{

		final Runtime runtime = Runtime.getRuntime();
		Process process = null;
		try 
		{
			/*
			 * open a shell and execute the command in that shell
			 */
			if(lock)
			{
				String[] OPEN_SHELL = { "/bin/sh", "-c", LOCKING };
				process = runtime.exec(OPEN_SHELL);

			}
			else
			{
				String[] OPEN_SHELL = { "/bin/sh", "-c", UNLOCKING };
				process = runtime.exec(OPEN_SHELL);
			}
		} catch(final IOException e) 
		{
			e.printStackTrace();
		}
	}
}
