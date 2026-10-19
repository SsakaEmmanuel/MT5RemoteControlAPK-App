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
import android.app.AlertDialog;
import android.net.Uri;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends Activity {
    private WebView webView;
    private ProgressBar progressBar;
    private static final String HOME_URL = "https://ssakaemmanuel.github.io/ScalpingBot/";
    private static final String SITE_BASE = "https://ssakaemmanuel.github.io/ScalpingBot/";
    private static final String CURRENT_VERSION = "1.2";
    private static final String RELEASES_API = "https://api.github.com/repos/SsakaEmmanuel/MT5RemoteControlAPK-App/releases/latest";

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
        progressBar.setProgressTintList(android.content.res.ColorStateList.valueOf(Color.rgb(255, 210, 31)));

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
                        + "<p><a style='color:#ffd21f' href='" + HOME_URL + "'>Retry</a></p></body></html>",
                        "text/html", "UTF-8", null);
                }
            }
        });
        if (savedInstanceState != null) webView.restoreState(savedInstanceState);
        else webView.loadUrl(HOME_URL);

        // Check published GitHub Releases in the background; never block app startup.
        checkForAppUpdate();
    }

    private void checkForAppUpdate() {
        new Thread(() -> {
            HttpURLConnection connection = null;
            try {
                URL url = new URL(RELEASES_API);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(6000);
                connection.setReadTimeout(6000);
                connection.setRequestProperty("Accept", "application/vnd.github+json");
                connection.setRequestProperty("User-Agent", "ScalpingBot-Android");
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) return;

                StringBuilder response = new StringBuilder();
                try (InputStream input = connection.getInputStream();
                     BufferedReader reader = new BufferedReader(new InputStreamReader(input, "UTF-8"))) {
                    String line;
                    while ((line = reader.readLine()) != null) response.append(line);
                }

                JSONObject release = new JSONObject(response.toString());
                if (release.optBoolean("draft", false) || release.optBoolean("prerelease", false)) return;
                String tag = release.optString("tag_name", "").replaceFirst("^[vV]", "").trim();
                if (tag.isEmpty() || !isNewerVersion(tag, CURRENT_VERSION)) return;

                JSONArray assets = release.optJSONArray("assets");
                if (assets == null) return;
                String apkUrl = null;
                for (int i = 0; i < assets.length(); i++) {
                    JSONObject asset = assets.optJSONObject(i);
                    if (asset == null) continue;
                    String name = asset.optString("name", "");
                    String download = asset.optString("browser_download_url", "");
                    if (name.toLowerCase(java.util.Locale.ROOT).endsWith(".apk")
                            && download.startsWith("https://")) {
                        apkUrl = download;
                        break;
                    }
                }
                if (apkUrl == null) return;

                final String latestVersion = tag;
                final String downloadUrl = apkUrl;
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) return;
                    new AlertDialog.Builder(MainActivity.this)
                        .setTitle("ScalpingBot update available")
                        .setMessage("Version " + latestVersion + " is available. Update now to get the latest improvements?")
                        .setCancelable(true)
                        .setNegativeButton("Later", (dialog, which) -> dialog.dismiss())
                        .setPositiveButton("Update now", (dialog, which) -> {
                            try {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl)));
                            } catch (Exception ignored) {
                                new AlertDialog.Builder(MainActivity.this)
                                    .setMessage("Could not open the download. Please try again later.")
                                    .setPositiveButton("OK", null)
                                    .show();
                            }
                        })
                        .show();
                });
            } catch (Exception ignored) {
                // Update checks are optional; offline users can keep using the app.
            } finally {
                if (connection != null) connection.disconnect();
            }
        }).start();
    }

    private boolean isNewerVersion(String latest, String current) {
        try {
            String[] latestParts = latest.split("\\.");
            String[] currentParts = current.split("\\.");
            int count = Math.max(latestParts.length, currentParts.length);
            for (int i = 0; i < count; i++) {
                int a = i < latestParts.length ? Integer.parseInt(latestParts[i].replaceAll("[^0-9].*$", "")) : 0;
                int b = i < currentParts.length ? Integer.parseInt(currentParts[i].replaceAll("[^0-9].*$", "")) : 0;
                if (a > b) return true;
                if (a < b) return false;
            }
        } catch (Exception ignored) { }
        return false;
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
