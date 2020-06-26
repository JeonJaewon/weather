package com.example.weather_rabbit
import android.Manifest
import android.content.Context.LOCATION_SERVICE
import android.content.Context.MODE_PRIVATE
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_location.*
import java.io.*

class LocationFragment : Fragment() {
    val TO_GRID = 0
    val TO_GPS = 1
    val LOCATION_NOT_SET = 500.0
    lateinit var locationManager : LocationManager
    var curLatitude = LOCATION_NOT_SET
    var curLongitude = LOCATION_NOT_SET

    lateinit var bundle : Bundle

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_location, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val file = context?.getFileStreamPath("defaultLocation")
        if (file != null && file!!.exists()) {
            val os = activity?.openFileInput("defaultLocation")
            val br = BufferedReader(InputStreamReader(os))
            val str = br.readLine()
            if (str != null) {
                val x = str.toDouble()
                val y = br.readLine().toDouble()
                val gps = convertGRID_GPS(TO_GPS, x, y)!!
                val geocoder = Geocoder(activity)
                val resultList = geocoder.getFromLocation(gps.lat, gps.lng, 1)
                this.curLatitude = gps.lat
                this.curLongitude = gps.lng
                locationTextView.text = resultList[0].countryName +" "+ resultList[0].adminArea
            }
        }
    }
    override fun onResume() {
        super.onResume()
        activity?.invalidateOptionsMenu()
    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.loaction_menu, menu)
//        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.location_back -> {
                val fm = activity?.supportFragmentManager
                val transaction = fm?.beginTransaction()
                val frag = HomeFragment()

                bundle = Bundle()
                //위치 정보 수신 종료
                locationManager = activity?.getSystemService(LOCATION_SERVICE) as LocationManager
                locationManager?.removeUpdates(locationListener)

                // 위치정보 프래그먼트 전달
                if(curLatitude != LOCATION_NOT_SET && curLongitude != LOCATION_NOT_SET){
                    val tmp = convertGRID_GPS(TO_GRID, curLatitude, curLongitude)
                    bundle.putDouble("curX", tmp!!.x)
                    bundle.putDouble("curY", tmp!!.y)
                    bundle.putString("locationText", locationTextView.text.toString())
                    frag.arguments = bundle

                    // 내부 저장소에 기본 위치 저장
                    val os = activity?.openFileOutput("defaultLocation", MODE_PRIVATE)
                    val bw = BufferedWriter(OutputStreamWriter(os))
                    // 개행 여부로 x, y 구분
                    bw.write(tmp.x.toString() + "\n")
                    bw.write(tmp.y.toString() + "\n")
                    bw.write(locationTextView.text.toString())
                    bw.flush()
                }

                // 동일한 프래그먼트가 스택에 여러개 쌓이지 않게..
                fm?.popBackStack(this.javaClass.simpleName, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                transaction?.replace(R.id.main_frag_container, frag)?.commit()
//                transaction?.addToBackStack(this.javaClass.simpleName)

                return true
            }
            R.id.location_plus -> {
                getCurLocation()
                return true
            }
        }
        return false
