package telegra.ph

import org.json.JSONArray

operator fun JSONArray.iterator(): Iterator<Any> = (0 until length()).asSequence().map { opt(it) }.iterator()