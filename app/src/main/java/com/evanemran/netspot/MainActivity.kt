package com.evanemran.netspot

import android.content.Context
import android.graphics.Typeface
import android.net.DhcpInfo
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.net.wifi.p2p.WifiP2pDevice
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.widget.ArrayAdapter
import android.text.format.Formatter;
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.res.ResourcesCompat
import com.evanemran.netspot.utils.CustomTypefaceSpan
import java.io.IOException
import java.net.InetAddress


class MainActivity : AppCompatActivity() {

    private lateinit var deviceListView: ListView
    private lateinit var toolBar: Toolbar
    private lateinit var customAdapter: CustomAdapter
//    private lateinit var arrayAdapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        deviceListView = findViewById(R.id.deviceListView)
        toolBar = findViewById(R.id.toolBar)
//        arrayAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1)
        customAdapter = CustomAdapter(this, R.layout.custom_device_item, ArrayList())
        deviceListView.adapter = customAdapter

        setSupportActionBar(toolBar)
        val customTypeface: Typeface? =
            ResourcesCompat.getFont(this, R.font.custom_font_bold)

        if (customTypeface != null) {
            val title = getString(R.string.app_name)
            val spannableString = SpannableString(title)
            spannableString.setSpan(
                CustomTypefaceSpan(customTypeface),
                0,
                title.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            supportActionBar?.title = spannableString
        }

        scanDevices()
    }

    inner class CustomAdapter(
        context: Context,
        private val resource: Int,
        private val devices: ArrayList<String>
    ) : ArrayAdapter<String>(context, resource, devices) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var itemView = convertView
            val holder: ViewHolder

            if (itemView == null) {
                val inflater = LayoutInflater.from(context)
                itemView = inflater.inflate(resource, parent, false)

                holder = ViewHolder()
                holder.deviceNameTextView = itemView.findViewById(R.id.deviceNameTextView)
                holder.deviceInfoTextView = itemView.findViewById(R.id.deviceInfoTextView)

                itemView.tag = holder
            } else {
                holder = itemView.tag as ViewHolder
            }

            val device = devices[position]
            holder.deviceNameTextView.text = device
//            holder.deviceInfoTextView.text = "MAC Address: ${device}"

            return itemView!!
        }
    }

    inner class ViewHolder {
        lateinit var deviceNameTextView: TextView
        lateinit var deviceInfoTextView: TextView
    }

    private fun scanDevices() {
        Thread {
            val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val wifiInfo: WifiInfo = wifiManager.connectionInfo
            val dhcpInfo: DhcpInfo = wifiManager.dhcpInfo

            val ipAddress: String = Formatter.formatIpAddress(wifiInfo.ipAddress)
            val gateway: String = Formatter.formatIpAddress(dhcpInfo.gateway)

            Log.d("NetworkScanner", "Local IP Address: $ipAddress")
            Log.d("NetworkScanner", "Gateway IP Address: $gateway")

            val subnet: String = ipAddress.substring(0, ipAddress.lastIndexOf("."))
            for (i in 1..254) {
                val host: String = "$subnet.$i"

                try {
                    val address = InetAddress.getByName(host)
                    if (address.isReachable(1000)) {
                        runOnUiThread {
                            customAdapter.add(host)
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }.start()
    }
}