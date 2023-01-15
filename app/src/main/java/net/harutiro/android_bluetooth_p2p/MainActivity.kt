package net.harutiro.android_bluetooth_p2p

import android.Manifest
import android.bluetooth.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import pub.devrel.easypermissions.EasyPermissions
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*


class MainActivity : AppCompatActivity() , EasyPermissions.PermissionCallbacks {

    val TAG = "MainActivity"

    val uuid = "778e74ae-f1dd-4685-81f4-c179c7635381"

    companion object {
        private val REQUEST_CODE = 0
        private val BLUETOOTH_REQUEST_CODE = 1
    }

    var bluetoothManager: BluetoothManager? = null
    var bluetoothAdapter: BluetoothAdapter? = null

    private var isScanning: Boolean = false // スキャン中かどうかのフラグ

    // EasyPermissionについての記事
    // https://qiita.com/kaleidot725/items/fa31476d7b7076265b3d
//    val permissions = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
//        arrayOf(
//            Manifest.permission.ACCESS_COARSE_LOCATION,
//            Manifest.permission.ACCESS_FINE_LOCATION,
//            Manifest.permission.BLUETOOTH_CONNECT,
//            Manifest.permission.BLUETOOTH_SCAN,
//            Manifest.permission.BLUETOOTH_ADVERTISE
//
//        )
//    }else{
//        arrayOf(
//            Manifest.permission.ACCESS_COARSE_LOCATION,
//            Manifest.permission.ACCESS_FINE_LOCATION,
//        )
//    }

    val permissions = arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_ADVERTISE

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

