# Android内存分析命令

本篇文章是对[Gityuan](http://gityuan.com/)的文章[Android内存分析命令](http://gityuan.com/2016/01/02/memory-analysis-command/)一次实践。

说明：可以先执行adb shell进入到手机系统，然后再执行内存分析命令，也可以每次在命令前带上adb shell。

## 概述
### 内存指标
简写|全称|含义|等价
--|--|--|--
USS|Unique Set Size|物理内存|进程独占的内存
PSS|Proportional Set Size|物理内存|PSS=USS+按比例包含共享库
RSS|Resident Set Size|物理内存|RSS=USS+包含共享库
VSS|Virtual Set Size|虚拟内存|VSS=RSS+未分配实际物理内存

故内存的大小关系：VSS>=RSS>=PSS>=USS

### 内存分析命令
常用的分析命令：
- dumpsys meminfo
- procrank
- cat /proc/meminfo
- free
- showmap
- vmstat

## 命令说明
### dumpsys meminfo
划分类型|排序|说明
--|--|--
process|PSS|以进程的PSS从大到小依次排序显示，每行显示一个进程
OOM adjustment|PSS|Native/System/Persistent/Foreground/Visible/Perceptible/A Services/Home/B Services/Cached，分别显示每类的进程情况
category|PSS|以Dalvik/Native/.art mmap/.dex mmap等划分的各类进程的总PSS情况
total|<space></space>|总内存、剩余内存、可用内存、其他内存

```
➜  ~ adb shell dumpsys meminfo
Applications Memory Usage (in Kilobytes):
Uptime: 364716661 Realtime: 625834951

Total PSS by process:
    372,236K: com.tencent.mm (pid 30632 / activities)
    249,831K: com.baidu.netdisk (pid 9618 / activities)
    209,914K: system (pid 1390)
    197,255K: com.eg.android.AlipayGphone (pid 6214 / activities)
    177,944K: com.android.systemui (pid 2186 / activities)
    156,704K: com.jingdong.app.mall (pid 32419 / activities)
    129,794K: com.miui.home (pid 2476 / activities)
    112,407K: com.tencent.mm:appbrand0 (pid 7859)

    ...

Total PSS by OOM adjustment:
    439,157K: Native
         94,512K: surfaceflinger (pid 781)
         33,394K: logd (pid 569)
         26,830K: android.hardware.camera.provider@2.4-service (pid 738)
         18,008K: com.htffund.mobile.ec.ui:pushcore (pid 3005)
         17,867K: com.htffund.mobile.ec.ui (pid 2740)
         15,328K: android.hardware.graphics.composer@2.1-service (pid 744)
         15,141K: media.extractor (pid 943)
         12,773K: media.codec (pid 965)

         ...

    536,926K: Persistent
        209,914K: system (pid 1390)
        177,944K: com.android.systemui (pid 2186 / activities)
         33,620K: com.android.phone (pid 2440)
         25,905K: com.xiaomi.xmsf (pid 3485)
         22,042K: com.miui.daemon (pid 3458)
         21,241K: com.android.nfc (pid 3471)
         13,575K: com.xiaomi.finddevice (pid 3499)

        ...
    
    13,804K: Persistent Service
         13,804K: com.android.bluetooth (pid 6959)
     80,932K: Foreground
         30,055K: com.endselect.helloapp (pid 15638 / activities)
         27,266K: com.miui.securitycenter.remote (pid 3556)
         23,611K: com.miui.powerkeeper (pid 4485)
    373,634K: Visible
         94,016K: com.google.android.gms (pid 3037)
         85,948K: com.google.android.gms.persistent (pid 2828)
         56,045K: com.google.android.inputmethod.pinyin (pid 3595)
         55,418K: com.google.android.youtube (pid 15026)
         15,514K: com.google.android.gms.unstable (pid 29188)
         13,439K: com.google.android.googlequicksearchbox:interactor (pid 3411)
         12,412K: com.android.chrome:sandboxed_process0 (pid 8452)
         11,223K: com.miui.analytics (pid 28310)
         10,762K: com.miui.msa.global (pid 29047)
         10,388K: com.miui.hybrid:entrance (pid 28266)
          4,804K: com.miui.wmsvc (pid 3241)
          3,665K: com.android.smspush (pid 29442)
     24,421K: Perceptible
         12,790K: com.lbe.security.miui (pid 4093)
         11,631K: com.xiaomi.location.fused (pid 4666)
    404,757K: Backup
        372,236K: com.tencent.mm (pid 30632 / activities)
         32,521K: com.github.dawndiy.bifrostv (pid 3766)
    252,617K: A Services
         81,974K: com.alpha.lagouapk (pid 3599)
         61,922K: com.alpha.lagouapk:pushcore (pid 3806)
         52,865K: com.tencent.mm:push (pid 30732)
         41,838K: com.eg.android.AlipayGphone:push (pid 6566)
         14,018K: com.tencent.android.qqdownloader:tools (pid 6731)
    139,276K: Home
        129,794K: com.miui.home (pid 2476 / activities)
          9,482K: com.mi.android.globalpersonalassistant (pid 8225)
      8,796K: Previous
          8,796K: com.xiaomi.joyose (pid 5971)
    828,166K: B Services
        249,831K: com.baidu.netdisk (pid 9618 / activities)
        197,255K: com.eg.android.AlipayGphone (pid 6214 / activities)
         82,231K: com.tencent.android.qqdownloader (pid 6650)
         47,673K: com.tencent.mm:sandbox (pid 4585)
         46,413K: com.baidu.BaiduMap:MapCoreService (pid 6087)
         45,758K: com.tencent.android.qqdownloader:daemon (pid 6590)
         44,453K: com.baidu.BaiduMap:SandBoxProcess (pid 5927)
         18,993K: com.baidu.BaiduMap:QS (pid 29923)
         18,976K: com.baidu.netdisk:aiapps0 (pid 24206)
         16,289K: android.process.media (pid 23454)
         13,012K: com.mi.android.globalFileexplorer (pid 24256)
         11,965K: org.simalliance.openmobileapi.service:remote (pid 10620)
          9,142K: com.baidu.netdisk:bdservice_v1 (pid 9760)
          8,154K: com.miui.analytics (pid 30077)
          6,810K: com.facebook.services (pid 30448)
          6,790K: com.xiaomi.bluetooth (pid 15733)
          4,421K: com.android.chrome:sandboxed_process0 (pid 6300)
  1,041,888K: Cached
        156,704K: com.jingdong.app.mall (pid 32419 / activities)
        112,407K: com.tencent.mm:appbrand0 (pid 7859)
         69,099K: com.google.android.googlequicksearchbox:search (pid 5187)
         64,351K: com.tencent.mm:appbrand1 (pid 27666)
         62,037K: com.tencent.mm:appbrand2 (pid 9835)
         54,529K: com.airbnb.android (pid 14317)
         50,063K: com.baidu.BaiduMap (pid 3594)

         ...

Total PSS by category:
  1,000,573K: Native
    386,459K: Dalvik
    364,434K: Unknown
    283,289K: .art mmap
    165,764K: GL mtrack
    158,641K: Dalvik Other
    140,726K: .dex mmap
    139,024K: Gfx dev
    111,625K: .so mmap
     80,748K: EGL mtrack
     68,326K: .oat mmap
     29,834K: .apk mmap
     22,109K: Ashmem
      8,564K: Other mmap
      7,588K: .jar mmap
      3,727K: Stack
      1,883K: Other dev
        647K: .ttf mmap
        100K: Cursor
          0K: Other mtrack

Total RAM: 5,862,088K (status normal)
 Free RAM: 3,141,292K (1,041,888K cached pss + 1,975,308K cached kernel +    34,616K cached ion +    89,480K free)
 Used RAM: 3,931,894K (3,102,486K used pss +   829,408K kernel)
 Lost RAM:  -177,955K
     ZRAM:   439,820K physical used for 1,234,708K in swap (2,621,436K total swap)
   Tuning: 256 (large 512), oom   322,560K, restore limit   107,520K (high-end-gfx)
```

另外，可只输出某个pid或者package的进程信息
```
// 输出指定pid的进程
adb shell dumpsys meminfo <pid>
或者
// 输出指定包名的进程
adb shell dunpsys meminfo --package <packagename>
```


### procrank
procrank命令比dumpsys meminfo命令输出更详细的VSS/RSS/PSS/USS内存指标。

这个命令需要root权限才可以执行。

```
// 需要root才可以执行procrank
$ procrank
Error creating kernel interface -- does this kernel have pagemap?

```

### cat /proc/meminfo

查看更详细的内存信息

```
➜  ~ adb shell cat /proc/meminfo
MemTotal:        5862088 kB     // RAM总大小
MemFree:           67696 kB     // RAM未使用的大小
MemAvailable:    1912908 kB     // RAM可用的大小
Buffers:          551304 kB     // 用于文件缓冲
Cached:          1008420 kB     // 用于高速缓存
SwapCached:         8472 kB     // 用于swap缓存
Active:          1990724 kB     // 活跃使用状态
Inactive:        1502072 kB     // 非活跃使用状态
Active(anon):    1294588 kB     // Active = Active(anon) + Active(file)
Inactive(anon):   745900 kB     // Inactive = Inactive(anon) + Inactive(file)
Active(file):     696136 kB     
Inactive(file):   756172 kB
Unevictable:       83576 kB
Mlocked:           83576 kB
SwapTotal:       2621436 kB     // swap总大小
SwapFree:        1078024 kB     // swap可用大小
Dirty:               728 kB     // 等待往磁盘回写的大小
Writeback:             0 kB     // 正在往磁盘回写的大小
AnonPages:       2013820 kB     // 匿名页，用户空间的页表，没有对应的文件
Mapped:           449896 kB     // 文件通过mmap分配的内存，用于mmap设备、文件或者库
Shmem:             24056 kB
Slab:             851368 kB
SReclaimable:     500676 kB
SUnreclaim:       350692 kB
KernelStack:       86256 kB
PageTables:       110188 kB
NFS_Unstable:          0 kB
Bounce:                0 kB
WritebackTmp:          0 kB
CommitLimit:     5552480 kB
Committed_AS:   171250016 kB
VmallocTotal:   258867136 kB    // 总分配的虚拟地址空间    
VmallocUsed:           0 kB     // 已使用的虚拟地址空间
VmallocChunk:          0 kB     // 虚拟地址空间可用的最大连续内存块
CmaTotal:         163840 kB
CmaFree:               0 kB
```

### free
查看可用内存，默认单位KB。
```
➜  ~ adb shell free
		total        used        free      shared     buffers
Mem:       6002778112  5861277696   141500416    24522752   579592192
-/+ buffers/cache:     5281685504   721092608
Swap:      2684350464  1537990656  1146359808
```

### showmap
查看虚拟地址区域的内存情况。

showmap -a <pid>
或者
cat proc/<pid>/maps

该命令也需要root权限。

```
➜  ~ adb shell cat proc/15638/maps
cat: proc/15638/maps: Permission denied
```

### vmstat
可以查看内存情况、进程队列、系统切换、CPU时间占比等情况。

```
➜  ~ adb shell vmstat
procs -----------memory---------- ---swap-- -----io---- -system-- ----cpu----
 r  b   swpd   free   buff  cache   si   so    bi    bo   in   cs us sy id wa
 2  0 1511132 150348 477060 1276720  3    8   219    71    2  851 36 23 40  0
```
参数列总共15个参数，分为6大类：
- procs(进程)
    - r: Runing队列中进程数量
    - b: IO wait的进程数量
- memory(内存)
    - swapd 使用虚拟内存大小
    - free 可用内存大小
    - buff 用于文件缓冲的内存大小
    - cache 用于高速缓存的内存大小
- swap(虚拟内存)
    - si swap in 每秒从交换区读到内存的大小
    - so swap out 每秒写入到交换区的内存大小
- io
    - bi block in 从块设备读入的数据总量(KB/s)
    - bo block out 写入到块设备的数据总量(KB/s)
- system
    - in 每秒中断次数
    - cs 每秒上下文切换次数
- CPU(处理器)
    - us user time
    - ni nice time
    - sy system time
    - id idle time
    - wa iowait time
    - ir interrupt time

## 总结
- `dumpsys meminfo` 查看进程的oom adj或者dalvik/native内存情况，或者某个进程或者apk的内存情况
- `procrank` 查看进程的VSS/RSS/PSS/USS各项内存指标，**需要root权限**
- `cat /proc/meminfo` 查看系统的详细内存信息
- `free` 查看系统的可用内存
- `showmap` 查看进程的虚拟地址空间内存分配情况,**需要root权限**
- `vmstat` 查看内存情况、进程队列、系统切换、CPU时间占比等情况