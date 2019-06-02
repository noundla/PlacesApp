package com.example.places

import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.libraries.places.widget.Autocomplete
import android.content.Intent
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.AutocompleteActivity

class MainActivity : AppCompatActivity() {

    companion object {
        const val AUTOCOMPLETE_INTENT_REQUEST_CODE = 1001
        const val TAG = "MainActivity"
    }

    lateinit var placesClient: PlacesClient
    lateinit var mAdapter: AutoCompleteAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), "AIzaSyCdO2xQAGAOvAGE1aDZHTp-j99lj1NS9ig");
        }

        setUpAutoCompleteTextView()

        selectWithIntentBtn.setOnClickListener {
            // Set the fields to specify which types of place data to return.
            val fields =
                Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG)


            // Start the autocomplete intent.
            val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields).build(this)
            startActivityForResult(intent, AUTOCOMPLETE_INTENT_REQUEST_CODE)
        }
    }

    /**
     * Override the activity's onActivityResult(), check the request code, and
     * do something with the returned place data (in this example it's place name and place ID).
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == AUTOCOMPLETE_INTENT_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                val place = Autocomplete.getPlaceFromIntent(data!!)
                Log.i(TAG, "Place: ${place.toString()}" + place.name + ", " + place.id)
                placeTextView.setText("${place.toString()}")
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                // TODO: Handle the error.
                val status = Autocomplete.getStatusFromIntent(data!!)
                Log.i(TAG, status.statusMessage)
            } else if (resultCode == Activity.RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }


    private fun setUpAutoCompleteTextView() {
        placesClient = Places.createClient(this)
        locationACT.setThreshold(3)
        locationACT.onItemClickListener = autocompleteClickListener
        mAdapter = AutoCompleteAdapter(this, placesClient)
        locationACT.setAdapter(mAdapter)
    }


    var autocompleteClickListener: AdapterView.OnItemClickListener =
        AdapterView.OnItemClickListener { parent, view, position, id ->
            var item: AutocompletePrediction? = mAdapter.getItem(position)
            var placeID = item?.placeId
            var placeFields: List<Place.Field> =
                Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG)
            var request: FetchPlaceRequest? = null
            if (placeID != null) {
                request = FetchPlaceRequest.builder(placeID, placeFields).build()
            }

            if (request != null) {
                placesClient.fetchPlace(request).addOnSuccessListener {
                    //                responseView.setText(task.getPlace().getName() + "\n" + task.getPlace().getAddress());

                    val place = it.place
                    if (!place.address.isNullOrBlank() && place.latLng != null) {
                        val location = place.address!!
                        val latitude = place.latLng!!.latitude.toString()
                        val longitude = place.latLng!!.longitude.toString()
                        placeTextView.setText("Place Info: \n $location \nLat: $latitude , Lon: $longitude")
                    }
                    locationACT.setText("${place.address}")
                    Log.i("TAG", "Called getPlaceById to get Place details for $placeID")

                }.addOnFailureListener {
                    it.printStackTrace()
                }
            }
            hideKeyboard()
        }

    fun Activity.hideKeyboard() {
        hideKeyboard(if (currentFocus == null) View(this) else currentFocus)
    }

    fun Context.hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

}
