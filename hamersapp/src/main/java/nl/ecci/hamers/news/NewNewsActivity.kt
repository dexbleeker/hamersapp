package nl.ecci.hamers.news

import android.os.Bundle
import com.android.volley.VolleyError
import kotlinx.android.synthetic.main.news_new_acitivity.*
import nl.ecci.hamers.R
import nl.ecci.hamers.helpers.NewItemActivity
import nl.ecci.hamers.loader.Loader
import nl.ecci.hamers.loader.PostCallback
import org.json.JSONException
import org.json.JSONObject

class NewNewsActivity : NewItemActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.news_new_acitivity)

        initToolbar()
    }

    override fun postItem() {
        val body = JSONObject()
        try {
            body.put("name", news_title.text.toString())
            body.put("body", news_body.text.toString())
            body.put("cat", "l")
        } catch (ignored: JSONException) {
        }

        Loader.postOrPatchData(this, Loader.NEWSURL, body, -1, object : PostCallback {
            override fun onSuccess(response: JSONObject) {
                finish()
            }

            override fun onError(error: VolleyError) {}
        })
    }
}