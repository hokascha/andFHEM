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

import org.junit.Test;

import java.util.Arrays;

import li.klass.fhem.domain.core.DeviceXMLParsingBase;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItemInArray;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;

public class StructureDeviceTest extends DeviceXMLParsingBase {
    @Test
    public void testForCorrectlySetAttributesInOnOffDummy() {
        StructureDevice device = getDefaultDevice();

        assertThat(device.getName(), is(DEFAULT_TEST_DEVICE_NAME));
        assertThat(device.getRoomConcatenated(), is(DEFAULT_TEST_ROOM_NAME));

        assertThat(device.getState(), is("on"));
        assertThat(device.isSpecialButtonDevice(), is(false));
        assertThat(device.supportsToggle(), is(true));
        assertThat(device.isOnByState(), is(true));

        assertThat(device.getAvailableTargetStates(), hasItemInArray("on"));
        assertThat(device.getAvailableTargetStates(), hasItemInArray("off"));

        assertThat(device.getLogDevice(), is(nullValue()));
        assertThat(device.getDeviceCharts().size(), is(0));
    }

    @Test
    public void testDeviceWithSetList() {
        StructureDevice device = getDeviceFor("deviceWithSetlist");

        assertThat(device.getAvailableTargetStates(), is(arrayContaining("17", "18", "19", "20", "21", "21.5", "22")));
        assertThat(device.getAvailableTargetStates().length, is(7));
    }

    @Test
    public void testSlider() {
        StructureDevice device = getDeviceFor("slider");
        assertThat(device, is(notNullValue()));
        assertThat(device.getAvailableTargetStates(), hasItemInArray("slider,10,2,110"));
        assertThat(device.supportsDim(), is(true));
        assertThat(device.getDimLowerBound(), is(10));
        assertThat(device.getDimStep(), is(2));
        assertThat(device.getDimUpperBound(), is(110));
    }

    @Override
    protected String getFileName() {
        return "structure.xml";
    }
}
