package com.example.tm18app;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.tm18app.databinding.ActivityMainBinding;
import com.example.tm18app.fragment.FeedFragment;
import com.example.tm18app.viewModels.MyViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import me.pushy.sdk.Pushy;

/**
 * MainActivity.
 *
 * @author Sebastian Ampuero
 * @version 1.0
 * @since 03.12.2019
 */
public class MainActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private BottomNavigationView mBottonNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Make Pushy listen for incoming MQTT Notification messages. It only works if writing and
        // reading permissions are granted
        Pushy.listen(this);
        
        // Enable or disable Wi-Fi sleep policy compliance to prevent the SDK 
        // from acquiring a wake lock in case the "Keep Wi-Fi on during sleep" device setting is set to "Never"
        Pushy.toggleWifiPolicyCompliance(true, this.getApplicationContext());

        // Ask for permissions and show explanation why
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
            alertBuilder.setCancelable(true);
            alertBuilder.setTitle(getString(R.string.permission_necessary));
            alertBuilder.setMessage(getString(R.string.permission_necessary_explanation));
            alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int which) {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
                }
            });
            AlertDialog alert = alertBuilder.create();
            alert.show();
        }

        MyViewModel model = ViewModelProviders.of(this).get(MyViewModel.class);
        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.setMyVM(model);
        binding.setLifecycleOwner(this);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        Log.e("TAG", "Setting nav controller");
        model.setNavController(navController);
        model.setContext(this.getApplication());
        model.checkLoginStatus();

        mToolbar = findViewById(R.id.toolbar);
        mBottonNavigationView = findViewById(R.id.bottomNavView);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupWithNavController(mBottonNavigationView, navController);
        NavigationUI.setupWithNavController(mToolbar, navController, appBarConfiguration);

        navController.addOnDestinationChangedListener(new NavController.OnDestinationChangedListener() {
            @Override
            public void onDestinationChanged(@NonNull NavController controller, @NonNull NavDestination destination, @Nullable Bundle arguments) {
                if(destination.getId() == R.id.mainFragment || destination.getId() == R.id.loginFragment || destination.getId() == R.id.registrationFragment){
                    // disable mToolbar where Fragments don't need it
                    mToolbar.setVisibility(View.GONE);
                    mBottonNavigationView.setVisibility(View.GONE);
                }else{
                    mToolbar.setVisibility(View.VISIBLE);
                    mBottonNavigationView.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    public Toolbar getToolbar() {
        return mToolbar;
    }

}
