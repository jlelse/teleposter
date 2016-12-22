package telegra.ph

import com.afollestad.bridge.Bridge
import org.json.JSONArray
import org.json.JSONObject

class Api {

	private val ApiBase = "https://api.telegra.ph/"

	fun getPage(path: String?, accessToken: String?, callback: (success: Boolean, page: Page?) -> Unit) {
		Bridge.get("${ApiBase}getPage/$path?access_token=%s&return_content=true", accessToken).asString { response, s, bridgeException ->
			if (!s.isNullOrBlank() && bridgeException == null) try {
				callback(true, JSONObject(s).parsePageResponse())
			} catch (e: Exception) {
				callback(false, null)
			}
			else callback(false, null)
		}
	}

	fun createPage(accessToken: String?, content: String?, title: String?, name: String?, callback: (success: Boolean, Page?) -> Unit) {
		Bridge.get("${ApiBase}createPage?access_token=%s&title=%s&author_name=%s&content=%s&return_content=true", accessToken, title, name, content).asString { response, s, bridgeException ->
			if (!s.isNullOrBlank() && bridgeException == null) try {
				callback(true, JSONObject(s).parsePageResponse())
			} catch (e: Exception) {
				callback(false, null)
			}
			else callback(false, null)
		}
	}

	fun editPage(accessToken: String?, path: String?, content: String?, title: String?, name: String?, callback: (success: Boolean, Page?) -> Unit) {
		Bridge.get("${ApiBase}editPage/$path?access_token=%s&title=%s&author_name=%s&content=%s&return_content=true", accessToken, title, name, content).asString { response, s, bridgeException ->
			if (!s.isNullOrBlank() && bridgeException == null) try {
				callback(true, JSONObject(s).parsePageResponse())
			} catch (e: Exception) {
				callback(false, null)
			}
			else callback(false, null)
		}
	}

	fun createAccount(callback: (accessToken: String?) -> Unit) {
		Bridge.get("${ApiBase}createAccount?short_name=teleposter").asString { response, s, bridgeException ->
			if (!s.isNullOrBlank() && bridgeException == null) try {
				callback(JSONObject(s).optJSONObject("result")?.optString("access_token"))
			} catch (e: Exception) {
				callback(null)
			}
			else callback(null)
		}
	}

	fun getPageList(accessToken: String?, offset: Int = 0, callback: (success: Boolean, MutableList<Page>?) -> Unit) {
		Bridge.get("${ApiBase}getPageList?access_token=%s&limit=200&offset=$offset", accessToken).asString { response, s, bridgeException ->
			if (!s.isNullOrBlank() && bridgeException == null) try {
				JSONObject(s).optJSONObject("result")?.let {
					val totalCount = it.optInt("total_count")
					var currentCount = 200 + offset
					val result = mutableListOf<Page>()
					it.optJSONArray("pages")?.let {
						for (i in 0 until it.length()) {
							val page = it.optJSONObject(i)?.parsePage()
							if (page != null) result.add(page)
						}
					}
					if (currentCount < totalCount) {
						getPageList(accessToken, currentCount) { success, pages ->
							if (success && pages != null) {
								result.addAll(pages)
								callback(true, result)
							}
							if (!success) callback(false, null)
						}
						currentCount += 200
					} else callback(true, result)
				} ?: callback(false, null)
			} catch (e: Exception) {
				callback(false, null)
			}
			else callback(false, null)
		}
	}

	private fun JSONObject.parsePageResponse(): Page? {
		if (optBoolean("ok", false)) optJSONObject("result")?.let { return it.parsePage() }
		return null
	}

	private fun JSONObject.parsePage(): Page? {
		val result: Page = Page()
		result.path = optString("path", "")
		result.url = optString("url", "")
		result.title = optString("title", "")
		result.description = optString("description", "")
		result.author_name = optString("author_name", "")
		result.author_url = optString("author_url", "")
		result.image_url = optString("image_url", "")
		optJSONArray("content")?.parseContent(result)
		result.views = optInt("views", 0)
		result.can_edit = optBoolean("can_edit", false)
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