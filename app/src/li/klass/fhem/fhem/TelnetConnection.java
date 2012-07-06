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

package li.klass.fhem.fhem;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;
import android.util.Log;
import li.klass.fhem.AndFHEMApplication;
import li.klass.fhem.exception.AndFHEMException;
import li.klass.fhem.exception.HostConnectionException;
import li.klass.fhem.exception.TimeoutException;
import li.klass.fhem.util.CloseableUtil;
import org.apache.http.conn.ConnectTimeoutException;
import thor.net.DefaultTelnetTerminalHandler;
import thor.net.TelnetURLConnection;

import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TelnetConnection implements FHEMConnection {
    public static final String TELNET_URL = "TELNET_URL";
    public static final String TELNET_PORT = "TELNET_PORT";
    public static final String TELNET_PASSWORD = "TELNET_PASSWORD";

    public static final String FHEM_PROMPT = "fhem>";

    private static final String DEFAULT_HOST = "";
    private static final int DEFAULT_PORT = 0;

    public static final TelnetConnection INSTANCE = new TelnetConnection();
    private static final String PASSWORD_PROMPT = "Password: ";

    private TelnetConnection() {}

    public String xmllist() {
        return request("xmllist", "</FHZINFO>");
    }

    @Override
    public String fileLogData(String logName, Date fromDate, Date toDate, String columnSpec) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH:mm");
        String command = "get " + logName + " - - " + dateFormat.format(fromDate) + " " + dateFormat.format(toDate) + " " + columnSpec;

        return request(command, "#" + columnSpec);
    }

    public String executeCommand(String command) {
        return request(command, null);
    }

    @Override
    public Bitmap requestBitmap(String relativePath) {
        Log.e(TelnetConnection.class.getName(), "get image: " + relativePath);
        return null;
    }

    private String request(String command, String delimiter) {
        Log.i(TelnetConnection.class.getName(), "executeTask command " + command + " with delimiter " + delimiter);

        OutputStream outputStream = null;
        BufferedOutputStream bufferedOutputStream = null;
        PrintWriter printWriter = null;
        InputStream inputStream = null;

        try {
            URL url = new URL("telnet", getHost(), getPort(), "", new thor.net.URLStreamHandler());
            URLConnection urlConnection = url.openConnection();
            urlConnection.connect();

            if (urlConnection instanceof TelnetURLConnection) {
                ((TelnetURLConnection)urlConnection).
                        setTelnetTerminalHandler(new DefaultTelnetTerminalHandler());
            }
            urlConnection.setConnectTimeout(4000);

            inputStream = urlConnection.getInputStream();


            outputStream = urlConnection.getOutputStream();
            bufferedOutputStream = new BufferedOutputStream(outputStream);
            printWriter = new PrintWriter(bufferedOutputStream);

            handlePasswordVerification(inputStream, printWriter);

            printWriter.write(command + "\r\n");
            printWriter.write("exit" + "\r\n");
            printWriter.flush();

            int ch;
            StringBuilder buffer = new StringBuilder();
            while ((ch = inputStream.read()) != -1) {
                buffer.append((char) ch);
            }

            if (urlConnection instanceof  TelnetURLConnection) {
                ((TelnetURLConnection) urlConnection).disconnect();
            }

            String returnValue = buffer.toString();
            int startPos = returnValue.indexOf(", try help");
            if (startPos != -1) {
                returnValue = returnValue.substring(startPos + ", try help".length());
            }
            if (returnValue.endsWith("Bye...")) {
                returnValue = buffer.substring(0, buffer.length() - "Bye...".length());
            }
            return returnValue;
        } catch (AndFHEMException e) {
            throw e;
        } catch (ConnectTimeoutException e) {
            throw new TimeoutException(e);
        } catch (Exception e) {
            Log.e(TelnetConnection.class.getName(), "error occurred", e);
            throw new HostConnectionException(e);
        } finally {
            CloseableUtil.close(printWriter, bufferedOutputStream, outputStream, inputStream);
        }
    }

    private boolean handlePasswordVerification(InputStream inputStream, PrintWriter printWriter) throws Exception {
        String password = getPassword();
        if (password == null || "".equals(password)) return true;

        printWriter.write(password + "\n\r");
        printWriter.flush();

        return waitForPasswordPrompt(inputStream, printWriter);
    }

    private boolean waitForPasswordPrompt(InputStream inputStream, PrintWriter printWriter) throws Exception {
        int ch;
        int passwordPointer = 0;
        int fhemPromptPointer = 0;

        boolean validCharacter;
        while ((ch = inputStream.read()) != -1) {
            validCharacter = false;
            if (ch == PASSWORD_PROMPT.charAt(passwordPointer)) {
                passwordPointer ++;
                validCharacter = true;
            } else {
                passwordPointer = 0;
            }

            if (passwordPointer >= PASSWORD_PROMPT.length()) {
                return true;
            }

            if (ch == FHEM_PROMPT.charAt(fhemPromptPointer)) {
                fhemPromptPointer++;
                validCharacter = true;
            } else {
                fhemPromptPointer = 0;
            }

            if (fhemPromptPointer >= FHEM_PROMPT.length()) {
                return false;
            }

            if (! validCharacter) {
                return  false;
            }
        }
        return false;
    }

    private String getHost() {
        return getPreferenceString(TELNET_URL, DEFAULT_HOST);
    }

    private int getPort() {
        String value = getPreferenceString(TELNET_PORT, String.valueOf(DEFAULT_PORT));
        return Integer.valueOf(value);
    }

    private String getPassword() {
        return getPreferenceString(TELNET_PASSWORD, "");
    }

    private String getPreferenceString(String key, String defaultValue) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(AndFHEMApplication.getContext());
        return sharedPreferences.getString(key, defaultValue);
    }
}