package com.laba.partner.ui.activity.past_detail;

import android.annotation.SuppressLint;
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
import com.laba.partner.MvpApplication;
import com.laba.partner.R;
import com.laba.partner.base.BaseActivity;
import com.laba.partner.common.Constants;
import com.laba.partner.common.Utilities;
import com.laba.partner.data.network.model.HistoryDetail;
import com.laba.partner.data.network.model.Payment;
import com.laba.partner.data.network.model.Rating;
import com.laba.partner.data.network.model.User_Past;
import com.laba.partner.ui.bottomsheetdialog.invoice_show.InvoiceShowDialogFragment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;

public class PastTripDetailActivity extends BaseActivity implements PastTripDetailIView {

    @BindView(R.id.static_map)
    ImageView staticMap;
    @BindView(R.id.avatar)
    CircleImageView avatar;
    @BindView(R.id.first_name)
    TextView firstName;
    @BindView(R.id.rating)
    AppCompatRatingBar ratingBar;
    @BindView(R.id.finished_at)
    TextView finishedAt;
    @BindView(R.id.booking_id)
    TextView bookingId;
    @BindView(R.id.payment_mode)
    TextView paymentMode;
    @BindView(R.id.payable)
    TextView payable;
    @BindView(R.id.user_comment)
    TextView userComment;
    @BindView(R.id.view_receipt)
    Button viewReceipt;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.lblSource)
    TextView lblSource;
    @BindView(R.id.lblDestination)
    TextView lblDestination;
    @BindView(R.id.payment_image)
    ImageView paymentImage;
    @BindView(R.id.finished_at_time)
    TextView finishedAtTime;

    private PastTripDetailPresenter presenter = new PastTripDetailPresenter();

    @Override
    public int getLayoutId() {
        return R.layout.activity_past_trip_detail;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        presenter.attachView(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.past_trip_detail));
        if (DATUM_history != null) {
            Utilities.printV("ID===>", DATUM_history.getId() + "");
            presenter.getPastTripDetail(String.valueOf(DATUM_history.getId()));
        }
    }

    void initPayment(String paymentMode) {

        switch (paymentMode) {
            case Constants.PaymentMode.CASH:
                paymentImage.setImageResource(R.drawable.ic_money);
                this.paymentMode.setText(getString(R.string.cash));
                break;
            case Constants.PaymentMode.CARD:
                paymentImage.setImageResource(R.drawable.ic_card);
                this.paymentMode.setText(getString(R.string.card));
                break;
            case Constants.PaymentMode.PAYPAL:
                this.paymentMode.setText(getString(R.string.paypal));
                //this.paymentMode.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_paypal, 0, 0, 0);
                break;
            case Constants.PaymentMode.WALLET:
                this.paymentMode.setText(getString(R.string.wallet));
                // this.paymentMode.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_wallet, 0, 0, 0);
                break;
            case Constants.PaymentMode.razorpay:
                this.paymentMode.setText(getString(R.string.razorpay));
                this.paymentImage.setImageResource(R.drawable.ic_razorpay);
                break;
            default:
                break;
        }
    }

    @OnClick({R.id.view_receipt})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.view_receipt:
                InvoiceShowDialogFragment invoiceDialogFragment = new InvoiceShowDialogFragment();
                invoiceDialogFragment
                        .show(getSupportFragmentManager(), invoiceDialogFragment.getTag());
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

    @SuppressLint("SetTextI18n")
    @Override
    public void onSuccess(HistoryDetail historyDetail) {

        Utilities.printV("onSuccess==>", historyDetail.getBookingId());
        BaseActivity.DATUM_history_detail = historyDetail;
        bookingId.setText(historyDetail.getBookingId());

        String strCurrentDate = historyDetail.getFinishedAt();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        SimpleDateFormat timeFormat;
        Date newDate = null;
        try {
            newDate = format.parse(strCurrentDate);
            format = new SimpleDateFormat("dd MMM yyyy");
            timeFormat = new SimpleDateFormat("hh:mm a");
            String date = format.format(newDate);
            String time = timeFormat.format(newDate);
            finishedAt.setText(date);
            finishedAtTime.setText(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        lblSource.setText(historyDetail.getSAddress());
        lblDestination.setText(historyDetail.getDAddress());
        Glide.with(activity()).load(historyDetail.getStaticMap())
                .apply(RequestOptions.placeholderOf(R.drawable.ic_launcher_background).dontAnimate()
                        .error(R.drawable.ic_launcher_background)).into(staticMap);

        initPayment(historyDetail.getPaymentMode());


        User_Past user = historyDetail.getUser();
        if (user != null) {
            firstName.setText(user.getFirstName() + " " + user.getLastName());
            Glide.with(activity()).load(BuildConfig.BASE_IMAGE_URL +
                    user.getPicture())
                    .apply(RequestOptions.placeholderOf(R.drawable.ic_user_placeholder).
                            dontAnimate().error(R.drawable.ic_user_placeholder)).into(avatar);
        }

        Payment payment = historyDetail.getPayment();
        if (payment != null) {
            if (payment.getTotal() == 0 || payment.getTotal() == 0.0) {
                payable.setVisibility(View.GONE);
            } else {
                payable.setVisibility(View.VISIBLE);
                payable.setText(Constants.Currency + " " +
                        MvpApplication.getInstance().getNewNumberFormat(payment.getTotal()));
            }
        }

        Utilities.printV("===>", historyDetail.getRating().getUserComment() + "");

        Rating rating = historyDetail.getRating();
        if (rating != null) {
            userComment.setText(Utilities.getDecodeMessage(rating.getUserComment()));
            ratingBar.setRating(Float.parseFloat(String.valueOf(rating.getUserRating())));
        }
    }

    @Override
    public void onError(Throwable e) {
        hideLoading();
        if (e != null)
            onErrorBase(e);
    }
}
