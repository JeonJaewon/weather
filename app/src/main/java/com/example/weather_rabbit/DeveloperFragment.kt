package com.example.weather_rabbit

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment

class DeveloperFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_developer, container, false)
    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.back_only_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        return super.onOptionsItemSelected(item)
        when(item.itemId){
            R.id.dev_back -> {
                val fm = activity?.supportFragmentManager
                val transaction = fm?.beginTransaction()
                val frag = HomeFragment()
                transaction?.replace(R.id.main_frag_container, frag)?.commit()
                return true
            }
        }
        return false
    }
}