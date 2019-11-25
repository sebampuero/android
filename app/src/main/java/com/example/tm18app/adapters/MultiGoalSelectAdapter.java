package com.example.tm18app.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tm18app.R;
import com.example.tm18app.pojos.Goal;
import com.example.tm18app.pojos.GoalItemSelection;

import java.util.ArrayList;

public class MultiGoalSelectAdapter extends RecyclerView.Adapter<MultiGoalSelectAdapter.ViewHolder> {

    private Context appContext;
    private ArrayList<GoalItemSelection> goals;

    public MultiGoalSelectAdapter(Context appContext, ArrayList<GoalItemSelection> goals) {
        this.appContext = appContext;
        this.goals = goals;
    }

    public MultiGoalSelectAdapter(Context appContext){
        this.appContext = appContext;
    }

    public void setGoals(ArrayList<GoalItemSelection> goals) {
        this.goals = new ArrayList<>();
        this.goals = goals;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(appContext).inflate(R.layout.goals_listview, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(goals.get(position));
    }

    @Override
    public int getItemCount() {
        return goals.size();
    }

    public ArrayList<GoalItemSelection> getAll() {
        return goals;
    }

    public ArrayList<GoalItemSelection> getSelected() {
        ArrayList<GoalItemSelection> selected = new ArrayList<>();
        for(int i = 0; i < goals.size(); i++){
            if(goals.get(i).isChecked()){
                selected.add(goals.get(i));
            }
        }
        return selected;
    }

    public void setSelected(String[] selectedGoals){
        for(GoalItemSelection goalItem : goals){
            for(int i = 0; i < selectedGoals.length; i++){
                if(goalItem.getTag().equals(selectedGoals[i]))
                    goalItem.setChecked(true);
            }
        }
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        private CheckBox checkBox;
        private TextView tv;

        ViewHolder(View item) {
            super(item);
            tv = item.findViewById(R.id.goalTv);
            checkBox = item.findViewById(R.id.checkBox);
        }

        void bind(final GoalItemSelection goal){
            checkBox.setChecked(goal.isChecked());
            tv.setText(goal.getTag());

            checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    goal.setChecked(!goal.isChecked());
                    checkBox.setChecked(goal.isChecked());
                }
            });
        }
    }
}
