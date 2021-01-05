package com.faptastic.coroutine

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.coroutines.awaitStringResponseResult
import kotlinx.coroutines.*
// import kotlinx.android.synthetic.main.activity_main.* Don't use this anymore
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result;
import kotlinx.coroutines.Dispatchers.Main


class MainActivity : AppCompatActivity() {

    val logTag = "CoRoutinesExample"
    

    // Basically the ioScope thread. Coroutines are NOT Android OS THREADS
    // Coroutines compete to run within a thread.

    // https://medium.com/better-programming/asynchronous-programming-with-kotlin-coroutines-5b3417f53ac6
    /*
        Dispatchers are used for specifying on which thread a coroutine should be executed. They are similar to schedulers in Rx.
        We can specify on which dispatchers we want the execution of the fetchUser API request (in our case, it is Dispatchers.IO)
     */
    private val ioScope = CoroutineScope(Dispatchers.IO + Job() ) // IO, Main, Default
    private val mainScope = CoroutineScope(Dispatchers.Main + Job() ) // IO, Main, Default

    // UI Element
    //private var mStatusText : TextView? = null
    //private var mStatusText : TextView? = null

    // On Create
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val button_1: Button = findViewById(R.id.button_1)
        val button_2: Button = findViewById(R.id.button_2)

      //  mStatusText = findViewById(R.id.output_textview);


        // https://medium.com/better-programming/asynchronous-programming-with-kotlin-coroutines-5b3417f53ac6
        button_1.setOnClickListener {

            // Do this stuff in the UI co-routine scope
            ioScope.launch {

                setStatusField("")

                Log.i(logTag, "Launched ioScope co-routine.")
                val data = async {

                    Log.i(logTag, "Launched aSync data request.")

                    val (request, response, result) = Fuel.get("https://httpbin.org/ip").responseString()
                    delay(1000)

                    result.fold(
                        { data -> setStatusField(data) /* "{"origin":"127.0.0.1"}" */ },
                        { error -> println("An error of type ${error.exception} happened: ${error.message}") }
                    )

                    Log.i(logTag, "Ended aSync data request.")
                }

                Log.i(logTag, "Completed ioScope co-routine.")

            } // end ioScope.launch
        } // end onClickListener

        button_2.setOnClickListener {

            mainScope.launch {

                // Launch this direct-UI updating task as part of the Main coroutine scope
                var counter = 0
                var status_textview = findViewById<TextView>(R.id.status_textview)

                while(true)
                {
                    status_textview.apply {
                        text = (counter++).toString()
                    }
                    delay(150) // incremenent every second
                    // DO NOT USE Thread.Sleep as it WILL kill the entire mainScope 'Coroutine Scope'
                }
            }


        }
    }

    private suspend fun setStatusField(result : String) // on main thread
    {
        withContext(Main)
        {

            Log.i(logTag, "Populating output text with what was returned from HTTP call asyncronously.")
            // https://developer.android.com/topic/libraries/data-binding is the way to do it
            findViewById<TextView>(R.id.output_textview).apply {
                text = result
            }
        }
    } // end set status


}