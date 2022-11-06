package com.example.bookapp.filters;

import android.widget.Filter;

import com.example.bookapp.adapters.AdapterCategory;
import com.example.bookapp.models.ModelCategory;

import java.util.ArrayList;

public class FilterCategory extends Filter {

    //ArrayList in which we want to search
    ArrayList<ModelCategory> filterList;
    //adapter in which filter need to be implemented
    AdapterCategory adapterCategory;

    //constructor
    public FilterCategory(ArrayList<ModelCategory> filterList, AdapterCategory adapterCategory) {
        this.filterList = filterList;
        this.adapterCategory = adapterCategory;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();
        //value shouldn't be null and empty
        if (constraint != null && constraint.length() > 0) {

            //change to upper case, or lower case to avoid case sensitivity
            constraint = constraint.toString().toUpperCase();
            ArrayList<ModelCategory> filteredModels = new ArrayList<>();

            for (int i = 0; i < filterList.size(); i++) {
                //validate
                if (filterList.get(i).getCategory().toUpperCase().contains(constraint)) {
                    //add to filtered list
                    filteredModels.add(filterList.get(i));
                }
            }
            results.count = filteredModels.size();
            results.values = filteredModels;
        } else {
            results.count = filterList.size();
            results.values = filterList;
        }
        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        //apply filter changes
        adapterCategory.categoryArrayList = (ArrayList<ModelCategory>) results.values;

        //notify changes
        adapterCategory.notifyDataSetChanged();
    }
}
