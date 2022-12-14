package com.laba.partner.ui.bottomsheetdialog.rating;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.laba.partner.BuildConfig;
import com.laba.partner.R;
import com.laba.partner.base.BaseFragment;
import com.laba.partner.common.Utilities;
import com.laba.partner.data.network.model.Rating;
import com.laba.partner.data.network.model.Request_;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static com.laba.partner.base.BaseActivity.DATUM;

public class RatingDialogFragment extends BaseFragment implements RatingDialogIView {

    @BindView(R.id.rate_with_txt)
    TextView rateWithTxt;
    @BindView(R.id.user_img)
    ImageView userImg;
    @BindView(R.id.user_rating)
    RatingBar userRating;
    @BindView(R.id.comments)
    EditText comments;
    @BindView(R.id.btnSubmit)
    Button btnSubmit;
    Unbinder unbinder;

    RatingDialogPresenter presenter;

    @Override
    public int getLayoutId() {
        return R.layout.fragment_rating;
    }

    @Override
    public View initView(View view) {
        unbinder = ButterKnife.bind(this, view);
        // setCancelable(false);
        presenter = new RatingDialogPresenter();
        presenter.attachView(this);
        init();
        return view;
    }

    @SuppressLint("SetTextI18n")
    private void init() {

        Request_ data = DATUM;

        try {
            rateWithTxt.setText(getString(R.string.rate_your_trip) + " " +
                    data.getUser().getFirstName() + " " + data.getUser().getLastName());
            userRating.setRating(Float.parseFloat(data.getUser().getRating()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (data.getUser().getPicture() != null)
                Glide.with(activity()).load(BuildConfig.BASE_IMAGE_URL +
                        data.getUser().getPicture()).
                        apply(RequestOptions.placeholderOf(R.drawable.ic_user_placeholder).
                                dontAnimate().error(R.drawable.ic_user_placeholder)).into(userImg);
            else
                Glide.with(activity()).load(R.drawable.ic_user_placeholder).into(userImg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnClick(R.id.btnSubmit)
    public void onViewClicked() {
        if (DATUM != null) {
            Request_ datum = DATUM;
            HashMap<String, Object> map = new HashMap<>();
            map.put("rating", Math.round(userRating.getRating()));
            map.put("comment", Utilities.getEncodeMessage(comments.getText().toString()));
            showLoading();
            presenter.rate(map, datum.getId());
        }
    }

    @Override
    public void onSuccess(Rating rating) {
        hideLoading();
        Toast.makeText(activity(), getString(R.string.ride_complete_successfull), Toast.LENGTH_SHORT).show();
        activity().sendBroadcast(new Intent("INTENT_FILTER"));
    }

    @Override
    public void onError(Throwable e) {
        hideLoading();
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        if (e != null) try {
            onErrorBase(e);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    /*@Override
    public void initView(View view) {
        unbinder = ButterKnife.bind(this, view);
        setCancelable(false);
        presenter = new RatingDialogPresenter();
        presenter.attachView(this);
        init();

    }

    private void init() {

        Request_ data = DATUM;

        rateWithTxt.setText(getString(R.string.rate_your_trip)+ " " +
                data.getUser().getFirstName() + " " + data.getUser().getLastName());
        userRating.setRating(Float.parseFloat(data.getUser().getRating()));

        if (data.getUser().getPicture() != null)
            Glide.with(activity()).load(BuildConfig.BASE_IMAGE_URL +
                    data.getUser().getPicture()).
                    apply(RequestOptions.placeholderOf(R.drawable.ic_user_placeholder).
                    dontAnimate().error(R.drawable.ic_user_placeholder)).into(userImg);
    }

    @OnClick(R.id.btnSubmit)
    public void onViewClicked() {
        if (DATUM != null) {
            Request_ datum = DATUM;
            HashMap<String, Object> map = new HashMap<>();
            map.put("rating", Math.round(userRating.getRating()));
            map.put("comment", comments.getText().toString());
            showLoading();
            presenter.rate(map, datum.getId());

        }
    }

    @Override
    public void onSuccess(Rating rating) {
        dismissAllowingStateLoss();
        hideLoading();
        activity().sendBroadcast(new Intent("INTENT_FILTER"));
    }

    @Override
    public void onError(Throwable e) {

    }*/
}