//        return super.onOptionsItemSelected(item)
    }

    private fun getCurLocation(){
        // 위치 정보 권한 허용인지
        if (ContextCompat.checkSelfPermission(activity!!.applicationContext, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            try {
                locationManager = activity?.getSystemService(LOCATION_SERVICE) as LocationManager
                locationManager?.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000L, 0f, locationListener)
                locationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 0f, locationListener)
            } catch(ex: SecurityException) {
                Log.d("NO_LOCATION", "Security Exception, no location available")
            }
        } else {
            ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 100)

        }
    }

    private val locationListener : LocationListener = object : LocationListener{
        override fun onLocationChanged(location: Location?) {
            if(location != null){
                curLatitude = location.latitude
                curLongitude = location.longitude
            }
//            Toast.makeText(activity, location?.longitude.toString() + ", " + location?.latitude, Toast.LENGTH_SHORT).show()
            Toast.makeText(activity, "위치 정보 수신 완료!", Toast.LENGTH_SHORT).show()
            val geocoder = Geocoder(activity)
            val resultList = geocoder.getFromLocation(curLatitude, curLongitude, 1)
            locationTextView.text = resultList[0].countryName +" "+ resultList[0].adminArea
            locationManager = activity?.getSystemService(LOCATION_SERVICE) as LocationManager
            locationManager?.removeUpdates(this)
        }

        override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {
        }

        override fun onProviderEnabled(p0: String?) {
        }

        override fun onProviderDisabled(p0: String?) {
        }
    }

    // gps 위도, 경도 정보와 기상청 그리드 x,y 좌표 사이의 변환 함수
    // 출처: https://gist.github.com/fronteer-kr/14d7f779d52a21ac2f16
    private fun convertGRID_GPS(
        mode: Int,
        lat_X: Double,
        lng_Y: Double
    ): LatXLngY? {
        val RE = 6371.00877 // 지구 반경(km)
        val GRID = 5.0 // 격자 간격(km)
        val SLAT1 = 30.0 // 투영 위도1(degree)
        val SLAT2 = 60.0 // 투영 위도2(degree)
        val OLON = 126.0 // 기준점 경도(degree)
        val OLAT = 38.0 // 기준점 위도(degree)
        val XO = 43.0 // 기준점 X좌표(GRID)
        val YO = 136.0 // 기1준점 Y좌표(GRID)

        //
        // LCC DFS 좌표변환 ( code : "TO_GRID"(위경도->좌표, lat_X:위도,  lng_Y:경도), "TO_GPS"(좌표->위경도,  lat_X:x, lng_Y:y) )
        //
        val DEGRAD = Math.PI / 180.0
        val RADDEG = 180.0 / Math.PI
        val re = RE / GRID
        val slat1 = SLAT1 * DEGRAD
        val slat2 = SLAT2 * DEGRAD
        val olon = OLON * DEGRAD
        val olat = OLAT * DEGRAD
        var sn =
            Math.tan(Math.PI * 0.25 + slat2 * 0.5) / Math.tan(Math.PI * 0.25 + slat1 * 0.5)
        sn =
            Math.log(Math.cos(slat1) / Math.cos(slat2)) / Math.log(
                sn
            )
        var sf = Math.tan(Math.PI * 0.25 + slat1 * 0.5)
        sf = Math.pow(sf, sn) * Math.cos(slat1) / sn
        var ro = Math.tan(Math.PI * 0.25 + olat * 0.5)
        ro = re * sf / Math.pow(ro, sn)
        val rs = LatXLngY()
        if (mode == TO_GRID) {
            rs.lat = lat_X
            rs.lng = lng_Y
            var ra =
                Math.tan(Math.PI * 0.25 + lat_X * DEGRAD * 0.5)
            ra = re * sf / Math.pow(ra, sn)
            var theta = lng_Y * DEGRAD - olon
            if (theta > Math.PI) theta -= 2.0 * Math.PI
            if (theta < -Math.PI) theta += 2.0 * Math.PI
            theta *= sn
            rs.x = Math.floor(ra * Math.sin(theta) + XO + 0.5)
            rs.y = Math.floor(ro - ra * Math.cos(theta) + YO + 0.5)
        } else {
            rs.x = lat_X
            rs.y = lng_Y
            val xn = lat_X - XO
            val yn = ro - lng_Y + YO
            var ra = Math.sqrt(xn * xn + yn * yn)
            if (sn < 0.0) {
                ra = -ra
            }
            var alat = Math.pow(re * sf / ra, 1.0 / sn)
            alat = 2.0 * Math.atan(alat) - Math.PI * 0.5
            var theta = 0.0
            if (Math.abs(xn) <= 0.0) {
                theta = 0.0
            } else {
                if (Math.abs(yn) <= 0.0) {
                    theta = Math.PI * 0.5
                    if (xn < 0.0) {
                        theta = -theta
                    }
                } else theta = Math.atan2(xn, yn)
            }
            val alon = theta / sn + olon
            rs.lat = alat * RADDEG
            rs.lng = alon * RADDEG
        }
        return rs
    }
    internal class LatXLngY {
        var lat = 0.0
        var lng = 0.0
        var x = 0.0
        var y = 0.0
    }
}
