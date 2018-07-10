import com.sun.mail.util.MailSSLSocketFactory;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.sql.Array;
import java.text.SimpleDateFormat;
import java.util.*;
import java.security.GeneralSecurityException;
import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.net.ssl.HttpsURLConnection;

import com.sun.mail.util.MailSSLSocketFactory;
import com.sun.org.apache.bcel.internal.classfile.Constant;

/**
 * 文件名称为：URLAvailability.java
 * 文件功能简述： 描述一个URL地址是否有效
 *
 * @author maoxj
 * @time 2018-7-8
 */
public class URLAvailability {
    private static URL url;
    private static HttpURLConnection con;
    private static HttpsURLConnection cons;
    //private static Socket socket=new Socket();
    private static int state = -1;

    /**
     * 功能：检测当前URL是否可连接或是否有效,
     * 描述：最多连接网络 5 次, 如果 5 次都不成功，视为该地址不可用
     *
     * @param urlStr 指定URL网络地址
     * @return URL
     */
    public String isConnect(String urlStr) {
        int counts = 0;
        boolean returnflag=false;

        if (urlStr == null || urlStr.length() <= 0) {
            return "0";
        }
        String tempUrl=urlStr.substring(0, 5);//取出地址前5位
        String[] addr=urlStr.split(":");//地址和端口分离
        String returnMsg=null;
        while (counts < 5) {
            try {

                if(tempUrl.contains("http")){//判断传过来的地址中是否有http
                    if(tempUrl.equals("https")){//判断服务器是否是https协议
                        returnflag=httpsIsConnect(urlStr);
                    }else{
                        returnflag=httpIsConnect(urlStr);
                    }

                }else{//传过来的是IP地址

                    if(addr.length<2){
                        returnflag=socketIsConnect(addr[0],80);
                    }else{
                        returnflag=socketIsConnect(addr[0],Integer.valueOf(addr[1]) );
                    }


                }


                if(returnflag){
                    break;
                }else{
                    counts++;
                    System.out.println(urlStr+" URL不可用，连接第 " + counts + " 次");
                }

            } catch (Exception ex) {
                counts++;
                System.out.println(urlStr+" URL不可用，连接第 " + counts + " 次");
                //urlStr = null;
                continue;
            }
        }
    if(counts>3){
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
        returnMsg=urlStr+" 连接失败 "+counts+"/5 "+df.format(new Date());

    }else{
            return returnMsg="0";
    }

        return returnMsg;
    }
    public  boolean httpIsConnect(String urlStr){

        try{
            url = new URL(urlStr);
            con = (HttpURLConnection) url.openConnection();
            con.setConnectTimeout(5000);   //超过5秒就连接超时了
            //con.setRequestMethod("GET");    //使用的http的get方法
            state = con.getResponseCode();

           // System.out.println(counts + "= " + state);
            if (state == 200) {
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
                System.out.println(urlStr+" URL可用！"+df.format(new Date()));
                return true;
            }else{
                return false;
            }
        }catch (Exception ex) {
            //System.out.println("fail");
            //e.printStackTrace();
            return false;
        }finally{
            try{
                con.disconnect();
            }catch (Exception e) {
                e.printStackTrace();
            }
        }


    }

    public  boolean httpsIsConnect(String urlStr){

        try{
            url = new URL(urlStr);
            cons = (HttpsURLConnection) url.openConnection();
            cons.setConnectTimeout(5000);   //超过5秒就连接超时了
            //cons.setRequestMethod("GET");    //使用的http的get方法
            state = cons.getResponseCode();

            // System.out.println(counts + "= " + state);
            if (state == 200) {
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
                System.out.println(urlStr+" URL可用！"+df.format(new Date()));
                return true;
            }else{
                return false;
            }
        }catch (Exception ex) {
            //System.out.println("fail");
            //e.printStackTrace();
            return false;
        }finally{
            try{
                cons.disconnect();
            }catch (Exception e) {
                e.printStackTrace();
            }
        }


    }
    public  boolean socketIsConnect(String host,int port){
        Socket socket = new Socket();
        try{
            //socket.setSoTimeout(1000);
            socket.connect(new InetSocketAddress(host, port),3000);//超时时间为3秒
        }catch (IOException e) {
            System.out.println("fail "+e.getMessage());
            //e.printStackTrace();
            return false;
        }finally{
            try{
                if(socket!=null){
                socket.close();
                    //socket.isConnected();
                }
            }catch (IOException e) {
                e.printStackTrace();
            }
        }
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
        System.out.println(host+":"+port+" URL可用！"+df.format(new Date()));
        return true;

    }

