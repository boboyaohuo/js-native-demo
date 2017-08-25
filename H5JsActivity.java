package com.quzhuan.redpack.activity;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.github.lzyzsd.jsbridge.BridgeHandler;
import com.github.lzyzsd.jsbridge.BridgeWebView;
import com.github.lzyzsd.jsbridge.CallBackFunction;
import com.github.lzyzsd.jsbridge.DefaultHandler;
import com.quzhuan.redpack.R;
import com.quzhuan.redpack.base.BaseActivity;
import com.quzhuan.redpack.global.Constant;
import com.quzhuan.redpack.http.Apis;
import com.sharesdk.ShareDialog;

import org.json.JSONException;
import org.json.JSONObject;

public class H5JsActivity extends BaseActivity {

	private BridgeWebView webview;
	private String url;
	private String urlSuffix;
	private String title;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setDbContentView(R.layout.me_layout_h5_js);

		getBackBtn().setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (webview != null && webview.canGoBack()){
					webview.goBack();
				}else {
					H5JsActivity.this.finish();
				}
			}
		});
		getCloseBtn().setVisibility(View.VISIBLE);
		getCloseBtn().setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				H5JsActivity.this.finish();
			}
		});

		showLoadingDialog();

		final Intent intent = getIntent();
		if (intent != null) {
			title = intent.getStringExtra(Constant.INTENT_TITLE);
			url = intent.getStringExtra(Constant.INTENT_URL);
			urlSuffix = intent.getStringExtra(Constant.INTENT_URL_SUFFIX);
			if (TextUtils.isEmpty(url)){
				url = Apis.H5_BASE;
			}
			if (!TextUtils.isEmpty(title)) {
				setTitleText(title);
			}
			if (!TextUtils.isEmpty(url)) {
				String[] keys = intent.getStringArrayExtra(Constant.INTENT_PARAMS_KEY);
				String[] values = intent.getStringArrayExtra(Constant.INTENT_PARAMS_VALUE);
				StringBuilder sb = new StringBuilder();
				if (keys !=null && values != null){
					for (int i = 0; i < keys.length; i++) {
						if (i == 0){
							sb.append('?').append(keys[i]).append('=').append(values[i]);
						}else {
							sb.append('&').append(keys[i]).append('=').append(values[i]);
						}
					}
				}
				url += sb.toString();
				if (!TextUtils.isEmpty(urlSuffix)){
					url += urlSuffix;
				}

				webview = (BridgeWebView) findViewById(R.id.bridge_webview);
		        //设置WebView属性，能够执行Javascript脚本  
		        webview.getSettings().setJavaScriptEnabled(true);
				webview.getSettings().setDomStorageEnabled(true);
		        //加载需要显示的网页  
//		        webview.loadUrl(url);
		        //设置Web视图  
//				webview.setWebViewClient(new MyWebViewClient());

				/***********************jsBridge**************************/
				webview.setDefaultHandler(new DefaultHandler());
				webview.setWebChromeClient(new WebChromeClient() {

					@Override
					public void onProgressChanged(WebView view, int newProgress) {
						if(newProgress == 100)
							dismissDialog();
					}

					@Override
					public void onReceivedTitle(WebView view, String title) {
						super.onReceivedTitle(view, title);
						H5JsActivity.this.setTitleText(title);
					}

					@SuppressWarnings("unused")
					public void openFileChooser(ValueCallback<Uri> uploadMsg, String AcceptType, String capture) {
						this.openFileChooser(uploadMsg);
					}

					@SuppressWarnings("unused")
					public void openFileChooser(ValueCallback<Uri> uploadMsg, String AcceptType) {
						this.openFileChooser(uploadMsg);
					}

					public void openFileChooser(ValueCallback<Uri> uploadMsg) {
					}
				});
				webview.loadUrl(url);
				webview.registerHandler("submitFromWeb", new BridgeHandler() {

					@Override
					public void handler(String data, CallBackFunction function) {
						if (Constant.DEBUG){
							Log.i("dbg", "----handler = submitFromWeb, data from web = " + data);
						}
						function.onCallBack("submitFromWeb exe, response data from Java");

						try {
							JSONObject jsonObject = new JSONObject(data);
							int code = jsonObject.getInt("code");

							if (code == 1000){
								ShareDialog shareDialog = new ShareDialog(H5JsActivity.this, 1);
								shareDialog.showShareDialog();
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				});
				/***********************jsBridge**************************/

				if (Constant.DEBUG){
					Log.i("dbg", "----H5----" + url);
				}
			}
		}
	}


	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK) && webview != null && webview.canGoBack()) {
			webview.goBack(); // goBack()表示返回WebView的上一页面
			return true;
		}
		return super.onKeyDown(keyCode,event);
	}
}
