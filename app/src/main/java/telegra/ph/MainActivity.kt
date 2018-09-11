package telegra.ph

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import im.delight.android.webview.AdvancedWebView
import java.net.URI

class MainActivity : AppCompatActivity(), AdvancedWebView.Listener {
	private val viewer: Viewer? by lazy {
		findViewById<Viewer?>(R.id.viewer)?.apply {
			setListener(this@MainActivity, this@MainActivity)
		}
	}
	private val editor: Editor? by lazy {
		findViewById<Editor?>(R.id.editor)?.apply {
			setListener(this@MainActivity, this@MainActivity)
		}
	}

	private var currentPage: TelegraphApi.Page? = null
	private var editorMode = true
	private var canEdit = false
	private var isEdit = false

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)
		if (accessToken.isBlank()) TelegraphApi.createAccount(shortName = "teleposter") { success, account, error ->
			if (success && account != null && account.accessToken != null) {
				accessToken = account.accessToken
			} else {
				showError(error)
			}
		}
		if (intent.action == Intent.ACTION_VIEW) {
			val uri = URI.create(intent.dataString)
			when (uri.host) {
				"telegra.ph", "graph.org" -> loadPage(uri.path)
				"edit.telegra.ph", "edit.graph.org" -> login(uri.toString())
			}
		}
	}

	private fun loadEditor(path: String? = null) {
		runOnUiThread {
			editorMode = true
			canEdit = false
			isEdit = false
			invalidateOptionsMenu()
			editor?.visibility = View.VISIBLE
			viewer?.visibility = View.GONE
			currentPage = null
			// Load
			if (path != null) TelegraphApi.getPage(accessToken, path, true) { success, page, error ->
				if (success && page != null) {
					isEdit = true
					currentPage = page
					editor?.setContent(page.content)
				} else {
					showError(error)
				}
			} else {
				// Reset
				editor?.reset()
			}
		}
	}

	private fun login(authUrl: String) {
		TelegraphApi.login(authUrl) { success, accessToken, account ->
			if (success && accessToken != null) {
				this.accessToken = accessToken
				this.authorName = account?.authorName
				showMessage(getString(R.string.success), getString(R.string.login_success))
			} else showError(getString(R.string.login_failed))
		}
	}

	private fun loadPage(path: String) {
		runOnUiThread {
			editorMode = false
			canEdit = false
			invalidateOptionsMenu()
			viewer?.visibility = View.VISIBLE
			editor?.visibility = View.GONE
			currentPage = null
			// Load
			TelegraphApi.getPage(accessToken, path, true) { success, page, error ->
				if (success && page != null) showPage(page)
				else showError(error)
			}
		}
	}

	private fun showPage(page: TelegraphApi.Page?) {
		runOnUiThread {
			editorMode = false
			canEdit = page?.canEdit ?: false
			invalidateOptionsMenu()
			viewer?.visibility = View.VISIBLE
			editor?.visibility = View.GONE
			currentPage = page
			viewer?.clearHistory()
			// Show
			page?.let {
				viewer?.setArticleTitle(it.title)
				viewer?.setAuthor(it.authorName, it.authorUrl)
				viewer?.setViews(it.views)
				if (it.content == null) viewer?.setDescription(it.description)
				else viewer?.setContent(it.content)
			}
		}
	}

	private fun showError(message: String? = null) = showMessage(getString(R.string.error), message
			?: getString(R.string.error_desc))

	private fun showMessage(title: String? = null, message: String? = null) {
		runOnUiThread {
			MaterialDialog(this)
					.title(text = title ?: "")
					.message(text = message ?: "")
					.positiveButton(android.R.string.ok)
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

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		editor?.onActivityResult(requestCode, resultCode, data)
	}

	override fun onBackPressed() {
		if (viewer?.onBackPressed() == false) return
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
			R.id.create -> {
				loadEditor()
				true
			}
			R.id.publish -> {
				editor?.getText { json ->
					MaterialDialog(this)
							.title(R.string.title_question)
							.input(hintRes = R.string.title_hint, prefill = currentPage?.title
									?: "") { _, title ->
								MaterialDialog(this)
										.title(R.string.name_question)
										.input(hintRes = R.string.name_hint, prefill = if (isEdit) currentPage?.authorName
												?: authorName ?: "" else authorName
												?: "") { _, name ->
											if (!isEdit) authorName = name.toString()
											if (isEdit) TelegraphApi.editPage(accessToken, currentPage?.path
													?: "", authorName = name.toString(), title = title.toString(), content = json
													?: "", returnContent = true) { success, page, error ->
												if (success && page != null) showPage(page)
												else showError(error)
											} else TelegraphApi.createPage(accessToken, content = json
													?: "", title = title.toString(), authorName = name.toString(), returnContent = true) { success, page, error ->
												if (success && page != null) showPage(page)
												else showError(error)
											}
										}
										.show()
							}
							.show()
				}
				true
			}
			R.id.edit, R.id.try_edit -> {
				loadEditor(currentPage?.path)
				true
			}
			R.id.bookmarks -> {
				MaterialDialog(this)
						.title(R.string.bookmarks)
						.positiveButton(R.string.open)
						.negativeButton(android.R.string.cancel)
						.listItemsSingleChoice(items = bookmarks().reversed().map { it.second }) { _, index, _ ->
							loadPage(bookmarks().reversed().map { it.first }[index])
						}
						.show()
				true
			}
			R.id.delete_bookmark -> {
				MaterialDialog(this)
						.title(R.string.delete_bookmark)
						.positiveButton(R.string.delete)
						.negativeButton(android.R.string.cancel)
						.listItemsSingleChoice(items = bookmarks().reversed().map { it.second }) { _, index, _ ->
							MaterialDialog(this)
									.title(R.string.delete)
									.message(R.string.delete_question)
									.positiveButton(android.R.string.yes)
									.negativeButton(android.R.string.no)
									.positiveButton { _ ->
										deleteBookmark(bookmarks().reversed().map { it.first }[index])
									}
									.show()
						}
						.show()
				true
			}
			R.id.published -> {
				TelegraphApi.getPageList(accessToken) { success, pageList, error ->
					if (success && pageList != null && pageList.pages != null) {
						MaterialDialog(this)
								.title(R.string.published)
								.positiveButton(R.string.open)
								.negativeButton(android.R.string.cancel)
								.listItemsSingleChoice(items = pageList.pages.map { it.title }) { _, i, _ ->
									loadPage(pageList.pages[i].path)
								}
								.show()
					} else showError(error)
				}
				true
			}
			R.id.bookmark -> {
				MaterialDialog(this)
						.title(R.string.title_question)
						.input(hintRes = R.string.title_hint, prefill = currentPage?.title
								?: "") { _, input ->
							val curPage = currentPage
							if (curPage?.url != null) addBookmark(curPage.url.split("/").last(), input.toString())
						}
						.show()
				true
			}
			R.id.share -> {
				val shareIntent = Intent()
				shareIntent.action = Intent.ACTION_SEND
				shareIntent.type = "text/plain"
				shareIntent.putExtra(Intent.EXTRA_TITLE, currentPage?.title)
				shareIntent.putExtra(Intent.EXTRA_TEXT, currentPage?.url)
				startActivity(Intent.createChooser(shareIntent, getString(R.string.share)))
				true
			}
			R.id.help -> {
				MaterialDialog(this)
						.title(R.string.help)
						.message(R.string.help_text)
						.positiveButton(android.R.string.ok)
						.show()
				true
			}
			R.id.login -> {
				MaterialDialog(this)
						.title(R.string.login)
						.message(R.string.login_desc)
						.positiveButton(android.R.string.ok)
						.positiveButton { _ ->
							startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/telegraph")))
						}
						.show()
				true
			}
			else -> super.onOptionsItemSelected(item)
		}
	}
}
