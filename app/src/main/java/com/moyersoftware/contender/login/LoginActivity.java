package com.moyersoftware.contender.login;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.moyersoftware.contender.R;
import com.moyersoftware.contender.menu.MainActivity;
import com.moyersoftware.contender.util.Util;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public class LoginActivity extends AppCompatActivity {

    Button mSignInButton;
    EditText mEmailEditTxt, mPasswordEditTxt;
    TextView txt_goback, txt_forgot;

    // Usual variables
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private boolean mRestorePassword = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        mEmailEditTxt       =   findViewById(R.id.login_email_edit_txt);
        mPasswordEditTxt    =   findViewById(R.id.login_password_edit_txt);
        mSignInButton       =   findViewById(R.id.login_sign_in_btn);
        txt_goback          =   findViewById(R.id.txt_goback);
        txt_forgot          =   findViewById(R.id.txt_forgot);

        overrideActivityAnimation();
        initAuth();
        initStatusBar();

        mSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginClicked();
            }
        });

        txt_goback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginActivity.super.onBackPressed();
                overridePendingTransition(0, R.anim.activity_fade_out);
            }
        });

        txt_forgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    private void initAuth() {
        // Initialize FirebaseAuth
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                final FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    FirebaseDatabase.getInstance().getReference().child("users")
                            .child(user.getUid()).child("image").addListenerForSingleValueEvent
                            (new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        String oldPhoto = dataSnapshot.getValue(String.class);
                                        Util.setPhoto(oldPhoto);
                                    } else {
                                        Util.setPhoto(user.getPhotoUrl() + "");
                                    }

                                    String id = FirebaseAuth.getInstance()
                                            .getCurrentUser().getUid();
                                    Util.setCurrentPlayerId(id);

                                    finish();
                                    LoadingActivity.sActivity.finish();
                                    if (!Util.findFriendsShown(id)) {
                                        startActivity(new Intent(LoginActivity.this,
                                                FindFriendsActivity.class));
                                        Util.setFindFriendsShown(id);
                                    } else {
                                        startActivity(new Intent(LoginActivity.this,
                                                MainActivity.class));
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                }
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    /**
     * Creates a cross fade effect between loading and registration screens.
     */
    private void overrideActivityAnimation() {
        overridePendingTransition(R.anim.activity_fade_in, R.anim.activity_hold);
    }

    /**
     * Makes the status bar translucent.
     */
    private void initStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
    }

    private void LoginClicked() {
        if (!mRestorePassword) {
            String email = mEmailEditTxt.getText().toString();
            String password = mPasswordEditTxt.getText().toString();
            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(LoginActivity.this, "Some fields are empty", Toast.LENGTH_SHORT)
                        .show();
            } else {
                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this,
                        new OnCompleteListener<AuthResult>() {
                            @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                // If sign in fails, display a message to the user. If sign in succeeds
                                // the auth state listener will be notified and logic to handle the
                                // signed in user can be handled in the listener.
                                if (!task.isSuccessful()) {
                                    if (task.getException() != null && task.getException()
                                            .getMessage() != null) {
                                        Toast.makeText(LoginActivity.this, task.getException()
                                                .getMessage(), Toast.LENGTH_LONG).show();
                                    } else {
                                        Toast.makeText(LoginActivity.this, "Authentication failed",
                                                Toast.LENGTH_LONG).show();
                                    }
                                }
                            }
                        });
            }
        } else {
            restorePassword();
        }
    }


    public void onRestorePasswordButtonClicked(View view) {
        mPasswordEditTxt.setVisibility(View.INVISIBLE);
        txt_forgot.setVisibility(View.GONE);
        mSignInButton.setText(R.string.reset_password);
        mRestorePassword = true;
    }

    private void restorePassword() {
        String email = mEmailEditTxt.getText().toString();
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(LoginActivity.this, "Enter your email address", Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        mSignInButton.setText(R.string.loading);
        mSignInButton.setEnabled(false);

        FirebaseAuth auth = FirebaseAuth.getInstance();

        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this,
                                    "Please follow instructions in the email",
                                    Toast.LENGTH_SHORT).show();

                            mPasswordEditTxt.setVisibility(View.VISIBLE);
                            mPasswordEditTxt.setText("");
                            txt_forgot.setVisibility(View.VISIBLE);
                            mSignInButton.setText(R.string.login_submit);
                            mRestorePassword = false;
                        } else {
                            Toast.makeText(LoginActivity.this,
                                    "Email address is invalid or doesn't exist",
                                    Toast.LENGTH_SHORT).show();
                            mSignInButton.setText(R.string.login_forgot_password);
                        }

                        mSignInButton.setEnabled(true);
                    }
                });
    }
}