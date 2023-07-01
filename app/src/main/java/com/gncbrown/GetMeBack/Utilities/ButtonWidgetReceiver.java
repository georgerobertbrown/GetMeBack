package com.gncbrown.GetMeBack.Utilities;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.widget.RemoteViews;

import com.gncbrown.GetMeBack.MainActivity;
import com.gncbrown.GetMeBack.R;
import com.google.android.gms.maps.model.LatLng;

public class ButtonWidgetReceiver extends AppWidgetProvider {
	private static final String TAG = ButtonWidgetReceiver.class.getSimpleName();

	private static Context context;

	public static final String WIDGET_IDS_KEY = "com.gncbrown.GetMeBack.mywidgetproviderwidgetids";
	public static final String WIDGET_DATA_KEY = "com.gncbrown.GetMeBack.mywidgetproviderwidgetdata";
	public static final String ACTION_WIDGET_UPDATE_FROM_ACTIVITY = "com.gncbrown.GetMeBack.ACTION_WIDGET_UPDATE_FROM_ACTIVITY";
	public static final String ACTION_ACTIVITY_UPDATE_FROM_WIDGET = "com.gncbrown.GetMeBack.ACTION_ACTIVITY_UPDATE_FROM_WIDGET";
	public static final String ACTION_ACTIVITY_GO_TO_FROM_WIDGET = "com.gncbrown.GetMeBack.ACTION_ACTIVITY_GO_TO_FROM_WIDGET";
	public static final String ACTION_BUTTON_SELECTED = "buttonSelected";
	public static final String ACTION_WIDGET_WHICH_CHECKED = "ACTION_WIDGET_WHICH_CHECKED";
	public static final String ACTION_WIDGET_ENABLED = "ACTION_WIDGET_ENABLED";
	public static final String ACTION_MARK_LOCATION = "MarkMyLocation";
	public static final String ACTION_RETURN_TO_DESTINATION = "ReturnToDestination";
	public static final String ACTION_SHOW_APP = "ShowApp";

	private String[] navigationMethods;



	private static AppWidgetManager appWidgetManager = null;
    private int[] appWidgetIds = null;

	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		this.context = context;
        ButtonWidgetReceiver.appWidgetManager = appWidgetManager;

		navigationMethods = context.getResources().getStringArray(R.array.navigationMethods);

