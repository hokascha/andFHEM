/*
 * AndFHEM - Open Source Android application to control a FHEM home automation
 * server.
 *
 * Copyright (c) 2011, Matthias Klass or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU GENERAL PUBLIC LICENSE, as published by the Free Software Foundation.
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
 *   Boston, MA  02110-1301  USA
 */

package li.klass.fhem.domain.log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import li.klass.fhem.domain.core.Device;
import li.klass.fhem.domain.core.DeviceFunctionality;
import li.klass.fhem.service.graph.description.ChartSeriesDescription;
import li.klass.fhem.service.room.AllDevicesReadCallback;
import li.klass.fhem.service.room.DeviceReadCallback;

public abstract class LogDevice<T extends LogDevice> extends Device<T> {

    protected List<CustomGraph> customGraphs = new ArrayList<CustomGraph>();
    private String concerningDeviceRegexp;

    @SuppressWarnings("unused")
    public void readREGEXP(String value) {
        if (".".equals(value)) value = ".*";
        this.concerningDeviceRegexp = extractConcerningDeviceRegexpFromDefinition(value);
    }

    @Override
    public DeviceFunctionality getDeviceFunctionality() {
        return DeviceFunctionality.LOG;
    }

    public List<CustomGraph> getCustomGraphs() {
        return customGraphs;
    }

    /**
     * We extract the device names from the current log regexp. As the regexp always concerns
     * device name and reading, we have to skip the reading.
     *
     * The default format is <i>deviceName:reading </i>, so we have to skip the reading part and
     * the colon. In addition, we have to make sure that we can still write regexp style expression,
     * including OR expressions on different levels.
     *
     * @param definition regexp definition for the log device (including device events).
     * @return regexp definition for the log device names.
     */
    static String extractConcerningDeviceRegexpFromDefinition(String definition) {
        definition = definition.replaceAll(":\\|", "\\|");

        boolean isName = true;
        StringBuilder out = new StringBuilder();
        boolean firstCharFound = false;
        int level = 0;
        int baseLevel = 0;

        for (int i = 0; i < definition.length(); i++) {

            char c = definition.charAt(i);

            if (c == '(' || c == ')') {
                if (c == '(') level ++;
                if (c == ')') level --;
            }

            if (! firstCharFound && c != '(') {
                baseLevel = level;
                firstCharFound = true;
            }

            if (! firstCharFound || c == '(' || c == ')') continue;

            if (level <= baseLevel) {
                if (isName && c != '|' && c != ':') {
                    out.append(c);
                    continue;
                }

                if (c == ':' && isName) {
                    isName = false;
                    continue;
                }

                if (c == '|' && ! isName) {
                    isName = true;
                }

                if (c == '|') {
                    out.append('|');
                }
            } else if (isName) {
                out.append(c);
            }
        }

        String result = out.toString();
        return result
                .replaceAll("\\.\\|", ".*|")
                .replaceAll("\\.$", ".*");
    }

    public boolean concernsDevice(String deviceName) {
        return deviceName.matches(concerningDeviceRegexp);
    }

    public String getConcerningDeviceRegexp() {
        return concerningDeviceRegexp;
    }

    /**
     * Create a graph command for the given parameters.
     *
     *
     * @param device device which created the log entries.
     * @param fromDateFormatted formatted from log date (yyyy-MM-dd_HH:mm)
     * @param toDateFormatted formatted to log date (yyyy-MM-dd_HH:mm)
     * @param seriesDescription series specification.
     * @return command
     */
    public abstract String getGraphCommandFor(Device device, String fromDateFormatted,
                                              String toDateFormatted, ChartSeriesDescription seriesDescription);

    @Override
    public void afterDeviceXMLRead() {
        super.afterDeviceXMLRead();

        setAllDeviceReadCallback(new AllDevicesReadCallback() {

            @Override
            public void devicesRead(Map<String, Device> allDevices) {
                for (Device device : allDevices.values()) {
                    if (concernsDevice(device.getName())) {
                        device.setLogDevice(LogDevice.this);
                    }
                }
            }
        });
    }
}
