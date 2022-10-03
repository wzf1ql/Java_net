package glimmer.wzf1ql.ftp;

import java.io.IOException;
import java.nio.charset.Charset;

public class DealByte
{
    public static boolean compare(byte[] a, String b)
    {
        int length = b.length();//因为命令库里没有前几个字母一样的命令，所以直接对比前几个就可以
        for(int i=0;i<length;i++)//新想法，只要对比到空格就什么命令都可以了
        {
            if(a[i]!=b.charAt(i))
                return false;
        }
        return true;
    }
    public static String divideStr(byte[] a, byte begin, byte end)
    {
        String s = "";
        boolean began = false;
        for(int i = 0; i<a.length; i++)
        {
            if(began)
            {
                if(a[i] == end) return s;
                s += new String(a, i, 1 , Charset.defaultCharset());
            }
            else if(a[i] == begin)
            {
                began = true;
            }
        }
        return null;
    }
    public static String divideStr(byte[] a, int begin, byte end)
    {
        String s = "";
        for(int i = begin; i<a.length; i++)
        {
           if(a[i] == end || a[i] == '\0') return s;
           s += new String(a, i, 1 , Charset.defaultCharset());
        }
        return s;
    }
    public static String divideStr(byte a[], byte begin)
    {
        String s = "";
        for(int i = a.length-1; i>=0; i--)
        {
            if(a[i]==begin)
            {
                for(i=i+1; i<a.length; i++)
                {
                    if (a[i] == '\0') break;
                    s += new String(a, i, 1, Charset.defaultCharset());
                }
                return s;
            }
        }
        return null;
    }
    public static byte[] Int2Byte(int a)
    {
        byte[] b = new byte[4];
        b[0] = (byte) ((a >> 24) & 0xff);//用移位的方法将int存入byte
        b[1] = (byte) ((a >> 16) & 0xff);
        b[2] = (byte) ((a >> 8) & 0xff);
        b[3] = (byte) (a & 0xff);
        return b;
    }
    public static int Byte2Int(byte[] b)
    {
        int a = 0;
        a |= (b[0] & 0xff)<<24;
        a |= (b[1] & 0xff)<<16;
        a |= (b[2] & 0xff)<<8;
        a |= (b[3] & 0xff);
        return a;
    }

    /*public static void main(String [] args)
    {
        System.out.println(Byte2Int(Int2Byte(5534)));
    }*/
}
