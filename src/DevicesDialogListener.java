import ui.MyDialog;
import ui.MyDialog.DialogListener;



public class DevicesDialogListener implements DialogListener 
{

	@Override
	public void sendPressed(Object[] selectedItems, String message,MyDialog dialog) 
	{		
		for (Object item : selectedItems) 
		{
			ConnectSoftware.register(((BTDevice)item).toString(),((BTDevice)item).getBTURL(),message, dialog);	
		}
	}

}
