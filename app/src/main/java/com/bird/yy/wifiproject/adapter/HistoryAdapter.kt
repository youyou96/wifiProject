package com.bird.yy.wifiproject.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bird.yy.wifiproject.R
import com.bird.yy.wifiproject.entity.HistoryEntity

class HistoryAdapter : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {
    private var historyEntityList = ArrayList<HistoryEntity>()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        return HistoryViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_history, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return historyEntityList.size
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        historyEntityList[position].run {
            holder.historyDate.text = date
            if (router > 1000) {
                holder.historyRouter.text = ">1000"
            } else {
                holder.historyRouter.text = router.toString()
            }
            if (totalGame > 1000) {
                holder.historyTotalGame.text = ">1000"
            } else {
                holder.historyTotalGame.text = totalGame.toString()
            }
            if (cn > 1000) {
                holder.historyCN.text = ">1000"

            } else {
                holder.historyCN.text = cn.toString()

            }
            holder.historyName.text = name
        }
    }

    fun setNewData(historyEntity: ArrayList<HistoryEntity>?) {
        val lastItemCount = itemCount
        if (lastItemCount != 0) {
            this.historyEntityList.clear()
            notifyItemRangeRemoved(0, lastItemCount)
        }
        historyEntity?.let { this.historyEntityList.addAll(it) }
        notifyItemChanged(0, itemCount)
    }

    class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val historyDate: TextView = itemView.findViewById(R.id.item_history_date)
        val historyRouter: TextView = itemView.findViewById(R.id.item_history_router)
        val historyTotalGame: TextView = itemView.findViewById(R.id.item_history_tg)
        val historyCN: TextView = itemView.findViewById(R.id.item_history_cn)
        val historyName: TextView = itemView.findViewById(R.id.item_history_name)
    }
}