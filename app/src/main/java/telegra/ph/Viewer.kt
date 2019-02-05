package telegra.ph

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.webkit.JavascriptInterface
import im.delight.android.webview.AdvancedWebView

class Viewer @JvmOverloads constructor(
		context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AdvancedWebView(context, attrs, defStyleAttr) {

	init {
		prepare()
	}

	@SuppressLint("SetJavaScriptEnabled", "AddJavascriptInterface")
	fun prepare() {
		settings.javaScriptEnabled = true
		settings.loadWithOverviewMode = true
		settings.useWideViewPort = true
		overScrollMode = View.OVER_SCROLL_NEVER
		setMixedContentAllowed(true)
		loadDataWithBaseURL("https://telegra.ph", context.assets.open("viewer.html").bufferedReader().readText(), "text/html", "utf-8", null)
	}

	fun showPage(page: TelegraphApi.Page) {
		clearHistory()
		setArticleTitle(page.title)
		setAuthor(page.authorName, page.authorUrl)
		setViews(page.views)
		if (page.content == null) setDescription(page.description)
		else setContent(page.content)
	}

	private fun setArticleTitle(title: String) {
		this.loadUrl("javascript:setTitle('$title');")
	}

	private fun setAuthor(author: String?, url: String?) {
		this.loadUrl("javascript:setAuthor('$author','$url');")
	}

	private fun setViews(views: Int) {
		this.loadUrl("javascript:setViews('$views');")
	}

	private fun setDescription(description: String) {
		this.loadUrl("javascript:setDescription('${description.replace("\n", "<br>")}');")
	}

	private fun setContent(content: String?) {
		this.loadUrl("javascript:setContent('${content?.replace("'", "\\'")}');")
	}

}