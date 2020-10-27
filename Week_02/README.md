学习笔记
---

### 随机对象创建程序在不同垃圾回收器下的表现与分析

#### 0. 不同 `Xmx` 对GC的影响

* 较大一些的 `Xmx` 可以降低GC次数
* 降低OOM发生的概率

#### 测试统计表

|GC|Xmx|Result|分析|
|----|-----|-----|-----|
|SerialGC|256m|4392 | 随着对的增大吞吐并未提升，反而有所下降 |
|SerialGC|512m|8803 | | 
|SerialGC|1g|9539 | | 
|SerialGC|2g|8445 | | 
|SerialGC|4g|6569 | | 
|ParallelGC|256m| OOM | 堆大小敏感，小堆相比更容易OOM | 
|ParallelGC|512m|7890 | |
|ParallelGC|1g|9983 | | 
|ParallelGC|2g|11088 | 在1-2G的堆大小情况下表现良好, 堆增大有所下降|  
|ParallelGC|4g|7890 | | 
|CMS|256m|4302 | | 
|CMS|512m|9537 | | 
|CMS|1g|11584 | | 
|CMS|2g|12495 | 在1-2G的堆大小情况下表现最优, 堆增大有所下降| 
|CMS|4g|10384 | | 
|G1|256m| OOM | 对于小堆敏感，小堆相比更容易OOM | 
|G1|512m|9731 | | 
|G1|1g|10605 | | 
|G1|2g|11775 | | 
|G1|4g|11750 | 更适合大堆 | 


gateway-server test
---

### SerialGC

*  java -jar -Xms2g -Xmx2g -XX:+UseSerialGC gateway-server-0.0.1-SNAPSHOT.jar
> $ wrk -t8 -c40 -d60s http://localhost:8088/api/hello

`
Running 1m test @ http://localhost:8088/api/hello
  8 threads and 40 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency     5.46ms   20.56ms 332.74ms   94.29%
    Req/Sec     5.28k     1.86k   13.14k    78.68%
  2473910 requests in 1.00m, 295.36MB read
Requests/sec:  41162.76
Transfer/sec:      4.91MB
`

*  java -jar -Xms512m -Xmx512m -XX:+UseSerialGC gateway-server-0.0.1-SNAPSHOT.jar
> wrk -t8 -c40 -d60s http://localhost:8088/api/hello

`
Running 1m test @ http://localhost:8088/api/hello
  8 threads and 40 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency     4.87ms   15.98ms 259.39ms   93.91%
    Req/Sec     4.63k     1.74k    9.87k    63.80%
  2204654 requests in 1.00m, 263.21MB read
Requests/sec:  36690.33
Transfer/sec:      4.38MB
`

现象： 堆内存变小后，latency变好了，吞吐降低了


### ParallelGC

* java -jar -Xms2g -Xmx2g -XX:+UseParallelGC gateway-server-0.0.1-SNAPSHOT.jar

> wrk -t8 -c40 -d60s http://localhost:8088/api/hello

`
Running 1m test @ http://localhost:8088/api/hello
  8 threads and 40 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency     4.95ms   17.29ms 297.98ms   93.98%
    Req/Sec     5.64k     2.01k   14.04k    79.10%
  2516082 requests in 1.00m, 300.40MB read
Requests/sec:  41870.72
Transfer/sec:      5.00MB
`

现象：相比于SerialGC 有更高的吞吐量，更低的延时

### CMS

* java -jar -Xms2g -Xmx2g -XX:+UseConcMarkSweepGC gateway-server-0.0.1-SNAPSHOT.jar

> wrk -t8 -c40 -d60s http://localhost:8088/api/hello

`
Running 1m test @ http://localhost:8088/api/hello
  8 threads and 40 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency     5.38ms   25.04ms 877.96ms   95.08%
    Req/Sec     4.62k     1.80k   10.24k    64.56%
  2176591 requests in 1.00m, 259.86MB read
Requests/sec:  36223.39
Transfer/sec:      4.32MB
`

现象： 相比于ParallelGC， 默认配置的CMS吞吐和延迟都要更差一些


### G1

* java -jar -Xms2g -Xmx2g -XX:+UseG1GC gateway-server-0.0.1-SNAPSHOT.jar

> wrk -t8 -c40 -d60s http://localhost:8088/api/hello

`
Running 1m test @ http://localhost:8088/api/hello
  8 threads and 40 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency     5.96ms   19.73ms 280.90ms   93.41%
    Req/Sec     4.89k     1.79k   10.07k    75.57%
  2326579 requests in 1.00m, 277.77MB read
Requests/sec:  38719.23
Transfer/sec:      4.62MB
`

现象： 吞吐延迟略好于默认配置的CMS，差于ParallelGC


### 提高堆内存到8G 对比 ParallelGC/CMS/G1

* java -jar -Xms8g -Xmx8g -XX:+UseG1GC gateway-server-0.0.1-SNAPSHOT.jar

#### ParallelGC 表现与2g时 有所下降
`
Running 1m test @ http://localhost:8088/api/hello
  8 threads and 40 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency     3.99ms   16.84ms 269.50ms   95.75%
    Req/Sec     5.51k     2.09k   16.38k    78.92%
  2368654 requests in 1.00m, 282.79MB read
Requests/sec:  39416.15
Transfer/sec:      4.71MB
`

#### CMS 表现与2g时 稍微有所提升
`
Running 1m test @ http://localhost:8088/api/hello
  8 threads and 40 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency     5.40ms   17.35ms 278.73ms   93.84%
    Req/Sec     4.76k     1.73k   10.14k    66.56%
  2263958 requests in 1.00m, 270.29MB read
Requests/sec:  37677.72
Transfer/sec:      4.50MB
`

#### G1 表现与2g时 稍微有所下降
`
Running 1m test @ http://localhost:8088/api/hello
  8 threads and 40 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency     5.85ms   19.03ms 339.61ms   93.47%
    Req/Sec     4.76k     1.99k   10.19k    65.47%
  2265965 requests in 1.00m, 270.53MB read
Requests/sec:  37711.78
Transfer/sec:      4.50MB
`

------

[抄作业]

1、高分配速率(High Allocation Rate)

分配速率(Allocation rate)表示单位时间内分配的内存量。通常使用 MB/sec 作为单位。
上一次垃圾收集之后，与下一次 GC 开始之前的年轻代使用量，两者的差值除以时间,就是分配速率。
分配速率过高就会严重影响程序的性能，在 JVM 中可能会导致巨大的 GC 开销。

正常系统：分配速率较低 ~ 回收速率 -> 健康
内存泄漏：分配速率 持续大于 回收速率 -> OOM
性能劣化：分配速率较高 ~ 回收速率 -> 压健康

2、过早提升(Premature Promotion)

提升速率（promotion rate）用于衡量单位时间内从年轻代提升到老年代的数据量。
一般使用 MB/sec 作为单位, 和分配速率类似。

JVM 会将长时间存活的对象从年轻代提升到老年代。根据分代假设，可能存在一种情况，老年代中不仅有存活时间长的对象,
也可能有存活时间短的对象。这就是过早提升：对象存活时间还不够长的时候就被提升到了老年代。

major GC 不是为频繁回收而设计的，但 major GC 现在也要清理这些生命短暂的对象，就会导致 GC 暂停时间过长。
这会严重影响系统的吞吐量。





1. SerialGC

