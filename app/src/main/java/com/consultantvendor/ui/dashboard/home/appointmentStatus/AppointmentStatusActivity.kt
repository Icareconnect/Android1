package com.consultantvendor.ui.dashboard.home.appointmentStatus

import android.Manifest
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.view.WindowManager
import android.view.animation.LinearInterpolator
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.consultantvendor.R
import com.consultantvendor.data.models.responses.Request
import com.consultantvendor.data.models.responses.directions.Overview_polyline
import com.consultantvendor.data.network.ApisRespHandler
import com.consultantvendor.data.network.responseUtil.Status
import com.consultantvendor.data.repos.UserRepository
import com.consultantvendor.databinding.ActivityAppointmentStatusBinding
import com.consultantvendor.ui.drawermenu.DrawerActivity
import com.consultantvendor.utils.*
import com.consultantvendor.utils.PermissionUtils
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import dagger.android.support.DaggerAppCompatActivity
import io.socket.client.Ack
import org.json.JSONObject
import permissions.dispatcher.*
import java.util.*
import javax.inject.Inject
import kotlin.concurrent.schedule
import kotlin.math.sign

@RuntimePermissions
class AppointmentStatusActivity : DaggerAppCompatActivity(), OnMapReadyCallback {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var prefsManager: PrefsManager

    @Inject
    lateinit var appSocket: AppSocket

    @Inject
    lateinit var userRepository: UserRepository

    private lateinit var binding: ActivityAppointmentStatusBinding

    private lateinit var viewModel: DirectionViewModel

    private var mapFragment: SupportMapFragment? = null

    private var mMap: GoogleMap? = null

    private lateinit var placeLatLng: LatLng

    private lateinit var finalLatLng: LatLng

    lateinit var mFusedLocationClient: FusedLocationProviderClient

    private lateinit var geoCoder: Geocoder

    private var request: Request? = null

    private var polyline: Polyline? = null

