package com.laba.partner.ui.fragment.status_flow;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatRatingBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.chaos.view.PinView;
import com.google.android.gms.maps.model.LatLng;
import com.laba.partner.BuildConfig;
import com.laba.partner.R;
import com.laba.partner.base.BaseActivity;
import com.laba.partner.base.BaseFragment;
import com.laba.partner.common.Constants;
import com.laba.partner.common.SharedHelper;
import com.laba.partner.common.chat.ChatActivity;
import com.laba.partner.data.network.model.Request_;
import com.laba.partner.data.network.model.TripResponse;
import com.laba.partner.ui.activity.main.MainActivity;

import java.util.HashMap;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;

import static com.laba.partner.base.BaseActivity.DATUM;

public class StatusFlowFragment extends BaseFragment implements StatusFlowIView {

    @BindView(R.id.user_name)
    TextView userName;
    @BindView(R.id.user_rating)
    AppCompatRatingBar userRating;
    @BindView(R.id.imgCall)
    ImageView imgCall;
    @BindView(R.id.btnCancel)
    Button btnCancel;
    @BindView(R.id.btnStatus)
    Button btnStatus;
    @BindView(R.id.status_arrived_img)
    ImageView statusArrivedImg;
    @BindView(R.id.status_picked_up_img)
    ImageView statusPickedUpImg;
    @BindView(R.id.status_finished_img)
    ImageView statusFinishedImg;
    @BindView(R.id.user_img)
    CircleImageView userImg;
    Unbinder unbinder;
    Request_ data = null;
    TripResponse tripResponse = null;
    String STATUS = "";
    AlertDialog otpDialog;
    @BindView(R.id.imgMsg)
    ImageView imgMsg;
    @BindView(R.id.arrived_view)
    View arrivedView;
    private StatusFlowPresenter presenter = new StatusFlowPresenter();
    private Context thisContext;

    @Override
    public int getLayoutId() {
        return R.layout.fragment_status_flow;
    }

    @Override
    public View initView(View view) {
        unbinder = ButterKnife.bind(this, view);
        presenter.attachView(this);
        this.thisContext = getContext();
        init();
        return view;
    }

    private void init() {
        data = BaseActivity.DATUM;
        tripResponse = BaseActivity.tripResponse;
        //  Utilities.printV("data===>", data.getStatus());

        if (data != null && data.getStatus() != null) {
            LatLng currentLocation;
            changeFlow(data.getStatus());
            LatLng origin = new LatLng(data.getSLatitude(), data.getSLongitude());
            LatLng destination = new LatLng(data.getDLatitude(), data.getDLongitude());
            if (tripResponse != null && tripResponse.getProviderDetails() != null)
                currentLocation = new LatLng(tripResponse.getProviderDetails().getLatitude(),
                        tripResponse.getProviderDetails().getLongitude());
            else
                currentLocation = new LatLng(Double.parseDouble(SharedHelper.getKey(getContext(), "latitude")),
                        Double.parseDouble(SharedHelper.getKey(getContext(), "longitude")));
            if (data.getStatus().equalsIgnoreCase(Constants.checkStatus.ACCEPTED) ||
                    data.getStatus().equalsIgnoreCase(Constants.checkStatus.STARTED))
                ((MainActivity) getContext()).drawRoute(currentLocation, origin);
            else ((MainActivity) getContext()).drawRoute(origin, destination);
        }
    }

