# ADB命令使用
在Android开发中，我们经常会用到`adb`命令，比如：
- 安装apk使用**adb install**
- 查看当前连接的设备使用推送文件使用**adb devices**
- 推送文件使用**adb push**
- ...

但是，查看当前设备的屏幕分辨率、屏幕密度应该使用什么命令，修改屏幕分辨率呢？如何通过adb进行截屏到电脑呢？

## 安装adb环境
Mac下通过brew安装
```
brew cask install android-platform-tools
```
这个步骤可省略，一般都是直接安装Android环境
安装成功后，用手机连上电脑。

## 查看设备信息
我们直接输入adb shell wm，可以看到帮助信息。
```
~ adb shell wm
usage: wm [subcommand] [options]
       wm size [reset|WxH|WdpxHdp]
       wm density [reset|DENSITY]
       wm overscan [reset|LEFT,TOP,RIGHT,BOTTOM]
       wm scaling [off|auto]
       wm screen-capture [userId] [true|false]
```

### 屏幕分辨率
```
// 获取屏幕分辨率
adb shell wm size

// 修改屏幕分辨率，只需要在size后面加上具体的宽和高
adb shell wm size 720x1080
```
实际操作如下：
```
 ~ adb shell wm size
Physical size: 1080x1920
```
### 屏幕密度
```
//  获取屏幕密度
adb shell wm density

// 修改屏幕密度，只需要在density后跟上具体的数值
adb shell wm density 420
```
实际操作：
```
~ adb shell wm density
Physical density: 480
```

## 截图
截图保存到电脑可以使用如下命令：
```
adb exec-out screencap -p > ~/Desktop/sc.png
```
如果以上命令不生效的话，可以分两步，一步先生成截图到手机，然后从手机导出
```
adb shell screencap -p /sdcard/sc.png

adb pull /sdcard/sc.png ~/Desktop/sc.png
```
实际操作：
```
adb shell screencap -p /sdcard/sc.png
adb pull /sdcard/sc.png ~/Desktop/sc.png
/sdcard/sc.png: 1 file pulled. 17.7 MB/s (297914 bytes in 0.016s)
```

## 更多命令
更多命令可以参考[awesome-adb](https://github.com/mzlogin/awesome-adb)

