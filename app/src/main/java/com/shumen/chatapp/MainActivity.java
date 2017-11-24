package com.shumen.chatapp;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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

    private ConstraintLayout activity_main;
    private ListView listOfMessages;
    private FloatingActionButton fab;
    private EditText input;
    private TextView messageUserName, messageText, messageUserEmail, messageTime;

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SIGN_IN_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Snackbar.make(activity_main, "Successfully signed in. Welcome!", Snackbar.LENGTH_LONG).show();
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

        activity_main = findViewById(R.id.activity_main);
        listOfMessages = findViewById(R.id.list_of_messages);
        fab = findViewById(R.id.fab_send);
        input = findViewById(R.id.input_message);

        // Checking if the current user is connected to the application
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            // Redirecting the user sign up
            startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().build(),
                    SIGN_IN_REQUEST_CODE);
        } else {
            Snackbar.make(activity_main, "Welcome  " + FirebaseAuth.getInstance().getCurrentUser().getDisplayName(),
                    Snackbar.LENGTH_LONG).show();

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
                // **********************************************************
                // Work here to solve the problem
                // **********************************************************

                FirebaseDatabase.getInstance().getReference().push().setValue(
                        new ChatMessage(
                                // Extracting the first name from the full name DisplayName
                                FirebaseAuth.getInstance().getCurrentUser().getDisplayName().split(" ")[0],
                                input.getText().toString(),
                                FirebaseAuth.getInstance().getCurrentUser().getEmail()));

                // **********************************************************
                // **********************************************************
                input.setText("");
            }
        });
    }

    private void displayChatMessages() {
        adapter = new FirebaseListAdapter<ChatMessage>(this, ChatMessage.class, R.layout.list_item,
                FirebaseDatabase.getInstance().getReference()) {
            @Override
            protected void populateView(View v, ChatMessage model, int position) {

                messageUserName = v.findViewById(R.id.message_user_name);
                messageText = v.findViewById(R.id.message_text);
                messageUserEmail = v.findViewById(R.id.message_user_email);
                messageTime = v.findViewById(R.id.message_time);

                messageUserName.setText(model.getMessageUserName()+":");
                messageText.setText(model.getMessageText());
                if(model.getMessageUserEmail().equals(FirebaseAuth.getInstance().getCurrentUser().getEmail()))
                    messageText.append(". From "+messageUserName.getText());
                messageUserEmail.setText(model.getMessageUserEmail());
                messageTime.setText(DateFormat.format("dd-MM-yyyy (hh:mm:ss)", model.getMessageTime()));
            }
        };
        listOfMessages.setAdapter(adapter);
    }
}
