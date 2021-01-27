package com.example.viana_xplore_reset

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat

private const val NOTIFICATION_ID = 33
private const val CHANNEL_ID = "GeofenceChannel"

fun createChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val notificationChannel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.channel_name),

                NotificationManager.IMPORTANCE_HIGH
        )
                .apply {
                    setShowBadge(false)
                }

        notificationChannel.enableLights(true)
        notificationChannel.lightColor = Color.RED
        notificationChannel.enableVibration(true)
        notificationChannel.description = context.getString(R.string.notification_channel_description)

        //Builder do canal das notificações
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(notificationChannel)
    }
}

/*
 * A Kotlin extension function for AndroidX's NotificationCompat that sends our Geofence
 * entered notification.  It sends a custom notification based on the name string associated
 * with the LANDMARK_DATA from GeofencingConstatns in the GeofenceUtils file.
 */

//Definição do que vai ser enviado na notificação
fun NotificationManager.sendGeofenceEnteredNotification(context: Context, foundIndex: Int) {
    val contentIntent = Intent(context, AtividadeMapa::class.java)
    contentIntent.putExtra(Fences.GeofencingConstants.EXTRA_GEOFENCE_INDEX, foundIndex)
    val contentPendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT   //Valida a intent e atualiza com nova informação
    )

    // We use the name resource ID from the LANDMARK_DATA along with content_text to create
    // a custom message when a Geofence triggers.
    val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText(context.getString(R.string.content_text,
                    context.getString(Fences.GeofencingConstants.LANDMARK_DATA[foundIndex].name)))
            .setSmallIcon(R.drawable.viana)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(contentPendingIntent)

    //Builder
    notify(NOTIFICATION_ID, builder.build())
}

class NotificationsManager {

}