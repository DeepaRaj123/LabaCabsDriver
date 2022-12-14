package com.laba.partner.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.laba.partner.MvpApplication;
import com.laba.partner.R;
import com.laba.partner.common.Constants;
import com.laba.partner.data.network.model.Wallet;

import java.util.List;

public class WalletAdapter extends RecyclerView.Adapter<WalletAdapter.MyViewHolder> {

    private List<Wallet> mWallets;
    private Context mContext;

    public WalletAdapter(List<Wallet> wallets) {
        this.mWallets = wallets;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        return new MyViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_wallet, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.tvId.setText(mWallets.get(position).getTransactionAlias());
        holder.tvDate.setText(mWallets.get(position).getCreatedAt());

        if (mWallets.get(position).getType().equalsIgnoreCase("C")) {
            holder.tvAmt.setTextColor(ContextCompat.getColor(mContext, R.color.green));
            holder.tvAmt.setText(String.format("%s %s",
                    Constants.Currency,
                    MvpApplication.getInstance().getNewNumberFormat(mWallets.get(position).getAmount())));
        } else {
            holder.tvAmt.setTextColor(ContextCompat.getColor(mContext, R.color.red));
            holder.tvAmt.setText(String.format("%s %s",
                    Constants.Currency,
                    MvpApplication.getInstance().getNewNumberFormat(mWallets.get(position).getAmount())));
        }

        if (position == mWallets.size() - 1) {
            holder.seperatorView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return mWallets.size();
    }

//    @Override
//    public int getItemCount() {
//        return 100;
//    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView tvId, tvDate, tvAmt;
        private View seperatorView;

        private MyViewHolder(View view) {
            super(view);
            tvId = view.findViewById(R.id.tvId);
            tvDate = view.findViewById(R.id.tvDate);
            tvAmt = view.findViewById(R.id.tvAmt);
            seperatorView = view.findViewById(R.id.seperator);
        }
    }
}