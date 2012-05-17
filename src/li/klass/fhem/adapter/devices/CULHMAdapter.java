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

package li.klass.fhem.adapter.devices;

import android.content.Context;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import li.klass.fhem.R;
import li.klass.fhem.adapter.devices.generic.FieldNameAddedToDetailListener;
import li.klass.fhem.adapter.devices.generic.GenericDeviceAdapter;
import li.klass.fhem.adapter.devices.generic.SeekBarActionRow;
import li.klass.fhem.adapter.devices.generic.ToggleActionRow;
import li.klass.fhem.domain.CULHMDevice;

public class CULHMAdapter extends GenericDeviceAdapter<CULHMDevice> {

    public CULHMAdapter() {
        super(CULHMDevice.class);
    }

    @Override
    public void fillDeviceOverviewView(View view, final CULHMDevice device) {
        TableLayout layout = (TableLayout) view.findViewById(R.id.device_overview_generic);
        View deviceNameView = layout.findViewById(R.id.deviceName);
        deviceNameView.setVisibility(View.GONE);

        switch (device.getSubType()) {
            case DIMMER:
                layout.addView(new SeekBarActionRow<CULHMDevice>(device.getDimProgress(), device.getName(), SeekBarActionRow.LAYOUT_OVERVIEW)
                        .createRow(inflater, device));
                break;
            case SWITCH:
                layout.addView(new ToggleActionRow<CULHMDevice>(device.getName(), ToggleActionRow.LAYOUT_OVERVIEW, device.isOn())
                        .createRow(view.getContext(), inflater, device));
                break;
            default:
                super.fillDeviceOverviewView(view, device);
                deviceNameView.setVisibility(View.VISIBLE);
                break;
        }
    }

    @Override
    protected void afterPropertiesSet() {
        fieldNameAddedListeners.put("state", new FieldNameAddedToDetailListener<CULHMDevice>() {
            @Override
            public void onFieldNameAdded(Context context, TableLayout tableLayout, String field, CULHMDevice device, TableRow fieldTableRow) {
                switch (device.getSubType()) {
                    case DIMMER:
                        tableLayout.addView(new SeekBarActionRow<CULHMDevice>(device.getDimProgress(), R.string.blank, SeekBarActionRow.LAYOUT_DETAIL)
                                .createRow(inflater, device));
                        break;
                    case SWITCH:
                        tableLayout.addView(new ToggleActionRow<CULHMDevice>(device.getName(), ToggleActionRow.LAYOUT_DETAIL, device.isOn())
                                .createRow(tableLayout.getContext(), inflater, device));
                        break;
                }
            }
        });
    }
}
