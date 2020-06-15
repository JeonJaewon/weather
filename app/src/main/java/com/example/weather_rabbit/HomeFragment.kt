package com.example.weather_rabbit

import android.icu.util.Calendar
import android.os.AsyncTask
import android.os.Bundle
import android.util.Xml
import android.view.*
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import kotlinx.android.synthetic.main.fragment_home.*
import org.xmlpull.v1.XmlPullParser
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class HomeFragment : Fragment(){
    var curX : Double = 0.0
    var curY : Double = 0.0
    var networkTask = NetworkTask()

    lateinit var t3hText : TextView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
//        if(arguments != null){
//            curY = arguments?.getDouble("curY")!!
//            curX = arguments?.getDouble("curX")!!
//            networkTask.execute(arrayOf(curX, curY)) //매개변수 넣기
//        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        t3hText = t3h_text

        //swipe layout
        swipe_layout.setOnRefreshListener {
            swipe_layout.isRefreshing = true
            if(arguments != null){
                if(networkTask.status == AsyncTask.Status.FINISHED){
                    networkTask = NetworkTask()
                    networkTask.execute(arrayOf(curX,curY)) //매개변수 넣기
                    curY = arguments?.getDouble("curY")!!
                    curX = arguments?.getDouble("curX")!!
                }
            }
            swipe_layout.isRefreshing = false
        }
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        activity?.invalidateOptionsMenu()
        if(arguments != null){
            curY = arguments?.getDouble("curY")!!
            curX = arguments?.getDouble("curX")!!
            networkTask.execute(arrayOf(curX, curY)) //매개변수 넣기
        }
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

    inner class NetworkTask : AsyncTask<Array<Double>, Double, Double>(){
        val apiURL = "http://apis.data.go.kr/1360000/VilageFcstInfoService/getVilageFcst"
        val myKey = "%2BxNPCgo3zjIx4PS1ltDnV0bHyyELDt5Ed6L4GZvyC8ALqZBDlWy6bEda40xrmxyUIjjDBFYnA6t372jmh2SCEQ%3D%3D"
        lateinit var urlBuilder: StringBuilder

        val STEP_CATEGORY = 0
        val STEP_FCST_VALUE = 1

        override fun doInBackground(vararg p0: Array<Double>): Double? {
            // api url 생성
            val calendar =Calendar.getInstance()
            val baseTime = getLastBaseTime(calendar)
            var year = baseTime?.get(Calendar.YEAR).toString()
            var month = zeroAdder(baseTime!!.get(Calendar.MONTH)+1)
            var day = zeroAdder(baseTime!!.get(Calendar.DAY_OF_MONTH))
            var hour = zeroAdder(baseTime!!.get(Calendar.HOUR_OF_DAY))
            var minute = zeroAdder(baseTime!!.get(Calendar.MINUTE))

            urlBuilder = StringBuilder(apiURL)
            urlBuilder.append("?" + URLEncoder.encode("serviceKey", "UTF-8") + "=" + myKey)
            urlBuilder.append("&" + URLEncoder.encode("numOfRows", "UTF-8") + "=" + 10)
            urlBuilder.append("&" + URLEncoder.encode("pageNo", "UTF-8") + "=" + 1)
            urlBuilder.append("&" + URLEncoder.encode("base_date", "UTF-8") + "=" + year + month + day)
            urlBuilder.append("&" + URLEncoder.encode("base_time", "UTF-8") + "=" + hour + minute)
            urlBuilder.append("&" + URLEncoder.encode("nx", "UTF-8") + "=" + p0[0]?.get(0)?.toInt())
            urlBuilder.append("&" + URLEncoder.encode("ny", "UTF-8") + "=" + p0[0]?.get(1)?.toInt())

            var url = URL(urlBuilder.toString())
            var connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("Content-Type", "application/xml")

            val parser = Xml.newPullParser()
            var step = -1
            var eventType = parser.eventType
            var t3hValue : Double = 0.0
            var isT3H = false
            parser.setInput(InputStreamReader(connection.inputStream))
            while(eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    when(parser.name){
                        "category" -> {
                            if(parser.nextText() == "T3H"){
                                step = STEP_CATEGORY
                                isT3H = true
                            }
                        }
                        "fcstValue" -> {
                            if(isT3H){
                                t3hValue = parser.nextText().toDouble()
                                isT3H = false
                            }
                        }
                    }
                }
                eventType = parser.next()
            }
            return t3hValue
//            return connection.responseCode.toString()
        }

        override fun onPostExecute(result: Double?) {
            super.onPostExecute(result)
            t3hText.text = result.toString()
        }

        // 일의자리 수일 경우 앞에 0 붙이기
        private fun zeroAdder(num : Int) : String {
            if(num <= 9){
                val str = "0" + (num).toString()
                return str
            } else {
                return (num).toString()
            }
        }

        private fun getLastBaseTime(calBase: Calendar): Calendar? {
            val baseArr = arrayOf(2, 5, 8, 11, 14, 17, 20, 23)
            val hour = calBase[Calendar.HOUR_OF_DAY]
            val minute = calBase[Calendar.MINUTE]
            if (hour < 2) {
                calBase.add(Calendar.DATE, -1)
                calBase[Calendar.HOUR_OF_DAY] = 23
            } else {
                calBase[Calendar.HOUR_OF_DAY] = hour - (hour + 1) % 3
                for(i in 0..7){
                    if(hour == baseArr[i]) {
                        // 10분 후에야 정보를 받아올 수 있음..
                        if(minute <= 10){
                            if(i - 1 == -1){
                                calBase.add(Calendar.DATE, -1)
                                calBase[Calendar.HOUR_OF_DAY] = baseArr[7]
                            }
                            else
                                calBase[Calendar.HOUR_OF_DAY] = baseArr[i-1]
                            break
                        }
                    }
                }
            }
            calBase[Calendar.MINUTE] = 0
            return calBase
        }
    }

}
