-keep class * extends android.webkit.WebChromeClient { *; }
-dontwarn im.delight.android.webview.**
-keepclassmembers class telegra.ph.Editor$MyJavaScriptInterface {
    public *;
}
-keepattributes JavascriptInterface