    @OnClick({R.id.imgCall, R.id.btnCancel, R.id.btnStatus, R.id.imgMsg})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.imgCall:
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + data.getUser().getMobile()));
                startActivity(intent);
                break;
            case R.id.btnCancel:
                SharedHelper.putKey(thisContext, Constants.SharedPref.cancel_id, String.valueOf(data.getId()));
                cancelRequestPopup();
                break;
            case R.id.btnStatus:

                if (STATUS.equalsIgnoreCase(Constants.checkStatus.PICKEDUP)) {
                    if (data.getOtp() != null && !data.getOtp().equalsIgnoreCase("")) {
                        showOTP();
                    } else {
                        statusUpdateCall(STATUS);
                    }
                } else {
                    if (STATUS.equalsIgnoreCase(Constants.checkStatus.ARRIVED)) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setTitle("Are you sure taxi arrived at the pickup location?");
                        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                statusUpdateCall(STATUS);
                            }
                        });
                        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
                        builder.show();
                    } else if (STATUS.equalsIgnoreCase(Constants.checkStatus.DROPPED)) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setTitle("Are you sure want to end the trip?");
                        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                statusUpdateCall(STATUS);
                            }
                        });
                        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
                        builder.show();
                    } else {
                        statusUpdateCall(STATUS);
                    }
                }
                break;
            case R.id.imgMsg:
                if (DATUM != null) {
                    Intent i = new Intent(thisContext, ChatActivity.class);
                    i.putExtra(Constants.SharedPref.request_id, String.valueOf(DATUM.getId()));
                    startActivity(i);
                }
                break;
        }
    }

    @SuppressLint("SetTextI18n")
    public void changeFlow(String status) {

        btnCancel.setVisibility(View.GONE);
        userName.setText(data.getUser().getFirstName() + " " + data.getUser().getLastName());
        userRating.setRating(Float.parseFloat(data.getUser().getRating()));

        if (data.getUser().getPicture() != null)
            Glide.with(thisContext).
                    load(BuildConfig.BASE_IMAGE_URL +
                            data.getUser().getPicture()).
                    apply(RequestOptions.placeholderOf(R.drawable.ic_user_placeholder).
                            dontAnimate().error(R.drawable.ic_user_placeholder)).into(userImg);

        if ("PICKEDUP".equalsIgnoreCase(status)) imgMsg.setVisibility(View.GONE);
        else imgMsg.setVisibility(View.VISIBLE);

        switch (status) {
            case Constants.checkStatus.ACCEPTED:
                btnStatus.setText(getString(R.string.arrived));
                btnCancel.setVisibility(View.VISIBLE);
                STATUS = Constants.checkStatus.STARTED;
                break;
            case Constants.checkStatus.STARTED:
                btnStatus.setText(getString(R.string.arrived));
                btnCancel.setVisibility(View.VISIBLE);
                STATUS = Constants.checkStatus.ARRIVED;
                break;
            case Constants.checkStatus.ARRIVED:
                btnStatus.setText(getString(R.string.pick_up_invoice));
                btnCancel.setVisibility(View.VISIBLE);
                STATUS = Constants.checkStatus.PICKEDUP;
                statusArrivedImg.setImageResource(R.drawable.ic_arrived_select);
                statusPickedUpImg.setImageResource(R.drawable.ic_pickup);
                statusFinishedImg.setImageResource(R.drawable.ic_finished);
                break;
            case Constants.checkStatus.PICKEDUP:
                arrivedView.setBackgroundColor(getResources().getColor(R.color.colorPrimaryText));
                btnStatus.setBackgroundResource(R.drawable.button_round_primary);
                btnStatus.setText(getString(R.string.tap_when_dropped));
                STATUS = Constants.checkStatus.DROPPED;
                statusArrivedImg.setImageResource(R.drawable.ic_arrived_select);
                statusPickedUpImg.setImageResource(R.drawable.ic_pickup_select);
                statusFinishedImg.setImageResource(R.drawable.ic_finished);
                break;
            default:
                break;
        }
    }

    @Override
    public void onSuccess(Object object) {
        hideLoading();
        if (isAdded()) {
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .remove(StatusFlowFragment.this).commit();
            getContext().sendBroadcast(new Intent("INTENT_FILTER"));
        }
    }

    @Override
    public void onSuccessCancel(Object object) {
        Toast.makeText(getContext(), getString(R.string.ride_cancel_successfull),
                Toast.LENGTH_SHORT).show();
        hideLoading();
        requireActivity().sendBroadcast(new Intent("INTENT_FILTER"));
    }

    @Override
    public void onError(Throwable e) {
        hideLoading();
        try {
            if (e != null)
                onErrorBase(e);
        } catch (Exception throwable) {
            throwable.printStackTrace();
        }

        requireActivity().getSupportFragmentManager().beginTransaction()
                .remove(StatusFlowFragment.this).commit();
        getContext().sendBroadcast(new Intent("INTENT_FILTER"));
    }

    void statusUpdateCall(String status) {
        if (DATUM != null) {
            Request_ datum = DATUM;
            HashMap<String, Object> map = new HashMap<>();
            map.put("status", status);
            map.put("_method", "PATCH");
            showLoading();
            presenter.statusUpdate(map, datum.getId());
        }
    }

    void cancelRequestPopup() {

        try {

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(thisContext);
            // set dialog message
            alertDialogBuilder
                    .setMessage(thisContext.getResources().getString(R.string.cancel_request_title))
                    .setCancelable(false)
                    .setPositiveButton(thisContext.getResources().getString(R.string.yes), (dialog, id) -> {
                        cancelDialog();
                       /* try {
                            CancelDialogFragment cancelDialogFragment = new CancelDialogFragment();
                            cancelDialogFragment.show(getActivity().getSupportFragmentManager(), cancelDialogFragment.getTag());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }*/
                    }).setNegativeButton(thisContext.getResources().getString(R.string.no), (dialog, id) -> dialog.cancel());

            // create alert dialog
            AlertDialog alertDialog = alertDialogBuilder.create();

            // show it
            alertDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showOTP() {
        AlertDialog.Builder builder = new AlertDialog.Builder(thisContext);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.otp_dialog, null);

        Button submitBtn = view.findViewById(R.id.submit_btn);
        Button btnCall = view.findViewById(R.id.btnCall);
        Button btnMsg = view.findViewById(R.id.btnMsg);
        final PinView pinView = view.findViewById(R.id.pinView);

        builder.setView(view);
        otpDialog = builder.create();
        otpDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        btnCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + data.getUser().getMobile()));
                startActivity(intent);
            }
        });

        btnMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (DATUM != null) {
                    Intent i = new Intent(thisContext, ChatActivity.class);
                    i.putExtra(Constants.SharedPref.request_id, String.valueOf(DATUM.getId()));
                    startActivity(i);
                }
            }
        });

        submitBtn.setOnClickListener(view1 -> {

            if (data.getOtp().equalsIgnoreCase(pinView.getText().toString())) {
                try {
                    if (thisContext != null)
                        Toasty.success(thisContext, thisContext.getResources().getString(R.string.otp_verified), Toast.LENGTH_SHORT).show();
                    statusUpdateCall(STATUS);
                    otpDialog.dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else try {
                if (thisContext != null && isAdded())
                    Toasty.error(thisContext, thisContext.getResources().getString(R.string.otp_wrong), Toast.LENGTH_SHORT).show();
                else
                    Toasty.error(thisContext, thisContext.getResources().getString(R.string.otp_wrong), Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
            }

        });
        otpDialog.show();
    }


    private void cancelDialog() {

        AlertDialog alertDialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View view = getLayoutInflater().inflate(R.layout.fragment_cancel, null);
        builder.setView(view);
        alertDialog = builder.create();

        EditText cancelReason = view.findViewById(R.id.txtComments);

        view.findViewById(R.id.btnSubmit).setOnClickListener(view1 -> {
            if (cancelReason.getText().toString().isEmpty()) {
                Toasty.error(getActivity(), "Please provide valid cancel reason").show();
                return;
            }
            alertDialog.dismiss();
            String comment = cancelReason.getText().toString();
            HashMap<String, Object> map = new HashMap<>();
            map.put("id", SharedHelper.getKey(requireContext(), Constants.SharedPref.cancel_id));
            map.put("cancel_reason", comment);
            showLoading();
            presenter.cancelRequest(map);
        });

        view.findViewById(R.id.dismiss).setOnClickListener(view1 -> alertDialog.dismiss());

        alertDialog.show();
    }
}
