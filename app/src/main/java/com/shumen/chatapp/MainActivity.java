package com.shumen.chatapp;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.graphics.drawable.shapes.Shape;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.text.format.DateFormat;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    private static int SIGN_IN_REQUEST_CODE = 1;
    private FirebaseListAdapter<ChatMessage> adapter;
    private FirebaseUser user;

    private ConstraintLayout activity_main;
    private ListView listOfMessages;
    private FloatingActionButton fab;
    private EditText input;
    private ConstraintLayout messageItem;
    private TextView messageUserName, messageText, messageUserEmail, messageTime;

    // Signing out the current user.
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_sign_out) {
            AuthUI.getInstance().signOut(this).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Snackbar.make(activity_main, "You have been signed out", Snackbar.LENGTH_LONG).show();
                    finish();
                }
            });
        }
        return true;
    }

    // Adding the Sign out menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    // Manage signing in to the chat application
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SIGN_IN_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                user = FirebaseAuth.getInstance().getCurrentUser();
                Snackbar.make(activity_main, "Successfully signed in. Welcome " +
                        user.getDisplayName()+".", Snackbar.LENGTH_LONG).show();
                displayChatMessages();
            } else {
                Snackbar.make(activity_main, "We couldn't sign you in. Please try again later.", Snackbar.LENGTH_LONG).show();
                finish();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        intializeMainComponents();

        // Checking if the current user is connected to the application
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            // Redirecting the user sign up
            startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().build(),
                    SIGN_IN_REQUEST_CODE);
        } else {
            Snackbar.make(activity_main, "Welcome  " + FirebaseAuth.getInstance().getCurrentUser().getDisplayName(),
                    Snackbar.LENGTH_LONG).show();
            user = FirebaseAuth.getInstance().getCurrentUser();

            // Displaying existing messages in the chat room
            displayChatMessages();
        }

        // Let window moves up and down according to virtual keyboard
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        // Setting action on the send button
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Sending data to firebase

                FirebaseDatabase.getInstance().getReference().push().setValue(
                        new ChatMessage(
                                // Extracting the first name from the full name DisplayName
                                FirebaseAuth.getInstance().getCurrentUser().getDisplayName().split(" ")[0],
                                input.getText().toString(),
                                FirebaseAuth.getInstance().getCurrentUser().getEmail()));
                input.setText("");
            }
        });
    }

    private void intializeMainComponents() {
        activity_main = findViewById(R.id.activity_main);
        listOfMessages = findViewById(R.id.list_of_messages);
        fab = findViewById(R.id.fab_send);
        input = findViewById(R.id.input_message);
    }

    private void displayChatMessages() {
        adapter = new FirebaseListAdapter<ChatMessage>(this, ChatMessage.class, R.layout.row_item,
                FirebaseDatabase.getInstance().getReference()) {
            @Override
            protected void populateView(View v, ChatMessage model, int position) {

                messageItem = v.findViewById(R.id.message_item);
                messageUserName = v.findViewById(R.id.message_user_name);
                messageText = v.findViewById(R.id.message_text);
                messageUserEmail = v.findViewById(R.id.message_user_email);
                messageTime = v.findViewById(R.id.message_time);

                pickResource(v, model);
                writeTexts(model);
            }
        };
        listOfMessages.setAdapter(adapter);
    }

    private void writeTexts(ChatMessage model) {
        messageUserName.setText(model.getMessageUserName() + ":");
        messageText.setText(model.getMessageText());
        messageUserEmail.setText(model.getMessageUserEmail());
        messageTime.setText(DateFormat.format("dd-MM-yyyy(HH:mm)", model.getMessageTime()));
    }

    private void pickResource(View v, ChatMessage model) {
        GradientDrawable drawable = (GradientDrawable) messageItem.getBackground();
        ConstraintLayout.LayoutParams messageItemLayout= (ConstraintLayout.LayoutParams) messageItem.getLayoutParams();
        int margin=80;
        float radius=15;
        if (model.getMessageUserEmail().equals(user.getEmail())) {
            // This is an outgoing message.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                messageItem.setBackgroundTintList(getResources().getColorStateList(R.color.colorMessageBackgroundOut));
                messageItemLayout.setMarginEnd(margin);
            }
            else {
                drawable.setColor(getResources().getColor(R.color.colorMessageBackgroundOut));
                messageItemLayout.setMargins(0,0,margin,0);
            }
            drawable.setCornerRadii(new float[]{0, 0,radius,radius,radius,radius,radius,radius});
        } else {
            // This is an income message.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                messageItem.setBackgroundTintList(getResources().getColorStateList(R.color.colorMessageBackgroundIn));
                messageItemLayout.setMarginStart(margin);
            }
            else {
                drawable.setColor(getResources().getColor(R.color.colorMessageBackgroundIn));
                messageItemLayout.setMargins(margin,0,0,0);
            }
            drawable.setCornerRadii(new float[]{radius,radius,0,0,radius,radius,radius,radius});
        }
    }
}
