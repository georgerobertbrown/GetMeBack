package com.gncbrown.GetMeBack.Utilities;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.gncbrown.GetMeBack.GoToActivity;
import com.gncbrown.GetMeBack.R;
import com.gncbrown.GetMeBack.Services.LocationService;

public class ButtonWidgetReceiver extends AppWidgetProvider {
	private static final String TAG = ButtonWidgetReceiver.class.getSimpleName();

	public static final String ACTION_WIDGET_UPDATE_FROM_ACTIVITY = "com.gncbrown.GetMeBack.ACTION_WIDGET_UPDATE_FROM_ACTIVITY";
	public static final String ACTION_ACTIVITY_UPDATE_FROM_WIDGET = "com.gncbrown.GetMeBack.ACTION_ACTIVITY_UPDATE_FROM_WIDGET";
	public static final String ACTION_ACTIVITY_GO_TO_FROM_WIDGET = "com.gncbrown.GetMeBack.ACTION_ACTIVITY_GO_TO_FROM_WIDGET";
	public static final String ACTION_ACTIVITY_LAUNCH_FROM_WIDGET = "com.gncbrown.GetMeBack.ACTION_ACTIVITY_LAUNCH_FROM_WIDGET";
	public static final String ACTION_BUTTON_SELECTED = "buttonSelected";
	public static final String ACTION_MARK_LOCATION = "MarkMyLocation";
	public static final String ACTION_RETURN_TO_DESTINATION = "ReturnToDestination";
	public static final String ACTION_SHOW_APP = "ShowApp";

	public static final int REQ_CODE = 13;


	private static AppWidgetManager appWidgetManager = null;
	private int[] appWidgetIds = null;

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
						 int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		ButtonWidgetReceiver.appWidgetManager = appWidgetManager;

