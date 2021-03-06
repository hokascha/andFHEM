/*
 * AndFHEM - Open Source Android application to control a FHEM home automation
 * server.
 *
 * Copyright (c) 2012, Matthias Klass or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU GENERAL PUBLICLICENSE, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU GENERAL PUBLIC LICENSE
 * for more details.
 *
 * You should have received a copy of the GNU GENERAL PUBLIC LICENSE
 * along with this distribution; if not, write to:
 *   Free Software Foundation, Inc.
 *   51 Franklin Street, Fifth Floor
 */

package li.klass.fhem.appwidget.view.widget.base;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.SystemClock;
import android.view.View;
import android.widget.RemoteViews;

import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.R;
import li.klass.fhem.activities.AndFHEMMainActivity;
import li.klass.fhem.appwidget.WidgetConfiguration;
import li.klass.fhem.appwidget.WidgetConfigurationCreatedCallback;
import li.klass.fhem.appwidget.annotation.SupportsWidget;
import li.klass.fhem.appwidget.view.WidgetType;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.domain.core.Device;
import li.klass.fhem.fragments.core.DeviceDetailFragment;
import li.klass.fhem.util.ImageUtil;

public abstract class AppWidgetView {

    public boolean supports(Device<?> device) {
        if (!device.getClass().isAnnotationPresent(SupportsWidget.class)) return false;

        if (!device.supportsWidget(this.getClass())) {
            return false;
        }

        SupportsWidget annotation = device.getClass().getAnnotation(SupportsWidget.class);
        Class<? extends AppWidgetView>[] supportedWidgetViews = annotation.value();
        for (Class<? extends AppWidgetView> supportedWidgetView : supportedWidgetViews) {
            if (supportedWidgetView.equals(this.getClass())) return true;
        }

        return false;
    }

    public RemoteViews createView(Context context, Device<?> device, WidgetConfiguration widgetConfiguration) {
        RemoteViews views = new RemoteViews(context.getPackageName(), getContentView());

        if (shouldSetDeviceName()) {
            views.setTextViewText(R.id.deviceName, device.getWidgetName());
        }
        fillWidgetView(context, views, device, widgetConfiguration);

        return views;
    }

    protected void setTextViewOrHide(RemoteViews view, int viewId, String value) {
        if (value != null) {
            view.setTextViewText(viewId, value);
            view.setViewVisibility(viewId, View.VISIBLE);
        } else {
            view.setViewVisibility(viewId, View.GONE);
        }
    }

    protected void openDeviceDetailPageWhenClicking(int viewId, RemoteViews view, Device device, WidgetConfiguration widgetConfiguration) {
        openDeviceDetailPageWhenClicking(viewId, view, device, widgetConfiguration.widgetId);
    }

    protected void openDeviceDetailPageWhenClicking(int viewId, RemoteViews view, Device device, int widgetId) {
        PendingIntent pendingIntent = createOpenDeviceDetailPagePendingIntent(device, widgetId);

        view.setOnClickPendingIntent(viewId, pendingIntent);
    }

    protected PendingIntent createOpenDeviceDetailPagePendingIntent(Device device, int widgetId) {
        Context context = AndFHEMApplication.getContext();

        Intent openIntent = createOpenDeviceDetailPageIntent(device, context);
        return PendingIntent.getActivity(context, widgetId, openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    protected Intent createOpenDeviceDetailPageIntent(Device device, Context context) {
        Intent openIntent = new Intent(context, AndFHEMMainActivity.class);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        openIntent.putExtra(BundleExtraKeys.FRAGMENT_NAME, DeviceDetailFragment.class.getName());
        openIntent.putExtra(BundleExtraKeys.DEVICE_NAME, device.getName());
        openIntent.putExtra("unique", "foobar://" + SystemClock.elapsedRealtime());
        return openIntent;
    }

    public boolean shouldSetDeviceName() {
        return true;
    }

    public void createWidgetConfiguration(Context context, WidgetType widgetType, int appWidgetId,
                                          Device device,
                                          WidgetConfigurationCreatedCallback callback) {
        callback.widgetConfigurationCreated(new WidgetConfiguration(appWidgetId, device.getName(), widgetType));
    }

    protected void loadImageAndSetIn(RemoteViews view, int imageId, String url,
                                     boolean preventCache) {
        if (preventCache) {
            url += "?time=" + System.currentTimeMillis();
        }
        Bitmap bitmap = ImageUtil.loadBitmap(url);
        view.setImageViewBitmap(imageId, bitmap);
    }

    public abstract int getWidgetName();

    protected abstract int getContentView();

    protected abstract void fillWidgetView(Context context, RemoteViews view, Device<?> device,
                                           WidgetConfiguration widgetConfiguration);

}
