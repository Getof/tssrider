package ru.ecom42.tssrider.common.activities.splash;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.maps.model.LatLng;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import ru.ecom42.tssrider.BuildConfig;
import ru.ecom42.tssrider.MainActivity;
import ru.ecom42.tssrider.R;
import ru.ecom42.tssrider.common.activities.login.LoginActivity;
import ru.ecom42.tssrider.common.components.BaseActivity;
import ru.ecom42.tssrider.common.events.BackgroundServiceStartedEvent;
import ru.ecom42.tssrider.common.events.ConnectEvent;
import ru.ecom42.tssrider.common.events.ConnectResultEvent;
import ru.ecom42.tssrider.common.events.LoginEvent;
import ru.ecom42.tssrider.common.models.Rider;
import ru.ecom42.tssrider.common.utils.AlertDialogBuilder;
import ru.ecom42.tssrider.common.utils.AlerterHelper;
import ru.ecom42.tssrider.common.utils.CommonUtils;
import ru.ecom42.tssrider.common.utils.LocationHelper;
import ru.ecom42.tssrider.common.utils.MyPreferenceManager;

import ru.ecom42.tssrider.databinding.ActivitySplashBinding;
import ru.ecom42.tssrider.events.LoginResultEvent;
import ru.ecom42.tssrider.services.RiderService;

public class SplashActivity extends BaseActivity implements LocationListener {


    MyPreferenceManager SP;
    ActivitySplashBinding binding;
    private List<AuthUI.IdpConfig> providers;
    int RC_SIGN_IN = 123;
    Handler locationTimeoutHandler;
    LocationManager locationManager;
    LatLng currentLocation;

    private PermissionListener permissionlistener = new PermissionListener() {
        @Override
        public void onPermissionGranted() {
            boolean isServiceRunning = isMyServiceRunning(RiderService.class);
            if (!isServiceRunning)
                startService(new Intent(SplashActivity.this, RiderService.class));
        }

        @Override
        public void onPermissionDenied(List<String> deniedPermissions) {
            boolean isServiceRunning = isMyServiceRunning(RiderService.class);
            if (!isServiceRunning)
                startService(new Intent(SplashActivity.this, RiderService.class));
        }
    };

    private View.OnClickListener onLoginButtonClicked = v -> {
        String resourceName = "testMode";
        int testExists = SplashActivity.this.getResources().getIdentifier(resourceName, "string", SplashActivity.this.getPackageName());
        if (testExists > 0) {
            tryLogin(getString(testExists));
            return;
        }
        if (getResources().getBoolean(R.bool.use_custom_login)) {
            startActivityForResult(new Intent(SplashActivity.this, LoginActivity.class), RC_SIGN_IN);
        } else
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(
                                    Collections.singletonList(new AuthUI.IdpConfig.PhoneBuilder().build()))
                            .setTheme(R.style.LoginTheme)
                            .build(),
                    RC_SIGN_IN);
    };

    private void tryLogin(String phone) {
        binding.progressBar.setVisibility(View.VISIBLE);
        if (phone.substring(0, 1).equals("+"))
            phone = phone.substring(1);
        eventBus.post(new LoginEvent(Long.parseLong(phone), BuildConfig.VERSION_CODE));
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        setImmersive(true);
        super.onCreate(savedInstanceState, persistentState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        binding = DataBindingUtil.setContentView(SplashActivity.this, R.layout.activity_splash);
        binding.loginButton.setOnClickListener(onLoginButtonClicked);
        SP = MyPreferenceManager.getInstance(getApplicationContext());
        checkPermissions();
    }

    private void checkPermissions() {
        if (!CommonUtils.isInternetEnabled(this)) {
            AlertDialogBuilder.show(this, getString(R.string.message_enable_wifi), AlertDialogBuilder.DialogButton.CANCEL_RETRY, result -> {
                if (result == AlertDialogBuilder.DialogResult.RETRY) {
                    checkPermissions();
                } else {
                    finishAffinity();
                }
            });
            return;
        }
        TedPermission.with(this)
                .setPermissionListener(permissionlistener)
                .setDeniedMessage(getString(R.string.message_permission_denied))
                .setPermissions(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
                .check();
    }


    @Subscribe
    public void onServiceStarted(BackgroundServiceStartedEvent event) {
        tryConnect();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoginResultEvent(LoginResultEvent event) {
        if (event.hasError()) {
            event.showError(SplashActivity.this, result -> {
                if (result == AlertDialogBuilder.DialogResult.RETRY)
                    binding.loginButton.callOnClick();
                else
                    finish();
            });
            return;
        }
        CommonUtils.rider = event.rider;
        SP.putString("rider_user", event.riderJson);
        SP.putString("rider_token", event.jwtToken);
        tryConnect();
    }

    private void tryConnect() {
        String token = SP.getString("rider_token", null);
        if (token != null && !token.isEmpty()) {
            eventBus.post(new ConnectEvent(token));
        } else {
            binding.loginButton.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.GONE);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onConnectedResult(ConnectResultEvent event) {
        if (event.hasError()) {
            binding.progressBar.setVisibility(View.INVISIBLE);
            event.showError(SplashActivity.this, result -> {
                if (result == AlertDialogBuilder.DialogResult.RETRY) {
                    eventBus.post(new ConnectEvent(SP.getString("rider_token", null)));
                    binding.progressBar.setVisibility(View.VISIBLE);
                } else {
                    binding.loginButton.setVisibility(View.VISIBLE);
                }
            });
            return;
        }
        locationTimeoutHandler = new Handler();
        locationTimeoutHandler.postDelayed(() -> {
            locationManager.removeUpdates(SplashActivity.this);
            if (currentLocation == null) {
                String[] location = getString(R.string.defaultLocation).split(",");
                double lat = Double.parseDouble(location[0]);
                double lng = Double.parseDouble(location[1]);
                currentLocation = new LatLng(lat, lng);
            }
            startMainActivity(currentLocation);

        }, 5000);
        searchCurrentLocation();
        CommonUtils.rider = Rider.fromJson(SP.getString("rider_user", "{}"));
    }

    @SuppressLint("MissingPermission")
    private void searchCurrentLocation() {
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        assert locationManager != null;
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5, this);
    }

    private void startMainActivity(LatLng latLng) {
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        double[] array = LocationHelper.LatLngToDoubleArray(latLng);
        intent.putExtra("currentLocation", array);
        startActivity(intent);
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        try {
            ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            if (manager != null) {
                for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                    if (serviceClass.getName().equals(service.service.getClassName()))
                        return true;
                }
            }
        } catch (Exception exception) {
            AlertDialogBuilder.show(SplashActivity.this, exception.getMessage());
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                if (getResources().getBoolean(R.bool.use_custom_login)) {
                    if (data != null)
                        tryLogin(Objects.requireNonNull(data.getStringExtra("mobile")));
                } else {
                    IdpResponse idpResponse = IdpResponse.fromResultIntent(data);
                    String phone;
                    if (idpResponse != null) {
                        phone = idpResponse.getPhoneNumber();
                        assert phone != null;
                        tryLogin(phone);
                        return;
                    }
                }

            }
            AlerterHelper.showError(SplashActivity.this, getString(R.string.login_failed));
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
    }

}
