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

package li.klass.fhem.domain;

import org.w3c.dom.NamedNodeMap;

import li.klass.fhem.appwidget.annotation.ResourceIdMapper;
import li.klass.fhem.domain.core.Device;
import li.klass.fhem.domain.core.DeviceFunctionality;
import li.klass.fhem.domain.genericview.OverviewViewSettings;
import li.klass.fhem.domain.genericview.ShowField;

@SuppressWarnings("unused")
@OverviewViewSettings(showState = true)
public class CULFHTTKDevice extends Device<CULFHTTKDevice> {
    private String lastWindowState;
    private String windowState = "???";
    @ShowField(description = ResourceIdMapper.lastStateChange, showInOverview = true)
    private String lastStateChangeTime;

    public void readWINDOW(String value) {
        windowState = value;
    }

    public void readPREVIOUS(String value, NamedNodeMap attributes) {
        lastWindowState = value;
        lastStateChangeTime = attributes.getNamedItem("measured").getNodeValue();
    }

    @Override
    public void afterDeviceXMLRead() {
        String stateChangeText = "";
        if (getLastWindowState() != null) {
            stateChangeText += getLastWindowState() + " => ";
        }
        stateChangeText += getWindowState();
        setState(stateChangeText);
    }

    @Override
    public DeviceFunctionality getDeviceFunctionality() {
        return DeviceFunctionality.WINDOW;
    }

    public String getLastStateChangeTime() {
        return lastStateChangeTime;
    }

    public String getLastWindowState() {
        return lastWindowState;
    }

    public String getWindowState() {
        return windowState;
    }

    @Override
    public boolean isSensorDevice() {
        return true;
    }
}
