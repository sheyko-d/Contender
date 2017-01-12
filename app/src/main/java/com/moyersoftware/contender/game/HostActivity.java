package com.moyersoftware.contender.game;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.vending.billing.IInAppBillingService;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.moyersoftware.contender.BuildConfig;
import com.moyersoftware.contender.R;
import com.moyersoftware.contender.game.adapter.HostEventsAdapter;
import com.moyersoftware.contender.game.data.Event;
import com.moyersoftware.contender.game.data.GameInvite;
import com.moyersoftware.contender.game.data.SelectedSquare;
import com.moyersoftware.contender.game.receiver.BootReceiver;
import com.moyersoftware.contender.menu.data.Player;
import com.moyersoftware.contender.util.Util;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class HostActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks {

    // Constants
    private static final int PICK_IMAGE_CODE = 0;
    private static final int GAME_IMAGE_SIZE_PX = 256;
    private static final int MIN_PASSWORD_LENGTH = 4;
    private static final int LOCATION_PERMISSION_CODE = 0;

    // Views
    @Bind(R.id.host_id_txt)
    TextView mIdTxt;
    @Bind(R.id.host_game_img)
    ImageView mGameImg;
    @Bind(R.id.host_name_edit_txt)
    EditText mNameEditTxt;
    @Bind(R.id.host_password_edit_txt)
    EditText mPasswordEditTxt;
    @Bind(R.id.host_square_price_edit_txt)
    EditText mSquarePriceEditTxt;
    @Bind(R.id.host_quarter1_price_edit_txt)
    EditText mQuarter1PriceEditTxt;
    @Bind(R.id.host_quarter2_price_edit_txt)
    EditText mQuarter2PriceEditTxt;
    @Bind(R.id.host_quarter3_price_edit_txt)
    EditText mQuarter3PriceEditTxt;
    @Bind(R.id.host_final_price_edit_txt)
    EditText mFinalPriceEditTxt;
    @Bind(R.id.host_square_limit_edit_txt)
    EditText mSquareLimitEditTxt;
    @Bind(R.id.host_total_price_txt)
    TextView mTotalPriceTxt;
    @Bind(R.id.host_img_progress_bar)
    ProgressBar mProgressBar;
    @Bind(R.id.host_event_txt)
    TextView eventTxt;
    @Bind(R.id.host_code_edit_txt)
    EditText mCodeEditTxt;

    // Usual variables
    private DatabaseReference mDatabase;
    private String mGameId;
    private StorageReference mImageRef;
    private Bitmap mBitmap;
    private ProgressDialog mProgressDialog;
    private String mName;
    private String mPassword;
    private String mAuthorId;
    private String mAuthorEmail;
    private String mAuthorName;
    private Integer mSquarePrice = -1;
    private Integer mQuarter1Price = -1;
    private Integer mQuarter2Price = -1;
    private Integer mQuarter3Price = -1;
    private Integer mFinalPrice = -1;
    private Integer mTotalPrice = -1;
    private boolean mQuarterPricesEnabled = true;
    private boolean mListenToFieldChanges = true;
    private GoogleApiClient mGoogleApiClient;
    private double mLatitude;
    private double mLongitude;
    private HostEventsAdapter mAdapter;
    private ArrayList<Event> mEvents = new ArrayList<>();
    private AlertDialog mEventsDialog;
    private String mEventId;
    private String mAuthorImage;
    private String mCode;
    private Long mEventTime;
    private Boolean mFirstFreeGame = false;
    private Integer mSquaresLimit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host);
        ButterKnife.bind(this);

        initBilling();
        initId();
        initUser();
        initDatabase();
        initStorage();
        initPrices();
        initGoogleClient();
        initCodes();
        loadEvents();
    }

    protected void onPostResume() {
        super.onPostResume();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                loadFirstGameState();
            }
        }, 500L);
    }

    private void loadFirstGameState() {
        FirebaseDatabase.getInstance().getReference().child("free_first_game_enabled")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Boolean enabled;
                        try {
                            enabled = dataSnapshot.getValue(Boolean.class);
                        } catch (Exception e) {
                            enabled = true;
                        }
                        if (enabled == null) {
                            enabled = true;
                        }
                        if (enabled) {
                            checkFreeGameState();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
    }

    private void checkFreeGameState() {
        FirebaseDatabase.getInstance().getReference().child("users").child
                (FirebaseAuth.getInstance().getCurrentUser().getUid()).child("free_first_game")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue() == null || dataSnapshot.getValue(Boolean.class)) {
                            mFirstFreeGame = true;
                            mCodeEditTxt.setEnabled(false);
                            Toast.makeText(HostActivity.this, "First game is free, enjoy!",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void initCodes() {
        mCodeEditTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                loadCodes(mCodeEditTxt.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void loadCodes(String query) {
        OkHttpClient client = new OkHttpClient();

        RequestBody formBody = new FormBody.Builder()
                .add("query", query)
                .build();
        Request request = new Request.Builder()
                .url(Util.GET_CODES_URL)
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Util.Log("Can't retrieve codes: " + e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) return;

                String responseTxt = response.body().string();
                Util.Log("response: " + responseTxt);

                try {
                    JSONObject codeJson = new JSONObject(responseTxt);
                    boolean expired = codeJson.getInt("expired") == 1;
                    if (!expired) {
                        mCode = codeJson.getString("text");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(HostActivity.this, "Great, this game will be free!",
                                        Toast.LENGTH_SHORT).show();
                                mCodeEditTxt.setEnabled(false);
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(HostActivity.this, "Code is expired",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } catch (JSONException e) {
                    Util.Log("Can't parse codes: " + e);
                }
            }
        });
    }

    private void initBilling() {
        Intent serviceIntent =
                new Intent("com.android.vending.billing.InAppBillingService.BIND");
        serviceIntent.setPackage("com.android.vending");
        bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);
    }

    IInAppBillingService mService;

    ServiceConnection mServiceConn = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name,
                                       IBinder service) {
            mService = IInAppBillingService.Stub.asInterface(service);
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mService != null) {
            unbindService(mServiceConn);
        }
    }

    private void loadEvents() {
        mDatabase.child("events").orderByChild("time").addValueEventListener
                (new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        mEvents.clear();
                        if (dataSnapshot.exists()) {
                            String previousEventWeek = null;
                            String previousEventDate = null;
                            for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                                try {
                                    Event event = eventSnapshot.getValue(Event.class);
                                    if (event != null) {
                                        if (event.getTime() - 60 * 60 * 1000 > System.currentTimeMillis()) {
                                            if (mEvents.size() == 0 || !event.getWeek()
                                                    .equals(previousEventWeek)) {
                                                mEvents.add(new Event(null, null, null, event.getTime() - 60 * 60 * 1000, null, event.getWeek(),
                                                        HostEventsAdapter.TYPE_HEADER));
                                            }

                                            if (mEvents.size() == 0 || !Util.formatDate(event.getTime() - 60 * 60 * 1000)
                                                    .equals(previousEventDate)) {
                                                mEvents.add(new Event(null, null, null, event.getTime() - 60 * 60 * 1000,
                                                        null, "Date", HostEventsAdapter.TYPE_DATE));
                                            }

                                            Util.Log("Add event: " + event.getTeamHome().getName() + ", " + (event.getTime() - 60 * 60 * 1000));
                                            previousEventWeek = event.getWeek();
                                            previousEventDate = Util.formatDate(event.getTime() - 60 * 60 * 1000);

                                            mEvents.add(event);
                                        }
                                    }
                                } catch (Exception e) {
                                    // Can't add event
                                }
                            }
                        }
                        if (mAdapter != null) {
                            mAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void initGoogleClient() {
        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    private void initPrices() {
        mSquarePriceEditTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (mQuarterPricesEnabled == TextUtils.isEmpty(mSquarePriceEditTxt.getText()
                        .toString())) {
                    mQuarterPricesEnabled = !TextUtils.isEmpty(mSquarePriceEditTxt.getText()
                            .toString());

                    if (!mQuarterPricesEnabled) {
                        // Reset all text fields
                        mQuarter1PriceEditTxt.setText("-");
                        mQuarter2PriceEditTxt.setText("-");
                        mQuarter3PriceEditTxt.setText("-");
                        mFinalPriceEditTxt.setText("-");
                        mTotalPriceTxt.setText("-");
                    } else {
                        setDefaultPrices();
                    }

                    // Enable or disable text fields depending on a square price
                    mQuarter1PriceEditTxt.setEnabled(mQuarterPricesEnabled);
                    mQuarter2PriceEditTxt.setEnabled(mQuarterPricesEnabled);
                    mQuarter3PriceEditTxt.setEnabled(mQuarterPricesEnabled);
                    mFinalPriceEditTxt.setEnabled(mQuarterPricesEnabled);
                } else {
                    setDefaultPrices();
                }
            }

            private void setDefaultPrices() {
                int totalPrice = Integer.valueOf(mSquarePriceEditTxt.getText().toString())
                        * 100;
                mQuarter1PriceEditTxt.setText(String.valueOf((int) (totalPrice * 0.2)));
                mQuarter2PriceEditTxt.setText(String.valueOf((int) (totalPrice * 0.2)));
                mQuarter3PriceEditTxt.setText(String.valueOf((int) (totalPrice * 0.2)));
                mFinalPriceEditTxt.setText(String.valueOf((int) (totalPrice * 0.4)));
                mTotalPriceTxt.setText(String.valueOf(totalPrice));
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        mQuarter1PriceEditTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!mListenToFieldChanges) return;

                String text = mQuarter1PriceEditTxt.getText().toString();
                if (TextUtils.isEmpty(text)) {
                    mQuarter1PriceEditTxt.setText("0");
                    mQuarter1PriceEditTxt.setSelection(1);
                } else if (text.length() > 1 && text.startsWith("0")) {
                    mQuarter1PriceEditTxt.setText(text.substring(1, text.length()));
                } else if (text.length() == 1) {
                    mQuarter1PriceEditTxt.setSelection(1);
                }

                String squarePrice = mSquarePriceEditTxt.getText().toString();
                int totalPrice = TextUtils.isEmpty(squarePrice) ? 0 : Integer.parseInt(squarePrice)
                        * 100;
                try {
                    if (!TextUtils.isEmpty(text) && Integer.valueOf(mQuarter1PriceEditTxt.getText()
                            .toString()) > totalPrice) {
                        mQuarter1PriceEditTxt.setText(String.valueOf(totalPrice));
                        mQuarter1PriceEditTxt.setSelection(mQuarter1PriceEditTxt.getText().length());
                    }
                } catch (Exception e) {
                    mQuarter1PriceEditTxt.setText("0");
                }

                updateOtherPrices(mQuarter1PriceEditTxt);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        mQuarter2PriceEditTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!mListenToFieldChanges) return;

                String text = mQuarter2PriceEditTxt.getText().toString();
                if (TextUtils.isEmpty(text)) {
                    mQuarter2PriceEditTxt.setText("0");
                    mQuarter2PriceEditTxt.setSelection(1);
                } else if (text.length() > 1 && text.startsWith("0")) {
                    mQuarter2PriceEditTxt.setText(text.substring(1, text.length()));
                } else if (text.length() == 1) {
                    mQuarter2PriceEditTxt.setSelection(1);
                }

                String squarePrice = mSquarePriceEditTxt.getText().toString();
                int totalPrice = TextUtils.isEmpty(squarePrice) ? 0 : Integer.parseInt(squarePrice)
                        * 100;
                try {
                    if (!TextUtils.isEmpty(text) && Integer.valueOf(mQuarter2PriceEditTxt.getText()
                            .toString()) > totalPrice) {
                        mQuarter2PriceEditTxt.setText(String.valueOf(totalPrice));
                        mQuarter2PriceEditTxt.setSelection(mQuarter2PriceEditTxt.getText().length());
                    }
                } catch (Exception e) {
                    mQuarter2PriceEditTxt.setText("0");
                }

                updateOtherPrices(mQuarter2PriceEditTxt);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        mQuarter3PriceEditTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!mListenToFieldChanges) return;

                String text = mQuarter3PriceEditTxt.getText().toString();
                if (TextUtils.isEmpty(text)) {
                    mQuarter3PriceEditTxt.setText("0");
                    mQuarter3PriceEditTxt.setSelection(1);
                } else if (text.length() > 1 && text.startsWith("0")) {
                    mQuarter3PriceEditTxt.setText(text.substring(1, text.length()));
                } else if (text.length() == 1) {
                    mQuarter3PriceEditTxt.setSelection(1);
                }

                String squarePrice = mSquarePriceEditTxt.getText().toString();
                int totalPrice = TextUtils.isEmpty(squarePrice) ? 0 : Integer.parseInt(squarePrice)
                        * 100;
                try {
                    if (!TextUtils.isEmpty(text) && Integer.valueOf(mQuarter3PriceEditTxt.getText()
                            .toString()) > totalPrice) {
                        mQuarter3PriceEditTxt.setText(String.valueOf(totalPrice));
                        mQuarter3PriceEditTxt.setSelection(mQuarter3PriceEditTxt.getText().length());
                    }
                } catch (Exception e) {
                    mQuarter3PriceEditTxt.setText("0");
                }

                updateOtherPrices(mQuarter3PriceEditTxt);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        mFinalPriceEditTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!mListenToFieldChanges) return;

                String text = mFinalPriceEditTxt.getText().toString();
                if (TextUtils.isEmpty(text)) {
                    mFinalPriceEditTxt.setText("0");
                    mFinalPriceEditTxt.setSelection(1);
                } else if (text.length() > 1 && text.startsWith("0")) {
                    mFinalPriceEditTxt.setText(text.substring(1, text.length()));
                } else if (text.length() == 1) {
                    mFinalPriceEditTxt.setSelection(1);
                }

                String squarePrice = mSquarePriceEditTxt.getText().toString();
                int totalPrice = TextUtils.isEmpty(squarePrice) ? 0 : Integer.parseInt(squarePrice)
                        * 100;
                try {
                    if (!TextUtils.isEmpty(text) && Integer.valueOf(mFinalPriceEditTxt.getText()
                            .toString()) > totalPrice) {
                        mFinalPriceEditTxt.setText(String.valueOf(totalPrice));
                        mFinalPriceEditTxt.setSelection(mFinalPriceEditTxt.getText().length());
                    }
                } catch (Exception e) {
                    mFinalPriceEditTxt.setText("0");
                }

                updateOtherPrices(mFinalPriceEditTxt);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        mSquareLimitEditTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String limitTxt = mSquareLimitEditTxt.getText().toString();
                if (TextUtils.isEmpty(limitTxt)) return;
                int limit = Integer.parseInt(limitTxt);
                if (limit > 100) {
                    mSquareLimitEditTxt.setText("100");
                    mSquareLimitEditTxt.setSelection(3);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }

    private void updateOtherPrices(EditText editText) {
        String squarePrice = mSquarePriceEditTxt.getText().toString();
        int totalPrice = TextUtils.isEmpty(squarePrice) ? 0 : Integer.parseInt(squarePrice)
                * 100;
        int quarter1Price = Integer.parseInt(mQuarter1PriceEditTxt.getText().toString());
        int quarter2Price = Integer.parseInt(mQuarter2PriceEditTxt.getText().toString());
        int quarter3Price = Integer.parseInt(mQuarter3PriceEditTxt.getText().toString());
        int finalPrice = Integer.parseInt(mFinalPriceEditTxt.getText().toString());

        mListenToFieldChanges = false;
        if (mQuarter1PriceEditTxt == editText) {
            mQuarter2PriceEditTxt.setText(String.valueOf(totalPrice - quarter1Price - quarter3Price
                    - finalPrice));
            updateAdditionalPrices(mQuarter2PriceEditTxt);
        } else if (mQuarter2PriceEditTxt == editText) {
            mQuarter3PriceEditTxt.setText(String.valueOf(totalPrice - quarter1Price - quarter2Price
                    - finalPrice));
            updateAdditionalPrices(mQuarter3PriceEditTxt);
        } else if (mQuarter3PriceEditTxt == editText) {
            mFinalPriceEditTxt.setText(String.valueOf(totalPrice - quarter1Price - quarter2Price
                    - quarter3Price));
            updateAdditionalPrices(mFinalPriceEditTxt);
        } else if (mFinalPriceEditTxt == editText) {
            mQuarter1PriceEditTxt.setText(String.valueOf(totalPrice - quarter2Price - quarter3Price
                    - finalPrice));
            updateAdditionalPrices(mQuarter1PriceEditTxt);
        }

        mListenToFieldChanges = true;
    }

    private void updateAdditionalPrices(EditText editTxt) {
        if (Integer.valueOf(editTxt.getText().toString()) < 0) {
            editTxt.setText("0");
            updateOtherPrices(editTxt);
        }
    }

    private void initId() {
        mGameId = Util.generateGameId();
        mIdTxt.setText(mGameId);
    }

    private void initUser() {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            mAuthorId = firebaseUser.getUid();
            mAuthorEmail = firebaseUser.getEmail();
            mAuthorName = Util.getDisplayName();
            mAuthorImage = Util.getPhoto();
        }
    }

    private void initStorage() {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        // Create a storage reference from our app
        StorageReference storageRef = storage.getReferenceFromUrl
                ("gs://contender-3ef7d.appspot.com");

        // Create a reference to the photo
        mImageRef = storageRef.child(mGameId + ".jpg");
    }

    private void initDatabase() {
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    public void onBackButtonClicked(View view) {
        finish();
    }

    public void onCreateButtonClicked(View view) {
        readFieldValues();

        if (mProgressBar.getVisibility() == View.VISIBLE) {
            Toast.makeText(this, "Image is still loading", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(mName) || TextUtils.isEmpty(mPassword) || mSquarePrice == -1) {
            Toast.makeText(this, "Some fields are empty", Toast.LENGTH_SHORT).show();
        } else if (mPassword.length() < MIN_PASSWORD_LENGTH) {
            Toast.makeText(this, "Password is too short", Toast.LENGTH_SHORT).show();
        } else if (mSquaresLimit == 0) {
            Toast.makeText(this, "Squares limit is empty", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(mEventId)) {
            Toast.makeText(this, "Choose an upcoming game", Toast.LENGTH_SHORT).show();
        } else if (mFirstFreeGame) {
            FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance()
                    .getCurrentUser().getUid()).child("free_first_game").setValue(false);
            createGame();
        } else if (BuildConfig.DEBUG) {
            createGame();
        } else if (TextUtils.isEmpty(mCode)) {
            if (mService != null) {
                try {
                    Bundle buyIntentBundle = mService.getBuyIntent(3, getPackageName(),
                            "host_game", "inapp", "");
                    PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
                    assert pendingIntent != null;
                    startIntentSenderForResult(pendingIntent.getIntentSender(),
                            1001, new Intent(), 0, 0, 0);
                } catch (Exception e) {
                    Util.Log("exception: " + e);
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(this, "Can't pay to host the game right now", Toast.LENGTH_SHORT).show();
            }
        } else {
            OkHttpClient client = new OkHttpClient();

            RequestBody formBody = new FormBody.Builder()
                    .add("text", mCode)
                    .build();
            Request request = new Request.Builder()
                    .url(Util.SET_CODE_EXPIRED_URL)
                    .post(formBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Util.Log("Can't set code as expired: " + e);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) return;

                    String responseTxt = response.body().string();
                    if (responseTxt.equals("success")) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                createGame();
                            }
                        });
                    }
                }
            });
        }
    }

    private void readFieldValues() {
        mName = mNameEditTxt.getText().toString();
        mPassword = mPasswordEditTxt.getText().toString();
        String squarePrice = mSquarePriceEditTxt.getText().toString();
        if (!TextUtils.isEmpty(squarePrice)) {
            mSquarePrice = Integer.valueOf(squarePrice);
        } else {
            mSquarePrice = -1;
        }
        String quarter1Price = mQuarter1PriceEditTxt.getText().toString();
        if (!TextUtils.isEmpty(quarter1Price)) {
            mQuarter1Price = Integer.valueOf(quarter1Price);
        } else {
            mQuarter1Price = -1;
        }
        String quarter2Price = mQuarter2PriceEditTxt.getText().toString();
        if (!TextUtils.isEmpty(quarter2Price)) {
            mQuarter2Price = Integer.valueOf(quarter2Price);
        } else {
            mQuarter2Price = -1;
        }
        String quarter3Price = mQuarter3PriceEditTxt.getText().toString();
        if (!TextUtils.isEmpty(quarter3Price)) {
            mQuarter3Price = Integer.valueOf(quarter3Price);
        } else {
            mQuarter3Price = -1;
        }
        String finalPrice = mFinalPriceEditTxt.getText().toString();
        if (!TextUtils.isEmpty(finalPrice)) {
            mFinalPrice = Integer.valueOf(finalPrice);
        } else {
            mFinalPrice = -1;
        }
        String totalPrice = mTotalPriceTxt.getText().toString();
        if (!TextUtils.isEmpty(totalPrice)) {
            mTotalPrice = Integer.valueOf(totalPrice);
        } else {
            mTotalPrice = -1;
        }
        String squaresLimit = mSquareLimitEditTxt.getText().toString();
        if (!TextUtils.isEmpty(squaresLimit)) {
            mSquaresLimit = Integer.valueOf(squaresLimit);
        } else {
            mSquaresLimit = 100;
        }

    }

    private void uploadData(String imageUrl) {
        mDatabase.child("games").child(mGameId).setValue(new GameInvite.Game(mEventId, mGameId, mName,
                System.currentTimeMillis(), imageUrl, "100/100", new Player(mAuthorId, null,
                mAuthorEmail, mAuthorName, mAuthorImage), mPassword, mSquarePrice, mQuarter1Price,
                mQuarter2Price, mQuarter3Price, mFinalPrice, mTotalPrice, mLatitude, mLongitude,
                new ArrayList<Player>(), Util.generateBoardNumbers(), Util.generateBoardNumbers(),
                new ArrayList<SelectedSquare>(), null, null, null, null, false, "", mCode,
                mSquaresLimit))
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        createEmptyCellsReminder();

                        mProgressDialog.cancel();
                        startActivity(new Intent(HostActivity.this, GameBoardActivity.class)
                                .putExtra(GameBoardActivity.EXTRA_GAME_ID, mGameId));

                        finish();
                    }
                });
    }

    private void uploadImage() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        byte[] data = outputStream.toByteArray();

        UploadTask uploadTask = mImageRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                Util.Log("File uploading failure: " + exception);

                Toast.makeText(HostActivity.this, "Can't upload image", Toast.LENGTH_LONG).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                uploadData(taskSnapshot.getDownloadUrl() + "");
            }
        });
    }

    public void onImageButtonClicked(View view) {
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
                        .centerCrop().fit().into(mGameImg);

                mProgressBar.setVisibility(View.VISIBLE);
                Picasso.with(this).load(data.getData()).centerCrop().resize(GAME_IMAGE_SIZE_PX,
                        GAME_IMAGE_SIZE_PX).into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        mBitmap = bitmap;
                        mProgressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onBitmapFailed(Drawable errorDrawable) {
                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {
                    }
                });
            }
        } else if (requestCode == 1001) {
            String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");

            if (resultCode == RESULT_OK) {
                try {
                    JSONObject jo = new JSONObject(purchaseData);
                    String sku = jo.getString("productId");
                    final String token = jo.getString("purchaseToken");
                    if (sku.equals("host_game")) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    mService.consumePurchase(3, getPackageName(), token);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            createGame();
                                        }
                                    });
                                } catch (RemoteException e) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(HostActivity.this, "Purchase failed",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }
                        }).start();
                    }
                } catch (JSONException e) {
                    Toast.makeText(this, "Purchase failed", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        }
    }

    private void createGame() {
        mProgressDialog = ProgressDialog.show(this, "Creating game...", null, false);
        mProgressDialog.show();

        if (mBitmap != null) {
            uploadImage();
        } else {
            uploadData(null);
        }
    }

    private void createEmptyCellsReminder() {
        // Remember the time of the event
        JSONArray emptyCellReminderTimes = Util.getEmptyCellReminderTimes();
        try {
            emptyCellReminderTimes.put(new JSONObject()
                    .put("id", mGameId)
                    .put("time", mEventTime)
                    .put("name", mName)
                    .toString());
        } catch (Exception e) {
            Util.Log("Can't add a new game");
        }
        Util.setEmptyCellReminderTimes(emptyCellReminderTimes);

        // Don't disable the boot receiver
        ComponentName receiver = new ComponentName(this, BootReceiver.class);
        PackageManager pm = getPackageManager();
        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }

    /**
     * Required for the calligraphy library.
     */
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                    (this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        LOCATION_PERMISSION_CODE);
            } else {
                Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(
                        mGoogleApiClient);
                if (lastLocation != null) {
                    mLatitude = lastLocation.getLatitude();
                    mLongitude = lastLocation.getLongitude();
                }
            }
        } else {
            Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);
            if (lastLocation != null) {
                mLatitude = lastLocation.getLatitude();
                mLongitude = lastLocation.getLongitude();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERMISSION_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0]
                        == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_COARSE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(
                            mGoogleApiClient);
                    if (lastLocation != null) {
                        mLatitude = lastLocation.getLatitude();
                        mLongitude = lastLocation.getLongitude();
                    }
                }
            }
        }
    }

    public void onChooseGameClicked(View view) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this, R.style.MaterialDialog);
        dialogBuilder.setTitle("Choose a game");
        @SuppressLint("InflateParams")
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_events, null);
        RecyclerView eventsRecycler = (RecyclerView) dialogView.findViewById(R.id.events_recycler);
        eventsRecycler.setLayoutManager(new LinearLayoutManager(this));
        eventsRecycler.setHasFixedSize(true);
        mAdapter = new HostEventsAdapter(this, mEvents);
        eventsRecycler.setAdapter(mAdapter);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setNegativeButton("Cancel", null);
        mEventsDialog = dialogBuilder.create();
        mEventsDialog.show();
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @SuppressLint("SetTextI18n")
    public void setSelectedEvent(Event event) {
        mEventId = event.getId();
        mEventTime = event.getTime();
        mEventsDialog.cancel();

        try {
            eventTxt.setText(event.getTeamAway().getName() + " — " + event.getTeamHome().getName()
                    + ", " + Util.formatTime(event.getTime()));
        } catch (Exception e) {
            // Event doesn't exist
        }
    }
}
