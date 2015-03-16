/*
 * Copyright 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.sample.mobileassistant;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

/**
 * Handles the GCM notifications for the application.
 */
public class GcmIntentService extends IntentService {

    /**
     * Notification type.
     */
    public static final int NOTIFICATION_ID = 1;

    /**
     * Notifications builder.
     */
    private NotificationCompat.Builder builder;

    /**
     * Notifications manager.
     */
    private NotificationManager mNotificationManager;

    /**
     * Constructor.
     */
    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected final void onHandleIntent(final Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {  // has effect of unpacking Bundle
            /*
             * Filter messages based on message type. Since it is likely that
             * GCM will be extended in the future with new message types,
             * just ignore any message types you're not interested in, or that
             * you don't recognize.
             */
            switch (messageType) {
                case GoogleCloudMessaging.MESSAGE_TYPE_DELETED:
                    sendNotification("Deleted messages on server: "
                            + extras.toString());
                    // If it's a regular GCM message, do some work.
                    break;
                case GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE:
                    if (intent.getStringExtra("NotificationKind")
                            .equals("PriceCheckLowerPrices1")) {
                        final String message
                                = getUserMessageForPriceCheckLowerPricesNotif(
                                intent);

                        Handler h = new Handler(Looper.getMainLooper());
                        h.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast toast = Toast
                                        .makeText(getApplicationContext(),
                                                message,
                                                Toast.LENGTH_LONG);
                                toast.show();
                            }
                        });
                    }
                    break;
                case GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR:
                default:
                    sendNotification("Send error: " + extras.toString());
                    break;
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    /**
     * Constructs the message to be displayed when PriceCheckLowerPrices
     * notification has been
     * received. The payload for such a notification is expected to have two
     * additional keys:
     * ProductName and ProductCount. The value of these keys are used in the
     * message to the user.
     *
     * @param intent intent containing the payload as extras.
     * @return message to put in the notification.
     */
    private String getUserMessageForPriceCheckLowerPricesNotif(final
    Intent intent) {
        String firstProductName = intent.getStringExtra("ProductName");
        String numberOfProductsAsString = intent.getStringExtra("ProductCount");

        int parsedNumberOfProducts;

        try {
            parsedNumberOfProducts = Integer.parseInt(numberOfProductsAsString);
        } catch (NumberFormatException n) {
            // assume that the number of products is 1
            parsedNumberOfProducts = 1;
        }

        final int numberOfProducts = parsedNumberOfProducts;

        int resourceId;

        if (numberOfProducts == 1) {
            resourceId = R.string.notification_PriceCheckLowerPrices1_1product;
        } else {
            resourceId
                    = R.string.notification_PriceCheckLowerPrices1_manyProducts;
        }

        return String.format(getString(resourceId), firstProductName,
                numberOfProducts - 1);

    }

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.

    /**
     * Put the message into a notification and post it.
     * This is just one simple example of what you might choose to do with
     * a GCM message.
     * @param msg is the message to display in the notification.
     */
    private  void sendNotification(final String msg) {
        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("MobileAssistant GCM Notification")
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(msg))
                        .setContentText(msg);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
}
