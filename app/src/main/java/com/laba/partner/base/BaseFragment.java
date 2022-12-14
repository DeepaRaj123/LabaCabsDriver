package com.laba.partner.base;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.laba.partner.R;
import com.laba.partner.data.network.model.User;

import java.util.Objects;

public abstract class BaseFragment extends Fragment implements MvpView {

    protected View view;
    protected ProgressDialog progressDialog;

    public abstract int getLayoutId();

    public abstract View initView(View view);

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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
    public FragmentActivity activity() {
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

    public void onErrorBase(Throwable e) throws Exception {
        ((BaseActivity) Objects.requireNonNull(getActivity())).onErrorBase(e);
    }

    @Override
    public void onSuccessRefreshToken(User user) {
        ((BaseActivity) getActivity()).onSuccessRefreshToken(user);
    }

    @Override
    public void onErrorRefreshToken(Throwable throwable) {
        ((BaseActivity) getActivity()).onErrorRefreshToken(throwable);
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