		updateWidgetViews(context);
		saveWidgetViews(context);
	}

	@Override
	public void onEnabled(Context context) {
		IntentFilter iFilter = new IntentFilter(ACTION_WIDGET_UPDATE_FROM_ACTIVITY);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			context.getApplicationContext().registerReceiver(this,
					iFilter, Context.RECEIVER_NOT_EXPORTED);
		}

		super.onEnabled(context);
	}

	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		super.onDeleted(context, appWidgetIds);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();

		String message = "onReceive, action=" + action;
		Log.d(TAG, message);

		// v1.5 fix that doesn't call onDelete Action
		if (action.equals(AppWidgetManager.ACTION_APPWIDGET_DELETED)) {
			final int appWidgetId = intent.getExtras().getInt(
					AppWidgetManager.EXTRA_APPWIDGET_ID,
					AppWidgetManager.INVALID_APPWIDGET_ID);
			if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
				this.onDeleted(context, new int[] { appWidgetId });
			}
		} else if (action.equals(AppWidgetManager.ACTION_APPWIDGET_ENABLED)) {
		} else if (action.equals(AppWidgetManager.ACTION_APPWIDGET_UPDATE)) {
			String selectedButton = intent.getExtras().getString(
					ACTION_BUTTON_SELECTED);
			if (intent.hasExtra(ACTION_BUTTON_SELECTED)) {
				//String selectedButton = intent.getExtras().getString(
				//		ACTION_BUTTON_SELECTED);
				Log.d(TAG, "ACTION_APPWIDGET_UPDATE, selectedButton="
						+ selectedButton);

				if (selectedButton.equals(ACTION_MARK_LOCATION)) {
					setLocation();
				} else if (selectedButton.equals(ACTION_RETURN_TO_DESTINATION)) {
					goToLocation();
				} else if (selectedButton.equals((ACTION_SHOW_APP))) {
					Class<?> appClass = new JavaClassLoader()
							.getClass(context.getResources().getString(
									R.string.myPackage));
					Intent appIntent = new Intent(context, MainActivity.class); //appClass);
					appIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					context.startActivity(appIntent);
				}
			}
		} else if (action
				.equals(ACTION_WIDGET_UPDATE_FROM_ACTIVITY)) {
			if (intent.hasExtra(ACTION_WIDGET_WHICH_CHECKED)) {
				String selectedButton = intent.getExtras().getString(
						ACTION_WIDGET_WHICH_CHECKED);
				Boolean enabled = intent.getExtras().getBoolean(
						ACTION_WIDGET_ENABLED);
				Log.d(TAG,
						"ACTION_WIDGET_UPDATE_FROM_ACTIVITY, selectedButton="
								+ selectedButton + ", enabled=" + enabled);

				if (selectedButton.equals(ACTION_MARK_LOCATION)) {
					setLocation();
				} else if (selectedButton.equals(ACTION_RETURN_TO_DESTINATION)) {
					goToLocation();
				}
			}

			try {
				updateWidgetViews(context);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		} else {
			Log.d(TAG, "...skipping unknown action");
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

				Intent returnToDestinationButtonWidget = new Intent(context,
						ButtonWidgetReceiver.class);
				returnToDestinationButtonWidget
						.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
				returnToDestinationButtonWidget.putExtra(
						ACTION_BUTTON_SELECTED,
						ACTION_RETURN_TO_DESTINATION);
				returnToDestinationButtonWidget.putExtra(
						AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds);

				PendingIntent returnToDestinationPendingIntent = PendingIntent.getBroadcast(
						context, 0, returnToDestinationButtonWidget,
						PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
				remoteViews.setOnClickPendingIntent(R.id.returnToDestination,
						returnToDestinationPendingIntent);

				Intent markMyLocationButtonWidget = new Intent(context,
						ButtonWidgetReceiver.class);
				markMyLocationButtonWidget
						.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
				markMyLocationButtonWidget.putExtra(ACTION_BUTTON_SELECTED,
						ACTION_MARK_LOCATION);
				markMyLocationButtonWidget.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,
						allWidgetIds);

				PendingIntent markMyLocationPendingIntent = PendingIntent.getBroadcast(
						context, 1, markMyLocationButtonWidget,
						PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
				remoteViews.setOnClickPendingIntent(R.id.markMyLocation,
						markMyLocationPendingIntent);

				Intent showAppButtonWidget = new Intent(context,
						ButtonWidgetReceiver.class);
				showAppButtonWidget
						.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
				showAppButtonWidget.putExtra(
						ACTION_BUTTON_SELECTED,
						ACTION_SHOW_APP);
				showAppButtonWidget.putExtra(
						AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds);

				PendingIntent showAppPendingIntent = PendingIntent.getBroadcast(
						context, 0, showAppButtonWidget,
						PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
				remoteViews.setOnClickPendingIntent(R.id.showApp,
						showAppPendingIntent);

				appWidgetManager.updateAppWidget(widgetId, remoteViews);
			}
		} else {
			Log.d(TAG,
					"ButtonWidget.updateWidgetViews; appWidgetManager == null!!!");
		}
	}

	private void setLocation() {
		Log.d(TAG, "setLocation.sendBroadcast " + ACTION_ACTIVITY_UPDATE_FROM_WIDGET);
		context.sendBroadcast(new Intent(ACTION_ACTIVITY_UPDATE_FROM_WIDGET));
	}

	private void goToLocation() {
		Log.d(TAG, "goToLocation.sendBroadcast " + ACTION_ACTIVITY_GO_TO_FROM_WIDGET);
		context.sendBroadcast(new Intent(ACTION_ACTIVITY_GO_TO_FROM_WIDGET));
	}
}
