package telegra.ph

import com.afollestad.bridge.Bridge
import org.json.JSONArray
import org.json.JSONObject

class Api {

	private val ApiBase = "https://api.telegra.ph/"

	fun getPage(path: String?, accessToken: String?, callback: (success: Boolean, page: Page?) -> Unit) {
		Bridge.get("${ApiBase}getPage/$path?access_token=%s&return_content=true", accessToken).asString { response, s, bridgeException ->
			if (!s.isNullOrBlank() && bridgeException == null) try {
				callback(true, JSONObject(s).parsePage())
			} catch (e: Exception) {
				callback(false, null)
			}
			else callback(false, null)
		}
	}

	fun createPage(accessToken: String?, content: String?, title: String?, callback: (success: Boolean, Page?) -> Unit) {
		Bridge.get("${ApiBase}createPage?access_token=%s&title=%s&content=%s&return_content=true", accessToken, title, content).asString { response, s, bridgeException ->
			if (!s.isNullOrBlank() && bridgeException == null) try {
				callback(true, JSONObject(s).parsePage())
			} catch (e: Exception) {
				callback(false, null)
			}
			else callback(false, null)
		}
	}

	fun editPage(accessToken: String?, path: String?, content: String?, title: String?, callback: (success: Boolean, Page?) -> Unit) {
		Bridge.get("${ApiBase}editPage/$path?access_token=%s&title=%s&content=%s&return_content=true", accessToken, title, content).asString { response, s, bridgeException ->
			if (!s.isNullOrBlank() && bridgeException == null) try {
				callback(true, JSONObject(s).parsePage())
			} catch (e: Exception) {
				callback(false, null)
			}
			else callback(false, null)
		}
	}

	fun createAccount(callback: (accessToken: String?) -> Unit) {
		Bridge.get("${ApiBase}createAccount?short_name=teleposter").asJsonObject { response, jsonObject, bridgeException ->
			if (jsonObject != null) callback(jsonObject.optJSONObject("result")?.optString("access_token"))
			else callback(null)
		}
	}

	private fun JSONObject.parsePage(): Page? {
		val result: Page = Page()
		if (optBoolean("ok", false)) {
			optJSONObject("result")?.let {
				result.path = it.optString("path", "")
				result.url = it.optString("url", "")
				result.title = it.optString("title", "")
				result.description = it.optString("description", "")
				result.author_name = it.optString("author_name", "")
				result.author_url = it.optString("author_url", "")
				result.image_url = it.optString("image_url", "")
				it.optJSONArray("content")?.parseContent(result)
				result.views = it.optInt("views", 0)
				result.can_edit = it.optBoolean("can_edit", false)
			}
		}
		return result
	}

	private fun JSONArray.parseContent(result: Page) {
		for (i in 0 until length()) {
			optJSONObject(i)?.let {
				result.content += "<${it.optString("tag", "")}"
				it.optJSONObject("attrs")?.let {
					for (key in it.keys()) {
						result.content += " $key=\"${it.optString(key, "")}\""
					}
				}
				result.content += ">"
				it.optJSONArray("children")?.parseContent(result)
				result.content += "</${it.optString("tag", "")}>"
			}
			if (optJSONObject(i) == null) optString(i)?.let {
				result.content += it
			}
		}
	}

}

class Page(
		var path: String = "",
		var url: String = "",
		var title: String = "",
		var description: String = "",
		var author_name: String = "",
		var author_url: String = "",
		var image_url: String = "",
		var content: String = "",
		var views: Int = 0,
		var can_edit: Boolean = false
)