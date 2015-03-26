package com.intuit.quickfoods.helpers;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.support.v4.app.NotificationCompat;

import com.intuit.quickfoods.R;

public class Notification {
    public static int mId = 1337;
    public NotificationManager mNotificationManager;
    public Notification(Service service, String heading, String message){
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(service)
                        .setSmallIcon(R.drawable.ic_stat_qf)
                        .setContentTitle(heading)
                        .setContentText(message)
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
// Creates an explicit intent for an Activity in your app
//        Intent resultIntent = new Intent(this, ResultActivity.class);

// The stack builder object will contain an artificial back stack for the
// started Activity.
// This ensures that navigating backward from the Activity leads out of
// your application to the Home screen.
        //TaskStackBuilder stackBuilder = TaskStackBuilder.create(activity);
// Adds the back stack for the Intent (but not the Intent itself)
//        stackBuilder.addParentStack(ResultActivity.class);
//// Adds the Intent that starts the Activity to the top of the stack
//        stackBuilder.addNextIntent(resultIntent);
//        PendingIntent resultPendingIntent =
//                stackBuilder.getPendingIntent(
//                        0,
//                        PendingIntent.FLAG_UPDATE_CURRENT
//                );
//        mBuilder.setContentIntent(resultPendingIntent);
        mNotificationManager =
                (NotificationManager) service.getSystemService(Context.NOTIFICATION_SERVICE);

// mId allows you to update the notification later on.
        mNotificationManager.notify(mId, mBuilder.build());
    }
}
