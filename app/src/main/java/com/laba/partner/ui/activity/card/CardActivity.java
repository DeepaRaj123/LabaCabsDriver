package com.laba.partner.ui.activity.card;

import android.app.AlertDialog;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.laba.partner.R;
import com.laba.partner.base.BaseActivity;
import com.laba.partner.data.network.model.Card;
import com.laba.partner.ui.activity.add_card.AddCardActivity;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import es.dmoral.toasty.Toasty;

public class CardActivity extends BaseActivity implements CardIView {

    @BindView(R.id.cards_rv)
    RecyclerView cardsRv;
    @BindView(R.id.add_card)
    TextView addCard;
    @BindView(R.id.llCardContainer)
    LinearLayout llCardContainer;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    List<Card> cardsList = new ArrayList<>();
    private CardPresenter<CardActivity> presenter = new CardPresenter<>();
    private CardAdapter adapter;

    @Override
    public int getLayoutId() {
        return R.layout.activity_card;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        presenter.attachView(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.card));
        adapter = new CardAdapter(cardsList);
        cardsRv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        cardsRv.setAdapter(adapter);
    }

    @Override
    public void onSuccess(Object card) {
        hideLoading();
        Toasty.success(activity(), getString(R.string.card_deleted_successfully), Toast.LENGTH_SHORT).show();
        showLoading();
        presenter.card();
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.card();
    }

    @Override
    public void onSuccess(List<Card> cards) {
        hideLoading();
        cardsList.clear();
        cardsList.addAll(cards);
        cardsRv.setAdapter(new CardAdapter(cardsList));
    }


    @Override
    public void onError(Throwable e) {
        hideLoading();
        if (e != null)
            onErrorBase(e);
    }

    @Override
    public void onSuccessChangeCard(Object card) {
        hideLoading();
        Toasty.success(this, card.toString(), Toast.LENGTH_SHORT).show();
        showLoading();
        presenter.card();
    }

    @Override
    protected void onDestroy() {
        presenter.detachView();
        super.onDestroy();
    }

    public void deleteCard(@NonNull Card card) {
        new AlertDialog.Builder(this)
                .setMessage(getString(R.string.are_sure_you_want_to_delete))
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(getString(R.string.delete), (dialog, whichButton) -> {
                    showLoading();
                    presenter.deleteCard(card.getCardId());
                }).setNegativeButton(getString(R.string.no), null).show();
    }

    @OnClick(R.id.add_card)
    public void onViewClicked() {
        startActivity(new Intent(this, AddCardActivity.class));
    }

    private void sizeOfDeleteCard() {
        new AlertDialog.Builder(this)
                .setMessage(getString(R.string.cant_able_to_delete_card))
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(getString(R.string.ok), null).show();
    }

    private void changeCard(Card card) {
        new AlertDialog.Builder(this)
                .setMessage(getString(R.string.are_sure_you_want_to_change_default_card))
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(getString(R.string.change_card), (dialog, whichButton) -> {
                    showLoading();
                    presenter.changeCard(card.getCardId());
                })
                .setNegativeButton(getString(R.string.no), null).show();
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

    public class CardAdapter extends RecyclerView.Adapter<CardAdapter.MyViewHolder> {

        private List<Card> list;

        CardAdapter(List<Card> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new MyViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_card, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            Card item = list.get(position);
            holder.card.setText(getString(R.string.card_) + item.getLastFour());
           /* if (item.getIsDefault() != 0) {
                holder.cardSelect.setVisibility(View.VISIBLE);
            } else {
                holder.cardSelect.setVisibility(View.INVISIBLE);
            }*/
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
            private RelativeLayout itemView;
            private TextView card;
            private ImageView cardSelect;

            MyViewHolder(View view) {
                super(view);
                itemView = view.findViewById(R.id.item_view);
                card = view.findViewById(R.id.card);
                cardSelect = view.findViewById(R.id.card_select);
                // itemView.setOnClickListener(this);
                // itemView.setOnLongClickListener(this);
            }

            @Override
            public void onClick(View view) {
                int position = getAdapterPosition();
                Card card = list.get(position);
                if (view.getId() == R.id.item_view) {
                    if (card != null) {
                        if (list.size() > 1) {
                            changeCard(card);
                        } else {
                            sizeOfDeleteCard();
                        }
                    }
                }
            }

            @Override
            public boolean onLongClick(View v) {
                int position = getAdapterPosition();
                Card card = list.get(position);
                if (v.getId() == R.id.item_view) {
                    if (list.size() > 1) {
                        deleteCard(card);
                    } else {
                        sizeOfDeleteCard();
                    }
                }
                return true;
            }
        }
    }
}
