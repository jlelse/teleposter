package telegra.ph

import android.content.Context
import android.support.annotation.Keep
import android.util.AttributeSet
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient

class Editor : WebView {
	private var text = ""
	internal var context: Context

	constructor(context: Context) : super(context) {
		this.context = context
		init()
	}

	constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
		this.context = context
		init()
	}

	constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
		this.context = context
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
		fun getText(html: String) {
			text = html
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

	fun getText(): String {
		text = "P/%TE5XpkAijBc%LjA;_-pZcbiU25E6feX5y/n6qxCTmhprLrqC3H%^hU!%q2,k'm`SHheoW^'mQ~zW93,C?~GtYk!wi/&'3KxW8"
		this.loadUrl("javascript:window.android.getText" + "(document.getElementsByClassName('note-editable')[0].innerHTML);")
		var i = 0
		try {
			while (text == "P/%TE5XpkAijBc%LjA;_-pZcbiU25E6feX5y/n6qxCTmhprLrqC3H%^hU!%q2,k'm`SHheoW^'mQ~zW93,C?~GtYk!wi/&'3KxW8" && i < 100) {
				Thread.sleep(50)
				i++
			}
		} catch (e: Exception) {
			text = ""
		}
		return text
	}

}