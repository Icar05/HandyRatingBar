package com.Icar05.alex.handyratingbar

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        topBar.setRating(3f)
        topBar.setAllowTouch(true)
        bottomBar.setAllowTouch(true)

        green.setOnClickListener{
            showMessage("Rating "+topBar.getRating())
        }

        orange.setOnClickListener{
            showMessage("Rating "+middleBar.getRating())
        }

        red.setOnClickListener{
            showMessage("Rating "+bottomBar.getRating())
        }

    }




   private fun showMessage(message: String){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

}
