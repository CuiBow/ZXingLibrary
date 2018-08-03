# ZXingLibrary
二维码 条形码扫描工具
### 效果

### 特点 
可以完全自定义扫描框
### 版本功能

版本 | 更新内容
--------- | -------------
v1.0 | 添加ZXing扫码
v1.1 | 添加条形/二维码扫码框、添加相册扫描二维码、添加页面传递回调


### 使用方式
    
#####1.0 在你的项目的 build.gradle 添加以下代码 Add it in your root build.gradle at the end of repositories:
```
allprojects {

  repositories {
  	...
	  	maven { url 'https://jitpack.io' }
    }
} 
```

 
#####2.0 在你的 Library 中添加如下代码

``` 
dependencies { 

	  compile 'com.github.CuiBow:ZXingLibrary:v1.1'
     
} 
```
#####3.0 在Application添加如下代码
``` 
 ZXingLibrary.init(this);
``` 
#####4.0 普通模式使用默认扫描框 
* 扫描框分为两种
* BarCodeView//条形码默认扫描框
* QRCodeView//二维码默认扫描框

``` 
ScanFragment scanFragment =new ScanFragment();

getSupportFragmentManager().beginTransaction().replace(R.id.scan_fragment, scanFragment).commit();   
```
   
#####5.0 使用自定义扫描框
```     
//自定义扫描框需要继承BaseScanBox
ScanFragment scanFragment =new ScanFragment();
//进行xml替换
ScanUtil.setFragmentArgs(scanFragment, R.layout.fragment_my_scan);
getSupportFragmentManager().beginTransaction().replace(R.id.scan_fragment, scanFragment).commit();   
```
#####6.0 关于剩余功能以及用法
* 跳转相册扫描二维码

```          
ScanFragment.startPhotoAlbum();
```
* 重新调起摄像头

``` 
ScanFragment.scanAgain();
```
* 设置闪光灯

``` 
ScanFragment.setTorch(true/false);//开启/关闭
```
#####7.0 关于回调介绍
* 关于扫描以及相册回调
* 1.请实现ScanUtil.ScanCallback( )接口
* 2.考虑到页面间传递code问题，请在成功的回调中调用 ScanResultRxFinal.get().onScanResult(mBitmap,result);
* 3.在想要接收到code的地方实现ScanResultRxFinal.RxScanResultListener( )以便接收到扫描成功回来的值
* 以下为代码

```
 ScanFragment.setScanCallback(new ScanUtil.ScanCallback() {
       @Override
       public void onScanSuccess(Bitmap mBitmap, String result) {
       //成功回调返回一张二值图
       ScanResultRxFinal.get().onScanResult(mBitmap,result);
       }
       @Override
       public void onScanFailed() {
        //扫描失败
        }
    });
```
```
  ScanResultRxFinal.get().init(this);
  @Override
  public void onScanSuccessResult(Bitmap bitmap, String result) {
  }
```



            
    





