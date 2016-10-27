package com.mysugr.rxbleprototype;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.polidea.rxandroidble.RxBleClient;
import com.polidea.rxandroidble.internal.RxBleLog;

public class MainActivity
		extends AppCompatActivity {

	private BluetoothConnector bluetoothConnector;
	private View scanButton;
	private View connectButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);


		RxBleClient.setLogLevel(RxBleLog.DEBUG);

		bluetoothConnector = new BluetoothConnector(this);

		scanButton = this.findViewById(R.id.scanButton);
		scanButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				bluetoothConnector.scan();
			}
		});

		connectButton = this.findViewById(R.id.connectButton);
		connectButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				bluetoothConnector.connect();
			}
		});

	}


}
