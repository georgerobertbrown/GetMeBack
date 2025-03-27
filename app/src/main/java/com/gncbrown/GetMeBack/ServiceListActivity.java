package com.gncbrown.GetMeBack;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.gncbrown.GetMeBack.Utilities.ServiceInfo;
import com.gncbrown.GetMeBack.Utilities.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ServiceListActivity extends AppCompatActivity {
	private static final String TAG = ServiceListActivity.class.getSimpleName();

    private Context context = null;
	private ListView listView1;
	private static ServiceListAdapter adapter = null;
	private static final List<String> myServicesList = Arrays.asList(
			"com.gncbrown.GetMeBack.Services.LocationService",
			"com.gncbrown.GetMeBack.Services.WatchListenerService"
			);

	public void onCreate(Bundle icicle) {
		context = getApplicationContext();

		setContentView(R.layout.service_list);
		listView1 = findViewById(R.id.listView1);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
			EdgeToEdge.enable(this);
			Window window = getWindow();
			window.setDecorFitsSystemWindows(false);
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.topView), (v, insets) -> {
				int topInset = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
				v.setPadding(0, topInset, 0, 0);
				return WindowInsetsCompat.CONSUMED;
			});
		}

		new LoadApplications().execute();
		super.onCreate(icicle);
	}

	private class LoadApplications extends AsyncTask<Void, Void, Void> {
		private ProgressDialog progress = null;

		@Override
		protected Void doInBackground(Void... params) {
            ArrayList<ServiceInfo> servicesList = getServices(context);
			adapter = new ServiceListAdapter(context, servicesList);

			return null;
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
		}

		@Override
		protected void onPostExecute(Void result) {
			setTitle(Utils.getAppName(context) + " Services");
			listView1.setAdapter(adapter);
			progress.dismiss();
			super.onPostExecute(result);
		}

		@Override
		protected void onPreExecute() {
			progress = ProgressDialog.show(ServiceListActivity.this, null,
					"Loading services info...");
			super.onPreExecute();
		}

		@Override
		protected void onProgressUpdate(Void... values) {
			super.onProgressUpdate(values);
		}
	}

	private ArrayList<ServiceInfo> getServices(Context context) {
		ArrayList<ServiceInfo> myServices = new ArrayList<ServiceInfo>();
		for (String myService: myServicesList) {
			myServices.add(new ServiceInfo(myService, Utils.isServiceRunning(context, myService)));
		}

		return myServices;
	}
}