package com.sfumobile.wifilocator;

import org.json.JSONException;
import org.json.JSONObject;

import com.sfumobile.wifilocator.request.LocationRequest;
import com.sfumobile.wifilocator.request.RequestDelegateActivity;
import com.sfumobile.wifilocator.request.RequestHandler;
import com.sfumobile.wifilocator.request.RequestPackage;
import com.sfumobile.wifilocator.request.SingleRequestLauncher;
import com.sfumobile.wifilocator.request.WifiHandler;
import com.sfumobile.wifilocator.response.LocationResponse;
import com.sfumobile.wifilocator.types.RequestTypes;

import android.app.AlertDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.widget.Button;



public class WifiLocatorActivity extends RequestDelegateActivity implements OnClickListener{
    

	private String bssid;
	private TextView zoneName;
	private Button eventsButton, friendButton;
	private ImageView twitterIcon;
	private AutoPoll auto;
	private RequestHandler requestHandler;
	private WifiHandler wifiHandler;
	private AlertDialog alert;
	private Handler handler;
	private LocationRequest _req;
	private RequestPackage             _package;
	private LocationResponse _response;
	@SuppressWarnings("unused")
	private Drawable image;
	private ImageView mapView;
	
	RequestDelegateActivity _rd;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        UserObject.getInstance().set_userID(30001);

        zoneName     = (TextView)this.findViewById(R.id.zoneName);
        eventsButton = (Button)this.findViewById(R.id.eventsButton);
        twitterIcon  = (ImageView)this.findViewById(R.id.twitterIcon);
        friendButton = (Button)this.findViewById(R.id.friendButton);
        mapView      = (ImageView)this.findViewById(R.id.mapView);
        
        eventsButton.setOnClickListener(this);
        twitterIcon.setOnClickListener(this);
        friendButton.setOnClickListener(this);  
        
        handler        = new Handler();
        requestHandler = new RequestHandler(this);
        wifiHandler    = requestHandler.getWifiHandler();
        bssid          = "";
        

    	auto = new AutoPoll(this);


        _req     = new LocationRequest(UserObject.getInstance().get_userID(), wifiHandler.getBSSID());
    	_package = new RequestPackage(this, _req, handler);
        
     

    }
    
    public void onStart(){
    	super.onStart();

    	if(!wifiHandler.wifiEnabled()){
    		alert = AlertDialogBuilder.createDialog(this, "Wifi isn't turned on");
    		alert.show();
    		if(auto.getStatus() == AsyncTask.Status.RUNNING){
    			auto.cancel(true);
    		}
    	}
    	else if(!wifiHandler.wifiConnected()){
    		alert = AlertDialogBuilder.createDialog(this, "You aren't connected to any networks.");
    		alert.show();
    		if(auto.getStatus() == AsyncTask.Status.RUNNING){
    			auto.cancel(true);
    		}
    	}
    	else{
    		if(auto.getStatus() == AsyncTask.Status.PENDING || auto.isCancelled()){
		        auto = (AutoPoll) new AutoPoll(this).execute();
    		}
    	}
    }
    
	public void onClick(View src) {
		Intent myIntent;
		switch(src.getId()){
		case R.id.eventsButton:
    		myIntent = new Intent(this,EventsActivity.class);
    		startActivity(myIntent);
			break;
		case R.id.friendButton:
    		Intent nextScreen = new Intent(this,FriendsActivity.class);
    		startActivity(nextScreen);
    		break;
		case R.id.twitterIcon:
			myIntent = new Intent(this, TwitterActivity.class);
			myIntent.putExtra("zone", UserObject.getInstance().get_zone());
			startActivity(myIntent);
			break;
		}
	}
	
	public void onStop(){
		super.onStop();
		if (auto!=null) {
			auto.cancel(true);
		}
	}
	
	class AutoPoll extends AsyncTask<String, JSONObject, Void> {	

		RequestDelegateActivity _rd;
		
		public AutoPoll(RequestDelegateActivity rd){
	        _rd = rd;
		}
		
		@Override
		protected Void doInBackground(String... params) {
			
			while(!isCancelled()) {
		        try{
		        	//Check every second to see if the bssid has changed
		        	//Only poll the server if it has
		        	System.out.println("Running");
		        	if(bssidChanged()){
		        		updateZoneInfo(_rd);
		        	}
		        	Thread.sleep(1000);
		        } catch (InterruptedException e) {
		        	Thread.currentThread().destroy();
					e.printStackTrace();
				}
			}
			return null;
		}
	}

	public boolean bssidChanged(){
		String current_bssid = wifiHandler.getBSSID();
		if(current_bssid.hashCode() != bssid.hashCode()){
			bssid = current_bssid;
			return true;
		}
		return false;
	}
	
	public void updateZoneInfo(RequestDelegateActivity rd){
    	_req     = new LocationRequest(UserObject.getInstance().get_userID(), wifiHandler.getBSSID());
    	_package = new RequestPackage(rd, _req, handler);
    	SingleRequestLauncher sl = SingleRequestLauncher.getInstance();
    	sl.sendRequest(rd, _package);
	}
	
	@Override
	public void handleStringValue(int type, String val) {
		if ( type == RequestTypes.ZONE){
	    	Log.d("[RESPONSE DATA]", val);
			_response = new LocationResponse( val );
			 bssid     = wifiHandler.getBSSID();
		    JSONObject data = (JSONObject)_response.handleResponse();			
		    try{
				UserObject.getInstance().set_zone(data.getString("zone_name"));
		        UserObject.getInstance().set_map(data.getString("map_name"));
		        System.out.println("USER MAP: " + UserObject.getInstance().get_map());
		        MapUpdateThread mapThread = new MapUpdateThread(mapView, this);
		        mapThread.start();
		        
			} catch (JSONException e) {
				Log.e("JSON Error:", e.getLocalizedMessage());
			} finally {
				zoneName.setText(UserObject.getInstance().get_zone());
			}
			
		}
		
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		zoneName.setText(UserObject.getInstance().get_zone());
	}
	
	@Override
	public void handleIntValue(int type, int val) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleError(int type, int errorCode, Object errorString) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleImageDataValue(int type, String data) {
		// TODO Auto-generated method stub
		
	}
	
	
}