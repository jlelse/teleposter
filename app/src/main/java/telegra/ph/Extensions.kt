package telegra.ph

import org.json.JSONArray

operator fun JSONArray.iterator(): Iterator<Any> = (0..length() - 1).map { opt(it) }.iterator()