    public static void main(String[] args) throws GeneralSecurityException, IOException, InterruptedException {
        URLAvailability u = new URLAvailability();
        String outMsg="0";

        List<TextDTO> list=readFile( System.getProperty("user.dir").toString()+"/conf/serviceList.conf","UTF-8");
        List<TextDTO> list2=readFile( System.getProperty("user.dir").toString()+"/conf/EmailUserList.conf","UTF-8");

        while(true){
        for(int i=0;i<list.size();i++){
            String ss=u.isConnect(list.get(i).getCodeValue());
         if( !ss.equals("0")){
             outMsg=list.get(i).getCodeName()+" "+ss+"\n"+outMsg;
         };
       }
       if(!outMsg.equals("0")) {

           for (int i = 0; i < list2.size(); i++) {
               u.sendEmail(list2.get(i).getCodeName() + ":" + outMsg, list2.get(i).getCodeValue());
           }
           outMsg="0";
           System.out.println("发送邮件后休眠");
           Thread.sleep(600000);

       }
            System.out.println("普通休眠");
            Thread.sleep(60000);

        }



//        File directory = new File("");//设定为当前文件夹
//        try{
//            System.out.println(directory.getCanonicalPath());//获取标准的路径
//            System.out.println(directory.getAbsolutePath());//获取绝对路径
//            System.out.println(System.getProperty("user.dir"));//得到工程的路径
//            System.out.println(Class.class.getClass().getResource("/").getPath() );//类的绝对路径
//        }catch(IOException e) {
//            e.printStackTrace();
//        }
    }

    /**
     *
     * @param msg 发送信息
     * @param reUser 收件人
     * @throws GeneralSecurityException
     */
    public void sendEmail(String msg,String reUser) throws GeneralSecurityException {
        // 收件人电子邮箱
        String to = "281898533@qq.com";
        to=reUser;
        // 发件人电子邮箱
        String from = "systemreporter@163.com";

        // 指定发送邮件的主机为 smtp.qq.com
        String host = "smtp.163.com";  //QQ 邮件服务器

        // 获取系统属性
        Properties properties = System.getProperties();

        // 设置邮件服务器
        properties.setProperty("mail.smtp.host", host);
        properties.put("mail.smtp.auth", "true");
        MailSSLSocketFactory sf = new MailSSLSocketFactory();
        sf.setTrustAllHosts(true);
        properties.put("mail.smtp.ssl.enable", "true");
        properties.put("mail.smtp.ssl.socketFactory", sf);
        // 获取默认session对象
        Session session = Session.getDefaultInstance(properties,new javax.mail.Authenticator(){
            public PasswordAuthentication getPasswordAuthentication()
            {
                return new PasswordAuthentication("systemreporter@163.com", "abcd1234"); //发件人邮件用户名、密码
            }
        });

        try{
            // 创建默认的 MimeMessage 对象
            MimeMessage message = new MimeMessage(session);

            // Set From: 头部头字段
            message.setFrom(new InternetAddress(from));

            // Set To: 头部头字段
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

            // Set Subject: 头部头字段
            message.setSubject("来自淳安系统监控服务");

            // 设置消息体
            message.setText(msg);

            // 发送消息
            Transport.send(message);
            System.out.println("Sent message successfully...."+reUser);
        }catch (MessagingException mex) {
            mex.printStackTrace();
        }
    }

    /**
     *
     * @param filepath
     * @param charSet UTF-8
     * @return
     * @throws IOException
     */
    public static List<TextDTO> readFile(String filepath, String charSet) throws IOException {
        InputStreamReader read = null;
        BufferedReader bufferedReader = null;
        List<TextDTO> list= new ArrayList();

        try {
            read = new InputStreamReader(new FileInputStream(filepath), charSet);
            bufferedReader = new BufferedReader(read);
            String lineTxt = null;
            StringBuffer buffer = new StringBuffer();
            String[] infileStr =null;
            while ((lineTxt = bufferedReader.readLine()) != null) {
                TextDTO dto =new TextDTO();
                if(lineTxt.startsWith("\uFEFF")) {
                    lineTxt = lineTxt.replace("\uFEFF", "");
                    //在Windows下用文本编辑器创建的文本文件，如果选择以UTF-8等Unicode格式保存，会在文件头加入一个BOM标识
                    //Java在读取Unicode文件的时候，会统一把BOM变成“\uFEFF”
                }
                if(lineTxt.startsWith("#")) {
                    continue;
                }
                if(lineTxt.length()<2) {
                    continue;
                }
                infileStr=lineTxt.split("~");
                if(infileStr.length<2){
                    dto.setCodeValue(infileStr[0]);
                    dto.setCodeName("空");
                }else
                {
                    dto.setCodeValue(infileStr[0]);
                    dto.setCodeName(infileStr[1]);

                }
                list.add(dto);
            }
            return list;
        } catch (Exception e) {
            throw e;
        } finally {
            if (bufferedReader != null)
                bufferedReader.close();
            if (read != null) {
                read.close();
            }
        }
    }

}