> java -XX:+UseSerialGC -Xms512m -Xmx512m -Xloggc:gc.demo.log -XX:+PrintGCDetails -XX:+PrintGCDateStamps GCLogAnalysis
> OUTPUT: 执行结束!共生成对象次数:9001
GC Log 如下
`
CommandLine flags: -XX:InitialHeapSize=536870912 -XX:MaxHeapSize=536870912 -XX:+PrintGC -XX:+PrintGCDateStamps -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+UseCompressedClassPointers -XX:+UseCompressedOops -XX:+UseSerialGC
2020-10-28T00:45:51.080-0800: 0.168: [GC (Allocation Failure) 2020-10-28T00:45:51.080-0800: 0.168: [DefNew: 139776K->17472K(157248K), 0.0326745 secs] 139776K->50050K(506816K), 0.0327881 secs] [Times: user=0.01 sys=0.02, real=0.03 secs]
2020-10-28T00:45:51.134-0800: 0.222: [GC (Allocation Failure) 2020-10-28T00:45:51.134-0800: 0.222: [DefNew: 157248K->17469K(157248K), 0.0418511 secs] 189826K->98152K(506816K), 0.0419335 secs] [Times: user=0.03 sys=0.02, real=0.04 secs]
2020-10-28T00:45:51.200-0800: 0.288: [GC (Allocation Failure) 2020-10-28T00:45:51.200-0800: 0.288: [DefNew: 157194K->17472K(157248K), 0.0310208 secs] 237877K->142250K(506816K), 0.0311048 secs] [Times: user=0.02 sys=0.01, real=0.03 secs]
2020-10-28T00:45:51.254-0800: 0.343: [GC (Allocation Failure) 2020-10-28T00:45:51.254-0800: 0.343: [DefNew: 157248K->17471K(157248K), 0.0311895 secs] 282026K->183809K(506816K), 0.0312682 secs] [Times: user=0.02 sys=0.01, real=0.03 secs]
2020-10-28T00:45:51.315-0800: 0.403: [GC (Allocation Failure) 2020-10-28T00:45:51.315-0800: 0.403: [DefNew: 157219K->17471K(157248K), 0.0278817 secs] 323557K->223433K(506816K), 0.0279648 secs] [Times: user=0.02 sys=0.01, real=0.03 secs]
2020-10-28T00:45:51.368-0800: 0.457: [GC (Allocation Failure) 2020-10-28T00:45:51.368-0800: 0.457: [DefNew: 157247K->17471K(157248K), 0.0313351 secs] 363209K->266178K(506816K), 0.0314211 secs] [Times: user=0.01 sys=0.01, real=0.04 secs]
2020-10-28T00:45:51.422-0800: 0.511: [GC (Allocation Failure) 2020-10-28T00:45:51.422-0800: 0.511: [DefNew: 157247K->17470K(157248K), 0.0354739 secs] 405954K->315895K(506816K), 0.0355608 secs] [Times: user=0.02 sys=0.01, real=0.03 secs]
2020-10-28T00:45:51.477-0800: 0.565: [GC (Allocation Failure) 2020-10-28T00:45:51.477-0800: 0.565: [DefNew: 157246K->157246K(157248K), 0.0000212 secs]2020-10-28T00:45:51.477-0800: 0.566: [Tenured: 298425K->264154K(349568K), 0.0365040 secs] 455671K->264154K(506816K), [Metaspace: 2715K->2715K(1056768K)], 0.0366353 secs] [Times: user=0.03 sys=0.00, real=0.04 secs]
2020-10-28T00:45:51.537-0800: 0.626: [GC (Allocation Failure) 2020-10-28T00:45:51.537-0800: 0.626: [DefNew: 139776K->17471K(157248K), 0.0065569 secs] 403930K->308076K(506816K), 0.0066493 secs] [Times: user=0.00 sys=0.00, real=0.01 secs]
2020-10-28T00:45:51.565-0800: 0.653: [GC (Allocation Failure) 2020-10-28T00:45:51.565-0800: 0.653: [DefNew: 157247K->17469K(157248K), 0.0275415 secs] 447852K->352058K(506816K), 0.0276301 secs] [Times: user=0.02 sys=0.02, real=0.03 secs]
2020-10-28T00:45:51.615-0800: 0.704: [GC (Allocation Failure) 2020-10-28T00:45:51.615-0800: 0.704: [DefNew: 157109K->157109K(157248K), 0.0000234 secs]2020-10-28T00:45:51.615-0800: 0.704: [Tenured: 334589K->300039K(349568K), 0.0435803 secs] 491698K->300039K(506816K), [Metaspace: 2715K->2715K(1056768K)], 0.0437399 secs] [Times: user=0.05 sys=0.00, real=0.04 secs]
2020-10-28T00:45:51.682-0800: 0.771: [GC (Allocation Failure) 2020-10-28T00:45:51.682-0800: 0.771: [DefNew: 139776K->17471K(157248K), 0.0077204 secs] 439815K->347155K(506816K), 0.0078157 secs] [Times: user=0.01 sys=0.00, real=0.01 secs]
2020-10-28T00:45:51.715-0800: 0.804: [GC (Allocation Failure) 2020-10-28T00:45:51.715-0800: 0.804: [DefNew: 157247K->157247K(157248K), 0.0000224 secs]2020-10-28T00:45:51.715-0800: 0.804: [Tenured: 329683K->318889K(349568K), 0.0509862 secs] 486931K->318889K(506816K), [Metaspace: 2715K->2715K(1056768K)], 0.0511203 secs] [Times: user=0.05 sys=0.00, real=0.05 secs]
2020-10-28T00:45:51.789-0800: 0.878: [GC (Allocation Failure) 2020-10-28T00:45:51.789-0800: 0.878: [DefNew: 139731K->139731K(157248K), 0.0000224 secs]2020-10-28T00:45:51.789-0800: 0.878: [Tenured: 318889K->312119K(349568K), 0.0489409 secs] 458621K->312119K(506816K), [Metaspace: 2715K->2715K(1056768K)], 0.0491057 secs] [Times: user=0.05 sys=0.00, real=0.05 secs]
2020-10-28T00:45:51.862-0800: 0.950: [GC (Allocation Failure) 2020-10-28T00:45:51.862-0800: 0.950: [DefNew: 139776K->139776K(157248K), 0.0000225 secs]2020-10-28T00:45:51.862-0800: 0.950: [Tenured: 312119K->341794K(349568K), 0.0370978 secs] 451895K->341794K(506816K), [Metaspace: 2715K->2715K(1056768K)], 0.0372255 secs] [Times: user=0.03 sys=0.00, real=0.03 secs]
2020-10-28T00:45:51.931-0800: 1.019: [GC (Allocation Failure) 2020-10-28T00:45:51.931-0800: 1.019: [DefNew: 139776K->139776K(157248K), 0.0000224 secs]2020-10-28T00:45:51.931-0800: 1.019: [Tenured: 341794K->348654K(349568K), 0.0483848 secs] 481570K->348654K(506816K), [Metaspace: 2715K->2715K(1056768K)], 0.0485120 secs] [Times: user=0.05 sys=0.00, real=0.04 secs]
Heap
 def new generation   total 157248K, used 90834K [0x00000007a0000000, 0x00000007aaaa0000, 0x00000007aaaa0000)
  eden space 139776K,  64% used [0x00000007a0000000, 0x00000007a58b48c8, 0x00000007a8880000)
  from space 17472K,   0% used [0x00000007a8880000, 0x00000007a8880000, 0x00000007a9990000)
  to   space 17472K,   0% used [0x00000007a9990000, 0x00000007a9990000, 0x00000007aaaa0000)
 tenured generation   total 349568K, used 348654K [0x00000007aaaa0000, 0x00000007c0000000, 0x00000007c0000000)
   the space 349568K,  99% used [0x00000007aaaa0000, 0x00000007bff1bbb8, 0x00000007bff1bc00, 0x00000007c0000000)
 Metaspace       used 2722K, capacity 4486K, committed 4864K, reserved 1056768K
  class space    used 297K, capacity 386K, committed 512K, reserved 1048576K
`

2. ParallelGC

> java -XX:+UseParallelGC -Xms512m -Xmx512m -Xloggc:gc.demo.log -XX:+PrintGCDetails -XX:+PrintGCDateStamps GCLogAnalysis

> OUTPUT: 执行结束!共生成对象次数:7190

