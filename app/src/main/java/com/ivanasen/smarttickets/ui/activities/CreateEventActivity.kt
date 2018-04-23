package com.ivanasen.smarttickets.ui.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.*
import android.arch.lifecycle.Observer

import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.android.gms.location.places.ui.PlacePicker
import com.ivanasen.smarttickets.R
import com.ivanasen.smarttickets.viewmodels.CreateEventActivityViewModel
import kotlinx.android.synthetic.main.activity_create_event.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.sdk25.coroutines.textChangedListener
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.transition.Fade
import android.transition.TransitionManager
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.ivanasen.smarttickets.ui.adapters.ImageAdapter
import com.ivanasen.smarttickets.ui.adapters.TicketCreationTypeAdapter
import com.ivanasen.smarttickets.util.Utility
import droidninja.filepicker.FilePickerBuilder
import droidninja.filepicker.FilePickerConst
import java.util.*
import java.text.SimpleDateFormat


class CreateEventActivity : AppCompatActivity() {

    companion object {
        private val LOG_TAG: String = CreateEventActivity::class.java.simpleName

        private const val PLACE_PICKER_REQUEST = 1
        private const val PERMISSIONS_REQUEST_WRITE_STORAGE = 2
        private const val MAX_EVENT_IMAGES = 5
    }

    private lateinit var mAdapter: ImageAdapter
    val viewModel: CreateEventActivityViewModel by lazy {
        ViewModelProviders.of(this).get(CreateEventActivityViewModel::class.java)
    }

//    private val mGeoDataClient: GeoDataClient by lazy {
//        Places.getGeoDataClient(this, null)
//    }


//    private val mPlaceDetectionClient: PlaceDetectionClient by lazy {
//        Places.getPlaceDetectionClient(this, null)
//    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_event)

        observeLiveData()
        setupViews()
    }

    @SuppressLint("SimpleDateFormat")
    private fun observeLiveData() {
        viewModel.pickedPlace.observe(this, Observer {
            pickedPlaceText.text = it?.address
            viewModel.checkIfIsValidEvent()
        })

        viewModel.isValidEvent.observe(this, Observer {
            createEventBtn.isEnabled = it as Boolean
        })

        viewModel.eventTime.observe(this, Observer {
            val formatDate = SimpleDateFormat(getString(R.string.date_format))
            val formatTime = SimpleDateFormat(getString(R.string.time_format))

            eventDateView.text = formatDate.format(it?.time)
            eventTimeView.text = formatTime.format(it?.time)
            viewModel.checkIfIsValidEvent()
        })

        viewModel.ticketTypes.observe(this, Observer {
            viewModel.checkIfIsValidEvent()
        })
    }


    @SuppressLint("SimpleDateFormat")
    private fun setupViews() {
        inputEventName.textChangedListener {
            onTextChanged { text, _, _, _ ->
                viewModel.eventName.postValue(text.toString())
                viewModel.checkIfIsValidEvent()
            }
        }

        inputEventDescription.textChangedListener {
            onTextChanged { text, _, _, _ ->
                viewModel.eventDescription.postValue(text.toString())
                viewModel.checkIfIsValidEvent()
            }
        }

        pickLocationBtn.onClick {
            val builder = PlacePicker.IntentBuilder()
            this@CreateEventActivity.startActivityForResult(
                    builder.build(this@CreateEventActivity),
                    PLACE_PICKER_REQUEST)
        }

        viewModel.eventTime.postValue(GregorianCalendar.getInstance() as GregorianCalendar)

        val layoutManager = LinearLayoutManager(this)
        ticketsRecyclerView.layoutManager = layoutManager
        val ticketTypesAdapter = TicketCreationTypeAdapter(this, viewModel.ticketTypes)
        ticketsRecyclerView.adapter = ticketTypesAdapter

        eventDateView.onClick {
            showDatePickerDialog()
        }

        eventTimeView.onClick {
            showTimePickerDialog()
        }

        addTicketTypeBtn.onClick {
            showTicketTypeDialog()
        }


        mAdapter = ImageAdapter(this, viewModel.pickedImages)
        eventImagesGridView.adapter = mAdapter
        eventImagesGridView.isExpanded = true

        selectImagesButton.onClick {
            openImagePicker()
        }

        cancelCreateEventBtn.onClick {
            onBackPressed()
        }

        createEventBtn.onClick {
            viewModel.attemptCreateEvent()
                    .observe(this@CreateEventActivity, Observer {
                        when (it) {
                            Utility.Companion.TransactionStatus.PENDING -> {
                                Toast.makeText(this@CreateEventActivity,
                                        getString(R.string.event_creating_message),
                                        Toast.LENGTH_LONG)
                                        .show()
                                finish()
                            }
                            Utility.Companion.TransactionStatus.SUCCESS -> {
                                MaterialDialog.Builder(this@CreateEventActivity)
                                        .title(getString(R.string.event_success_title))
                                        .content(getString(R.string.event_creation_message))
                                        .positiveText(getString(R.string.OK))
                                        .onPositive({ _, _ ->
                                            finish()
                                        })
                            }
                            Utility.Companion.TransactionStatus.FAILURE -> {

                            }
                            Utility.Companion.TransactionStatus.ERROR -> {
                                hideLoadingScreen()
                                Toast.makeText(this@CreateEventActivity,
                                        getString(R.string.event_error_message),
                                        Toast.LENGTH_LONG)
                                        .show()
                            }
                        }
                    })
        }

    }

