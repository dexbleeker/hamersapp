package nl.ecci.hamers.helpers

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.widget.DatePicker
import kotlinx.android.synthetic.main.stub_new_event.*
import kotlinx.android.synthetic.main.stub_new_meeting.*
import kotlinx.android.synthetic.main.stub_new_review.*
import java.util.*

class DatePickerFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the current date as the default date in the picker
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        // Create a new instance of DatePickerDialog and return it
        return DatePickerDialog(activity, (activity as NewItemActivity), year, month, day)
    }
}