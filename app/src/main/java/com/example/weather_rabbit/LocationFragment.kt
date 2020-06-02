package com.example.weather_rabbit


import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import kotlinx.android.synthetic.main.main_toolbar.*

/**
 * A simple [Fragment] subclass.
 */
class LocationFragment : Fragment() {

    lateinit var locationManager : LocationManager

    companion object {
        public fun newInstance() : LocationFragment {
            return LocationFragment()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
        if (ContextCompat.checkSelfPermission(activity!!.applicationContext, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            try {
                locationManager = activity?.getSystemService(LOCATION_SERVICE) as LocationManager
                locationManager?.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0L, 0f, locationListener)
                locationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0f, locationListener)
            } catch(ex: SecurityException) {
                Log.d("myTag", "Security Exception, no location available")
            }
        } else {
            ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 100)
        }

    }



    private val locationListener : LocationListener = object : LocationListener{
        override fun onLocationChanged(location: Location?) {
            Toast.makeText(activity, location?.longitude.toString() + ", " + location?.latitude, Toast.LENGTH_SHORT).show()
        }

        override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {
            TODO("Not yet implemented")
        }

        override fun onProviderEnabled(p0: String?) {
            TODO("Not yet implemented")
        }

        override fun onProviderDisabled(p0: String?) {
            TODO("Not yet implemented")
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        init()
        return inflater.inflate(R.layout.fragment_location, container, false)
    }
    override fun onResume() {
        super.onResume()
        activity?.invalidateOptionsMenu()
    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.loaction_menu, menu)
//        super.onCreateOptionsMenu(menu, inflater)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){

            R.id.location_back -> {
                val fm = activity?.supportFragmentManager
                val transaction = fm?.beginTransaction()
                val frag = HomeFragment()
                fm?.popBackStack(this.javaClass.simpleName, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                transaction?.replace(R.id.main_frag_container, frag)?.commit()
                transaction?.addToBackStack(this.javaClass.simpleName)
                return true
            }
            R.id.location_plus -> {
                return true
            }
        }
        return false
//        return super.onOptionsItemSelected(item)
    }

    private fun init() {
        val myActivity = activity as AppCompatActivity
    }

}
