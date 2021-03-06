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

package li.klass.fhem.activities.locale;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

import li.klass.fhem.R;
import li.klass.fhem.activities.locale.LocaleIntentConstants;
import li.klass.fhem.adapter.ConnectionListAdapter;
import li.klass.fhem.constants.Actions;
import li.klass.fhem.constants.BundleExtraKeys;
import li.klass.fhem.constants.ResultCodes;
import li.klass.fhem.fhem.connection.FHEMServerSpec;
import li.klass.fhem.fhem.connection.ServerType;

import static li.klass.fhem.constants.BundleExtraKeys.COMMAND;
import static li.klass.fhem.constants.BundleExtraKeys.CONNECTION_ID;
import static li.klass.fhem.constants.BundleExtraKeys.CONNECTION_LIST;

public class SendCommandLocaleSettingActivity extends Activity {

    public static final String CURRENT_CONNECTION_ID = "current";
    private String selectedId = CURRENT_CONNECTION_ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.locale_send_command);

        final EditText commandView = (EditText) findViewById(R.id.fhemCommand);

        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra(LocaleIntentConstants.EXTRA_BUNDLE);
        if (bundle != null && bundle.containsKey(COMMAND)) {
            if (bundle.containsKey(COMMAND)) {
                commandView.setText(bundle.getString(COMMAND));
            }
            if (bundle.containsKey(CONNECTION_ID)) {
                selectedId = bundle.getString(CONNECTION_ID);
            }
        }

        final Spinner spinner = (Spinner) findViewById(R.id.connectionList);
        final ConnectionListAdapter connectionListAdapter = new ConnectionListAdapter(this, new ArrayList<FHEMServerSpec>());
        spinner.setAdapter(connectionListAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedId = (String) view.getTag();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        Intent connectionIntent = new Intent(Actions.CONNECTIONS_LIST);
        connectionIntent.putExtra(BundleExtraKeys.RESULT_RECEIVER, new ResultReceiver(new Handler()) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                super.onReceiveResult(resultCode, resultData);

                fillConnectionSpinner(resultCode, resultData, connectionListAdapter);

            }
        });
        startService(connectionIntent);

        Button saveButton = (Button) findViewById(R.id.save);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent resultIntent = new Intent();
                assert commandView != null;
                String command = commandView.getText().toString();

                resultIntent.putExtra(LocaleIntentConstants.EXTRA_STRING_BLURB, command);

                Bundle bundle = new Bundle();
                bundle.putString(BundleExtraKeys.ACTION, Actions.EXECUTE_COMMAND);
                bundle.putString(COMMAND, command);
                resultIntent.putExtra(LocaleIntentConstants.EXTRA_BUNDLE, bundle);

                if (selectedId != null && ! CURRENT_CONNECTION_ID.equals(selectedId)) {
                    bundle.putString(CONNECTION_ID, selectedId);
                }

                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void fillConnectionSpinner(int resultCode, Bundle resultData, ConnectionListAdapter connectionListAdapter) {
        if (resultCode == ResultCodes.SUCCESS && resultData != null &&
                resultData.containsKey(CONNECTION_LIST) && connectionListAdapter != null) {

            List<FHEMServerSpec> connectionList = (ArrayList<FHEMServerSpec>)
                    resultData.getSerializable(CONNECTION_LIST);
            assert connectionList != null;

            for (FHEMServerSpec serverSpec : new ArrayList<FHEMServerSpec>(connectionList)) {
                if (serverSpec.getServerType() == ServerType.DUMMY) {
                    connectionList.remove(serverSpec);
                }
            }

            FHEMServerSpec current = new FHEMServerSpec(CURRENT_CONNECTION_ID);
            current.setName(getResources().getString(R.string.connectionCurrent));
            connectionList.add(0, current);

            connectionListAdapter.updateData(connectionList, selectedId);
            selectConnection(connectionListAdapter);
        }
    }

    private void selectConnection(ConnectionListAdapter connectionListAdapter) {
        Spinner spinner = (Spinner) findViewById(R.id.connectionList);
        List<FHEMServerSpec> data = connectionListAdapter.getData();
        for (int i = 0; i < data.size(); i++) {
            FHEMServerSpec spec = data.get(i);
            if (spec.getId().equals(selectedId)) {
                spinner.setSelection(i);
                break;
            }
        }
    }
}
