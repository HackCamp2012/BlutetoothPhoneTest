

import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;
import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;

import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;

import com.sun.midp.io.j2me.btgoep.BTGOEPConnection;
import com.sun.midp.io.j2me.btl2cap.BTL2CAPConnection;
 

public class BluetoothClient implements Runnable {
 
    private InquiryListener listener;
    private String deviceName;
    private boolean listening = true;
    private BTGOEPConnection con;
    private Main main;
    
    public BluetoothClient(Main m){
    	this.main = m;
    }
    
    public void discover(){
    	Main.log("starting device discovery...");
        try {
            LocalDevice local_device = LocalDevice.getLocalDevice();
            
            DiscoveryAgent disc_agent = local_device.getDiscoveryAgent();
            local_device.setDiscoverable(DiscoveryAgent.GIAC);
            listener = new InquiryListener();
            synchronized(listener)	{
                disc_agent.startInquiry(DiscoveryAgent.GIAC, listener);
                try {
                	listener.wait(); 
                } catch(InterruptedException e){}
            }
 
            Enumeration devices = listener.cached_devices.elements();
            UUID[] u = new UUID[]{new UUID( "6ABC1C60693D11E1B86C0800200C9A66", false )};
            int attrbs[] = { 0x0100 };
            
            while( devices.hasMoreElements() ) {
                synchronized(listener)	{
                	//Main.log("loop1");
                    disc_agent.searchServices(attrbs, u, (RemoteDevice)devices.nextElement(), listener);
                    //Main.log("loop2");
                    try {
                    	listener.wait();
                    } catch(InterruptedException e){
                    	Main.log(e.toString());
                    }
                    //Main.log("loop3");
                }
            }
            //Main.log("loop end");
        } catch (BluetoothStateException e) {
        	Main.log(e.toString());
        }
 
        if (listener.service!=null){
            try {
                String url;
                url = listener.service.getConnectionURL(0, false);
                deviceName = LocalDevice.getLocalDevice().getFriendlyName();
                con = (BTGOEPConnection) Connector.open( url );
                Main.log("sending greeting...");
                send(("Hello server, my name is: " + LocalDevice.getLocalDevice().getFriendlyName()).getBytes());
                Main.log("now listen in new Thread");
                Thread t = new Thread(this);
                t.start();
            } catch (IOException g) {
            	Main.log(g.toString());
            }
        }else{
        	Main.log("no service!");
        }
        
        	

        
    }

	public void run() {
		byte[] b = new byte[1000];
        while (listening) {            
    		try {
    			
				con.read(b);
			    String s = new String(b, 0, b.length);
                Main.log("Received from server: " + s.trim());	
    			
				
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
        
	}
    
    public void send(byte[] b){
    	try {
			con.write(b,b.length);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
}


class InquiryListener implements DiscoveryListener {
    public Vector cached_devices;
    public ServiceRecord service;
    public InquiryListener() {
        cached_devices = new Vector();
    }
 
    public void deviceDiscovered( RemoteDevice btDevice, DeviceClass cod )	{
        int major = cod.getMajorDeviceClass();
        if( ! cached_devices.contains( btDevice ) )	{
            cached_devices.addElement( btDevice );
            try {
				Main.log("DEVICE FOUND: "+btDevice.getFriendlyName(false));
			} catch (IOException e) {
				Main.log("DEVICE FOUND: "+btDevice.getBluetoothAddress());
			}
        }
    }
 
    public void inquiryCompleted( int discType )	{
        synchronized(this){	
        	this.notify();
        	Main.log("inquiry complete");
        }
    }
 
    public void servicesDiscovered( int transID, ServiceRecord[] servRecord )	{
    	Main.log("service found!");
    	this.service=servRecord[0];
    }
    public void serviceSearchCompleted( int transID, int respCode )	{
    	synchronized(this){	
    		Main.log("service discover complete");
    		this.notify();
    	}
    }
}
 

