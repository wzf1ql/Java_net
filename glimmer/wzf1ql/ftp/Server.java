package glimmer.wzf1ql.ftp;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Server extends Thread
{
    private int port;
    private ServerSocket serverSocket1;
    private Scanner scanner = new Scanner(System.in);
    private Socket client1;
    private OutputStream o1;
    private InputStream i1;
    private byte[] bytes1 = new byte[1024];
    private boolean connected1 = false;
    private String workingDirectory = "D:\\";
    //private byte[] b_GET = {'\\','G','E','T'};
    private byte[] b_over = {'\\','O','V','E','R'};

    public Server(int port) throws IOException
    {
        this.port = port;
        serverSocket1 = new ServerSocket(port);
        start();
    }

    //get命令用的方法
    private int Download(File f) throws IOException
    {

        InputStream ind = new FileInputStream(f);
        ServerSocket sd;

        int i;
        byte[] bytes = new byte[Math.toIntExact(f.length())];
        for(i=1;;i++)//检查一个未被占用的端口
        {
            try
            {
                new Socket(String.valueOf(InetAddress.getLocalHost().getHostAddress()), port + i);
            } catch (IOException ignore)
            {
                sd = new ServerSocket(port+i);
                System.out.println("打开数据端口："+(port+i)+"等待连接中…");
                break;
            }
        }
        Runnable tDownload = () ->
        {
            try
            {
                Socket cd;
                cd = sd.accept();
                System.out.println("客户端连接成功，传输文件" + f.getName());
                OutputStream oud = cd.getOutputStream();
                ind.read(bytes);
                oud.write(bytes);
                System.out.println(bytes[0]);
                ind.close();
                oud.close();
                cd.close();
                System.out.println("传输成功，Socket已关闭");
            } catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        };
        new Thread(tDownload).start();
        return port+i;
    }
    //put命令用的方法
    public int Upload(File f) throws IOException
    {
        OutputStream ouu = new FileOutputStream(f);
        ServerSocket sd;

        byte[] b_len = new byte[4];
        int i;
        for(i=1;;i++)//检查一个未被占用的端口
        {
            try
            {
                new Socket(String.valueOf(InetAddress.getLocalHost().getHostAddress()), port + i);
            } catch (IOException ignore)
            {
                sd = new ServerSocket(port+i);
                System.out.println("打开数据端口："+(port+i)+"等待连接中…");
                break;
            }
        }
        Runnable tUpload = ()->
        {
            try
            {
                Socket cd;
                cd = sd.accept();
                System.out.println("客户端连接成功，等待客户端上传文件…");
                InputStream inu = cd.getInputStream();
                inu.read(b_len, 0, 4);
                int len = DealByte.Byte2Int(b_len);//先读取文件长度
                byte[] bytes = new byte[len];
                inu.read(bytes);
                ouu.write(bytes);
                cd.close();
                System.out.println("传输成功，Socket已关闭");
            } catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        };
        new Thread(tUpload).start();
        return port+i;
    }


    public void run()
    {
        try
        {
            while(true)
            {
                if(!connected1)
                {
                    System.out.println("等待连接，端口号：" + serverSocket1.getLocalPort());
                    client1 = serverSocket1.accept();//监听客户端，没有接收到数据会停在此处
                    System.out.println("远程主机地址：" + client1.getRemoteSocketAddress());
                    o1 = client1.getOutputStream();
                    i1 = client1.getInputStream();
                    connected1 = true;
                    continue;
                }
                bytes1 = new byte[1024];//使用新的数组防止上一次残留的数据误判
                int length = i1.read(bytes1);
                String read = new String(bytes1, 0, length, Charset.defaultCharset());
                System.out.println(read);
                if(DealByte.compare(bytes1,"qwd"))
                {
                    o1.write(workingDirectory.getBytes(StandardCharsets.UTF_8));
                    i1.read();//每次输出后阻塞至客户端发回确认信息
                    o1.write(b_over);
                    i1.read();
                }
                else if(DealByte.compare(bytes1, "cd "))
                {
                    workingDirectory = new String(bytes1, 3, length-3, Charset.defaultCharset());
                    o1.write(("工作目录改为:"+workingDirectory).getBytes(StandardCharsets.UTF_8));
                    i1.read();
                    o1.write(b_over);
                    i1.read();
                }
                else if(DealByte.compare(bytes1, "ls"))
                {
                    File[] fileList = new File(workingDirectory).listFiles();
                    for(File f : fileList)
                    {
                        if(f.isFile())
                        {
                            o1.write(f.getPath().getBytes(StandardCharsets.UTF_8));
                            i1.read();
                        }
                    }
                    o1.write(b_over);
                    i1.read();
                }
                else if(DealByte.compare(bytes1, "get "))
                {
                    File f = new File(workingDirectory + new String(bytes1, 4, length-4, Charset.defaultCharset()));
                    o1.write( ("\\GET" + (Download(f)) + '_' + Math.toIntExact(f.length()) + '\\' + f.getName()) .getBytes(StandardCharsets.UTF_8));
                    i1.read();
                    o1.write(b_over);
                    i1.read();
                }
                else if(DealByte.compare(bytes1, "put "))
                {
                    File f = new File(workingDirectory + DealByte.divideStr(bytes1, (byte) '\\'));
                    System.out.println(f.getPath());
                    o1.write( ("\\PUT" + (Upload(f)) ).getBytes(StandardCharsets.UTF_8) );
                    i1.read();
                    o1.write(b_over);
                    i1.read();
                }
                else if(DealByte.compare(bytes1, "quit"))
                {
                    o1.write( ("\\QUIT").getBytes(StandardCharsets.UTF_8) );
                    i1.read();
                    serverSocket1.close();
                    break;
                }
                else if(DealByte.compare(bytes1, "mkdir "))
                {
                    String s_dir = workingDirectory + new String(bytes1, 6, length-6, Charset.defaultCharset());
                    File dir = new File(s_dir);
                    if(dir.mkdirs())
                    {
                        o1.write(("成功建立目录：" + s_dir).getBytes(StandardCharsets.UTF_8));
                        i1.read();
                    }
                    else
                    {
                        o1.write(("建立失败").getBytes(StandardCharsets.UTF_8));
                        i1.read();
                    }
                    o1.write(b_over);
                    i1.read();
                }
                else if(DealByte.compare(bytes1, "delete "))
                {
                    String s_dir = workingDirectory + new String(bytes1, 7, length-7, Charset.defaultCharset());
                    File del = new File(s_dir);
                    if(del.delete())
                    {
                        o1.write(("成功删除文件：" + s_dir).getBytes(StandardCharsets.UTF_8));
                        i1.read();
                    }
                    else
                    {
                        o1.write(("删除失败").getBytes(StandardCharsets.UTF_8));
                        i1.read();
                    }
                    o1.write(b_over);
                    i1.read();
                }
                else if(DealByte.compare(bytes1, "help"))
                {
                    o1.write(("pwd:显示远程工作目录\n" +
                            "cd:改变远程工作目录为您输入的绝对路径\n" +
                            "ls:显示远程工作目录包含的文件名\n" +
                            "get:下载工作目录下的指定文件\n" +
                            "put:上传指定文件，请输入文件的绝对路径\n" +
                            "mkdir:在工作目录下创建目录\n" +
                            "delete:删除工作目录下的单个文件\n" +
                            "help:显示此说明\n" +
                            "mput:使用通配符上传多个文件\n" +
                            "mget:使用通配符下载多个文件\n" +
                            "quit:关闭连接并退出程序").getBytes(StandardCharsets.UTF_8));
                    i1.read();
                    o1.write(b_over);
                    i1.read();
                }
                else if(DealByte.compare(bytes1, "mget "))
                {
                    String name = new String(bytes1, 5, length-5, Charset.defaultCharset());
                    File[] fileList = new File(workingDirectory).listFiles();
                    for(File f : fileList)
                    {
                        if(f.isFile() && Wildcard.wildcard(f, name))
                        {
                            o1.write( ("\\GET" + (Download(f)) + '_' + Math.toIntExact(f.length()) + '\\' + f.getName()) .getBytes(StandardCharsets.UTF_8));
                            i1.read();
                            sleep(10);//不知道为嘛，加上这句就能解决传输多个文件时第一个传不过去的问题
                        }
                    }
                    o1.write(b_over);
                    i1.read();
                }
                else if(DealByte.compare(bytes1, "mput "))
                {
                    o1.write(("\\MPUT".getBytes(StandardCharsets.UTF_8)));
                    i1.read();
                    byte[] b_read = new byte[100];
                    while(true)
                    {
                        i1.read(b_read);
                        if(DealByte.compare(b_read,"\\PUT"))
                        {
                            File f = new File(workingDirectory + '\\' + DealByte.divideStr(b_read, (byte)'\\' ));
                            o1.write( ("\\" + Upload(f)).getBytes(StandardCharsets.UTF_8) );
                        }
                        else break;
                    }
                }
                else
                {
                    o1.write(("err " + DealByte.divideStr(bytes1, 0, (byte)' ') + "不是正确的命令").getBytes(StandardCharsets.UTF_8));
                    i1.read();
                    o1.write(b_over);
                    i1.read();
                }
            }
        } catch (IOException e)
        {
            throw new RuntimeException(e);
        } catch (InterruptedException e)
        {
            throw new RuntimeException(e);
        }
    };

    public static void main(String [] args) throws IOException
    {
        Server server = new Server(8000);
    }
}
