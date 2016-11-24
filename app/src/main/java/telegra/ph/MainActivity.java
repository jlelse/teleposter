package telegra.ph;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.*;

public class MainActivity extends AppCompatActivity {

	private WebView webView;
	private WebViewClient webViewClient;
	private WebChromeClient webChromeClient;

	private static final String TELEGRAPH = "http://telegra.ph/";

	@SuppressLint("SetJavaScriptEnabled")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		webView = (WebView) findViewById(R.id.webView);
		WebSettings webSettings = webView.getSettings();

		// Enable Javascript
		webSettings.setJavaScriptEnabled(true);
		// Allow File Access
		webSettings.setAllowFileAccess(true);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			webSettings.setAllowFileAccessFromFileURLs(true);
		}
		// Add Database support
		webSettings.setDatabaseEnabled(true);
		webSettings.setDomStorageEnabled(true);
		// Add Cache support
		webSettings.setAppCacheEnabled(true);

		// Set WebViewClient
		webViewClient = new WebViewClient() {
			@SuppressWarnings("deprecation")
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				return urlAllowed(url);
			}

			@TargetApi(Build.VERSION_CODES.N)
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
				return urlAllowed(request.getUrl().toString());
			}
		};
		webView.setWebViewClient(webViewClient);

		// Set WebChromeClient
		webChromeClient = new WebChromeClient() {
		};
		webView.setWebChromeClient(webChromeClient);

		// Check if app is opened to show special page
		String urlToLoad = TELEGRAPH;
		if (getIntent() != null && getIntent().getAction().equals(Intent.ACTION_VIEW) && getIntent().getDataString() != null && getIntent().getDataString().contains("telegra.ph"))
			urlToLoad = getIntent().getDataString();

		// Load URL
		webView.loadUrl(urlToLoad);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.share:
				Intent shareIntent = new Intent();
				shareIntent.setAction(Intent.ACTION_SEND);
				shareIntent.setType("text/plain");
				shareIntent.putExtra(Intent.EXTRA_TITLE, webView.getTitle());
				shareIntent.putExtra(Intent.EXTRA_TEXT, webView.getUrl());
				startActivity(Intent.createChooser(shareIntent, getString(R.string.share)));
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	// Extra methods
	private boolean urlAllowed(String url) {
		return url.contains("telegra.ph");
	}

}