GC Log 如下
`
CommandLine flags: -XX:InitialHeapSize=536870912 -XX:MaxHeapSize=536870912 -XX:+PrintGC -XX:+PrintGCDateStamps -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+UseCompressedClassPointers -XX:+UseCompressedOops -XX:+UseParallelGC
2020-10-28T00:54:21.023-0800: 0.172: [GC (Allocation Failure) [PSYoungGen: 131280K->21502K(153088K)] 131280K->47714K(502784K), 0.0234132 secs] [Times: user=0.03 sys=0.08, real=0.02 secs]
2020-10-28T00:54:21.070-0800: 0.219: [GC (Allocation Failure) [PSYoungGen: 153086K->21490K(153088K)] 179298K->94478K(502784K), 0.0309568 secs] [Times: user=0.03 sys=0.15, real=0.03 secs]
2020-10-28T00:54:21.125-0800: 0.274: [GC (Allocation Failure) [PSYoungGen: 153074K->21496K(153088K)] 226062K->136012K(502784K), 0.0236648 secs] [Times: user=0.04 sys=0.10, real=0.02 secs]
2020-10-28T00:54:21.172-0800: 0.321: [GC (Allocation Failure) [PSYoungGen: 153080K->21499K(153088K)] 267596K->178081K(502784K), 0.0269233 secs] [Times: user=0.05 sys=0.09, real=0.02 secs]
2020-10-28T00:54:21.225-0800: 0.373: [GC (Allocation Failure) [PSYoungGen: 153083K->21487K(153088K)] 309665K->214143K(502784K), 0.0251395 secs] [Times: user=0.04 sys=0.08, real=0.03 secs]
2020-10-28T00:54:21.279-0800: 0.428: [GC (Allocation Failure) [PSYoungGen: 153071K->21488K(80384K)] 345727K->250729K(430080K), 0.0277680 secs] [Times: user=0.05 sys=0.08, real=0.03 secs]
2020-10-28T00:54:21.317-0800: 0.466: [GC (Allocation Failure) [PSYoungGen: 80368K->37305K(116736K)] 309609K->272901K(466432K), 0.0078268 secs] [Times: user=0.02 sys=0.01, real=0.01 secs]
2020-10-28T00:54:21.339-0800: 0.488: [GC (Allocation Failure) [PSYoungGen: 96141K->53127K(116736K)] 331738K->292673K(466432K), 0.0105408 secs] [Times: user=0.05 sys=0.00, real=0.02 secs]
2020-10-28T00:54:21.364-0800: 0.512: [GC (Allocation Failure) [PSYoungGen: 111952K->57850K(116736K)] 351498K->312744K(466432K), 0.0188696 secs] [Times: user=0.05 sys=0.03, real=0.02 secs]
2020-10-28T00:54:21.394-0800: 0.543: [GC (Allocation Failure) [PSYoungGen: 116730K->45149K(116736K)] 371624K->335030K(466432K), 0.0246732 secs] [Times: user=0.05 sys=0.08, real=0.02 secs]
2020-10-28T00:54:21.429-0800: 0.577: [GC (Allocation Failure) [PSYoungGen: 104029K->22767K(116736K)] 393910K->355881K(466432K), 0.0252786 secs] [Times: user=0.03 sys=0.10, real=0.03 secs]
2020-10-28T00:54:21.454-0800: 0.603: [Full GC (Ergonomics) [PSYoungGen: 22767K->0K(116736K)] [ParOldGen: 333113K->244626K(349696K)] 355881K->244626K(466432K), [Metaspace: 2715K->2715K(1056768K)], 0.0442371 secs] [Times: user=0.19 sys=0.01, real=0.04 secs]
2020-10-28T00:54:21.512-0800: 0.661: [GC (Allocation Failure) [PSYoungGen: 58687K->18289K(116736K)] 303313K->262916K(466432K), 0.0040063 secs] [Times: user=0.02 sys=0.00, real=0.00 secs]
2020-10-28T00:54:21.529-0800: 0.677: [GC (Allocation Failure) [PSYoungGen: 77096K->17594K(116736K)] 321722K->279655K(466432K), 0.0066334 secs] [Times: user=0.03 sys=0.00, real=0.01 secs]
2020-10-28T00:54:21.546-0800: 0.695: [GC (Allocation Failure) [PSYoungGen: 76474K->21126K(116736K)] 338535K->299321K(466432K), 0.0072560 secs] [Times: user=0.03 sys=0.00, real=0.01 secs]
2020-10-28T00:54:21.565-0800: 0.714: [GC (Allocation Failure) [PSYoungGen: 79408K->20824K(116736K)] 357603K->317926K(466432K), 0.0074116 secs] [Times: user=0.04 sys=0.00, real=0.01 secs]
2020-10-28T00:54:21.583-0800: 0.732: [GC (Allocation Failure) [PSYoungGen: 79630K->20146K(116736K)] 376733K->337326K(466432K), 0.0068784 secs] [Times: user=0.03 sys=0.00, real=0.01 secs]
2020-10-28T00:54:21.590-0800: 0.739: [Full GC (Ergonomics) [PSYoungGen: 20146K->0K(116736K)] [ParOldGen: 317180K->270459K(349696K)] 337326K->270459K(466432K), [Metaspace: 2715K->2715K(1056768K)], 0.0431485 secs] [Times: user=0.17 sys=0.01, real=0.04 secs]
2020-10-28T00:54:21.650-0800: 0.799: [GC (Allocation Failure) [PSYoungGen: 58880K->20347K(116736K)] 329339K->290806K(466432K), 0.0050238 secs] [Times: user=0.02 sys=0.00, real=0.00 secs]
2020-10-28T00:54:21.666-0800: 0.815: [GC (Allocation Failure) [PSYoungGen: 79227K->20687K(116736K)] 349686K->310771K(466432K), 0.0071193 secs] [Times: user=0.03 sys=0.00, real=0.01 secs]
2020-10-28T00:54:21.687-0800: 0.836: [GC (Allocation Failure) [PSYoungGen: 79567K->19509K(116736K)] 369651K->329265K(466432K), 0.0067448 secs] [Times: user=0.02 sys=0.00, real=0.01 secs]
2020-10-28T00:54:21.694-0800: 0.843: [Full GC (Ergonomics) [PSYoungGen: 19509K->0K(116736K)] [ParOldGen: 309755K->283367K(349696K)] 329265K->283367K(466432K), [Metaspace: 2715K->2715K(1056768K)], 0.0389179 secs] [Times: user=0.21 sys=0.01, real=0.04 secs]
2020-10-28T00:54:21.749-0800: 0.898: [GC (Allocation Failure) [PSYoungGen: 58880K->19795K(116736K)] 342247K->303162K(466432K), 0.0062135 secs] [Times: user=0.02 sys=0.00, real=0.01 secs]
2020-10-28T00:54:21.769-0800: 0.918: [GC (Allocation Failure) [PSYoungGen: 78675K->25194K(116736K)] 362042K->327090K(466432K), 0.0066604 secs] [Times: user=0.03 sys=0.00, real=0.01 secs]
2020-10-28T00:54:21.789-0800: 0.938: [GC (Allocation Failure) [PSYoungGen: 84074K->19782K(116736K)] 385970K->345902K(466432K), 0.0064632 secs] [Times: user=0.05 sys=0.00, real=0.01 secs]
2020-10-28T00:54:21.795-0800: 0.944: [Full GC (Ergonomics) [PSYoungGen: 19782K->0K(116736K)] [ParOldGen: 326120K->298366K(349696K)] 345902K->298366K(466432K), [Metaspace: 2715K->2715K(1056768K)], 0.0434107 secs] [Times: user=0.18 sys=0.00, real=0.04 secs]
2020-10-28T00:54:21.856-0800: 1.004: [GC (Allocation Failure) [PSYoungGen: 58418K->20225K(116736K)] 356785K->318592K(466432K), 0.0044914 secs] [Times: user=0.02 sys=0.00, real=0.01 secs]
2020-10-28T00:54:21.874-0800: 1.023: [GC (Allocation Failure) [PSYoungGen: 79105K->21015K(116736K)] 377472K->337466K(466432K), 0.0050291 secs] [Times: user=0.03 sys=0.00, real=0.00 secs]
2020-10-28T00:54:21.880-0800: 1.028: [Full GC (Ergonomics) [PSYoungGen: 21015K->0K(116736K)] [ParOldGen: 316450K->304506K(349696K)] 337466K->304506K(466432K), [Metaspace: 2715K->2715K(1056768K)], 0.0441382 secs] [Times: user=0.20 sys=0.01, real=0.04 secs]
Heap
 PSYoungGen      total 116736K, used 52162K [0x00000007b5580000, 0x00000007c0000000, 0x00000007c0000000)
  eden space 58880K, 88% used [0x00000007b5580000,0x00000007b8870bc8,0x00000007b8f00000)
  from space 57856K, 0% used [0x00000007bc780000,0x00000007bc780000,0x00000007c0000000)
  to   space 57856K, 0% used [0x00000007b8f00000,0x00000007b8f00000,0x00000007bc780000)
 ParOldGen       total 349696K, used 304506K [0x00000007a0000000, 0x00000007b5580000, 0x00000007b5580000)
  object space 349696K, 87% used [0x00000007a0000000,0x00000007b295eba0,0x00000007b5580000)
 Metaspace       used 2721K, capacity 4486K, committed 4864K, reserved 1056768K
  class space    used 297K, capacity 386K, committed 512K, reserved 1048576K
`

3. ConcMarkSweepGC (CMS)

* 阶段 1:Initial Mark(初始标记)
* 阶段 2:Concurrent Mark(并发标记)
* 阶段 3:Concurrent Preclean(并发预清理)
* 阶段 4: Final Remark(最终标记)
* 阶段 5: Concurrent Sweep(并发清除)
* 阶段 6: Concurrent Reset(并发重置)

