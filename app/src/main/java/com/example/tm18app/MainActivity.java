package com.example.tm18app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.tm18app.constants.Constant;
import com.example.tm18app.databinding.ActivityMainBinding;
import com.example.tm18app.viewModels.MyViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private NavController navController;
    private Toolbar toolbar;
    private AppBarConfiguration appBarConfiguration;
    private BottomNavigationView bottomNavigationView;
    private TextView toolbarTitle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //get a new or already existing ViewModel that survived a possible rotation
        MyViewModel model = ViewModelProviders.of(this).get(MyViewModel.class);
        //get the automatically generated binding class for MainActivity
        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        //connects the ViewModel with the binding class and it's internal objects
        binding.setMyVM(model);

        //let the binding class get notified about the activities lifecycle to be able to notify the UI
        //components when new values or initial values need to be set
        binding.setLifecycleOwner(this);

        navController = Navigation.findNavController(this, R.id.nav_host_fragment);

        //hand the navcontroller to the viewmodel for navigating
        model.setNavController( navController);
        //model.setActionBar(getSupportActionBar());
        model.setContext(this.getApplication());
        model.checkLoginStatus();
        toolbar = findViewById(R.id.toolbar);
        bottomNavigationView = findViewById(R.id.bottomNavView);
        ImageButton logoutBtn = findViewById(R.id.logoutBtn);
        toolbarTitle = findViewById(R.id.toolbarTitle);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupWithNavController(bottomNavigationView, navController);
        NavigationUI.setupWithNavController(toolbar, navController, appBarConfiguration);
        navController.addOnDestinationChangedListener(new NavController.OnDestinationChangedListener() {
            @Override
            public void onDestinationChanged(@NonNull NavController controller, @NonNull NavDestination destination, @Nullable Bundle arguments) {
                if(destination.getId() == R.id.mainFragment || destination.getId() == R.id.loginFragment || destination.getId() == R.id.registrationFragment){
                    toolbar.setVisibility(View.GONE);
                    bottomNavigationView.setVisibility(View.GONE);
                }else{
                    toolbar.setVisibility(View.VISIBLE);
                    bottomNavigationView.setVisibility(View.VISIBLE);
                    switch (destination.getId()){
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
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences preferences = getSharedPreferences(Constant.USER_INFO, Context.MODE_PRIVATE);
                SharedPreferences.Editor e = preferences.edit();
                e.clear().apply();
                switch (navController.getCurrentDestination().getId()){
                    case R.id.feedFragment:
                        navController.navigate(R.id.action_feedFragment_to_ftime_nav);
                        break;
                    case R.id.profileFragment:
                        navController.navigate(R.id.action_profileFragment_to_ftime_nav);
                        break;
                    case R.id.settingsFragment:
                        navController.navigate(R.id.action_settingsFragment_to_ftime_nav);
                        break;
                    case R.id.commentSectionFragment:
                        navController.navigate(R.id.action_commentSectionFragment_to_ftime_nav);
                        break;
                    case R.id.newPostFragment:
                        navController.navigate(R.id.action_newPostFragment_to_ftime_nav);
                        break;
                }
            }
        });

    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
}
