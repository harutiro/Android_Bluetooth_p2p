package net.harutiro.android_bluetooth_p2p

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import pub.devrel.easypermissions.EasyPermissions


class MainActivity : AppCompatActivity() , EasyPermissions.PermissionCallbacks {

    val TAG = "MainActivity"

    companion object {
        private val REQUEST_CODE = 0
        private val BLUETOOTH_REQUEST_CODE = 1
    }

    var bluetoothManager: BluetoothManager? = null
    var bluetoothAdapter: BluetoothAdapter? = null

    // EasyPermissionについての記事
    // https://qiita.com/kaleidot725/items/fa31476d7b7076265b3d
    @SuppressLint("InlinedApi")
    val permissions = arrayOf(
        Manifest.permission.BLUETOOTH,
        Manifest.permission.BLUETOOTH_ADMIN,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT
    )

    // Create a BroadcastReceiver for ACTION_FOUND.
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d(TAG,intent.action.toString())
            if(intent.action.equals(BluetoothDevice.ACTION_FOUND)){
                val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)

                Log.d(TAG,"検出中")

                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                    val deviceName = device?.name
                    val deviceHardwareAddress = device?.address // MAC address

                    Log.d(TAG,deviceName.toString())
                    Log.d(TAG,deviceHardwareAddress.toString())

                }
            }
        }
    }

    override fun onPermissionsGranted(requestCode: Int, list: List<String>) {
        // ユーザーの許可が得られたときに呼び出される
        //Activityを再読み込み
        recreate()
    }

    override fun onPermissionsDenied(requestCode: Int, list: List<String>) {
        // ユーザーの許可が得られなかったときに呼び出される。
        Log.d(TAG,"パーミッションが許可されてません。")

    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        bluetoothManager = getSystemService(BluetoothManager::class.java)
        bluetoothAdapter = bluetoothManager?.adapter


        // パーミッションが許可されていない時の処理
        if (!EasyPermissions.hasPermissions(this, *permissions)) {
            Log.d(TAG,"パーミッションが許可されてません。")
            //許可をもらう部分。
            EasyPermissions.requestPermissions(this, "パーミッションに関する説明", REQUEST_CODE, *permissions)

        }else{
            Log.d(TAG,"パーミッションが許可されました。")
        }


        //すでにペアリングされているデバイスを取得する。
        findViewById<Button>(R.id.search_button).setOnClickListener{
            val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices
            pairedDevices?.forEach { device ->
                val deviceName = device.name
                val deviceHardwareAddress = device.address // MAC address

                Log.d("$TAG:bluetooth",deviceName)
                Log.d("$TAG:bluetooth",deviceHardwareAddress)
            }
        }

        // Bluetoothをオンにするプログラム。
        findViewById<Button>(R.id.bluetooth_on_button).setOnClickListener {
            if (bluetoothAdapter?.isEnabled == false) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                    startActivityForResult(enableBtIntent, BLUETOOTH_REQUEST_CODE)
                    Log.d(TAG,"パーミッション取れてるよ。")

                }else{
                    Log.d(TAG,"パーミッション取れてないよ。")

                }
            }
        }

        //新しいやつを検索するやつ
        findViewById<Button>(R.id.bluetooth_new_search_button).setOnClickListener {
            // Register for broadcasts when a device is discovered.
            Log.d(TAG,"新しいデバイスを検索をする")

            val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
            registerReceiver(receiver, filter)
        }

        //検出可能にする。
        findViewById<Button>(R.id.bluetooth_discoverable_button).setOnClickListener {
            val requestCode = 1;
            val discoverableIntent: Intent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
                putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
            }
            startActivityForResult(discoverableIntent, requestCode)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == BLUETOOTH_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // "Bluetooth が有効化された"
                Log.d(TAG,"Bluetooth が有効化された")

            }
        }
    }



    override fun onDestroy() {
        super.onDestroy()

        // Don't forget to unregister the ACTION_FOUND receiver.
        unregisterReceiver(receiver)
    }

}