package com.example.vehicleindentifier.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.vehicleindentifier.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.fragment_user_data.*


class UserDataFragment : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user_data, container, false)



    }


    override fun onResume() {
        super.onResume()


        if (arguments?.getBoolean("success") == true){
            animation_bottom_sheet_add_new_message.setAnimation(R.raw.successful)
            animation_bottom_sheet_add_new_message.visibility = View.VISIBLE
            txt_title_bottom_sheet_add_new_message.visibility = View.VISIBLE
            txt_title_bottom_sheet_add_new_message_2.visibility = View.GONE
        }else{
            animation_bottom_sheet_add_new_message.setAnimation(R.raw.failed)

            animation_bottom_sheet_add_new_message.visibility = View.VISIBLE
            txt_title_bottom_sheet_add_new_message.visibility = View.GONE
            txt_title_bottom_sheet_add_new_message_2.visibility = View.VISIBLE
        }
    }




}