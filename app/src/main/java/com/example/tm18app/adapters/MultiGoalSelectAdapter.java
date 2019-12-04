package com.example.tm18app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tm18app.R;
import com.example.tm18app.pojos.GoalItemSelection;

import java.util.ArrayList;

/**
 * Adapter for multigoal select dropdown that displays goal tags
 *
 * @author Sebastian Ampuero
 * @version 1.0
 * @since 03.12.2019
 */
public class MultiGoalSelectAdapter extends RecyclerView.Adapter<MultiGoalSelectAdapter.ViewHolder> {

    private ArrayList<GoalItemSelection> goals;

    public MultiGoalSelectAdapter() {
    }

    public void setGoals(ArrayList<GoalItemSelection> goals) {
        this.goals = new ArrayList<>();
        this.goals = goals;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.goals_listview, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(goals.get(position));
    }

    @Override
    public int getItemCount() {
        return (goals != null) ? goals.size() : 0;
    }

    /**
     * Searches for selected goal items in the list of goal tags
     * @return {@link ArrayList} containing the selected goal items
     */
    public ArrayList<GoalItemSelection> getSelected() {
        ArrayList<GoalItemSelection> selected = new ArrayList<>();
        if(goals != null){
            for(int i = 0; i < goals.size(); i++){
                if(goals.get(i).isChecked()){
                    selected.add(goals.get(i));
                }
            }
        }
        return selected;
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
