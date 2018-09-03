package com.moyersoftware.contender.login;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.moyersoftware.contender.R;
import com.moyersoftware.contender.login.data.User;
import com.moyersoftware.contender.menu.MainActivity;
import com.moyersoftware.contender.util.Util;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.ByteArrayOutputStream;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class RegisterActivity extends AppCompatActivity {

    // Constants
    private static final int PICK_IMAGE_CODE = 0;
    private static final int USER_PHOTO_SIZE_PX = 500;

    // Views
    @BindView(R.id.register_name_edit_txt)
    EditText mNameEditTxt;
    @BindView(R.id.register_email_edit_txt)
    EditText mEmailEditTxt;
    @BindView(R.id.register_password_edit_txt)
    EditText mPasswordEditTxt;
    @BindView(R.id.register_repeat_password_edit_txt)
    EditText mRepeatPasswordEditTxt;
    @BindView(R.id.register_photo_img)
    ImageView mPhotoImg;

    // Usual variables
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private Bitmap mBitmap;
    private String mUserId;
    private StorageReference mImageRef;
    private ProgressDialog mDialog;
    private Target mTarget = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            mBitmap = bitmap;
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {
        }
    };

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
                            user.getEmail(), "", null));
                    Util.setPhoto("");

                    if (mBitmap != null) {
                        mDialog = ProgressDialog.show(RegisterActivity.this, "Loading...",
                                "");
                        uploadImage();
                    } else {
                        finish();
                        LoadingActivity.sActivity.finish();
                        Util.setCurrentPlayerId(FirebaseAuth.getInstance()
                                .getCurrentUser().getUid());

                        startActivity(new Intent(RegisterActivity.this, FindFriendsActivity.class));
                    }
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
            Toast.makeText(RegisterActivity.this, "Some fields are empty!", Toast.LENGTH_SHORT)
                    .show();
        } else if (!password.equals(repeatedPassword)) {
            Toast.makeText(RegisterActivity.this, "Passwords don't match!", Toast.LENGTH_SHORT)
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

    public void onBackButtonClicked(View view) {
        finish();
    }

    public void onLoginButtonClicked(View view) {
        finish();
        startActivity(new Intent(this, LoginActivity.class));
    }

    public void onUpdatePhotoClicked(View v) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select a game image"),
                PICK_IMAGE_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                Picasso.with(this).load(data.getData()).placeholder(android.R.color.white)
                        .centerCrop().fit().into(mPhotoImg);

                Picasso.with(this).load(data.getData()).centerCrop().resize(USER_PHOTO_SIZE_PX,
                        USER_PHOTO_SIZE_PX).into(mTarget);
            }
        }
    }

    private void uploadImage() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            mUserId = user.getUid();
        }

        initStorage();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        byte[] data = outputStream.toByteArray();

        UploadTask uploadTask = mImageRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                Util.Log("File uploading failure: " + exception);

                Toast.makeText(RegisterActivity.this, "Can't upload image", Toast.LENGTH_LONG).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                @SuppressWarnings("ConstantConditions")
                Task<Uri> downUrl = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                savePhoto(downUrl.getResult().toString());
            }
        });
    }

    private void savePhoto(String photo) {
        Util.setPhoto(photo);

        FirebaseDatabase.getInstance().getReference().child("users").child(mUserId).child("image")
                .setValue(photo);

        mDialog.dismiss();
        finish();
        LoadingActivity.sActivity.finish();
        Util.setCurrentPlayerId(FirebaseAuth.getInstance()
                .getCurrentUser().getUid());
        startActivity(new Intent(RegisterActivity.this, MainActivity.class));
    }

    private void initStorage() {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        // Create a storage reference from our app
        StorageReference storageRef = storage.getReferenceFromUrl
                ("gs://contender-3ef7d.appspot.com");

        // Create a reference the photo
        mImageRef = storageRef.child(mUserId + ".jpg");
    }
}
