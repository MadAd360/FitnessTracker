package com.uni.ard.fitnesstracker;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import java.util.Date;
import java.util.HashMap;

/**
 * Created by Adam on 19/02/2015.
 */
public class GlobalVariables {

    public static final int GOAL_MODE_ACTIVE = 0;
    public static final int GOAL_MODE_OVERDUE = 1;
    public static final int GOAL_MODE_SUSPENDED = 2;
    public static final int GOAL_MODE_PENDING = 3;
    public static final int GOAL_MODE_COMPLETED = 4;
    public static final int GOAL_MODE_ABANDONED = 5;

    public static final String KEY_GOAL_STATUS = "status";

    public static boolean testMode = false;
    public static Long testDate;

    public static Long getTime() {
        Date currentDate = new Date();
        if (!testMode) {
            return currentDate.getTime();
        } else {
            if (testDate == null) {
                testDate = currentDate.getTime();
            }
            return testDate;
        }
    }

    public static int getStatus(boolean active, boolean closed, Long start, Long end, boolean completed) {
        Date currentDate = new Date(getTime());
        Date startDate = new Date(start);
        Date endDate = new Date(end);
        if (closed) {
            if (completed) {
                return GOAL_MODE_COMPLETED;
            } else {
                return GOAL_MODE_ABANDONED;
            }
        } else if (active) {
            if (!startDate.before(currentDate)) {
                return GOAL_MODE_PENDING;
            } else if (endDate.after(currentDate)) {
                return GOAL_MODE_ACTIVE;
            } else {
                return GOAL_MODE_OVERDUE;
            }
        } else {
            return GOAL_MODE_SUSPENDED;
        }
    }

    public static void setTestTime(Long time) {
        testDate = time;
    }

    public static void setTestMode(Boolean test) {
        testMode = test;
    }

    public static boolean getTestMode() {
        return testMode;
    }


    public static double convertUnits(int value, boolean climbType, String to, String from) {
        HashMap<String, Double> stepsConversion = new HashMap<String, Double>();
        if (climbType) {
            //stair = 19cm or 7.48031496 inches
            stepsConversion.put("Steps", 1.0);
            stepsConversion.put("Miles", 8470.2315796606510803924758804541);
            stepsConversion.put("Metres", 5.2631578947368421052631578947368);
            stepsConversion.put("Kilometres", 5263.1578947368421052631578947368);
            stepsConversion.put("Feet", 1.6042105264508808864379689167527);
            stepsConversion.put("Calories", 20.0);
        } else {
            //step = 76.2cm or 30 inches
            stepsConversion.put("Steps", 1.0);
            stepsConversion.put("Miles", 2112.0);
            stepsConversion.put("Metres", 1.3123359580052493438320209973753);
            stepsConversion.put("Kilometres", 1312.3359580052493438320209973753);
            stepsConversion.put("Feet", 0.4);
            stepsConversion.put("Calories", 20.0);
        }

        double steps = value * stepsConversion.get(from);

        double goalFormat = steps / stepsConversion.get(to);
        return goalFormat;
    }


    public static void createNotification(long rowId, String goalName, int state, Context context) {

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        boolean notificationDisable = sp.getBoolean(context.getResources().getString(R.string.pref_notification_disable), false);

        if (!notificationDisable) {
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(context)
                            .setSmallIcon(R.drawable.moonprint_small)
                            .setContentText(goalName);
            Intent resultIntent = new Intent(context, GoalDetails.class);
            resultIntent.putExtra(DBAdapter.KEY_ROWID, rowId);
            resultIntent.putExtra(GlobalVariables.KEY_GOAL_STATUS, state);

            PendingIntent resultPendingIntent =
                    PendingIntent.getActivity(
                            context,
                            0,
                            resultIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );
            mBuilder.setContentIntent(resultPendingIntent);
            mBuilder.setAutoCancel(true);
            int mNotificationId = 001;
            NotificationManager mNotifyMgr =
                    (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
            if (state == GlobalVariables.GOAL_MODE_OVERDUE) {
                mBuilder.setContentTitle("Goal Overdue");
            } else {
                mBuilder.setContentTitle("Goal Complete");
            }

            mNotifyMgr.notify(mNotificationId, mBuilder.build());
        }
    }
}
