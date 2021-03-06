package com.sfumobile.wifilocator.request;

import android.os.Handler;

import com.sfumobile.wifilocator.types.RequestTypes;

public class RequestPackage {

	private RequestDelegateActivity _rd;
	private Request _request;
	private WifiLocatorRequestThread _thread;
	private WifiHandler wifiHandler;
	private Handler _handler;
	
	
	public RequestPackage( RequestDelegateActivity rd, Request req, Handler handler){
		_rd = rd;
		_request = req;
		_handler = handler;
		wifiHandler = new WifiHandler(_rd);
	}
		
	/*private boolean contains( String[] list, String val ){
		int length = list.length;
		boolean retVal = false;
		for(int i = 0; i < length; i++){
			if ( list[i].equals(val)){
				retVal = true;
				break;
			}
		}
		return retVal;
	}*/
	public void init() {
		System.out.println("[SFUMOBILE] - starting init");
		if (wifiHandler.wifiEnabled()){
			System.out.println("[SFUMOBILE] - connected to an SSID");
						
			int type = _request.getType();
			String url = _request.getURL();

			if ( type == RequestTypes.ZONE){
				_request.setProperty("mac_address",
						 wifiHandler.getBSSID(),
						 RequestTypes.STRING_TYPE);
			}
			String payload = _request.getPayload().toString();
			
			System.out.println( "[SFUMOBILE] The payload - "+payload);
			//special case since this request is dynamic
			System.out.println("[SFUMOBILE] - testing out this url "+url);
			_thread = new WifiLocatorRequestThread(type,
					url,
					payload,
					_rd,
					_handler);
			
			if (_rd == null ) {
				System.out.println("Unable to get instance of the application");
			}
			else {
				_thread.start();
			}
		}

	}
}
