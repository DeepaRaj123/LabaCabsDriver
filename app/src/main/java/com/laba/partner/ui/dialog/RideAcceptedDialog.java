package com.laba.partner.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.telecom.Call;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDialog;

import com.laba.partner.R;

public class RideAcceptedDialog extends AppCompatDialog {

    private Context context;
    private Callback callback;

    public RideAcceptedDialog(@NonNull Context context, Callback callback) {
        super(context);
        this.context = context;
        this.callback = callback;
        setContentView(R.layout.dialog_ride_accepted);

        setCancelable(false);


        findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        findViewById(R.id.textView7).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                if(callback != null) callback.showRideDetails();
            }
        });
    }

    public interface Callback{
        void showRideDetails();
    }

}
