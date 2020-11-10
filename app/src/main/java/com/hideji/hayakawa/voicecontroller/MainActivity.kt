package com.hideji.hayakawa.voicecontroller

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.felhr.usbserial.UsbSerialDevice
import com.felhr.usbserial.UsbSerialInterface
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    var speechRecognizer: SpeechRecognizer? = null
    var intentRecognizer: Intent? = null
    lateinit var usbManager : UsbManager
    var device : UsbDevice? = null
    var serial : UsbSerialDevice? = null
    var connection : UsbDeviceConnection? = null

    val ACTION_USER_PERMISSION = "permission"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        usbManager = getSystemService(Context.USB_SERVICE) as UsbManager

        val filter = IntentFilter()
        filter.addAction(ACTION_USER_PERMISSION)
        filter.addAction(UsbManager.ACTION_USB_ACCESSORY_ATTACHED)
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
        registerReceiver(broadcastReceiver, filter)

        disconnect.setOnClickListener{ disconnecting() }
        connect.setOnClickListener{ startUsbConnecting() }

        intentRecognizer = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intentRecognizer!!.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer!!.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray) {}
            override fun onEndOfSpeech() {}
            override fun onError(error: Int) {
                speechRecognizer!!.startListening(intentRecognizer)
            }

            override fun onResults(results: Bundle) {
                val matches =
                    results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                var string: String? = ""
                var conc : String =""
                if (matches != null) {
                    string = matches[0]

                    txt.text = string

                    if  (serial != null)
                        sendData(string.replace(' ','\n'))
                }
                speechRecognizer!!.startListening(intentRecognizer)
            }

            override fun onPartialResults(partialResults: Bundle) {}
            override fun onEvent(eventType: Int, params: Bundle) {}
        })

        speechRecognizer!!.startListening(intentRecognizer)
    }

    private fun startUsbConnecting() {
        val usbDevices : HashMap<String, UsbDevice>? = usbManager.deviceList
        if (!usbDevices?.isEmpty()!!) {
            usbDevices.forEach { entry ->
                device = entry.value
                val intent : PendingIntent = PendingIntent.getBroadcast(this, 0, Intent(ACTION_USER_PERMISSION), 0)
                usbManager.requestPermission(device, intent)
            }
        }
    }

    private fun sendData(input : String)
    {
        serial!!.write(input.toByteArray())
    }

    private fun disconnecting()
    {
        serial!!.close()
    }

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION_USER_PERMISSION) {
                val granted: Boolean = intent.extras!!.getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED)
                if ( granted)
                {
                    connection = usbManager.openDevice(device)
                    serial = UsbSerialDevice.createUsbSerialDevice(device, connection)
                    if (serial != null)
                    {
                        serial!!.open()
                        serial!!.setBaudRate(9600)
                        serial!!.setDataBits(UsbSerialInterface.DATA_BITS_8)
                        serial!!.setStopBits(UsbSerialInterface.STOP_BITS_1)
                        serial!!.setParity(UsbSerialInterface.PARITY_NONE)
                        serial!!.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF)
                    }
                }
            }
            else if (intent?.action == UsbManager.ACTION_USB_ACCESSORY_ATTACHED)
            {
                startUsbConnecting()
            }
            else if (intent!!.action == UsbManager.ACTION_USB_ACCESSORY_DETACHED)
            {
                disconnecting()
            }
        }
    }
}