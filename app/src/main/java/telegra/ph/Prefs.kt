package telegra.ph

import android.content.Context
import android.preference.PreferenceManager

fun Context.bookmarks(): MutableList<String> = PreferenceManager.getDefaultSharedPreferences(this).getString("bookmarks", null)?.split("+++;+++")?.toMutableList() ?: mutableListOf("apixxx;xxxAPI Documentation")

fun Context.addBookmark(entry: String) {
	PreferenceManager.getDefaultSharedPreferences(this).edit().putString("bookmarks", bookmarks().apply {
		add(entry)
	}.joinToString(separator = "+++;+++")).apply()
}

fun Context.deleteBookmark(path: String) {
	PreferenceManager.getDefaultSharedPreferences(this).edit().putString("bookmarks", bookmarks().filter { !it.contains(path) }.joinToString(separator = "+++;+++")).apply()
}

fun Context.accessToken(): String? = PreferenceManager.getDefaultSharedPreferences(this).getString("accessToken", null)

fun Context.saveAccessToken(token: String) {
	PreferenceManager.getDefaultSharedPreferences(this).edit().putString("accessToken", token).apply()
}