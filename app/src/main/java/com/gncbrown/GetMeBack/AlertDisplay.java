package com.gncbrown.GetMeBack;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import com.gncbrown.GetMeBack.Utilities.Utils;

public class AlertDisplay extends Activity {
	private static final String TAG = AlertDisplay.class.getSimpleName();

	private Context context;

	private WebView mWebView;

	ProgressDialog progress = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		context = getBaseContext();

		String from;
		String message;
		try {
			Intent x = getIntent();
			from = getIntent().getStringExtra("From");
			message = getIntent().getStringExtra("Message");
		} catch (Exception e) {
			e.printStackTrace();
			from = "FROM";
			message = "MESSAGE";
		}
		if (from == null)
			from = "No title found";
		if (message == null)
			message = "No message found";

		setContentView(R.layout.notification_display);

		mWebView = findViewById(R.id.alertWebView);
		mWebView.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageFinished(WebView view, String url) {
				// Inject CSS on PageFinished
				injectCSS();
				super.onPageFinished(view, url);
			}
		});
		registerForContextMenu(mWebView);
		mWebView.setBackgroundColor(Utils.isDark(context) ? Color.BLACK : Color.WHITE);

		String updatedMessage = message.replaceAll("\n", "<br/>\n");
		setTitle(from + " Notification");
		mWebView.loadData(String.format("<font color='%s'>",
						Utils.isDark(context) ? "white" : "black") + updatedMessage + "</font>",
				"text/html", "UTF-8");

		Button mPreferencesButton = findViewById(R.id.preferencesButton);
		mPreferencesButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// Log.d(TAG, "clicked preferences");
				Intent showMain = new Intent(getApplicationContext(), MainActivity.class);
				showMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(showMain);
			}
		});

		Button mClearNotificationsButton = findViewById(R.id.clearNotificationsButton);
		mClearNotificationsButton
				.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						// Log.d(TAG,
						// "clicked clearNotificationsButton");

						NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
						mNotificationManager.cancelAll();
					}
				});
	}

	@Override protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
	}


	private void injectCSS() {
		mWebView.loadUrl("javascript:document.body.style.color=\""
				+ (!Utils.isDark(context) ? "white" : "black")
				+ "\";");
		mWebView.loadUrl("javascript:document.body.style.setProperty(\"color\", "
				+ (Utils.isDark(context) ? "white" : "black")
				+ "\";");

//		mWebView.loadUrl("javascript:document.body.style.color=\"black\";");
//		mWebView.loadUrl(
//				"javascript:document.body.style.setProperty(\"color\", \"black\");"
//		);
	}

}