package com.laba.partner.ui.activity.upcoming_detail;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatRatingBar;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.laba.partner.BuildConfig;
import com.laba.partner.R;
import com.laba.partner.base.BaseActivity;
import com.laba.partner.common.Constants;
import com.laba.partner.common.SharedHelper;
import com.laba.partner.data.network.model.HistoryDetail;
import com.laba.partner.data.network.model.User_Past;
import com.laba.partner.ui.bottomsheetdialog.cancel.CancelDialogFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;

public class UpcomingTripDetailActivity extends BaseActivity implements UpcomingTripDetailIView {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.cancel)
    Button cancel;
    @BindView(R.id.call)
    Button call;
    @BindView(R.id.static_map)
    ImageView staticMap;
    @BindView(R.id.avatar)
    CircleImageView avatar;
    @BindView(R.id.first_name)
    TextView firstName;
    @BindView(R.id.rating)
    AppCompatRatingBar rating;
    @BindView(R.id.booking_id)
    TextView bookingId;
    @BindView(R.id.schedule_at)
    TextView scheduleAt;
    @BindView(R.id.lblSource)
    TextView lblSource;
    @BindView(R.id.lblDestination)
    TextView lblDestination;
    @BindView(R.id.payment_mode)
    TextView paymentMode;
    @BindView(R.id.payable)
    TextView payable;
    @BindView(R.id.upcoming_payment)
    ImageView upcomingPayment;

    private UpcomingTripDetailPresenter presenter = new UpcomingTripDetailPresenter();

    @Override
    public int getLayoutId() {
        return R.layout.activity_upcoming_trip_detail;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        presenter.attachView(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.upcoming_trip_details));
        if (DATUM_history != null) {
            presenter.getUpcomingDetail(String.valueOf(DATUM_history.getId()));
        }
    }


    void initPayment(String mode) {

        switch (mode) {
            case Constants.PaymentMode.CASH:
                paymentMode.setText(getString(R.string.cash));
                upcomingPayment.setImageResource(R.drawable.ic_money);
                // paymentMode.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_money, 0, 0, 0);
                break;
            case Constants.PaymentMode.CARD:
                paymentMode.setText(getString(R.string.card));
                upcomingPayment.setImageResource(R.drawable.ic_card);
                //  paymentMode.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_card, 0, 0, 0);
                break;
            case Constants.PaymentMode.PAYPAL:
                paymentMode.setText(getString(R.string.paypal));
                //  paymentMode.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_paypal, 0, 0, 0);
                break;
            case Constants.PaymentMode.WALLET:
                paymentMode.setText(getString(R.string.wallet));
                //  paymentMode.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_wallet, 0, 0, 0);
                break;
            default:
                break;
        }
    }


    @OnClick({R.id.cancel, R.id.call})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.cancel:
                SharedHelper.putKey(getBaseContext(), Constants.SharedPref.cancel_id, String.valueOf(DATUM_history.getId()));
                cancelRequestPopup();
                break;
            case R.id.call:
                //callTask();
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + DATUM_history_detail.getUser().getMobile()));
                startActivity(intent);

                break;
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //do whatever
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onSuccess(HistoryDetail historyDetail) {

        BaseActivity.DATUM_history_detail = historyDetail;

        bookingId.setText(historyDetail.getBookingId());
        scheduleAt.setText(historyDetail.getScheduleAt());
        lblSource.setText(historyDetail.getSAddress());
        lblDestination.setText(historyDetail.getDAddress());
        Glide.with(activity()).load(historyDetail.getStaticMap()).
                apply(RequestOptions.placeholderOf(R.drawable.ic_launcher_background).dontAnimate().
                        error(R.drawable.ic_launcher_background)).into(staticMap);
        initPayment(historyDetail.getPaymentMode());
        User_Past user = historyDetail.getUser();
        if (user != null) {
            firstName.setText(user.getFirstName());
            Glide.with(activity()).load(BuildConfig.BASE_IMAGE_URL +
                    user.getPicture()).apply(RequestOptions.placeholderOf(R.drawable.ic_user_placeholder).
                    dontAnimate().error(R.drawable.ic_user_placeholder)).into(avatar);


        }

        User_Past ratingValue = historyDetail.getUser();
        if (ratingValue.getRating() != null) {
            rating.setRating(Float.parseFloat(String.valueOf(ratingValue.getRating())));
        } else {
            rating.setRating(0);
        }
    }

    @Override
    public void onError(Throwable e) {
        hideLoading();
        if (e != null)
            onErrorBase(e);
    }


    @SuppressLint("MissingPermission")
    void makeCall(String number) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + number));
        startActivity(intent);
    }


    void cancelRequestPopup() {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity());
        // set dialog message
        alertDialogBuilder
                .setMessage(getString(R.string.cancel_request_title))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.yes), (dialog, id) -> {
                    CancelDialogFragment cancelDialogFragment = new CancelDialogFragment();
                    cancelDialogFragment.show(getSupportFragmentManager(), cancelDialogFragment.getTag());
                }).setNegativeButton(getString(R.string.no), (dialog, id) -> dialog.cancel());

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }
}
