package ru.ecom42.tssrider.common.components;

import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.transition.Fade;
import androidx.transition.TransitionInflater;

import org.greenrobot.eventbus.EventBus;

import ru.ecom42.tssrider.R;

public class BaseActivity extends AppCompatActivity {
    public ActionBar toolbar;
    public float screenDensity;
    private boolean isImmersive = false;
    public EventBus eventBus = EventBus.getDefault();
    public boolean registerEventBus = true;
    public boolean isInForeground = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
//        if(registerEventBus)
//            eventBus = EventBus.getDefault();
        screenDensity = getApplicationContext().getResources().getDisplayMetrics().density;
    }

    public void initializeToolbar(String title) {
        Toolbar toolbarView = findViewById(R.id.toolbar);
        setSupportActionBar(toolbarView);
        toolbar = getSupportActionBar();
        if (toolbar != null) {
            toolbar.setDisplayHomeAsUpEnabled(true);
            toolbar.setTitle(title);
            toolbarView.setNavigationOnClickListener(v -> onBackPressed());
        }
    }

    public int getAccentColor() {
        TypedValue typedValue = new TypedValue();

        TypedArray a = this.obtainStyledAttributes(typedValue.data, new int[] { R.attr.colorAccent });
        int color = a.getColor(0, 0);

        a.recycle();

        return color;
    }

    public int getPrimaryColor() {
        TypedValue typedValue = new TypedValue();

        TypedArray a = this.obtainStyledAttributes(typedValue.data, new int[] { R.attr.colorPrimary });
        int color = a.getColor(0, 0);

        a.recycle();

        return color;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus & isImmersive) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    @Override
    public void onStart() {
        if (registerEventBus)
            eventBus.register(this);
        super.onStart();

    }

    @Override
    public void onStop() {
        if (registerEventBus)
            eventBus.unregister(this);
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isInForeground = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        isInForeground = false;

    }

    public int convertDPToPixel(int dp) {
        return (int) (dp * (screenDensity));
    }

    @Override
    public void setImmersive(boolean immersive) {
        isImmersive = immersive;
    }
}
