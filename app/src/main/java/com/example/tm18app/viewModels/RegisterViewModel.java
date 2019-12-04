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

import retrofit2.Call;
import retrofit2.Response;

public class RegisterViewModel extends ViewModel {

    public MutableLiveData<String> name = new MutableLiveData<>();
    public MutableLiveData<String> lastname = new MutableLiveData<>();
    public MutableLiveData<String> email = new MutableLiveData<>();
    public MutableLiveData<String> password = new MutableLiveData<>();
    public MutableLiveData<String> passwordConf = new MutableLiveData<>();
    public LiveData<List<Goal>> getGoalLiveData() {
        return goalItemsLiveData;
    }
    public LiveData<HashMap<Integer, User>> getUserLiveData(){
        return userLiveData;
    }
    public SingleLiveEvent<Boolean> triggerLoadingBtn = new SingleLiveEvent<>();

    private Context ctx;
    private MultiGoalSelectAdapter adapter;
    private LiveData<List<Goal>> goalItemsLiveData;
    private LiveData<HashMap<Integer, User>> userLiveData = new MutableLiveData<>();


    public RegisterViewModel(){
    }

    private void fetchGoals() {
        GoalsItemRepository goalsItemRepository = new GoalsItemRepository();
        this.goalItemsLiveData = goalsItemRepository.getGoals();
    }

    public void setContext(Context context) {
        this.ctx = context;
        fetchGoals();
    }

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
             userRepository.registerUser(user, (MutableLiveData<HashMap<Integer, User>>) userLiveData);
            triggerLoadingBtn.call();
        }
    }


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
        }else if(!email.getValue().contains("@")){
            Toast.makeText(ctx, ctx.getString(R.string.invalid_email), Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    public void setGoalsAdapter(MultiGoalSelectAdapter adapter) {
        this.adapter = adapter;
    }

}
