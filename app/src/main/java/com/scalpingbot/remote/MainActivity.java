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
import android.graphics.Color;

public class MainActivity extends Activity {
    private WebView webView;
    private ProgressBar progressBar;
    private static final String HOME_URL = "https://ssakaemmanuel.github.io/ScalpingBot/";

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

        root.addView(webView, new FrameLayout.LayoutParams(-1, -1));
        root.addView(progressBar, new FrameLayout.LayoutParams(-1, dp(3)));
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
