package com.gncbrown.GetMeBack;

import static android.text.Html.FROM_HTML_MODE_COMPACT;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.gncbrown.GetMeBack.Utilities.Prefs;
import com.gncbrown.GetMeBack.Utilities.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HelpDisplay extends Activity {
	private static final String TAG = HelpDisplay.class.getSimpleName();

	private String helpType = "help";

	private static final int permissionsCode = 42;
	private static Context context;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

//		Log.d(TAG, "onCreate:HelpDisplay");
		context = MainActivity.context;
		try {
			helpType = getIntent().getStringExtra("type");
		} catch (Exception e) {
			helpType = "help";
		}
//		Log.d(TAG, "help type=" + helpType);
		setContentView(R.layout.help_display);

		TextView helpText = findViewById(R.id.helpText);
		Context context = helpText.getContext();


		Button requestPermission = findViewById(R.id.buttonRequestPermission);
		requestPermission.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (helpType.equals("welcome")) {
					if (!Utils.hasPermissions(MainActivity.requiredPermissions, context))
							ActivityCompat.requestPermissions(HelpDisplay.this, MainActivity.requiredPermissions, permissionsCode);
					else
						Toast.makeText(getApplicationContext(), "Permissions already granted.", Toast.LENGTH_SHORT).show();

					//////Prefs.saveFirstTimeToPreference(false);
					finish();
				}
			}
		});
		if (!helpType.equals("welcome") || Utils.hasPermissions(MainActivity.requiredPermissions, context))
			requestPermission.setVisibility(View.GONE);

		String releaseNotes = "";
		try {
			// Programmatically load text from an asset and place it into the
			// text view. Note that the text we are loading is ASCII, so we
			// need to convert it to UTF-16.
			InputStream is;
			if (helpType.equals("notification")) {
				String title = getIntent().getStringExtra("title");
				String message = getIntent().getStringExtra("message");
				releaseNotes = title + "\n\n" + message;
			} else if (helpType.equals("changeLog")) {
				releaseNotes = "<h1>Change Log</h1><br/><br/>";
				List<String> assets = Arrays.asList(getAssets().list(""));
				Collections.sort(assets,  Collections.reverseOrder());
				for (String asset : assets) {
					if (asset.contains("release_") && asset.endsWith(".txt")) {
//						Log.d(TAG, "asset=" + asset);
						is = getAssets().open(asset);

						// We guarantee that the available method returns the
						// total size of the asset... of course, this does
						// mean that a single asset can't be more than 2 gigs.
						int size = is.available();

						// Read the entire asset into a local byte buffer.
						byte[] buffer = new byte[size];
						is.read(buffer);
						is.close();

						// Convert the buffer into a string.
						releaseNotes += "<i>" + asset.replaceFirst(".txt", "") + "</i><br/>----------------------<br/>"
								+ new String(buffer) + "<br/><br/>";
					}
				}
			} else {
				String helpFileName = "help_text.html";
				if (helpType.equals("welcome"))
					helpFileName = "welcome.html";
				else if (helpType.equals("product_summary"))
					helpFileName = "product_summary.html";

				is = getAssets().open(helpFileName);

				// We guarantee that the available method returns the total
				// size of the asset... of course, this does mean that a single
				// asset can't be more than 2 gigs.
				int size = is.available();

				// Read the entire asset into a local byte buffer.
				byte[] buffer = new byte[size];
				is.read(buffer);
				is.close();

				// Convert the buffer into a string.
				releaseNotes = new String(buffer);
			}

		} catch (IOException e) {
			// Should never happen!
			releaseNotes = e.toString();
			//throw new RuntimeException(e);
		}

		String newlinesReplaced = releaseNotes.replaceAll("\n", "<br>\n");
		String newReleaseNotes = newlinesReplaced;

		String regex = "<img src=\"(.*?\\.png)\">";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(newlinesReplaced);
		while (matcher.find()) {
			String imgString = matcher.group();
			String icon = imgString.replaceFirst(".*?\"", "")
					.replaceFirst("\".*?>", "")
					.replaceFirst("\\..*", "");
			int resId = this.getResources().getIdentifier(icon, "drawable", this.getPackageName());
			newReleaseNotes = newReleaseNotes.replaceFirst(String.format("<img src=\"%s.png\">", icon),
					String.format("<img src=\"%s\"/>", resId));
		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			helpText.setText(Html.fromHtml(newReleaseNotes,  FROM_HTML_MODE_COMPACT, new Html.ImageGetter() {
				@Override
				public Drawable getDrawable(final String source) {
					Drawable d = null;
					try {
						d = getResources().getDrawable(Integer.parseInt(source));
						d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
					} catch (Resources.NotFoundException e) {
						Log.e(TAG, "Image not found. Check the ID.", e);
					} catch (NumberFormatException e) {
						Log.e(TAG, "Source string not a valid resource ID.", e);
					}

					return d;
				}
			}, null));
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
	}

}