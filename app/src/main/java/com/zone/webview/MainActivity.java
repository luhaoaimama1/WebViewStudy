package com.zone.webview;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private WebView mWebView;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mWebView = (WebView) findViewById(R.id.webView);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mProgressBar.setMax(100);

        setCallback();

//        downloadNative();

//        js();
    }

    private void setCallback() {
        //        WebViewClient 主要提供网页加载各个阶段的通知，比如网页开始加载onPageStarted，网页结束加载onPageFinished等；
        mWebView.setWebViewClient(mWebViewClient);
        //        WebChromeClient主要提供网页加载过程中提供的数据内容，比如返回网页的 进度条，title,favicon等。
        mWebView.setWebChromeClient(mWebChromeClient);
        mWebView.loadUrl("http://www.baidu.com/");
    }

    private WebChromeClient mWebChromeClient = new WebChromeClient() {

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            mProgressBar.setProgress(newProgress);
            System.out.println("进度:" + newProgress);
        }

        @Override
        public void onReceivedTitle(WebView view, String title) {
            System.out.println("标题:" + title);
        }
    };
    private WebViewClient mWebViewClient = new WebViewClient() {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                //todo  6.0应该抓到什么菜显示error呢 而且不是http的错误
//                    if(view.getUrl().equals(request.getUrl())&&error.getStatusCode()==404)
//                        wv_home.loadUrl("file:///android_asset/error.html");
            } else
                mWebView.loadUrl("file:///android_asset/error.html");
        }

        //http的错误 回调
        @Override
        public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
            super.onReceivedHttpError(view, request, errorResponse);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (view.getUrl().equals(request.getUrl()) && errorResponse.getStatusCode() == 404)
                    mWebView.loadUrl("file:///android_asset/error.html");
            } else
                mWebView.loadUrl("file:///android_asset/error.html");
        }

    };

    private void downloadNative() {
        //        2. 调用系统download的模块（代码简单）效果是:调到 默认浏览器 进行下载 并且通知栏有进度条
        Uri uri = Uri.parse("https://file.shenxian.com/mobile/static_file/2016/04/shenxian-release.1.0.apk");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    @SuppressLint({"SetJavaScriptEnabled", "JavascriptInterface"})
    private void js() {
        //总结就是  给Js注入一个 可调用的java对象 然后回调;
        mWebView.requestFocus();
        mWebView.setOnKeyListener(new View.OnKeyListener() {        // webview can go back
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK && mWebView.canGoBack()) {
                    mWebView.goBack();
                    return true;
                }
                return false;
            }
        });
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDefaultTextEncodingName("utf-8");
        mWebView.addJavascriptInterface(insertObj, "jsObj");//js端:window.jsObj 获取到当前的java端的insertObj
        mWebView.loadUrl("file:///android_asset/js.html");
    }

    private Object insertObj = new Object() {
        @JavascriptInterface
        public String HtmlcallJava() {
            System.out.println("Html call Java");
            return "Html call Java";
        }

        @JavascriptInterface
        public String HtmlcallJava2(final String param) {
            System.out.println("Html call Java param:" + param);
            return "Html call Java : " + param;
        }

        @JavascriptInterface
        public void JavacallHtml() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mWebView.onPause();
                    mWebView.loadUrl("javascript: showFromHtml()");
                    Toast.makeText(MainActivity.this, "clickBtn", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @JavascriptInterface
        public void JavacallHtml2() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mWebView.loadUrl("javascript: showFromHtml2('IT-homer blog')");
                    Toast.makeText(MainActivity.this, "clickBtn2", Toast.LENGTH_SHORT).show();
                }
            });
        }
    };
}