            if (intent.action.equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED)){
                Toast.makeText(
                    context,
                    "Bluetooth検出開始",
                    Toast.LENGTH_SHORT
                ).show()
                Log.d("$TAG discoStart", "Discovery Started")
                isScanning = true
            }

            if(intent.action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)){
                isScanning = false

                Toast.makeText(
                    context,
                    "Bluetooth検出終了",
                    Toast.LENGTH_SHORT
                ).show()
                Log.d("$TAG discoFin", "Discovery finished")
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
//        Log.d(TAG,"パーミッションが許可されてません。")

    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var connectDevice: BluetoothDevice? = null


        bluetoothManager = getSystemService(BluetoothManager::class.java)
        bluetoothAdapter = bluetoothManager?.adapter

        val sendAndReceiveThread = SendAndReceiveThread(this)




        // パーミッションが許可されていない時の処理
        if (!EasyPermissions.hasPermissions(this, *permissions)) {
            Log.d(TAG,"パーミッションが許可されてません。許可部分")
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

                //TODO:接続できるスマホをここで指定しています。
                //pixel7
                if(deviceHardwareAddress == "D4:3A:2C:79:62:33"){
                    connectDevice = device
                }

                //AQUOS sense4 lite
                if(deviceHardwareAddress == "AC:A8:8E:EA:3C:1D"){
                    connectDevice = device
                }

                findViewById<TextView>(R.id.connect_name_textView).text = connectDevice?.name
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

            registerReceiver(receiver, IntentFilter(BluetoothDevice.ACTION_FOUND))
            registerReceiver(receiver, IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED))
            registerReceiver(receiver, IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED))

            bluetoothAdapter!!.startDiscovery()


            Toast.makeText(
                this,
                "Update Button Clicked!!",
                Toast.LENGTH_SHORT
            ).show()
        }

        //検出可能にする。
        findViewById<Button>(R.id.bluetooth_discoverable_button).setOnClickListener {
            val requestCode = 1;
            val discoverableIntent: Intent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
                putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
            }
            startActivityForResult(discoverableIntent, requestCode)
        }

        findViewById<Button>(R.id.bluetooth_server_button).setOnClickListener {

            findViewById<TextView>(R.id.conect_type_textview).text = "サーバー"

            val acceptThread = AcceptThread( bluetoothAdapter?.listenUsingInsecureRfcommWithServiceRecord("myapp", UUID.fromString(uuid)),sendAndReceiveThread)
            acceptThread.start()
        }

        findViewById<Button>(R.id.bluetooth_client_button).setOnClickListener {
            findViewById<TextView>(R.id.conect_type_textview).text = "クライアント"

            val connectThread =  ConnectThread(this, connectDevice?.createRfcommSocketToServiceRecord(UUID.fromString(uuid)),sendAndReceiveThread)
            connectThread.start()

        }

        findViewById<Button>(R.id.send_data_button).setOnClickListener {
            val text = findViewById<EditText>(R.id.send_data_edit_text_view).text.toString()
            sendAndReceiveThread.write(text.toByteArray())
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

    private inner class AcceptThread(
        val mmServerSocket: BluetoothServerSocket?,
        val sendAndReceiveThread: SendAndReceiveThread
    ) : Thread() {


        override fun run() {
            // Keep listening until exception occurs or a socket is returned.
            var shouldLoop = true
            while (shouldLoop) {
                val socket: BluetoothSocket? = try {
                    mmServerSocket?.accept()
                } catch (e: IOException) {
                    Log.e(TAG, "Socket's accept() method failed", e)
                    shouldLoop = false
                    null
                }
                socket?.also {
//                    manageMyConnectedSocket(it)
                    Log.d("$TAG:サーバーの接続",it.toString())

                    this@MainActivity.runOnUiThread{
                        findViewById<TextView>(R.id.conect_type_textview).text = "サーバー　接続済み"
                    }

                    sendAndReceiveThread.mmSocket = it

                    mmServerSocket?.close()
                    shouldLoop = false
                }
            }
        }

        // Closes the connect socket and causes the thread to finish.
        fun cancel() {
            try {
                mmServerSocket?.close()
            } catch (e: IOException) {
                Log.e(TAG, "Could not close the connect socket", e)
            }
        }
    }

    private inner class ConnectThread(
        val context: Context,
        val mmSocket: BluetoothSocket? = null,
        val sendAndReceiveThread: SendAndReceiveThread
    ) : Thread() {

        override fun run() {
            // Cancel discovery because it otherwise slows down the connection.
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
                bluetoothAdapter?.cancelDiscovery()
            }


            mmSocket?.use { socket ->
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                socket.connect()

                // The connection attempt succeeded. Perform work associated with
                // the connection in a separate thread.
//                manageMyConnectedSocket(socket)
                Log.d("$TAG:クライアントの接続" , socket.toString())

                this@MainActivity.runOnUiThread{
                    findViewById<TextView>(R.id.conect_type_textview).text = "クライアント　接続済み"
                }

                sendAndReceiveThread.mmSocket = socket



            }
        }

        // Closes the client socket and causes the thread to finish.
        fun cancel() {
            try {
                mmSocket?.close()
            } catch (e: IOException) {
                Log.e(TAG, "Could not close the client socket", e)
            }
        }
    }

    private inner class SendAndReceiveThread(val context: Context) : Thread() {

        var mmSocket: BluetoothSocket? = null

        val MESSAGE_READ: Int = 0
        val MESSAGE_WRITE: Int = 1
        val MESSAGE_TOAST: Int = 2

        private val mmBuffer: ByteArray = ByteArray(1024) // mmBuffer store for the stream

        override fun run() {

            var numBytes: Int // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs.
            while (true) {
                // Read from the InputStream.

                if(mmSocket?.inputStream != null){
                    numBytes = try {
                        mmSocket?.inputStream!!.read(mmBuffer)
                    } catch (e: IOException) {
                        Log.d(TAG, "Input stream was disconnected", e)
                        break
                    }

                    // Send the obtained bytes to the UI activity.
//                    val readMsg = handler.obtainMessage(
//                        MESSAGE_READ, numBytes, -1,
//                        mmBuffer)
//                    readMsg.sendToTarget()

                    //TODO:ここの部分で書き換えを行っているのを止める
                    this@MainActivity.runOnUiThread{
                        Log.d("$TAG:受信部分",numBytes.toString())
//                        findViewById<TextView>(R.id.conect_type_textview).text = "クライアント　接続済み"
                    }


                }
            }
        }

        // Call this from the main activity to send data to the remote device.
        fun write(bytes: ByteArray) {


            if(mmSocket?.outputStream != null){
                try {
                    mmSocket?.outputStream?.write(bytes)
                } catch (e: IOException) {
                    Log.e(TAG, "Error occurred when sending data", e)

//                    // Send a failure message back to the activity.
//                    val writeErrorMsg = handler.obtainMessage(MESSAGE_TOAST)
//                    val bundle = Bundle().apply {
//                        putString("toast", "Couldn't send data to the other device")
//                    }
//                    writeErrorMsg.data = bundle
//                    handler.sendMessage(writeErrorMsg)
                    return
                }

                // Share the sent message with the UI activity.
//                val writtenMsg = handler.obtainMessage(
//                    MESSAGE_WRITE, -1, -1, mmBuffer)
//                writtenMsg.sendToTarget()

                Toast.makeText(context,"送信しました",Toast.LENGTH_LONG).show()
                Log.d("$TAG:送信部分","送信したよ")

            }
        }

        // Call this method from the main activity to shut down the connection.
        fun cancel() {
            if(mmSocket != null){
                try {
                    mmSocket!!.close()
                } catch (e: IOException) {
                    Log.e(TAG, "Could not close the connect socket", e)
                }
            }
        }
    }

}

