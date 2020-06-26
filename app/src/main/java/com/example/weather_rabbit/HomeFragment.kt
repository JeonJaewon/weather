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
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class HomeFragment : Fragment() {
    val NOT_SET = -1.0
    var curX: Double = NOT_SET
    var curY: Double = NOT_SET
    var networkTask = NetworkTask()
    lateinit var t3hText: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)

        // 이미 지역 위치가 설정되어 있는지 확인
        val file = context?.getFileStreamPath("defaultLocation")
        if (file != null && file!!.exists()) {
            val os = activity?.openFileInput("defaultLocation")
            val br = BufferedReader(InputStreamReader(os))
            val str = br.readLine()
            if (str != null) {
                curX = str.toDouble()
                curY = br.readLine().toDouble()
                if (networkTask.status != AsyncTask.Status.RUNNING) {
                    if (networkTask.status == AsyncTask.Status.FINISHED) {
                        networkTask = NetworkTask()
                    }
                    networkTask.execute(arrayOf(curX, curY)) //매개변수 넣기
                }
            }
        }
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        t3hText = t3h_text
        if (curX == 0.0 && curY == 0.0)
            t3hText.text = "위치 정보 설정이 필요합니다"
        //swipe layout
        swipe_layout.setOnRefreshListener {
            swipe_layout.isRefreshing = true
            val file = context?.getFileStreamPath("defaultLocation")
            if (file != null && file!!.exists()) {
                val os = activity?.openFileInput("defaultLocation")
                val br = BufferedReader(InputStreamReader(os))
                val str = br.readLine()
                if (str != null) {
                    curX = str.toDouble()
                    curY = br.readLine().toDouble()
                }
                if (networkTask.status == AsyncTask.Status.FINISHED) {
                    networkTask = NetworkTask()
                    networkTask.execute(arrayOf(curX, curY)) //매개변수 넣기
                }
            }
            swipe_layout.isRefreshing = false
        }

        // 홈 프래그먼트의 각종 뷰 날씨에 따라 초기화
        val file = context?.getFileStreamPath("defaultLocation")
        if (file != null && file!!.exists()) {
            val os = activity?.openFileInput("defaultLocation")
            val br = BufferedReader(InputStreamReader(os))
            br.readLine()
            br.readLine()
            val str = br.readLine()
            location_text.text = str
        }
        DisplayInitializer(this).init()
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
        when (item.itemId) {
            R.id.location_item -> {
                val fm = activity?.supportFragmentManager
                val transaction = fm?.beginTransaction()
                val frag = LocationFragment()
                fm?.popBackStack(
                    this.javaClass.simpleName,
                    FragmentManager.POP_BACK_STACK_INCLUSIVE
                )
                transaction?.replace(R.id.main_frag_container, frag)?.commit()
//                transaction?.addToBackStack(this.javaClass.simpleName)
                return true
            }
            R.id.dev_item -> {
                val fm = activity?.supportFragmentManager
                val transaction = fm?.beginTransaction()
                val frag = DeveloperFragment()
                transaction?.replace(R.id.main_frag_container, frag)?.commit()
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

    inner class NetworkTask : AsyncTask<Array<Double>, Double, Double>() {
        val apiURL = "http://apis.data.go.kr/1360000/VilageFcstInfoService/getVilageFcst"
        val myKey =
            "%2BxNPCgo3zjIx4PS1ltDnV0bHyyELDt5Ed6L4GZvyC8ALqZBDlWy6bEda40xrmxyUIjjDBFYnA6t372jmh2SCEQ%3D%3D"
        lateinit var urlBuilder: StringBuilder

        val STEP_NOT_SET = -1
        val STEP_CATEGORY = 0
        val STEP_T3H_VALUE = 1
        val STEP_REH_VALUE = 2
        val STEP_POP_VALUE = 3
        val STEP_TMN_VALUE = 4
        val STEP_TMX_VALUE = 5

        var t3hValue: Double = 0.0
        var rehValue = 0 // 습도
        var popValue = 0 // 강수확률
        var tmnValue: Double = 0.0 // 최저기온
        var tmxValue: Double = 0.0 // 최고기온

        override fun doInBackground(vararg p0: Array<Double>): Double? {
            // api url 생성
            val calendar = Calendar.getInstance()
            val baseTime = getLastBaseTime(calendar)
            var year = baseTime?.get(Calendar.YEAR).toString()
            var month = zeroAdder(baseTime!!.get(Calendar.MONTH) + 1)
            var day = zeroAdder(baseTime!!.get(Calendar.DAY_OF_MONTH))
            var hour = zeroAdder(baseTime!!.get(Calendar.HOUR_OF_DAY))
            var minute = zeroAdder(baseTime!!.get(Calendar.MINUTE))

            urlBuilder = StringBuilder(apiURL)
            urlBuilder.append("?" + URLEncoder.encode("serviceKey", "UTF-8") + "=" + myKey)
            urlBuilder.append("&" + URLEncoder.encode("numOfRows", "UTF-8") + "=" + 10)
            urlBuilder.append("&" + URLEncoder.encode("pageNo", "UTF-8") + "=" + 1)
            urlBuilder.append(
                "&" + URLEncoder.encode(
                    "base_date",
                    "UTF-8"
                ) + "=" + year + month + day
            )
            urlBuilder.append("&" + URLEncoder.encode("base_time", "UTF-8") + "=" + hour + minute)
            urlBuilder.append("&" + URLEncoder.encode("nx", "UTF-8") + "=" + p0[0]?.get(0)?.toInt())
            urlBuilder.append("&" + URLEncoder.encode("ny", "UTF-8") + "=" + p0[0]?.get(1)?.toInt())

            var url = URL(urlBuilder.toString())
            var connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("Content-Type", "application/xml")

            val parser = Xml.newPullParser()
            var step = STEP_NOT_SET
            var eventType = parser.eventType

            var isT3H = false
            parser.setInput(InputStreamReader(connection.inputStream))
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    when (parser.name) {
                        "category" -> {
                            val txt = parser.nextText()
                            if (txt == "T3H") {
                                step = STEP_T3H_VALUE
                            } else if (txt == "REH") {
                                step = STEP_REH_VALUE
                            } else if (txt == "POP") {
                                step = STEP_POP_VALUE
                            } else if (txt == "TMN") {
                                step = STEP_TMN_VALUE
                            } else if (txt == "TMX") {
                                step = STEP_TMX_VALUE
                            }
                        }
                        "fcstValue" -> {
                            val txt = parser.nextText()
                            when (step) {
                                STEP_T3H_VALUE -> {
                                    t3hValue = txt.toDouble()
                                }
                                STEP_REH_VALUE -> {
                                    rehValue = txt.toInt()
                                }
                                STEP_POP_VALUE -> {
                                    popValue = txt.toInt()
                                }
                                STEP_TMN_VALUE -> {
                                    tmnValue = txt.toDouble()
                                }
                                STEP_TMX_VALUE -> {
                                    tmxValue = txt.toDouble()
                                }
                            }
                            step = STEP_NOT_SET
                        }
                    }
                }
                eventType = parser.next()
            }
            return t3hValue
//            return connection.responseCode.toString()
        }

        override fun onPostExecute(result: Double) {
            super.onPostExecute(result)
            t3hText.text = result.toString()
//            high_low_text.text = tmnValue.toString() +"° / "+ tmxValue.toString()+ "°"
            val trans = fragmentManager?.beginTransaction()
            trans?.replace(
                R.id.card_container,
                CardFragment(CardData(this.t3hValue, this.rehValue, this.popValue))
            )
            trans?.commit()
        }

        // 일의자리 수일 경우 앞에 0 붙이기
        private fun zeroAdder(num: Int): String {
            if (num <= 9) {
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
                for (i in 0..7) {
                    if (hour == baseArr[i]) {
                        // 10분 후에야 정보를 받아올 수 있음..
                        if (minute <= 10) {
                            if (i - 1 == -1) {
                                calBase.add(Calendar.DATE, -1)
                                calBase[Calendar.HOUR_OF_DAY] = baseArr[7]
                            } else
                                calBase[Calendar.HOUR_OF_DAY] = baseArr[i - 1]
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
