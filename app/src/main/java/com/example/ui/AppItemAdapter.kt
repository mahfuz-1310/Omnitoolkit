package com.example.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * RecyclerView Adapter for displaying installed applications with enhanced 3-line date/time layout:
 * - Line 1: Installed: DD/MM/YYYY - HH:mm:ss
 * - Line 2: Day: Tuesday, 28 July 2026 (Day & Month formatted with full names)
 * - Line 3: Relative Time Counter (e.g., Installed 25 days ago, Installed 1 month 5 days ago)
 */
class AppItemAdapter(
    private val onUninstallClick: (AppItem) -> Unit = {},
    private val onCopyPackageClick: (AppItem) -> Unit = {}
) : ListAdapter<AppItem, AppItemAdapter.AppViewHolder>(AppDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            android.R.layout.simple_list_item_2, parent, false
        )
        return AppViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val app = getItem(position)
        holder.bind(app, onUninstallClick, onCopyPackageClick)
    }

    class AppViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val text1: TextView? = itemView.findViewById(android.R.id.text1)
        private val text2: TextView? = itemView.findViewById(android.R.id.text2)

        fun bind(
            app: AppItem,
            onUninstallClick: (AppItem) -> Unit,
            onCopyPackageClick: (AppItem) -> Unit
        ) {
            val context = itemView.context
            
            // App Name & Package
            text1?.text = "${app.appName} (${if (app.isSystemApp) "System" else "User"})"
            
            // 3-Line Formatted Date Output:
            // Line 1: Installed: DD/MM/YYYY - HH:mm:ss
            // Line 2: Day: Tuesday, 28 July 2026
            // Line 3: Relative Time Counter
            val formattedDateDetails = buildString {
                append("Installed: ").append(app.firstInstallTimeFormatted).append("\n")
                append(app.dayFormattedDate).append("\n")
                append(app.relativeTimeAgo)
            }
            
            text2?.text = formattedDateDetails

            itemView.setOnClickListener {
                onCopyPackageClick(app)
            }
        }
    }

    class AppDiffCallback : DiffUtil.ItemCallback<AppItem>() {
        override fun areItemsTheSame(oldItem: AppItem, newItem: AppItem): Boolean {
            return oldItem.packageName == newItem.packageName
        }

        override fun areContentsTheSame(oldItem: AppItem, newItem: AppItem): Boolean {
            return oldItem == newItem
        }
    }
}
