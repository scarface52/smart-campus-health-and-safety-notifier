package com.theretros.smartcampus.adapters

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.color.MaterialColors
import com.theretros.smartcampus.EditNotificationActivity
import com.theretros.smartcampus.NotificationDetailsActivity
import com.theretros.smartcampus.R
import com.theretros.smartcampus.SessionManager
import com.theretros.smartcampus.data.classes
import com.theretros.smartcampus.data.dataclasses.IncidentWithFollow
import com.theretros.smartcampus.data.followIncident
import com.theretros.smartcampus.data.unfollowIncident
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.time.ExperimentalTime
import kotlin.time.toJavaInstant

class NotificationCardAdapter(private val notificationList: List<IncidentWithFollow> ): RecyclerView.Adapter<NotificationCardAdapter.NotificationCardHolder>() {

        // Describes the item view and its place within the RecyclerView
        class NotificationCardHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val card = itemView.findViewById<CardView>(R.id.incidentCard)
            val typeButton: MaterialButton = itemView.findViewById(R.id.typeButton)
            val starButton: ImageButton = itemView.findViewById(R.id.starButton)
            val titleTextView: TextView = itemView.findViewById(R.id.title)
            val descriptionTextView: TextView = itemView.findViewById(R.id.description)
            val dateTextView: TextView = itemView.findViewById(R.id.date)
            val statusIcon: ImageView = itemView.findViewById(R.id.statusIcon)
            val statusTextView: TextView = itemView.findViewById(R.id.statusText)
        }

        private lateinit var con: Context

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationCardHolder {
            con = parent.context
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.notification_card, parent, false)
            return NotificationCardHolder(view)
        }

        // Binds data to the views inside the ViewHolder
        @OptIn(ExperimentalTime::class)
        override fun onBindViewHolder(holder: NotificationCardHolder, position: Int) {
            val incident = notificationList[position]

            val type = classes[incident.class_id]
            holder.typeButton.text = type
            val icon = when (type) {
                "Health" -> R.drawable.ic_health_24
                "Safety" -> R.drawable.ic_safety_24
                "Environmental" -> R.drawable.ic_environmental_24
                "Lost and Found" -> R.drawable.ic_lost_and_found_24
                "Maintenance" -> R.drawable.ic_maintenance_24
                else -> R.drawable.ic_health_24
            }
            holder.typeButton.setIconResource(icon)


            holder.titleTextView.text = incident.title
            holder.descriptionTextView.text = incident.description
            setStarButtonImage(incident.is_followed, holder)

            val userZone = ZoneId.systemDefault()

            val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.getDefault())
                .withZone(userZone)

            val formattedDate = formatter.format(incident.report_time.toJavaInstant())

            holder.dateTextView.text = formattedDate
            holder.statusIcon.setImageResource(if (incident.status == "Resolved") R.drawable.tick_circle else R.drawable.ic_circle_24)

            if (incident.status != "Resolved") {
                holder.statusIcon.imageTintList =
                    ColorStateList.valueOf(
                        MaterialColors.getColor(
                            holder.statusIcon,
                            if (incident.status == "Under Review") R.attr.colorInfo
                            else R.attr.colorWarning
                        )

                    )
                holder.statusTextView.setTextColor(MaterialColors.getColor(
                    holder.statusTextView,
                    if (incident.status == "Under Review") R.attr.colorInfo
                    else R.attr.colorWarning

                ))

            }


            holder.statusTextView.text = incident.status

            setupClickListeners(holder, incident)
        }

    fun setupClickListeners(holder: NotificationCardHolder, incident: IncidentWithFollow) {
        val session = SessionManager(con)

        holder.starButton.setOnClickListener {
            incident.is_followed = !(incident.is_followed)

            holder.itemView.findViewTreeLifecycleOwner()?.lifecycleScope?.launch {
                try {
                    if (incident.is_followed)
                        followIncident(session.getUserId()!!.toInt(), incident.incident_id)
                    else
                        unfollowIncident(session.getUserId()!!.toInt(), incident.incident_id)
                    setStarButtonImage(incident.is_followed, holder)
                } catch (e: Exception) {
                    println("message: $e")
                    Toast.makeText(con, "An error occured", Toast.LENGTH_SHORT).show()
                }
            }
        }

        holder.card.setOnClickListener {
            if (!session.isAdmin())
            {
                val intent = Intent(con, NotificationDetailsActivity::class.java)
                intent.putExtra("incidentId", incident.incident_id)
                con.startActivity(intent)
            } else {
                println("User is admin")
                val intent = Intent(con, EditNotificationActivity::class.java)
                intent.putExtra("incidentId", incident.incident_id)
                con.startActivity(intent)
            }
        }
    }

    fun setStarButtonImage(isFollowed: Boolean, holder: NotificationCardHolder) {
        val emptyStar = R.drawable.ic_empty_star_24
        val filledStar = R.drawable.ic_filled_star_24
        holder.starButton.setImageResource(if (isFollowed) filledStar
        else emptyStar)
    }

        override fun getItemCount(): Int = notificationList.size
}
