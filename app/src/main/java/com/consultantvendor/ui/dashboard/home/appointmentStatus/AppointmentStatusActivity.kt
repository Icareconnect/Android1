package com.consultantvendor.ui.dashboard.home.appointmentStatus

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.os.AsyncTask
import android.os.Bundle
import com.consultantvendor.R
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.consultantvendor.data.models.requests.SaveAddress
import com.consultantvendor.data.repos.UserRepository
import com.consultantvendor.databinding.ActivityAppointmentStatusBinding
import com.consultantvendor.ui.drawermenu.DrawerActivity
import com.consultantvendor.utils.*
import com.consultantvendor.utils.PermissionUtils
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import dagger.android.support.DaggerAppCompatActivity
import permissions.dispatcher.*
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

@RuntimePermissions
class AppointmentStatusActivity : DaggerAppCompatActivity(), GoogleMap.OnCameraChangeListener, OnMapReadyCallback {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var prefsManager: PrefsManager

    @Inject
    lateinit var userRepository: UserRepository

    private lateinit var binding: ActivityAppointmentStatusBinding

    private var saveAddress = SaveAddress()

    private var fromLat = 0.0
    private var fromLng = 0.0
    private var center: LatLng? = null

    private var mapFragment: SupportMapFragment? = null
    private var isPlacePicker = false
    private var mMap: GoogleMap? = null
    private var placeLatLng: LatLng? = null
    private lateinit var geoCoder: Geocoder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_appointment_status)

        setEditAddress()
        initialise()
        setListeners()
    }

    private fun initialise() {

        geoCoder = Geocoder(this, Locale.getDefault())
        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        getLocationWithPermissionCheck()

    }

    private fun setEditAddress() {
        if (intent.hasExtra(EXTRA_ADDRESS)) {
            saveAddress = intent.getSerializableExtra(EXTRA_ADDRESS) as SaveAddress
            saveAddress.addressId = saveAddress._id
        }
    }


    private fun setListeners() {
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        binding.tvReached.setOnClickListener {
            binding.tvReached.gone()
            binding.groupOne.visible()
        }

        binding.tvStartService.setOnClickListener {
            startActivity(Intent(this, DrawerActivity::class.java)
                    .putExtra(PAGE_TO_OPEN, DrawerActivity.UPDATE_SERVICE))
            finish()
        }

        binding.tvCancelService.setOnClickListener {
            finish()
        }
    }


    override fun onCameraChange(cameraPosition: CameraPosition) {
        center = mMap?.cameraPosition?.target
        if (!isPlacePicker) {
            placeLatLng = center

            fromLat = placeLatLng?.latitude ?: 0.0
            fromLng = placeLatLng?.longitude ?: 0.0
        }
        isPlacePicker = false
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap?.isTrafficEnabled = false
        mMap?.setOnCameraChangeListener(this)

        // mMap?.isMyLocationEnabled = true
        mMap?.uiSettings?.isMyLocationButtonEnabled = true
        //GeneralFunctions.moveMapButton(mapFragment)

        // Get user current location
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val statusOfGPS = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

        if (!saveAddress.location.isNullOrEmpty()) {
            val current = LatLng(saveAddress.location?.get(1) ?: 0.0, saveAddress.location?.get(0)
                    ?: 0.0)
            mMap?.moveCamera(CameraUpdateFactory.newLatLng(current))
            mMap?.animateCamera(CameraUpdateFactory.zoomTo(15f))
        }


        val gps = GPSTracker(this)
        // check if GPS location can get Location
        if (gps.canGetLocation() && statusOfGPS) {
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                if (saveAddress.location.isNullOrEmpty()) {
                    intent.removeExtra(EXTRA_ADDRESS)
                    val current = LatLng(gps.getLatitude(), gps.getLongitude())
                    placeLatLng = current
                    mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(current, 15f))
                }
            }
        } else {
            gps.showSettingsAlert(this)
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode, grantResults)
    }

    @NeedsPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    fun getLocation() {
        mapFragment?.getMapAsync(this)
    }

    @OnShowRationale(Manifest.permission.ACCESS_FINE_LOCATION)
    fun showLocationRationale(request: PermissionRequest) {
        PermissionUtils.showRationalDialog(this, R.string.we_will_need_your_location, request)
    }

    @OnNeverAskAgain(Manifest.permission.ACCESS_FINE_LOCATION)
    fun onNeverAskAgainRationale() {
        PermissionUtils.showAppSettingsDialog(
                this,
                R.string.we_will_need_your_location)
    }

    @OnPermissionDenied(Manifest.permission.ACCESS_FINE_LOCATION)
    fun showDeniedForStorage() {
        PermissionUtils.showAppSettingsDialog(
                this, R.string.we_will_need_your_location)
    }

    companion object {
        const val EXTRA_ADDRESS = "EXTRA_ADDRESS"
    }

}
