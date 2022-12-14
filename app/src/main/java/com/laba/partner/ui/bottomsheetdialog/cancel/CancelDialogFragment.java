package com.laba.partner.ui.bottomsheetdialog.cancel;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.laba.partner.R;
import com.laba.partner.base.BaseBottomSheetDialogFragment;
import com.laba.partner.common.Constants;
import com.laba.partner.common.SharedHelper;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class CancelDialogFragment extends BaseBottomSheetDialogFragment implements CancelDialogIView {


    @BindView(R.id.txtComments)
    EditText comments;
    @BindView(R.id.btnSubmit)
    Button btnSubmit;
    @BindView(R.id.dismiss)
    Button dismiss;

    Unbinder unbinder;

    CancelDialogPresenter presenter;

    @Override
    public int getLayoutId() {
        return R.layout.fragment_cancel;
    }

    @Override
    public void initView(View view) {
        unbinder = ButterKnife.bind(this, view);
        presenter = new CancelDialogPresenter();
        presenter.attachView(this);

        dismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
    }


    @OnClick({R.id.btnSubmit})
    public void onViewClicked() {
        String comment = comments.getText().toString();
        if (comment.isEmpty()) {
            Toast.makeText(getActivity(), "Please enter valid cancel reason", Toast.LENGTH_SHORT).show();
        } else {
            HashMap<String, Object> map = new HashMap<>();
            map.put("id", SharedHelper.getKey(getContext(), Constants.SharedPref.cancel_id));
            map.put("cancel_reason", comment);
            showLoading();
            presenter.cancelRequest(map);
        }
    }

    @Override
    public void onSuccessCancel(Object object) {
        dismissAllowingStateLoss();
        Toast.makeText(getContext(), getString(R.string.ride_cancel_successfull), Toast.LENGTH_SHORT).show();
        hideLoading();
        activity().sendBroadcast(new Intent("INTENT_FILTER"));
    }

    @Override
    public void onError(Throwable e) {
        hideLoading();
        if (e != null)
            onErrorBase(e);
    }
}
