import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.TextBox;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;


public class Main extends MIDlet implements CommandListener {
	private final BluetoothClient client;
	private Display display; 
	private TextBox deviceList;
	private static Main self;
	
	
	public static void log(String s){
		String ss = Main.self.deviceList.getString();
		Main.self.deviceList.setString(ss+"\n"+s);
	}
	public Main() {
		client = new BluetoothClient(this);
		display = Display.getDisplay(this);
	
		
	 
		
	}

	protected void destroyApp(boolean arg0) throws MIDletStateChangeException {
		// TODO Auto-generated method stub

	}

	protected void pauseApp() {
		// TODO Auto-generated method stub

	}

	protected void startApp() throws MIDletStateChangeException {
		Main.self=this;
		deviceList = new TextBox("devices","",256,0);
		display.setCurrent(deviceList);
	
		
		client.discover();
	}
	public void commandAction(Command c, Displayable d) {
	
	}
			  
		 
	
}
