package nl.ecci.hamers.ui.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.preference.PreferenceManager
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import androidx.fragment.app.DialogFragment
import nl.ecci.hamers.R
import nl.ecci.hamers.data.Loader
import nl.ecci.hamers.models.User
import nl.ecci.hamers.utils.DataUtils
import nl.ecci.hamers.utils.DataUtils.usernameToID
import nl.ecci.hamers.utils.Utils
import org.json.JSONException
import org.json.JSONObject

class NewQuoteFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val prefs = PreferenceManager.getDefaultSharedPreferences(activity)
        val builder = AlertDialog.Builder(activity)
        val inflater = activity?.layoutInflater
        val view = inflater?.inflate(R.layout.fragment_new_quote, null)
        builder.setView(view)
                .setTitle(R.string.quote)
                .setPositiveButton(R.string.send_quote) { _, _ ->
                    val edit = view?.findViewById(R.id.quote_input) as EditText
                    val quote = edit.text.toString()

                    val userSpinner = view.findViewById(R.id.quote_user_spinner) as Spinner
                    val userID = usernameToID(prefs, userSpinner.selectedItem.toString())

                    postQuote(quote, userID)
                }
        val spinner = view?.findViewById(R.id.quote_user_spinner) as Spinner
        val users = DataUtils.createActiveMemberList(prefs)
        val names = users.map(User::name)
        val adapter = ArrayAdapter(activity!!, android.R.layout.simple_spinner_dropdown_item, names)
        spinner.adapter = adapter

        return builder.create()
    }

    override fun onDismiss(dialog: DialogInterface?) {
        super.onDismiss(dialog)
        val parentFragment = parentFragment
        if (parentFragment is DialogInterface.OnDismissListener) {
            parentFragment.onDismiss(dialog)
        }
    }

    private fun postQuote(quote: String, userID: Int) {
        try {
            val body = JSONObject()
            body.put("text", quote)
            body.put("user_id", userID)
            Loader.postOrPatchData(context!!, Loader.QUOTEURL, body, Utils.notFound, null)
        } catch (ignored: JSONException) {
        }
    }
}