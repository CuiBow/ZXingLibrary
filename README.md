# ZXingLibrary
二维码 条形码扫描工具
### 效果

### 特点 

可以完全自定义扫描框

### 使用方式
    
1.在你的项目的 build.gradle 添加以下代码 Add it in your root build.gradle at the end of repositories:
```
allprojects {

  repositories {
  	...
	  	maven { url 'https://jitpack.io' }
    }
} 
```

 
2.在你的 Library 中添加如下代码

``` 
dependencies { 

	  compile 'com.github.CuiBow:ZXingLibrary:v1.0'
     
} 
```
3.在Application添加如下代码
``` 
 ZXingLibrary.init(this);
``` 
4.普通模式使用默认扫描框
``` 
        ScanFragment scanFragment =new ScanFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.scan_fragment, scanFragment).commit();
        scanFragment.setScanCallback(new ScanUtil.ScanCallback() {
            @Override
            public void onScanSuccess(Bitmap mBitmap, String result) {
              //扫描成功返回一张灰度图以及返回值
            }
            @Override
            public void onScanFailed() {
              //扫描失败
            }
        });
        
        //开启或关闭闪光灯
        scanFragment.setTorch(true/false);
   ```
   
5.使用自定义扫描框
```     
        //自定义扫描框需要继承BaseScanBox
        ScanFragment scanFragment =new ScanFragment();
        //进行xml替换
        ScanUtil.setFragmentArgs(scanFragment, R.layout.fragment_my_scan);
        getSupportFragmentManager().beginTransaction().replace(R.id.scan_fragment, scanFragment).commit();
        scanFragment.setScanCallback(new ScanUtil.ScanCallback() {
            @Override
            public void onScanSuccess(Bitmap mBitmap, String result) {
              //扫描成功返回一张灰度图以及返回值
            }
            @Override
            public void onScanFailed() {
              //扫描失败
            }
        });
        
        //开启或关闭闪光灯
        scanFragment.setTorch(true/false);
```
            
    





