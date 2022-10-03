package glimmer.wzf1ql.ftp;

import java.io.File;

public class Wildcard
{
    public static boolean wildcard (File f, String name)
    {
        int checkpoint = -1;
        int j = 0;
        String fname = f.getName();
        int flength = fname.length();//我总觉得把这类在循环里要用到的但是不变的量提前计算能提高一点效率
        int length = name.length();
        for(int i = 0; i < flength; i++)
        {
            if(name.charAt(j) == '*')
            {
                checkpoint = j+1;
                if(j == length-1)return true;//末尾是*后面不重要，肯定符合
            }
            else if(name.charAt(j) == '?')
            {
                if(j == length-1)return true;
                else if(fname.charAt(i+1) == name.charAt(j+1))//问号跳过一个比较下一个
                {
                    i++;
                    j++;
                }
                else return false;//下一个不对就不符合
            }
            if(fname.charAt(i) != name.charAt(j))
            {
                if(checkpoint == -1)
                    return false;//还没遇到*就不同，肯定不符合
                else
                {
                    j=checkpoint;//假如对应不上就回溯到*位置
                }
            }
            else
            {
                if(i == flength-1 && j == length-1)return true;//两个同时到达末尾，说明前面都符合，那就符合
                j++;//如果目前为止符合就继续判断下一个
            }
        }
        return false;
    }

    /*public static void main(String [] args)
    {
        File f =new File("D:/secret.txt");
        System.out.println(wildcard(f, "s*r*x?"));
    }*/
}
