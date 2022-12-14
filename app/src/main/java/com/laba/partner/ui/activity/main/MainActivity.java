package com.laba.partner.ui.activity.main;

import static com.laba.partner.MvpApplication.DEFAULT_ZOOM;
import static com.laba.partner.MvpApplication.mLastKnownLocation;
import static com.laba.partner.common.SharedHelper.getLocation;
import static com.laba.partner.common.SharedHelper.putLocation;
import static com.laba.partner.common.fcm.MyFireBaseMessagingService.INTENT_FILTER;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Leg;
import com.akexorcist.googledirection.model.Route;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.laba.partner.BuildConfig;
import com.laba.partner.R;
import com.laba.partner.base.BaseActivity;
import com.laba.partner.common.Constants;
import com.laba.partner.common.LocaleHelper;
import com.laba.partner.common.SharedHelper;
import com.laba.partner.common.Utilities;
import com.laba.partner.common.fcm.ForceUpdateChecker;
import com.laba.partner.common.fcm.MyFireBaseMessagingService;
import com.laba.partner.common.swipe_button.SwipeButton;
import com.laba.partner.data.network.model.HomeCheck;
import com.laba.partner.data.network.model.InitSettingsResponse;
import com.laba.partner.data.network.model.TripResponse;
import com.laba.partner.data.network.model.UserResponse;
import com.laba.partner.service.CheckScheduleService;
import com.laba.partner.service.FetchLocationService;
import com.laba.partner.ui.activity.card.CardActivity;
import com.laba.partner.ui.activity.document.DocumentActivity;
import com.laba.partner.ui.activity.earnings.EarningsActivity;
import com.laba.partner.ui.activity.help.HelpActivity;
import com.laba.partner.ui.activity.invite.InviteActivity;
import com.laba.partner.ui.activity.profile.ProfileActivity;
import com.laba.partner.ui.activity.setting.SettingsActivity;
import com.laba.partner.ui.activity.summary.SummaryActivity;
import com.laba.partner.ui.activity.wallet.WalletActivity;
import com.laba.partner.ui.activity.your_trips.YourTripActivity;
import com.laba.partner.ui.bottomsheetdialog.invoice_flow.InvoiceDialogFragment;
import com.laba.partner.ui.bottomsheetdialog.rating.RatingDialogFragment;
import com.laba.partner.ui.dialog.UserCancelledRideDialog;
import com.laba.partner.ui.fragment.incoming_request.IncomingRequestFragment;
import com.laba.partner.ui.fragment.offline.OfflineFragment;
import com.laba.partner.ui.fragment.status_flow.StatusFlowFragment;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends BaseActivity implements
        MainIView,
        NavigationView.OnNavigationItemSelectedListener,
        OnMapReadyCallback,
        GoogleMap.OnCameraMoveListener,
        GoogleMap.OnCameraIdleListener,
        DirectionCallback,
        ForceUpdateChecker.OnUpdateNeededListener {

    protected static final String TAG = "LocationOnOff";


    private GoogleApiClient googleApiClient;
    final static int REQUEST_LOCATION = 199;

    private static final int CHATHEAD_OVERLAY_PERMISSION_REQUEST_CODE = 101;
    private static final int APP_PERMISSION_REQUEST = 102;
    public static boolean myLocationCalculationCheck = false;
    //private final LatLng mDefaultLocation = new LatLng(-33.8523341, 151.2106085);
    DisableDriver disableDriver;
    @BindView(R.id.container)
    FrameLayout container;
    @BindView(R.id.menu)
    ImageView menu;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;
    @BindView(R.id.top_layout)
    LinearLayout topLayout;
    @BindView(R.id.lnrLocationHeader)
    LinearLayout lnrLocationHeader;
    @BindView(R.id.lblLocationType)
    TextView lblLocationType;
    @BindView(R.id.lblLocationName)
    TextView lblLocationName;
    @BindView(R.id.offline_container)
    FrameLayout offlineContainer;
    @BindView(R.id.nav_view)
    NavigationView navView;
    @BindView(R.id.gps)
    ImageView gps;
    @BindView(R.id.navigation_img)
    ImageView navigationImg;
    @BindView(R.id.sbChangeStatus)
    SwipeButton sbChangeStatus;
    TextView lblMenuName, lblMenuEmail;
    ImageView imgMenu, imgStatus;
    MainPresenter presenter = new MainPresenter();
    SupportMapFragment mapFragment;
    GoogleMap googleMap;
    private String title;
    private NavigationView navigationView;
    /*private Runnable r;
    private Handler h;*/
    private Handler checkHandler;
    private Runnable checkRunnable;
    private final int delay = 5000;
    private String STATUS = "";
    private String ACCOUNTSTATUS = "";
    private FusedLocationProviderClient mFusedLocation;


    boolean isFromTimer = false;

    //    private BottomSheetBehavior bottomSheetBehavior;
    private final BroadcastReceiver myReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            int status = intent.getIntExtra(Constants.INTENT_BROADCAST, -1);
            Log.d("myReceiver", "onReceive: " + status);
            if (status == Constants.BC_OFFLINE) {
                putOffline();
                offlineFragment("");
                return;
            }


            String message = intent.getStringExtra("msg");
            if (message != null && !message.isEmpty()) {
                if (message.equalsIgnoreCase("User Cancelled the Ride")) {
                    new UserCancelledRideDialog(MainActivity.this).show();
                }
            }

            HashMap<String, Object> params = new HashMap<>();
            if (mLastKnownLocation != null) {
                params.put("latitude", mLastKnownLocation.getLatitude());
                params.put("longitude", mLastKnownLocation.getLongitude());
            }
            System.out.println("RRR BroadcastReceiver called...");
            presenter.getTrip(params);
        }
    };
    private FetchLocationService foregroundOnlyLocationService;
    private boolean foregroundOnlyLocationServiceBound = false;
    private final ServiceConnection foregroundOnlyServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            FetchLocationService.LocalBinder binder = (FetchLocationService.LocalBinder) service;
       //     foregroundOnlyLocationService = binder.getService$LabaCabsDriver_app();
    //        foregroundOnlyLocationService = binder.getService$app_release();
      //      foregroundOnlyLocationService = binder.getService$app_debug();
            foregroundOnlyLocationService = binder.getService();
            foregroundOnlyLocationServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            foregroundOnlyLocationService = null;
            foregroundOnlyLocationServiceBound = false;
        }
    };
    /*private val foregroundOnlyServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service:IBinder) {
            val binder = service as FetchLocationService.LocalBinder
                    foregroundOnlyLocationService = binder.service
            foregroundOnlyLocationServiceBound = true
        }
        override fun onServiceDisconnected(name: ComponentName) {
            foregroundOnlyLocationService = null
            foregroundOnlyLocationServiceBound = false
        }
    }*/
    private final ActivityResultLauncher<String[]> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), permissions -> {
                /*if (permissions.get(Manifest.permission.ACCESS_FINE_LOCATION) ==true ||
                        permissions.get(Manifest.permission.ACCESS_FINE_LOCATION) ==true ) {
                    startForegroundService();
                } else {
                    putOffline();
                }*/

                if (checkLocationPermission()) {
                    getDeviceLocation();
                    updateLocationUI();
                } else {
                    putOffline();
                }
            });

    private void startFloatingViewService() {
        // *** You must follow these rules when obtain the cutout(FloatingViewManager.findCutoutSafeArea) ***
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // 1. 'windowLayoutInDisplayCutoutMode' do not be set to 'never'
            if (getWindow().getAttributes().layoutInDisplayCutoutMode == WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_NEVER) {
                throw new RuntimeException("'windowLayoutInDisplayCutoutMode' do not be set to 'never'");
            }
            // 2. Do not set Activity to landscape
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                throw new RuntimeException("Do not set Activity to landscape");
            }
        }

        /*String serviceStatus = SharedHelper.getKey(this, "serviceStatus");
        if (serviceStatus.equalsIgnoreCase(Constants.User.Service.active)) {
            //final Intent intent = new Intent(this,  ChatHeadService.class);
            //intent.putExtra(ChatHeadService.EXTRA_CUTOUT_SAFE_AREA, FloatingViewManager.findCutoutSafeArea(activity));
           // ContextCompat.startForegroundService(this, intent);
        }*/
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(navigationView)) {
            drawerLayout.closeDrawer(Gravity.LEFT);
        } else {
            super.onBackPressed();
//            if(!ACCOUNTSTATUS.equalsIgnoreCase("approved")){
//                super.onBackPressed();
//            }
        }
    }

    @Override
    public void onSuccessCheck(HomeCheck homeCheck) {

        String status = "";
        int color = Color.RED;

        boolean documentExpired = homeCheck.getDocument_status().equalsIgnoreCase("disable");
        boolean documentExpireSoon = homeCheck.getSevendays().equalsIgnoreCase("disable");
        boolean noBalance = homeCheck.getWallet_status().equalsIgnoreCase("disable");
        boolean noDocument = homeCheck.getDocument_check().equalsIgnoreCase("false");
        boolean documentNotVerified = homeCheck.getProvider_status().equalsIgnoreCase("false");
        boolean blocked = homeCheck.getProvider_enable_status().equalsIgnoreCase("disable");


        if (documentExpired) {
//            status += "Your account has been disabled, upload renewed document. Please Contact admin for verification.";
//            status += "Your document expired. Upload new valid document.";
            status += "Your document expired. Upload new valid document. After that contact for verification.";
        } else if (documentExpireSoon) {
            status += "\n\n";
//            status += "One of your document expiring soon, upload renewed document. Please Contact admin for verification.";
            status += "Your document expiring soon. Upload new valid document. " +
                    "After that Contact for verification";
            color = ResourcesCompat.getColor(getResources(), R.color.colorPrimary, null);
        }

        if (noBalance) {
            status += "\n\n";
            status += "No wallet balance. \n" +
                    "Add money your wallet. Go to online.";
        }

        if (noDocument) {
            status += "\n\n";
//            status += "Please upload your document for verification and contact admin.";
            status += "Upload your documents for verification.";

            color = ResourcesCompat.getColor(getResources(), R.color.colorSecondaryText, null);
        }

        if (documentNotVerified) {
            status += "\n\n";
            status += "Your account has not been verified yet. Please Contact admin for verification.";
            //   status += "Your documents not verified yet, Please contact for verification.";
            color = ResourcesCompat.getColor(getResources(), R.color.colorSecondaryText, null);

        }

        if (blocked) {
            status += "\n\n";
//            status += "Your account has not been verified yet. Please Contact admin for verification.";
            status += "Your account has been blocked. Please Contact.";
        }


        if (documentExpired ||
                noBalance ||
                noDocument ||
                documentNotVerified ||
                blocked) {

            if (disableDriver != null)
                disableDriver.disable(status, color);
            String serviceStatus = SharedHelper.getKey(this, "serviceStatus");
            if (!serviceStatus.equalsIgnoreCase(Constants.User.Service.offLine)) {
                putOffline();
            }
        } else if (documentExpireSoon) {
            if (disableDriver != null)
                disableDriver.disable(status, color);
        } else {
            if (disableDriver != null)
                disableDriver.enable(status);
        }


        //added from trip response
        /*String accountStatus = response.getAccountStatus();
        String serviceStatus = response.getServiceStatus();

        tripResponse = response;
        Log.e("TAG", "accountStatus" + accountStatus);

        SharedHelper.putKey(activity(), "accountStatus", accountStatus);
        SharedHelper.putKey(activity(), "serviceStatus", serviceStatus);
        SharedHelper.putKey(activity(),"is_OnRide", String.valueOf(response.getRequests().size()));

        if (!ACCOUNTSTATUS.equalsIgnoreCase(accountStatus)) {
            ACCOUNTSTATUS = accountStatus;
            if (accountStatus.equalsIgnoreCase(Constants.User.Account.pendingDocument)) {
                // TODO: 23/09/21 remove the command if need to make older flow
                *//*startActivity(
                        new Intent(MainActivity.this, DocumentActivity.class)
                                .putExtra("documentUploaded",false));*//*

                imgStatus.setImageResource(R.drawable.banner_waiting);
                stopForegroundService();
            } else if (accountStatus.equalsIgnoreCase(Constants.User.Account.pendingCard)) {
//                startActivity(new Intent(MainActivity.this, AddCardActivity.class));
//                imgStatus.setImageResource(R.drawable.banner_waiting);
//                stopForegroundService();
                offlineFragment(Constants.User.Account.onBoarding);
                imgStatus.setImageResource(R.drawable.banner_waiting);
                stopForegroundService();
            } else if (accountStatus.equalsIgnoreCase(Constants.User.Account.onBoarding)) {
                offlineFragment(Constants.User.Account.onBoarding);
                imgStatus.setImageResource(R.drawable.banner_waiting);
                stopForegroundService();
            } else if (Constants.User.Account.banned.equalsIgnoreCase(accountStatus)) {
                offlineFragment(Constants.User.Account.banned);
                imgStatus.setImageResource(R.drawable.banner_banned);
                stopForegroundService();
            } else if (Constants.User.Account.approved.equalsIgnoreCase(accountStatus)
                    && Constants.User.Service.offLine.equalsIgnoreCase(serviceStatus)) {
                offlineFragment(Constants.User.Service.offLine);
                imgStatus.setImageResource(R.drawable.banner_active);
                stopForegroundService();
            } else if (Constants.User.Account.approved.equalsIgnoreCase(accountStatus)
                    && Constants.User.Service.active.equalsIgnoreCase(serviceStatus)) {
                offlineContainer.removeAllViews();
                imgStatus.setImageResource(R.drawable.banner_active);
                startForegroundService();
            }
        }
        if (response.getRequests().isEmpty()) {
            googleMap.clear();
            getDeviceLocation();
            changeFlow(Constants.checkStatus.EMPTY);
        } else {
            time_to_left = response.getRequests().get(0).getTimeLeftToRespond();
            DATUM = response.getRequests().get(0).getRequest();
            changeFlow(DATUM.getStatus());
            startForegroundService();
        }*/

        // Todo:  To be removed

//        HashMap<String, Object> params = new HashMap<>();
//        if (mLastKnownLocation != null) {
//            params.put("latitude", mLastKnownLocation.getLatitude());
//            params.put("longitude", mLastKnownLocation.getLongitude());
//        }
//        isFromTimer = true;
//        presenter.getTrip(params);
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_main;
    }

    //Intent floatingWidgetIntent;

    @Override
    public void initView() {

        //startService(new Intent(this, GPSTracker.class));
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(deviceToken -> {
            String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
            SharedHelper.putKeyFCM(MainActivity.this, "device_token", deviceToken);
            SharedHelper.putKeyFCM(MainActivity.this, "device_id", deviceId);
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        } else {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        }
        ButterKnife.bind(this);
        presenter.attachView(this);
        mFusedLocation = LocationServices.getFusedLocationProviderClient(this);
        registerReceiver(myReceiver, new IntentFilter(MyFireBaseMessagingService.INTENT_FILTER));
        ForceUpdateChecker.with(this).onUpdateNeeded(this).check();

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View headerView = navigationView.getHeaderView(0);

        imgMenu = headerView.findViewById(R.id.imgMenu);
        lblMenuName = headerView.findViewById(R.id.lblMenuName);

        imgStatus = headerView.findViewById(R.id.imgStatus);
        lblMenuEmail = headerView.findViewById(R.id.lblMenuEmail);

        headerView.setOnClickListener(v -> {
            DrawerLayout drawer = findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation
                    (this, imgMenu, ViewCompat.getTransitionName(imgMenu));
            startActivity(new Intent(MainActivity.this, ProfileActivity.class), options.toBundle());
        });

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

//        bottomSheetBehavior = BottomSheetBehavior.from(container);
//        bottomSheetBehavior.setHideable(false);
//        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
//            @Override
//            public void onStateChanged(@NonNull View bottomSheet, int newState) {
//                // Un used
//            }
//
//            @Override
//            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
//                // Un used
//            }
//        });

        String profileUrl = getIntent().getStringExtra("avartar");
        if (profileUrl != null && !profileUrl.isEmpty()) Glide
                .with(activity()).load(profileUrl)
                .apply(RequestOptions.placeholderOf(R.drawable.ic_user_placeholder).
                        dontAnimate().error(R.drawable.ic_user_placeholder))
                .into(imgMenu);

        /*sbChangeStatus.setOnStateChangeListener(new OnStateChangeListener() {
            @Override
            public void onStateChange(boolean active) {
                Toast.makeText(getApplicationContext(),"Your account is deactivated " +
                                "temporarily. Please contact customer care!!",
                        Toast.LENGTH_SHORT).show();
            }
        });*/

        sbChangeStatus.setOnStateChangeListener(active -> {
            if (active) {

                showLoading();
                putOffline();
            }

        });


        //----------------CODE FOR LOOKING UPDATES OF DRIVER CONTINUOUSLY------------------
        HashMap<String, Object> params = new HashMap<>();
        if (mLastKnownLocation != null) {
            params.put("latitude", mLastKnownLocation.getLatitude());
            params.put("longitude", mLastKnownLocation.getLongitude());
        }
        System.out.println("RRR BroadcastReceiver called...");
        presenter.getTrip(params);

        //if want to continuously look for updates uncomment the code
        /*try {
            h = new Handler();
            r = () -> {
                HashMap<String, Object> params = new HashMap<>();
                if (mLastKnownLocation != null) {
                    params.put("latitude", mLastKnownLocation.getLatitude());
                    params.put("longitude", mLastKnownLocation.getLongitude());
                }
                System.out.println("RRR BroadcastReceiver called...");
                presenter.getTrip(params);
                h.postDelayed(r, delay);
            };
            h.postDelayed(r, delay);
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        //---------------------------------------------------------------

        try {
            checkHandler = new Handler();

            checkRunnable = () -> {
                presenter.check();

                checkHandler.postDelayed(checkRunnable, 5000);
            };
            checkHandler.postDelayed(checkRunnable, 5000);
        } catch (Exception e) {

        }


        showFloatingView(true);

//        if (floatingWidgetIntent == null)
//            floatingWidgetIntent = new Intent(MainActivity.this, FloatWidgetService.class);

        //       Bundle extras = getIntent().getExtras();
//        String d = extras.getString("move_to");

//        if(d.equalsIgnoreCase("now")){
//            Intent in = new Intent(getApplicationContext(),YourTripActivity.class);
//            in.putExtra("move_to","upcomming_trip");
//            startActivity(in);
//        }

        String here = getIntent().getStringExtra("move_to");
        if (here != null && here.equalsIgnoreCase("now")) {
            Intent in = new Intent(getApplicationContext(), YourTripActivity.class);
            in.putExtra("move_to", "upcomming_trip");
            startActivity(in);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

    }

    private void showFloatingView(boolean isShowOverlayPermission) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
            startFloatingViewService();
            return;
        }

        if (Settings.canDrawOverlays(this)) {
            startFloatingViewService();
            return;
        }

        if (isShowOverlayPermission) {
            final Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, CHATHEAD_OVERLAY_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_LOCATION && resultCode == RESULT_CANCELED) {
            offlineFragment("Please enable GPS");
        }

        if (requestCode == APP_PERMISSION_REQUEST && resultCode == RESULT_OK) {
            openMap();
        } else if (requestCode == CHATHEAD_OVERLAY_PERMISSION_REQUEST_CODE) {
            showFloatingView(false);
        } else {
            Toast.makeText(this, "Draw over other app permission not enable.", Toast.LENGTH_SHORT).show();
        }
    }

    private void openMap() {
      /*  try {
            startService(floatingWidgetIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        if (getString(R.string.pick_up_location).equalsIgnoreCase(lblLocationType.getText().toString())) {
            if (DATUM.getSAddress() != null && !DATUM.getSAddress().isEmpty()) {

                String lat = DATUM.getSLatitude() + "";
                String lon = DATUM.getSLongitude() + "";

                String geoUri = "http://maps.google.com/maps?q=loc:" + lat + "," + lon + " (" + "PICK UP" + ")";

                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(geoUri));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                finish();
                Intent intent2 = new Intent(MainActivity.this, MainActivity.class);
                intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getApplicationContext().startActivity(intent2);
                getApplicationContext().startActivity(intent);




                /*Uri gmmIntentUri = Uri.parse("google.navigation:q=" + DATUM.getSAddress());
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                mapIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                finish();
                startActivity(new Intent(MainActivity.this, MainActivity.class));
                getApplicationContext().startActivity(mapIntent);*/

            }
        } else {
            if (DATUM.getDAddress() != null && !DATUM.getDAddress().isEmpty()) {

                String lat = DATUM.getDLatitude() + "";
                String lon = DATUM.getDLongitude() + "";

                String geoUri = "http://maps.google.com/maps?q=loc:" + lat + "," + lon + " (" + "DROP OFF" + ")";

                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(geoUri));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                finish();

                Intent intent2 = new Intent(MainActivity.this, MainActivity.class);
                intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getApplicationContext().startActivity(intent2);
                getApplicationContext().startActivity(intent);

               /* Uri gmmIntentUri = Uri.parse("google.navigation:q=" + DATUM.getDAddress());
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                mapIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                finish();
                startActivity(new Intent(MainActivity.this, MainActivity.class));
                getApplicationContext().startActivity(mapIntent);*/
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        enableGPS();
        ACCOUNTSTATUS = "";
        //      gpsServiceIntent = new Intent(this, GPSTracker.class);
//        startService(gpsServiceIntent);
        presenter.getProfile();
        /*HashMap<String, Object> params = new HashMap<>();
        if (mLastKnownLocation != null) {
            params.put("latitude", mLastKnownLocation.getLatitude());
            params.put("longitude", mLastKnownLocation.getLongitude());
            SharedHelper.putKey(this, "latitude", String.valueOf(mLastKnownLocation.getLatitude()));
            SharedHelper.putKey(this, "longitude", String.valueOf(mLastKnownLocation.getLongitude()));
        }
        presenter.getTrip(params);*/
        presenter.settings();

//        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
//                Manifest.permission.ACCESS_BACKGROUND_LOCATION)
//                != PackageManager.PERMISSION_GRANTED){
//            getBackgroundLocationPermission();
//        }
    }

    public void enableGPS() {
        final LocationManager manager = (LocationManager) MainActivity.this.getSystemService(Context.LOCATION_SERVICE);
        if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER) && hasGPSDevice(MainActivity.this)) {
//            Toast.makeText(MainActivity.this,"Gps already enabled",Toast.LENGTH_SHORT).show();
//            finish();
        }

        if (!hasGPSDevice(MainActivity.this)) {
            Toast.makeText(MainActivity.this, "Gps not Supported", Toast.LENGTH_SHORT).show();
        }

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER) && hasGPSDevice(MainActivity.this)) {
            Log.e("keshav", "Gps already enabled");
//            Toast.makeText(MainActivity.this,"Gps not enabled",Toast.LENGTH_SHORT).show();
            enableLoc();
        } else {
            Log.e("keshav", "Gps already enabled");
//            Toast.makeText(MainActivity.this,"Gps already enabled",Toast.LENGTH_SHORT).show();
        }
    }

    public boolean isGPSEnabled() {
        final LocationManager manager = (LocationManager) MainActivity.this.getSystemService(Context.LOCATION_SERVICE);
        if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER) && hasGPSDevice(MainActivity.this)) {
            return true;
        }

        if (!hasGPSDevice(MainActivity.this)) {
            return false;
        }

        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER) || !hasGPSDevice(MainActivity.this);
    }

    private boolean hasGPSDevice(Context context) {
        final LocationManager mgr = (LocationManager) context
                .getSystemService(Context.LOCATION_SERVICE);
        if (mgr == null)
            return false;
        final List<String> providers = mgr.getAllProviders();
        if (providers == null)
            return false;
        return providers.contains(LocationManager.GPS_PROVIDER);
    }

    private void enableLoc() {

        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(MainActivity.this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                        @Override
                        public void onConnected(Bundle bundle) {

                        }

                        @Override
                        public void onConnectionSuspended(int i) {
                            googleApiClient.connect();
                        }
                    })
                    .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                        @Override
                        public void onConnectionFailed(ConnectionResult connectionResult) {

                            Log.d("Location error", "Location error " + connectionResult.getErrorCode());
                        }
                    }).build();
            googleApiClient.connect();

            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(30 * 1000);
            locationRequest.setFastestInterval(5 * 1000);
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest);

            builder.setAlwaysShow(true);

            SettingsClient client = LocationServices.getSettingsClient(this);
            Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

            task.addOnFailureListener(this, e -> {
                if (e instanceof ResolvableApiException) try {
                    ResolvableApiException resolvable = (ResolvableApiException) e;
                    resolvable.startResolutionForResult(MainActivity.this, REQUEST_LOCATION);
                } catch (IntentSender.SendIntentException sendEx) {
                    Toast.makeText(this, sendEx.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
            });

//            PendingResult<LocationSettingsResult> result =
//                    LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
//            result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
//                @Override
//                public void onResult(LocationSettingsResult result) {
//                    final Status status = result.getStatus();
//                    switch (status.getStatusCode()) {
//                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
//                            try {
//                                // Show the dialog by calling startResolutionForResult(),
//                                // and check the result in onActivityResult().
//                                status.startResolutionForResult(MainActivity.this, REQUEST_LOCATION);
//
////                                finish();
//                            } catch (IntentSender.SendIntentException e) {
//                                // Ignore the error.
//                            }
//                            break;
//                    }
//                }
//            });

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(myReceiver);
        /*if (h != null)
            h.removeCallbacks(r);*/

        if (checkHandler != null)
            checkHandler.removeCallbacks(checkRunnable);

//        if (floatingWidgetIntent != null)
//            stopService(floatingWidgetIntent);
//        EventBus.getDefault().unregister(this);
//        if (gpsServiceIntent != null) stopService(gpsServiceIntent);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        switch (id) {
            case R.id.nav_card:
                startActivity(new Intent(this, CardActivity.class));
                break;
            case R.id.nav_your_trips:
                startActivity(new Intent(this, YourTripActivity.class));
                break;
            case R.id.nav_earnings:
                startActivity(new Intent(MainActivity.this, EarningsActivity.class));
                break;
            case R.id.nav_wallet:
                startActivity(new Intent(MainActivity.this, WalletActivity.class));
                break;
            case R.id.nav_summary:
                startActivity(new Intent(MainActivity.this, SummaryActivity.class));
                break;
            case R.id.nav_settings:
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                intent.putExtra("setting", "isClick");
                startActivity(intent);
                break;
            case R.id.nav_help:
                startActivity(new Intent(MainActivity.this, HelpActivity.class));
                break;
            case R.id.nav_share:
                navigateToShareScreen();
                break;
            case R.id.nav_document:
//                startActivity(new Intent(MainActivity.this, DocumentActivity.class));
                Intent documentintent = new Intent(MainActivity.this, DocumentActivity.class);
                documentintent.putExtra("isFromMainPage", "YES");
                startActivity(documentintent);
                break;
            case R.id.nav_invite_referral:
                startActivity(new Intent(MainActivity.this, InviteActivity.class));
                break;
            case R.id.nav_logout:
                ShowLogoutPopUp();
                break;
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return false;
    }

    @Override
    public void onCameraIdle() {
//        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    @Override
    public void onCameraMove() {
//        if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED)
//            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        updateLocationUI();
        getDeviceLocation();
    }

    @SuppressLint("WrongConstant")
    @OnClick({R.id.menu, R.id.nav_view, R.id.navigation_img, R.id.gps})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.menu:
                if (drawerLayout.isDrawerOpen(GravityCompat.START))
                    drawerLayout.closeDrawer(GravityCompat.START);
                else {
                    UserResponse user = new Gson().fromJson(SharedHelper.getKey(this, "userInfo"), UserResponse.class);
                    if (user != null) {
                        SharedHelper.putKey(this, Constants.SharedPref.currency, user.getCurrency());
                        Constants.Currency = SharedHelper.getKey(this, Constants.SharedPref.currency);

                        lblMenuName.setText(String.format("%s %s", user.getFirstName(), user.getLastName()));
                        lblMenuEmail.setText(user.getEmail());
                        SharedHelper.putKey(activity(), "picture", user.getAvatar());
                        Glide.with(activity())
                                .load(BuildConfig.BASE_IMAGE_URL + user.getAvatar())
                                .apply(RequestOptions.placeholderOf(R.drawable.ic_user_placeholder)
                                        .dontAnimate()
                                        .error(R.drawable.ic_user_placeholder))
                                .into(imgMenu);
                    } else presenter.getProfile();
                    drawerLayout.openDrawer(Gravity.START);
                }
                break;
            case R.id.nav_view:
                break;
            case R.id.gps:
                if (mLastKnownLocation != null) {
                    LatLng currentLatLng = new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, DEFAULT_ZOOM));
                }
                break;
            case R.id.navigation_img:

                if (STATUS.equalsIgnoreCase(Constants.checkStatus.ARRIVED)) {
                    Toast.makeText(getApplicationContext(), "Please tap PICKED UP", Toast.LENGTH_LONG).show();
                    return;
                }


                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this))
                    startActivityForResult(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName())), APP_PERMISSION_REQUEST);
                else openMap();
                //openMap();
                break;
        }
    }

    private void putOffline() {

        HashMap<String, Object> map = new HashMap<>();
        map.put("service_status", Constants.User.Service.offLine);
        presenter.providerAvailable(map);
        hideLoading();
        //stopForegroundService();

    }

    @Override
    public void onLocalBroadcastReceiver(int status) {
        if (status == Constants.BC_START_LOCATION_TRACK) {
            Log.e("ForegroundOnlyLocationService", "onLocalBroadcastReceiver");
            startForegroundService();
        } else if (status == Constants.BC_STOP_LOCATION_TRACK) {
            Log.e("ForegroundOnlyLocationService", "BC_STOP_LOCATION_TRACK");
            stopForegroundService();
        }
    }

    private void updateLocationUI() {
        if (googleMap == null) return;
        try {
            if (checkLocationPermission()) {
                googleMap.setMyLocationEnabled(true);
                googleMap.getUiSettings().setMyLocationButtonEnabled(false);
                googleMap.getUiSettings().setCompassEnabled(false);
                googleMap.setOnCameraMoveListener(this);
                googleMap.setOnCameraIdleListener(this);
            } else {
                googleMap.setMyLocationEnabled(false);
                googleMap.getUiSettings().setMyLocationButtonEnabled(false);
                googleMap.getUiSettings().setCompassEnabled(false);
                mLastKnownLocation = null;
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    public boolean checkLocationPermission() {
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED;
    }

    void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (checkLocationPermission()) {
                Task<Location> locationResult = mFusedLocation.getLastLocation();
                locationResult.addOnCompleteListener(this, task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        // Set the map's camera position to the current location of the device.
                        mLastKnownLocation = task.getResult();
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(
                                        mLastKnownLocation.getLatitude(),
                                        mLastKnownLocation.getLongitude()),
                                DEFAULT_ZOOM));

                        if (foregroundOnlyLocationService != null) {
                            foregroundOnlyLocationService.updateLocation(mLastKnownLocation);
                        }
                        locationUpdates(mLastKnownLocation);

                    } else {
                        Log.d("Map", "Current location is null. Using defaults.");
                        Log.e("Map", "Exception: %s", task.getException());
                        //googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void changeFragment(Fragment fragment) {

        if (isFinishing()) return;

        if (fragment != null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.container, fragment,
                    (fragment instanceof IncomingRequestFragment) ? "INCOMING_REQUEST_FRAGMENT" : fragment.getTag());
            transaction.commitAllowingStateLoss();
           // sbChangeStatus.setVisibility(View.GONE);
        } else {
            if (IncomingRequestFragment.countDownTimer != null)
                IncomingRequestFragment.countDownTimer.cancel();

            if (IncomingRequestFragment.mPlayer != null) {
                if (IncomingRequestFragment.mPlayer.isPlaying()) {
                    IncomingRequestFragment.mPlayer.stop();
                }
            }

            container.removeAllViews();
            // TODO: 20/09/21 change this comma
            sbChangeStatus.setVisibility(View.VISIBLE);
            lnrLocationHeader.setVisibility(View.GONE);
            googleMap.clear();
        }
    }

    private void offlineFragment(String s) {
        Log.e("ForegroundOnlyLocationService", "offlineFragment");
        stopForegroundService();
        Fragment fragment = new OfflineFragment(title);
        disableDriver = (DisableDriver) fragment;
        Bundle b = new Bundle();
        b.putString("status", s);
        fragment.setArguments(b);
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.offline_container, fragment, fragment.getTag());
        transaction.commitAllowingStateLoss();
        ACCOUNTSTATUS = "";

        presenter.check();
    }

    @Override
    public void onSuccess(UserResponse user) {
        if (user != null) {
            Log.e("user response", new Gson().toJson(user));
            String dd = LocaleHelper.getLanguage(this);
            if (user.getProfile() != null && user.getProfile().getLanguage() != null &&
                    !user.getProfile().getLanguage().equalsIgnoreCase(dd)) {
                LocaleHelper.setLocale(getApplicationContext(), user.getProfile().getLanguage());
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK |
                        Intent.FLAG_ACTIVITY_NEW_TASK));
            }
            lblMenuName.setText(String.format("%s %s", user.getFirstName(), user.getLastName()));
            lblMenuEmail.setText(user.getEmail());
            SharedHelper.putKey(activity(), "picture", user.getAvatar());
            Glide.with(activity())
                    .load(BuildConfig.BASE_IMAGE_URL + user.getAvatar())
                    .apply(RequestOptions.placeholderOf(R.drawable.ic_user_placeholder)
                            .dontAnimate()
                            .error(R.drawable.ic_user_placeholder))
                    .into(imgMenu);
            SharedHelper.putKey(this, "stripe_publishable_key", user.getStripePublishableKey());
            SharedHelper.putKey(this, Constants.SharedPref.user_id, String.valueOf(user.getId()));
            SharedHelper.putKey(this, Constants.SharedPref.user_name, user.getFirstName()
                    + " " + user.getLastName());
            SharedHelper.putKey(this, Constants.SharedPref.user_avatar, BuildConfig.BASE_IMAGE_URL + user.getAvatar());
            SharedHelper.putKey(this, Constants.SharedPref.currency, user.getCurrency());
            SharedHelper.putKey(this, "userInfo", printJSON(user));
            Constants.Currency = SharedHelper.getKey(this, Constants.SharedPref.currency);
            int card = user.getCard();
            if (card == 0) {
                Menu nav_Menu = navView.getMenu();
                nav_Menu.findItem(R.id.nav_card).setVisible(false);
            } else {
                Menu nav_Menu = navView.getMenu();
                nav_Menu.findItem(R.id.nav_card).setVisible(true);
            }
            SharedHelper.putKey(this, "card", card);
        }

    }

    @Override
    public void onError(Throwable e) {
        hideLoading();
        Log.e("on error", e.toString());
        if (e != null)
            onErrorBase(e);
    }

    @Override
    public void onSuccessLogout(Object object) {
        offlineFragment("");
        Utilities.LogoutApp(activity(), "");
    }

    @Override
    public void onSuccess(TripResponse response) {

        if (isFromTimer) {
            Log.d(TAG, "onSuccess: Called!");
            isFromTimer = false;
            IncomingRequestFragment myFragment = (IncomingRequestFragment) getSupportFragmentManager().findFragmentByTag("INCOMING_REQUEST_FRAGMENT");
            if (response.getRequests() == null || response.getRequests().size() == 0 || response.getRequests().get(0).getRequest() == null) {
                if (myFragment != null && myFragment.isVisible()) {
                    googleMap.clear();
                    getDeviceLocation();
                    changeFlow(Constants.checkStatus.EMPTY);
                }
            } else {
                if (myFragment == null || !myFragment.isVisible()) {
                    time_to_left = response.getRequests().get(0).getTimeLeftToRespond();
                    if (response.getRequests() == null) return;
                    if (response.getRequests().size() == 0) return;
                    if (response.getRequests().get(0).getRequest() == null) return;
                    DATUM = response.getRequests().get(0).getRequest();
                    if (DATUM != null && DATUM.getStatus().equals(Constants.checkStatus.SEARCHING)) {
                        changeFlow(DATUM.getStatus());
                        startForegroundService();
                    }
                }
            }
            return;
        }
        String accountStatus = response.getAccountStatus();
        String serviceStatus = response.getServiceStatus();

        tripResponse = response;
        Log.e("TAG", "accountStatus" + accountStatus);

        SharedHelper.putKey(activity(), "accountStatus", accountStatus);
        SharedHelper.putKey(activity(), "serviceStatus", serviceStatus);
        SharedHelper.putKey(activity(), "is_OnRide", String.valueOf(response.getRequests().size()));
        Log.e("TAGG", "is_OnRide :" + response.getRequests().size());
        if (foregroundOnlyLocationService != null) {
            foregroundOnlyLocationService.showNotification();
            foregroundOnlyLocationService.updateLocation(mLastKnownLocation);
        }

        if (!ACCOUNTSTATUS.equalsIgnoreCase(accountStatus)) {
            ACCOUNTSTATUS = accountStatus;
            if (accountStatus.equalsIgnoreCase(Constants.User.Account.pendingDocument)) {
                // TODO: 23/09/21 remove the command if need to make older flow
                /*startActivity(
                        new Intent(MainActivity.this, DocumentActivity.class)
                                .putExtra("documentUploaded",false));*/

                imgStatus.setImageResource(R.drawable.banner_waiting);
                offlineFragment(Constants.User.Account.onBoarding);
                presenter.check();
            } else if (accountStatus.equalsIgnoreCase(Constants.User.Account.wallet)) {
                // TODO: 23/09/21 remove the command if need to make older flow
                /*startActivity(
                        new Intent(MainActivity.this, DocumentActivity.class)
                                .putExtra("documentUploaded",false));*/

                imgStatus.setImageResource(R.drawable.banner_waiting);
                //offlineFragment("Your wallet Limit is low please add money in wallet and go online");
                offlineFragment(Constants.User.Account.wallet);

                presenter.check();
            } else if (accountStatus.equalsIgnoreCase(Constants.User.Account.pendingCard)) {
//                startActivity(new Intent(MainActivity.this, AddCardActivity.class));
//                imgStatus.setImageResource(R.drawable.banner_waiting);
                offlineFragment(Constants.User.Account.onBoarding);
                imgStatus.setImageResource(R.drawable.banner_waiting);
            } else if (accountStatus.equalsIgnoreCase(Constants.User.Account.onBoarding)) {
                offlineFragment(Constants.User.Account.onBoarding);
                imgStatus.setImageResource(R.drawable.banner_waiting);
            } else if (Constants.User.Account.banned.equalsIgnoreCase(accountStatus)) {
                offlineFragment(Constants.User.Account.banned);
                imgStatus.setImageResource(R.drawable.banner_banned);
            } else if (Constants.User.Account.approved.equalsIgnoreCase(accountStatus)
                    && Constants.User.Service.offLine.equalsIgnoreCase(serviceStatus)) {
                offlineFragment(Constants.User.Service.offLine);
                imgStatus.setImageResource(R.drawable.banner_active);
            } else if (Constants.User.Account.approved.equalsIgnoreCase(accountStatus)
                    && Constants.User.Service.active.equalsIgnoreCase(serviceStatus)) {
                showLoading();
                offlineContainer.removeAllViews();
                imgStatus.setImageResource(R.drawable.banner_active);
                Log.e("ForegroundOnlyLocationService", "startForegroundService status");
                startForegroundService();
                hideLoading();
            }
        }
        if (response.getRequests().isEmpty()) {
            googleMap.clear();
            getDeviceLocation();
            changeFlow(Constants.checkStatus.EMPTY);
        } else {
            time_to_left = response.getRequests().get(0).getTimeLeftToRespond();
            DATUM = response.getRequests().get(0).getRequest();
            changeFlow(DATUM.getStatus());
            startForegroundService();
            Log.e("ForegroundOnlyLocationService", "startForegroundService changeFlow");
        }
    }

    @Override
    public void onSuccessProviderAvailable(Object object) {
        Log.e("TAGG", "onSuccessProviderAvailable :");
        sendBroadcast(new Intent(INTENT_FILTER));
        Log.d("PROVOIDER AVAILABLE", object.toString());
        offlineFragment("");
        sbChangeStatus.toggleState();

    }

    @Override
    public void onSuccessFCM(Object object) {
        Utilities.printV("onSuccessFCM", "onSuccessFCM");
    }

    @Override
    public void onSuccessLocationUpdate(TripResponse tripResponse) {

    }

    @Override
    public void onSuccess(InitSettingsResponse initSettingsResponse) {

        SharedHelper.putKey(activity(), "map_key", initSettingsResponse.getMapKey());
        Log.d("drawRoute", "drawRoute: " + SharedHelper.getKey(activity(), "map_key"));


    }

    public void changeFlow(String status) {

        // dismissDialog(Constants.checkStatus.INVOICE);
        //dismissDialog(Constants.checkStatus.RATING);
        Utilities.printV("RRR Current status == >", status);
        if (!STATUS.equalsIgnoreCase(status)) {
            STATUS = status;

            lblLocationType.setText(getString(R.string.pick_up_location));
            if (DATUM != null &&
                    DATUM.getSAddress() != null && !DATUM.getSAddress().isEmpty()) {
                lblLocationName.setText(DATUM.getSAddress());
            }

            switch (status) {
                case Constants.checkStatus.EMPTY:
                    changeFragment(null);
                    break;
                case Constants.checkStatus.SEARCHING:
                    if (myLocationCalculationCheck) try {
                        System.out.println("  Distance points received = " +
                                new Gson().toJson(getLocation(this)));
                        putLocation(this, "");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    changeFragment(new IncomingRequestFragment());
                    break;
                case Constants.checkStatus.ACCEPTED:
                    lnrLocationHeader.setVisibility(View.VISIBLE);


                    changeFragment(new StatusFlowFragment());
                    break;
                case Constants.checkStatus.STARTED:
                    lnrLocationHeader.setVisibility(View.VISIBLE);
                    changeFragment(new StatusFlowFragment());
                    break;
                case Constants.checkStatus.ARRIVED:
                    googleMap.clear();
                    lblLocationType.setText(getString(R.string.drop_off_location));
                    lblLocationName.setText(DATUM.getDAddress());


                    // TODO: 21/03/22 visible
                    lnrLocationHeader.setVisibility(View.VISIBLE);
//                    lnrLocationHeader.setVisibility(View.GONE);


                    changeFragment(new StatusFlowFragment());
                    break;
                case Constants.checkStatus.PICKEDUP:
                    lblLocationType.setText(getString(R.string.drop_off_location));
                    lblLocationName.setText(DATUM.getDAddress());
                    lnrLocationHeader.setVisibility(View.VISIBLE);
                    changeFragment(new StatusFlowFragment());
                    break;
                case Constants.checkStatus.DROPPED:
                    lblLocationType.setText(getString(R.string.drop_off_location));
                    lblLocationName.setText(DATUM.getDAddress());
                    changeFragment(new InvoiceDialogFragment());
                /*InvoiceDialogFragment invoiceDialogFragment = new InvoiceDialogFragment();
                invoiceDialogFragment.show(getSupportFragmentManager(), Constants.checkStatus.INVOICE);*/
                    break;
                case Constants.checkStatus.COMPLETED:
                    lblLocationType.setText(getString(R.string.drop_off_location));
                    lblLocationName.setText(DATUM.getDAddress());
                    changeFragment(new RatingDialogFragment());
               /* RatingDialogFragment ratingDialogFragment = new RatingDialogFragment();
                ratingDialogFragment.show(getSupportFragmentManager(), Constants.checkStatus.RATING);*/
                    break;
                default:
                    break;
            }
        }
    }

    public void ShowLogoutPopUp() {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder
                .setMessage(getString(R.string.log_out_title))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.ok), (dialog, id) -> {
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("id", SharedHelper.getKey(activity(),
                            Constants.SharedPref.user_id) + "");
                    presenter.logout(map);
                }).setNegativeButton(getString(R.string.cancel), (dialog, id) -> {
                    String user_id = SharedHelper.getKey(activity(), Constants.SharedPref.user_id);
                    Utilities.printV("user_id===>", user_id);
                    dialog.cancel();
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    void redirectNavigation() {
        startActivity(new Intent(Intent.ACTION_VIEW,
                Uri.parse("http://maps.google.com/maps?saddr=20.344,34.34&daddr=20.5666,45.345")));
    }

    @Override
    protected void onStart() {
        super.onStart();
        //EventBus.getDefault().register(this);
        startLocationService();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (foregroundOnlyLocationServiceBound) {
            unbindService(foregroundOnlyServiceConnection);
            foregroundOnlyLocationServiceBound = false;
        }
    }

    @Subscribe
    public void getEvent(final Location location) {

        if (myLocationCalculationCheck)
            Toast.makeText(getApplicationContext(), "The Latitude is "
                    + location.getLatitude()
                    + " and the Longitude is "
                    + location.getLongitude(), Toast.LENGTH_SHORT).show();

        mLastKnownLocation = location;
        System.out.println("RRR BroadcastReceiver Event buss called...");
//        locationUpdates(new LatLng(location.getLatitude(), location.getLongitude()));
    }

    private void startLocationService() {
        Intent serviceIntent = new Intent(this, FetchLocationService.class);
        bindService(serviceIntent, foregroundOnlyServiceConnection, Context.BIND_AUTO_CREATE);
    }

    public void drawRoute(LatLng source, LatLng destination) {
        GoogleDirection.withServerKey(SharedHelper.getKey(activity(), "map_key"))
                .from(source)
                .to(destination)
                .transportMode(TransportMode.DRIVING)
                .execute(this);
    }

    @Override
    public void onDirectionSuccess(Direction direction, String rawBody) {

        if (direction.isOK()) {
            Route route = direction.getRouteList().get(0);
            if (!route.getLegList().isEmpty()) {

                Leg startLeg = route.getLegList().get(0);
                Leg endLeg = route.getLegList().get(0);

                LatLng origin = new LatLng(startLeg.getStartLocation().getLatitude(), startLeg.getStartLocation().getLongitude());
                LatLng destination = new LatLng(endLeg.getEndLocation().getLatitude(), endLeg.getEndLocation().getLongitude());
                googleMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.src_icon)).position(origin)).setTag(startLeg);
                googleMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.des_icon)).position(destination)).setTag(endLeg);
            }

            ArrayList<LatLng> directionPositionList = route.getLegList().get(0).getDirectionPoint();
            googleMap.addPolyline(DirectionConverter.createPolyline(this, directionPositionList, 5, getResources().getColor(R.color.colorPrimary)));
            setCameraWithCoordinationBounds(route);

        } else Toast.makeText(this, direction.getStatus(), Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onDirectionFailure(Throwable t) {
        Toast.makeText(this, t.getMessage(), Toast.LENGTH_SHORT).show();
    }

    private void setCameraWithCoordinationBounds(Route route) {
        LatLng southwest = route.getBound().getSouthwestCoordination().getCoordination();
        LatLng northeast = route.getBound().getNortheastCoordination().getCoordination();
        LatLngBounds bounds = new LatLngBounds(southwest, northeast);
        googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
    }

    public void navigateToShareScreen() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
        sendIntent.putExtra(Intent.EXTRA_TEXT, "Hey Checkout this app," +
                getString(R.string.app_name) + "\nhttps://play.google.com/store/apps/details?id=" +
                BuildConfig.APPLICATION_ID);
        sendIntent.setType("text/plain");
        startActivity(sendIntent);
    }

    private void locationUpdates(Location latlng) {

        if (DATUM == null) return;

        if (Constants.checkStatus.ACCEPTED.equalsIgnoreCase(DATUM.getStatus())
                || Constants.checkStatus.STARTED.equalsIgnoreCase(DATUM.getStatus())
                || Constants.checkStatus.ARRIVED.equalsIgnoreCase(DATUM.getStatus())
                || Constants.checkStatus.PICKEDUP.equalsIgnoreCase(DATUM.getStatus())
                || Constants.checkStatus.DROPPED.equalsIgnoreCase(DATUM.getStatus())) {

            //            TODO: Notification Blocked. Because conflict occurs when riding with cross platform IOS and web
            //            JsonObject jPayload = new JsonObject();
            //            JsonObject jData = new JsonObject();
            //
            //            jData.addProperty("latitude", latlng.latitude);
            //            jData.addProperty("longitude", latlng.longitude);
            //            jPayload.addProperty("to", "/topics/" + DATUM.getId());
            //            jPayload.addProperty("priority", "high");
            //            jPayload.add("data", jData);
            //
            //            presenter.sendFCM(jPayload);

            HashMap<String, Object> params = new HashMap<>();
            params.put("latitude", latlng.getLatitude());
            params.put("longitude", latlng.getLongitude());

            presenter.getTripLocationUpdate(params);

        }
    }

    public void requestLocation() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) ||
                shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)) {

            new AlertDialog.Builder(MainActivity.this)
                    //.setTitle("Your Alert")
                    .setMessage("Location permission was denied, but is needed for core functionality")
                    .setCancelable(false)
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent();
                            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null);
                            intent.setData(uri);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                    }).show();

        } else {
            // You can directly ask for the permission.
            // The registered ActivityResultCallback gets the result of this request.
                /*requestPermissionLauncher.launch(String[Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION]);*/
            requestPermissionLauncher.launch(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION});
        }
    }

    private void startForegroundService() {

        if (checkLocationPermission()) {
            startLocationService();
            /*if (!isJobServiceOn(MainActivity.this)) {
                scheduleJob(MainActivity.this);
            }*/
            if (foregroundOnlyLocationService != null) {
                foregroundOnlyLocationService.subscribeToLocationUpdates();
            }

            getDeviceLocation();
            updateLocationUI();
            showFloatingView(true);
        } else {
            putOffline();
            requestLocation();
        }
       /* if (!isServiceRunning()) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                activity().startService(new Intent(activity(), LocationShareService.class));
            } else {
                Intent serviceIntent = new Intent(activity(), LocationShareService.class);
                ContextCompat.startForegroundService(activity(), serviceIntent);
            }
        }*/
    }

    private void stopForegroundService() {
        /*if (isServiceRunning())
            stopService(new Intent(MainActivity.this, LocationShareService.class));*/

        if (foregroundOnlyLocationService != null) {
            Log.e("ForegroundOnlyLocationService", "cancelLocationTrackingFromActivity");
            foregroundOnlyLocationService.unsubscribeToLocationUpdates();
        }
        if (foregroundOnlyLocationServiceBound) {

            unbindService(foregroundOnlyServiceConnection);
            foregroundOnlyLocationServiceBound = false;
        } else {
            //  stopService(new Intent(MainActivity.this, FetchLocationService.class));
        }

    }

    @TargetApi(Build.VERSION_CODES.M)
    public void scheduleJob(Context context) {
        ComponentName serviceComponent = new ComponentName(context, CheckScheduleService.class);
        JobInfo.Builder builder = new JobInfo.Builder(Constants.CHECK_SCHEDULE_JOB_ID, serviceComponent);
        builder.setMinimumLatency(5000); // wait at least
        builder.setOverrideDeadline(12000); // maximum delay
        builder.setRequiresDeviceIdle(true); // device should be idle
        builder.setRequiresCharging(false); // we don't care if the device is charging or not
        JobScheduler jobScheduler = context.getSystemService(JobScheduler.class);
        jobScheduler.schedule(builder.build());
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private boolean isJobServiceOn(Context context) {
        JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        boolean hasBeenScheduled = false;
        for (JobInfo jobInfo : scheduler.getAllPendingJobs()) {
            if (jobInfo.getId() == Constants.CHECK_SCHEDULE_JOB_ID) {
                hasBeenScheduled = true;
                break;
            }
        }
        return hasBeenScheduled;
    }

    @Override
    public void onUpdateNeeded(String updateUrl, boolean isPriorityUpdate) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.newVersionAvailable))
                .setMessage(getString(R.string.forceUpdateMsg))
                .setPositiveButton(getString(R.string.update),
                        (dialog12, which) -> {
                            final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(updateUrl));
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        });
        if (isPriorityUpdate) {
            builder.setNegativeButton(getString(R.string.noThanks),
                    (dialog1, which) -> dialog1.dismiss());
        }
        AlertDialog dialog = builder.create();
        dialog.show();

    }

/*    private boolean isServiceRunning() {
        ActivityManager manager =
                (ActivityManager) activity().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service :
                manager.getRunningServices(Integer.MAX_VALUE)) {
            if (LocationShareService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }*/

    public interface DisableDriver {
        void enable(String message);

        void disable(String message, @ColorInt int color);
    }

    public void closeBottomSheet() {
        container.removeAllViews();
        sbChangeStatus.setVisibility(View.VISIBLE);
        lnrLocationHeader.setVisibility(View.GONE);
        googleMap.clear();
    }

}
