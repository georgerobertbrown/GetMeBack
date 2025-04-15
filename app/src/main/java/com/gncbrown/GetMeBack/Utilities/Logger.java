package com.gncbrown.GetMeBack.Utilities;

import static android.app.Notification.DEFAULT_SOUND;
import static android.app.Notification.DEFAULT_VIBRATE;
import static androidx.core.app.NotificationCompat.PRIORITY_HIGH;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.gncbrown.GetMeBack.AlertDisplay;
import com.gncbrown.GetMeBack.MainActivity;
import com.gncbrown.GetMeBack.R;

import java.text.SimpleDateFormat;
import java.util.Date;


public class Logger {

	private static final String TAG = Logger.class.getSimpleName();
	public enum LogLevel {None, Normal, Verbose, Ridiculous,
		Info, Debug, Warning, Error}

	public static final LogLevel DEFAULT_LOG_LEVEL = LogLevel.Normal;


	// Notification types
	public static final int NOTIFICATION_UNKNOWN = 0;
	public static final int NOTIFICATION_WIDGET_UPDATE = 1;
	public static final int NOTIFICATION_WIDGET_RECEIVE = 2;
	public static final int NOTIFICATION_SCREEN_OFF = 3;
	public static final int NOTIFICATION_SHOOK = 4;
	public static final int NOTIFICATION_SMS_RECEIVED = 5;
	public static final int NOTIFICATION_ACTION = 6;
	public static final int NOTIFICATION_SPEECH_SERVICE = 7;
	public static final int NOTIFICATION_SPEECH_COMMAND = 8;
	public static final int NOTIFICATION_PROXIMITY = 9;
	public static final int NOTIFICATION_WAVE = 10;

	public static final int NOTIFICATION_SOCIAL_NOTIFICATION_APPS_SELECTED = 82;
	public static final int NOTIFICATION_NO_NOTIFICATION_APPS_SELECTED = 83;
	public static final int NOTIFICATION_PROXIMITY_STARTED_ERROR = 84;
	public static final int NOTIFICATION_SHAKE_STARTED_ERROR = 85;
	public static final int NOTIFICATION_SPEECH_STARTED_ERROR = 86;
	public static final int NOTIFICATION_VOICE_RECOGNITION_TIMEOUT = 87;
	public static final int NOTIFICATION_SPEECH_COMMAND_ERROR = 88;
	public static final int NOTIFICATION_TTS_ERROR = 89;
	public static final int NOTIFICATION_AREA_CODE_ERROR = 90;
	public static final int NOTIFICATION_SPEECH_RECOGNITION_ERROR = 91;
	public static final int NOTIFICATION_PHONE_STATE_IDLE = 93;
	public static final int NOTIFICATION_PHONE_INCOMING_CALL = 94;
	public static final int NOTIFICATION_PHONE_STATE_ERROR = 95;
	public static final int NOTIFICATION_CALL_ERROR = 96;
	public static final int NOTIFICATION_ACTION_ERROR = 97;
	public static final int NOTIFICATION_FEEDBACK_ERROR = 98;
	public static final int NOTIFICATION_DEBUG = 99;

	public static Preferences prefs;


	public Logger(Context context) {
		prefs = new Preferences(context);
	}


