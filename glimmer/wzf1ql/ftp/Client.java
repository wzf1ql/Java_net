package glimmer.wzf1ql.ftp;

import java.io.*;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

public class Client extends Thread
{
    private Scanner scanner = new Scanner(System.in);
    private InputStream i1;
    private OutputStream o1;
    private String command;
    private byte[] feedback = new byte[1024];
    private String address;
    private CountDownLatch latch = new CountDownLatch(0);
    private Socket socket1;
    private int threadNumber = 0;
    public Client(int port, String address) throws IOException
    {
        this.address = address;
        socket1 = new Socket(address, port);
        i1 = socket1.getInputStream();
        o1 = socket1.getOutputStream();
        start();
    }
    private void Download(File f, int port, int length) throws IOException
    {
        Runnable tDownload = ()->
        {
            try
            {
                Socket skt;
                skt = new Socket(address, port);
                System.out.println("连接已建立，port：" + port);
                InputStream ind = skt.getInputStream();
                OutputStream oud = new FileOutputStream(f);
                byte[] bt1 = new byte[length];
                System.out.println("开始下载文件…");
                ind.read(bt1);
                oud.write(bt1);
                skt.close();
                ind.close();
                oud.close();
                System.out.println(port + "文件传输完毕，Socket已关闭");
                while (latch.getCount() <= 0) ;//这个判断防止在主线程new latch之前countDown
                latch.countDown();
            } catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        };
        new Thread(tDownload).start();
    }
    private void Upload(File f, int port) throws IOException
    {
        Socket skt = new Socket(address, port);
        System.out.println("连接已建立，port："+port);
        InputStream inu = new FileInputStream(f);
        OutputStream ouu = skt.getOutputStream();
        byte[] bt1 = new byte[Math.toIntExact(f.length()+4)];
        byte[] b_len = DealByte.Int2Byte( Math.toIntExact(f.length()) );
        for(int i=0; i<4; i++)
        {
            bt1[i] = b_len[i];//把长度写到前四个字节
        }
        Runnable tUpload = ()->
        {
            System.out.println("开始上传文件…");
            try
            {
                inu.read(bt1, 4, (int)f.length());
                ouu.write(bt1);
                skt.close();
                inu.close();
                ouu.close();
                System.out.println("文件传输完毕，Socket已关闭");
                while(latch.getCount()<=0);
                latch.countDown();
            } catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        };
        new Thread(tUpload).start();
    }

    public void run()
    {
        while(true)
        {
            if(socket1.isClosed())break;//断开链接就跳出循环
            try
            {
                latch.await();//门闩阻塞，等待所有线程结束再继续
            } catch (InterruptedException e)
            {
                throw new RuntimeException(e);
            }
            System.out.print(">>>");
            command = scanner.nextLine();
            try
            {
                o1.write( command.getBytes(StandardCharsets.UTF_8) );
            } catch (IOException e)
            {
                throw new RuntimeException(e);
            }

            while(true)
            {
                try
                {
                    int length = i1.read(feedback);
                    o1.write('\\');//反馈表示确认收到了
                    String s = new String(feedback, 0, length, Charset.defaultCharset());
                    if(!DealByte.compare(feedback,"\\"))
                        System.out.println(s);
                    if(DealByte.compare(feedback,"\\GET"))
                    {
                        File f = new File(".\\" + DealByte.divideStr(feedback, (byte) '\\'));
                        Download(f, Integer.parseInt( DealByte.divideStr(feedback, (byte)'T', (byte)'_' ) ), Integer.parseInt( DealByte.divideStr(feedback, (byte)'_', (byte)'\\' ) ) );
                        threadNumber += 1;
                    }
                    else if(DealByte.compare(feedback,"\\PUT"))
                    {
                        File f = new File(new String(command.getBytes(), 4, command.length()-4, Charset.defaultCharset()));
                        Upload(f, Integer.parseInt( DealByte.divideStr(feedback, (byte) 'T') ));
                        threadNumber += 1;
                    }
                    else if(DealByte.compare(feedback,"\\MPUT"))
                    {
                        String name = DealByte.divideStr(command.getBytes(), (byte)'\\');
                        File[] fileList = new File(new String(command.getBytes(), 5, command.length()-5-name.length(), Charset.defaultCharset())).listFiles();
                        for(File f : fileList)
                        {
                            byte[] b_read = new byte[100];
                            if(f.isFile() && Wildcard.wildcard(f, name))
                            {
                                o1.write( ("\\PUT\\" + f.getName() ).getBytes(StandardCharsets.UTF_8) );
                                i1.read(b_read);
                                Upload(f, Integer.parseInt( DealByte.divideStr(b_read, (byte) '\\', (byte)'\0' ) ));
                                threadNumber +=1;
                            }
                        }
                        o1.write(("\\OVER").getBytes(StandardCharsets.UTF_8));
                        latch = new CountDownLatch(threadNumber);
                        break;
                    }
                    else if(DealByte.compare(feedback,"\\QUIT"))
                    {
                        socket1.close();
                        break;
                    }
                    else if(s.equals("\\OVER"))
                    {
                        if(threadNumber>0)
                        {
                            latch = new CountDownLatch(threadNumber);
                            threadNumber = 0;
                        }
                        break;
                    }
                } catch (IOException e)
                {
                    throw new RuntimeException(e);
                }
            }

        }
    };


    public static void main(String [] args) throws IOException
    {
        Client client = new Client(8000,"0.0.0.0");
    }
}
