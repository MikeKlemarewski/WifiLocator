package com.sfumobile.wifilocator.request;

import java.util.Vector;

import net.rim.device.api.ui.UiApplication;

public class PollingService {
	private static PollingService _self;
	private RequestThread _pollingThread;
	private int _pollingThreadID = 2;
	private Vector _actions;
	private PollingService(){
		_pollingThread = new RequestThread();
		_actions = new Vector();
	}
	
	
	public void addRequest( RequestPackage t ){
		_actions.addElement(t);
	}
	public void removeRequest( RequestPackage t ) {
		_actions.removeElement( t );
	}
	
	public static PollingService getInstance(){
		if (_self == null ){
			_self = new PollingService();
		}
		
		return _self;
	}
	
	public boolean isActive(){
		return _pollingThreadID != -1;
	}
	
	public void startPolling(){
		_pollingThreadID = UiApplication.getUiApplication().invokeLater( _pollingThread, 5*60*1000, true );
	}
	
	public void stopPolling(){
		UiApplication.getUiApplication().cancelInvokeLater( _pollingThreadID );
		_pollingThreadID = -1;
	}
	
class RequestThread extends Thread{
		
		public void run(){
			for(int i = 0; i < _actions.size(); i++){
				RequestPackage currentRequest = (RequestPackage)_actions.elementAt( i );
				currentRequest.init();
			}
		}
	}
}
	
