package com.laba.partner.ui.activity.profile;

import android.content.Intent;
import android.net.Uri;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.laba.partner.BuildConfig;
import com.laba.partner.MvpApplication;
import com.laba.partner.R;
import com.laba.partner.base.BaseActivity;
import com.laba.partner.common.Constants;
import com.laba.partner.common.SharedHelper;
import com.laba.partner.common.Utilities;
import com.laba.partner.data.network.model.UserResponse;
import com.laba.partner.ui.activity.change_password.ChangePasswordActivtiy;
import com.laba.partner.ui.activity.main.MainActivity;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;
import id.zelory.compressor.Compressor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static com.laba.partner.common.Constants.MULTIPLE_PERMISSION;
import static com.laba.partner.common.Constants.RC_MULTIPLE_PERMISSION_CODE;

public class ProfileActivity extends BaseActivity implements ProfileIView {

    ProfilePresenter presenter = new ProfilePresenter();
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.imgProfile)
    CircleImageView imgProfile;
    @BindView(R.id.txtFirstName)
    EditText txtFirstName;
    @BindView(R.id.txtLastName)
    EditText txtLastName;
    @BindView(R.id.txtPhoneNumber)
    EditText txtPhoneNumber;
    @BindView(R.id.txtEmail)
    EditText txtEmail;
    @BindView(R.id.txtService)
    EditText txtService;
    @BindView(R.id.btnSave)
    Button btnSave;
    @BindView(R.id.lblChangePassword)
    TextView lblChangePassword;

    File imgFile = null;

    @Override
    public int getLayoutId() {
        return R.layout.activity_profile;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        presenter.attachView(this);
        showLoading();
        presenter.getProfile();
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.profile));
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

    @OnClick({R.id.btnSave, R.id.lblChangePassword, R.id.imgProfile})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btnSave:
                profileUpdate();
                break;
            case R.id.lblChangePassword:
                startActivity(new Intent(ProfileActivity.this, ChangePasswordActivtiy.class));
                break;
            case R.id.imgProfile:
                MultiplePermissionTask();
                break;
        }
    }

    void profileUpdate() {

        //All the String parameters, you have to put like
        Map<String, RequestBody> map = new HashMap<>();

        map.put("first_name", toRequestBody(txtFirstName.getText().toString()));
        map.put("last_name", toRequestBody(txtLastName.getText().toString()));
        map.put("email", toRequestBody(txtEmail.getText().toString()));
        map.put("mobile", toRequestBody(txtPhoneNumber.getText().toString()));

        MultipartBody.Part filePart = null;
        if (imgFile != null)
            try {
                File compressedImageFile = new Compressor(this).compressToFile(imgFile);
                filePart = MultipartBody.Part.createFormData("avatar", compressedImageFile.getName(),
                        RequestBody.create(MediaType.parse("image*//*"), compressedImageFile));
            } catch (IOException e) {
                e.printStackTrace();
            }


        Utilities.printV("Params ===> 2", map.toString());
        showLoading();
        presenter.profileUpdate(map, filePart);
    }

    @Override
    public void onSuccess(UserResponse user) {

        hideLoading();
        try {
            Utilities.printV("User===>", user.getFirstName() + user.getLastName());
            Utilities.printV("TOKEN===>", SharedHelper.getKey(MvpApplication.getInstance(),
                    Constants.SharedPref.access_token, ""));

            String loginBy = user.getLoginBy();
            if (loginBy != null) {
                if (loginBy.equalsIgnoreCase("facebook") || loginBy.equalsIgnoreCase("google"))
                    lblChangePassword.setVisibility(View.INVISIBLE);
                else lblChangePassword.setVisibility(View.VISIBLE);
            }
            txtFirstName.setText(user.getFirstName());
            txtLastName.setText(user.getLastName());
            txtPhoneNumber.setText(String.valueOf(user.getMobile()));
            txtEmail.setText(user.getEmail());
            SharedHelper.putKey(this, "stripe_publishable_key", user.getStripePublishableKey());
            if (user.getService() != null)
                txtService.setText((user.getService().getServiceType() != null)
                        ? user.getService().getServiceType().getName() : "");
            Glide.with(activity())
                    .load(BuildConfig.BASE_IMAGE_URL + user.getAvatar())
                    .apply(RequestOptions
                            .placeholderOf(R.drawable.ic_user_placeholder)
                            .dontAnimate()
                            .error(R.drawable.ic_user_placeholder))
                    .into(imgProfile);
        } catch (Exception e) {
            e.getMessage();
        }
    }

    @Override
    public void onSuccessUpdate(UserResponse object) {
        hideLoading();
        Toasty.success(this, getString(R.string.profile_updated_success), Toast.LENGTH_SHORT, true).show();
        Intent profileIntent = new Intent(this, MainActivity.class);
        profileIntent.putExtra("avartar", BuildConfig.BASE_IMAGE_URL + object.getAvatar());
        startActivity(profileIntent);
    }

    @Override
    public void onError(Throwable e) {
        hideLoading();
        if (e != null)
            onErrorBase(e);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        EasyImage.handleActivityResult(requestCode, resultCode, data, ProfileActivity.this,
                new DefaultCallback() {
                    @Override
                    public void onImagesPicked(@NonNull List<File> imageFiles,
                                               EasyImage.ImageSource source, int type) {
                        imgFile = imageFiles.get(0);
                        Glide.with(activity()).load(Uri.fromFile(imgFile)).
                                apply(RequestOptions.placeholderOf(R.drawable.ic_user_placeholder).dontAnimate().
                                        error(R.drawable.ic_user_placeholder)).into(imgProfile);
                    }
                });

    }

    private boolean hasMultiplePermission() {
        return EasyPermissions.hasPermissions(this, MULTIPLE_PERMISSION);
    }

    @AfterPermissionGranted(RC_MULTIPLE_PERMISSION_CODE)
    void MultiplePermissionTask() {
        if (hasMultiplePermission()) pickImage();
        else EasyPermissions.requestPermissions(
                this, getString(R.string.please_accept_permission),
                RC_MULTIPLE_PERMISSION_CODE,
                MULTIPLE_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }
}
