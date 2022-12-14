package com.laba.partner.base;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.laba.partner.R;
import com.laba.partner.data.network.model.User;


public abstract class BaseBottomSheetDialogFragment extends BottomSheetDialogFragment implements MvpView {


    public Message message = new Message();
    View view;
    ProgressDialog progressDialog;

    public abstract int getLayoutId();

    public abstract void initView(View view);


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable
            Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        if (view == null) {
            view = inflater.inflate(getLayoutId(), container, false);
            initView(view);
        }

        progressDialog = new ProgressDialog(activity());
        progressDialog.setMessage(getString(R.string.please_wait));
        progressDialog.setCancelable(false);

        return view;
    }


    @Override
    public Activity activity() {
        return getActivity();
    }

    @Override
    public void showLoading() {
        progressDialog.show();
    }

    @Override
    public void hideLoading() {
        progressDialog.dismiss();
    }

    @Override
    public void onSuccessRefreshToken(User user) {
        ((BaseActivity) getActivity()).onSuccessRefreshToken(user);
    }

    @Override
    public void onErrorRefreshToken(Throwable throwable) {
        ((BaseActivity) getActivity()).onErrorRefreshToken(throwable);
    }

    public void onErrorBase(Throwable e) {
        ((BaseActivity) getActivity()).onErrorBase(e);
    }

    @Override
    public void onSuccessLogout(Object object) {
        ((BaseActivity) getActivity()).onSuccessLogout(object);
    }

    @Override
    public void onError(Throwable throwable) {
        ((BaseActivity) getActivity()).onError(throwable);
    }
}
