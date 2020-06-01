package com.example.weather_rabbit


import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.main_toolbar.*

/**
 * A simple [Fragment] subclass.
 */
class LocationFragment : Fragment() {
    companion object {
        public fun newInstance() : LocationFragment {
            return LocationFragment()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        init()
        return inflater.inflate(R.layout.fragment_location, container, false)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.loaction_menu, menu)
//        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId){
            R.id.location_back -> {
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
        myActivity.setSupportActionBar(main_toolbar)
    }

}
