package com.laba.partner.ui.activity.email;

import android.content.Intent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.laba.partner.R;
import com.laba.partner.base.BaseActivity;
import com.laba.partner.common.Constants;
import com.laba.partner.common.SharedHelper;
import com.laba.partner.ui.activity.password.PasswordActivity;
import com.laba.partner.ui.activity.regsiter.RegisterActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import es.dmoral.toasty.Toasty;

public class EmailActivity extends BaseActivity implements EmailIView {

    @BindView(R.id.email)
    TextInputEditText email;
    @BindView(R.id.sign_up)
    TextView signUp;
    @BindView(R.id.next)
    FloatingActionButton next;

    EmailIPresenter presenter = new EmailPresenter();

    @Override
    public int getLayoutId() {
        return R.layout.activity_email;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        presenter.attachView(this);
    }

    @OnClick({R.id.back, R.id.sign_up, R.id.next})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.back:
                activity().onBackPressed();
                break;
            case R.id.sign_up:
                startActivity(new Intent(this, RegisterActivity.class));
                break;
            case R.id.next:
                if (email.getText().toString().isEmpty()) {
                    Toasty.error(this, getString(R.string.invalid_email), Toast.LENGTH_SHORT, true).show();
                    return;
                }
                Intent i = new Intent(this, PasswordActivity.class);
                i.putExtra(Constants.SharedPref.email, email.getText().toString());
                SharedHelper.putKey(this, Constants.SharedPref.txt_email, email.getText().toString());
                startActivity(i);
                break;
        }
    }
}
