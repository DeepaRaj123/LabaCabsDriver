package com.laba.partner.ui.bottomsheetdialog.invoice_flow;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.laba.partner.MvpApplication;
import com.laba.partner.R;
import com.laba.partner.base.BaseFragment;
import com.laba.partner.common.Constants;
import com.laba.partner.common.SharedHelper;
import com.laba.partner.data.network.model.Payment;
import com.laba.partner.data.network.model.Request_;

import java.util.HashMap;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import butterknife.internal.ListenerClass;

import static com.laba.partner.base.BaseActivity.DATUM;
import static com.laba.partner.common.SharedHelper.getKey;

public class InvoiceDialogFragment extends BaseFragment implements InvoiceDialogIView {

    @BindView(R.id.promotion_amount)
    TextView promotionAmount;
    @BindView(R.id.wallet_amount)
    TextView walletAmount;
    @BindView(R.id.booking_id)
    TextView bookingId;
    @BindView(R.id.total_amount)
    TextView totalAmount;
    @BindView(R.id.payable_amount)
    TextView payableAmount;
    @BindView(R.id.payment_mode_img)
    ImageView paymentModeImg;
    @BindView(R.id.payment_mode_layout)
    LinearLayout paymentModeLayout;
    @BindView(R.id.llAmountToBePaid)
    LinearLayout llAmountToBePaid;
    @BindView(R.id.distance)
    TextView distance;
    @BindView(R.id.time_fare)
    TextView timeFare;
    @BindView(R.id.fixed)
    TextView fixed;
    @BindView(R.id.distance_fare)
    TextView distanceFare;
    @BindView(R.id.tax)
    TextView tax;
    @BindView(R.id.llBaseFare)
    LinearLayout llBaseFare;
    InvoiceDialogPresenter presenter;
    @BindView(R.id.lblPaymentType)
    TextView lblPaymentType;
    @BindView(R.id.llDistanceFareContainer)
    LinearLayout llDistanceFareContainer;
    @BindView(R.id.llTimeFareContainer)
    LinearLayout llTimeFareContainer;
    Unbinder unbinder;
    private Payment payment;

    @Override
    public int getLayoutId() {
        return R.layout.fragment_invoice_dialog;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View initView(View view) {
        unbinder = ButterKnife.bind(this, view);
        presenter = new InvoiceDialogPresenter();
        presenter.attachView(this);
        // setCancelable(false);
        if (DATUM != null) {
            Request_ datum = DATUM;
            bookingId.setText(datum.getBookingId());

            if (datum.getPayment() != null) {
                if (datum.getPayment().getTotal() > 0)
                    totalAmount.setText(Constants.Currency + " " +
                            MvpApplication.getInstance().getNewNumberFormat(Double.parseDouble(datum.getPayment().getTotal() + "")));
                if (datum.getPayment().getFixed() > 0) {
                    llBaseFare.setVisibility(View.VISIBLE);
                    fixed.setText(Constants.Currency + " " +
                            MvpApplication.getInstance().getNewNumberFormat(Double.parseDouble(datum.getPayment().getFixed() + "")));
                }
                if (datum.getPayment().getPayable() > 0) {
                    llAmountToBePaid.setVisibility(View.VISIBLE);
                    payableAmount.setText(Constants.Currency + " " +
                            MvpApplication.getInstance().getNewNumberFormat(Double.parseDouble(datum.getPayment().getPayable() + "")));
                } else llAmountToBePaid.setVisibility(View.GONE);
                if (datum.getDistance() > 1 || datum.getDistance() > 1.0)
                    distance.setText(String.format("%s %s", datum.getDistance(), "Kms"));
                else
                    distance.setText(String.format("%s %s", datum.getDistance(), "Kms"));
                if (datum.getPayment().getHour() >0)
                    timeFare.setText(Constants.Currency + " " +
                            MvpApplication.getInstance().getNewNumberFormat(Double.parseDouble(datum.getPayment().getHour() + "")));
                else if(datum.getPayment().getMinute() >0)
                    timeFare.setText(Constants.Currency + " " +
                            MvpApplication.getInstance().getNewNumberFormat(Double.parseDouble(datum.getPayment().getMinute() + "")));
                else llTimeFareContainer.setVisibility(View.GONE);
                    tax.setText(Constants.Currency + " " +
                            MvpApplication.getInstance().getNewNumberFormat(Double.parseDouble(datum.getPayment().getTax() + "")));
                if (datum.getPayment().getDistance() == 0.0 || datum.getPayment().getDistance() == 0)
                    llDistanceFareContainer.setVisibility(View.GONE);
                else {
                    llDistanceFareContainer.setVisibility(View.VISIBLE);
                    distanceFare.setText(Constants.Currency + " " +
                            MvpApplication.getInstance().getNewNumberFormat(Double.parseDouble(datum.getPayment().getDistance()  + "")));

                }
            }
        }
        return view;
    }

    @Override
    public void onSuccess(Object object) {
        hideLoading();
        activity().sendBroadcast(new Intent("INTENT_FILTER"));
    }

    @Override
    public void onError(Throwable e) {
        hideLoading();
        try {
            if (e != null)
                onErrorBase(e);
        } catch (Exception e1) {
            e1.printStackTrace();
        }

    }

    @OnClick(R.id.btnConfirmPayment)
    public void onViewClicked() {

        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setTitle("Are you sure you collected the amount?");
        dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (DATUM != null) {
                    Request_ datum = DATUM;
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("status", Constants.checkStatus.COMPLETED);
                    map.put("_method", Constants.checkStatus.PATCH);
                    showLoading();
                    presenter.statusUpdate(map, datum.getId());
                }
            }
        });
        dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        dialog.show();
    }

   /* @SuppressLint("SetTextI18n")
    @Override
    public void initView(View view) {
        unbinder = ButterKnife.bind(this, view);
        presenter = new InvoiceDialogPresenter();
        presenter.attachView(this);
        setCancelable(false);
        numberFormat = MvpApplication.getInstance().getNewNumberFormat();

        if (DATUM != null) {
            Request_ datum = DATUM;
            bookingId.setText(datum.getBookingId());
            if (datum.getPayment() != null)
                if (datum.getPayment().getTotal() != 0 ||
                        datum.getPayment().getPayable() != 0) {
                    totalAmount.setText(Constants.Currency + " " + numberFormat.format(Double.parseDouble(datum.getPayment().getTotal() + "")));
                    payableAmount.setText(Constants.Currency + " " + numberFormat.format(Double.parseDouble(datum.getPayment().getPayable() + "")));
                }
        }
    }

    *//*@Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //Save the fragment's instance
        getActivity().getSupportFragmentManager().putFragment(outState, "InvoiceDialogFragment", InvoiceDialogFragment.this);
    }*//*

    @OnClick(R.id.btnConfirmPayment)
    public void onViewClicked() {

        if (DATUM != null) {
            Request_ datum = DATUM;
            HashMap<String, Object> map = new HashMap<>();
            map.put("status", Constants.checkStatus.COMPLETED);
            map.put("_method", Constants.checkStatus.PATCH);
            showLoading();
            presenter.statusUpdate(map, datum.getId());
        }
    }

    @Override
    public void onSuccess(Object object) {
        dismissAllowingStateLoss();
        hideLoading();
        activity().sendBroadcast(new Intent("INTENT_FILTER"));
    }


    @Override
    public void onError(Throwable e) {
        hideLoading();
    }*/
}
