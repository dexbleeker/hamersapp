package nl.ecci.hamers.news

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import kotlinx.android.synthetic.main.news_row.view.*
import nl.ecci.hamers.MainActivity
import nl.ecci.hamers.R
import java.util.*

internal class NewsAdapter(private val dataSet: ArrayList<News>) : RecyclerView.Adapter<NewsAdapter.ViewHolder>(), Filterable {

    private var filteredDataSet: ArrayList<News> = dataSet

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.news_row, parent, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindNews(filteredDataSet[position])
    }

    override fun getItemCount(): Int {
        return filteredDataSet.size
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence?): Filter.FilterResults {
                val results = Filter.FilterResults()

                //If there's nothing to filter on, return the original data for your list
                if (charSequence == null || charSequence.isEmpty()) {
                    results.values = dataSet
                    results.count = dataSet.size
                } else {
                    val filterResultsData = dataSet.filter {
                        it.title.toLowerCase().contains(charSequence)
                                || it.body.toLowerCase().contains(charSequence)
                    }
                    results.values = filterResultsData
                    results.count = filterResultsData.size
                }
                return results
            }

            override fun publishResults(charSequence: CharSequence, filterResults: Filter.FilterResults) {
                filteredDataSet = filterResults.values as ArrayList<News>
                notifyDataSetChanged()
            }
        }
    }

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        fun bindNews(news: News) {
            with(news) {
                itemView.newsItem_title.text = news.title
                itemView.newsItem_body.text = news.body
                itemView.newsItem_date.text = MainActivity.appDF2.format(date)
            }
        }
    }
}