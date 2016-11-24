package telegra.ph;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends AppCompatActivity {

	private WebView webView;
	private WebSettings webSettings;

	@SuppressLint("SetJavaScriptEnabled")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);


		webView = (WebView) findViewById(R.id.webView);
		webSettings = webView.getSettings();

		// Enable Javascript
		webSettings.setJavaScriptEnabled(true);

		// Set WebViewClient
		webView.setWebViewClient(new WebViewClient() {
			@SuppressWarnings("deprecation")
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				return handleUrl(url);
			}

			@TargetApi(Build.VERSION_CODES.N)
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
				return handleUrl(request.getUrl().toString());
			}
		});

		// Set WebChromeClient
		webView.setWebChromeClient(new WebChromeClient() {

		});

		// Load Telegra.ph
		webView.loadUrl("http://telegra.ph");

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

	// Own methods
	private boolean handleUrl(String url) {
		return url.contains("telegra.ph");
	}

}
