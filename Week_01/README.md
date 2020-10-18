学习笔记
===

![JVM 进阶梳理](https://github.com/xhwSkhizein/JAVA-000/raw/main/Week_01/static/JVM%E8%BF%9B%E9%98%B6.png)

---

作业
===

#### 1. 自定义类加载器加载 Hello.xlass 并执行其hello方法

  执行方法
  
```bash
 cd Week_01/
 javac src/HelloCustomClassLoader.java
 javap java -cp ./src/ HelloCustomClassLoader
```

#### 2. 画一张图，展示 Xmx、Xms、Xmn、Meta、DirectMemory、Xss 这些内存参数的 关系
 

![JVM Memory](https://raw.githubusercontent.com/xhwSkhizein/JAVA-000/main/Week_01/static/jvm_memory.png)