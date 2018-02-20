package com.ivanasen.smarttickets

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup


/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [MyTicketsFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [MyTicketsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MyTicketsFragment : Fragment() {


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.fragment_my_tickets, container, false)
    }

}
