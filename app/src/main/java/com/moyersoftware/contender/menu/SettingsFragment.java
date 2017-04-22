package com.moyersoftware.contender.menu;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.moyersoftware.contender.R;
import com.moyersoftware.contender.util.Util;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.ByteArrayOutputStream;

import butterknife.Bind;
import butterknife.ButterKnife;

public class SettingsFragment extends Fragment {

    // Constants
    private static final int PICK_IMAGE_CODE = 0;
    private static final int USER_PHOTO_SIZE_PX = 500;

    // Views
    @Bind(R.id.settings_name_txt)
    TextView mNameTxt;
    @Bind(R.id.settings_username_txt)
    TextView mUsernameTxt;
    @Bind(R.id.settings_email_txt)
    TextView mEmailTxt;
    @Bind(R.id.settings_photo_img)
    ImageView mPhotoImg;
    @Bind(R.id.settings_photo_btn)
    Button mPhotoBtn;

    // Usual variables
    private Bitmap mBitmap;
    private StorageReference mImageRef;
    private String mUserId;
    private Target mTarget = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            mBitmap = bitmap;

            uploadImage();
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {
        }
    };

    public SettingsFragment() {
        // Required empty public constructor
    }

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        ButterKnife.bind(this, view);

        initMyAccount();
        initStorage();

        return view;
    }

    private void initMyAccount() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            mUserId = user.getUid();
            mNameTxt.setText(Util.getDisplayName());
            mUsernameTxt.setText(Util.parseUsername(user));
            mEmailTxt.setText(user.getEmail());
            Picasso.with(getActivity()).load(Util.getPhoto())
                    .placeholder(R.drawable.avatar_placeholder).fit().into(mPhotoImg);
            mPhotoBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onUpdatePhotoClicked();
                }
            });
        }
    }

    public void onUpdatePhotoClicked() {
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
                Picasso.with(getActivity()).load(data.getData()).placeholder(android.R.color.white)
                        .centerCrop().fit().into(mPhotoImg);

                Picasso.with(getActivity()).load(data.getData()).centerCrop().resize(USER_PHOTO_SIZE_PX,
                        USER_PHOTO_SIZE_PX).into(mTarget);
            }
        }
    }

    private void uploadImage() {
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

                Toast.makeText(getActivity(), "Can't upload image", Toast.LENGTH_LONG).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                savePhoto(taskSnapshot.getDownloadUrl() + "");
            }
        });
    }

    private void savePhoto(String photo) {
        Util.setPhoto(photo);

        FirebaseDatabase.getInstance().getReference().child("users").child(mUserId).child("image")
                .setValue(photo);
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