> java -XX:+UseConcMarkSweepGC -Xms512m -Xmx512m -Xloggc:gc.demo.log -XX:+PrintGCDetails -XX:+PrintGCDateStamps GCLogAnalysis
> OUTPUT: 执行结束!共生成对象次数:9537
GC Log如下:
`
CommandLine flags: -XX:InitialHeapSize=536870912 -XX:MaxHeapSize=536870912 -XX:MaxNewSize=178958336 -XX:MaxTenuringThreshold=6 -XX:NewSize=178958336 -XX:OldPLABSize=16 -XX:OldSize=357912576 -XX:+PrintGC -XX:+PrintGCDateStamps -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+UseCompressedClassPointers -XX:+UseCompressedOops -XX:+UseConcMarkSweepGC -XX:+UseParNewGC
2020-10-28T01:03:51.451-0800: 0.144: [GC (Allocation Failure) 2020-10-28T01:03:51.451-0800: 0.144: [ParNew: 139776K->17471K(157248K), 0.0220602 secs] 139776K->50108K(506816K), 0.0221704 secs] [Times: user=0.04 sys=0.10, real=0.02 secs]
2020-10-28T01:03:51.491-0800: 0.183: [GC (Allocation Failure) 2020-10-28T01:03:51.491-0800: 0.183: [ParNew: 157247K->17472K(157248K), 0.0211047 secs] 189884K->97944K(506816K), 0.0211875 secs] [Times: user=0.04 sys=0.09, real=0.02 secs]
2020-10-28T01:03:51.530-0800: 0.222: [GC (Allocation Failure) 2020-10-28T01:03:51.530-0800: 0.222: [ParNew: 157197K->17472K(157248K), 0.0243971 secs] 237669K->141895K(506816K), 0.0245181 secs] [Times: user=0.17 sys=0.02, real=0.02 secs]
2020-10-28T01:03:51.572-0800: 0.264: [GC (Allocation Failure) 2020-10-28T01:03:51.572-0800: 0.264: [ParNew: 157248K->17472K(157248K), 0.0232801 secs] 281671K->183645K(506816K), 0.0234239 secs] [Times: user=0.15 sys=0.01, real=0.02 secs]
2020-10-28T01:03:51.617-0800: 0.309: [GC (Allocation Failure) 2020-10-28T01:03:51.617-0800: 0.309: [ParNew: 157220K->17470K(157248K), 0.0224583 secs] 323394K->223720K(506816K), 0.0225717 secs] [Times: user=0.15 sys=0.02, real=0.02 secs]
2020-10-28T01:03:51.640-0800: 0.332: [GC (CMS Initial Mark) [1 CMS-initial-mark: 206250K(349568K)] 224464K(506816K), 0.0002607 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
2020-10-28T01:03:51.640-0800: 0.332: [CMS-concurrent-mark-start]
2020-10-28T01:03:51.642-0800: 0.334: [CMS-concurrent-mark: 0.002/0.002 secs] [Times: user=0.01 sys=0.00, real=0.00 secs]
2020-10-28T01:03:51.642-0800: 0.334: [CMS-concurrent-preclean-start]
2020-10-28T01:03:51.643-0800: 0.335: [CMS-concurrent-preclean: 0.001/0.001 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
2020-10-28T01:03:51.643-0800: 0.335: [CMS-concurrent-abortable-preclean-start]
2020-10-28T01:03:51.658-0800: 0.350: [GC (Allocation Failure) 2020-10-28T01:03:51.658-0800: 0.350: [ParNew: 157246K->17467K(157248K), 0.0236930 secs] 363496K->266807K(506816K), 0.0238292 secs] [Times: user=0.16 sys=0.02, real=0.03 secs]
2020-10-28T01:03:51.698-0800: 0.390: [GC (Allocation Failure) 2020-10-28T01:03:51.698-0800: 0.390: [ParNew: 157243K->17471K(157248K), 0.0285548 secs] 406583K->316474K(506816K), 0.0286549 secs] [Times: user=0.21 sys=0.01, real=0.03 secs]
2020-10-28T01:03:51.742-0800: 0.435: [GC (Allocation Failure) 2020-10-28T01:03:51.742-0800: 0.435: [ParNew: 157247K->157247K(157248K), 0.0000195 secs]2020-10-28T01:03:51.743-0800: 0.435: [CMS2020-10-28T01:03:51.743-0800: 0.435: [CMS-concurrent-abortable-preclean: 0.003/0.100 secs] [Times: user=0.41 sys=0.03, real=0.10 secs]
 (concurrent mode failure): 299003K->246831K(349568K), 0.0386008 secs] 456250K->246831K(506816K), [Metaspace: 2716K->2716K(1056768K)], 0.0387225 secs] [Times: user=0.04 sys=0.00, real=0.04 secs]
2020-10-28T01:03:51.801-0800: 0.493: [GC (Allocation Failure) 2020-10-28T01:03:51.801-0800: 0.493: [ParNew: 139776K->17471K(157248K), 0.0064226 secs] 386607K->290230K(506816K), 0.0065084 secs] [Times: user=0.04 sys=0.00, real=0.00 secs]
2020-10-28T01:03:51.807-0800: 0.500: [GC (CMS Initial Mark) [1 CMS-initial-mark: 272758K(349568K)] 290578K(506816K), 0.0001808 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
2020-10-28T01:03:51.808-0800: 0.500: [CMS-concurrent-mark-start]
2020-10-28T01:03:51.809-0800: 0.501: [CMS-concurrent-mark: 0.001/0.001 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
2020-10-28T01:03:51.809-0800: 0.501: [CMS-concurrent-preclean-start]
2020-10-28T01:03:51.809-0800: 0.502: [CMS-concurrent-preclean: 0.001/0.001 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
2020-10-28T01:03:51.809-0800: 0.502: [CMS-concurrent-abortable-preclean-start]
2020-10-28T01:03:51.827-0800: 0.519: [GC (Allocation Failure) 2020-10-28T01:03:51.827-0800: 0.519: [ParNew: 157247K->17471K(157248K), 0.0153342 secs] 430006K->334737K(506816K), 0.0154158 secs] [Times: user=0.11 sys=0.01, real=0.02 secs]
2020-10-28T01:03:51.860-0800: 0.552: [GC (Allocation Failure) 2020-10-28T01:03:51.860-0800: 0.552: [ParNew: 157247K->157247K(157248K), 0.0000199 secs]2020-10-28T01:03:51.860-0800: 0.552: [CMS2020-10-28T01:03:51.860-0800: 0.552: [CMS-concurrent-abortable-preclean: 0.002/0.051 secs] [Times: user=0.15 sys=0.01, real=0.06 secs]
 (concurrent mode failure): 317265K->283035K(349568K), 0.0391064 secs] 474513K->283035K(506816K), [Metaspace: 2716K->2716K(1056768K)], 0.0392312 secs] [Times: user=0.04 sys=0.00, real=0.03 secs]
2020-10-28T01:03:51.919-0800: 0.611: [GC (Allocation Failure) 2020-10-28T01:03:51.919-0800: 0.611: [ParNew: 139776K->17471K(157248K), 0.0068496 secs] 422811K->330797K(506816K), 0.0069369 secs] [Times: user=0.05 sys=0.00, real=0.01 secs]
2020-10-28T01:03:51.926-0800: 0.618: [GC (CMS Initial Mark) [1 CMS-initial-mark: 313325K(349568K)] 331556K(506816K), 0.0002027 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
2020-10-28T01:03:51.926-0800: 0.618: [CMS-concurrent-mark-start]
2020-10-28T01:03:51.927-0800: 0.620: [CMS-concurrent-mark: 0.001/0.001 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
2020-10-28T01:03:51.927-0800: 0.620: [CMS-concurrent-preclean-start]
2020-10-28T01:03:51.928-0800: 0.620: [CMS-concurrent-preclean: 0.000/0.000 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
2020-10-28T01:03:51.928-0800: 0.620: [CMS-concurrent-abortable-preclean-start]
2020-10-28T01:03:51.944-0800: 0.636: [GC (Allocation Failure) 2020-10-28T01:03:51.944-0800: 0.636: [ParNew: 157247K->157247K(157248K), 0.0000181 secs]2020-10-28T01:03:51.944-0800: 0.636: [CMS2020-10-28T01:03:51.944-0800: 0.636: [CMS-concurrent-abortable-preclean: 0.001/0.016 secs] [Times: user=0.02 sys=0.00, real=0.02 secs]
 (concurrent mode failure): 313325K->302361K(349568K), 0.0449274 secs] 470573K->302361K(506816K), [Metaspace: 2716K->2716K(1056768K)], 0.0450629 secs] [Times: user=0.05 sys=0.00, real=0.04 secs]
2020-10-28T01:03:52.009-0800: 0.701: [GC (Allocation Failure) 2020-10-28T01:03:52.009-0800: 0.701: [ParNew: 139776K->17469K(157248K), 0.0098272 secs] 442137K->345500K(506816K), 0.0098921 secs] [Times: user=0.05 sys=0.01, real=0.01 secs]
2020-10-28T01:03:52.019-0800: 0.711: [GC (CMS Initial Mark) [1 CMS-initial-mark: 328031K(349568K)] 346296K(506816K), 0.0001679 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
2020-10-28T01:03:52.019-0800: 0.712: [CMS-concurrent-mark-start]
2020-10-28T01:03:52.021-0800: 0.713: [CMS-concurrent-mark: 0.002/0.002 secs] [Times: user=0.01 sys=0.00, real=0.01 secs]
2020-10-28T01:03:52.021-0800: 0.713: [CMS-concurrent-preclean-start]
2020-10-28T01:03:52.022-0800: 0.714: [CMS-concurrent-preclean: 0.001/0.001 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
2020-10-28T01:03:52.022-0800: 0.714: [CMS-concurrent-abortable-preclean-start]
2020-10-28T01:03:52.022-0800: 0.714: [CMS-concurrent-abortable-preclean: 0.000/0.000 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
2020-10-28T01:03:52.022-0800: 0.714: [GC (CMS Final Remark) [YG occupancy: 34277 K (157248 K)]2020-10-28T01:03:52.022-0800: 0.714: [Rescan (parallel) , 0.0013508 secs]2020-10-28T01:03:52.023-0800: 0.716: [weak refs processing, 0.0000214 secs]2020-10-28T01:03:52.023-0800: 0.716: [class unloading, 0.0002391 secs]2020-10-28T01:03:52.024-0800: 0.716: [scrub symbol table, 0.0003267 secs]2020-10-28T01:03:52.024-0800: 0.716: [scrub string table, 0.0001758 secs][1 CMS-remark: 328031K(349568K)] 362309K(506816K), 0.0022257 secs] [Times: user=0.01 sys=0.00, real=0.00 secs]
2020-10-28T01:03:52.024-0800: 0.717: [CMS-concurrent-sweep-start]
2020-10-28T01:03:52.025-0800: 0.717: [CMS-concurrent-sweep: 0.001/0.001 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
2020-10-28T01:03:52.025-0800: 0.717: [CMS-concurrent-reset-start]
2020-10-28T01:03:52.025-0800: 0.718: [CMS-concurrent-reset: 0.000/0.000 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
2020-10-28T01:03:52.043-0800: 0.736: [GC (Allocation Failure) 2020-10-28T01:03:52.043-0800: 0.736: [ParNew: 157245K->17470K(157248K), 0.0164396 secs] 451483K->358604K(506816K), 0.0165508 secs] [Times: user=0.09 sys=0.00, real=0.02 secs]
2020-10-28T01:03:52.060-0800: 0.752: [GC (CMS Initial Mark) [1 CMS-initial-mark: 341134K(349568K)] 358640K(506816K), 0.0001303 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
2020-10-28T01:03:52.060-0800: 0.752: [CMS-concurrent-mark-start]
2020-10-28T01:03:52.062-0800: 0.754: [CMS-concurrent-mark: 0.002/0.002 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
2020-10-28T01:03:52.062-0800: 0.754: [CMS-concurrent-preclean-start]
2020-10-28T01:03:52.062-0800: 0.755: [CMS-concurrent-preclean: 0.001/0.001 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
2020-10-28T01:03:52.062-0800: 0.755: [CMS-concurrent-abortable-preclean-start]
2020-10-28T01:03:52.062-0800: 0.755: [CMS-concurrent-abortable-preclean: 0.000/0.000 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
2020-10-28T01:03:52.062-0800: 0.755: [GC (CMS Final Remark) [YG occupancy: 32201 K (157248 K)]2020-10-28T01:03:52.062-0800: 0.755: [Rescan (parallel) , 0.0008607 secs]2020-10-28T01:03:52.063-0800: 0.756: [weak refs processing, 0.0000411 secs]2020-10-28T01:03:52.063-0800: 0.756: [class unloading, 0.0003116 secs]2020-10-28T01:03:52.064-0800: 0.756: [scrub symbol table, 0.0003012 secs]2020-10-28T01:03:52.064-0800: 0.756: [scrub string table, 0.0003728 secs][1 CMS-remark: 341134K(349568K)] 373336K(506816K), 0.0020417 secs] [Times: user=0.01 sys=0.00, real=0.00 secs]
2020-10-28T01:03:52.065-0800: 0.757: [CMS-concurrent-sweep-start]
2020-10-28T01:03:52.065-0800: 0.758: [CMS-concurrent-sweep: 0.001/0.001 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
2020-10-28T01:03:52.065-0800: 0.758: [CMS-concurrent-reset-start]
2020-10-28T01:03:52.066-0800: 0.758: [CMS-concurrent-reset: 0.000/0.000 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
2020-10-28T01:03:52.083-0800: 0.776: [GC (Allocation Failure) 2020-10-28T01:03:52.083-0800: 0.776: [ParNew: 157246K->157246K(157248K), 0.0000301 secs]2020-10-28T01:03:52.083-0800: 0.776: [CMS: 306857K->330861K(349568K), 0.0478869 secs] 464103K->330861K(506816K), [Metaspace: 2716K->2716K(1056768K)], 0.0480531 secs] [Times: user=0.05 sys=0.00, real=0.05 secs]
2020-10-28T01:03:52.132-0800: 0.824: [GC (CMS Initial Mark) [1 CMS-initial-mark: 330861K(349568K)] 331334K(506816K), 0.0001608 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
2020-10-28T01:03:52.132-0800: 0.824: [CMS-concurrent-mark-start]
2020-10-28T01:03:52.133-0800: 0.825: [CMS-concurrent-mark: 0.001/0.001 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
2020-10-28T01:03:52.133-0800: 0.825: [CMS-concurrent-preclean-start]
2020-10-28T01:03:52.134-0800: 0.826: [CMS-concurrent-preclean: 0.001/0.001 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
2020-10-28T01:03:52.134-0800: 0.826: [CMS-concurrent-abortable-preclean-start]
2020-10-28T01:03:52.150-0800: 0.842: [GC (Allocation Failure) 2020-10-28T01:03:52.150-0800: 0.842: [ParNew: 139776K->139776K(157248K), 0.0000202 secs]2020-10-28T01:03:52.150-0800: 0.843: [CMS2020-10-28T01:03:52.150-0800: 0.843: [CMS-concurrent-abortable-preclean: 0.001/0.017 secs] [Times: user=0.02 sys=0.00, real=0.02 secs]
 (concurrent mode failure): 330861K->330779K(349568K), 0.0464813 secs] 470637K->330779K(506816K), [Metaspace: 2716K->2716K(1056768K)], 0.0466374 secs] [Times: user=0.04 sys=0.00, real=0.04 secs]
2020-10-28T01:03:52.215-0800: 0.907: [GC (Allocation Failure) 2020-10-28T01:03:52.215-0800: 0.907: [ParNew: 139776K->139776K(157248K), 0.0000186 secs]2020-10-28T01:03:52.215-0800: 0.907: [CMS: 330779K->334165K(349568K), 0.0493011 secs] 470555K->334165K(506816K), [Metaspace: 2716K->2716K(1056768K)], 0.0494360 secs] [Times: user=0.05 sys=0.00, real=0.05 secs]
2020-10-28T01:03:52.265-0800: 0.957: [GC (CMS Initial Mark) [1 CMS-initial-mark: 334165K(349568K)] 334299K(506816K), 0.0002184 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
2020-10-28T01:03:52.265-0800: 0.957: [CMS-concurrent-mark-start]
2020-10-28T01:03:52.266-0800: 0.959: [CMS-concurrent-mark: 0.002/0.002 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
2020-10-28T01:03:52.266-0800: 0.959: [CMS-concurrent-preclean-start]
2020-10-28T01:03:52.267-0800: 0.959: [CMS-concurrent-preclean: 0.001/0.001 secs] [Times: user=0.01 sys=0.00, real=0.00 secs]
2020-10-28T01:03:52.267-0800: 0.959: [CMS-concurrent-abortable-preclean-start]
2020-10-28T01:03:52.267-0800: 0.959: [CMS-concurrent-abortable-preclean: 0.000/0.000 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
2020-10-28T01:03:52.267-0800: 0.959: [GC (CMS Final Remark) [YG occupancy: 16147 K (157248 K)]2020-10-28T01:03:52.267-0800: 0.959: [Rescan (parallel) , 0.0008271 secs]2020-10-28T01:03:52.268-0800: 0.960: [weak refs processing, 0.0000324 secs]2020-10-28T01:03:52.268-0800: 0.960: [class unloading, 0.0002724 secs]2020-10-28T01:03:52.268-0800: 0.961: [scrub symbol table, 0.0003105 secs]2020-10-28T01:03:52.269-0800: 0.961: [scrub string table, 0.0001574 secs][1 CMS-remark: 334165K(349568K)] 350313K(506816K), 0.0016795 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
2020-10-28T01:03:52.269-0800: 0.961: [CMS-concurrent-sweep-start]
2020-10-28T01:03:52.269-0800: 0.962: [CMS-concurrent-sweep: 0.001/0.001 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
2020-10-28T01:03:52.269-0800: 0.962: [CMS-concurrent-reset-start]
2020-10-28T01:03:52.270-0800: 0.962: [CMS-concurrent-reset: 0.000/0.000 secs] [Times: user=0.00 sys=0.00, real=0.01 secs]
2020-10-28T01:03:52.285-0800: 0.977: [GC (Allocation Failure) 2020-10-28T01:03:52.285-0800: 0.977: [ParNew: 139776K->139776K(157248K), 0.0000327 secs]2020-10-28T01:03:52.285-0800: 0.977: [CMS: 333974K->332395K(349568K), 0.0526707 secs] 473750K->332395K(506816K), [Metaspace: 2716K->2716K(1056768K)], 0.0528282 secs] [Times: user=0.05 sys=0.00, real=0.05 secs]
2020-10-28T01:03:52.338-0800: 1.030: [GC (CMS Initial Mark) [1 CMS-initial-mark: 332395K(349568K)] 335195K(506816K), 0.0002383 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
2020-10-28T01:03:52.338-0800: 1.030: [CMS-concurrent-mark-start]
2020-10-28T01:03:52.339-0800: 1.032: [CMS-concurrent-mark: 0.001/0.001 secs] [Times: user=0.00 sys=0.00, real=0.01 secs]
2020-10-28T01:03:52.340-0800: 1.032: [CMS-concurrent-preclean-start]
2020-10-28T01:03:52.340-0800: 1.033: [CMS-concurrent-preclean: 0.001/0.001 secs] [Times: user=0.01 sys=0.00, real=0.00 secs]
2020-10-28T01:03:52.340-0800: 1.033: [CMS-concurrent-abortable-preclean-start]
2020-10-28T01:03:52.340-0800: 1.033: [CMS-concurrent-abortable-preclean: 0.000/0.000 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
2020-10-28T01:03:52.341-0800: 1.033: [GC (CMS Final Remark) [YG occupancy: 19563 K (157248 K)]2020-10-28T01:03:52.341-0800: 1.033: [Rescan (parallel) , 0.0002165 secs]2020-10-28T01:03:52.341-0800: 1.033: [weak refs processing, 0.0000399 secs]2020-10-28T01:03:52.341-0800: 1.033: [class unloading, 0.0003579 secs]2020-10-28T01:03:52.341-0800: 1.034: [scrub symbol table, 0.0003718 secs]2020-10-28T01:03:52.342-0800: 1.034: [scrub string table, 0.0002521 secs][1 CMS-remark: 332395K(349568K)] 351958K(506816K), 0.0013554 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
2020-10-28T01:03:52.342-0800: 1.034: [CMS-concurrent-sweep-start]
2020-10-28T01:03:52.343-0800: 1.035: [CMS-concurrent-sweep: 0.001/0.001 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
2020-10-28T01:03:52.343-0800: 1.035: [CMS-concurrent-reset-start]
2020-10-28T01:03:52.343-0800: 1.036: [CMS-concurrent-reset: 0.000/0.000 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
2020-10-28T01:03:52.357-0800: 1.049: [GC (Allocation Failure) 2020-10-28T01:03:52.357-0800: 1.049: [ParNew: 139776K->139776K(157248K), 0.0000275 secs]2020-10-28T01:03:52.357-0800: 1.049: [CMS: 332390K->335595K(349568K), 0.0434309 secs] 472166K->335595K(506816K), [Metaspace: 2716K->2716K(1056768K)], 0.0435952 secs] [Times: user=0.05 sys=0.00, real=0.05 secs]
2020-10-28T01:03:52.401-0800: 1.093: [GC (CMS Initial Mark) [1 CMS-initial-mark: 335595K(349568K)] 335883K(506816K), 0.0002509 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
2020-10-28T01:03:52.401-0800: 1.093: [CMS-concurrent-mark-start]
Heap
 par new generation   total 157248K, used 5851K [0x00000007a0000000, 0x00000007aaaa0000, 0x00000007aaaa0000)
  eden space 139776K,   4% used [0x00000007a0000000, 0x00000007a05b6ca8, 0x00000007a8880000)
  from space 17472K,   0% used [0x00000007a8880000, 0x00000007a8880000, 0x00000007a9990000)
  to   space 17472K,   0% used [0x00000007a9990000, 0x00000007a9990000, 0x00000007aaaa0000)
 concurrent mark-sweep generation total 349568K, used 335595K [0x00000007aaaa0000, 0x00000007c0000000, 0x00000007c0000000)
 Metaspace       used 2722K, capacity 4486K, committed 4864K, reserved 1056768K
  class space    used 297K, capacity 386K, committed 512K, reserved 1048576K
`

