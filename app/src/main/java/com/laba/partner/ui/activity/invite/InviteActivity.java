package com.laba.partner.ui.activity.invite;

import android.view.MenuItem;
import android.widget.Button;

import androidx.appcompat.widget.Toolbar;

import com.laba.partner.R;
import com.laba.partner.base.BaseActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

public class InviteActivity extends BaseActivity implements InviteIView {


    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.call)
    Button call;

    InvitePresenter presenter = new InvitePresenter();

    @Override
    public int getLayoutId() {
        return R.layout.activity_invite;
    }

    @Override
    public void initView() {

        ButterKnife.bind(this);
        presenter.attachView(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


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


}
