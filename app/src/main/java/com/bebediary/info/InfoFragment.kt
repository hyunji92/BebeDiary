package com.bebediary.info

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import com.bebediary.R


class InfoFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_info, container, false)
        val webView = view.findViewById(R.id.web_view) as WebView
        // Force links and redirects to open in the WebView instead of in a browser
        webView.webViewClient = WebViewClient()
        // Enable Javascript
        val webSettings = webView.settings
        webSettings.javaScriptEnabled = true

        webView.loadUrl("http://www.google.com")
        return view

    }
}
