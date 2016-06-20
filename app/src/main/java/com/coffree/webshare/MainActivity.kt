package com.coffree.webshare

import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import java.net.MalformedURLException
import java.net.URL

class MainActivity : AppCompatActivity() {

    val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Intent.ACTION_SEND.equals(intent.action) && intent.type != null && "text/plain".equals(intent.type)) {
            // find the first URL
            intent.getStringExtra(Intent.EXTRA_TEXT).splitToSequence(" ").forEach {
                try {
                    val url = URL(it)
                    val uri = Uri.parse("https://www.facebook.com/sharer.php?u=${it}")
                    val i = Intent(Intent.ACTION_VIEW)
                    i.data = uri
                    startActivity(i)
                    finish()
                    return // we are done, don't fall through
                }
                catch (e : MalformedURLException) {
                    // not a URL, oops
                }
            }
        }
        setContentView(R.layout.activity_main)
    }
}
