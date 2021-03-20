/*
 * Copyright © 2021 WireGuard LLC. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.wireguard.android.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.util.Log;
import android.widget.RemoteViews;

import com.wireguard.android.R;
import com.wireguard.android.model.TunnelManager.IntentReceiver;

/**
 * Implementation of App Widget functionality.
 */
public class VpnSwitchAppWidget extends AppWidgetProvider {


    private static final String TAG = "VpnSwitchAppWidget";

    public static void updateAppWidget(final Context context, final AppWidgetManager appWidgetManager,
                                final int appWidgetId) {


        final ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final Network[] networks = cm.getAllNetworks();

        boolean isVpnOn = false;

        for(int i = 0; i < networks.length; i++) {
            final NetworkCapabilities caps = cm.getNetworkCapabilities(networks[i]);
            Log.i(TAG, "Network " + i + ": " + networks[i]);
            Log.i(TAG, "VPN transport is: " + caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN));
            Log.i(TAG, "NOT_VPN capability is: " + caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_VPN));
            if (caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) {
                isVpnOn = true;
            }
        }

        // Construct the RemoteViews object
        final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.vpn_switch_app_widget);
        final Intent intent = new Intent(context, IntentReceiver.class);
        if (isVpnOn) {
            views.setTextViewText(R.id.vpnSwitchText, "OFF");
            intent.setAction("com.wireguard.android.action.SET_TUNNEL_DOWN");
        } else {
            views.setTextViewText(R.id.vpnSwitchText, "ON");
            intent.setAction("com.wireguard.android.action.SET_TUNNEL_UP");
        }
        final PendingIntent homePendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.vpnSwitchText, homePendingIntent);


        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(final Context context, final AppWidgetManager appWidgetManager, final int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (final int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {

        final AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);
        final ComponentName widgetComponent = new ComponentName(context, VpnSwitchAppWidget.class);
        final int[] widgetIds = widgetManager.getAppWidgetIds(widgetComponent);
        for (final int widgetId : widgetIds) {
            VpnSwitchAppWidget.updateAppWidget(context, AppWidgetManager.getInstance(context), widgetId);
        }
        super.onReceive(context, intent);
    }

    @Override
    public void onEnabled(final Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(final Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}