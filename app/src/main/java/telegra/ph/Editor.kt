package telegra.ph

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.webkit.JavascriptInterface
import im.delight.android.webview.AdvancedWebView

class Editor : AdvancedWebView {
	private var getCallback: (json: String?) -> Unit? = {}

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
		this.addJavascriptInterface(MyJavaScriptInterface(), "android")
		this.settings.loadWithOverviewMode = true
		this.settings.useWideViewPort = true
		this.loadDataWithBaseURL("http://telegra.ph", context.assets.open("editor.html").bufferedReader().readText(), "text/html", "utf-8", null)
	}

	private inner class MyJavaScriptInterface {
		@JavascriptInterface
		fun getText(json: String) {
			getCallback(json)
		}
	}

	fun reset() {
		this.loadUrl("javascript:reset();")
	}

	fun setContent(content: String?) {
		this.loadUrl("javascript:setContent('${content?.replace("'", "\\'")}');")
	}

	fun getText(callback: (json: String?) -> Unit) {
		getCallback = callback
		this.loadUrl("javascript:getNodeJson();")
	}

}