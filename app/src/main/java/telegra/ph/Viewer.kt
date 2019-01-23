package telegra.ph

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import im.delight.android.webview.AdvancedWebView

class Viewer : AdvancedWebView {

	constructor(context: Context) : super(context) {
		init()
	}

	constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
		init()
	}

	constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
		init()
	}

	@SuppressLint("SetJavaScriptEnabled", "AddJavascriptInterface")
	private fun init() {
		this.settings.javaScriptEnabled = true
		this.settings.loadWithOverviewMode = true
		this.settings.useWideViewPort = true
		overScrollMode = View.OVER_SCROLL_NEVER
		setMixedContentAllowed(true)
		this.loadDataWithBaseURL("https://telegra.ph", context.assets.open("viewer.html").bufferedReader().readText(), "text/html", "utf-8", null)
	}

	fun setArticleTitle(title: String) {
		this.loadUrl("javascript:setTitle('$title');")
	}

	fun setAuthor(author: String?, url: String?) {
		this.loadUrl("javascript:setAuthor('$author','$url');")
	}

	fun setViews(views: Int) {
		this.loadUrl("javascript:setViews('$views');")
	}

	fun setDescription(description: String) {
		this.loadUrl("javascript:setDescription('${description.replace("\n", "<br>")}');")
	}

	fun setContent(content: String?) {
		this.loadUrl("javascript:setContent('${content?.replace("'", "\\'")}');")
	}

}