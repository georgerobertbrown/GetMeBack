package com.gncbrown.GetMeBack;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.gncbrown.GetMeBack.Services.WatchListenerService;
import com.gncbrown.GetMeBack.Utilities.ServiceInfo;
import com.gncbrown.GetMeBack.Utilities.Utils;

import java.util.ArrayList;

public class ServiceListAdapter extends ArrayAdapter<ServiceInfo> {
	private static final String TAG = ServiceListAdapter.class.getSimpleName();

	private final Context context;
	private final ArrayList<ServiceInfo> servicesList;

	public ServiceListAdapter(Context context, ArrayList<ServiceInfo> servicesList) {
		super(context, R.layout.service_row);
		this.context = context;
		this.servicesList = servicesList;
	}

	@Override
	public int getCount() {
		return ((null != servicesList) ? servicesList.size() : 0);
	}

	@Override
	public ServiceInfo getItem(int position) {
		return ((null != servicesList) ? servicesList.get(position) : null);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	static class ViewHolder {
		protected TextView text;
		protected CheckBox checkbox;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.service_row, parent, false);
		TextView serviceNameTextView = rowView
				.findViewById(R.id.serviceName);
		serviceNameTextView.setText(servicesList.get(position).getServiceName());
		CheckBox checkBox = rowView.findViewById(R.id.checkBoxServiceRunning);
		checkBox.setChecked(servicesList.get(position).isRunning());

		checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				String serviceName = servicesList.get(position).getServiceName();
				Log.d(TAG, "app=" + serviceName
						+ ", checked=" + isChecked);
				servicesList.get(position).setRunning(isChecked);
				switch (serviceName) {
					case "LocationService":
						Toast.makeText(context,Utils.getAppName(context)
										+ " can not " + (isChecked ? "start" : "stop")
										+ " LocationService.",
								Toast.LENGTH_SHORT).show();
						break;

                    case "WatchListenerService":
						Intent intent = new Intent(context, WatchListenerService.class);
						if (isChecked) {
							context.startService(intent);
						} else {
							context.stopService(intent);
						}
                        Toast.makeText(context,Utils.getAppName(context)
										+ " " + (isChecked ? "start" : "stop")
										+ " WatchListenerService.",
								Toast.LENGTH_SHORT).show();
						break;
                }
			}
		});

		return rowView;
	}
}