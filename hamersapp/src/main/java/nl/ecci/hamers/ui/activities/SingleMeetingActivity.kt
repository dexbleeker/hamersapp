package nl.ecci.hamers.ui.activities

import android.os.Bundle
import kotlinx.android.synthetic.main.activity_general.*
import kotlinx.android.synthetic.main.stub_detail_meeting.*
import nl.ecci.hamers.BuildConfig
import nl.ecci.hamers.R
import nl.ecci.hamers.models.Meeting
import nl.ecci.hamers.utils.DataUtils
import nl.ecci.hamers.utils.Utils

class SingleMeetingActivity : HamersActivity() {

    private var meeting: Meeting? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_general)

        initToolbar()

        stub.layoutResource = R.layout.stub_detail_meeting
        stub.inflate()

        // Intent by clicking an event in EventFragment or by clicking a link elsewhere
        val appLinkData = intent.data
        val meetingID = intent.getIntExtra(Meeting.MEETING, Utils.notFound)
        if (appLinkData != null) {
            meeting = DataUtils.getMeeting(this, Utils.getIdFromUri(appLinkData))
        } else {
            meeting = DataUtils.getMeeting(this, meetingID)
        }

        var meetingSubject = meeting?.subject
        if (BuildConfig.DEBUG) {
            meetingSubject += " (" + meeting?.id + ")"
        }
        meeting_subject.text = meetingSubject
        meeting_date.text = MainActivity.appDF.format(meeting?.date)
        meeting_user.text = String.format("Genotuleerd door: %s", DataUtils.getUser(this, meeting!!.userID).name)
        meeting_agenda.loadMarkdown(meeting?.agenda)
        meeting_notes.loadMarkdown(meeting?.notes)
    }
}