package com.laba.partner.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.text.HtmlCompat;

import com.laba.partner.R;

public class PermissionAlertDialog extends Dialog {

    PositiveClick positiveClick;
    NegativeClick negativeClick;

    public PermissionAlertDialog(@NonNull Context context) {
        super(context);
        setContentView(R.layout.dialog_alert);


        TextView message = findViewById(R.id.textView2);

        String htmlString = "For improved pickups and drop-offs, customer support, and safety, LaBa Driver collects your location data, when the app is in background, from the time of trip request through five minutes after the trip ends. Learn more: https://labacabs.com/privacy ";


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

    public void setPositiveClickListener(PositiveClick positiveClick) {
        this.positiveClick = positiveClick;
    }

    public void setNegativeClickListener(NegativeClick negativeClick) {
        this.negativeClick = negativeClick;
    }

    public interface PositiveClick {
        void click();
    }

    public interface NegativeClick {
        void click();
    }
}