	public static void showNotification(Context context, String title, String message, Intent intent,
										int reqCode, int importance) {
		intent.putExtra("From", title);
		intent.putExtra("Message", message);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, reqCode, intent,
				PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);
		String channelId = Utils.getAppName(context);
		NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, channelId)
				.setSmallIcon(android.R.drawable.ic_btn_speak_now)
				.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))
				.setTicker(title)
				.setContentTitle(title)
				.setContentText(message)
				.setAutoCancel(true)
				.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
				.setContentIntent(pendingIntent)
				.setDefaults(DEFAULT_SOUND | DEFAULT_VIBRATE)
				.setPriority(NotificationManager.IMPORTANCE_HIGH);
		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		CharSequence name = Utils.getAppName(context) + " notifications";
		NotificationChannel mChannel = new NotificationChannel(channelId, name, importance);
		mChannel.setShowBadge(true);
		mChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
		notificationManager.createNotificationChannel(mChannel);
		notificationManager.notify(reqCode, notificationBuilder.build());

		Log.d("showNotification", "showNotification: " + reqCode);
	}

	public static void notifyError(Context context, Throwable e,
								   String message, int reqCode) {
		// Log.e(TAG, "notifyError[" + code + "]: " + message);
		String reason = "Unknown";
		if (e != null) {
			e.printStackTrace();
			reason = e.toString();
		}
		Toast.makeText(context, "[" + reqCode + "] " + message, Toast.LENGTH_SHORT)
				.show();

		if (loggable(context, LogLevel.Ridiculous)) {
			Date date = new Date();
			SimpleDateFormat formatter = new SimpleDateFormat(
					"EEEE, dd MMMM yyyy hh:mm");
			String dateString = formatter.format(date);

			String errorMessage = "Date: " + dateString + "\n" + message + "["
					+ reqCode + "]" + " Reason " + reason + ".\nTrace:\n"
					+ (e == null ? getTrace() : getTrace(e));

			makeNotification(context, "Notification", errorMessage, reqCode, NotificationManager.IMPORTANCE_HIGH);
		}
	}

	public static void makeNotification(Context context, String title,
										String message, int reqCode, int importance) {
		Date date = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat(
				"EEEE, dd MMMM yyyy hh:mm.SSS");
		String dateString = formatter.format(date);

		String notificationMessage = "Date: " + dateString;
		if (reqCode == NOTIFICATION_DEBUG
				&& loggable(context, LogLevel.Ridiculous)) {
			String errorMessage = message + "[" + reqCode + "].\nTrace:\n"
					+ getTrace();

			notificationMessage += "\n" + errorMessage;
		} else {
			notificationMessage += "\n" + message;
		}

		Intent intent = new Intent(context, MainActivity.class);
		showNotification(context, title, notificationMessage, intent, reqCode, importance);
	}

	public static void makeAlertNotification(Context context, String from,
											 String message, int reqCode, int importance) {
		Date date = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat(
				"EEEE, dd MMMM yyyy hh:mm");
		String dateString = formatter.format(date);

		Intent showAlert = new Intent(context, AlertDisplay.class);
		showAlert.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP
				| Intent.FLAG_ACTIVITY_CLEAR_TOP);
		showAlert.putExtra("From", from);
		showAlert.putExtra("Message", message);

		CharSequence msg = dateString + ": " + message;

		Intent intent = new Intent(context, AlertDisplay.class);
		showNotification(context, from, message, intent, reqCode, importance);
	}

	public static NotificationCompat.Builder createServiceNotification(Context context, int id,
																	   String title,
																	   String message,
																	   String channelId,
																	   String channelName) {
		Intent intent = new Intent(context, AlertDisplay.class);
		intent.putExtra("From", title);
		intent.putExtra("Message", message);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 99, intent,
				PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		createNotificationChannel(notificationManager, channelId, channelName);
		NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, channelId);
		//@SuppressLint("ResourceAsColor") Notification notification =
		notificationBuilder
				.setSmallIcon(android.R.drawable.ic_btn_speak_now) //R.mipmap.ic_launcher) //R.drawable.icon)
				//.setSmallIcon(R.mipmap.ic_launcher)
				.setOngoing(true)
				//.setColor(R.color.red)
				.setContentTitle(title)
				.setContentText(message)
				.setPriority(PRIORITY_HIGH)
				.setContentIntent(pendingIntent)
				.setCategory(NotificationCompat.CATEGORY_SERVICE);
		return notificationBuilder;
	}

	private static void createNotificationChannel(NotificationManager notificationManager,
												  String channelId,
												  String channelName){
		NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
		// omitted the LED color
		channel.setImportance(NotificationManager.IMPORTANCE_HIGH);
		channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
		channel.enableLights(true);
		channel.enableVibration(true);
		notificationManager.createNotificationChannel(channel);
	}

	public static boolean loggable(Context context, LogLevel level) {
		if (level == LogLevel.None)
			return false;
		if (level == LogLevel.Error || level == LogLevel.Warning || level == LogLevel.Info)
			return true;

		String logLevelString = (String)prefs.retrieveFromPreferences("LogLevel");
		LogLevel savedLevel = logLevelString == null ? DEFAULT_LOG_LEVEL : LogLevel.valueOf(logLevelString);
		return savedLevel.ordinal() >= level.ordinal();
	}

	public static String getTrace() {
		StringBuilder traceString = new StringBuilder();
		StackTraceElement[] trace = Thread.currentThread().getStackTrace();
		for (StackTraceElement traceElement : trace) {
			String traceLine = traceElement.getClassName() + " at "
					+ traceElement.getMethodName() + "("
					+ traceElement.getFileName() + ":"
					+ traceElement.getLineNumber() + ")";
			traceString.append(traceLine).append("\n");
		}
		return traceString.toString();
	}

	public static String getTrace(Throwable e) {
		StringBuilder errorMessage = new StringBuilder();
		StackTraceElement[] trace = e.getStackTrace();
		for (StackTraceElement traceElement : trace) {
			String traceLine = traceElement.getClassName() + " at "
					+ traceElement.getMethodName() + "("
					+ traceElement.getFileName() + ":"
					+ traceElement.getLineNumber() + ")";
			errorMessage.append(traceLine).append("\n");
		}

		return errorMessage.toString();
	}

}