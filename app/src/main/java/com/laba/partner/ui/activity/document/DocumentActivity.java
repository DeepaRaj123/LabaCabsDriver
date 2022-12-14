package com.laba.partner.ui.activity.document;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.laba.partner.BuildConfig;
import com.laba.partner.R;
import com.laba.partner.base.BaseActivity;
import com.laba.partner.common.Constants;
import com.laba.partner.common.SharedHelper;
import com.laba.partner.common.Utilities;
import com.laba.partner.data.network.model.Document;
import com.laba.partner.data.network.model.DriverDocumentResponse;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
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

public class DocumentActivity extends BaseActivity implements DocumentIView, EasyPermissions.PermissionCallbacks {

    public static final int ASK_MULTIPLE_PERMISSION_REQUEST_CODE = 123;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.rvDocuments)
    RecyclerView rvDocuments;
    @BindView(R.id.tvNoDocument)
    TextView tvNoDocument;

    @BindView(R.id.btProceed)
    Button btSave;
    private DocumentPresenter presenter = new DocumentPresenter();
    private List<Document> documents;
    private DocumentAdapter mAdapter;
    private boolean isFromSettings = false;
    private String isFromMainPage = "";
    private int adapterPos;
    private String setting;
    private String documentID = "0";
    private boolean isDocumentUploaded;

    @Override
    public int getLayoutId() {
        return R.layout.activity_document;
    }

    @Override
    public void initView() {

        isDocumentUploaded = getIntent()
                .getBooleanExtra("documentUploaded", true);
        /*if(isDocumentUploaded)
            btSave.setVisibility(View.GONE);*/

        ButterKnife.bind(this);
        presenter.attachView(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.walletTransactions));
        try {
            setting = getIntent().getStringExtra("setting");
            isFromSettings = getIntent().getExtras().getBoolean("isFromSettings");
            isFromMainPage = getIntent().getExtras().getString("isFromMainPage");
        } catch (Exception e) {
            e.printStackTrace();
            isFromSettings = false;
        }
        documents = new ArrayList<>();
        showLoading();
        presenter.getDocuments();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (setting != null && !setting.equalsIgnoreCase("") && setting.equalsIgnoreCase("isClick")) {
                    onBackPressed();
                } else if (isFromMainPage != null && !isFromMainPage.equalsIgnoreCase("") && isFromMainPage.equalsIgnoreCase("YES")) {
                    onBackPressed();
                } else {
//                    showPopUp();
                    onBackPressed();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void showPopUp() {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(DocumentActivity.this);

        alertDialogBuilder
                .setMessage(getString(R.string.log_out_title))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.ok), (dialog, id) -> {
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("id", SharedHelper.getKey(activity(),
                            Constants.SharedPref.user_id) + "");
                    presenter.logout(map);
                }).setNegativeButton(getString(R.string.cancel), (dialog, id) -> {
            String user_id = SharedHelper.getKey(activity(), Constants.SharedPref.user_id);
            Utilities.printV("user_id===>", user_id);
            dialog.cancel();
        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    @OnClick(R.id.btProceed)
    public void onViewClicked() {
        showLoading();
        if (isFromSettings) {
            boolean canHitApi = false;
            for (Document document : documents)
                if (!TextUtils.isEmpty(document.getIdVal()) || document.getImgFile() != null)
                    canHitApi = true;
            if (canHitApi) {
                Map<String, RequestBody> params = new HashMap<>();
                List<MultipartBody.Part> file = new ArrayList<>();
                for (int i = 0; i < documents.size(); i++)
                    if (documents.get(i).getImgFile() != null) {
                        params.put("id[" + params.size() + "]", toRequestBody(String.valueOf(documents.get(i).getId())));
                        file.add(MultipartBody.Part.createFormData("document[" + file.size() + "]", documents.get(i).getImgFile().getName(),
                                RequestBody.create(MediaType.parse("image/*"), documents.get(i).getImgFile())));
                    }
                presenter.postUploadDocuments(params, file);
            } else {
                hideLoading();
                Toasty.error(this, getString(R.string.no_document_changed), Toast.LENGTH_SHORT, true).show();
            }
        } else {
            boolean canHitApi = true;
            for (Document document : documents)
                if (TextUtils.isEmpty(document.getIdVal()) || document.getImgFile() == null)
                    canHitApi = false;

            if (canHitApi) {
                Map<String, RequestBody> params = new HashMap<>();
                List<MultipartBody.Part> file = new ArrayList<>();
                for (int i = 0; i < documents.size(); i++)
                    if (documents.get(adapterPos).getImgFile() != null) {
                        params.put("id[" + i + "]", toRequestBody(String.valueOf(documents.get(i).getId())));
                        if (documents.get(i).getImgFile() != null) {
                            file.add(MultipartBody.Part.createFormData("document[" + i + "]",
                                    documents.get(i).getImgFile().getName(),
                                    RequestBody.create(MediaType.parse("image/*"), documents.get(i).getImgFile())));
                        }
                    }
                presenter.postUploadDocuments(params, file);
            } else {
                hideLoading();
                Toasty.error(this, getString(R.string.add_all_documents), Toast.LENGTH_SHORT, true).show();
            }
        }
    }

    @Override
    public void onSuccess(DriverDocumentResponse response) {
        if (isDocumentUploaded)
            btSave.setVisibility(View.GONE);

        documents = response.getDocuments();
        mAdapter = new DocumentAdapter(documents);
        if (documents.size() > 0) {
            rvDocuments.setLayoutManager(new LinearLayoutManager(activity(), LinearLayoutManager.VERTICAL, false));
            rvDocuments.setAdapter(mAdapter);
            tvNoDocument.setVisibility(View.INVISIBLE);
            rvDocuments.setVisibility(View.VISIBLE);
        } else {
            tvNoDocument.setVisibility(View.VISIBLE);
            rvDocuments.setVisibility(View.INVISIBLE);
        }
        hideLoading();
    }

    @Override
    public void onDocumentSuccess(DriverDocumentResponse response) {
        /*if (!isFromSettings) finish();
        else {
            documents = response.getDocuments();
            mAdapter.setmDocuments(documents);
            mAdapter.notifyDataSetChanged();
        }*/

        documents = response.getDocuments();
        mAdapter.setmDocuments(documents);
        mAdapter.notifyDataSetChanged();

        hideLoading();
    }

    @AfterPermissionGranted(RC_MULTIPLE_PERMISSION_CODE)
    public void MultiplePermissionTask(int pos) {
        this.adapterPos = pos;
        if (hasMultiplePermission()) pickImage();
        else EasyPermissions.requestPermissions(
                this, getString(R.string.please_accept_permission),
                RC_MULTIPLE_PERMISSION_CODE,
                MULTIPLE_PERMISSION);
    }

    private boolean hasMultiplePermission() {
        return EasyPermissions.hasPermissions(this, MULTIPLE_PERMISSION);
    }


    @Override
    public void onError(Throwable e) {
        hideLoading();
        if (e != null)
            onErrorBase(e);
    }

    @Override
    public void onSuccessLogout(Object object) {
        Utilities.LogoutApp(activity(), "");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            EasyImage.handleActivityResult(requestCode, resultCode, data, DocumentActivity.this,
                    new DefaultCallback() {
                        @Override
                        public void onImagesPicked(@NonNull List<File> imageFiles,
                                                   EasyImage.ImageSource source, int type) {
                            setImageToView(imageFiles.get(0));
                        }
                    });
        } catch (Exception e) {
            Toasty.error(this, getString(R.string.invalid_img_file), Toast.LENGTH_SHORT, true).show();
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (permissions.length == 0) return;
        boolean allPermissionsGranted = true;
        if (grantResults.length > 0) for (int grantResult : grantResults)
            if (grantResult != PackageManager.PERMISSION_GRANTED) {
                allPermissionsGranted = false;
                break;
            }
        if (!allPermissionsGranted) {
            boolean somePermissionsForeverDenied = false;
            for (String permission : permissions) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                    //denied
                    Log.e("denied", permission);
                } else {
                    if (ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
                        //allowed
                        Log.e("allowed", permission);
                    } else {
                        //set to never ask again
                        Log.e("set to never ask again", permission);
                        somePermissionsForeverDenied = true;
                    }
                }
            }
            if (somePermissionsForeverDenied) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder
                        .setTitle("Permissions Required")
                        .setMessage("You have forcefully denied some of the required permissions " +
                                "for this action. Please open settings, go to permissions and allow them.")
                        .setPositiveButton("Settings", (dialog, which) -> {
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    Uri.fromParts("package", getPackageName(), null));
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> {
                        })
                        .setCancelable(false)
                        .create()
                        .show();
            }
        } else {
            switch (requestCode) {
                case ASK_MULTIPLE_PERMISSION_REQUEST_CODE:
                    if (grantResults.length > 0) {
                        boolean permission1 = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                        boolean permission2 = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                        if (permission1 && permission2) pickImage();
                        else
                            Toast.makeText(getApplicationContext(), "Please give permission", Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    }


    private void setImageToView(@NonNull File file) {
        try {
            File compressedImageFile = new Compressor(this).compressToFile(file);
            documents.get(adapterPos).setImgFile(compressedImageFile);
            mAdapter.setmDocuments(documents);
            documents.get(adapterPos).setIdVal("id[" + adapterPos + "]");
            mAdapter.notifyItemChanged(adapterPos);

//            if(isDocumentUploaded){
            if (true) {

                RequestBody id =
                        RequestBody.create(MediaType.parse("text/plain"), documentID + "");

                RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"),
                        compressedImageFile);

                MultipartBody.Part multipartBody = MultipartBody.Part.createFormData(
                        "document", file.getName(), requestFile);

                presenter.postUploadSingleDocuments(id, multipartBody);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {

    }

    public long getTimeDifference(Date start_date, String end_date) {

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        try {

            Date d1 = start_date;
            Date d2 = sdf.parse(end_date);

            // Calucalte time difference
            // in milliseconds
            long difference_In_Time
                    = d2.getTime() - d1.getTime();

            // Calucalte time difference in
            // seconds, minutes, hours, years,
            // and days
            long difference_In_Seconds
                    = (difference_In_Time
                    / 1000)
                    % 60;

            long difference_In_Minutes
                    = (difference_In_Time
                    / (1000 * 60))
                    % 60;

            long difference_In_Hours
                    = (difference_In_Time
                    / (1000 * 60 * 60))
                    % 24;

            long difference_In_Years
                    = (difference_In_Time
                    / (1000l * 60 * 60 * 24 * 365));
            if (difference_In_Years > 0) {
                return 100;
            }

            long difference_In_Days
                    = (difference_In_Time
                    / (1000 * 60 * 60 * 24))
                    % 365;

            return difference_In_Days;
            /*if(difference_In_Years != 0){
                return false;
            }
            else if(difference_In_Years == 0){
                if(difference_In_Days < 0){
                    return true;
                }
                else return false;
            }
            else return false;*/

        }

        // Catch the Exception
        catch (ParseException e) {
            e.printStackTrace();
            return 100;
        }
    }

    /*public boolean isExpired(Date start_date, String end_date) {

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        try {

            Date d1 = start_date;
            Date d2 = sdf.parse(end_date);

            // Calucalte time difference
            // in milliseconds
            long difference_In_Time
                    = d2.getTime() - d1.getTime();

            // Calucalte time difference in
            // seconds, minutes, hours, years,
            // and days
            long difference_In_Seconds
                    = (difference_In_Time
                    / 1000)
                    % 60;

            long difference_In_Minutes
                    = (difference_In_Time
                    / (1000 * 60))
                    % 60;

            long difference_In_Hours
                    = (difference_In_Time
                    / (1000 * 60 * 60))
                    % 24;

            long difference_In_Years
                    = (difference_In_Time
                    / (1000l * 60 * 60 * 24 * 365));

            long difference_In_Days
                    = (difference_In_Time
                    / (1000 * 60 * 60 * 24))
                    % 365;

            if(difference_In_Years != 0){
                return false;
            }
            else if(difference_In_Years == 0){
                if(difference_In_Days < 0){
                    return true;
                }
                else return false;
            }
            else return false;

        }

        // Catch the Exception
        catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }*/

    public class DocumentAdapter extends RecyclerView.Adapter<DocumentAdapter.MyViewHolder> {

        private List<Document> mDocuments;
        private Context mContext;

        DocumentAdapter(List<Document> documents) {
            this.mDocuments = documents;
        }

        void setmDocuments(List<Document> mDocuments) {
            this.mDocuments = mDocuments;
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            mContext = parent.getContext();
            return new MyViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_document, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

            /**
             * if doc required is 'YES'
             *  -- the document is either expired or not uploaded
             *
             *
             * */

            holder.btAddDocument.setVisibility(View.VISIBLE);

            holder.tvDocTitle.setText(mDocuments.get(position).getName());
            if (mDocuments.get(position).getImgFile() != null) {

                Glide.with(activity())
                        .load(Uri.fromFile(mDocuments.get(position).getImgFile()))
                        .apply(RequestOptions.placeholderOf(R.drawable.ic_document_placeholder).
                                diskCacheStrategy(DiskCacheStrategy.NONE)
                                .skipMemoryCache(true)
                                .dontAnimate().error(R.drawable.ic_document_placeholder))
                        .into(holder.ivDocument);
                holder.btAddDocument.setText(mContext.getText(R.string.edit));

            } else if (mDocuments.get(position).getProviderDocuments() != null
                    && mDocuments.get(position).getProviderDocuments().getUrl() != null) {
                Glide.with(activity())
                        .load(BuildConfig.BASE_IMAGE_URL + mDocuments.get(position).getProviderDocuments().getUrl())
                        .apply(RequestOptions.placeholderOf(R.drawable.ic_document_placeholder).diskCacheStrategy(DiskCacheStrategy.NONE)
                                .skipMemoryCache(true)
                                .dontAnimate().error(R.drawable.ic_document_placeholder))
                        .into(holder.ivDocument);
                holder.btAddDocument.setText(mContext.getText(R.string.edit));

                String server_image_url = mDocuments.get(position).getProviderDocuments().getUrl();
                String base_url = BuildConfig.BASE_IMAGE_URL;
            }

            Document document = mDocuments.get(position);


            if (document.getProviderDocuments() != null) {
                String status = document.getProviderDocuments().getStatus();
                String expires_at = document.getProviderDocuments().getExpiresAtText();

                switch (status) {
                    case "EXPIRED":
                        int redColor = getResources().getColor(R.color.red);
                        holder.tvExpiryDate.setTextColor(redColor);
                        holder.btAddDocument.setVisibility(View.VISIBLE);
                        break;
                    case "ACTIVE":
                        int greenColor = getResources().getColor(R.color.green);
                        holder.tvExpiryDate.setTextColor(greenColor);
                        holder.btAddDocument.setVisibility(View.GONE);
                        break;
                    case "ASSESSING":
                        int yellowColor = getResources().getColor(R.color.yellow);
                        holder.tvExpiryDate.setTextColor(yellowColor);
                        holder.btAddDocument.setVisibility(View.VISIBLE);
                        break;
                    default:
                        int blackColor = getResources().getColor(R.color.black);
                        holder.tvExpiryDate.setTextColor(blackColor);
                        holder.btAddDocument.setVisibility(View.VISIBLE);
                }

                if (expires_at != null) {
                    if (status.equalsIgnoreCase("ASSESSING")) {
                        holder.tvExpiryDate.setText(status);
                        return;
                    }

                    holder.tvExpiryDate.setText(expires_at + "\n" + status);

                    Date todayDate = new Date();
                    long difference = getTimeDifference(todayDate, expires_at);
                    if (difference < 7) {
                        holder.btAddDocument.setVisibility(View.VISIBLE);
                    }

                    if (difference < 0) {
                        holder.btAddDocument.setVisibility(View.VISIBLE);

                        int redColor = getResources().getColor(R.color.red);
                        holder.tvExpiryDate.setTextColor(redColor);
                        holder.tvExpiryDate.setText(expires_at + "\n" + "EXPIRED");
                    }
                } else {
                    holder.tvExpiryDate.setText(status);
                }
            }

        }

        @Override
        public int getItemCount() {
            return mDocuments.size();
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        class MyViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.tvDocTitle)
            TextView tvDocTitle;

            @BindView(R.id.expiry_date_label)
            TextView tvExpiryDate;

            @BindView(R.id.ivDocument)
            ImageView ivDocument;
            @BindView(R.id.btAddDocument)
            Button btAddDocument;

            MyViewHolder(View view) {
                super(view);
                ButterKnife.bind(this, itemView);
            }

            @OnClick(R.id.btAddDocument)
            public void onViewClicked() {
                int position = getAdapterPosition();
                Document document = mDocuments.get(position);
//                documentID = document.getProviderDocuments().getDocumentId();
                documentID = document.getId() + "";
//                Toast.makeText(getApplicationContext(),documentID+"",Toast.LENGTH_SHORT).show();
                MultiplePermissionTask(getAdapterPosition());
            }
        }
    }

}



