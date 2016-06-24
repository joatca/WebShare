/* This file is part of Web Share.

    Web Share is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Foobar is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Foobar.  If not, see <http://www.gnu.org/licenses/>.

    Copyright 2016 Fraser McCrossan
 */

package com.coffree.webshare

import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import java.net.MalformedURLException
import java.net.URL
import org.jetbrains.anko.*
import java.util.*

class MainActivity : AppCompatActivity() {

    val TAG = "MainActivity"

    val services: Map<String, (String, String) -> String> = mapOf(
            "Facebook" to { u, t -> "https://www.facebook.com/sharer.php?u=$u" }, // FB only takes the link
            "Twitter" to { u, t -> "https://twitter.com/intent/tweet?text=$t${Uri.encode(" ")}$u" }, // Twitter can take all the text
            "Google+" to { u, t -> "https://plus.google.com/share?url=$u" }, // G+ also only takes the link
            "Reddit" to { u, t -> "https://www.reddit.com/submit?url=$u&title=$t"}, // Reddit can take a title
            "LinkedIn" to { u, t -> "https://www.linkedin.com/shareArticle?mini=true&url=$u&title=$t&summary=&source=" }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Intent.ACTION_SEND.equals(intent.action) && intent.type != null && "text/plain".equals(intent.type)) {
            shareToWeb()
        } else {
            showMainScreen()
        }
    }

    fun shareToWeb() {
        // find first URL and other non-URL words
        var url: String = ""
        var words = ArrayList<String>()
        intent.getStringExtra(Intent.EXTRA_TEXT).splitToSequence(" ").forEach { word ->
            if (url.length == 0) {
                try {
                    val urlObject = URL(word)
                    url = word
                } catch (e: MalformedURLException) {
                    words.add(word)
                }
            } else {
                words.add(word)
            }
        }
        val text = Uri.encode(words.joinToString(separator = " "))
        val prefs = getPreferences(MODE_PRIVATE)
        val enabledServices = services.filterKeys { name -> prefs.getBoolean(name, false) }.toList()
        if (enabledServices.size == 1) {
            val (name, lamb) = enabledServices[0]
            webIntent(lamb.invoke(url, text))
        }
        else if (enabledServices.size >= 1) {
            val enabledNames = enabledServices.map { it.component1() }
            selector("", enabledNames) { i ->
                val(name, lamb) = enabledServices[i]
                webIntent(lamb.invoke(url, text))
            }
        }
        else {
            // else no services are enabled, might as well show the config screen
            longToast(R.string.no_services_enabled)
            showMainScreen()
        }
    }

    fun webIntent(url: String?) {
        val uri = Uri.parse(url)
        val i = Intent(Intent.ACTION_VIEW)
        i.data = uri
        startActivity(i)
        finish()
    }

    fun showMainScreen() {
        val vmargin = dip(5)
        val hmargin = dip(8)
        val checkHmargin = dip(10)
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
                val prefs = getPreferences(MODE_PRIVATE)
                services.forEach {
                    val (name, lamb) = it
                    checkBox(name) {
                        textSize = 18f
                        setChecked(prefs.getBoolean(name, false))
                        onCheckedChange { button, b ->
                            getPreferences(MODE_PRIVATE).edit().let { prefs ->
                                prefs.putBoolean(name, isChecked)
                                prefs.commit()
                            }
                        }
                    }.lparams(width = matchParent) {
                        topMargin = vmargin
                        horizontalMargin = checkHmargin
                    }
                }
            }
        }
    }
}
