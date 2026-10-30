package com.example

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.webkit.JsResult
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient

/** Uses the same authenticated V3.5 interface as the web application. */
class MainActivity : Activity() {
    private lateinit var browser: WebView
    private val appHost = "vowii-ai-ea-v3-5.vercel.app"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        browser = WebView(this)
        browser.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = false
            allowContentAccess = false
            mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
        }
        browser.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                return request.url.scheme != "https" || request.url.host != appHost
            }
        }
        browser.webChromeClient = object : WebChromeClient() {
            override fun onJsConfirm(view: WebView, url: String, message: String, result: JsResult): Boolean {
                if (android.net.Uri.parse(url).host != appHost) { result.cancel(); return true }
                AlertDialog.Builder(this@MainActivity).setTitle("Confirm real trade")
                    .setMessage(message).setPositiveButton("Submit") { _, _ -> result.confirm() }
                    .setNegativeButton("Cancel") { _, _ -> result.cancel() }
                    .setOnCancelListener { result.cancel() }.show()
                return true
            }
        }
        setContentView(browser)
        browser.loadUrl("https://$appHost/")
    }
    override fun onDestroy() {
        browser.destroy()
        super.onDestroy()
    }
}

