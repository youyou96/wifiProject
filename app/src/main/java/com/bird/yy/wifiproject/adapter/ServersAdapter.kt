package com.bird.yy.wifiproject.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bird.yy.wifiproject.R
import com.bird.yy.wifiproject.entity.Country

class ServersAdapter : RecyclerView.Adapter<ServersAdapter.ServersViewHolder>() {
    private var countryEntityList = ArrayList<Country>()
    var itemClickListener: ItemClickListener? = null

    class ServersViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val seversSrc: ImageView = itemView.findViewById(R.id.item_servers_src)
        val serversTv: TextView = itemView.findViewById(R.id.item_servers_tv)
        val serversCheck: ImageView = itemView.findViewById(R.id.item_servers_check)
        val seversBackground: LinearLayout = itemView.findViewById(R.id.vpn_home_city)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServersViewHolder {
        return ServersViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_service, parent, false)
        )

    }

    override fun getItemCount(): Int {
        return countryEntityList.size
    }

    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: ServersViewHolder, position: Int) {
        countryEntityList[position].run {
            holder.serversTv.text = name
            src?.let { holder.seversSrc.setBackgroundResource(it) }
            if (isChoose == true) {
                holder.serversCheck.setBackgroundResource(R.mipmap.servers_check)
                holder.seversBackground.setBackgroundResource(R.drawable.servers_item_check_background)
                holder.serversTv.setTextColor(R.color.black)

            } else {
                holder.serversCheck.setBackgroundResource(R.mipmap.servers_no_check)
                holder.seversBackground.setBackgroundResource(R.drawable.servers_item_background)
                holder.serversTv.setTextColor(R.color.white)

            }
            holder.itemView.setOnClickListener {
                itemClickListener?.onItemClick(countryEntityList[position])
            }
        }
    }

    interface ItemClickListener {
        fun onItemClick(countryInfo: Country)
    }

    fun setNewData(countryList: MutableList<Country>?) {
        val lastItemCount = itemCount
        if (lastItemCount != 0) {
            this.countryEntityList.clear()
            notifyItemRangeRemoved(0, lastItemCount)
        }
        countryList?.let { this.countryEntityList.addAll(it) }
        notifyItemChanged(0, itemCount)
    }

}