		updateWidgetViews(context);
		saveWidgetViews(context);
		//showMessage(context, "ButtonWidgetReceiver.onUpdate");
	}

	@Override
	public void onEnabled(Context context) {
		IntentFilter iFilter = new IntentFilter(ACTION_WIDGET_UPDATE_FROM_ACTIVITY);
		context.getApplicationContext().registerReceiver(this,
				iFilter, Context.RECEIVER_NOT_EXPORTED);

		super.onEnabled(context);
	}

	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		super.onDeleted(context, appWidgetIds);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
		String action = intent.getAction();

		String message = "onReceive, action=" + action;
		Log.d(TAG, message);

		// v1.5 fix that doesn't call onDelete Action
		switch (action) {
			case AppWidgetManager.ACTION_APPWIDGET_DELETED:
				final int appWidgetId = intent.getExtras().getInt(
						AppWidgetManager.EXTRA_APPWIDGET_ID,
						AppWidgetManager.INVALID_APPWIDGET_ID);
				if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
					this.onDeleted(context, new int[]{appWidgetId});
				}
				break;
			case AppWidgetManager.ACTION_APPWIDGET_ENABLED:
				break;
			case AppWidgetManager.ACTION_APPWIDGET_UPDATE:
				String selectedButton = intent.getExtras().getString(
						ACTION_BUTTON_SELECTED);
				if (intent.hasExtra(ACTION_BUTTON_SELECTED)) {
					//String selectedButton = intent.getExtras().getString(
					//		ACTION_BUTTON_SELECTED);
					Log.d(TAG, "ACTION_APPWIDGET_UPDATE, selectedButton="
							+ selectedButton);

					switch (selectedButton) {
						case ACTION_MARK_LOCATION:
							setLocation(context);
							break;
						case ACTION_RETURN_TO_DESTINATION:
							goToLocation(context);
							break;
						case (ACTION_SHOW_APP):
							launchApp(context);
							break;
					}
				}
				break;
			default:
				Log.d(TAG, "...skipping unknown action");
				break;
		}

		super.onReceive(context, intent);
	}

	private void saveWidgetViews(Context context) {
		if (appWidgetManager != null) {
			// Get all ids
			ComponentName thisWidget = new ComponentName(context,
					ButtonWidgetReceiver.class);
			appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
			Log.d(TAG, "ButtonWidget.saveWidgetViews; appWidgetIds="
					+ appWidgetIds);
		}
	}

	private void updateWidgetViews(Context context) {
		if (appWidgetManager != null) {
			// Get all ids
			ComponentName thisWidget = new ComponentName(context,
					ButtonWidgetReceiver.class);
			int[] allWidgetIds = appWidgetIds == null ? appWidgetManager
					.getAppWidgetIds(thisWidget) : appWidgetIds;
			Log.d(TAG, "ButtonWidget.updateWidgetViews; allWidgetIds="
					+ allWidgetIds);
			for (int widgetId : allWidgetIds) {
				Log.d(TAG, "ButtonWidget.updateWidgetViews; widgetId="
						+ widgetId);
				RemoteViews remoteViews = new RemoteViews(
						context.getPackageName(), R.layout.widget);

				Intent markMyLocationButtonWidget = new Intent(context,
						ButtonWidgetReceiver.class);
				markMyLocationButtonWidget
						.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
				markMyLocationButtonWidget.putExtra(
						ACTION_BUTTON_SELECTED, ACTION_MARK_LOCATION);
				markMyLocationButtonWidget.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,
						allWidgetIds);

				PendingIntent markMyLocationPendingIntent = PendingIntent.getBroadcast(
						context, 1, markMyLocationButtonWidget,
						PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
				remoteViews.setOnClickPendingIntent(R.id.markMyLocation,
						markMyLocationPendingIntent);


				Intent returnToDestinationButtonWidget = new Intent(context,
						ButtonWidgetReceiver.class);
				returnToDestinationButtonWidget
						.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
				returnToDestinationButtonWidget.putExtra(
						ACTION_BUTTON_SELECTED, ACTION_RETURN_TO_DESTINATION);
				returnToDestinationButtonWidget.putExtra(
						AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds);

				PendingIntent returnToDestinationPendingIntent = PendingIntent.getBroadcast(
						context, 0, returnToDestinationButtonWidget,
						PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
				remoteViews.setOnClickPendingIntent(R.id.returnToDestination,
						returnToDestinationPendingIntent);

				appWidgetManager.updateAppWidget(widgetId, remoteViews);
			}
		} else {
			Log.d(TAG,
					"ButtonWidget.updateWidgetViews; appWidgetManager == null!!!");
		}
	}

	private void setLocation(Context context) {
		Log.d(TAG, "setLocation " + ACTION_ACTIVITY_UPDATE_FROM_WIDGET);
		Intent locationIntent = new Intent(context, LocationService.class);
		locationIntent.putExtra("action", context.getResources().getString(R.string.ACTION_GET_LOCATION));
		try {
			context.startForegroundService(locationIntent);
			Toast.makeText(context, "Requesting location", Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			try {
				context.startService(locationIntent);
				Utils.makeNotification(context, "Location", "Requesting location", REQ_CODE);
			} catch (Exception e1) {
				String msg = "setLocation: Could not start LocationService: " + e1.getMessage();
				Log.d(TAG, msg);
				showMessage(context, msg);
				Utils.makeNotification(context, "Location", msg, REQ_CODE);
			}
		}
	}

	private void goToLocation(Context context) {
		Log.d(TAG, "goToLocation " + ACTION_ACTIVITY_GO_TO_FROM_WIDGET);

		Intent launchIntent = new Intent(context, GoToActivity.class);
		launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(launchIntent);
	}

	private void launchApp(Context context) {
		Log.d(TAG, "launchApp " + ACTION_ACTIVITY_LAUNCH_FROM_WIDGET);
		Intent locationIntent = new Intent(context, LocationService.class);
		locationIntent.putExtra("action", context.getResources().getString(R.string.ACTION_LAUNCH));
		try {
			context.startService(locationIntent);
		} catch (Exception e) {
			String msg = "launchApp: Could not start LocationService: " + e.getMessage();
			Log.d(TAG, msg);
			showMessage(context, msg);
		}
	}

	private void showMessage(Context context, String msg) {
		Handler handler = new Handler(Looper.getMainLooper());
		handler.post(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
			}
		});
	}
}