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

package li.klass.fhem.service.device;

import android.content.Intent;
import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.domain.core.Device;
import li.klass.fhem.domain.core.RoomDeviceList;
import li.klass.fhem.service.CommandExecutionService;
import li.klass.fhem.service.room.RoomListService;
import li.klass.fhem.util.StringUtil;

import static li.klass.fhem.service.room.RoomListService.NEVER_UPDATE_PERIOD;

/**
 * Class accumulating all device actions like renaming, moving or deleting.
 */
public class DeviceService {

    public static final DeviceService INSTANCE = new DeviceService();

    DeviceService() {
    }

    /**
     * Rename a device.
     *
     * @param device  concerned device
     * @param newName new device name
     */
    public void renameDevice(final Device device, final String newName) {
        CommandExecutionService.INSTANCE.executeSafely("rename " + device.getName() + " " + newName);
        device.setName(newName);
    }

    /**
     * Deletes a device.
     *
     * @param device concerned device
     */
    public void deleteDevice(final Device device) {
        CommandExecutionService.INSTANCE.executeSafely("delete " + device.getName());
        RoomListService.INSTANCE.getAllRoomsDeviceList(NEVER_UPDATE_PERIOD).removeDevice(device);
    }

    /**
     * Sets an alias for a device.
     *
     * @param device concerned device
     * @param alias  new alias to set
     */
    public void setAlias(final Device device, final String alias) {
        if (StringUtil.isBlank(alias)) {
            CommandExecutionService.INSTANCE.executeSafely("deleteattr " + device.getName() + " alias");
        } else {
            CommandExecutionService.INSTANCE.executeSafely("attr " + device.getName() + " alias " + alias);
        }
        device.setAlias(alias);
    }

    /**
     * Moves a device.
     *
     * @param device              concerned device
     * @param newRoomConcatenated new room to move the concerned device to.
     */
    public void moveDevice(final Device device, final String newRoomConcatenated) {
        CommandExecutionService.INSTANCE.executeSafely("attr " + device.getName() + " room " + newRoomConcatenated);

        device.setRoomConcatenated(newRoomConcatenated);

        AndFHEMApplication.getContext().sendBroadcast(new Intent(Actions.DO_UPDATE));
    }
}
