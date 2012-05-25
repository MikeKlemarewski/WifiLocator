package com.sfumobile.wifilocator.request;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.io.Connection;
import javax.microedition.io.InputConnection;

import net.rim.device.api.io.transport.ConnectionDescriptor;
import net.rim.device.api.io.transport.ConnectionFactory;
import net.rim.device.api.io.transport.TransportInfo;

public class WifiLocatorRequestThread extends Thread {
	private String _url;
	private RequestDelegate _delegate;
	private int     _type;
	private static final int CONNECTION_FAILED = 1;
	public WifiLocatorRequestThread( int type,  String url, RequestDelegate delegate ) {
		_url = url;
		_delegate = delegate;
		_type  = type;
	}
	public void run(){
		if ( _url == null ) {
			return;
		}
        ConnectionFactory factory = new ConnectionFactory();
        String url = _url;
        int[] preferredTypes = { TransportInfo.TRANSPORT_TCP_WIFI};
        factory.setPreferredTransportTypes(preferredTypes);

        factory.setConnectionMode(ConnectionFactory.ACCESS_READ);
        ConnectionDescriptor _connectionDescriptor = null;
        try {
        	_connectionDescriptor  = factory.getConnection(url);
        } catch (NullPointerException ex ){
        	_delegate.handleError( _type, CONNECTION_FAILED , "Unable to get connection");
        }
        String result = "";
            OutputStream os = null;
            InputStream is = null;
            Connection connection = _connectionDescriptor.getConnection();
            try{                
                // Get InputConnection and read the server's response
                InputConnection inputConn = (InputConnection) connection;
                is = inputConn.openInputStream();                                
                byte[] data = net.rim.device.api.io.IOUtilities.streamToBytes(is);
                result = new String(data);
                is.close();
                            
            }
            catch(Exception e){
                result = "ERROR fetching content: " + e.toString();    
            }
            finally {
                // Close OutputStream
                if(os != null){
                    try{
                        os.close();
                    }
                    catch(IOException e){}
                }
                
                // Close InputStream
                if(is != null){
                    try{
                        is.close();
                    }
                    catch(IOException e){}
                }  
                try{
                    connection.close();
                }
                catch(IOException ioe){}              
            }
            _delegate.handleStringValue( _type, result );
    }
}
