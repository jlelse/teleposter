package telegra.ph

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.webkit.*

class MainActivity : AppCompatActivity() {

	private val TELEGRAPH = "http://telegra.ph/"

	private val webView: WebView? by lazy { findViewById(R.id.webView) as WebView }

	@SuppressLint("SetJavaScriptEnabled")
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		webView?.settings?.apply {
			// Enable Javascript
			javaScriptEnabled = true
			// Allow File Access
			allowFileAccess = true
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
				allowFileAccessFromFileURLs = true
			}
			// Add Database support
			databaseEnabled = true
			domStorageEnabled = true
			// Add Cache support
			setAppCacheEnabled(true)
		}

		// Set WebViewClient
		webView?.setWebViewClient(object : WebViewClient() {
			@SuppressWarnings("deprecation")
			override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
				return urlAllowed(url)
			}

			@TargetApi(Build.VERSION_CODES.N)
			override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
				return urlAllowed(request.url.toString())
			}
		})

		// Set WebChromeClient
		webView?.setWebChromeClient(object : WebChromeClient() {

		})

		// Check if app is opened to show special page
		var urlToLoad = TELEGRAPH
		if (intent.action == Intent.ACTION_VIEW && !intent.dataString.isNullOrBlank() && intent.dataString.contains("telegra.ph"))
			urlToLoad = intent.dataString

		// Load URL
		webView?.loadUrl(urlToLoad)

	}

	override fun onCreateOptionsMenu(menu: Menu): Boolean {
		super.onCreateOptionsMenu(menu)
		menuInflater.inflate(R.menu.activity_main, menu)
		return true
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		when (item.itemId) {
			R.id.share -> {
				val shareIntent = Intent()
				shareIntent.action = Intent.ACTION_SEND
				shareIntent.type = "text/plain"
				shareIntent.putExtra(Intent.EXTRA_TITLE, webView?.title)
				shareIntent.putExtra(Intent.EXTRA_TEXT, webView?.url)
				startActivity(Intent.createChooser(shareIntent, getString(R.string.share)))
				return true
			}
			else -> return super.onOptionsItemSelected(item)
		}
	}

	// Extra methods
	private fun urlAllowed(url: String) = url.contains("telegra.ph")

}
