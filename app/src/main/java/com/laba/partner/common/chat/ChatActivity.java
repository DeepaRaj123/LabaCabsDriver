package com.laba.partner.common.chat;

import android.os.Bundle;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import androidx.annotation.NonNull;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.laba.partner.R;
import com.laba.partner.base.BaseActivity;
import com.laba.partner.common.Constants;
import com.laba.partner.data.network.APIClient;

import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class ChatActivity extends BaseActivity {

    public static String sender = "provider";
    String chatPath = null;
    @BindView(R.id.chat_lv)
    ListView chatLv;
    @BindView(R.id.message)
    EditText message;
    @BindView(R.id.send)
    ImageView send;
    @BindView(R.id.chat_controls_layout)
    LinearLayout chatControlsLayout;
    private ChatMessageAdapter mAdapter;
    private CompositeDisposable mCompositeDisposable;
    private DatabaseReference myRef;

    @Override
    public int getLayoutId() {
        return R.layout.activity_chat;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        mCompositeDisposable = new CompositeDisposable();
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            chatPath = extras.getString(Constants.SharedPref.request_id, null);
//            Toast.makeText(getApplicationContext(),"Chat Path is "+chatPath,Toast.LENGTH_LONG).show();
            Log.d("ChatPath ", chatPath);
            initChatView(chatPath);
        }
    }

    private void initChatView(String chatPath) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef = database.getReference().child(chatPath)/*.child("chat")*/;

        try {
            if (chatPath == null) return;

            message.setOnEditorActionListener((v, actionId, event) -> {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    String myText = message.getText().toString().trim();
                    if (myText.length() > 0) sendMessage(myText);
                    handled = true;
                }
                return handled;
            });

            mAdapter = new ChatMessageAdapter(activity(), new ArrayList<>());
            chatLv.setAdapter(mAdapter);


            myRef.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String prevChildKey) {
                    Chat mChat = dataSnapshot.getValue(Chat.class);
                    if (Objects.requireNonNull(mChat).getSender() != null && mChat.getRead() != null)
                        if (!mChat.getSender().equals(sender) && mChat.getRead() == 0) {
                            mChat.setRead(1);
                            dataSnapshot.getRef().setValue(mChat);
                        }
                    mAdapter.add(mChat);
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String prevChildKey) {
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String prevChildKey) {
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnClick(R.id.send)
    public void onViewClicked() {
        String myText = message.getText().toString();
        if (myText.length() > 0) sendMessage(myText);
    }

    private void sendMessage(String messageStr) {

        Chat chat = new Chat();
        chat.setSender(sender);
        chat.setTimestamp(new Date().getTime());
        chat.setType("text");
        chat.setText(messageStr);
        chat.setRead(0);
        chat.setUrl("");

        if (DATUM != null) {
            chat.setDriverId(DATUM.getProviderId());
            chat.setUserId(DATUM.getUserId());
        }

        myRef.push().setValue(chat);
        message.setText("");

        if (DATUM != null) {
            mCompositeDisposable.add(APIClient
                    .getAPIClient()
                    .postChatItem("provider", String.valueOf(DATUM.getUserId()), messageStr)
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(o -> System.out.println("RRR o = " + o)));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCompositeDisposable.dispose();
    }
}
