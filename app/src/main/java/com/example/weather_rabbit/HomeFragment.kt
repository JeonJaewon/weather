package com.example.weather_rabbit

import android.os.AsyncTask
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_location.*
import kotlinx.android.synthetic.main.main_toolbar.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.StringBuilder
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class HomeFragment : Fragment(){
    var curX : Double = 0.0
    var curY : Double = 0.0
    lateinit var resText : TextView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if(arguments != null){
            curY = arguments?.getDouble("curY")!!
            curX = arguments?.getDouble("curX")!!
            val task = NetworkTask()
            task.execute(arrayOf(curX,curY)) //매개변수 넣기
        }
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
    }

    /* You should inflate your layout in onCreateView but shouldn't initialize other views using findViewById in onCreateView.
    Because sometimes view is not properly initialized.
    So always use findViewById in onViewCreated(when view is fully created) and it also passes the view as parameter.
    onViewCreated is a make sure that view is fully created. */

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        resText = resCode
        //swipe layout
        swipe_layout.setOnRefreshListener {
            swipe_layout.isRefreshing = true
            Toast.makeText(activity,"das",Toast.LENGTH_SHORT).show()
            swipe_layout.isRefreshing = false
        }
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        activity?.invalidateOptionsMenu()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_menu, menu)
//        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.location_item -> {
                val fm = activity?.supportFragmentManager
                val transaction = fm?.beginTransaction()
                val frag = LocationFragment()
                fm?.popBackStack(this.javaClass.simpleName, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                transaction?.replace(R.id.main_frag_container, frag)?.commit()
                transaction?.addToBackStack(this.javaClass.simpleName)
                return true
            }
            R.id.settings_item -> {
                return true
            }
            else -> {
                return false
//                super.onOptionsItemSelected(item)
            }
        }
        return false
//        return super.onOptionsItemSelected(item)
    }

    inner class NetworkTask : AsyncTask<Array<Double>, Double, String>(){
        val apiURL = "http://apis.data.go.kr/1360000/VilageFcstInfoService/getVilageFcst"
        val myKey = "%2BxNPCgo3zjIx4PS1ltDnV0bHyyELDt5Ed6L4GZvyC8ALqZBDlWy6bEda40xrmxyUIjjDBFYnA6t372jmh2SCEQ%3D%3D"
        lateinit var urlBuilder: StringBuilder

        override fun doInBackground(vararg p0: Array<Double>?): String? {
            // api url 생성
            urlBuilder = StringBuilder(apiURL)
            urlBuilder.append("?" + URLEncoder.encode("serviceKey", "UTF-8") + "=" + myKey)
            urlBuilder.append("&" + URLEncoder.encode("numOfRows", "UTF-8") + "=" + 10)
            urlBuilder.append("&" + URLEncoder.encode("pageNo", "UTF-8") + "=" + 1)
            urlBuilder.append("&" + URLEncoder.encode("base_date", "UTF-8") + "=" + "20200605")
            urlBuilder.append("&" + URLEncoder.encode("base_time", "UTF-8") + "=" + "1400")
            urlBuilder.append("&" + URLEncoder.encode("nx", "UTF-8") + "=" + p0[0]?.get(0)?.toInt())
            urlBuilder.append("&" + URLEncoder.encode("ny", "UTF-8") + "=" + p0[0]?.get(1)?.toInt())

            var url = URL(urlBuilder.toString())
            var connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("Content-Type", "application/xml")

            val br = BufferedReader(InputStreamReader(connection.inputStream))
            var line : String = ""
            line = br.readLine()
            return connection.responseCode.toString()
//        while(!line.equals("")){
//        }
//        Toast.makeText(activity, connection.responseCode, Toast.LENGTH_SHORT).show()
//        Toast.makeText(activity, curX.toString(), Toast.LENGTH_SHORT).show()
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            resText.text = result
        }
    }
}
