package telegra.ph

import com.afollestad.bridge.Bridge
import org.json.JSONArray
import org.json.JSONObject

class Api {

	private val ApiBase = "https://api.telegra.ph/"

	fun getPage(id: String?, callback: (page: Page?) -> Unit) {
		Bridge.get("${ApiBase}getPage/$id?return_content=true").asJsonObject { response, jsonObject, bridgeException ->
			if (jsonObject != null) callback(jsonObject.parsePage())
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
		try {
			for (child in this) {
				try {
					if (child is String) result.content += child
					else if (child is JSONObject) {
						result.content += "<${child.optString("tag", "")}"
						child.optJSONObject("attrs")?.let {
							for (key in it.keys()) {
								result.content += " $key=\"${it.optString(key, "")}\""
							}
							for (i in 0 until length()) {
								result.content += "${it.names()}"
							}
						}
						result.content += ">"
						child.optJSONArray("children").parseContent(result)
						result.content += "</${child.optString("tag", "")}>"
					}
				} catch (e: Exception) {
					e.printStackTrace()
				}
			}
		} catch (e: Exception) {
			e.printStackTrace()
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