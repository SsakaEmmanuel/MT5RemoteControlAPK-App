package com.scalpingbot.remote;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceError;
import android.net.Uri;
import android.content.Intent;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.Gravity;
import android.graphics.Color;

public class MainActivity extends Activity {
    private WebView webView;
    private ProgressBar progressBar;
    private static final String HOME_URL = "https://ssakaemmanuel.github.io/ScalpingBot/";
    private static final String SITE_BASE = "https://ssakaemmanuel.github.io/ScalpingBot/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setStatusBarColor(Color.rgb(12, 16, 27));
        getWindow().setNavigationBarColor(Color.rgb(12, 16, 27));

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.rgb(12, 16, 27));
        webView = new WebView(this);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setSupportZoom(false);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(false);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);

        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setMax(100);
        progressBar.setProgressTintList(android.content.res.ColorStateList.valueOf(Color.rgb(77, 205, 160)));

        // Reserve space for native navigation so it remains visible even when
        // the website's responsive navigation is hidden inside the WebView.
        webView.setPadding(0, 0, 0, dp(58));
        webView.setClipToPadding(false);
        root.addView(webView, new FrameLayout.LayoutParams(-1, -1));

        HorizontalScrollView navScroll = new HorizontalScrollView(this);
        navScroll.setHorizontalScrollBarEnabled(false);
        navScroll.setFillViewport(false);
        navScroll.setBackgroundColor(Color.rgb(18, 25, 42));
        LinearLayout navBar = new LinearLayout(this);
        navBar.setOrientation(LinearLayout.HORIZONTAL);
        navBar.setGravity(Gravity.CENTER_VERTICAL);
        String[] navLabels = {"Home", "Overview", "Market", "Demo", "History", "Account", "Settings"};
        String[] navPaths = {"", "dashboard.html", "market.html", "demo.html", "dashboard.html", "dashboard.html", "dashboard.html"};
        for (int i = 0; i < navLabels.length; i++) {
            final int index = i;
            TextView button = new TextView(this);
            button.setText(navLabels[i]);
            button.setTextColor(Color.rgb(235, 241, 250));
            button.setTextSize(12);
            button.setGravity(Gravity.CENTER);
            button.setPadding(dp(12), dp(15), dp(12), dp(15));
            button.setBackgroundColor(Color.TRANSPARENT);
            button.setOnClickListener(v -> {
                String path = navPaths[index];
                if (index == 4 || index == 5 || index == 6) {
                    String section = index == 4 ? "Trade history" : index == 5 ? "Account status" : "Settings";
                    webView.evaluateJavascript(
                        "(function(){var names=['Overview','Market Analysis','Demo Trading','Trade history','Account status','Settings'];" +
                        "var items=[...document.querySelectorAll('#privateDashboard aside b')];" +
                        "var i=names.indexOf(" + org.json.JSONObject.quote(section) + ");" +
                        "if(items[i]) items[i].click(); else window.location.href=" + org.json.JSONObject.quote(SITE_BASE + "dashboard.html") +
                        ";})()", null);
                } else {
                    webView.loadUrl(SITE_BASE + path);
                }
            });
            navBar.addView(button, new LinearLayout.LayoutParams(-2, -1));
        }
        navScroll.addView(navBar, new HorizontalScrollView.LayoutParams(-2, -1));
        FrameLayout.LayoutParams navParams = new FrameLayout.LayoutParams(-1, dp(56), Gravity.BOTTOM);
        root.addView(navScroll, navParams);
        root.addView(progressBar, new FrameLayout.LayoutParams(-1, dp(3), Gravity.TOP));
        setContentView(root);

        webView.setWebChromeClient(new WebChromeClient() {
            @Override public void onProgressChanged(WebView view, int progress) {
                progressBar.setProgress(progress);
                progressBar.setVisibility(progress >= 100 ? View.GONE : View.VISIBLE);
            }
        });
        webView.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                String host = uri.getHost();
                if ("ssakaemmanuel.github.io".equalsIgnoreCase(host)
                        || (host != null && host.endsWith(".supabase.co"))) return false;
                if ("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme())) {
                    startActivity(new Intent(Intent.ACTION_VIEW, uri));
                    return true;
                }
                return true;
            }
            @Override public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                if (request.isForMainFrame()) {
                    view.loadDataWithBaseURL(null,
                        "<html><meta name='viewport' content='width=device-width, initial-scale=1'>"
                        + "<body style='background:#0c101b;color:#f5f7fb;font-family:sans-serif;padding:24px'>"
                        + "<h2>Connection problem</h2><p>ScalpingBot could not load. Check internet and try again.</p>"
                        + "<p><a style='color:#4dcda0' href='" + HOME_URL + "'>Retry</a></p></body></html>",
                        "text/html", "UTF-8", null);
                }
            }
        });
        if (savedInstanceState != null) webView.restoreState(savedInstanceState);
        else webView.loadUrl(HOME_URL);
    }
    private int dp(int value) { return (int)(value * getResources().getDisplayMetrics().density + 0.5f); }
    @Override public void onBackPressed() {
        if (webView != null && webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }
    @Override protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (webView != null) webView.saveState(outState);
    }
    @Override protected void onDestroy() {
        if (webView != null) { webView.destroy(); webView = null; }
        super.onDestroy();
    }
}
