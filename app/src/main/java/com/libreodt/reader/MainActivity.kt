package com.libreodt.reader

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Base64
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private var filePathCallback: ValueCallback<Array<Uri>>? = null
    private var pendingIntentUri: Uri? = null
    private var pageLoaded = false

    // Системный выбор .odt файла для <input type="file"> внутри WebView
    private val fileChooserLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            filePathCallback?.onReceiveValue(arrayOf(uri))
        } else {
            filePathCallback?.onReceiveValue(null)
        }
        filePathCallback = null
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        webView = WebView(this)
        setContentView(webView)

        val settings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.allowFileAccess = true
        settings.allowContentAccess = true
        settings.builtInZoomControls = true
        settings.displayZoomControls = false
        settings.useWideViewPort = true
        settings.loadWithOverviewMode = true
        settings.cacheMode = WebSettings.LOAD_DEFAULT
        settings.userAgentString = settings.userAgentString + " LibreODTAndroidAPK"

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                pageLoaded = true
                pendingIntentUri?.let { uri ->
                    loadOdtUriIntoWebView(uri)
                    pendingIntentUri = null
                }
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(
                webView: WebView?,
                callback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                filePathCallback?.onReceiveValue(null)
                filePathCallback = callback
                fileChooserLauncher.launch(arrayOf(
                    "application/vnd.oasis.opendocument.text",
                    "*/*"
                ))
                return true
            }
        }

        // Обработка системной кнопки «Назад»
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack()) {
                    webView.goBack()
                } else {
                    finish()
                }
            }
        })

        handleIncomingIntent(intent)
        webView.loadUrl("file:///android_asset/www/index.html")
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        if (intent?.action == Intent.ACTION_VIEW && intent.data != null) {
            val uri = intent.data!!
            if (pageLoaded) {
                loadOdtUriIntoWebView(uri)
            } else {
                pendingIntentUri = uri
            }
        }
    }

    private fun loadOdtUriIntoWebView(uri: Uri) {
        try {
            val fileName = getDisplayName(uri) ?: "document.odt"
            val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return
            val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
            val safeName = JSONObject.quote(fileName)
            webView.post {
                webView.evaluateJavascript("window.openOdtFromAndroidBase64('$base64', $safeName);", null)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getDisplayName(uri: Uri): String? {
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (idx >= 0 && cursor.moveToFirst()) {
                return cursor.getString(idx)
            }
        }
        return uri.lastPathSegment
    }
}
