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

package li.klass.fhem.service.device;

import android.content.Intent;
import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.domain.AtDevice;
import li.klass.fhem.service.CommandExecutionService;
import li.klass.fhem.service.room.RoomListService;

public class AtService {
    public static final AtService INSTANCE = new AtService();

    private AtService() {}

    public void createNew(String timerName, int hour, int minute, int second, String repetition, String type,
                          String targetDeviceName, String targetState, String targetStateAppendix, boolean isActive) {
        AtDevice device = new AtDevice();

        setValues(hour, minute, second, repetition, type, targetDeviceName, targetState, targetStateAppendix, device, isActive);

        String definition = device.toFHEMDefinition();
        String command = "define " + timerName + " at " + definition;
        CommandExecutionService.INSTANCE.executeSafely(command);

        Intent intent = new Intent(Actions.DO_UPDATE);
        intent.putExtra(BundleExtraKeys.DO_REFRESH, true);
        AndFHEMApplication.getContext().sendBroadcast(intent);
    }

    public void modify(String timerName, int hour, int minute, int second, String repetition, String type,
                       String targetDeviceName, String targetState, String targetStateAppendix, boolean isActive) {
        AtDevice device = (AtDevice) RoomListService.INSTANCE.getDeviceForName(timerName, RoomListService.NEVER_UPDATE_PERIOD);

        setValues(hour, minute, second, repetition, type, targetDeviceName, targetState, targetStateAppendix, device, isActive);
        String definition = device.toFHEMDefinition();
        String command = "modify " + timerName + " " + definition;
        CommandExecutionService.INSTANCE.executeSafely(command);
    }

    private void setValues(int hour, int minute, int second, String repetition, String type, String targetDeviceName, String targetState, String targetStateAppendix, AtDevice device, boolean isActive) {
        device.setHour(hour);
        device.setMinute(minute);
        device.setSecond(second);
        device.setRepetition(AtDevice.AtRepetition.valueOf(repetition));
        device.setTimerType(AtDevice.TimerType.valueOf(type));
        device.setTargetDevice(targetDeviceName);
        device.setTargetState(targetState);
        device.setTargetStateAddtionalInformation(targetStateAppendix);
        device.setActive(isActive);
    }
}
