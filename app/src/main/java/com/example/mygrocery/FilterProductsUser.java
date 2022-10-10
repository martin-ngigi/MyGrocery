package com.example.mygrocery;

import android.widget.Filter;

import com.example.mygrocery.Adapters.AdapterProductSeller;
import com.example.mygrocery.Adapters.AdapterProductUser;
import com.example.mygrocery.Models.ModelProduct;

import java.util.ArrayList;

public class FilterProductsUser extends Filter {

    private AdapterProductUser adapter;
    private ArrayList<ModelProduct> filterList;

    public FilterProductsUser(AdapterProductUser adapter, ArrayList<ModelProduct> filterList) {
        this.adapter = adapter;
        this.filterList = filterList;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();
        //validate data for search query
        if (constraint != null && constraint.length()>0){
            //search filed mot empty, searching something, perform search

            //change to upperCase to make case sensitive
            constraint = constraint.toString().toUpperCase();
            //store our filtered list
            ArrayList<ModelProduct> filteredModels = new ArrayList<>();
            for (int i=00; i<filterList.size(); i++){
                //check, search by title or category
                if (filterList.get(i).getProductTitle().toUpperCase().contains(constraint) ||
                        filterList.get(i).getProductTitle().toUpperCase().contains(constraint)){
                    //add filtered data to list
                    filteredModels.add(filterList.get(i));

                }
            }

            results.count = filteredModels.size();
            results.values = filteredModels;
        }
        else {
            //search filed empty, not searching something, return original/all/complete list

            results.count = filterList.size();
            results.values = filterList;

        }


        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        adapter.productsList =  (ArrayList<ModelProduct>) results.values;
        //refresh adapter
        adapter.notifyDataSetChanged();

    }
}
