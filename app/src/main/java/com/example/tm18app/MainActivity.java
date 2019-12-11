package com.example.tm18app;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.NavDeepLinkBuilder;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.tm18app.constants.Constant;
import com.example.tm18app.databinding.ActivityMainBinding;
import com.example.tm18app.fragment.FeedFragment;
import com.example.tm18app.fragment.ProfileFragment;
import com.example.tm18app.repository.PostItemRepository;
import com.example.tm18app.viewModels.MyViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.net.URL;

import me.pushy.sdk.Pushy;

/**
 * MainActivity.
 *
 * @author Sebastian Ampuero
 * @version 1.0
 * @since 03.12.2019
 */
public class MainActivity extends AppCompatActivity {

    private NavController navController;
    private Toolbar toolbar;
    private AppBarConfiguration appBarConfiguration;
    private ImageButton logoutBtn;
    private BottomNavigationView bottomNavigationView;
    private TextView toolbarTitle;


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

        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        model.setNavController( navController);
        model.setContext(this.getApplication());
        model.checkLoginStatus();

        toolbar = findViewById(R.id.toolbar);
        bottomNavigationView = findViewById(R.id.bottomNavView);
        logoutBtn = findViewById(R.id.logoutBtn);
        toolbarTitle = findViewById(R.id.toolbarTitle);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupWithNavController(bottomNavigationView, navController);
        NavigationUI.setupWithNavController(toolbar, navController, appBarConfiguration);

        navController.addOnDestinationChangedListener(new NavController.OnDestinationChangedListener() {
            @Override
            public void onDestinationChanged(@NonNull NavController controller, @NonNull NavDestination destination, @Nullable Bundle arguments) {
                if(destination.getId() == R.id.mainFragment || destination.getId() == R.id.loginFragment || destination.getId() == R.id.registrationFragment){
                    // disable toolbar where Fragments don't need it
                    toolbar.setVisibility(View.GONE);
                    bottomNavigationView.setVisibility(View.GONE);
                }else{
                    toolbar.setVisibility(View.VISIBLE);
                    bottomNavigationView.setVisibility(View.VISIBLE);
                    switch (destination.getId()){ // set corresponding title to fragments depending on nav controller location
                        case R.id.feedFragment:
                            toolbarTitle.setText(R.string.feed_toolbar_title);
                            break;
                        case R.id.profileFragment:
                            toolbarTitle.setText(R.string.profile_toolbar_title);
                            break;
                        case R.id.settingsFragment:
                            toolbarTitle.setText(R.string.settings_toolbar_title);
                            break;
                        case R.id.commentSectionFragment:
                            toolbarTitle.setText(R.string.comments_toolbar_title);
                            break;
                        case R.id.newPostFragment:
                            toolbarTitle.setText(R.string.newpost_toolbar_title);
                            break;
                    }
                }
            }
        });
        // handle logout action
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Show a dialog
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(MainActivity.this);
                alertBuilder.setCancelable(true);
                alertBuilder.setTitle(getApplicationContext().getString(R.string.log_out_alert_title));
                alertBuilder.setMessage(getApplicationContext().getString(R.string.log_out_conf_message));
                alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        SharedPreferences preferences = getSharedPreferences(Constant.USER_INFO, Context.MODE_PRIVATE);
                        SharedPreferences.Editor e = preferences.edit();
                        e.clear().apply(); // clear SharedPreferences info
                        Pushy.unregister(getApplicationContext()); // wipe user token and auth key info
                        // Navigate to the main fragment with a deep link
                        PendingIntent pendingIntent = new NavDeepLinkBuilder(getApplicationContext())
                                .setComponentName(MainActivity.class)
                                .setGraph(R.navigation.nav_graph)
                                .setDestination(R.id.mainFragment)
                                .createPendingIntent();
                        try {
                            pendingIntent.send();
                        } catch (PendingIntent.CanceledException ex) {
                            ex.printStackTrace();
                        }
                    }
                });
                AlertDialog alert = alertBuilder.create();
                alert.show();
            }
        });

    }

}
