package com.laba.partner.ui.fragment.offline;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import com.laba.partner.BuildConfig;
import com.laba.partner.R;
import com.laba.partner.base.BaseFragment;
import com.laba.partner.common.Constants;
import com.laba.partner.common.swipe_button.SwipeButton;
import com.laba.partner.ui.activity.document.DocumentActivity;
import com.laba.partner.ui.activity.main.MainActivity;

import org.json.JSONObject;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import es.dmoral.toasty.Toasty;

import static com.laba.partner.common.fcm.MyFireBaseMessagingService.INTENT_FILTER;

public class OfflineFragment extends BaseFragment implements OfflineIView, MainActivity.DisableDriver {

    @BindView(R.id.menu_img)
    ImageView menuImg;
    @BindView(R.id.tvApprovalDesc)
    TextView tvApprovalDesc;
    @BindView(R.id.swipeBtnEnabled)
    SwipeButton swipeBtnEnabled;
    @BindView(R.id.button3)
    Button uploadFilesBt;
    private OfflinePresenter presenter = new OfflinePresenter();
    private String message = "";

    private DrawerLayout drawer;
    private Context context;

    private String title;

    public OfflineFragment(String title) {
        this.title = title;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_offline;
    }

    @Override
    public View initView(View view) {
        ButterKnife.bind(this, view);
        this.context = getContext();
        presenter.attachView(this);
        drawer = activity().findViewById(R.id.drawer_layout);
        String s = getArguments().getString("status");
        if (!TextUtils.isEmpty(s))
            if (s.equalsIgnoreCase(Constants.User.Account.onBoarding))
                tvApprovalDesc.setVisibility(View.VISIBLE);
            else if (s.equalsIgnoreCase(Constants.User.Account.banned)) {
                tvApprovalDesc.setVisibility(View.VISIBLE);
                tvApprovalDesc.setText(getString(R.string.banned_desc));
            } else if (s.equalsIgnoreCase(Constants.User.Service.offLine) || s.equalsIgnoreCase(Constants.User.Account.wallet)) {
                tvApprovalDesc.setVisibility(View.VISIBLE);
                tvApprovalDesc.setText("");
            } else {
                tvApprovalDesc.setVisibility(View.VISIBLE);
                tvApprovalDesc.setText(s);
            }

        swipeBtnEnabled.setOnStateChangeListener(active -> {
            if (active) {

//                enableGPS();

                if(!((MainActivity)getActivity()).isGPSEnabled()){
                    Toast.makeText(getActivity(),"Please enable gps to turn online",Toast.LENGTH_LONG).show();
                    return;
                }

                MainActivity mainActivity = (MainActivity) getActivity();
                if (mainActivity.checkLocationPermission()) {

                    if (isIgnoringBatteryOptimizations()) {
                        showLoading();
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("service_status", Constants.User.Service.active);
                        presenter.providerAvailable(map);
                        hideLoading();
                    } else {
                        swipeBtnEnabled.toggleState();
                       /* new AlertDialog.Builder(requireActivity())
                                .setMessage("Need ignore battery optimisation permission for getting ride and your actual location to us")
                                .setPositiveButton("ok", (dialog, which) -> {*/
                                    Intent intent = new Intent();
                                    String packageName = requireActivity().getPackageName();
                                    PowerManager pm = (PowerManager) requireActivity().getSystemService(Context.POWER_SERVICE);
                                    if (pm.isIgnoringBatteryOptimizations(packageName))
                                        intent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                                    else {
                                        intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                                        intent.setData(Uri.parse("package:" + packageName));
                                    }
                                    requireActivity().startActivity(intent);
                             //   }).show();
                    }
                } else {
                    mainActivity.requestLocation();
                    swipeBtnEnabled.toggleState();
                }
            }
        });
        return view;
    }

    Boolean isIgnoringBatteryOptimizations() {
        PowerManager pm = (PowerManager) requireActivity().getApplication().getSystemService(Context.POWER_SERVICE);
        String packageName = requireActivity().getApplication().getPackageName();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return pm.isIgnoringBatteryOptimizations(packageName);
        }
        return true;
    }

    @SuppressLint("WrongConstant")
    @OnClick({R.id.menu_img})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.menu_img:
                drawer.openDrawer(Gravity.START);
                break;
        }
    }

    @Override
    public void onSuccess(Object object) {
        try {
            JSONObject jsonObj = new JSONObject(new Gson().toJson(object));
            if (jsonObj.has("error"))
                Toasty.error(activity(), jsonObj.optString("error"), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.e("TAGG", "OfflineFragment");
        context.sendBroadcast(new Intent(INTENT_FILTER));
    }

    @Override
    public void onError(Throwable e) {
        hideLoading();
        swipeBtnEnabled.toggleState();
        if (e != null) try {
            onErrorBase(e);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    @Override
    public void enable(String s) {

        /*if(!message.isEmpty() && !message.equalsIgnoreCase(s)){
            tvApprovalDesc.setText(s);
        }

        this.message = s;*/

        tvApprovalDesc.setText(s);
//        swipeBtnEnabled.setVisibility(View.VISIBLE);
        swipeBtnEnabled.setEnabled(true);


    }

    @Override
    public void disable(String s,@ColorInt int color) {
//        swipeBtnEnabled.setVisibility(View.INVISIBLE);

        /*if(message.isEmpty()){
            tvApprovalDesc.setText(s);
        }

        this.message = s;

        if(!message.equalsIgnoreCase(s)){
            tvApprovalDesc.setText(s);
        }*/

        tvApprovalDesc.setText(s);
        tvApprovalDesc.setTextColor(color);
        /*swipeBtnEnabled.setEnabled(false);
        swipeBtnEnabled.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Toast.makeText(getActivity(),text,Toast.LENGTH_SHORT).show();
                return true;
            }
        });*/

        if (s.equalsIgnoreCase("\n\nUpload your documents for verification.")) {
            uploadFilesBt.setVisibility(View.VISIBLE);
            uploadFilesBt.setOnClickListener(view -> startActivity(new Intent(getActivity(), DocumentActivity.class)));
        }
    }

}
