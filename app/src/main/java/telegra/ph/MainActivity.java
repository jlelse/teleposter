package telegra.ph;

import android.content.Intent;
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);


		webView = (WebView) findViewById(R.id.webView);
		webSettings = webView.getSettings();

		// Enable Javascript
		webSettings.setJavaScriptEnabled(true);

		webView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
				return true;
			}
		});

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
}
