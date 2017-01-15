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
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.moyersoftware.contender.R;
import com.moyersoftware.contender.login.data.User;
import com.moyersoftware.contender.menu.MainActivity;
import com.moyersoftware.contender.util.Util;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class LoadingActivity extends AppCompatActivity {

    public static LoadingActivity sActivity;
    private CallbackManager mCallbackManager;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private String mFacebookName = null;
    private String mFacebookPhoto = null;
    private String mFacebookEmail = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        initActivity();
        initAuth();
        initStatusBar();
        initFacebook();
    }

    private void initAuth() {
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                final FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    if (mFacebookName != null) {
                        FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid())
                                .child("name").setValue(mFacebookName);
                        FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid())
                                .child("image").setValue(mFacebookPhoto);
                        FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid())
                                .child("email").setValue(mFacebookEmail);

                        /*if (Util.isReferralAsked()) {

                                    Util.setCurrentPlayerId(FirebaseAuth.getInstance()
                                            .getCurrentUser().getUid());
                            startActivity(new Intent(LoadingActivity.this, MainActivity.class));
                            finish();
                        } else {*/
                        askReferral();
                        //}
                    } else {
                        FirebaseDatabase.getInstance().getReference().child("users")
                                .child(user.getUid()).child("image").addListenerForSingleValueEvent
                                (new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()) {
                                            String oldPhoto = dataSnapshot.getValue(String.class);
                                            Util.setPhoto(oldPhoto);
                                            FirebaseDatabase.getInstance().getReference().child("users")
                                                    .child(user.getUid()).setValue(new User(user.getUid(),
                                                    user.getDisplayName(), Util.parseUsername(user), user.getEmail(),
                                                    oldPhoto));
                                        } else {
                                            Util.setPhoto(user.getPhotoUrl() + "");
                                            FirebaseDatabase.getInstance().getReference().child("users")
                                                    .child(user.getUid()).setValue(new User(user.getUid(),
                                                    user.getDisplayName(), Util.parseUsername(user), user.getEmail(),
                                                    user.getPhotoUrl() + ""));
                                        }
                                        /*if (Util.isReferralAsked()) {
                                    Util.setCurrentPlayerId(FirebaseAuth.getInstance()
                                            .getCurrentUser().getUid());
                                            startActivity(new Intent(LoadingActivity.this, MainActivity.class));
                                            finish();
                                        } else {*/
                                        askReferral();
                                        //}
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                    }
                }
            }
        };
    }

    private void askReferral() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder
                (LoadingActivity.this, R.style.MaterialDialog);
        dialogBuilder.setTitle("Did someone invite you?");
        @SuppressLint("InflateParams")
        View view = LayoutInflater.from(LoadingActivity.this).inflate
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

                Util.setCurrentPlayerId(FirebaseAuth.getInstance()
                        .getCurrentUser().getUid());
                startActivity(new Intent(LoadingActivity.this, MainActivity.class));
                finish();
            }
        });
        dialogBuilder.setNegativeButton("Skip", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Util.setReferralAsked();
                Util.setCurrentPlayerId(FirebaseAuth.getInstance()
                        .getCurrentUser().getUid());
                startActivity(new Intent(LoadingActivity.this, MainActivity.class));
                finish();
            }
        });
        dialogBuilder.create().show();
    }

    private void initFacebook() {
        mCallbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().logOut();
        LoginManager.getInstance().registerCallback(mCallbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(final LoginResult loginResult) {
                        GraphRequest request = GraphRequest.newMeRequest(
                                loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                                    @Override
                                    public void onCompleted(JSONObject me, GraphResponse response) {
                                        if (response.getError() != null) {
                                            Util.Log("Can't retrieve Facebook info");
                                        } else {
                                            try {
                                                mFacebookName = response.getJSONObject()
                                                        .get("name").toString();
                                                mFacebookPhoto = "https://graph.facebook.com/" + response.getJSONObject()
                                                        .get("id").toString() + "/picture?type=large";
                                                mFacebookEmail = response.getJSONObject()
                                                        .get("email").toString();

                                                Util.setDisplayName(mFacebookName);
                                                Util.setPhoto(mFacebookPhoto);
                                            } catch (JSONException e) {
                                                Util.Log("Can't retrieve Facebook name: " + e + ", " + me.toString());
                                            }
                                        }
                                        handleFacebookAccessToken(loginResult.getAccessToken());
                                    }
                                });
                        Bundle parameters = new Bundle();
                        parameters.putString("fields", "id,name,email");
                        request.setParameters(parameters);
                        request.executeAsync();
                    }

                    @Override
                    public void onCancel() {
                        Toast.makeText(LoadingActivity.this, "Login Canceled", Toast.LENGTH_LONG)
                                .show();
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        Util.Log("Facebook error: " + exception);
                        Toast.makeText(LoadingActivity.this, exception.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void handleFacebookAccessToken(AccessToken accessToken) {
        AuthCredential credential = FacebookAuthProvider.getCredential(accessToken
                .getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Toast.makeText(LoadingActivity.this, "Authentication failed: " + task.getException(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void initActivity() {
        sActivity = this;
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

    /**
     * Opens a log in screen.
     */
    public void onLoginButtonClicked(View view) {
        startActivity(new Intent(this, LoginActivity.class));
    }

    /**
     * Signs in with Facebook.
     */
    public void onFacebookButtonClicked(View view) {
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile",
                "email"));
    }

    /**
     * Opens a registration screen.
     */
    public void onRegisterButtonClicked(View view) {
        startActivity(new Intent(this, RegisterActivity.class));
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
     * Required for the calligraphy library.
     */
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}
