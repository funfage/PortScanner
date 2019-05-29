package cn.gdut.zrf;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Unit test for simple App.
 */
public class AppTest 
{

    @Test
    public void nmapTest(){
        System.out.println(getReturnData("C:/Program Files (x86)/Nmap/nmap.exe", "-sS -P0 -A -v www.baidu.com"));
    }
    /**
     * 调用nmap进行扫描
     * @param nmapDir nmap路径
     * @param command 执行命令
     *
     * @return 执行回显
     * */
    public String getReturnData(String nmapDir,String command){
        Process process = null;
        StringBuffer stringBuffer = new StringBuffer();
        try {
            process = Runtime.getRuntime().exec(nmapDir + " " + command);
            System.out.println("请稍等。。。");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(),"UTF-8"));
            String line = null;
            while((line = reader.readLine()) != null){
                stringBuffer.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuffer.toString();
    }


}
