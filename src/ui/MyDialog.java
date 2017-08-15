package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.concurrent.TimeUnit;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class MyDialog extends JDialog implements ActionListener
{

	private JTextArea jTextArea;
	private JList jList;
	private DialogListener listener;
	private Object[] items;
	private JLabel statusLabel;
	private JButton connectButton;
	private JButton deleteButton;
	private boolean reseted = false;

	public MyDialog(String title, Object [] items , DialogListener listener ) 
	{
		super.setTitle(title);

		this.listener = listener;
		this.setSize(650,500);		

		jList = new JList();
		if(items==null)
		{
			items = new Object[0];
		}
		jList.setListData(items);
		this.items = items;

		JScrollPane jScrollPane = new JScrollPane(jList);
		jScrollPane.setSize(255,5);

		this.getContentPane().setLayout(new BorderLayout());
		this.getContentPane().add(jScrollPane,BorderLayout.CENTER);

		//Button connect to register the device in the file paired_device
		connectButton = new JButton("Connect");
		connectButton.addActionListener(this);
		this.getContentPane().add(connectButton,BorderLayout.EAST);


		//Button delete (Reset software to remove the file paired_device)
		deleteButton = new JButton("Reset Software");
		this.getContentPane().add(deleteButton,BorderLayout.SOUTH);
		deleteButton.addActionListener(this);

		//status
		statusLabel = new JLabel("Started");
		this.getContentPane().add(statusLabel,BorderLayout.NORTH);
		this.getContentPane().setBackground(Color.blue);


		setLocationRelativeTo(null);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.show();
	}

	//every run of the loop we have to empty the list of precedents found devices
	public void resetItems()
	{
		Object [] newitems = new Object[0];
		items = newitems;
		jList.setListData(newitems);
	}

	//function to disable the button connect
	public void disableConnect()
	{
		connectButton.setEnabled(false);
	}

	//function to enable the button connect
	public void enableConnect()
	{
		connectButton.setEnabled(true);
	}

	//function to disable the button deleteButton (reset software)
	public void disableDeleteButton()
	{
		deleteButton.setEnabled(false);
	}

	//function to enable the button deleteButton (reset software)
	public void enableDeleteButton()
	{
		deleteButton.setEnabled(true);
	}

	//add the found devices to the list 
	public void addItem(Object item)
	{

		Object [] newitems = new Object[items.length+1];

		for (int i = 0; i < items.length; i++) 
		{
			newitems[i] = items[i];
		}
		newitems[newitems.length-1] =item;

		items = newitems;
		jList.setListData(newitems);
	}


	//function to set the status of the dialog box
	public void setStatus(String text)
	{
		statusLabel.setText(text);
	}

	//Listener of the buttons
	@Override
	public void actionPerformed(ActionEvent e) 
	{
		// if click on connect
		if(e.getActionCommand().equals("Connect"))
		{
			Object [] selectedItems = jList.getSelectedValues();
			String message = "";//jTextArea.getText();

			listener.sendPressed(selectedItems, message,this);
		}

		//if click on Reset Software
		if(e.getActionCommand().equals("Reset Software"))
		{

			File f = new File("paired_device.txt");

			try
			{
				f.delete();
				reseted=true;
				this.setStatus("Software reset , please restart the application");


			}catch(Exception e2) 
			{   
				// if any error occurs
				this.setStatus("There is nothing to delete , please restart the application");
			}
		}
	}

	//the dialog listener
	public interface DialogListener
	{
		public void sendPressed(Object [] selectedItems, String message,MyDialog dialog);
	}

	//return the boolean reseted to stop the main loop
	public boolean getResetedBoolean()
	{
		return reseted;
	}

}
