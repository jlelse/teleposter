package telegra.ph

import android.content.Context
import android.support.annotation.Keep
import android.util.AttributeSet
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient

class Editor : WebView {
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

	private fun init() {
		this.settings.javaScriptEnabled = true
		this.settings.cacheMode = WebSettings.LOAD_NO_CACHE
		this.addJavascriptInterface(MyJavaScriptInterface(), "android")
		this.settings.loadWithOverviewMode = true
		this.settings.useWideViewPort = true
		this.loadUrl("file:///android_asset/editor.html")
	}

	private inner class MyJavaScriptInterface {
		@JavascriptInterface
		fun getText(json: String) {
			getCallback(json)
		}
	}

	fun setText(html: String) {
		setWebViewClient(object : WebViewClient() {
			override fun onPageFinished(view: WebView, url: String) {
				setText(html)
			}
		})
		this.loadUrl("javascript:$('#summernote').summernote('reset');")
		this.loadUrl("javascript:$('#summernote').summernote('code', '" + html.replace("'", "\\'") + "');")
	}

	fun getText(callback: (json: String?) -> Unit) {
		getCallback = callback
		this.loadUrl("javascript:getNodeJson();")
	}

}