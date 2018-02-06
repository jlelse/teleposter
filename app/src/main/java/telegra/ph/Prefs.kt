package telegra.ph

import android.content.Context
import android.preference.PreferenceManager

const val listItemSeparator = "+++;+++"
const val itemSeparator = "xxx;xxx"

fun Context.bookmarks(): List<Pair<String, String>> {
	val list = PreferenceManager.getDefaultSharedPreferences(this).getString("bookmarks", null)?.split(listItemSeparator)?.map {
		val splitParts = it.split(itemSeparator)
		if (splitParts.size == 2) splitParts[0] to splitParts[1]
		else null
	}?.filterNotNull()
	return if (list != null && list.isNotEmpty()) list else listOf("api" to "API Documentation")
}

fun Context.addBookmark(path: String, title: String) {
	saveBookmarks(bookmarks().plus(path to title))
}

fun Context.deleteBookmark(path: String) {
	saveBookmarks(bookmarks().filter { it.first != path })
}

fun Context.saveBookmarks(bookmarks: List<Pair<String, String>>) {
	PreferenceManager.getDefaultSharedPreferences(this).edit().putString("bookmarks",
			bookmarks.joinToString(separator = listItemSeparator) { "${it.first}$itemSeparator${it.second}" }
	).apply()
}

fun Context.accessToken(): String = PreferenceManager.getDefaultSharedPreferences(this).getString("accessToken", "")

fun Context.saveAccessToken(token: String) {
	PreferenceManager.getDefaultSharedPreferences(this).edit().putString("accessToken", token).apply()
}

fun Context.authorName(): String? = PreferenceManager.getDefaultSharedPreferences(this).getString("authorName", null)

fun Context.saveAuthorName(name: String) {
	PreferenceManager.getDefaultSharedPreferences(this).edit().putString("authorName", name).apply()
}