    private var markerToMove: Marker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_appointment_status)

        initialise()
        setListeners()
        setRequestData()
        bindObservers()
    }

    private fun initialise() {
        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment?.getMapAsync(this)
        viewModel = ViewModelProvider(this, viewModelFactory)[DirectionViewModel::class.java]

        /*Ask for location*/
        geoCoder = Geocoder(this, Locale.getDefault())
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // These flags ensure that the activity can be launched when the screen is locked.
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun setRequestData() {
        request = intent.getSerializableExtra(EXTRA_REQUEST_ID) as Request
        finalLatLng = LatLng(request?.extra_detail?.lat?.toDouble()
                ?: 0.0, request?.extra_detail?.long?.toDouble() ?: 0.0)

        binding.tvName.text = request?.from_user?.name
        binding.tvTime.text = request?.time
        loadImage(binding.ivPic, request?.from_user?.profile_image,
                R.drawable.ic_profile_placeholder)

        when (request?.status) {
            CallAction.INPROGRESS -> {
                binding.tvReached.visible()
                binding.groupOne.gone()
            }
            CallAction.REACHED -> {
                binding.tvReached.gone()
                binding.groupOne.visible()
            }
        }
    }


    private fun setListeners() {
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        binding.tvReached.setOnClickListener {
            request?.status = CallAction.REACHED

            polyline?.remove()
            markerToMove?.remove()
            mMap?.moveCamera(CameraUpdateFactory.newLatLng(finalLatLng))
            mMap?.animateCamera(CameraUpdateFactory.zoomTo(14f))

            binding.tvReached.gone()
            binding.tvTime.gone()
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


    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        if (request?.status == CallAction.INPROGRESS) {
            if (checkPermissions()) {
                if (isLocationEnabled()) {
                    mFusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
                        val location: Location? = task.result
                        if (location == null) {
                            requestNewLocationData()
                        } else {
                            placeLatLng = LatLng(location.latitude, location.longitude)
                            //placeLatLng = LatLng(30.7457, 76.7332)
                            drawPolyLineApi()
                        }
                    }
                } else {
                    Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show()
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(intent)
                }
            } else {
                getLocationWithPermissionCheck()
            }
        } else if (request?.status == CallAction.REACHED) {
            runOnUiThread {
                polyline?.remove()
                mMap?.moveCamera(CameraUpdateFactory.newLatLng(finalLatLng))
                mMap?.animateCamera(CameraUpdateFactory.zoomTo(14f))
            }
        }
    }

    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true
        }
        return false
    }


    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        val mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 0
        mLocationRequest.fastestInterval = 0
        mLocationRequest.numUpdates = 1

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback,
                Looper.myLooper())
    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation: Location = locationResult.lastLocation
            placeLatLng = LatLng(mLastLocation.latitude, mLastLocation.longitude)

            //placeLatLng = LatLng(30.7457, 76.7332)
            drawPolyLineApi()
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
                getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
        )
    }

    private fun drawPolyLineApi() {
        runOnUiThread {
            if (isConnectedToInternet(this, true)) {
                if (request?.status == CallAction.INPROGRESS && markerToMove == null) {
                    markerToMove = mMap?.addMarker(MarkerOptions()
                            .position(placeLatLng)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_location_arrow))
                            .anchor(0.5f, 0.5f)
                            .flat(true))

                } else
                    animateMarker()

                liveLocationUpdate()

                val hashMap = HashMap<String, String>()
                hashMap["origin"] = "${placeLatLng.latitude},${placeLatLng.longitude}"
                hashMap["destination"] = "${finalLatLng.latitude},${finalLatLng.longitude}"
                hashMap["key"] = getString(R.string.google_places_api_key)
                viewModel.directions(hashMap)
            }
        }
    }

    private fun liveLocationUpdate() {
        val obj = JSONObject()
        obj.put("request_id", request?.id)
        obj.put("senderId", request?.to_user?.id)
        obj.put("receiverId", request?.from_user?.id)
        obj.put("lat", placeLatLng.latitude)
        obj.put("long", placeLatLng.longitude)

        appSocket.emit(AppSocket.Events.SEND_LIVE_LOCATION, obj, Ack {

        })
    }

    private fun delayTimeDirectionApi() {
        if (request?.status == CallAction.INPROGRESS) {
            Timer().schedule(30000) {

                /* placeLatLng = LatLng(30.7512, 76.7584)
                 drawPolyLineApi()*/

                getLastLocation()
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap?.isTrafficEnabled = false

        // mMap?.isMyLocationEnabled = true
        mMap?.uiSettings?.isMyLocationButtonEnabled = true

        mMap?.addMarker(MarkerOptions()
                .position(finalLatLng)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_drop_location_mrkr))
                .anchor(0.5f, 0.5f)
                .flat(true))

        if(::placeLatLng.isInitialized) {
            mMap?.moveCamera(CameraUpdateFactory.newLatLng(placeLatLng))
            mMap?.animateCamera(CameraUpdateFactory.zoomTo(14f))
        }

        if (request?.status == CallAction.INPROGRESS && markerToMove == null && ::placeLatLng.isInitialized) {
            markerToMove = mMap?.addMarker(MarkerOptions()
                    .position(placeLatLng)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_location_arrow))
                    .anchor(0.5f, 0.5f)
                    .flat(true))
        }
    }

    private fun moveMarker() {
        markerToMove?.position = placeLatLng
    }

    private fun animateMarker() {
        if (markerToMove != null) {
            val locationStart = Location(LocationManager.GPS_PROVIDER)
            locationStart.latitude = placeLatLng.latitude
            locationStart.longitude = placeLatLng.longitude

            val startPosition = markerToMove?.position ?: LatLng(0.0, 0.0)
            val endPosition = placeLatLng
            val startRotation = markerToMove?.rotation ?: 0f
            val latLngInterpolator: LatLngInterpolator = LatLngInterpolator.LinearFixed()
            val valueAnimator = ValueAnimator.ofFloat(0f, 1f)
            valueAnimator.duration = 1000 // duration 1 second
            valueAnimator.interpolator = LinearInterpolator()
            valueAnimator.addUpdateListener { animation ->
                try {
                    val v = animation.animatedFraction
                    val newPosition = latLngInterpolator.interpolate(v, startPosition, endPosition)
                            ?: LatLng(0.0, 0.0)
                    markerToMove?.position = newPosition
                    markerToMove?.rotation = computeRotation(v, startRotation, locationStart.bearing)
                } catch (ex: Exception) {
                    // I don't care atm..
                }
            }
            valueAnimator.start()
        }
    }

    private interface LatLngInterpolator {
        fun interpolate(fraction: Float, a: LatLng, b: LatLng): LatLng?
        class LinearFixed : LatLngInterpolator {
            override fun interpolate(fraction: Float, a: LatLng, b: LatLng): LatLng {
                val lat = (b.latitude - a.latitude) * fraction + a.latitude
                var lngDelta = b.longitude - a.longitude
                // Take the shortest path across the 180th meridian.
                if (Math.abs(lngDelta) > 180) {
                    lngDelta -= sign(lngDelta) * 360
                }
                val lng = lngDelta * fraction + a.longitude
                return LatLng(lat, lng)
            }
        }
    }

    private fun computeRotation(fraction: Float, start: Float, end: Float): Float {
        val normalizeEnd = end - start // rotate start to 0
        val normalizedEndAbs = (normalizeEnd + 360) % 360
        val direction = if (normalizedEndAbs > 180) (-1).toFloat() else 1.toFloat() // -1 = anticlockwise, 1 = clockwise
        val rotation: Float
        rotation = if (direction > 0) {
            normalizedEndAbs
        } else {
            normalizedEndAbs - 360
        }
        val result = fraction * rotation + start
        return (result + 360) % 360
    }


    private fun bindObservers() {
        viewModel.directions.observe(this, Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {

                    if (request?.status == CallAction.INPROGRESS) {
                        binding.tvTime.text = getString(R.string.estimate_time_of_arrival_s,
                                it.data?.routes?.get(0)?.legs?.get(0)?.duration?.text)
                        drawDirectionToStop(it.data?.routes?.get(0)?.overview_polyline)
                    }

                }
                Status.ERROR -> {
                    ApisRespHandler.handleError(it.error, this, prefsManager)
                }
                Status.LOADING -> {

                }
            }
        })
    }

    override fun onBackPressed() {
        if (request?.status == CallAction.INPROGRESS) {
            AlertDialogUtil.instance.createOkCancelDialog(this, R.string.quit,
                    R.string.quit_message, R.string.yes, R.string.no, false,
                    object : AlertDialogUtil.OnOkCancelDialogListener {
                        override fun onOkButtonClicked() {
                            finish()
                        }

                        override fun onCancelButtonClicked() {
                        }
                    }).show()
        } else
            super.onBackPressed()
    }

    private fun drawDirectionToStop(overviewPolyline: Overview_polyline?) {
        if (overviewPolyline != null) {
            val polyz = decodeOverviewPolyLinePonts(overviewPolyline.points)
            if (polyz != null) {
                val lineOptions = PolylineOptions()
                lineOptions.addAll(polyz)
                lineOptions.width(20f)
                lineOptions.color(ContextCompat.getColor(this, R.color.colorBlack))

                polyline?.remove()

                polyline = mMap?.addPolyline(lineOptions)
                mMap?.moveCamera(CameraUpdateFactory.newLatLng(placeLatLng))
                mMap?.animateCamera(CameraUpdateFactory.zoomTo(14f))

                delayTimeDirectionApi()
            }
        }
    }


    //This function is to parse the value of "points"
    private fun decodeOverviewPolyLinePonts(encoded: String?): List<LatLng>? {
        val poly = ArrayList<LatLng>()
        if (encoded != null && encoded.isNotEmpty() && encoded.trim { it <= ' ' }.isNotEmpty()) {
            var index = 0
            val len = encoded.length
            var lat = 0
            var lng = 0
            while (index < len) {
                var b: Int
                var shift = 0
                var result = 0
                do {
                    b = encoded[index++].toInt() - 63
                    result = result or (b and 0x1f shl shift)
                    shift += 5
                } while (b >= 0x20)
                val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
                lat += dlat
                shift = 0
                result = 0
                do {
                    b = encoded[index++].toInt() - 63
                    result = result or (b and 0x1f shl shift)
                    shift += 5
                } while (b >= 0x20)
                val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
                lng += dlng
                val p = LatLng(lat.toDouble() / 1E5,
                        lng.toDouble() / 1E5)
                poly.add(p)
            }
        }
        return poly
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode, grantResults)
    }

    @NeedsPermission(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
    fun getLocation() {
        getLastLocation()
    }

    @OnShowRationale(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
    fun showLocationRationale(request: PermissionRequest) {
        PermissionUtils.showRationalDialog(this, R.string.we_will_need_your_location, request)
    }

    @OnNeverAskAgain(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
    fun onNeverAskAgainRationale() {
        PermissionUtils.showAppSettingsDialog(
                this,
                R.string.we_will_need_your_location)
    }

    @OnPermissionDenied(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
    fun showDeniedForStorage() {
        PermissionUtils.showAppSettingsDialog(
                this, R.string.we_will_need_your_location)
    }

    @SuppressLint("NoDelegateOnResumeDetector")
    override fun onResume() {
        super.onResume()
        getLocationWithPermissionCheck()
    }

}
