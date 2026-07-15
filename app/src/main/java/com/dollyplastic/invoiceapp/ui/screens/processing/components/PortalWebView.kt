package com.dollyplastic.invoiceapp.ui.screens.processing.components

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.dollyplastic.invoiceapp.data.credentials.Credential

@Composable
fun PortalWebView(
    url: String,
    onShowFileChooser: (ValueCallback<Array<Uri>>) -> Boolean,
    onDownload: (String, String, String, String, Long) -> Unit,
    modifier: Modifier = Modifier,
    credentials: List<Credential> = emptyList(),
    onWebViewCreated: (WebView) -> Unit,
    onHtmlScraped: (String) -> Unit, // New callback
    firmIdentifier: String = "UnknownFirm",
    portalMode: String = "EWAY"
) {
    var showErrorDialog by remember { mutableStateOf(false) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    
    AndroidView(
        factory = { context ->

            WebView(context).apply {
                webViewRef = this
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    allowFileAccess = true
                    // Fix for page not fitting screen
                    useWideViewPort = true
                    loadWithOverviewMode = true
                    setSupportZoom(true)
                    builtInZoomControls = true
                    displayZoomControls = false
                    javaScriptCanOpenWindowsAutomatically = true // Allow Print/Popups
                }
                
                // Enable Cookies
                CookieManager.getInstance().setAcceptCookie(true)
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
                }

                // SESSION ISOLATION LOGIC
                val sharedPrefs = context.getSharedPreferences("PortalSessions", Context.MODE_PRIVATE)
                val sessionKey = "${firmIdentifier}_${portalMode}"
                
                // 1. Clear globally leaked cookies from other firms immediately
                CookieManager.getInstance().removeAllCookies(null)
                CookieManager.getInstance().flush()
                
                // 2. Restore this specific firm's cookies
                val savedCookies = sharedPrefs.getString(sessionKey, "")
                if (!savedCookies.isNullOrBlank()) {
                    val cookieArray = savedCookies.split(";")
                    for (cookie in cookieArray) {
                        val cleanCookie = cookie.trim()
                        if (cleanCookie.isNotEmpty()) {
                            CookieManager.getInstance().setCookie(url, cleanCookie)
                        }
                    }
                    android.util.Log.d("InvoiceWorkflow", "[Session] Restored ${cookieArray.size} cookies for $sessionKey")
                } else {
                    android.util.Log.d("InvoiceWorkflow", "[Session] No previous session for $sessionKey")
                }
                CookieManager.getInstance().flush()

                // Enable standard text selection
                isLongClickable = true
                
                webViewClient = object : WebViewClient() {
                    override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                        super.onReceivedError(view, request, error)
                        // Only show for main frame errors (prevent ads/analytics blocking loop)
                        if (request?.isForMainFrame == true) {
                            showErrorDialog = true
                        }
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        // Reset scroll
                        view?.scrollTo(0, 0)
                        
                        // FLUSH COOKIES for persistence and Log them
                        CookieManager.getInstance().flush()
                        val cookies = CookieManager.getInstance().getCookie(url)
                        android.util.Log.d("InvoiceWorkflow", "[PortalWebView] Cookies for $url: ${cookies?.take(50)}... (Length: ${cookies?.length ?: 0})")
                        
                        // Save session for this specific firm
                        if (cookies != null && cookies.isNotEmpty()) {
                            val sharedPrefs = context.getSharedPreferences("PortalSessions", Context.MODE_PRIVATE)
                            val sessionKey = "${firmIdentifier}_${portalMode}"
                            sharedPrefs.edit().putString(sessionKey, cookies).apply()
                            android.util.Log.d("InvoiceWorkflow", "[Session] Saved session for $sessionKey")
                        }
                        
                        // Inject Print Hook
                        view?.evaluateJavascript(
                            "window.print = function() { Android.printPage(); };",
                            null
                        )

                        // Inject CSS for Copy Support (Aggressive)
                        // disable user-select: none everywhere
                        val css = """
                            * { 
                                -webkit-user-select: text !important; 
                                -moz-user-select: text !important;
                                -ms-user-select: text !important;
                                user-select: text !important; 
                            }
                        """.trimIndent().replace("\n", "")
                        
                        val js = """
                            var style = document.createElement('style'); 
                            style.innerHTML = '$css'; 
                            document.head.appendChild(style);
                            
                            // Unblock Context Menu & Selection
                            document.oncontextmenu = null;
                            document.onselectstart = null;
                            document.ondragstart = null;
                            document.onmousedown = null;
                            
                            // Force allow selection on body
                            document.body.style.userSelect = 'text';
                            document.body.style.webkitUserSelect = 'text';
                        """.trimIndent()
                        
                        view?.evaluateJavascript(js, null)
                        
                        // AUTO-FILL CREDENTIALS Logic
                        val tag = "InvoiceWorkflow"
                        android.util.Log.d(tag, "[PortalWebView] onPageFinished for $url. Credentials count: ${credentials.size}")
                        
                        if (credentials.isNotEmpty()) {
                            // Robust Matching Logic
                            // 1. Try to find a credential where the stored URL base matches the current Page URL
                            //    e.g. Stored: "ewaybillgst.gov.in", Current: "https://ewaybillgst.gov.in/Login.aspx"
                            
                            val cred = credentials.firstOrNull { 
                                val storedBase = it.url.trim().lowercase().removePrefix("https://").removePrefix("http://").split("/").firstOrNull()
                                val currentBase = url?.trim()?.lowercase()?.removePrefix("https://")?.removePrefix("http://")?.split("/")?.firstOrNull()
                                
                                (storedBase != null && currentBase != null && currentBase.contains(storedBase)) ||
                                (storedBase != null && currentBase != null && storedBase.contains(currentBase))
                            } ?: credentials.firstOrNull() // Fallback: If no URL match, try the first one (User likely only has one set of creds)
                            
                            if (cred != null) {
                                android.util.Log.d(tag, "[PortalWebView] Selected Credential: ${cred.name} (${cred.username}) for URL: $url")
                                
                                val autoFillJs = """
                                    (function() {
                                        console.log("[Auto-Fill] Script Started for username: ${cred.username}");
                                        
                                        function setNativeValue(element, value) {
                                            if (!element) return;
                                            var lastValue = element.value;
                                            element.value = value;
                                            var event = new Event('input', { bubbles: true });
                                            event.simulated = true;
                                            element._valueTracker?.setValue(lastValue);
                                            element.dispatchEvent(new Event('change', { bubbles: true }));
                                            element.dispatchEvent(event);
                                            element.dispatchEvent(new Event('blur', { bubbles: true }));
                                        }
                                        
                                        var userField = document.getElementById('txt_username') || document.getElementById('userName') || document.querySelector('input[type=text]');
                                        var passField = document.getElementById('txt_password') || document.getElementById('password') || document.querySelector('input[type=password]');
                                        
                                        console.log("[Auto-Fill] Found UserField: " + (userField ? "YES" : "NO"));
                                        console.log("[Auto-Fill] Found PassField: " + (passField ? "YES" : "NO"));
                                        
                                        if (userField && passField) {
                                            if (!userField.value) {
                                                console.log("[Auto-Fill] Filling username");
                                                setNativeValue(userField, '${cred.username}');
                                            } else {
                                                console.log("[Auto-Fill] Username already present: " + userField.value);
                                            }
                                            
                                            if (!passField.value) {
                                                console.log("[Auto-Fill] Filling password");
                                                setNativeValue(passField, '${cred.password}');
                                            }
                                            
                                            var captcha = document.getElementById('txtCaptcha') || document.getElementById('captcha');
                                            if (captcha) { 
                                                console.log("[Auto-Fill] Focusing Captcha");
                                                captcha.focus(); 
                                            }
                                        } else {
                                            console.log("[Auto-Fill] Fields not found!");
                                        }
                                    })();
                                """.trimIndent()
                                view?.evaluateJavascript(autoFillJs, null)
                            } else {
                                android.util.Log.w(tag, "[PortalWebView] No suitable credential found for URL: $url")
                            }
                        } else {
                             android.util.Log.w(tag, "[PortalWebView] Credentials list is EMPTY. Cannot auto-fill.")
                        }
                    }
                }
                
                onWebViewCreated(this)
                
                webChromeClient = object : WebChromeClient() {
                    override fun onShowFileChooser(
                        webView: WebView?,
                        filePathCallback: ValueCallback<Array<Uri>>?,
                        fileChooserParams: FileChooserParams?
                    ): Boolean {
                        return if (filePathCallback != null) {
                            onShowFileChooser(filePathCallback)
                        } else false
                    }
                    
                    override fun onConsoleMessage(consoleMessage: android.webkit.ConsoleMessage?): Boolean {
                        android.util.Log.d("InvoiceWorkflow", "[JS Console] ${consoleMessage?.message()} -- From line ${consoleMessage?.lineNumber()} of ${consoleMessage?.sourceId()}")
                        return true
                    }
                }
                
                // Inject Print Interface
                addJavascriptInterface(object {
                    @android.webkit.JavascriptInterface
                    fun printPage() {
                        // Must run on UI thread
                        post {
                            val printManager = context.getSystemService(Context.PRINT_SERVICE) as? android.print.PrintManager
                            val jobName = "EWayBill_Report_${System.currentTimeMillis()}"
                            val printAdapter = createPrintDocumentAdapter(jobName)
                            printManager?.print(jobName, printAdapter, android.print.PrintAttributes.Builder().build())
                        }
                    }

                    @android.webkit.JavascriptInterface
                    fun processHtml(htmlContent: String) {
                        post {
                            onHtmlScraped(htmlContent)
                        }
                    }
                }, "Android")

                setDownloadListener { url, userAgent, contentDisposition, mimetype, contentLength ->
                    android.util.Log.d("InvoiceWorkflow", "[WebView] Download Detected: $url, MimeType: $mimetype")

                    // 1. Ignore HTML pages falsely identified as downloads
                    if (mimetype.equals("text/html", ignoreCase = true) || mimetype.equals("application/xhtml+xml", ignoreCase = true)) {
                        android.util.Log.w("InvoiceWorkflow", "[WebView] Ignoring HTML download. This is likely a page load.")
                        return@setDownloadListener
                    }

                    // 2. Metadata Check: CommonReport.aspx requires POST.
                    if (url.contains("CommomReport.aspx", ignoreCase = true)) {
                         android.util.Log.w("InvoiceWorkflow", "[WebView] Blocked broken Excel download ($mimetype). Force-guiding to PDF.")
                         Toast.makeText(context, "Excel export not supported. Please click 'Print' -> 'Save as PDF'.", Toast.LENGTH_LONG).show()
                         return@setDownloadListener
                    }

                    onDownload(url, userAgent, contentDisposition, mimetype, contentLength)
                }
                
                loadUrl(url)
            }
        },
        update = { webView ->
            // Inject the window.print override script on every update/load
        },
        modifier = modifier
    )

    if (showErrorDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = { androidx.compose.material3.Text("Connection Error") },
            text = { androidx.compose.material3.Text("Unable to load the government portal. Please check your internet connection.") },
            confirmButton = {
                androidx.compose.material3.Button(
                    onClick = {
                        showErrorDialog = false
                        webViewRef?.reload()
                    }
                ) {
                    androidx.compose.material3.Text("Retry")
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        showErrorDialog = false
                        // User can now press System Back to exit the portal
                    }
                ) {
                    androidx.compose.material3.Text("Go Back")
                }
            }
        )
    }
}


