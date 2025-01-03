package com.learning.billbuddy.adapters;

import com.learning.billbuddy.R;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ItemInExpenseAdapter extends RecyclerView.Adapter<ItemInExpenseAdapter.ViewHolder> {

//    private List<Person> personList;

//    public ItemInExpenseAdapter(List<Person> personList) {
//        this.personList = personList;
//    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_in_expense, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
//        Person person = personList.get(position);
//        holder.personNameTextView.setText(person.getName());
//        holder.personAmountTextView.setText(person.getAmount());
        // ... (Set other views based on your Person object)

        // Handle CheckBox clicks if needed
        holder.checkBox.setOnClickListener(v -> {
            // Update the Person object's selected state
//                person.setSelected(holder.checkBox.isChecked());
            // ... (Handle the selection, e.g., update total amount)
        });
    }

    @Override
    public int getItemCount() {
//        return personList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;
        TextView personNameTextView;
        TextView personAmountTextView;
        // ... (Add other views from your item layout)

        public ViewHolder(View view) {
            super(view);
            checkBox = view.findViewById(R.id.item_in_expense_checkbox);
            personNameTextView = view.findViewById(R.id.item_in_expense_person_name);
            personAmountTextView = view.findViewById(R.id.item_in_expense_person_amount);
            // ... (Initialize other views)
        }
    }
}
