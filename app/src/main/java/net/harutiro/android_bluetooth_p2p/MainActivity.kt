package net.harutiro.android_bluetooth_p2p

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import pub.devrel.easypermissions.EasyPermissions


class MainActivity : AppCompatActivity() , EasyPermissions.PermissionCallbacks {

    val TAG = "MainActivity"

    companion object {
        private val REQUEST_CODE = 0
        private val BLUETOOTH_REQUEST_CODE = 1
    }

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

        val bluetoothManager: BluetoothManager = getSystemService(BluetoothManager::class.java)
        val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter

        // パーミッションが許可されていない時の処理
        if (!EasyPermissions.hasPermissions(this, *permissions)) {
            Log.d(TAG,"パーミッションが許可されてません。")
            //許可をもらう部分。
            EasyPermissions.requestPermissions(this, "パーミッションに関する説明", REQUEST_CODE, *permissions)

        }else{
            Log.d(TAG,"パーミッションが許可されました。")
        }


        findViewById<Button>(R.id.search_button).setOnClickListener{

            @SuppressLint("MissingPermission")
            if(EasyPermissions.hasPermissions(this, *permissions)){
                if(bluetoothAdapter?.startDiscovery() == true) {
                    // 発見開始（結果はブロードキャストで取得）
                } else {
                    // 発見開始できず。Bluetooth が無効であるなど。
                }
            }

        }

        findViewById<Button>(R.id.bluetooth_on_button).setOnClickListener {

            @SuppressLint("MissingPermission")
            if(EasyPermissions.hasPermissions(this, *permissions)){
                if (bluetoothAdapter?.isEnabled == false) {
                    val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    startActivityForResult(enableBtIntent, BLUETOOTH_REQUEST_CODE)
                }
            }

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

}