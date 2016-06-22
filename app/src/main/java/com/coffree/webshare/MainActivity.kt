package com.coffree.webshare

import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import java.net.MalformedURLException
import java.net.URL
import java.util.*
import org.jetbrains.anko.*

class MainActivity : AppCompatActivity() {

    val TAG = "MainActivity"

    val services: Map<String, (String) -> String> = mapOf(
            "Facebook" to { u -> "https://www.facebook.com/sharer.php?u=${u}" },
            "Twitter" to { u -> "https://twitter.com/home?status=${u}" },
            "Google+" to { u -> "https://plus.google.com/share?url=${u}" },
            "LinkedIn" to { u -> "https://www.linkedin.com/shareArticle?mini=true&url=${u}&title=&summary=&source=" }
    )

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
                }
                catch (e : MalformedURLException) {
                    // not a URL, oops
                }
            }
        } else {
            //setContentView(R.layout.activity_main)
            val vmargin = dip(5)
            val hmargin = dip(8)
            val checkHmartin = dip(10)
            verticalLayout {
                textView {
                    textResource = R.string.service_select
                    textSize = 18f
                }.lparams(width = matchParent) {
                    verticalMargin = vmargin
                    horizontalMargin = hmargin
                }
                textView {
                    textResource = R.string.service_explain
                    textSize = 14f
                }.lparams(width = matchParent) {
                    bottomMargin = vmargin
                    horizontalMargin = hmargin
                }
                verticalLayout {
                    services.forEach {
                        val (name, lamb) = it
                        checkBox(name) {
                            textSize = 18f
                            setChecked(getPreferences(MODE_PRIVATE).getBoolean(name, false))
                            onCheckedChange { button, b ->
                                getPreferences(MODE_PRIVATE).edit().let { prefs ->
                                    prefs.putBoolean(name, isChecked)
                                    prefs.commit()
                                }
                            }
                        }.lparams(width = matchParent) {
                            topMargin = vmargin
                            horizontalMargin = checkHmartin
                        }
                    }
                }
            }
        }
    }
}
