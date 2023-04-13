package com.bird.yy.wifiproject.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.bird.yy.wifiproject.R
import com.bird.yy.wifiproject.entity.WIFIEntity

class WIFIAdapter : RecyclerView.Adapter<WIFIAdapter.WIFIViewHolder>() {

    private val wifiData = ArrayList<WIFIEntity>()

    var itemClickListener: ItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WIFIViewHolder {
        return WIFIViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_wifi, parent, false)
        )
    }

    override fun onBindViewHolder(holder: WIFIViewHolder, position: Int) {
//        if (position == itemCount - 1 || position == itemCount - 2){
//            holder.itemView.visibility = View.INVISIBLE
//        }else{
            wifiData[position].run {
                holder.tvWifiName.text = wifiSSID
                holder.ivWifiStrength.setImageResource(getStrengthIcon(wifiStrength))
                holder.ivNeedPassword.setImageResource(if (needPassword) R.mipmap.icon_lock else R.mipmap.icon_lock)

//            }
        }

        holder.itemView.setOnClickListener {
            itemClickListener?.onItemClick(wifiData[position])
        }
    }

    override fun getItemCount(): Int {
        return wifiData.size
    }

    fun setNewData(wifiData: ArrayList<WIFIEntity>?) {
        val lastItemCount = itemCount
        if (lastItemCount != 0) {
            this.wifiData.clear()
            notifyItemRangeRemoved(0, lastItemCount)
        }
        wifiData?.let { this.wifiData.addAll(it) }
        notifyItemChanged(0, itemCount)
    }

    private fun getStrengthIcon(wifiStrength: Int): Int {
        return when (wifiStrength) {
            0 -> R.mipmap.wifi_strength_3
            1 -> R.mipmap.wifi_strength_2
            2 -> R.mipmap.wifi_strength_1
            else -> R.mipmap.wifi_strength_0
        }
    }

    interface ItemClickListener {
        fun onItemClick(wifiInfo: WIFIEntity)
    }

    class WIFIViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvWifiName: TextView = itemView.findViewById(R.id.item_wifi_name)
        val ivNeedPassword: ImageView = itemView.findViewById(R.id.item_wifi_lock_src)
        val ivWifiStrength: ImageView = itemView.findViewById(R.id.home_wifi_src)
    }
}