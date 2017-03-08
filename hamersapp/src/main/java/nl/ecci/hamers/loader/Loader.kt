package nl.ecci.hamers.loader

import android.content.Context
import android.support.v7.preference.PreferenceManager
import android.util.Log
import android.widget.Toast
import com.android.volley.*
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import nl.ecci.hamers.R
import nl.ecci.hamers.helpers.Utils
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.util.*

object Loader {
    // URL Appendices
    val QUOTEURL = "quotes"
    val USERURL = "users"
    val EVENTURL = "events"
    val NEWSURL = "news"
    val BEERURL = "beers"
    val REVIEWURL = "reviews"
    val WHOAMIURL = "whoami"
    val MEETINGURL = "meetings"
    val SIGNUPURL = "signups"
    val GCMURL = "register"
    val STICKERURL = "stickers"
    val CHANGEURL = "changes"
    // Data keys
    val APIKEYKEY = "apikey"

    // URL
    private val baseURL = "https://zondersikkel.nl/api/v2/"
    // private static final String baseURL = "http://192.168.100.100:3000/api/v2/";

    fun getData(context: Context, dataURL: String, callback: GetCallback?, params: Map<String, String>?) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val url = buildURL(dataURL, params, -1)

        val request = object : StringRequest(Request.Method.GET, url,
                Response.Listener<String> { response ->
                    Log.d("GET-response", response)
                    if (dataURL != SIGNUPURL && dataURL != EVENTURL) {
                        prefs.edit().putString(dataURL, response).apply()
                    }
                    callback?.onSuccess(response)
                },
                Response.ErrorListener { error ->
                    Log.d("GET-error", error.toString())
                    callback?.onError(error)
                    handleErrorResponse(context, error)
                }) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                headers.put("Authorization", "Token token=" + prefs.getString(APIKEYKEY, "")!!)
                return headers
            }

            public override fun getParams(): Map<String, String> {
                return params!!
            }
        }

        Log.d("Request", request.toString())
        Singleton.getInstance(context).addToRequestQueue<String>(request)
    }

    fun postOrPatchData(context: Context, dataURL: String, body: JSONObject, urlAppendix: Int, callback: PostCallback?) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val url = buildURL(dataURL, null, urlAppendix)

        Log.d("PostRequest: ", body.toString())

        val request = object : JsonObjectRequest(url, body,
                Response.Listener<JSONObject> { response ->
                    Log.d("POST-response", response.toString())

                    if (dataURL != GCMURL)
                        Utils.showToast(context, context.getString(R.string.posted), Toast.LENGTH_SHORT)

                    callback?.onSuccess(response)
                },
                Response.ErrorListener { error ->
                    Log.d("POST-error", error.toString())
                    callback?.onError(error)
                    handleErrorResponse(context, error)
                }) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val params = HashMap<String, String>()
                params.put("Authorization", "Token token=" + prefs.getString(APIKEYKEY, "")!!)
                params.put("Content-Type", "Application/json")
                if (urlAppendix != -1) {
                    params.put("X-HTTP-Method-Override", "PATCH")
                }
                return params
            }
        }
        Log.d("PostRequest: ", request.toString())
        Singleton.getInstance(context).addToRequestQueue<JSONObject>(request)
    }

    private fun buildURL(URL: String, params: Map<String, String>?, appendix: Int): String {
        val builder = StringBuilder()
        builder.append(baseURL).append(URL)

        if (appendix != -1) {
            builder.append("/").append(appendix)
        }

        if (params != null) {
            builder.append("?")
            for (key in params.keys) {
                var value: Any? = params[key]
                if (value != null) {
                    try {
                        value = URLEncoder.encode((value).toString(), "UTF-8")
                        if (builder.isNotEmpty())
                            builder.append("&")
                        builder.append(key).append("=").append(value)
                    } catch (ignored: UnsupportedEncodingException) {
                    }

                }
            }
        }
        return builder.toString()
    }

    private fun handleErrorResponse(context: Context, error: VolleyError) {
        if (error is AuthFailureError) {
            // Wrong API key
            Utils.showToast(context, context.getString(R.string.auth_error), Toast.LENGTH_SHORT)
        } else if (error is TimeoutError) {
            // Timeout
            Utils.showToast(context, context.getString(R.string.timeout_error), Toast.LENGTH_SHORT)
        } else if (error is ServerError) {
            // Server error (500)
            Utils.showToast(context, context.getString(R.string.server_error), Toast.LENGTH_SHORT)
        } else if (error is NoConnectionError) {
            // No network connection
            Utils.showToast(context, context.getString(R.string.connection_error), Toast.LENGTH_SHORT)
        } else if (error is NetworkError) {
            // Network error
            Utils.showToast(context, context.getString(R.string.network_error), Toast.LENGTH_SHORT)
        } else {
            // Other error
            Utils.showToast(context, context.getString(R.string.volley_error), Toast.LENGTH_SHORT)
        }
    }

    fun getAllData(context: Context) {
        Loader.getData(context, Loader.QUOTEURL, null, null)
        Loader.getData(context, Loader.EVENTURL, null, null)
        Loader.getData(context, Loader.NEWSURL, null, null)
        Loader.getData(context, Loader.BEERURL, null, null)
        Loader.getData(context, Loader.REVIEWURL, null, null)
        Loader.getData(context, Loader.WHOAMIURL, null, null)
        Loader.getData(context, Loader.MEETINGURL, null, null)
    }
}