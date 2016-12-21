package telegra.ph

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.afollestad.materialdialogs.MaterialDialog
import im.delight.android.webview.AdvancedWebView

class MainActivity : AppCompatActivity(), AdvancedWebView.Listener {

	private val TELEGRAPH = "http://telegra.ph/"
	private val htmlHead = "<!DOCTYPE html><html><head><meta charset=\"utf-8\"><meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\"><meta name=\"viewport\" content=\"width=device-width, initial-scale=1\"><link rel=\"stylesheet\" href=\"https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css\"><style> * { max-width: 100%; height: auto; word-break: break-all; word-break: break-word; }</style></head><body>"
	private val htmlEnd = "<script src=\"https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js\"></script></body></html>"

	private val webView: AdvancedWebView? by lazy { findViewById(R.id.webView) as AdvancedWebView }

	private var url = ""

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		webView?.apply {
			setListener(this@MainActivity, this@MainActivity)
			setMixedContentAllowed(true)
			setCookiesEnabled(true)
			setThirdPartyCookiesEnabled(true)
			addPermittedHostname("telegra.ph")
			isHorizontalScrollBarEnabled = false
			isVerticalScrollBarEnabled = false
			overScrollMode = View.OVER_SCROLL_NEVER
		}

		if (intent.action == Intent.ACTION_VIEW && !intent.dataString.isNullOrBlank() && intent.dataString.contains("telegra.ph")) {
			loadPage(intent.dataString.split("/").last())
		} else {
			webView?.loadUrl(TELEGRAPH)
		}
	}

	private fun loadPage(path: String) {
		Api().getPage(path) { page ->
			page?.let {
				var html = htmlHead
				html += "<h1>${it.title}</h1>"
				if (!it.author_name.isNullOrEmpty() && !it.author_url.isNullOrBlank()) html += "<a href=\"${it.author_url}\">${it.author_name}</a><br>"
				else if (!it.author_name.isNullOrEmpty()) html += "${it.author_name}<br>"
				if (it.views != 0) html += "${it.views} times viewed<br><br>"
				if (it.content.isNullOrBlank()) html += it.description.replace("\n", "<br>") else html += it.content
				html += htmlEnd
				webView?.loadDataWithBaseURL(it.url, html, "text/html; charset=UTF-8", null, null)
				url = it.url
				addToHistory("${it.path}xxx;xxx${it.title}")
			}
		}
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

	override fun onCreateOptionsMenu(menu: Menu): Boolean {
		super.onCreateOptionsMenu(menu)
		menuInflater.inflate(R.menu.activity_main, menu)
		return true
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		return when (item.itemId) {
			R.id.history -> {
				MaterialDialog.Builder(this)
						.title(R.string.history)
						.items(getHistory().reversed().map { it.split("xxx;xxx")[1] })
						.itemsCallback { materialDialog, view, i, charSequence ->
							loadPage(getHistory().reversed().map { it.split("xxx;xxx")[0] }[i])
						}
						.show()
				true
			}
			R.id.share -> {
				val shareIntent = Intent()
				shareIntent.action = Intent.ACTION_SEND
				shareIntent.type = "text/plain"
				shareIntent.putExtra(Intent.EXTRA_TITLE, webView?.title)
				shareIntent.putExtra(Intent.EXTRA_TEXT, if (webView?.url != "about:blank") webView?.url ?: url else url)
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

}
