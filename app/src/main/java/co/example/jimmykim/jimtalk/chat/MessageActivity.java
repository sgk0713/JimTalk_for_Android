package co.example.jimmykim.jimtalk.chat;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import co.example.jimmykim.jimtalk.R;
import co.example.jimmykim.jimtalk.model.ChatModel;
import co.example.jimmykim.jimtalk.model.UserModel;

public class MessageActivity extends AppCompatActivity {

    private String destinationUid;
    private Button button;
    private EditText editText;

    private String uid;
    private String chatRoomUid;

    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        destinationUid = getIntent().getStringExtra("destinationUid");
        button = findViewById(R.id.messageactivity_button);
        editText = findViewById(R.id.messageactivity_editText);

        recyclerView = findViewById(R.id.messageactivity_recyclerview);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String tmpMessage = editText.getText().toString();
                editText.setText("");
                if(tmpMessage.equals("")) {
                    return;
                }
                ChatModel chatModel = new ChatModel();
                chatModel.users.put(uid, true);
                chatModel.users.put(destinationUid, true);

                if(chatRoomUid == null) {
                    button.setEnabled(false);

                    FirebaseDatabase.getInstance().getReference().child("chatrooms").push().setValue(chatModel)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                checkChatRoom();
                            }
                        });
                }else {
                    ChatModel.Comment comment = new ChatModel.Comment();
                    comment.uid = uid;
                    comment.message = tmpMessage;
                    FirebaseDatabase.getInstance().getReference().child("chatrooms").child(chatRoomUid).child("comments").push().setValue(comment);
                }
            }
        });


    }

    void checkChatRoom(){
        FirebaseDatabase
                .getInstance()
                .getReference()
                .child("chatrooms")
                .orderByChild("users/"+uid)
                .equalTo(true)
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot item : dataSnapshot.getChildren()){
                    ChatModel chatModel = item.getValue(ChatModel.class);
                    if(chatModel.users.containsKey(destinationUid)){
                        chatRoomUid = item.getKey();
                        button.setEnabled(true);

                        recyclerView.setLayoutManager(new LinearLayoutManager(MessageActivity.this));
                        recyclerView.setAdapter(new RecyclerViewAdapter());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        List<ChatModel.Comment> comments;
        UserModel userModel;
        public RecyclerViewAdapter() {
            comments = new ArrayList<>();

            FirebaseDatabase.getInstance().getReference().child("users").child(destinationUid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    userModel = dataSnapshot.getValue(UserModel.class);
                    getMessageList();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }

        void getMessageList(){
            FirebaseDatabase.getInstance().getReference().child("chatrooms").child(chatRoomUid).child("comments").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    comments.clear();
                    for(DataSnapshot item : dataSnapshot.getChildren()){
                        comments.add(item.getValue(ChatModel.Comment.class));
                    }
                    notifyDataSetChanged();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
            return new MessageViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            MessageViewHolder messageViewHolder = ((MessageViewHolder)holder);

            if(comments.get(position).uid.equals(uid)) {
                messageViewHolder.textView_message.setText(comments.get(position).message);
                messageViewHolder.textView_message.setBackgroundResource(R.drawable.right);
                messageViewHolder.linearLayout_destination.setVisibility(View.INVISIBLE);
            }else{
                Glide.with(holder.itemView.getContext())
                        .load(userModel.profileImageUrl)
                        .apply(new RequestOptions().circleCrop())
                        .into(messageViewHolder.imageView_profile);
                messageViewHolder.textView_name.setText(userModel.userName);
                messageViewHolder.linearLayout_destination.setVisibility(View.VISIBLE);
                messageViewHolder.textView_message.setBackgroundResource(R.drawable.left);
                messageViewHolder.textView_message.setText(comments.get(position).message);
                messageViewHolder.textView_message.setTextSize(25);
            }
        }

        @Override
        public int getItemCount() {
            return comments.size();
        }

        private class MessageViewHolder extends RecyclerView.ViewHolder {
            public TextView textView_message;
            public TextView textView_name;
            public ImageView imageView_profile;
            public LinearLayout linearLayout_destination;

            public MessageViewHolder(View view) {
                super(view);
                textView_message = view.findViewById(R.id.messageitem_textview);
                textView_name = view.findViewById(R.id.messageitem_textview_name);
                imageView_profile = view.findViewById(R.id.messageitem_imageview_profile);
                linearLayout_destination = view.findViewById(R.id.messageitem_linearlayout_destination);



            }
        }
    }
}
