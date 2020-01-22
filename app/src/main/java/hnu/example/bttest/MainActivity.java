package hnu.example.bttest;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements IBTMsgClient {

    //Deklarieren der Komponenten in MainActivity
    private Button mButton_Connect;
    private Button mButton_Disconnect;
    private Button mButton_ChangeActivity2;

    private ListView mDeviceList;

    private TextView mBluetoothStatus;
    private TextView mTextAnzParcels;

    private long mAnzParcels = 0L;

    //reaction after selection of an BT-Device to connect
    private AdapterView.OnItemClickListener myListClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView av, View v, int arg2, long arg3) {
            // Get the device MAC address, the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            final String address = info.substring(info.length() - 17);

            mBluetoothStatus.setText("Connecting...");
            mDeviceList.setVisibility(View.INVISIBLE);
            BTManager.connect(address);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mButton_Connect = (Button) findViewById(R.id.button_connect);
        mButton_Disconnect = (Button) findViewById(R.id.button_disconnect);
        mButton_ChangeActivity2 = (Button) findViewById(R.id.button_CTC);

        mDeviceList = (ListView) findViewById(R.id.listView);
        mBluetoothStatus = (TextView) findViewById(R.id.bluetoothStatus);
        mTextAnzParcels = (TextView) findViewById(R.id.text_anzParcels);

        setConnectButtons(false);


        //init the BTManager to handle the connections to BTdevices
        BTManager.init(this, false); //we are using the BTManager in Test-Mode

        //add the activity as listener for messages
        BTManager.addBTMsgClient(this);


        //disconnect the BT-device
        mButton_Disconnect.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                BTManager.cancel();
            }
        });

        //click event to show the paired devices
        mButton_Connect.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                pairedDevicesList();
            }
        });


        //Test change Activity
        mButton_ChangeActivity2.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //Change the Activity
                startActivity(2);
            }
        });

        //we don't want see the keyboard emulator on start
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    private void startActivity(int nr) {
        if (nr == 2) {
            Intent launchMain2Activity = new Intent(this, Activity2.class);
            startActivity(launchMain2Activity);
        }
    }

    //get all paired devices from BTManager
    private void pairedDevicesList() {
        mDeviceList.setVisibility(View.VISIBLE);

        ArrayList list = BTManager.getPairedDeviceInfos();

        if (list.size() > 0) {
            final ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, list);
            mDeviceList.setAdapter(adapter);
            mDeviceList.setOnItemClickListener(myListClickListener); //Method called when the device from the list is clicked
        } else {
            Toast.makeText(getApplicationContext(), "No Paired Bluetooth Devices Found.", Toast.LENGTH_LONG).show();
        }
    }

    //adapt GUI corresponding to connect status
    private void setConnectButtons(boolean isConnected) {
        mButton_Disconnect.setEnabled(isConnected);
        mButton_Connect.setEnabled(!isConnected);

        if (isConnected == false) {
            mBluetoothStatus.setText("Not Connected");
            mAnzParcels = 0L;
            mTextAnzParcels.setText(mAnzParcels + "");
        } else {
            mBluetoothStatus.setText("Connected");
        }
    }

    @Override
    public void receiveMessage(final String msg) {
        Log.d("msg_tag", msg);
        mBluetoothStatus.setText(msg);
        mAnzParcels++;
        mTextAnzParcels.setText(mAnzParcels + "");
    }

    @Override
    public void receiveConnectStatus(final boolean isConnected) {
        setConnectButtons(isConnected);
    }

    @Override
    public void handleException(final Exception e) {
        mBluetoothStatus.setText("-");
        setConnectButtons(false);
        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
    }

}
