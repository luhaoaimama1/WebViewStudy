package com.zone.webview;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.Nullable;
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

import java.io.File;
import java.io.IOException;

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

//        setCallback();

//        downloadNative();

        js();
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

        @Nullable
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
//            return realInterceptRequest(view, url);
            return super.shouldInterceptRequest(view,url);
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Nullable
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            String url = request.getUrl().toString();
//            return realInterceptRequest(view, url);
            return super.shouldInterceptRequest(view,request);
        }

        private WebResourceResponse realInterceptRequest(WebView view, String url) {
            WebResourceResponse response = null;
            /** request.getUrl(): 获取拦截的url地址 */
            /** 使用String中的contanins方法判断当前地址是否包含/app/ */
            /** 当包含该请求时，截取文件名 */
            if(url.startsWith("file")){  //拦截了本地文件加载了
                try {
                    /**
                     使用getAssets().open()打开本地资源，其中*代表所有，其它：
                     js: mimeType = "application/x-javascript";
                     css: mimeType = "text/css";
                     html: mimeType = "text/html";
                     jpg/png:  mimeType = "image/png";
                     */
                    response = new WebResourceResponse("*", "UTF-8", getAssets().open("abcd.jpg"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return response;
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
        //允许 webview 图片加载本地文件
        webSettings.setAllowUniversalAccessFromFileURLs(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowFileAccessFromFileURLs(true);

        mWebView.addJavascriptInterface(insertObj, "jsObj");//js端:window.jsObj 获取到当前的java端的insertObj
        mWebView.loadUrl("file:///android_asset/js.html");
        mWebView.setWebViewClient(mWebViewClient);
        //        WebChromeClient主要提供网页加载过程中提供的数据内容，比如返回网页的 进度条，title,favicon等。
        mWebView.setWebChromeClient(mWebChromeClient);

    }

    private Object insertObj = new Object() {
        @JavascriptInterface
        public String HtmlcallJava() {
            System.out.println("Html call Java");
            return "Html call Java";
        }

        @JavascriptInterface
        public String getVersion() {
            System.out.println("Html call Java");
            return "233";
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

                    String s = "file://" + new File(Environment.getExternalStorageDirectory(),
                            "DCIM/weibo/img-00f31057981f10b0ca987eb6f466c35b.jpg").getPath();
                    // 确保这个图片存在  并且手机存储权限还在！
                    //todo zone  每个用力一个demo  拦截替换
                    mWebView.loadUrl("javascript: showHtmlcallJava2('" + s + "')");
//                    mWebView.loadUrl("javascript: showHtmlcallJava2('"+"https://github.com/"+"')");
                    Toast.makeText(MainActivity.this, "clickBtn2", Toast.LENGTH_SHORT).show();
                }
            });
        }
    };
}
