package com.gncbrown.GetMeBack;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.gncbrown.GetMeBack.Utilities.LogEntry;
import com.gncbrown.GetMeBack.Utilities.MySQLiteHelper;
import com.gncbrown.GetMeBack.Utilities.Preferences;
import com.gncbrown.GetMeBack.Utilities.Utils;

import java.lang.reflect.Method;
import java.util.List;

public class LogDisplay extends AppCompatActivity {
	private static final String TAG = LogDisplay.class.getSimpleName();

	public static String NL = "<br>\n"; // "\n";

	private WebView mWebView;
	private LinearLayout findLayout;
	private LinearLayout findBoxLayout;
	private Context context = null;
	private String logContents;
	private EditText findBox;
	private String findString = null;

	private String title = "Log";
	private String type = "showLog";

	private static Preferences prefs;
	private static MySQLiteHelper dbHelper;

	public void onCreate(Bundle icicle) {
		context = getApplicationContext();
		prefs = new Preferences(context);
		dbHelper = MySQLiteHelper.getInstance(this);;

		setContentView(R.layout.activity_log);
		findLayout = findViewById(R.id.findLayout);

		findLayout.setVisibility(View.INVISIBLE);

		mWebView = findViewById(R.id.logText);
		mWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
		mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
		mWebView.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageFinished(WebView view, String url) {
				// Inject CSS on PageFinished
				injectCSS();
				super.onPageFinished(view, url);
			}
		});
		//mWebView.setBackgroundColor(Utils.isDark(context) ? Color.BLACK : Color.WHITE);
		registerForContextMenu(mWebView);

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

		try {
			Bundle bundle = getIntent().getExtras();
			type = bundle.getString("type", "showLog");
		} catch (Exception e) {
			type = "showLog";
		}

		switch (type) {
			case "showLog":
				//Log.d(TAG, "LogDisplay; reverse=" + false);
				prefs.saveToPreferences("ReverseLog", false);
				new LoadLogFile().execute();
				break;

			case "reverseLog":
				//Log.d(TAG, "LogDisplay; reverse=" + true);
				prefs.saveToPreferences("ReverseLog", true);
				new LoadLogFile().execute();
				break;

			case "deleteLog":
				//Log.d(TAG, "LogDisplay; deleteLog");
				dbHelper.clearLogEntries();
				new LoadLogFile().execute();
				break;

			default:
		}
		prefs.saveToPreferences("FilterLog", "");

		new LoadLogFile().execute();
		super.onCreate(icicle);
	}

	private void onApplyWindowInsets(View v, WindowInsetsCompat insets) {
		int topInset = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
		int bottomInset = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;
		v.setPadding(0, topInset+500, 0, 0);
	}

	public class LoadLogFile extends AsyncTask<Void, Void, Void> {
		private ProgressDialog progress = null;

		@Override
		protected Void doInBackground(Void... params) {
			boolean reverseLog = (boolean)prefs.retrieveFromPreferences("ReverseLog");
			String filter = (String)prefs.retrieveFromPreferences("FilterLog");
			int logEntries = 0;

			boolean colorize = true;
			List<LogEntry> entries = dbHelper.getEntriesAsLogEntries(reverseLog, filter, colorize);
			StringBuffer fancyLogContents = new StringBuffer();
			for (LogEntry e : entries) {
				String logEntry = e.fancyString(colorize);
				if ("".equals(filter) || logEntry.toLowerCase().contains(filter.toLowerCase())) {
					fancyLogContents.append(logEntry + NL);
					logEntries++;
				}
			}
			logContents = fancyLogContents.toString(); //newLogBuffer.toString();

			title = String.format( (reverseLog ? "Reversed " : "") +
					"Log [" + logEntries + " entries" +
					(!"".equals(filter) ? ", filter " + filter : "") + "]");
			return null;
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
		}

		@Override
		protected void onPostExecute(Void result) {
			setTitle(title);
			mWebView.loadData(String.format("<font color='%s'>",
							Utils.isDark(context) ? "white" : "black") + logContents + "</font>",
					"text/html", "UTF-8");
			progress.dismiss();
			super.onPostExecute(result);
		}

		@Override
		protected void onPreExecute() {
			progress = ProgressDialog.show(LogDisplay.this, null,
					"Loading log file...");
			super.onPreExecute();
		}

		@Override
		protected void onProgressUpdate(Void... values) {
			super.onProgressUpdate(values);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_log, menu);

		MenuCompat.setGroupDividerEnabled(menu, true);
		menu.setGroupDividerEnabled(true);

		return(super.onCreateOptionsMenu(menu));
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.copyLog) {
			ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
			clipboard.setText(logContents);
			return true;
		} else if (item.getItemId() == R.id.refreshLog) {
			prefs.saveToPreferences("ReverseLog", false);
			prefs.saveToPreferences("FilterLog", "");
			new LoadLogFile().execute();
			Log.d(TAG, "LogDisplay.onOptionsItemSelected; setText done.");
		} else if (item.getItemId() == R.id.reverseLog) {
			prefs.saveToPreferences("ReverseLog", true);
			prefs.saveToPreferences("FilterLog", "");
			new LoadLogFile().execute();
			Log.d(TAG, "LogDisplay.onOptionsItemSelected; setText done.");
		} else if (item.getItemId() == R.id.pruneLog) {
			int pruned = dbHelper.pruneLogEntries(500);

			new LoadLogFile().execute();

			Toast.makeText(context,
					Utils.getAppName(context) + " pruned " + pruned + " log entries.",
					Toast.LENGTH_SHORT).show();

			Log.d(TAG, "LogDisplay.onOptionsItemSelected; prune log done.");
		} else if (item.getItemId() == R.id.deleteLog) {
			logContents = "Log file reset";
			mWebView.loadData(logContents, "text/html", "UTF-8");
			dbHelper.clearLogEntries();
			new LoadLogFile().execute();
		} else if (item.getItemId() == R.id.topLog) {
			mWebView.scrollTo(0, 0);
		} else if (item.getItemId() == R.id.bottomLog) {
			int lines = logContents.split(System.getProperty("line.separator")).length;
			int height = mWebView.getContentHeight();
			Log.d(TAG, "scroll to bottom: lines=" + lines + ", height=" + height);
			mWebView.scrollTo(0, height*lines);
		} else if (item.getItemId() == R.id.searchLog) {
			search();
		} else if (item.getItemId() == R.id.filterLog) {
			filter();
		}
		return super.onOptionsItemSelected(item);
	}

	public static void setNotifyFromApp(Context context, String packageName,
										Boolean value) {
		SharedPreferences sprefs = context.getSharedPreferences(
				"AppNotifications", 0);
		SharedPreferences.Editor prefEditor = sprefs.edit();
		prefEditor.putBoolean(packageName, value);
		prefEditor.commit();
	}

	public void search() {
		findLayout = findViewById(R.id.findLayout);
		findBoxLayout = findViewById(R.id.findBox);
		findBoxLayout.setBackgroundColor(Color.WHITE);
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
		findLayout.setVisibility(View.VISIBLE);

		findString = null;

		findBox = findViewById(R.id.editTextFind);
		findBox.setText("");
		findBox.setHint("Find string");
		findBox.requestFocus();
		findBox.setOnKeyListener(new OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if ((event.getAction() == KeyEvent.ACTION_DOWN)
						&& ((keyCode == KeyEvent.KEYCODE_ENTER))) {
					findString = findBox.getText().toString();
					mWebView.findAll(findString);

					try {
						Method m = WebView.class.getMethod("setFindIsUp",
								Boolean.TYPE);
						m.invoke(mWebView, true);
					} catch (Exception ignored) {
						Log.e(TAG, "Exception in search(): " + ignored.getMessage());
					}
					return true;
				}
				return false;
			}
		});


		Button closeButton = findViewById(R.id.buttonCancel);
		closeButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				findLayout.setVisibility(View.INVISIBLE);
				findString = null;
			}
		});

		Button nextButton = findViewById(R.id.buttonNext);
		nextButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				findString = findBox.getText().toString();
				if (findString == null) {
					Toast.makeText(context,
							"Press ENTER in text field!", Toast.LENGTH_SHORT).show();
					findBox.requestFocus();
				} else {
					mWebView.findNext(true);
				}
			}
		});
	}

	public void filter() {
		findLayout = findViewById(R.id.findLayout);
		findBoxLayout = findViewById(R.id.findBox);
		findBoxLayout.setBackgroundColor(Color.WHITE);
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
		findLayout.setVisibility(View.VISIBLE);

		findString = null;

		findBox = findViewById(R.id.editTextFind);
		findBox.setText("");
		findBox.setHint("Filter string");
		findBox.requestFocus();
		findBox.setOnKeyListener(new OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if ((event.getAction() == KeyEvent.ACTION_DOWN)
						&& ((keyCode == KeyEvent.KEYCODE_ENTER))) {
					findString = findBox.getText().toString();
					//prefs.saveToPreferences("FilterLog", findString); // TODO save to preferences??
					new LoadLogFile().execute();
					return true;
				}
				return false;
			}
		});

		Button closeButton = findViewById(R.id.buttonCancel);
		closeButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				findLayout.setVisibility(View.INVISIBLE);
				findString = null;
				//prefs.saveToPreferences("FilterLog", findString); // TODO save to preferences??
			}
		});

		Button nextButton = findViewById(R.id.buttonNext);
		nextButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				findString = findBox.getText().toString();
				if (findString == null) {
					Toast.makeText(context,
							"Press ENTER in text field!", Toast.LENGTH_SHORT).show();
					findBox.requestFocus();
				} else {
					prefs.saveToPreferences("FilterLog", findString);
					new LoadLogFile().execute();
				}
			}
		});
	}

	private void injectCSS() {
		mWebView.loadUrl("javascript:document.body.style.color=\""
				+ (!Utils.isDark(context) ? "white" : "black")
				+ "\";");
		mWebView.loadUrl("javascript:document.body.style.setProperty(\"color\", "
				+ (Utils.isDark(context) ? "white" : "black")
				+ "\";");
	}

}