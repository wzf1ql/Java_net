package glimmer.wzf1ql.echo;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Scanner;

public class ClientE extends Thread
{
    private Scanner scanner = new Scanner(System.in);
    private String sendMessage;
    private InputStream inputStream;
    private OutputStream outputStream;
    private int read;
    byte bytes[] = new byte[1024];
    GUI gui;
    private Runnable runnable = ()->{

            while(true)
            {
                try
                {
                    read = inputStream.read(bytes);

                } catch (IOException e)
                {
                    throw new RuntimeException(e);
                }
                if(gui!=null)gui.addText("服务端：" + new String(bytes, 0, read, Charset.defaultCharset()));
                System.out.println("服务端：" + new String(bytes, 0, read, Charset.defaultCharset()));
            }
    };

    public ClientE(int port) throws IOException
    {
        System.out.println("请输入主机IP地址：");
        Socket socket = new Socket(scanner.next(), port);
        inputStream = socket.getInputStream();
        outputStream = socket.getOutputStream();
        this.start();
    }
    public ClientE(int port, GUI ggui) throws IOException, InterruptedException
    {
        this.gui = ggui;
        while(true)
        {
            if(!Objects.equals(gui.getText(), ""))
            {
                Socket socket = new Socket(gui.getText(), port);
                System.out.println(gui.getText());
                inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();
                break;
            }
            //sleep(100);
        }
        this.start();
    }

    public void send(String sendMessage)
    {
        try
        {
            outputStream.write( sendMessage.getBytes(StandardCharsets.UTF_8) );
        }catch (UnknownHostException e)
        {
            throw new RuntimeException(e);
        } catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }
    public void run()
    {
        new Thread(runnable).start();
        while (true)
        {
            if(gui==null)
                sendMessage = scanner.next();
            else
            {
                sendMessage = gui.getText();
            }
            if(!Objects.equals(sendMessage, ""))
            {
                send(sendMessage);
                if(gui!=null)gui.addText("客户端：" + sendMessage);
            }
        }
    }


    public static void main(String [] args) throws IOException, InterruptedException
    {

        //GUI gui = new GUI("客户端");
        ClientE clientE = new ClientE(9797);
    }
}
//192.168.1.108