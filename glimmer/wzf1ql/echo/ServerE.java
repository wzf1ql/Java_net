package glimmer.wzf1ql.echo;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Scanner;

public class ServerE extends Thread
{
    private GUI gui;
    private ServerSocket serverSocket;
    private Scanner scanner = new Scanner(System.in);
    private Socket client;
    boolean connected = false;
    private String sendMessage;
    private OutputStream outputStream;
    private InputStream inputStream;
    private byte[] bytes = new byte[1024];
    private Runnable runnalbe = ()->{
        try
        {
            while(true)
            {
                int read = inputStream.read(bytes);
                if(gui==null)
                    System.out.println("客户端：" + new String(bytes, 0, read, Charset.defaultCharset()));
                else
                {
                    gui.addText("客户端：" + new String(bytes, 0, read, Charset.defaultCharset()));
                }
            }
        } catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    };
    public ServerE(int port) throws IOException
    {
        serverSocket = new ServerSocket(port);
        serverSocket.setSoTimeout(1000000);
    }
    public ServerE(int port, GUI gui) throws IOException
    {
        serverSocket = new ServerSocket(port);
        serverSocket.setSoTimeout(1000000);
        this.gui = gui;
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
        while(true)
        {
            try
            {
                if(!connected)
                {
                    if(gui==null)
                    {
                        System.out.println("等待连接，端口号：" + serverSocket.getLocalPort());
                        client = serverSocket.accept();//监听客户端，没有接收到数据会停在此处
                        System.out.println("远程主机地址：" + client.getRemoteSocketAddress());
                    }
                    else
                    {
                        gui.addText("等待连接，端口号：" + serverSocket.getLocalPort());
                        gui.addText(String.valueOf(InetAddress.getLocalHost().getHostAddress()));
                        System.out.println("等待连接，端口号：" + serverSocket.getLocalPort());
                        client = serverSocket.accept();//监听客户端，没有接收到数据会停在此处
                        gui.addText("远程主机地址：" + client.getRemoteSocketAddress());

                        System.out.println("远程主机地址：" + client.getRemoteSocketAddress());
                    }
                    outputStream = client.getOutputStream();
                    inputStream = client.getInputStream();
                    new Thread(runnalbe).start();
                    connected = true;
                    continue;
                }
                if(gui==null)
                    sendMessage = scanner.next();
                else
                {
                    sendMessage = gui.getText();
                }
                if(!Objects.equals(sendMessage, ""))
                {
                    send(sendMessage);
                    if(gui!=null)gui.addText("服务端：" + sendMessage);
                    sendMessage="";
                }
            }catch(SocketTimeoutException e)
            {
                System.out.println("Time out!");
                break;
            } catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    public static void main(String [] args) throws IOException
    {
        GUI gui = new GUI("服务端");
        ServerE serverE = new ServerE(9797, gui);
        serverE.start();
    }
}
