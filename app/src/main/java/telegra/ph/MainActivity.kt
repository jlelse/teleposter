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
	private val webView: AdvancedWebView? by lazy { findViewById(R.id.webView) as AdvancedWebView? }
	private val editor: Editor? by lazy { findViewById(R.id.editor) as Editor? }

	private var currentUrl = ""
	private var currentPage: Page? = null
	private var editorMode = true
	private var canEdit = false
	private var isEdit = false

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
		if (accessToken().isNullOrBlank()) Api().createAccount { accessToken ->
			if (accessToken != null) saveAccessToken(accessToken)
		}
		if (intent.action == Intent.ACTION_VIEW && !intent.dataString.isNullOrBlank() && intent.dataString.contains("telegra.ph")) loadPage(intent.dataString.split("/").last())
		else loadEditor()
	}

	private fun loadEditor(path: String? = null) {
		runOnUiThread {
			editorMode = true
			canEdit = false
			isEdit = false
			invalidateOptionsMenu()
			editor?.visibility = View.VISIBLE
			webView?.visibility = View.GONE
			currentPage = null
			// Load
			if (path != null) Api().getPage(path, accessToken()) { success, page ->
				if (success) runOnUiThread {
					isEdit = true
					currentPage = page
					editor?.setText(page?.content ?: "")
				}
				else showError()
			}
		}
	}

	private fun loadPage(path: String) {
		runOnUiThread {
			editorMode = false
			canEdit = false
			invalidateOptionsMenu()
			webView?.visibility = View.VISIBLE
			editor?.visibility = View.GONE
			currentPage = null
			// Load
			Api().getPage(path, accessToken()) { success, page ->
				if (success) showPage(page)
				else showError()
			}
		}
	}

	private fun showPage(page: Page?) {
		runOnUiThread {
			editorMode = false
			canEdit = page?.can_edit ?: false
			invalidateOptionsMenu()
			webView?.visibility = View.VISIBLE
			editor?.visibility = View.GONE
			currentPage = page
			// Show
			page?.let {
				var html = getString(R.string.viewer_html_head)
				html += "<h1>${it.title}</h1>"
				if (!it.author_name.isNullOrEmpty() && !it.author_url.isNullOrBlank()) html += "<a href=\"${it.author_url}\">${it.author_name}</a><br>"
				else if (!it.author_name.isNullOrEmpty()) html += "${it.author_name}<br>"
				if (it.views != 0) html += "${it.views} times viewed<br><br>"
				if (it.content.isNullOrBlank()) html += it.description.replace("\n", "<br>") else html += it.content
				html += getString(R.string.viewer_html_end)
				webView?.loadDataWithBaseURL(it.url, html, "text/html; charset=UTF-8", null, null)
				currentUrl = it.url
			}
		}
	}

	private fun showError() {
		runOnUiThread {
			MaterialDialog.Builder(this)
					.title(R.string.error)
					.content(R.string.error_desc)
					.positiveText(android.R.string.ok)
					.show()
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

	override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
		super.onPrepareOptionsMenu(menu)
		menu?.findItem(R.id.create)?.isVisible = !editorMode
		menu?.findItem(R.id.share)?.isVisible = !editorMode
		menu?.findItem(R.id.try_edit)?.isVisible = !editorMode && !canEdit
		menu?.findItem(R.id.publish)?.isVisible = editorMode
		menu?.findItem(R.id.edit)?.isVisible = canEdit
		return true
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		return when (item.itemId) {
			R.id.create -> {
				loadEditor()
				true
			}
			R.id.publish -> {
				editor?.getText { json ->
					MaterialDialog.Builder(this)
							.title(R.string.title_question)
							.input(getString(R.string.title_hint), currentPage?.title ?: "", { dialog, title ->
								if (isEdit) Api().editPage(accessToken(), currentPage?.path, json, title.toString()) { success, page ->
									if (success) showPage(page)
									else showError()
								}
								else Api().createPage(accessToken(), json, title.toString()) { success, page ->
									if (success) showPage(page)
									else showError()
								}
							})
							.show()
				}
				true
			}
			R.id.edit, R.id.try_edit -> {
				loadEditor(currentPage?.path)
				true
			}
			R.id.bookmarks -> {
				MaterialDialog.Builder(this)
						.title(R.string.bookmarks)
						.items(bookmarks().reversed().map { it.split("xxx;xxx")[1] })
						.itemsCallback { materialDialog, view, i, charSequence ->
							loadPage(bookmarks().reversed().map { it.split("xxx;xxx")[0] }[i])
						}
						.itemsLongCallback { materialDialog, view, i, charSequence ->
							MaterialDialog.Builder(this)
									.title(R.string.delete)
									.content(R.string.delete_question)
									.positiveText(android.R.string.yes)
									.negativeText(android.R.string.no)
									.onPositive { materialDialog, dialogAction ->
										deleteBookmark(bookmarks().reversed().map { it.split("xxx;xxx")[0] }[i])
									}
									.show()
							true
						}
						.show()
				true
			}
			R.id.bookmark -> {
				MaterialDialog.Builder(this)
						.title(R.string.title_question)
						.input(getString(R.string.title_hint), "", { dialog, input ->
							addBookmark("${(if (webView?.url != "about:blank") webView?.url ?: currentUrl else currentUrl).split("/").last()}xxx;xxx$input")
						})
						.show()
				true
			}
			R.id.share -> {
				val shareIntent = Intent()
				shareIntent.action = Intent.ACTION_SEND
				shareIntent.type = "text/plain"
				shareIntent.putExtra(Intent.EXTRA_TITLE, webView?.title)
				shareIntent.putExtra(Intent.EXTRA_TEXT, currentUrl)
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
