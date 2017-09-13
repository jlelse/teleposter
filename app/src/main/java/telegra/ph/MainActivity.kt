package telegra.ph

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.folderselector.FileChooserDialog
import im.delight.android.webview.AdvancedWebView
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import java.io.File

class MainActivity : AppCompatActivity(), AdvancedWebView.Listener, FileChooserDialog.FileCallback {
	private val webView: AdvancedWebView? by lazy { findViewById<AdvancedWebView?>(R.id.webView) }
	private val editor: Editor? by lazy { findViewById<Editor?>(R.id.editor) }

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
		if (accessToken().isNullOrBlank()) Api.createAccount { accessToken ->
			if (accessToken != null) saveAccessToken(accessToken)
		}
		if (intent.action == Intent.ACTION_VIEW && intent.dataString.contains("telegra.ph")) loadPage(intent.dataString.split("/").last())
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
			if (path != null) Api.getPage(path, accessToken()) { success, page ->
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
			Api.getPage(path, accessToken()) { success, page ->
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
			webView?.clearHistory()
			// Show
			page?.let {
				var html = getString(R.string.viewer_html_head)
				html += "<h1>${it.title}</h1>"
				if (!it.author_name.isEmpty() && !it.author_url.isBlank()) html += "<a href=\"${it.author_url}\">${it.author_name}</a><br>"
				else if (!it.author_name.isEmpty()) html += "${it.author_name}<br>"
				if (it.views != 0) html += "${it.views} times viewed<br><br>"
				if (it.content.isBlank()) html += it.description.replace("\n", "<br>") else html += it.content
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

	@AfterPermissionGranted(100)
	private fun uploadImage() {
		if (EasyPermissions.hasPermissions(this, "android.permission.READ_EXTERNAL_STORAGE")) {
			FileChooserDialog.Builder(this)
					.mimeType("image/*")
					.show(this)
		} else {
			EasyPermissions.requestPermissions(this, "", 100, "android.permission.READ_EXTERNAL_STORAGE")
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

	override fun onFileSelection(p0: FileChooserDialog, file: File) {
		Api.uploadImage(file) { src ->
			if (src != null) editor?.addImage(src)
		}
	}

	override fun onFileChooserDismissed(p0: FileChooserDialog) {
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
		menu?.findItem(R.id.image)?.isVisible = editorMode
		menu?.findItem(R.id.publish)?.isVisible = editorMode
		menu?.findItem(R.id.edit)?.isVisible = canEdit
		return true
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		return when (item.itemId) {
			R.id.image -> {
				uploadImage()
				true
			}
			R.id.create -> {
				loadEditor()
				true
			}
			R.id.publish -> {
				editor?.getText { json ->
					MaterialDialog.Builder(this)
							.title(R.string.title_question)
							.input(getString(R.string.title_hint), currentPage?.title ?: "", { _, title ->
								MaterialDialog.Builder(this)
										.title(R.string.name_question)
										.input(getString(R.string.name_hint), if (isEdit) currentPage?.author_name ?: authorName() ?: "" else authorName() ?: "", { _, name ->
											if (!isEdit) saveAuthorName(name.toString())
											if (isEdit) Api.editPage(accessToken(), currentPage?.path, json, title.toString(), name.toString()) { success, page ->
												if (success) showPage(page)
												else showError()
											}
											else Api.createPage(accessToken(), json, title.toString(), name.toString()) { success, page ->
												if (success) showPage(page)
												else showError()
											}
										})
										.show()
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
						.positiveText(android.R.string.ok)
						.items(bookmarks().reversed().map { it.split("xxx;xxx")[1] })
						.itemsCallback { _, _, i, _ ->
							loadPage(bookmarks().reversed().map { it.split("xxx;xxx")[0] }[i])
						}
						.itemsLongCallback { _, _, i, _ ->
							MaterialDialog.Builder(this)
									.title(R.string.delete)
									.content(R.string.delete_question)
									.positiveText(android.R.string.yes)
									.negativeText(android.R.string.no)
									.onPositive { _, _ ->
										deleteBookmark(bookmarks().reversed().map { it.split("xxx;xxx")[0] }[i])
									}
									.show()
							true
						}
						.show()
				true
			}
			R.id.published -> {
				Api.getPageList(accessToken()) { success, result ->
					if (!success || result == null || result.isEmpty()) showError()
					else {
						MaterialDialog.Builder(this)
								.title(R.string.published)
								.positiveText(android.R.string.ok)
								.items(result.map(Page::title))
								.itemsCallback { _, _, i, _ ->
									loadPage(result.map(Page::path)[i])
								}
								.show()
					}
				}
				true
			}
			R.id.bookmark -> {
				MaterialDialog.Builder(this)
						.title(R.string.title_question)
						.input(getString(R.string.title_hint), "", { _, input ->
							addBookmark("${currentUrl.split("/").last()}xxx;xxx$input")
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
						.content(R.string.help_text, true)
						.positiveText(android.R.string.ok)
						.show()
				true
			}
			else -> super.onOptionsItemSelected(item)
		}
	}

	override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults)
		EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
	}
}