//    private fun showLoadingScreen() {
//        TransitionManager.beginDelayedTransition(rootView as ViewGroup, Fade())
//        contentView.visibility = View.GONE
//        eventProgressBar.visibility = View.VISIBLE
//    }

    private fun hideLoadingScreen() {
        TransitionManager.beginDelayedTransition(rootView as ViewGroup, Fade())
        contentView.visibility = View.GONE
        eventProgressBar.visibility = View.VISIBLE
    }

    private fun showTicketTypeDialog() {
        val dialog = MaterialDialog.Builder(this)
                .customView(R.layout.ticket_type_form, false)
                .title(R.string.add_ticket_type)
                .theme(Theme.LIGHT)
                .positiveText(R.string.add_text)
                .negativeText(R.string.cancel_text)
                .onPositive({ view: MaterialDialog, _: DialogAction ->
                    val price = (view.findViewById(R.id.inputTicketTypePrice) as TextView)
                            .text
                    val supply = (view.findViewById(R.id.inputTicketTypeSupply) as TextView)
                            .text
                    val refundable = (view.findViewById(R.id.ticketTypeRefundableCheckBox) as CheckBox)
                            .isChecked

                    if (price.toString().toInt() >= 0 && supply.toString().toInt() >= 0) {
                        viewModel.addTicketType(price.toString().toDouble(),
                                supply.toString().toDouble(),
                                refundable)
                    }
                })
                .build()
        dialog.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            PLACE_PICKER_REQUEST ->
                if (resultCode == RESULT_OK) {
                    val place = PlacePicker.getPlace(this, data)
                    viewModel.onPlacePicked(place)
                }
            FilePickerConst.REQUEST_CODE_PHOTO ->
                if (resultCode == RESULT_OK && data != null) {
                    viewModel.pickedImages.postValue(
                            data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_MEDIA))
                }
            else ->
                Log.d(LOG_TAG, "Unknown requestCode")
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_REQUEST_WRITE_STORAGE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openImagePicker()
                }
            }
        }
    }

    private fun showDatePickerDialog() {
        val dateFragment = DatePickerFragment()
        dateFragment.show(fragmentManager, "datePicker")
    }

    private fun showTimePickerDialog() {
        val timeFragment = TimePickerFragment()
        timeFragment.show(fragmentManager, "timePicker")
        viewModel.eventTime.value
    }


    private fun openImagePicker() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    PERMISSIONS_REQUEST_WRITE_STORAGE)
        } else {
            FilePickerBuilder.getInstance()
                    .setMaxCount(MAX_EVENT_IMAGES)
                    .setSelectedFiles(viewModel.pickedImages.value as ArrayList<String>?)
                    .setActivityTheme(R.style.AppTheme)
                    .pickPhoto(this@CreateEventActivity)
        }
    }

    open class TimePickerFragment : DialogFragment(), TimePickerDialog.OnTimeSetListener {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            // Use the current date as the default date in the picker
            val c = Calendar.getInstance()
            val hour = c.get(Calendar.HOUR)
            val minute = c.get(Calendar.MINUTE)

            // Create a new instance of DatePickerDialog and return it
            return TimePickerDialog(activity, this, hour, minute, true)
        }

        override fun onTimeSet(view: TimePicker, hour: Int, minute: Int) {
            val viewModel = (activity as CreateEventActivity).viewModel
            val currentTime = viewModel.eventTime.value
            val calendar = GregorianCalendar.getInstance() as GregorianCalendar

            calendar.set(Calendar.YEAR,
                    (currentTime ?: calendar).get(Calendar.YEAR))
            calendar.set(Calendar.MONTH,
                    (currentTime ?: calendar).get(Calendar.MONTH))
            calendar.set(Calendar.DAY_OF_MONTH,
                    (currentTime ?: calendar).get(Calendar.DAY_OF_MONTH))

            calendar.set(Calendar.HOUR_OF_DAY, hour)
            calendar.set(Calendar.MINUTE, minute)

            viewModel.eventTime.postValue(calendar)
        }
    }

    class DatePickerFragment : DialogFragment(), DatePickerDialog.OnDateSetListener {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            // Use the current date as the default date in the picker
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)

            // Create a new instance of DatePickerDialog and return it
            return DatePickerDialog(activity, this, year, month, day)
        }

        override fun onDateSet(view: DatePicker, year: Int, month: Int, day: Int) {
            val viewModel = (activity as CreateEventActivity).viewModel
            val currentDate = viewModel.eventTime.value
            val calendar = GregorianCalendar.getInstance() as GregorianCalendar

            calendar.set(Calendar.HOUR_OF_DAY,
                    (currentDate ?: calendar).get(Calendar.HOUR_OF_DAY))
            calendar.set(Calendar.MINUTE,
                    (currentDate ?: calendar).get(Calendar.MINUTE))

            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, day)

            viewModel.eventTime.postValue(calendar)
        }
    }

}