4. G1 GC

* Evacuation Pause: young(纯年轻代模式转移暂停)
* Concurrent Marking(并发标记)
* 阶段 1: Initial Mark(初始标记)
* 阶段 2: Root Region Scan(Root区扫描)
* 阶段 3: Concurrent Mark(并发标记)
* 阶段 4: Remark(再次标记)
* 阶段 5: Cleanup(清理)
* Evacuation Pause (mixed)(转移暂停: 混合模式)
* Full GC (Allocation Failure)

> java -XX:+UseG1GC -Xms512m -Xmx512m -Xloggc:gc.demo.log -XX:+PrintGCDetails -XX:+PrintGCDateStamps GCLogAnalysis
> OUTPUT: 执行结束!共生成对象次数:9200
`
CommandLine flags: -XX:InitialHeapSize=536870912 -XX:MaxHeapSize=536870912 -XX:+PrintGC -XX:+PrintGCDateStamps -XX:+PrintGCTimeStamps -XX:+UseCompressedClassPointers -XX:+UseCompressedOops -XX:+UseG1GC
2020-10-28T01:05:37.102-0800: 0.111: [GC pause (G1 Evacuation Pause) (young) 36M->13M(512M), 0.0040614 secs]
2020-10-28T01:05:37.113-0800: 0.122: [GC pause (G1 Evacuation Pause) (young) 38M->23M(512M), 0.0030953 secs]
2020-10-28T01:05:37.141-0800: 0.150: [GC pause (G1 Evacuation Pause) (young) 86M->46M(512M), 0.0071410 secs]
2020-10-28T01:05:37.170-0800: 0.179: [GC pause (G1 Evacuation Pause) (young) 118M->73M(512M), 0.0101765 secs]
2020-10-28T01:05:37.205-0800: 0.214: [GC pause (G1 Evacuation Pause) (young) 161M->103M(512M), 0.0100811 secs]
2020-10-28T01:05:37.246-0800: 0.255: [GC pause (G1 Evacuation Pause) (young) 222M->142M(512M), 0.0157674 secs]
2020-10-28T01:05:37.303-0800: 0.312: [GC pause (G1 Evacuation Pause) (young) 299M->186M(512M), 0.0188105 secs]
2020-10-28T01:05:37.338-0800: 0.347: [GC pause (G1 Humongous Allocation) (young) (initial-mark) 288M->212M(512M), 0.0099213 secs]
2020-10-28T01:05:37.348-0800: 0.357: [GC concurrent-root-region-scan-start]
2020-10-28T01:05:37.348-0800: 0.357: [GC concurrent-root-region-scan-end, 0.0001618 secs]
2020-10-28T01:05:37.348-0800: 0.357: [GC concurrent-mark-start]
2020-10-28T01:05:37.350-0800: 0.359: [GC concurrent-mark-end, 0.0018107 secs]
2020-10-28T01:05:37.350-0800: 0.359: [GC remark, 0.0012812 secs]
2020-10-28T01:05:37.352-0800: 0.361: [GC cleanup 223M->223M(512M), 0.0006471 secs]
2020-10-28T01:05:37.407-0800: 0.416: [GC pause (G1 Evacuation Pause) (young)-- 416M->335M(512M), 0.0170691 secs]
2020-10-28T01:05:37.425-0800: 0.434: [GC pause (G1 Evacuation Pause) (mixed) 340M->321M(512M), 0.0044715 secs]
2020-10-28T01:05:37.430-0800: 0.439: [GC pause (G1 Humongous Allocation) (young) (initial-mark) 325M->323M(512M), 0.0011566 secs]
2020-10-28T01:05:37.431-0800: 0.440: [GC concurrent-root-region-scan-start]
2020-10-28T01:05:37.431-0800: 0.440: [GC concurrent-root-region-scan-end, 0.0000935 secs]
2020-10-28T01:05:37.431-0800: 0.440: [GC concurrent-mark-start]
2020-10-28T01:05:37.432-0800: 0.441: [GC concurrent-mark-end, 0.0008976 secs]
2020-10-28T01:05:37.432-0800: 0.441: [GC remark, 0.0011032 secs]
2020-10-28T01:05:37.433-0800: 0.442: [GC cleanup 329M->328M(512M), 0.0006379 secs]
2020-10-28T01:05:37.434-0800: 0.443: [GC concurrent-cleanup-start]
2020-10-28T01:05:37.434-0800: 0.443: [GC concurrent-cleanup-end, 0.0000112 secs]
2020-10-28T01:05:37.450-0800: 0.459: [GC pause (G1 Evacuation Pause) (young) 425M->357M(512M), 0.0046565 secs]
2020-10-28T01:05:37.458-0800: 0.467: [GC pause (G1 Evacuation Pause) (mixed) 374M->316M(512M), 0.0036733 secs]
2020-10-28T01:05:37.467-0800: 0.476: [GC pause (G1 Evacuation Pause) (mixed) 349M->291M(512M), 0.0044154 secs]
2020-10-28T01:05:37.476-0800: 0.485: [GC pause (G1 Evacuation Pause) (mixed) 317M->287M(512M), 0.0050064 secs]
2020-10-28T01:05:37.481-0800: 0.490: [GC pause (G1 Humongous Allocation) (young) (initial-mark) 287M->287M(512M), 0.0018798 secs]
2020-10-28T01:05:37.483-0800: 0.492: [GC concurrent-root-region-scan-start]
2020-10-28T01:05:37.483-0800: 0.492: [GC concurrent-root-region-scan-end, 0.0001332 secs]
2020-10-28T01:05:37.483-0800: 0.492: [GC concurrent-mark-start]
2020-10-28T01:05:37.484-0800: 0.494: [GC concurrent-mark-end, 0.0012830 secs]
2020-10-28T01:05:37.485-0800: 0.494: [GC remark, 0.0017687 secs]
2020-10-28T01:05:37.487-0800: 0.496: [GC cleanup 294M->294M(512M), 0.0008516 secs]
2020-10-28T01:05:37.506-0800: 0.515: [GC pause (G1 Evacuation Pause) (young) 397M->320M(512M), 0.0044232 secs]
2020-10-28T01:05:37.514-0800: 0.523: [GC pause (G1 Evacuation Pause) (mixed) 336M->297M(512M), 0.0053885 secs]
2020-10-28T01:05:37.520-0800: 0.530: [GC pause (G1 Humongous Allocation) (young) (initial-mark) 302M->298M(512M), 0.0015211 secs]
2020-10-28T01:05:37.522-0800: 0.531: [GC concurrent-root-region-scan-start]
2020-10-28T01:05:37.522-0800: 0.531: [GC concurrent-root-region-scan-end, 0.0001334 secs]
2020-10-28T01:05:37.522-0800: 0.531: [GC concurrent-mark-start]
2020-10-28T01:05:37.523-0800: 0.532: [GC concurrent-mark-end, 0.0011341 secs]
2020-10-28T01:05:37.523-0800: 0.533: [GC remark, 0.0015379 secs]
2020-10-28T01:05:37.525-0800: 0.534: [GC cleanup 307M->306M(512M), 0.0007813 secs]
2020-10-28T01:05:37.526-0800: 0.535: [GC concurrent-cleanup-start]
2020-10-28T01:05:37.526-0800: 0.535: [GC concurrent-cleanup-end, 0.0000316 secs]
2020-10-28T01:05:37.542-0800: 0.551: [GC pause (G1 Evacuation Pause) (young) 388M->321M(512M), 0.0043561 secs]
2020-10-28T01:05:37.549-0800: 0.558: [GC pause (G1 Evacuation Pause) (mixed) 338M->299M(512M), 0.0062345 secs]
2020-10-28T01:05:37.556-0800: 0.565: [GC pause (G1 Humongous Allocation) (young) (initial-mark) 305M->300M(512M), 0.0015171 secs]
2020-10-28T01:05:37.558-0800: 0.567: [GC concurrent-root-region-scan-start]
2020-10-28T01:05:37.558-0800: 0.567: [GC concurrent-root-region-scan-end, 0.0001160 secs]
2020-10-28T01:05:37.558-0800: 0.567: [GC concurrent-mark-start]
2020-10-28T01:05:37.559-0800: 0.568: [GC concurrent-mark-end, 0.0010559 secs]
2020-10-28T01:05:37.559-0800: 0.568: [GC remark, 0.0013377 secs]
2020-10-28T01:05:37.560-0800: 0.569: [GC cleanup 308M->308M(512M), 0.0006723 secs]
2020-10-28T01:05:37.578-0800: 0.587: [GC pause (G1 Evacuation Pause) (young) 407M->330M(512M), 0.0040970 secs]
2020-10-28T01:05:37.585-0800: 0.594: [GC pause (G1 Evacuation Pause) (mixed) 344M->317M(512M), 0.0055920 secs]
2020-10-28T01:05:37.591-0800: 0.600: [GC pause (G1 Humongous Allocation) (young) (initial-mark) 319M->317M(512M), 0.0012650 secs]
2020-10-28T01:05:37.593-0800: 0.602: [GC concurrent-root-region-scan-start]
2020-10-28T01:05:37.593-0800: 0.602: [GC concurrent-root-region-scan-end, 0.0001160 secs]
2020-10-28T01:05:37.593-0800: 0.602: [GC concurrent-mark-start]
2020-10-28T01:05:37.594-0800: 0.603: [GC concurrent-mark-end, 0.0010223 secs]
2020-10-28T01:05:37.594-0800: 0.603: [GC remark, 0.0016071 secs]
2020-10-28T01:05:37.596-0800: 0.605: [GC cleanup 323M->323M(512M), 0.0006779 secs]
2020-10-28T01:05:37.613-0800: 0.622: [GC pause (G1 Evacuation Pause) (young) 411M->342M(512M), 0.0036588 secs]
2020-10-28T01:05:37.620-0800: 0.629: [GC pause (G1 Evacuation Pause) (mixed) 361M->331M(512M), 0.0070543 secs]
2020-10-28T01:05:37.628-0800: 0.637: [GC pause (G1 Humongous Allocation) (young) (initial-mark) 334M->331M(512M), 0.0017256 secs]
2020-10-28T01:05:37.630-0800: 0.639: [GC concurrent-root-region-scan-start]
2020-10-28T01:05:37.630-0800: 0.639: [GC concurrent-root-region-scan-end, 0.0000906 secs]
2020-10-28T01:05:37.630-0800: 0.639: [GC concurrent-mark-start]
2020-10-28T01:05:37.631-0800: 0.640: [GC concurrent-mark-end, 0.0010883 secs]
2020-10-28T01:05:37.632-0800: 0.641: [GC remark, 0.0017730 secs]
2020-10-28T01:05:37.633-0800: 0.642: [GC cleanup 339M->339M(512M), 0.0006846 secs]
2020-10-28T01:05:37.647-0800: 0.656: [GC pause (G1 Evacuation Pause) (young) 404M->350M(512M), 0.0044909 secs]
2020-10-28T01:05:37.656-0800: 0.665: [GC pause (G1 Evacuation Pause) (mixed) 372M->334M(512M), 0.0063609 secs]
2020-10-28T01:05:37.663-0800: 0.672: [GC pause (G1 Humongous Allocation) (young) (initial-mark) 336M->334M(512M), 0.0020582 secs]
2020-10-28T01:05:37.665-0800: 0.674: [GC concurrent-root-region-scan-start]
2020-10-28T01:05:37.665-0800: 0.675: [GC concurrent-root-region-scan-end, 0.0001155 secs]
2020-10-28T01:05:37.665-0800: 0.675: [GC concurrent-mark-start]
2020-10-28T01:05:37.667-0800: 0.676: [GC concurrent-mark-end, 0.0010411 secs]
2020-10-28T01:05:37.667-0800: 0.676: [GC remark, 0.0024688 secs]
2020-10-28T01:05:37.669-0800: 0.678: [GC cleanup 339M->339M(512M), 0.0007628 secs]
2020-10-28T01:05:37.682-0800: 0.691: [GC pause (G1 Evacuation Pause) (young) 400M->350M(512M), 0.0059008 secs]
2020-10-28T01:05:37.692-0800: 0.701: [GC pause (G1 Evacuation Pause) (mixed) 372M->337M(512M), 0.0074892 secs]
2020-10-28T01:05:37.700-0800: 0.709: [GC pause (G1 Humongous Allocation) (young) (initial-mark) 339M->337M(512M), 0.0031392 secs]
2020-10-28T01:05:37.704-0800: 0.713: [GC concurrent-root-region-scan-start]
2020-10-28T01:05:37.704-0800: 0.713: [GC concurrent-root-region-scan-end, 0.0001508 secs]
2020-10-28T01:05:37.704-0800: 0.713: [GC concurrent-mark-start]
2020-10-28T01:05:37.705-0800: 0.714: [GC concurrent-mark-end, 0.0012051 secs]
2020-10-28T01:05:37.705-0800: 0.714: [GC remark, 0.0026838 secs]
2020-10-28T01:05:37.708-0800: 0.717: [GC cleanup 343M->343M(512M), 0.0006746 secs]
2020-10-28T01:05:37.720-0800: 0.729: [GC pause (G1 Evacuation Pause) (young) 396M->355M(512M), 0.0056083 secs]
2020-10-28T01:05:37.730-0800: 0.739: [GC pause (G1 Evacuation Pause) (mixed) 377M->343M(512M), 0.0071801 secs]
2020-10-28T01:05:37.737-0800: 0.746: [GC pause (G1 Humongous Allocation) (young) (initial-mark) 344M->343M(512M), 0.0032935 secs]
2020-10-28T01:05:37.741-0800: 0.750: [GC concurrent-root-region-scan-start]
2020-10-28T01:05:37.741-0800: 0.750: [GC concurrent-root-region-scan-end, 0.0000381 secs]
2020-10-28T01:05:37.741-0800: 0.750: [GC concurrent-mark-start]
2020-10-28T01:05:37.742-0800: 0.751: [GC concurrent-mark-end, 0.0015984 secs]
2020-10-28T01:05:37.742-0800: 0.751: [GC remark, 0.0024936 secs]
2020-10-28T01:05:37.745-0800: 0.754: [GC cleanup 349M->349M(512M), 0.0008372 secs]
2020-10-28T01:05:37.757-0800: 0.766: [GC pause (G1 Evacuation Pause) (young) 404M->357M(512M), 0.0050666 secs]
2020-10-28T01:05:37.767-0800: 0.776: [GC pause (G1 Evacuation Pause) (mixed) 379M->349M(512M), 0.0088634 secs]
2020-10-28T01:05:37.776-0800: 0.785: [GC pause (G1 Humongous Allocation) (young) (initial-mark) 350M->349M(512M), 0.0028555 secs]
2020-10-28T01:05:37.779-0800: 0.788: [GC concurrent-root-region-scan-start]
2020-10-28T01:05:37.779-0800: 0.788: [GC concurrent-root-region-scan-end, 0.0000373 secs]
2020-10-28T01:05:37.779-0800: 0.788: [GC concurrent-mark-start]
2020-10-28T01:05:37.780-0800: 0.789: [GC concurrent-mark-end, 0.0015091 secs]
2020-10-28T01:05:37.780-0800: 0.789: [GC remark, 0.0027533 secs]
2020-10-28T01:05:37.783-0800: 0.792: [GC cleanup 355M->355M(512M), 0.0007030 secs]
2020-10-28T01:05:37.798-0800: 0.807: [GC pause (G1 Evacuation Pause) (young) 411M->366M(512M), 0.0044417 secs]
2020-10-28T01:05:37.808-0800: 0.817: [GC pause (G1 Evacuation Pause) (mixed) 389M->355M(512M), 0.0069581 secs]
2020-10-28T01:05:37.815-0800: 0.825: [GC pause (G1 Humongous Allocation) (young) (initial-mark) 356M->355M(512M), 0.0027222 secs]
2020-10-28T01:05:37.818-0800: 0.827: [GC concurrent-root-region-scan-start]
2020-10-28T01:05:37.818-0800: 0.827: [GC concurrent-root-region-scan-end, 0.0000611 secs]
2020-10-28T01:05:37.818-0800: 0.827: [GC concurrent-mark-start]
2020-10-28T01:05:37.820-0800: 0.829: [GC concurrent-mark-end, 0.0012197 secs]
2020-10-28T01:05:37.820-0800: 0.829: [GC remark, 0.0023367 secs]
2020-10-28T01:05:37.822-0800: 0.831: [GC cleanup 361M->361M(512M), 0.0007874 secs]
2020-10-28T01:05:37.833-0800: 0.842: [GC pause (G1 Evacuation Pause) (young) 409M->369M(512M), 0.0052421 secs]
2020-10-28T01:05:37.842-0800: 0.852: [GC pause (G1 Evacuation Pause) (mixed) 395M->356M(512M), 0.0085514 secs]
2020-10-28T01:05:37.856-0800: 0.865: [GC pause (G1 Evacuation Pause) (mixed) 382M->361M(512M), 0.0039514 secs]
2020-10-28T01:05:37.861-0800: 0.870: [GC pause (G1 Humongous Allocation) (young) (initial-mark) 362M->362M(512M), 0.0028763 secs]
2020-10-28T01:05:37.863-0800: 0.873: [GC concurrent-root-region-scan-start]
2020-10-28T01:05:37.864-0800: 0.873: [GC concurrent-root-region-scan-end, 0.0000610 secs]
2020-10-28T01:05:37.864-0800: 0.873: [GC concurrent-mark-start]
2020-10-28T01:05:37.865-0800: 0.874: [GC concurrent-mark-end, 0.0012261 secs]
2020-10-28T01:05:37.865-0800: 0.874: [GC remark, 0.0025168 secs]
2020-10-28T01:05:37.868-0800: 0.877: [GC cleanup 367M->367M(512M), 0.0006827 secs]
2020-10-28T01:05:37.876-0800: 0.885: [GC pause (G1 Evacuation Pause) (young) 404M->378M(512M), 0.0040536 secs]
2020-10-28T01:05:37.884-0800: 0.893: [GC pause (G1 Evacuation Pause) (mixed) 402M->366M(512M), 0.0076111 secs]
2020-10-28T01:05:37.896-0800: 0.905: [GC pause (G1 Evacuation Pause) (mixed) 391M->370M(512M), 0.0040490 secs]
2020-10-28T01:05:37.902-0800: 0.911: [GC pause (G1 Humongous Allocation) (young) (initial-mark) 376M->373M(512M), 0.0036431 secs]
2020-10-28T01:05:37.906-0800: 0.915: [GC concurrent-root-region-scan-start]
2020-10-28T01:05:37.906-0800: 0.915: [GC concurrent-root-region-scan-end, 0.0001516 secs]
2020-10-28T01:05:37.906-0800: 0.915: [GC concurrent-mark-start]
2020-10-28T01:05:37.907-0800: 0.916: [GC concurrent-mark-end, 0.0014910 secs]
2020-10-28T01:05:37.907-0800: 0.916: [GC remark, 0.0024351 secs]
2020-10-28T01:05:37.910-0800: 0.919: [GC cleanup 381M->381M(512M), 0.0008143 secs]
2020-10-28T01:05:37.914-0800: 0.923: [GC pause (G1 Evacuation Pause) (young) 402M->384M(512M), 0.0040275 secs]
2020-10-28T01:05:37.924-0800: 0.933: [GC pause (G1 Evacuation Pause) (mixed)-- 408M->373M(512M), 0.0074215 secs]
2020-10-28T01:05:37.937-0800: 0.946: [GC pause (G1 Evacuation Pause) (mixed) 397M->375M(512M), 0.0041325 secs]
2020-10-28T01:05:37.941-0800: 0.950: [GC pause (G1 Humongous Allocation) (young) (initial-mark) 377M->374M(512M), 0.0027603 secs]
2020-10-28T01:05:37.944-0800: 0.953: [GC concurrent-root-region-scan-start]
2020-10-28T01:05:37.944-0800: 0.953: [GC concurrent-root-region-scan-end, 0.0001135 secs]
2020-10-28T01:05:37.944-0800: 0.953: [GC concurrent-mark-start]
2020-10-28T01:05:37.945-0800: 0.954: [GC concurrent-mark-end, 0.0010866 secs]
2020-10-28T01:05:37.945-0800: 0.954: [GC remark, 0.0022799 secs]
2020-10-28T01:05:37.948-0800: 0.957: [GC cleanup 381M->381M(512M), 0.0007336 secs]
2020-10-28T01:05:37.953-0800: 0.962: [GC pause (G1 Evacuation Pause) (young) 403M->384M(512M), 0.0039379 secs]
2020-10-28T01:05:37.962-0800: 0.971: [GC pause (G1 Evacuation Pause) (mixed)-- 412M->390M(512M), 0.0091039 secs]
2020-10-28T01:05:37.976-0800: 0.985: [GC pause (G1 Evacuation Pause) (mixed) 415M->392M(512M), 0.0051491 secs]
2020-10-28T01:05:37.982-0800: 0.991: [GC pause (G1 Humongous Allocation) (young) (initial-mark) 394M->392M(512M), 0.0031252 secs]
2020-10-28T01:05:37.985-0800: 0.994: [GC concurrent-root-region-scan-start]
2020-10-28T01:05:37.985-0800: 0.994: [GC concurrent-root-region-scan-end, 0.0000539 secs]
2020-10-28T01:05:37.985-0800: 0.994: [GC concurrent-mark-start]
2020-10-28T01:05:37.987-0800: 0.996: [GC concurrent-mark-end, 0.0022201 secs]
2020-10-28T01:05:37.987-0800: 0.996: [GC remark, 0.0027732 secs]
2020-10-28T01:05:37.990-0800: 0.999: [GC cleanup 397M->396M(512M), 0.0008119 secs]
2020-10-28T01:05:37.991-0800: 1.000: [GC concurrent-cleanup-start]
2020-10-28T01:05:37.991-0800: 1.000: [GC concurrent-cleanup-end, 0.0000115 secs]
2020-10-28T01:05:37.996-0800: 1.005: [GC pause (G1 Evacuation Pause) (young) 421M->401M(512M), 0.0040992 secs]
2020-10-28T01:05:38.006-0800: 1.015: [GC pause (G1 Evacuation Pause) (mixed)-- 426M->396M(512M), 0.0060858 secs]
2020-10-28T01:05:38.017-0800: 1.026: [GC pause (G1 Evacuation Pause) (mixed) 421M->399M(512M), 0.0048446 secs]
2020-10-28T01:05:38.022-0800: 1.031: [GC pause (G1 Humongous Allocation) (young) (initial-mark) 400M->400M(512M), 0.0029429 secs]
2020-10-28T01:05:38.025-0800: 1.034: [GC concurrent-root-region-scan-start]
2020-10-28T01:05:38.025-0800: 1.034: [GC concurrent-root-region-scan-end, 0.0001030 secs]
2020-10-28T01:05:38.025-0800: 1.034: [GC concurrent-mark-start]
2020-10-28T01:05:38.026-0800: 1.035: [GC concurrent-mark-end, 0.0013401 secs]
2020-10-28T01:05:38.027-0800: 1.036: [GC remark, 0.0026950 secs]
2020-10-28T01:05:38.029-0800: 1.038: [GC cleanup 407M->407M(512M), 0.0008073 secs]
2020-10-28T01:05:38.035-0800: 1.044: [GC pause (G1 Evacuation Pause) (young) 431M->406M(512M), 0.0037502 secs]
2020-10-28T01:05:38.044-0800: 1.053: [GC pause (G1 Evacuation Pause) (mixed)-- 435M->417M(512M), 0.0058935 secs]
2020-10-28T01:05:38.056-0800: 1.065: [GC pause (G1 Evacuation Pause) (mixed)-- 446M->441M(512M), 0.0046661 secs]
2020-10-28T01:05:38.062-0800: 1.071: [GC pause (G1 Humongous Allocation) (young) (initial-mark) 444M->442M(512M), 0.0037367 secs]
2020-10-28T01:05:38.066-0800: 1.075: [GC concurrent-root-region-scan-start]
2020-10-28T01:05:38.066-0800: 1.075: [GC concurrent-root-region-scan-end, 0.0001553 secs]
2020-10-28T01:05:38.066-0800: 1.075: [GC concurrent-mark-start]
2020-10-28T01:05:38.067-0800: 1.077: [GC concurrent-mark-end, 0.0016403 secs]
2020-10-28T01:05:38.068-0800: 1.077: [GC remark, 0.0032053 secs]
2020-10-28T01:05:38.071-0800: 1.080: [GC cleanup 449M->448M(512M), 0.0009397 secs]
2020-10-28T01:05:38.072-0800: 1.081: [GC concurrent-cleanup-start]
2020-10-28T01:05:38.072-0800: 1.081: [GC concurrent-cleanup-end, 0.0000165 secs]
2020-10-28T01:05:38.073-0800: 1.082: [GC pause (G1 Humongous Allocation) (young)-- 451M->446M(512M), 0.0031337 secs]

`
