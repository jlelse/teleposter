package telegra.ph

import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import com.afollestad.materialdialogs.MaterialDialog
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

	override fun onBackPressed() {
		if (webView?.onBackPressed() == false) return
		else super.onBackPressed()
	}

	override fun onActionModeStarted(mode: ActionMode?) {
		val menu = mode?.menu
		mode?.menuInflater?.inflate(R.menu.formatting, menu)
		menu?.findItem(R.id.format)?.apply {
			setOnMenuItemClickListener {
				executeJavaScript("javascript:showFormatTooltip();")
				false
			}
		}
		super.onActionModeStarted(mode)
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
			R.id.help -> {
				MaterialDialog.Builder(this)
						.title(R.string.help)
						.content(R.string.help_text)
						.show()
				true
			}
			else -> super.onOptionsItemSelected(item)
		}
	}

	private fun executeJavaScript(code: String) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			webView?.evaluateJavascript(code, null)
		} else {
			webView?.loadUrl(code)
		}
	}

}
