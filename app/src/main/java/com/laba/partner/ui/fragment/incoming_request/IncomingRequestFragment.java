package com.laba.partner.ui.fragment.incoming_request;

import static com.laba.partner.base.BaseActivity.DATUM;
import static com.laba.partner.base.BaseActivity.time_to_left;
import static com.laba.partner.MvpApplication.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION;

import static java.lang.Math.round;
import static com.laba.partner.MvpApplication.mLastKnownLocation;

import android.Manifest;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatRatingBar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;
import com.laba.partner.BuildConfig;
import com.laba.partner.R;
import com.laba.partner.base.BaseFragment;
import com.laba.partner.common.Constants;
import com.laba.partner.common.Utilities;
import com.laba.partner.data.network.APIClient;
import com.laba.partner.data.network.model.Rating;
import com.laba.partner.data.network.model.Request_;
import com.laba.partner.data.network.model.UserResponse;
import com.laba.partner.ui.RideModel;
import com.laba.partner.ui.dialog.RideAcceptedDialog;

import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.StringTokenizer;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class IncomingRequestFragment extends BaseFragment implements IncomingRequestIView {

    public static CountDownTimer countDownTimer;
    public static MediaPlayer mPlayer;
    @BindView(R.id.btnReject)
    Button btnReject;
    @BindView(R.id.btnAccept)
    Button btnAccept;
    Unbinder unbinder;
    @BindView(R.id.lblCount)
    TextView lblCount;
    @BindView(R.id.imgUser)
    CircleImageView imgUser;
    @BindView(R.id.lblUserName)
    TextView lblUserName;
    @BindView(R.id.ratingUser)
    AppCompatRatingBar ratingUser;
    @BindView(R.id.lblLocationName)
    TextView lblLocationName;
    @BindView(R.id.lblDrop)
    TextView lblDrop;
    @BindView(R.id.lblScheduleDate)
    TextView lblScheduleDate;
    @BindView(R.id.service_name)
    TextView serviceName;
    @BindView(R.id.lblDistanceTravelled)
    TextView lblDistanceTravelled;
    @BindView(R.id.payment_mode)
    TextView payment_mode;
    @BindView(R.id.time_distance)
    TextView time_distance;
    @BindView(R.id.price)
    TextView price;
    private final IncomingRequestPresenter presenter = new IncomingRequestPresenter();
    private Context context;
    String latitude, longitude;
    private static final int REQUEST_LOCATION = 1;
    public Criteria criteria;
    HashMap<String, String> RIDE_REQUEST = new HashMap<String, String>();//Creating HashMap
    private boolean mLocationPermissionGranted;
    private FusedLocationProviderClient mFusedLocation;

    @Override
    public int getLayoutId() {
        return R.layout.fragment_incoming_request;
    }

    @Override
    public View initView(View view) {
        unbinder = ButterKnife.bind(this, view);
        context = getContext();
        presenter.attachView(this);
        if (DATUM != null) {

            Request_ datum = DATUM;
        }
        presenter.getProfile();
        mPlayer = MediaPlayer.create(context, R.raw.alert_tone);
        mFusedLocation = LocationServices.getFusedLocationProviderClient(context);

        init();


        return view;
    }

    public static boolean isLocationEnabled(Context context) {
        //...............
        return true;
    }

    private void getLocation() {

        try {
            if (checkLocationPermission()) {

                Task<Location> locationResult = mFusedLocation.getLastLocation();
                locationResult.addOnCompleteListener((Activity) getContext(), task -> {

                    if (task.isSuccessful() && task.getResult() != null) {
                        // Set the map's camera position to the current location of the device.

                        mLastKnownLocation = task.getResult();
                        locationUpdates(mLastKnownLocation);


                    } else {
                        Log.d("Map", "Current location is null. Using defaults.");
                        Log.e("Map", "Exception: %s", task.getException());
                    }
                });

            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void locationUpdates(Location mLastKnownLocation) {

        Request_ data = DATUM;
        if (data != null) {
            Location startPoint = new Location("locationA");
            startPoint.setLatitude(Double.parseDouble(String.valueOf(mLastKnownLocation.getLatitude())));
            startPoint.setLongitude(Double.parseDouble(String.valueOf(mLastKnownLocation.getLongitude())));

            Location endPoint = new Location("locationB");
            endPoint.setLatitude(data.getSLatitude());
            endPoint.setLongitude(data.getSLongitude());

            Log.e("testData", String.valueOf(endPoint));

            float distance = startPoint.distanceTo(endPoint) / 1000;

            double estimatedDriveTimeInMinutes = distance * 3;

            distance = (int) (Math.round(distance));
            estimatedDriveTimeInMinutes = (int) (Math.round(estimatedDriveTimeInMinutes));
            String result = String.valueOf(estimatedDriveTimeInMinutes);
            result = (result.indexOf(".") >= 0 ? result.replaceAll("\\.?0+$", "") : result);
            String result1 = String.valueOf(distance);
            result1 = (result1.indexOf(".") >= 0 ? result1.replaceAll("\\.?0+$", "") : result1);
            time_distance.setText(result + "min (" + result1 + "km) away");

        }
    }

    public boolean checkLocationPermission() {
        int result = ContextCompat.checkSelfPermission(this.getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION);
        int result1 = ContextCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_COARSE_LOCATION);
        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED;
    }


    @SuppressLint("SetTextI18n")
    void init() {

        Request_ data = DATUM;


        if (data != null) {
            lblUserName.setText(String.format("%s %s", data.getUser().getFirstName(),
                    data.getUser().getLastName()));
            lblDistanceTravelled.setText(data.getDistance() + " km");
            payment_mode.setText(data.getPaymentMode());
            RIDE_REQUEST.put("payment_mode", data.getPaymentMode());
            RIDE_REQUEST.put("service_type", String.valueOf(data.getServiceTypeId()));
            RIDE_REQUEST.put("d_longitude", String.valueOf(data.getDLongitude()));
            RIDE_REQUEST.put("distance", String.valueOf(data.getDistance()));
            RIDE_REQUEST.put("s_latitude", String.valueOf(data.getSLatitude()));
            RIDE_REQUEST.put("d_latitude", String.valueOf(data.getDLatitude()));
            RIDE_REQUEST.put("d_address", data.getSAddress());
            RIDE_REQUEST.put("s_address", data.getSAddress());
            RIDE_REQUEST.put("s_longitude", String.valueOf(data.getSLongitude()));

            estimatedApiCall();




            if(data.getScheduleAt()!=null){
                String scheduledDate = parseDateToddMMyyyy(data.getScheduleAt());;
                Animation anim = new AlphaAnimation(0.0f, 1.0f);
                anim.setDuration(1000); //You can manage the blinking time with this parameter
                anim.setStartOffset(20);
                anim.setRepeatMode(Animation.REVERSE);
                anim.setRepeatCount(Animation.INFINITE);
                time_distance.startAnimation(anim);
                time_distance.setText("Upcoming Ride - " + scheduledDate);
                time_distance.setTextColor(getResources().getColor(R.color.colorPrimary));
            }else{
                if (checkLocationPermission()) {

                    getLocation();

                } else {
                    latitude = "-33.8523341";
                    longitude = "151.2106085";}
            }

            ratingUser.setRating(Float.parseFloat(data.getUser().getRating()));
            if (data.getSAddress() != null && !data.getSAddress().isEmpty()
                    || data.getDAddress() != null && !data.getDAddress().isEmpty()) {
                lblLocationName.setText(data.getSAddress());
                lblDrop.setText(data.getDAddress());
            }
            if (data.getUser().getPicture() != null)
                Glide.with(activity()).load(BuildConfig.BASE_IMAGE_URL + data.getUser()
                        .getPicture()).apply(RequestOptions.placeholderOf(R.drawable.ic_user_placeholder)
                        .dontAnimate().error(R.drawable.ic_user_placeholder)).into(imgUser);
        }

        if (data.getIsScheduled() != null/* && data.getScheduleAt() != null*/) {
            String isScheduled = data.getIsScheduled();
            String scheduledAt = data.getScheduleAt();

            if (isScheduled != null && isScheduled.equalsIgnoreCase("NO")) {
                lblScheduleDate.setVisibility(View.INVISIBLE);
            } else {
                if (scheduledAt != null && isScheduled.equalsIgnoreCase("YES")) {
                    StringTokenizer tk = new StringTokenizer(scheduledAt);
                    String date = tk.nextToken();
                    String time = tk.nextToken();
                    @SuppressLint("SimpleDateFormat")
                    SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss");
                    @SuppressLint("SimpleDateFormat")
                    SimpleDateFormat sdfs = new SimpleDateFormat("hh:mm a");
                    Date dt;
                    try {
                        dt = sdf.parse(time);
                        lblScheduleDate.setVisibility(View.VISIBLE);
                        lblScheduleDate.setText(getString(R.string.schedule_for) + " " +
                                Utilities.convertDate(scheduledAt) + " " + sdfs.format(dt));
                    } catch (ParseException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                }
            }
            countDownTimer = new CountDownTimer(time_to_left * 1000, 1000) {

                public void onTick(long millisUntilFinished) {
                    int time = (int) (millisUntilFinished / 1000);
                    if (time == 0) {
                        close();
                        try {
                            context.sendBroadcast(new Intent("INTENT_FILTER"));
                            mPlayer.stop();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return;
                    }
                    lblCount.setText(String.valueOf(time));
                    setTvZoomInOutAnimation(lblCount);
                }

                public void onFinish() {
                }
            };
            countDownTimer.start();
        }

        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
                mMessageReceiver, new IntentFilter("UserCancelled"));
    }


    private final BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e("User Cancelled", "onReceive");
            boolean cancelled = intent.getBooleanExtra("Cancelled", false);
            if (cancelled) {
                countDownTimer.cancel();
                requireActivity().getSupportFragmentManager().beginTransaction().remove(IncomingRequestFragment.this).commitAllowingStateLoss();
                Toasty.error(context, getString(R.string.ride_cancelled), Toast.LENGTH_SHORT, true).show();
                //       context.sendBroadcast(new Intent("INTENT_FILTER"));

            }
        }
    };

    private void setTvZoomInOutAnimation(final TextView textView) {
        final float startSize = 20;
        final float endSize = 13;
        final int animationDuration = 900; // Animation duration in ms

        ValueAnimator animator = ValueAnimator.ofFloat(startSize, endSize);
        animator.setDuration(animationDuration);

        animator.addUpdateListener(valueAnimator -> {
            float animatedValue = (Float) valueAnimator.getAnimatedValue();
            textView.setTextSize(animatedValue);
        });
        //animator.setRepeatCount(ValueAnimator.INFINITE);  // Use this line for infinite animations
        animator.setRepeatCount(2);
        animator.start();
    }

    public void estimatedApiCall() {

        Call<EstimateFare> call = APIClient.getAPIClient().estimateFare(RIDE_REQUEST);

        call.enqueue(new Callback<EstimateFare>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(@NonNull Call<EstimateFare> call,
                                   @NonNull Response<EstimateFare> response) {

                try {
                    hideLoading();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                if (response.body() != null) {
                    EstimateFare estimateFare = response.body();
                    int finalprice = (int) estimateFare.getEstimatedFare();
                    int fixedprice = (int) (finalprice-estimateFare.getCommission_percentage());
                    int count = countDigit(finalprice);

                    if(count>4){
                        price.setTextSize(35);
                    }
                    price.setText("\u20B9"+finalprice+" - \u20B9"+fixedprice);

                } else if (response.raw().code() == 500) {
                    try {
                        JSONObject object = new JSONObject(response.errorBody().string());
                        if (object.has("error"))
                            Toast.makeText(activity(), object.optString("error"), Toast.LENGTH_SHORT).show();

                    } catch (Exception exp) {
                        exp.printStackTrace();
                    }
                }

            }


            @Override
            public void onFailure(@NonNull Call<EstimateFare> call, @NonNull Throwable t) {
//                try {
//                    hideLoading();
//                } catch (Exception e1) {
//                    e1.printStackTrace();
//                }
                try {
                    onErrorBase(t);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                System.out.println(" call = [" + call + "], t = [" + t + "]");

            }

        });

    }

    int countDigit(int n)
    {
        if (n == 0)
            return 1;
        int count = 0;
        while (n != 0) {
            n = n / 10;
            ++count;
        }
        return count;
    }

    public String parseDateToddMMyyyy(String time) {
        String inputPattern = "yyyy-MM-dd HH:mm:ss";
        String outputPattern = "dd-MMM-yyyy h:mm a";
        SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern);
        SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern);

        Date date = null;
        String str = null;

        try {
            date = inputFormat.parse(time);
            str = outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return str;
    }

    @OnClick({R.id.btnReject, R.id.btnAccept})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btnReject:
                close();
                if (DATUM != null) {
                    Request_ datum = DATUM;
//                    showLoading();
                    presenter.cancel(datum.getId());
                    time_to_left = 160;
                }

                break;
            case R.id.btnAccept:
                if (DATUM != null) {
                    Request_ datum = DATUM;
                    showLoading();
                    presenter.accept(datum.getId());
                    countDownTimer.cancel();
                    getContext().sendBroadcast(new Intent("INTENT_FILTER"));
                }
                break;
        }
    }

    @Override
    public void onSuccessAccept(RideModel responseBody) {
        //   countDownTimer.cancel();
        hideLoading();
//        Toast.makeText(getContext(), getString(R.string.ride_accepted), Toast.LENGTH_SHORT).show();
        //     getContext().sendBroadcast(new Intent("INTENT_FILTER"));
        try {
            getActivity().getSupportFragmentManager().beginTransaction().remove(IncomingRequestFragment.this).commitAllowingStateLoss();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (responseBody.getStatus().equalsIgnoreCase("SCHEDULED")) {

                new RideAcceptedDialog(getActivity(), new RideAcceptedDialog.Callback() {
                    @Override
                    public void showRideDetails() {
//                        getActivity().startActivity(new Intent(getActivity(), YourTripActivity.class));
                    }
                }).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onSuccessCancel(Object object) {
        countDownTimer.cancel();
        hideLoading();
        getActivity().getSupportFragmentManager().beginTransaction().remove(IncomingRequestFragment.this).commitAllowingStateLoss();
        Toasty.success(context, getString(R.string.ride_cancelled), Toast.LENGTH_SHORT, true).show();
        context.sendBroadcast(new Intent("INTENT_FILTER"));
    }

    @Override
    public void onError(Throwable e) {
        try {
            hideLoading();
            if (mPlayer.isPlaying()) mPlayer.stop();
            if (e != null)
                onErrorBase(e);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    @Override
    public void onSuccess(Rating trendsResponse) {

    }

    @Override
    public void onSuccess(UserResponse userResponse) {
        if (userResponse.getService() != null)
            serviceName.setText((userResponse.getService().getServiceType() != null)
                    ? userResponse.getService().getServiceType().getName() : "");

    }

    @Override
    public void onStart() {
        super.onStart();
        if (!mPlayer.isPlaying())
            mPlayer.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mPlayer.isPlaying())
            mPlayer.stop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPlayer.isPlaying())
            mPlayer.stop();
    }

    private void close() {
        getActivity().getSupportFragmentManager().beginTransaction().remove(IncomingRequestFragment.this).commitAllowingStateLoss();
    }

}