package com.sfumobile.wifilocator;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import com.sfumobile.wifilocator.request.FriendshipConfirmRequest;
import com.sfumobile.wifilocator.request.RequestDelegateActivity;
import com.sfumobile.wifilocator.request.RequestPackage;
import com.sfumobile.wifilocator.request.SingleRequestLauncher;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.view.View.OnClickListener;

public class FriendRequestAdapter extends BaseAdapter{

	private LayoutInflater mInflater;
	private RequestDelegateActivity _rd;
	private static ArrayList<JSONObject> _data;
	private Handler handler;
	private FriendshipConfirmRequest  _req;
	private RequestPackage            _package;
	
	public FriendRequestAdapter(RequestDelegateActivity rd, ArrayList<JSONObject> data){
		mInflater = LayoutInflater.from(rd);
		_rd = rd;
		_data = data;
		handler = new Handler();
	}
	
	public View getView(final int position, View convertView, ViewGroup parent) {

		ViewHolder holder;

		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.friend_request_line, null);
				
			holder = new ViewHolder();
			holder.friendText = (TextView) convertView.findViewById(R.id.friendNameText);
			holder.confirmButton = (Button) convertView.findViewById(R.id.confirmButton);
			holder.rejectButton = (Button) convertView.findViewById(R.id.rejectButton);

			holder.confirmButton.setOnClickListener(new OnClickListener() {
			private int pos = position;
			public void onClick(View v) {
						
				try {
					_req = new FriendshipConfirmRequest( _data.get(pos).getInt("request_id") );
					_data.remove(pos);
					notifyDataSetChanged();
				} catch (JSONException e) {
					e.printStackTrace();
				}
				_package = new RequestPackage(_rd, _req, handler);
				SingleRequestLauncher launcher = SingleRequestLauncher.getInstance();
				launcher.sendRequest(_rd, _package);
				
			}
			});
			
			holder.rejectButton.setOnClickListener(new OnClickListener() {
			private int pos = position;
			 	
				public void onClick(View v) {
					delete(pos);
				}
			});
			
			convertView.setTag(holder);

		}
	    else {
		holder = (ViewHolder) convertView.getTag();
		}
				 		
		if(_data.size() > 0){
			try {
				String last_name = _data.get(position).getString("last_name");
				String first_name = _data.get(position).getString("first_name");
				holder.friendText.setText(first_name + " " + last_name);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		else{
			holder.friendText.setText("No friend requests");
			holder.confirmButton.setVisibility(View.INVISIBLE);
			holder.rejectButton.setVisibility(View.INVISIBLE);
		}		 
				
		return convertView;
	}
		 
	public void delete(int index){
		_data.remove(index);
		System.out.println("DATA After Delete- " + _data.toString());
		notifyDataSetChanged();
	}
	
	public int getCount() {
		return _data.size() > 0 ? _data.size() : 1;
	}

	public Object getItem(int index) {
		return _data.get(index);
	}

	public long getItemId(int arg0) {
		return arg0;
	}

	static class ViewHolder {
		TextView friendText;
		Button confirmButton;
		Button rejectButton;
	}
}
