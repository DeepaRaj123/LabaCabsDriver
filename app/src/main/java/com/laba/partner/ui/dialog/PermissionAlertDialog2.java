package com.laba.partner.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.text.Spanned;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.text.HtmlCompat;

import com.laba.partner.R;

public class PermissionAlertDialog2 extends Dialog {

    PermissionAlertDialog.PositiveClick positiveClick;
    PermissionAlertDialog.NegativeClick negativeClick;

    public PermissionAlertDialog2(@NonNull Context context) {
        super(context);
        setContentView(R.layout.dialog_alert_permisson_2);


        TextView message = findViewById(R.id.textView2);

//        String htmlString = "<p>This app my want to access your location <strong>Allow all the time</strong>, even when you're not using the app.</p>";
        String htmlString = "App currently can access location 'Only while using the app'";

        Spanned spanned = HtmlCompat.fromHtml(htmlString, HtmlCompat.FROM_HTML_MODE_COMPACT);

        message.setText(spanned);

        findViewById(R.id.textView3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                positiveClick.click();
            }
        });

        findViewById(R.id.textView4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                negativeClick.click();
            }
        });
    }

    public void setPositiveClickListener(PermissionAlertDialog.PositiveClick positiveClick) {
        this.positiveClick = positiveClick;
    }

    public void setNegativeClickListener(PermissionAlertDialog.NegativeClick negativeClick) {
        this.negativeClick = negativeClick;
    }

    public interface PositiveClick {
        void click();
    }

    public interface NegativeClick {
        void click();
    }
}
