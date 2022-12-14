/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.laba.partner.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Binder
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.RemoteViews
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.content.edit
import com.google.android.gms.location.*
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.gson.Gson
import com.laba.partner.LatLngPointModel
import com.laba.partner.MvpApplication
import com.laba.partner.R
import com.laba.partner.base.BaseActivity
import com.laba.partner.common.Constants
import com.laba.partner.common.SharedHelper
import com.laba.partner.data.network.APIClient
import com.laba.partner.data.network.model.LatLngFireBaseDB
import com.laba.partner.ui.activity.main.MainActivity
import com.laba.partner.widget.SimpleFloatingWindow
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import jp.co.recruit_lifestyle.android.floatingview.FloatingViewListener
import org.imperiumlabs.geofirestore.GeoFirestore
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


/**
 * Service tracks location when requested and updates Activity via binding. If Activity is
 * stopped/unbinds and tracking is enabled, the service promotes itself to a foreground service to
 * insure location updates aren't interrupted.
 *
 * For apps running in the background on O+ devices, location is computed much less than previous
 * versions. Please reference documentation for details.
 */
/*fun Location?.toText(): String {
    return if (this != null) {
        "($latitude, $longitude)"
    } else {
        "Unknown location"
    }
}*/

class FetchLocationService : Service(), FloatingViewListener,
    ConnectivityReceiver.ConnectivityReceiverListener {
    /*
     * Checks whether the bound activity has really gone away (foreground service with notification
     * created) or simply orientation change (no-op).
     */
    private var configurationChange = false

    private var serviceRunningInForeground = false

    private val localBinder = LocalBinder()

    private lateinit var notificationManager: NotificationManager


    private lateinit var mLocationManager: LocationManager
    private lateinit var mLocationListener: LocationListener

    // FusedLocationProviderClient - Main class for receiving location updates.
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    // LocationRequest - Requirements for the location updates, i.e., how often you should receive
    // updates, the priority, etc.
    private lateinit var locationRequest: LocationRequest

    // LocationCallback - Called when FusedLocationProviderClient has a new Location.
    private lateinit var locationCallback: LocationCallback

    // Used only for local storage of the last known location. Usually, this would be saved to your
    // database, but because this is a simplified sample without a full database, we only need the
    // last location to create a Notification if the user navigates away from the app.
    private var currentLocation: Location? = null

    private var mNotificationTitle: String = "You are currently online"

    val EXTRA_CUTOUT_SAFE_AREA = "cutout_safe_area"
    //private var mFloatingViewManager: FloatingViewManager? = null
    private  var simpleFloatingWindow: SimpleFloatingWindow? = null
    lateinit var mGeoFire: GeoFirestore
    private var connectivityReceiver: ConnectivityReceiver? = null


    override fun onCreate() {
        super.onCreate()
        val intentFilter = IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
        connectivityReceiver = ConnectivityReceiver();

        registerReceiver(
            connectivityReceiver,
            IntentFilter(intentFilter)
        )
        mGeoFire = GeoFirestore(FirebaseFirestore.getInstance().collection(Constants.FIREBASE_LOCATION_DB))


        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        locationRequest = LocationRequest.create().apply {
            // Sets the desired interval for active location updates. This interval is inexact. You
            // may not receive updates at all if no location sources are available, or you may
            // receive them less frequently than requested. You may also receive updates more
            // frequently than requested if other applications are requesting location at a more
            // frequent interval.
            //
            // IMPORTANT NOTE: Apps running on Android 8.0 and higher devices (regardless of
            // targetSdkVersion) may receive updates less frequently than this interval when the app
            // is no longer in the foreground.
            interval = TimeUnit.SECONDS.toMillis(10)
            smallestDisplacement = 3f

            // Sets the fastest rate for active location updates. This interval is exact, and your
            // application will never receive updates more frequently than this value.
            fastestInterval = TimeUnit.SECONDS.toMillis(5)

            // Sets the maximum time when batched location updates are delivered. Updates may be
            // delivered sooner than this interval.
            maxWaitTime = TimeUnit.MINUTES.toMillis(2)

            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                Log.e(TAG,"Fused location")
                updateLocation(locationResult.lastLocation)
            }
        }
        mLocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        mLocationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                Log.e(TAG, "LocationManager")
                updateLocation(location)
            }

            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
            override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand()")


        // Tells the system not to recreate the service after it's been killed.
        val cancelLocationTrackingFromNotification =
            intent.getBooleanExtra(EXTRA_CANCEL_LOCATION_TRACKING_FROM_NOTIFICATION, false)

        if (cancelLocationTrackingFromNotification) {
            Log.e(TAG, "cancelLocationTrackingFromNotification")
            unsubscribeToLocationUpdates()
        }
        if (simpleFloatingWindow==null){
            simpleFloatingWindow = SimpleFloatingWindow(applicationContext)
         /*if (mFloatingViewManager == null) {
            mFloatingViewManager = FloatingViewManager(this, this)
            //mFloatingViewManager.setFixedTrashIconImage(R.drawable.ic_trash_fixed);
            //mFloatingViewManager.setFixedTrashIconImage(R.drawable.ic_trash_fixed);
            mFloatingViewManager!!.isTrashViewEnabled = false
            //mFloatingViewManager.setActionTrashIconImage(R.drawable.ic_trash_action);
            //mFloatingViewManager.setActionTrashIconImage(R.drawable.ic_trash_action);
            mFloatingViewManager!!.setDisplayMode(FloatingViewManager.DISPLAY_MODE_SHOW_ALWAYS)
            mFloatingViewManager!!.setSafeInsetRect(
                intent.getParcelableExtra<Parcelable>(
                    EXTRA_CUTOUT_SAFE_AREA
                ) as Rect?
            )*/
            val notifications = notificationManager.activeNotifications
            var show = false
            for (notification in notifications) {
                if (notification.id == NOTIFICATION_ID) {
                    show = true
                }
            }
            if (show) {
                val serviceStatus = SharedHelper.getKey(this, "serviceStatus")
                if (!serviceStatus.equals(Constants.User.Service.offLine, ignoreCase = true)) {
                    showFloatingButton()
                }
            }
            //return START_REDELIVER_INTENT
        }
        return START_REDELIVER_INTENT;
        //return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder {
        Log.d(TAG, "onBind()")

        // MainActivity (client) comes into foreground and binds to service, so the service can
        // become a background services.
        stopForeground(false)
        serviceRunningInForeground = false
        configurationChange = false
        return localBinder
    }

    override fun onRebind(intent: Intent) {
        Log.d(TAG, "onRebind()")

        // MainActivity (client) returns to the foreground and rebinds to service, so the service
        // can become a background services.
        stopForeground(false)
        serviceRunningInForeground = false
        configurationChange = false
        super.onRebind(intent)
    }

    override fun onUnbind(intent: Intent): Boolean {
        Log.d(TAG, "onUnbind()")

        // MainActivity (client) leaves foreground, so service needs to become a foreground service
        // to maintain the 'while-in-use' label.
        // NOTE: If this method is called due to a configuration change in MainActivity,
        // we do nothing.
        if (!configurationChange && SharedPreferenceUtil.getLocationTrackingPref(this)) {
            Log.d(TAG, "Start foreground service")
            val notification = generateNotification()
            mNotificationTitle = getNotificationTitle()
            notification.setContentTitle(mNotificationTitle)
            notification.setNotificationSilent()
            notification.priority = Notification.PRIORITY_MIN;
            val noti = notification.build()
            //noti.flags = Notification.FLAG_ONGOING_EVENT;
            startForeground(NOTIFICATION_ID, noti)
            serviceRunningInForeground = true

        }

        // Ensures onRebind() is called if MainActivity (client) rebinds.
        return true
    }

    override fun stopService(name: Intent?): Boolean {
        cancelNotification()
        return super.stopService(name)
    }

    override fun onDestroy() {
        cancelNotification()
        unregisterReceiver(connectivityReceiver)
        super.onDestroy()

        Log.d(TAG, "onDestroy()")
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        configurationChange = true
    }

    @RequiresPermission(anyOf = ["android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION"])
    fun subscribeToLocationUpdates() {
        //Log.e("eeeeeeee SS", "|| " + SharedHelper.getKey(this, "serviceStatus"))
        Log.e(TAG, "subscribeToLocationUpdates()")


        SharedPreferenceUtil.saveLocationTrackingPref(this, true)

        // Binding to this service doesn't actually trigger onStartCommand(). That is needed to
        // ensure this Service can be promoted to a foreground service, i.e., the service needs to
        // be officially started (which we do here).
        startService(Intent(applicationContext, FetchLocationService::class.java))
        showNotification()
        try {

            //fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                null
            )

            mLocationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                5000,
                3.toFloat(),
                mLocationListener
            )

        } catch (unlikely: SecurityException) {
            SharedPreferenceUtil.saveLocationTrackingPref(this, false)
            Log.e(TAG, "Lost location permissions. Couldn't remove updates. $unlikely")
        }
    }

    fun unsubscribeToLocationUpdates() {
        //Log.e("eeeeeeee UU", "|| " + SharedHelper.getKey(this, "serviceStatus"))
        Log.e(TAG, "unsubscribeToLocationUpdates()")
        SharedPreferenceUtil.saveLocationTrackingPref(this, false)
        try {
            val removeTask = fusedLocationProviderClient.removeLocationUpdates(locationCallback)
            removeTask.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Location Callback removed.")
                    //stopSelf()
                } else {
                    Log.d(TAG, "Failed to remove Location Callback.")
                }
            }
            mLocationManager.removeUpdates(mLocationListener)
            mGeoFire.removeLocation(SharedHelper.getKey(this, Constants.SharedPref.user_id))
            currentLocation = null
        } catch (unlikely: SecurityException) {
            //SharedPreferenceUtil.saveLocationTrackingPref(this, true)
            Log.e(TAG, "Lost location permissions. Couldn't remove updates. $unlikely")
        }
        //stopSelf()
        showNotification()
    }

    private var key: String? = null
    private var lastRideStatus: Boolean? = null
    fun updateLocation(location: Location?) {

        val serviceStatus = SharedHelper.getKey(this, "serviceStatus");
        /*
        if (SharedHelper.getKey(this, Constants.SharedPref.logged_in).equals("true", ignoreCase = true) &&
            (isOnRide || serviceStatus.equals(Constants.User.Service.active,true))) {*/

        if (!serviceStatus.equals(Constants.User.Service.offLine, true)) {

            val isOnRideStatus = SharedHelper.getKey(this, "is_OnRide")
            val isOnRide = (isOnRideStatus != null && isOnRideStatus != "null" && isOnRideStatus.isNotEmpty() && !isOnRideStatus.equals("0", ignoreCase = true))
            val userId = SharedHelper.getKey(this, Constants.SharedPref.user_id);


            if (lastRideStatus != null && lastRideStatus == isOnRide &&
                currentLocation != null && currentLocation!!.distanceTo(location) < 8) {
                return
            }
            lastRideStatus = isOnRide
            currentLocation = location
            /*Log.e(
                "updateLocation eee",
                "latitude : ${currentLocation!!.latitude} ,longitude  to ${currentLocation!!.longitude}"
            )*/
            /* val location = hashMapOf(
                 "latitude" to currentLocation!!.latitude,
                 "longitude" to currentLocation!!.longitude)*/

            //if (serviceStatus.equals(Constants.User.Service.active,true)) {


            val map = HashMap<String, Any>()
            map["latitude"] = currentLocation!!.latitude
            map["longitude"] = currentLocation!!.longitude
            APIClient.getAPIClient().postCurrentLocation(map).subscribeOn(Schedulers.io())
                .subscribe(object : DisposableSingleObserver<Any?>() {
                    override fun onSuccess(o: Any) {
                        Log.d(TAG, o.toString())
                    }

                    override fun onError(e: Throwable) {
                        Log.e(TAG, e.toString())
                    }
                })
            // firebase
            if (isOnRide){
                mGeoFire.removeLocation(userId)
            }else{
                mGeoFire.setLocation(
                    userId,
                    GeoPoint(currentLocation!!.latitude, currentLocation!!.longitude)
                )
            }

            if (MainActivity.myLocationCalculationCheck) {
                location?.let {
                    locationProcessing(it)
                }
            }

            if (BaseActivity.DATUM == null) return

            if (BaseActivity.DATUM.status.equals("ACCEPTED", ignoreCase = true)
                || BaseActivity.DATUM.status.equals("STARTED", ignoreCase = true)
                || BaseActivity.DATUM.status.equals("ARRIVED", ignoreCase = true)
                || BaseActivity.DATUM.status.equals("PICKEDUP", ignoreCase = true)
            /*|| DATUM.getStatus().equalsIgnoreCase("DROPPED")*/) {

                MvpApplication.mLastKnownLocation = location
                applicationContext.sendBroadcast(Intent("INTENT_FILTER"))
                saveLocationToFireBaseDB(
                    MvpApplication.mLastKnownLocation.latitude,
                    MvpApplication.mLastKnownLocation.longitude
                )
            }
        } else {
            Log.e(TAG, "cancelLocationTracking Status chaged")
            unsubscribeToLocationUpdates()
        }


        // Updates notification content if this service is running as a foreground
        // service.
        if (serviceRunningInForeground) {
            showNotification()
        }
    }

    fun showNotification() {
        val serviceStatus = SharedHelper.getKey(this, "serviceStatus")
        /*val isOnRideStatus = SharedHelper.getKey(this, "is_OnRide")
        val isOnRide = (isOnRideStatus != null && isOnRideStatus != "null" && isOnRideStatus.isNotEmpty() && !isOnRideStatus.equals("0", ignoreCase = true))
        if (serviceStatus.equals(Constants.User.Service.offLine, ignoreCase = true) && !isOnRide) {*/
        if (serviceStatus.equals(Constants.User.Service.offLine, ignoreCase = true)) {
            ConnectivityReceiver.connectivityReceiverListener = null
            mGeoFire.removeLocation(SharedHelper.getKey(this, Constants.SharedPref.user_id))
            currentLocation = null
            cancelNotification()
            return
        }

        //  val mNotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val notifications = notificationManager.activeNotifications
        var show = true
        val title = getNotificationTitle()

        if (mNotificationTitle.equals(title, true)) {
            for (notification in notifications) {
                if (notification.id == NOTIFICATION_ID) {
                    show = false
                }
            }
        }


        if (show) {
            mNotificationTitle = getNotificationTitle()
            val noti = generateNotification()
                .setContentTitle(mNotificationTitle)
                .build()
            noti.flags = Notification.FLAG_ONGOING_EVENT
            notificationManager.notify(NOTIFICATION_ID, noti)

            ConnectivityReceiver.connectivityReceiverListener = this
            showFloatingButton()
        }
    }

    private fun cancelNotification() {
        /*if (mFloatingViewManager != null) {
            mFloatingViewManager!!.removeAllViewToWindow()
        }*/
        if (simpleFloatingWindow !=null){
            simpleFloatingWindow!!.dismiss()
        }

        notificationManager.cancel(NOTIFICATION_ID) // Notification ID to cancel

        //stopSelf()
    }

    private fun showFloatingButton() {
        if (simpleFloatingWindow !=null){
            simpleFloatingWindow!!.show()
        }

        /* if (mFloatingViewManager != null) {
             mFloatingViewManager!!.removeAllViewToWindow()
             //val metrics =resources.displayMetrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT
             //val metrics = (Resources.getSystem().displayMetrics.density)
             *//* val metrics = DisplayMetrics()
             val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
             windowManager.defaultDisplay.getMetrics(metrics)*//*
            val options = FloatingViewManager.Options()
            //options.shape=FloatingViewManager.SHAPE_CIRCLE
            // return dp * ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
            //options.floatingViewWidth=((90 * metrics).toInt())
            //options.floatingViewHeight=((90 * metrics).toInt())
            options.floatingViewWidth = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                85f,
                resources.displayMetrics
            ).toInt()
            options.floatingViewHeight = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                72f,
                resources.displayMetrics
            ).toInt()
            options.overMargin = -20

            val inflater = LayoutInflater.from(this)
            val iconView =
                inflater.inflate(R.layout.widget_chathead, null, false) as CircleImageView
            iconView.setOnClickListener { v: View? ->
                if (Utilities.isAppIsInBackground(applicationContext)) {
                    val isLogged =
                        SharedHelper.getKey(applicationContext, "loggged_in", "false")
                    if (isLogged.equals("true", ignoreCase = true)) {
                        val intent1 = Intent(applicationContext, MainActivity::class.java)
                        intent1.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent1)
                    }
                }
            }
            mFloatingViewManager!!.addViewToWindow(iconView, options)
        }*/
    }


    private fun saveLocationToFireBaseDB(lat: Double, lng: Double) {
        val refPath = "loc_p_" + BaseActivity.DATUM.providerId
        println("RRR GPSTracker.saveLocationToFireBaseDB :: $refPath")
        if (refPath != "loc_p_0") try {
            val mLocationRef = FirebaseDatabase.getInstance().getReference(refPath)
            key = if (key == null) mLocationRef.push().key else key
            mLocationRef.setValue(LatLngFireBaseDB(lat, lng))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun locationProcessing(location: Location) {
        var pointModels = SharedHelper.getLocation(this)
        if (pointModels == null) pointModels = ArrayList()
        val latLngPoint = LatLngPointModel()
        latLngPoint.id = pointModels.size
        latLngPoint.lat = location.latitude
        latLngPoint.lng = location.longitude
        latLngPoint.timeStamp = getCurrentTime()
        pointModels.add(latLngPoint)
        SharedHelper.putLocation(this, Gson().toJson(pointModels))
        println("  TimeStamp = " + latLngPoint.timeStamp)
    }

    private fun getCurrentTime(): String? {
        return SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().time)
    }

    private fun getNotificationTitle(): String {
        val serviceStatus = SharedHelper.getKey(this, "serviceStatus")
        val is_OnRide = SharedHelper.getKey(this, "is_OnRide")

        if (serviceStatus.equals(Constants.User.Service.active, ignoreCase = true)) {
            return "You are currently online"
        } else if (is_OnRide != null && is_OnRide != "null" && is_OnRide.isNotEmpty() && !is_OnRide.equals(
                "0",
                ignoreCase = true
            )
        ) {
            return "You are currently on ride"
        } else if (serviceStatus.equals(Constants.User.Service.offLine, ignoreCase = true)) {
            return "You are currently offline"
        }
        return "Current location sharing"
    }

    /*
     * Generates a BIG_TEXT_STYLE Notification that represent latest location.
     */
    private fun generateNotification(): NotificationCompat.Builder {
        //Log.d(TAG, "generateNotification()")
        // Main steps for building a BIG_TEXT_STYLE notification:
        //      0. Get data
        //      1. Create Notification Channel for O+
        //      2. Build the BIG_TEXT_STYLE
        //      3. Set up Intent / Pending Intent for notification
        //      4. Build and issue the notification

        // 0. Get data
        //val mainNotificationText = location?.toText() ?: getString(R.string.no_location_text)
        val titleText = "Tracking Your Location"

        // 1. Create Notification Channel for O+ and beyond devices (26+).
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val notificationChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID, titleText, NotificationManager.IMPORTANCE_HIGH
            )
            notificationChannel.setLightColor(Color.BLUE);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            // Adds NotificationChannel to system. Attempting to create an
            // existing notification channel with its original values performs
            // no operation, so it's safe to perform the below sequence.
            notificationManager.createNotificationChannel(notificationChannel)
        }

        // 2. Build the BIG_TEXT_STYLE.
        //val bigTextStyle = NotificationCompat.BigTextStyle().bigText(mainNotificationText).setBigContentTitle(titleText)

        // 3. Set up main Intent/Pending Intents for notification.
        //val launchActivityIntent = Intent(this, HomeActivity::class.java)

        //val cancelIntent = Intent(this, FetchLocationService::class.java)
        //cancelIntent.putExtra(EXTRA_CANCEL_LOCATION_TRACKING_FROM_NOTIFICATION, true)

        // val servicePendingIntent = PendingIntent.getService( this, 0, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        // val activityPendingIntent = PendingIntent.getActivity(this, 0, launchActivityIntent, 0)

        // 4. Build and issue the notification.
        // Notification Channel Id is ignored for Android pre O (26).
        val notificationCompatBuilder =
            NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_ID)


        val resultIntent = Intent(this, MainActivity::class.java)
        resultIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

      /*  val resultPendingIntent =
            PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_CANCEL_CURRENT) */

          val resultPendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
              PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_IMMUTABLE)
          } else {
              PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_CANCEL_CURRENT)
          }


        val build = notificationCompatBuilder
            .setOngoing(true)
            //.setStyle(bigTextStyle)
            .setContentTitle(titleText)
            //  .setContentText(mainNotificationText)
            //.setSmallIcon(MyFirebaseMessagingService.getNotificationIcon())
            .setSmallIcon(R.drawable.noti_icon_new)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setCategory(Notification.CATEGORY_SERVICE)
            .setAutoCancel(false)
            //.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(resultPendingIntent)
        /*.addAction(
            R.drawable.ic_apple, getString(R.string.launch_activity),
            activityPendingIntent
        )
        .addAction(
            R.drawable.ic_close,
            getString(R.string.stop_location_updates_button_text),
            servicePendingIntent
        )*/
        //.build()

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            build.priority = NotificationManager.IMPORTANCE_MAX
        }

        return build
    }

    /**
     * Class used for the client Binder.  Since this service runs in the same process as its
     * clients, we don't need to deal with IPC.
     */
    inner class LocalBinder : Binder() {
      //  internal val service: FetchLocationService
     //       get() = this@FetchLocationService
        fun getService() = this@FetchLocationService

    }

    companion object {
        private const val TAG = "ForegroundOnlyLocationService"

        private const val PACKAGE_NAME = "com.example.android.whileinuselocation"

        //internal const val ACTION_FOREGROUND_ONLY_LOCATION_BROADCAST = "$PACKAGE_NAME.action.FOREGROUND_ONLY_LOCATION_BROADCAST"

        internal const val EXTRA_LOCATION = "$PACKAGE_NAME.extra.LOCATION"

        private const val EXTRA_CANCEL_LOCATION_TRACKING_FROM_NOTIFICATION =
            "$PACKAGE_NAME.extra.CANCEL_LOCATION_TRACKING_FROM_NOTIFICATION"

        private const val NOTIFICATION_ID = 12345678

        private const val NOTIFICATION_CHANNEL_ID = "while_in_use_channel_01"
    }

    internal object SharedPreferenceUtil {

        const val KEY_FOREGROUND_ENABLED = "tracking_foreground_location"

        /**
         * Returns true if requesting location updates, otherwise returns false.
         *
         * @param context The [Context].
         */
        fun getLocationTrackingPref(context: Context): Boolean =
            context.getSharedPreferences(
                context.getString(R.string.preference_file_key), Context.MODE_PRIVATE
            )
                .getBoolean(KEY_FOREGROUND_ENABLED, false)

        /**
         * Stores the location updates state in SharedPreferences.
         * @param requestingLocationUpdates The location updates state.
         */
        fun saveLocationTrackingPref(context: Context, requestingLocationUpdates: Boolean) =
            context.getSharedPreferences(
                context.getString(R.string.preference_file_key),
                Context.MODE_PRIVATE
            ).edit {
                putBoolean(KEY_FOREGROUND_ENABLED, requestingLocationUpdates)
            }
    }

    override fun onFinishFloatingView() {
    }

    override fun onTouchFinished(isFinishing: Boolean, x: Int, y: Int) {
    }

    override fun onNetworkConnectionChanged(isConnected: Boolean) {
        if (!isConnected) {

            val notificationLayout = RemoteViews(packageName, R.layout.notification_red)
            notificationLayout.setTextViewText(
                R.id.notification_title,
                "Please turn on your internet"
            );
            // Apply the layouts to the notification
            val notification = generateNotification()
                // val customNotification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setStyle(NotificationCompat.DecoratedCustomViewStyle())
                .setCustomContentView(notificationLayout)
                .setContentTitle("Please turn on your internet")
                .setCustomBigContentView(notificationLayout)
                .build()

            notification.flags = Notification.FLAG_ONGOING_EVENT;
            notificationManager.notify(1122, notification)
        } else {
            notificationManager.cancel(1122)
        }
    }

    private val TWO_MINUTES = 1000 * 60 * 2
    protected fun isBetterLocation(location: Location, currentBestLocation: Location?): Boolean {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true
        }

        // Check whether the new location fix is newer or older
        val timeDelta = location.time - currentBestLocation.time
        val isSignificantlyNewer: Boolean = timeDelta > TWO_MINUTES
        val isSignificantlyOlder: Boolean = timeDelta < -TWO_MINUTES
        val isNewer = timeDelta > 0

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false
        }

        // Check whether the new location fix is more or less accurate
        val accuracyDelta = (location.accuracy - currentBestLocation.accuracy).toInt()
        val isLessAccurate = accuracyDelta > 0
        val isMoreAccurate = accuracyDelta < 0
        val isSignificantlyLessAccurate = accuracyDelta > 200

        // Check if the old and new location are from the same provider
        val isFromSameProvider: Boolean = isSameProvider(
            location.provider,
            currentBestLocation.provider
        )

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true
        } else if (isNewer && !isLessAccurate) {
            return true
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true
        }
        return false
    }

    /** Checks whether two providers are the same  */
    private fun isSameProvider(provider1: String?, provider2: String?): Boolean {
        return if (provider1 == null) {
            provider2 == null
        } else provider1 == provider2
    }

}
