package com.laba.partner.ui.activity.setting;

import android.content.Intent;
import android.view.MenuItem;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.appcompat.widget.Toolbar;

import com.laba.partner.R;
import com.laba.partner.base.BaseActivity;
import com.laba.partner.common.Constants;
import com.laba.partner.common.LocaleHelper;
import com.laba.partner.ui.activity.document.DocumentActivity;
import com.laba.partner.ui.activity.main.MainActivity;


import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class SettingsActivity extends BaseActivity implements SettingsIView {

    @BindView(R.id.english)
    RadioButton english;
    @BindView(R.id.arabic)
    RadioButton arabicRb;
    @BindView(R.id.french)
    RadioButton frenchRb;
    @BindView(R.id.choose_language)
    RadioGroup chooseLanguage;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    private String setting;

    private SettingsPresenter presenter = new SettingsPresenter();
    private String language;

    @Override
    public int getLayoutId() {
        return R.layout.activity_settings;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        presenter.attachView(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setting = getIntent().getStringExtra("setting");

        getSupportActionBar().setTitle(getString(R.string.settings));
        languageReset();

        chooseLanguage.setOnCheckedChangeListener((group, checkedId) -> {
            showLoading();
            switch (checkedId) {
                case R.id.english:
                    language = Constants.LANGUAGE_ENGLISH;
                    break;
                case R.id.french:
                    language = Constants.LANGUAGE_FRENCH;
                    break;
                case R.id.arabic:
                    language = Constants.LANGUAGE_ARABIC;
                    break;
                default:
                    language = Constants.LANGUAGE_ARABIC;

                    break;
            }
            presenter.changeLanguage(language);
        });
    }

    private void languageReset() {
        String dd = LocaleHelper.getLanguage(this);
        switch (dd) {
            case Constants.LANGUAGE_ENGLISH:
                english.setChecked(true);
                break;
            case Constants.LANGUAGE_ARABIC:
                arabicRb.setChecked(true);
                break;
            case Constants.LANGUAGE_FRENCH:
                frenchRb.setChecked(true);
                break;
            default:
                arabicRb.setChecked(true);
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onSuccess(Object o) {
        hideLoading();
        LocaleHelper.setLocale(getApplicationContext(), language);
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK |
                Intent.FLAG_ACTIVITY_NEW_TASK));
        this.overridePendingTransition(R.anim.rotate_in, R.anim.rotate_out);
    }

    @Override
    public void onError(Throwable e) {
        hideLoading();
        if (e != null)
            onErrorBase(e);
    }

    @OnClick(R.id.tvChangeDoc)
    public void onViewClicked() {
        Intent intent = new Intent(this, DocumentActivity.class);
        intent.putExtra("isFromSettings", true);
        intent.putExtra("setting", setting);
        startActivity(intent);
    }
}
