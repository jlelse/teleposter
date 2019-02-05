package telegra.ph

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.webkit.JavascriptInterface
import im.delight.android.webview.AdvancedWebView

class Editor @JvmOverloads constructor(
		context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AdvancedWebView(context, attrs, defStyleAttr) {
	private var getCallback: (json: String?) -> Unit? = {}

	init {
		prepare()
	}

	@SuppressLint("SetJavaScriptEnabled", "AddJavascriptInterface")
	fun prepare() {
		this.settings.javaScriptEnabled = true
		this.addJavascriptInterface(MyJavaScriptInterface(), "android")
		this.settings.loadWithOverviewMode = true
		this.settings.useWideViewPort = true
		setMixedContentAllowed(true)
		this.loadDataWithBaseURL("https://telegra.ph", context.assets.open("editor.html").bufferedReader().readText(), "text/html", "utf-8", null)
	}

	private inner class MyJavaScriptInterface {
		@JavascriptInterface
		@SuppressWarnings("unused")
		fun getText(json: String) {
			getCallback(json)
		}
	}

	fun setContent(content: String?) {
		this.loadUrl("javascript:setContent('${content?.replace("'", "\\'")}');")
	}

	fun getText(callback: (json: String?) -> Unit) {
		getCallback = callback
		this.loadUrl("javascript:getNodeJson();")
	}

}