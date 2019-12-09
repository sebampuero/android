package com.example.tm18app.viewModels;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.navigation.NavController;

import com.example.tm18app.constants.Constant;
import com.example.tm18app.R;
import com.example.tm18app.adapters.MultiGoalSelectAdapter;
import com.example.tm18app.network.RetrofitNetworkConnectionSingleton;
import com.example.tm18app.network.UserRestInterface;
import com.example.tm18app.pojos.Goal;
import com.example.tm18app.pojos.User;
import com.example.tm18app.repository.GoalsItemRepository;
import com.example.tm18app.repository.UserRepository;
import com.example.tm18app.util.SingleLiveEvent;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;

import me.pushy.sdk.Pushy;
import me.pushy.sdk.util.exceptions.PushyException;
import retrofit2.Call;
import retrofit2.Response;

/**
 * A {@link ViewModel} class representing the ViewModel for the {@link com.example.tm18app.fragment.RegistrationFragment} View
 *
 * @author Sebastian Ampuero
 * @version 1.0
 * @since 03.12.2019
 */
public class RegisterViewModel extends ViewModel {

    public MutableLiveData<String> name = new MutableLiveData<>();
    public MutableLiveData<String> lastname = new MutableLiveData<>();
    public MutableLiveData<String> email = new MutableLiveData<>();
    public MutableLiveData<String> password = new MutableLiveData<>();
    public MutableLiveData<String> passwordConf = new MutableLiveData<>();
    public SingleLiveEvent<Boolean> triggerLoadingBtn = new SingleLiveEvent<>();

    private Context ctx;
    private MultiGoalSelectAdapter adapter;
    private LiveData<List<Goal>> goalItemsLiveData;
    private LiveData<HashMap<Integer, User>> userLiveData = new MutableLiveData<>();


    public RegisterViewModel(){
    }

    /**
     * Getter for the goals list {@link LiveData}
     * @return {@link LiveData}
     */
    public LiveData<List<Goal>> getGoalLiveData() {
        return goalItemsLiveData;
    }

    /**
     * Getter for the user {@link LiveData} response status
     * @return {@link LiveData}
     */
    public LiveData<HashMap<Integer, User>> getUserLiveData(){
        return userLiveData;
    }

    /**
     * Calls repository and fetches goals from the server
     */
    private void fetchGoals() {
        GoalsItemRepository goalsItemRepository = new GoalsItemRepository();
        this.goalItemsLiveData = goalsItemRepository.getGoals();
    }

    /**
     * Sets the {@link Context} for this ViewModel
     * @param context {@link Context}
     */
    public void setContext(Context context) {
        this.ctx = context;
        fetchGoals();
    }

    /**
     * Event method for when the register button is pressed
     */
    public void onRegister(){
        if(isRegisterValid()){
            User user = new User();
            user.setName(name.getValue());
            user.setLastname(lastname.getValue());
            user.setEmail(email.getValue());
            user.setPassword(password.getValue());
            Integer[] goalIds = new Integer[this.adapter.getSelected().size()];
            String[] goalTags = new String[this.adapter.getSelected().size()];
            for(int i = 0; i < this.adapter.getSelected().size(); i++){
                goalIds[i] = this.adapter.getSelected().get(i).getId();
                goalTags[i] = this.adapter.getSelected().get(i).getTag();
             }
             user.setGoals(goalIds);
             user.setGoalTags(goalTags);
            UserRepository userRepository = new UserRepository();
            // pass MutableLiveData to the repository to change for when status of response updates
             userRepository.registerUser(user, (MutableLiveData<HashMap<Integer, User>>) userLiveData, this.ctx);
            triggerLoadingBtn.call();
        }
    }

    /**
     * Checks whether the input fields for the registration are valid
     * @return true if valid, false otherwise
     */
    private boolean isRegisterValid() {
        if(name.getValue() == null
                || lastname.getValue() == null
                || password.getValue() == null
                || passwordConf.getValue() == null
                || email.getValue() == null){
            Toast.makeText(ctx, ctx.getString(R.string.empty_fields), Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(name.getValue().trim().equals("")
                || lastname.getValue().trim().equals("")
                || password.getValue().trim().equals("")
                || passwordConf.getValue().trim().equals("")
                || email.getValue().trim().equals("")){
            Toast.makeText(ctx, ctx.getString(R.string.empty_fields), Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(!passwordConf.getValue().equals(password.getValue())){
            Toast.makeText(ctx, ctx.getString(R.string.pass_dont_match), Toast.LENGTH_SHORT).show();
            return false;
        }else if(!email.getValue().contains("@")){ // vague verification, dont use in production
            Toast.makeText(ctx, ctx.getString(R.string.invalid_email), Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    /**
     * Sets the {@link MultiGoalSelectAdapter} for this ViewModel
     * @param adapter {@link MultiGoalSelectAdapter}
     */
    public void setGoalsAdapter(MultiGoalSelectAdapter adapter) {
        this.adapter = adapter;
    }

}
