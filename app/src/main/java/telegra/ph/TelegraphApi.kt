package telegra.ph

import com.github.kittinunf.fuel.android.core.Json
import com.github.kittinunf.fuel.android.extension.responseJson
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import org.json.JSONArray
import org.json.JSONObject

class TelegraphApi {

	// Telegraph

	init {
		FuelManager.instance.basePath = "https://api.telegra.ph"
	}

	private fun callService(method: String, parameters: List<Pair<String, Any?>>, handler: (Request, Response, Result<Json, FuelError>) -> Unit) {
		method.httpGet(parameters.filter { it.second != null }).responseJson(handler)
	}


	fun createAccount(shortName: String, authorName: String? = null, authorUrl: String? = null, callback: (success: Boolean, account: Account?, error: String?) -> Unit) {
		callService("/createAccount", listOf("short_name" to shortName, "author_name" to authorName, "author_url" to authorUrl)) { _, _, result ->
			handleResponse(result, callback) { obj: JSONObject -> callback(true, Account(obj), null) }
		}
	}

	fun editAccountInfo(accessToken: String, shortName: String? = null, authorName: String? = null, authorUrl: String? = null, callback: (success: Boolean, account: Account?, error: String?) -> Unit) {
		callService("/editAccountInfo", listOf("access_token" to accessToken, "short_name" to shortName, "author_name" to authorName, "author_url" to authorUrl)) { _, _, result ->
			handleResponse(result, callback) { obj: JSONObject -> callback(true, Account(obj), null) }
		}
	}

	fun getAccountInfo(accessToken: String, fields: Array<String>? = null, callback: (success: Boolean, account: Account?, error: String?) -> Unit) {
		callService("/getAccountInfo", listOf("access_token" to accessToken, "fields" to fields)) { _, _, result ->
			handleResponse(result, callback) { obj: JSONObject -> callback(true, Account(obj), null) }
		}
	}

	fun revokeAccessToken(accessToken: String, callback: (success: Boolean, account: Account?, error: String?) -> Unit) {
		callService("/revokeAccessToken", listOf("access_token" to accessToken)) { _, _, result ->
			handleResponse(result, callback) { obj: JSONObject -> callback(true, Account(obj), null) }
		}
	}

	fun createPage(accessToken: String, title: String, authorName: String? = null, authorUrl: String? = null, content: String, returnContent: Boolean? = null, callback: (success: Boolean, page: Page?, error: String?) -> Unit) {
		callService("/createPage", listOf("access_token" to accessToken, "title" to title, "author_name" to authorName, "author_url" to authorUrl, "content" to content, "return_content" to returnContent)) { _, _, result ->
			handleResponse(result, callback) { obj: JSONObject -> callback(true, Page(obj), null) }
		}
	}

	fun editPage(accessToken: String, path: String, title: String, authorName: String? = null, authorUrl: String? = null, content: String, returnContent: Boolean? = null, callback: (success: Boolean, page: Page?, error: String?) -> Unit) {
		callService("/editPage", listOf("access_token" to accessToken, "path" to path, "title" to title, "author_name" to authorName, "author_url" to authorUrl, "content" to content, "return_content" to returnContent)) { _, _, result ->
			handleResponse(result, callback) { obj: JSONObject -> callback(true, Page(obj), null) }
		}
	}

	fun getPage(path: String, returnContent: Boolean? = null, callback: (success: Boolean, page: Page?, error: String?) -> Unit) {
		callService("/getPage", listOf("path" to path, "return_content" to returnContent)) { _, _, result ->
			handleResponse(result, callback) { obj: JSONObject -> callback(true, Page(obj), null) }
		}
	}

	fun getPageList(accessToken: String, offset: Int? = null, limit: Int? = null, callback: (success: Boolean, page: PageList?, error: String?) -> Unit) {
		callService("/getPage", listOf("access_token" to accessToken, "offset" to offset, "limit" to limit)) { _, _, result ->
			handleResponse(result, callback) { obj: JSONObject -> callback(true, PageList(obj), null) }
		}
	}

	fun getViews(path: String, year: Int? = null, month: Int? = null, day: Int? = null, hour: Int? = null, callback: (success: Boolean, page: PageViews?, error: String?) -> Unit) {
		callService("/getPage", listOf("path" to path, "year" to year, "month" to month, "day" to day, "hour" to hour)) { _, _, result ->
			handleResponse(result, callback) { obj: JSONObject -> callback(true, PageViews(obj), null) }
		}
	}

	class Account(json: JSONObject) {
		val shortName: String? = json.optString("short_name")
		val authorName: String? = json.optString("author_name")
		val authorUrl: String? = json.optString("author_url")
		val accessToken: String? = json.optString("access_token")
		val authUrl: String? = json.optString("auth_url")
		val pageCount: Int? = json.optInt("page_count")
	}

	class PageList(json: JSONObject) {
		val totalCount: Int? = json.optInt("total_count")
		val pages: Array<Page>? = json.optJSONArray("pages")?.let {
			mutableListOf<Page>().apply { for (i in 0 until it.length()) add(Page(it.optJSONObject(i))) }.toTypedArray()
		}
	}

	class Page(json: JSONObject) {
		val path: String = json.optString("path")
		val url: String = json.optString("url")
		val title: String = json.optString("title")
		val description: String = json.optString("description")
		val authorName: String? = json.optString("authorName")
		val authorUrl: String? = json.optString("author_url")
		val imageUrl: String? = json.optString("image_url")
		val content: String? = json.optJSONArray("content")?.parseContent()
		val views: Int = json.optInt("views")
		val canEdit: Boolean? = json.optBoolean("can_edit")

		private fun JSONArray.parseContent(): String? {
			var content = ""
			for (i in 0 until length()) {
				optJSONObject(i)?.let {
					content += "<${it.optString("tag", "")}"
					it.optJSONObject("attrs")?.let {
						for (key in it.keys()) {
							content += " $key=\"${it.optString(key, "")}\""
						}
					}
					content += ">"
					content += it.optJSONArray("children")?.parseContent() ?: ""
					content += "</${it.optString("tag", "")}>"
				}
				if (optJSONObject(i) == null) optString(i)?.let {
					content += it
				}
			}
			return content
		}
	}

	class PageViews(json: JSONObject) {
		val views: Int? = json.optInt("views")
	}

	// Teleposter

	private fun <T> handleResponse(result: Result<Json, FuelError>, handler: (success: Boolean, obj: T?, error: String?) -> Unit, callback: (obj: JSONObject) -> Unit) {
		val (json, error) = result
		if (error == null && json != null) {
			val jsonObj = json.obj()
			if (jsonObj.optBoolean("ok")) {
				handler(true, null, null)
			} else {
				callback(jsonObj.optJSONObject("result"))
			}
		} else {
			handler(false, null, error?.message)
		}
	}


}