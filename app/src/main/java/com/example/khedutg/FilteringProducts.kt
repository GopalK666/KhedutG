package com.example.khedutg

import android.widget.Filter
import com.example.khedutg.adapter.AdapterProduct
import com.example.khedutg.model.Product

class FilteringProducts(
    private val adapter: AdapterProduct,
    private val originalList: List<Product>
) : Filter() {

    override fun performFiltering(constraint: CharSequence?): FilterResults {
        val filteredList = ArrayList<Product>()
        val query = constraint?.toString()?.trim()?.lowercase() ?: ""

        if (query.isEmpty()) {
            filteredList.addAll(originalList)
        } else {
            for (product in originalList) {
                val title = product.productTitle?.lowercase() ?: ""
                val category = product.productCategory?.lowercase() ?: ""
                val type = product.productType?.lowercase() ?: ""

                if (title.contains(query) || category.contains(query) || type.contains(query)) {
                    filteredList.add(product)
                }
            }
        }

        val results = FilterResults()
        results.values = filteredList
        results.count = filteredList.size
        return results
    }

    override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
        val resultList = results?.values as? List<Product> ?: emptyList()
        adapter.differ.submitList(resultList)
    }
}

