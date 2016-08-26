package com.moyersoftware.contender.login;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.moyersoftware.contender.R;
import com.moyersoftware.contender.login.data.User;
import com.moyersoftware.contender.menu.MainActivity;
import com.moyersoftware.contender.util.Util;

import butterknife.Bind;
import butterknife.ButterKnife;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class RegisterActivity extends AppCompatActivity {

    // Views
    @Bind(R.id.register_name_edit_txt)
    EditText mNameEditTxt;
    @Bind(R.id.register_email_edit_txt)
    EditText mEmailEditTxt;
    @Bind(R.id.register_password_edit_txt)
    EditText mPasswordEditTxt;
    @Bind(R.id.register_repeat_password_edit_txt)
    EditText mRepeatPasswordEditTxt;

    // Usual variables
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);

        overrideActivityAnimation();
        initAuth();
        initStatusBar();
    }

    private void initAuth() {
        // Initialize FirebaseAuth
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    FirebaseDatabase.getInstance().getReference().child("users")
                            .child(user.getUid()).setValue(new User(user.getUid(),
                            mNameEditTxt.getText().toString(), Util.parseUsername(user),
                            user.getEmail(), ""));
                    Util.setPhoto("");
                    /*if (Util.isReferralAsked()) {
                        finish();
                        LoadingActivity.sActivity.finish();
                        startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                    } else {*/
                        askReferral();
                    //}
                }
            }
        };
    }

    private void askReferral() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder
                (RegisterActivity.this, R.style.MaterialDialog);
        dialogBuilder.setTitle("Did someone invite you?");
        @SuppressLint("InflateParams")
        View view = LayoutInflater.from(RegisterActivity.this).inflate
                (R.layout.dialog_code, null);
        final EditText editTxt = (EditText) view.findViewById(R.id.loading_code_edit_txt);
        editTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                Util.setReferralCode(editTxt.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        dialogBuilder.setView(view);
        dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Util.setReferralAsked();
                finish();
                LoadingActivity.sActivity.finish();
                Util.setCurrentPlayerId(FirebaseAuth.getInstance()
                        .getCurrentUser().getUid());
                startActivity(new Intent(RegisterActivity.this, MainActivity.class));
            }
        });
        dialogBuilder.setNegativeButton("Skip", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Util.setReferralAsked();
                finish();
                LoadingActivity.sActivity.finish();
                Util.setCurrentPlayerId(FirebaseAuth.getInstance()
                        .getCurrentUser().getUid());
                startActivity(new Intent(RegisterActivity.this, MainActivity.class));
            }
        });
        dialogBuilder.create().show();
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(0, R.anim.activity_fade_out);
    }

    /**
     * Required for the calligraphy library.
     */
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    public void onRegisterButtonClicked(View view) {
        String email = mEmailEditTxt.getText().toString();
        String password = mPasswordEditTxt.getText().toString();
        String repeatedPassword = mRepeatPasswordEditTxt.getText().toString();
        if (TextUtils.isEmpty(mNameEditTxt.getText().toString()) || TextUtils.isEmpty(email)
                || TextUtils.isEmpty(password)) {
            Toast.makeText(RegisterActivity.this, "Some fields are empty", Toast.LENGTH_SHORT)
                    .show();
        } else if (!password.equals(repeatedPassword)) {
            Toast.makeText(RegisterActivity.this, "Passwords don't match", Toast.LENGTH_SHORT)
                    .show();
        } else {
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this,
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
                                    Toast.makeText(RegisterActivity.this, task.getException()
                                            .getMessage(), Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(RegisterActivity.this, "Authentication failed",
                                            Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                    });
        }
    }
}
