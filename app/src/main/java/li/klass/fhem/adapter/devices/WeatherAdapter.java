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

package li.klass.fhem.adapter.devices;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import li.klass.fhem.R;
import li.klass.fhem.adapter.ListDataAdapter;
import li.klass.fhem.adapter.devices.core.GenericDeviceAdapter;
import li.klass.fhem.domain.WeatherDevice;
import li.klass.fhem.util.ImageUtil;
import li.klass.fhem.util.ListViewUtil;

public class WeatherAdapter extends GenericDeviceAdapter<WeatherDevice> {

    public WeatherAdapter() {
        super(WeatherDevice.class);
    }

    @Override
    protected int getOverviewLayout(WeatherDevice device) {
        return R.layout.device_overview_weather;
    }

    @Override
    protected void fillDeviceOverviewView(final View view, WeatherDevice device, long lastUpdate) {
        setTextView(view, R.id.deviceName, device.getAliasOrName());
        setTextViewOrHideTableRow(view, R.id.tableRowTemperature, R.id.temperature, device.getTemperature());
        setTextViewOrHideTableRow(view, R.id.tableRowWind, R.id.wind, device.getWind());
        setTextViewOrHideTableRow(view, R.id.tableRowHumidity, R.id.humidity, device.getHumidity());
        setTextViewOrHideTableRow(view, R.id.tableRowCondition, R.id.condition, device.getCondition());

        setWeatherIconIn((ImageView) view.findViewById(R.id.weatherImage), device.getIcon());
    }

    @Override
    protected void fillOtherStuffDetailLayout(Context context, LinearLayout layout, WeatherDevice device, LayoutInflater inflater) {
        LinearLayout currentWeatherHolder = (LinearLayout) inflater.inflate(R.layout.device_detail_other_layout, null);
        setTextView(currentWeatherHolder, R.id.caption, R.string.currentWeather);
        RelativeLayout currentWeatherContent = createCurrentWeatherContent(device, inflater);
        currentWeatherHolder.addView(currentWeatherContent);
        layout.addView(currentWeatherHolder);

        LinearLayout forecastHolder = (LinearLayout) inflater.inflate(R.layout.device_detail_other_layout, null);
        setTextView(forecastHolder, R.id.caption, R.string.forecast);
        layout.addView(forecastHolder);

        ListView weatherForecastList = createWeatherForecastList(context, device);
        forecastHolder.addView(weatherForecastList);
        ListViewUtil.setHeightBasedOnChildren(weatherForecastList);
    }

    private ListView createWeatherForecastList(final Context context, final WeatherDevice device) {
        final ListView weatherForecastList = new ListView(context);
        ListDataAdapter<WeatherDevice.WeatherDeviceForecast> forecastAdapter = new ListDataAdapter<WeatherDevice.WeatherDeviceForecast>(
                context, R.layout.weather_forecast_item, device.getForecasts()
        ) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                WeatherDevice.WeatherDeviceForecast item = data.get(position);
                View view = inflater.inflate(resource, null);

                String date = item.getDayOfWeek() + ", " + item.getDate();
                setTextViewOrHideTableRow(view, R.id.tableRowDate, R.id.date, date);

                String temperature = item.getLowTemperature() + " - " + item.getHighTemperature();
                setTextViewOrHideTableRow(view, R.id.tableRowTemperature, R.id.temperature, temperature);

                setTextViewOrHideTableRow(view, R.id.tableRowCondition, R.id.condition, item.getCondition());

                setWeatherIconIn((ImageView) view.findViewById(R.id.forecastWeatherImage), item.getIcon());

                return view;
            }

            @Override
            public boolean areAllItemsEnabled() {
                return false;
            }

            @Override
            public boolean isEnabled(int position) {
                return false;
            }
        };
        weatherForecastList.setAdapter(forecastAdapter);
        return weatherForecastList;
    }

    private RelativeLayout createCurrentWeatherContent(WeatherDevice device, LayoutInflater inflater) {
        RelativeLayout currentWeather = (RelativeLayout) inflater.inflate(R.layout.weather_current, null);

        setTextViewOrHideTableRow(currentWeather, R.id.tableRowTemperature, R.id.temperature, device.getTemperature());
        setTextViewOrHideTableRow(currentWeather, R.id.tableRowWind, R.id.wind, device.getWind());
        setTextViewOrHideTableRow(currentWeather, R.id.tableRowHumidity, R.id.humidity, device.getHumidity());
        setTextViewOrHideTableRow(currentWeather, R.id.tableRowCondition, R.id.condition, device.getCondition());

        setWeatherIconIn((ImageView) currentWeather.findViewById(R.id.currentWeatherImage), device.getIcon());

        return currentWeather;
    }


    private void setWeatherIconIn(final ImageView imageView, String weatherIcon) {
        final String imageURL = WeatherDevice.IMAGE_URL_PREFIX + weatherIcon + ".png";
        ImageUtil.setExternalImageIn(imageView, imageURL);
    }
}
