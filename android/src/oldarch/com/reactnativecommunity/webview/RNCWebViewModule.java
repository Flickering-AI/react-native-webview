package com.reactnativecommunity.webview;

import android.app.DownloadManager;
import android.net.Uri;

import androidx.annotation.NonNull;

import android.util.Log;
import com.tencent.smtt.sdk.ValueCallback;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.module.annotations.ReactModule;
import com.tencent.smtt.sdk.QbSdk;
import com.tencent.smtt.sdk.TbsListener;

@ReactModule(name = RNCWebViewModuleImpl.NAME)
public class RNCWebViewModule extends ReactContextBaseJavaModule {
    private static final String TAG = "RNCWebViewModule";
    final private RNCWebViewModuleImpl mRNCWebViewModuleImpl;

    public RNCWebViewModule(ReactApplicationContext reactContext) {
        super(reactContext);
        mRNCWebViewModuleImpl = new RNCWebViewModuleImpl(reactContext);
    }

    @ReactMethod
    public void initTBSX5(final ReadableMap config, final Promise promise) {
      /* 设置允许移动网络下进行内核下载。默认不下载，会导致部分一直用移动网络的用户无法使用x5内核 */
      QbSdk.setDownloadWithoutWifi(config.getBoolean("downloadWithoutWifi"));

      QbSdk.setCoreMinVersion(config.getInt("coreMinVersion"));
      /* SDK内核初始化周期回调，包括 下载、安装、加载 */

      QbSdk.setTbsListener(new TbsListener() {

        /**
         * @param stateCode 用户可处理错误码请参考{@link com.tencent.smtt.sdk.TbsCommonCode}
         */
        @Override
        public void onDownloadFinish(int stateCode) {
          Log.i(TAG, "onDownloadFinished: " + stateCode);
        }

        /**
         * @param stateCode 用户可处理错误码请参考{@link com.tencent.smtt.sdk.TbsCommonCode}
         */
        @Override
        public void onInstallFinish(int stateCode) {
          Log.i(TAG, "onInstallFinished: " + stateCode);
        }

        /**
         * 首次安装应用，会触发内核下载，此时会有内核下载的进度回调。
         * @param progress 0 - 100
         */
        @Override
        public void onDownloadProgress(int progress) {
          Log.i(TAG, "Core Downloading: " + progress);
        }
      });

      /* 此过程包括X5内核的下载、预初始化，接入方不需要接管处理x5的初始化流程，希望无感接入 */
      QbSdk.initX5Environment(getCurrentActivity(), new QbSdk.PreInitCallback() {
        @Override
        public void onCoreInitFinished() {
          // 内核初始化完成，可能为系统内核，也可能为系统内核
        }

        /**
         * 预初始化结束
         * 由于X5内核体积较大，需要依赖wifi网络下发，所以当内核不存在的时候，默认会回调false，此时将会使用系统内核代替
         * 内核下发请求发起有24小时间隔，卸载重装、调整系统时间24小时后都可重置
         * 调试阶段建议通过 WebView 访问 debugtbs.qq.com -> 安装线上内核 解决
         * @param isX5 是否使用X5内核
         */
        @Override
        public void onViewInitFinished(boolean isX5) {
          Log.i(TAG, "onViewInitFinished: " + isX5);
          // hint: you can use QbSdk.getX5CoreLoadHelp(context) anytime to get help.
        }
      });
    }

    @ReactMethod
    public void isFileUploadSupported(final Promise promise) {
        promise.resolve(mRNCWebViewModuleImpl.isFileUploadSupported());
    }

    @ReactMethod
    public void shouldStartLoadWithLockIdentifier(boolean shouldStart, double lockIdentifier) {
        mRNCWebViewModuleImpl.shouldStartLoadWithLockIdentifier(shouldStart, lockIdentifier);
    }

    public void startPhotoPickerIntent(ValueCallback<Uri> filePathCallback, String acceptType) {
        mRNCWebViewModuleImpl.startPhotoPickerIntent(acceptType, filePathCallback);
    }

    public boolean startPhotoPickerIntent(final ValueCallback<Uri[]> callback, final String[] acceptTypes, final boolean allowMultiple, final boolean isCaptureEnabled) {
        return mRNCWebViewModuleImpl.startPhotoPickerIntent(acceptTypes, allowMultiple, callback, isCaptureEnabled);
    }

    public void setDownloadRequest(DownloadManager.Request request) {
        mRNCWebViewModuleImpl.setDownloadRequest(request);
    }

    public void downloadFile(String downloadingMessage) {
        mRNCWebViewModuleImpl.downloadFile(downloadingMessage);
    }

    public boolean grantFileDownloaderPermissions(String downloadingMessage, String lackPermissionToDownloadMessage) {
        return mRNCWebViewModuleImpl.grantFileDownloaderPermissions(downloadingMessage, lackPermissionToDownloadMessage);
    }

    @NonNull
    @Override
    public String getName() {
        return RNCWebViewModuleImpl.NAME;
    }
}