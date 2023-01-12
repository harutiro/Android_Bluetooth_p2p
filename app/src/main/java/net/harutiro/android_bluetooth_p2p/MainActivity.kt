package net.harutiro.android_bluetooth_p2p

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import net.harutiro.android_bluetooth_p2p.MainActivity.Companion.BLUETOOTH_REQUEST_CODE
import pub.devrel.easypermissions.EasyPermissions


class MainActivity : AppCompatActivity() , EasyPermissions.PermissionCallbacks {

    val TAG = "MainActivity"

    companion object {
        private val REQUEST_CODE = 0
        private val BLUETOOTH_REQUEST_CODE = 1
    }

    // EasyPermissionについての記事
    // https://qiita.com/kaleidot725/items/fa31476d7b7076265b3d
    val permissions = arrayOf(
        Manifest.permission.BLUETOOTH,
        Manifest.permission.BLUETOOTH_ADMIN,
        Manifest.permission.ACCESS_COARSE_LOCATION
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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        @SuppressLint("MissingPermission")
        if(!EasyPermissions.hasPermissions(this, *permissions)){
            val reqEnableBTIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(reqEnableBTIntent, BLUETOOTH_REQUEST_CODE)
        }


        // パーミッションが許可されていない時の処理
        if (!EasyPermissions.hasPermissions(this, *permissions)) {
            Log.d(TAG,"パーミッションが許可されてません。")
            //許可をもらう部分。
            EasyPermissions.requestPermissions(this, "パーミッションに関する説明", REQUEST_CODE, *permissions)

        }else{
            Log.d(TAG,"パーミッションが許可されました。")
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