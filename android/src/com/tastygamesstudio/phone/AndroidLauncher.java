package com.tastygamesstudio.phone;

import android.net.wifi.WifiManager;
import android.os.Bundle;

import android.text.format.Formatter;
import android.widget.Toast;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.tastygamesstudio.phone.Phone;

public class AndroidLauncher extends AndroidApplication {
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
		String ipAddress = Formatter.formatIpAddress(wifiManager.getConnectionInfo().getIpAddress());
		Toast.makeText(this, "Server ip : " + ipAddress, Toast.LENGTH_LONG).show();
		initialize(new Phone(ipAddress), config);
	}
}
