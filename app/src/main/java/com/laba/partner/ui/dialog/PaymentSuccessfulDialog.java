package com.laba.partner.ui.dialog;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDialog;

import com.laba.partner.R;

public class PaymentSuccessfulDialog extends AppCompatDialog {

    private Context context;

    public PaymentSuccessfulDialog(@NonNull Context context) {
        super(context);
        this.context = context;
        setContentView(R.layout.dialog_payment_successful);

        setCancelable(false);


        findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

    }

}
