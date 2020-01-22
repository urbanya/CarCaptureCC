package hnu.example.bttest;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Timer;
import java.util.TimerTask;


public class BTManager {
    private static boolean IS_TEST; //switch between simulator and real BT


    private static BTManager instance;

    private static Map<String, IBTMsgClient> allMsgClients = new HashMap<String, IBTMsgClient>();

    private BluetoothAdapter mBTAdapter = null;
    private Set<BluetoothDevice> mPairedDevices;

    private ConnectedThread mConnectedThread; // bluetooth background worker thread to send and receive data
    private BluetoothSocket mBTSocket = null; // bi-directional client-to-client data path
    private DebugTask mDebugTask;
    private Timer mDebugTimer;

    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // "random" unique identifier

    // #defines for identifying shared types between calling functions
    public final static int REQUEST_ENABLE_BT = 1; // used to identify adding bluetooth names
    public final static int MESSAGE_READ = 2; // used in bluetooth handler to identify message update
    public final static int CONNECTING_STATUS = 3; // used in bluetooth handler to identify message status


    public static void addBTMsgClient(IBTMsgClient c) {
        String key = c.getClass().getName();
        allMsgClients.put(key, c);
    }

    /**
     * Constructor
     *
     * @param act
     */

    public static void init(AppCompatActivity act, boolean isTest) {
        IS_TEST = isTest;
        try {
            if (instance == null) {
                instance = new BTManager(act);
            }
        } catch (Exception e) {
            Toast.makeText(act.getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private BTManager(AppCompatActivity act) throws Exception {

        if (IS_TEST) {
            Log.d("BTManager", "constructor: in Test mode");

        } else {

            mBTAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mBTAdapter == null) {
                throw new Exception("Adapter not enabled");
            } else {
                if (mBTAdapter.isEnabled()) {
                } else {
                    //Ask to the user turn the bluetooth on
                    Intent turnBTon = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    act.startActivityForResult(turnBTon, 1);
                }
            }

        }
    }


    /**
     * Get a list of names and MAC-Addresses of all paired devices
     */
    public static ArrayList<String> getPairedDeviceInfos() {
        if (instance != null) {
            return instance.doGetPairedDeviceInfos();
        } else {
            System.out.println("BTManager not yet initialised - use init-Method");
            return new ArrayList();
        }
    }


    private ArrayList<String> doGetPairedDeviceInfos() {

        ArrayList list = new ArrayList();

        if (IS_TEST) {
            //only for Test
            list.add("device1" + "\n" + "98:D3:33:81:17:07"); //Get the device's name and the address
            list.add("device2" + "\n" + "98:D3:34:91:0A:32"); //Get the device's name and the address
        } else {
            Set<BluetoothDevice> pairedDevices = instance.mBTAdapter.getBondedDevices();

            if (pairedDevices != null && pairedDevices.size() > 0) {
                for (BluetoothDevice bt : pairedDevices) {
                    list.add(bt.getName() + "\n" + bt.getAddress()); //Get the device's name and the address
                }
            }

        }
        return list;
    }


    /**
     * write a text to device
     */
    public static void write(String input) {
        if (instance != null) {
            instance.doWrite(input);
        } else {
            System.out.println("BTManager not yet initialised - use init-Method");
        }
    }

    public void doWrite(String input) {
        if (IS_TEST) {
            Log.d("BTManager", "write:" + input);
        } else {
            mConnectedThread.write(input);
        }

    }

    /**
     * close the communication thread
     */
    public static void cancel() {
        if (instance != null) {
            instance.doCancel();
        } else {
            System.out.println("BTManager not yet initialised - use init-Method");
        }
    }

    private void doCancel() {
        if (IS_TEST) {
            if (mDebugTimer != null) {
                mDebugTimer.cancel();
            }
        } else {
            if (mConnectedThread != null) {
                mConnectedThread.cancel();
            }
        }
        sendMyMessages(CONNECTING_STATUS, -1, -1, null);

    }


    /**
     * connect to device for given address
     *
     * @param address
     */
    public static void connect(final String address) {
        if (instance != null) {
            instance.doConnect(address);
        } else {
            System.out.println("BTManager not yet initialised - use init-Method");
        }
    }


    private void doConnect(final String address) {

        new Thread() {
            public void run() {
                boolean fail = false;

                if (IS_TEST) {
                    Log.d("BTManager", "connect in test mode");
                } else {

                    BluetoothDevice device = mBTAdapter.getRemoteDevice(address);

                    try {
                        mBTSocket = createBluetoothSocket(device);
                    } catch (IOException e) {
                        fail = true;
                    }
                    // Establish the Bluetooth socket connection.
                    try {
                        mBTSocket.connect();
                    } catch (IOException e) {
                        try {
                            fail = true;
                            mBTSocket.close();

                            sendMyMessages(CONNECTING_STATUS, -1, 1, null);

                        } catch (IOException e2) {
                            //insert code to deal with this

                        }
                    }
                }
                if (fail == false) {
                    if (IS_TEST) {
                        DebugTask debugTask = new DebugTask();
                        mDebugTimer = new Timer(true);
                        mDebugTimer.scheduleAtFixedRate(debugTask, 0, BTSimulator.INTERVALL_MS);

                    } else {
                        mConnectedThread = new ConnectedThread(mBTSocket);
                        mConnectedThread.start();
                    }
                    sendMyMessages(CONNECTING_STATUS, 1, -1, address);

                }

            }
        }.start();
    }


    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
        //creates secure outgoing connection with BT device using UUID
    }


    class DebugTask extends TimerTask {
        public void run() {
            try {
                Log.d("BTManager:DebugTask", "in timerTask test mode");
                String text = BTSimulator.simulateValue();

                sendMyMessages(MESSAGE_READ, text.length(), -1, text.getBytes());

            } catch (Exception ex) {
                System.out.println(ex);
            }
        }
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()
            String text;

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.available();
                    if (bytes != 0) {
                        SystemClock.sleep(50); //pause and wait for rest of data. Adjust this depending on your sending speed.
                        bytes = mmInStream.available(); // how many bytes are ready to be read?

                        text = "";
                        for (int i = 0; i < bytes; i++) {
                            int val = mmInStream.read();

                            if (val != 10 && val != 13) {
                                text += (char) val;
                            } else if (val == 10) { //new Line
                                if (text.length() > 0) {
                                    sendMyMessages(MESSAGE_READ, text.length(), -1, text.getBytes());
                                }
                                text = "";
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();

                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(String input) {
            byte[] bytes = input.getBytes();           //converts entered String into bytes
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
            }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
        }

    }

    private void sendMyMessages(final int what, final int arg1, final int arg2, final Object obj) {

        for (String key : allMsgClients.keySet()) {
            final IBTMsgClient c = allMsgClients.get(key);
            AppCompatActivity act = (AppCompatActivity) c;

            act.runOnUiThread(new Runnable() {
                public void run() {
                    if (what == BTManager.MESSAGE_READ) {
                        String readMessage = null;
                        try {
                            readMessage = new String((byte[]) obj, "UTF-8");
                            c.receiveMessage(readMessage);
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                            c.handleException(e);
                        }
                    }

                    if (what == BTManager.CONNECTING_STATUS) {

                        if (arg1 == 1)
                            //Connected to device
                            c.receiveConnectStatus(true);
                        else {
                            //Connection failed
                            c.receiveConnectStatus(false);
                        }

                        if (arg2 == 1) {
                            c.handleException(new Exception("Connection Failed"));
                        }

                    }
                }
            });


        }


    }


}
