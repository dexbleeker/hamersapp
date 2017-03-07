package nl.ecci.hamers.loader

import com.android.volley.VolleyError

import org.json.JSONObject

interface PostCallback {
    fun onSuccess(response: JSONObject)

    fun onError(error: VolleyError)
}
