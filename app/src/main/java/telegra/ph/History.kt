package telegra.ph

import android.content.Context
import android.preference.PreferenceManager

fun Context.getHistory() = PreferenceManager.getDefaultSharedPreferences(this).getStringSet("history", mutableSetOf<String>())

fun Context.addToHistory(entry: String) {
	PreferenceManager.getDefaultSharedPreferences(this).edit().putStringSet("history", getHistory().apply {
		add(entry)
	}).apply()
}