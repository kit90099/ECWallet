package com.bc.ecwallet

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.bc.ecwallet.utils.PopUpMenuView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.fragment_main.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MainFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MainFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var fabClicked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view:View=inflater.inflate(R.layout.fragment_main, container, false)

        view.viewTreeObserver.addOnGlobalLayoutListener {
            val pager_main = view.findViewById<View>(R.id.pager_main)
            pager_main.layoutParams.height= view.findViewById<View>(R.id.bottomAppBar).y.toInt()
            pager_main.requestLayout()
        }

        val temp = activity?.supportFragmentManager?.fragments

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bottom_navigation.background = null

        pager_main.adapter=MainFragmentPagerAdaptor(this)
        pager_main.currentItem=0
        pager_main.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                when(position){
                    0->{
                        if(bottom_navigation.selectedItemId==R.id.page_account){
                            bottom_navigation.selectedItemId=R.id.page_overview
                        }
                    }
                    1->{
                        if(bottom_navigation.selectedItemId==R.id.page_overview){
                            bottom_navigation.selectedItemId=R.id.page_account
                        }
                    }
                }
            }
        })

        bottom_navigation.setOnNavigationItemSelectedListener { item ->
            when(item.title){
                "Account"->{
                    pager_main.currentItem=1
                    true
                }
                "Overview"->{
                    pager_main.currentItem=0
                    true
                }
                else->{
                    false
                }
            }
        }
        val popUpMenuView = view.findViewById<PopUpMenuView>(R.id.menu_popup)

        popUpMenuView.setMenu(arrayListOf(
            PopUpMenuView.MenuItem("Paying",R.drawable.ic_pay),
            PopUpMenuView.MenuItem("Receiving",R.drawable.ic_receive)))

        popUpMenuView.addOnItemTouchCallback {
            when(it){
                "Paying"->findNavController().navigate(R.id.action_mainFragment_to_paymentFragment)
                "Receiving"->findNavController().navigate(R.id.action_mainFragment_to_receiveInfoFragment)
            }
        }

        view.findViewById<FloatingActionButton>(R.id.floating_action_button).setOnClickListener{
            popUpMenuView.show()
        }

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment MainFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MainFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    inner class MainFragmentPagerAdaptor(fragment:Fragment):FragmentStateAdapter(fragment){
        override fun getItemCount(): Int =2

        override fun createFragment(position: Int): Fragment {
            when(position){
                0->{
                    return OverviewFragment()
                }
                1->{
                    return AccountFragment()
                }
                else->{
                    return OverviewFragment()
                }
            }
        }


    }
}