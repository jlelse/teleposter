package telegra.ph

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import im.delight.android.webview.AdvancedWebView

class MainActivity : AppCompatActivity(), AdvancedWebView.Listener {

	private val TELEGRAPH = "http://telegra.ph/"

	private val webView: AdvancedWebView? by lazy { findViewById(R.id.webView) as AdvancedWebView }

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		webView?.setListener(this, this)

		webView?.apply {
			setMixedContentAllowed(true)
			setCookiesEnabled(true)
			setThirdPartyCookiesEnabled(true)
			addPermittedHostname("telegra.ph")
		}

		webView?.settings?.apply {

		}

		// Check if app is opened to show special page
		var urlToLoad = TELEGRAPH
		if (intent.action == Intent.ACTION_VIEW && !intent.dataString.isNullOrBlank() && intent.dataString.contains("telegra.ph"))
			urlToLoad = intent.dataString

		// Load URL
		webView?.loadUrl(urlToLoad)
	}

	override fun onPageFinished(url: String?) {
	}

	override fun onPageStarted(url: String?, favicon: Bitmap?) {
	}

	override fun onPageError(errorCode: Int, description: String?, failingUrl: String?) {
	}

	override fun onDownloadRequested(url: String?, suggestedFilename: String?, mimeType: String?, contentLength: Long, contentDisposition: String?, userAgent: String?) {
	}

	override fun onExternalPageRequest(url: String?) {
		AdvancedWebView.Browsers.openUrl(this, url)
	}

	override fun onResume() {
		super.onResume()
		webView?.onResume()
	}

	override fun onPause() {
		webView?.onPause()
		super.onPause()
	}

	override fun onDestroy() {
		webView?.onDestroy()
		super.onDestroy()
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		webView?.onActivityResult(requestCode, resultCode, data)
	}

	override fun onCreateOptionsMenu(menu: Menu): Boolean {
		super.onCreateOptionsMenu(menu)
		menuInflater.inflate(R.menu.activity_main, menu)
		return true
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		return when (item.itemId) {
			R.id.share -> {
				val shareIntent = Intent()
				shareIntent.action = Intent.ACTION_SEND
				shareIntent.type = "text/plain"
				shareIntent.putExtra(Intent.EXTRA_TITLE, webView?.title)
				shareIntent.putExtra(Intent.EXTRA_TEXT, webView?.url)
				startActivity(Intent.createChooser(shareIntent, getString(R.string.share)))
				true
			}
			else -> super.onOptionsItemSelected(item)
		}
	}

}
