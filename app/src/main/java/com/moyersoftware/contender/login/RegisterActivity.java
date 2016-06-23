package com.moyersoftware.contender.login;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.moyersoftware.contender.R;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overrideActivityAnimation();
        setContentView(R.layout.activity_register);

        initStatusBar();
    }

    /**
     * Creates a cross fade effect between loading and registration screens.
     */
    private void overrideActivityAnimation() {
        overridePendingTransition(R.anim.activity_fade_in, R.anim.activity_fade_hold);
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
}
