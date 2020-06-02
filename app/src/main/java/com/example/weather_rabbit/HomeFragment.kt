package com.example.weather_rabbit

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_location.*
import kotlinx.android.synthetic.main.main_toolbar.*
import java.util.concurrent.atomic.LongAccumulator

class HomeFragment : Fragment(){
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
    }

    /* You should inflate your layout in onCreateView but shouldn't initialize other views using findViewById in onCreateView.
    Because sometimes view is not properly initialized.
    So always use findViewById in onViewCreated(when view is fully created) and it also passes the view as parameter.
    onViewCreated is a make sure that view is fully created. */

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

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
}
