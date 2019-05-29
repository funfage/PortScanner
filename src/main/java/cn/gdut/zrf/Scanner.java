package cn.gdut.zrf;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

public class Scanner {

    private int textLength = 15;

    private int timeout = 800;

    private ExecutorService threadPool;

    JFrame frame = new JFrame("多线程端口扫描器");
    // 字体
    Font font = new Font("宋体",Font.BOLD,25);
    //主机IP
    JLabel hostIpLabel = new JLabel("主机IP：");
    JTextField hostIpText = new JTextField(textLength);
    //起始端口
    JLabel startPortLabel = new JLabel("起始端口：");
    JTextField startPortField = new JTextField(textLength);
    //结束端口
    JLabel endPortLabel = new JLabel("结束端口：");
    JTextField endPortField = new JTextField(textLength);
    //线程数
    JLabel threadNumberLabel = new JLabel("线程数：");
    JTextField threadNumberField = new JTextField(textLength);
    //扫描结果按钮和标签
    JButton clearResult = new JButton("清除结果");
    JLabel resultLabel = new JLabel("扫描结果：");
    //扫描结果文本域
    JTextArea resultArea = new JTextArea(22, 70);
    //扫描状态
    JLabel statusLabel = new JLabel("扫描状态：");
    JLabel statusField = new JLabel("未扫描");
    //扫描按钮
    JButton scanButton = new JButton("开始扫描");
    //重置按钮
    JButton resetButton = new JButton("重置");
    //结束按钮
    JButton stopButton = new JButton("停止扫描");

    public void init(){
        //设置所有组件的字体
        setMyFont(font);
        /**
         * 添加中间部分的组件
         */
        //创建一个垂直排列的box组件
        Box mainBox = Box.createVerticalBox();
        //顶部面板
        JPanel topPanel = new JPanel();
        topPanel.add(hostIpLabel);
        topPanel.add(hostIpText);
        topPanel.add(startPortLabel);
        topPanel.add(startPortField);
        topPanel.add(endPortLabel);
        topPanel.add(endPortField);
        topPanel.add(threadNumberLabel);
        topPanel.add(threadNumberField);
        mainBox.add(topPanel);
        mainBox.add(Box.createVerticalStrut(15));
        //中部面板
        Box centerBox = Box.createHorizontalBox();
        centerBox.add(resultLabel);
        centerBox.add(Box.createHorizontalStrut(800));
        centerBox.add(clearResult);
        mainBox.add(centerBox);
        mainBox.add(Box.createVerticalStrut(10));
        //创建滚动面板
        Box textBox = Box.createHorizontalBox();
        textBox.add(Box.createHorizontalStrut(20));
        resultArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(resultArea);
        textBox.add(scrollPane);
        textBox.add(Box.createHorizontalStrut(20));
        mainBox.add(textBox);
        mainBox.add(Box.createVerticalStrut(10));
        /**
         * 创建容纳底部的文本，按钮等的面板
         */
        JPanel bottom = new JPanel();
        //采用网格布局
        bottom.setLayout(new GridLayout(1, 5, 10, 10));
        bottom.add(statusLabel);
        bottom.add(statusField);
        bottom.add(scanButton);
        bottom.add(resetButton);
        bottom.add(stopButton);
        mainBox.add(bottom);
        mainBox.add(Box.createVerticalStrut(10));
        frame.add(mainBox);
        /**
         * 添加按钮点击事件
         */
        clearResult.addActionListener(new clearListener());
        scanButton.addActionListener(new scanListener());
        resetButton.addActionListener(new resetListener());
        stopButton.addActionListener(new stopListener());
        /**
         * 窗体相关设置
         */
        //设置关闭时窗口时退出程序
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //设置窗体的位置
        frame.setLocation(300,180);
        frame.pack();
        frame.setVisible(true);
    }

    public void setMyFont(Font font){
        hostIpLabel.setFont(font);
        hostIpText.setFont(font);
        startPortLabel.setFont(font);
        startPortField.setFont(font);
        endPortLabel.setFont(font);
        endPortField.setFont(font);
        threadNumberLabel.setFont(font);
        threadNumberField.setFont(font);
        clearResult.setFont(font);
        resultLabel.setFont(font);
        resultArea.setFont(new Font("宋体",Font.BOLD,20));
        statusLabel.setFont(font);
        statusField.setFont(font);
        scanButton.setFont(font);
        resetButton.setFont(font);
        stopButton.setFont(font);
    }

    /**
     * 清除结果
     */
    class clearListener implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e) {
            resultArea.setText("");
        }
    }

    /**
     * 扫描按钮监听器
     */
    class scanListener implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e) {
            if(threadPool != null && !threadPool.isTerminated()){
                JOptionPane.showMessageDialog(null,"程序正在扫描中，请耐心等待") ;
            }else{
                //清除记录
                resultArea.setText("");
                System.out.println("开始扫描。。。");
                /**
                 * 判断输入的数据是否合法
                 */
                //ip地址是否符合规范
                String hostIp = hostIpText.getText();
                if(checkIP(hostIp)){
                    try {
                        int startPort = Integer.parseInt(startPortField.getText().trim());
                        int endPort = Integer.parseInt(endPortField.getText().trim());
                        int threadNumber = Integer.parseInt(threadNumberField.getText().trim());
                        if(startPort <=0  || startPort > 65535 || startPort >= endPort){
                            JOptionPane.showMessageDialog(null,"起始端口必须在1~65535之间，并且小于结束端口") ;
                        }else if(endPort <=0  || endPort > 65535){
                            JOptionPane.showMessageDialog(null,"结束端口必须在1~65535之间") ;
                        }else if(threadNumber <=0 || threadNumber > 200){
                            JOptionPane.showMessageDialog(null,"线程数必须在1~200之间   ") ;
                        }else{
                            //多线程扫描
                            threadPool = Executors.newCachedThreadPool();
                            for(int i = 0; i < threadNumber; i++){
                                ScannerThread scannerThread = new ScannerThread(hostIp, startPort, endPort, threadNumber, i, timeout);
                                threadPool.execute(scannerThread);
                            }
                            threadPool.shutdown();
                        }
                    } catch (NumberFormatException e1) {
                        e1.printStackTrace();
                        JOptionPane.showMessageDialog(null,"错误的端口号或端口号和线程数必须为整数") ;
                    }
                }else{
                    JOptionPane.showMessageDialog(null,"请输入正确的Ip地址") ;
                }
            }
        }
    }

    /**
     * 重置按钮监听器
     */
    class resetListener implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e) {
            hostIpText.setText("");
            startPortField.setText("");
            endPortField.setText("");
            threadNumberField.setText("");
            resultArea.setText("");
            statusField.setText("未扫描");
        }
    }

    /**
     * 试图停止所有正在执行的活动任务，暂停处理正在等待的任务。
     */
    class stopListener implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e) {
            if(threadPool != null && !threadPool.isTerminated()){
                threadPool.shutdownNow();
                resultArea.append("扫描中止");
                statusField.setText("扫描中止");
            }
        }
    }

    class ScannerThread implements Runnable {

        private String hostIp;

        private int startPort, endPort, threadNumber, serial, timeout;

        /**
         *
         * @param hostIp 待扫描IP
         * @param startPort 起始端口
         * @param endPort 结束端口
         * @param threadNumber 线程数
         * @param serial 标记是第几个线程
         * @param timeout 超时时间
         */
        public ScannerThread(String hostIp, int startPort, int endPort, int threadNumber, int serial, int timeout) {
            this.hostIp = hostIp;
            this.startPort = startPort;
            this.endPort = endPort;
            this.threadNumber = threadNumber;
            this.serial = serial;
            this.timeout = timeout;
        }

        @Override
        public void run() {
            int port = 0;
            try {
                InetAddress address = InetAddress.getByName(hostIp);
                Socket socket;
                SocketAddress socketAddress;
                for (port = startPort + serial; port <= endPort; port += threadNumber){
                    if(Thread.currentThread().isInterrupted()){
                        break;
                    }
                    SwingUtilities.invokeLater(new ProgressRunnable(port));  //更新界面
                    socket = new Socket();
                    socketAddress = new InetSocketAddress(address, port);
                    try {
                        socket.connect(socketAddress, timeout);
                        socket.close();
                        SwingUtilities.invokeLater(new ResultRunnable(hostIp, port));  //更新界面
                    } catch (IOException e) {
                        //e.printStackTrace();
                    }
                }
                try {
                    if(port == endPort + threadNumber){
                        Thread.sleep(1000);
                        resultArea.append("扫描完毕！\n");
                        statusField.setText("扫描完毕！");
                    }
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }

            } catch (UnknownHostException e) {
                resultArea.append("找不到IP地址为：" + hostIp + "的主机" + "\n");
                System.out.println("找不到IP地址为：" + hostIp + "的主机");
                e.printStackTrace();
            }
        }
    }

    /**
     * 由EDT调用来更新界面的线程
     * */
    class ResultRunnable implements Runnable{
        private String hostIp;
        private int currentPort = 0;

        public ResultRunnable (String hostIp, int currentPort) {
            this.currentPort = currentPort;
            this.hostIp = hostIp;
        }
        @Override
        public void run() {
            resultArea.setEditable(true);
            resultArea.append("主机IP："+ hostIp +"     端口：" + currentPort + "    开放" + "     服务类型：" + getServerType(currentPort) + "\n");
            resultArea.setEditable(false);
            //设置最新内容
            resultArea.selectAll();
            resultArea.setCaretPosition(resultArea.getSelectionEnd());
        }
    }

    /**
     * 由EDT调用来更新界面的线程
     * */
    class ProgressRunnable implements Runnable{
        private int currentPort = 0;

        public ProgressRunnable (int currentPort) {
            this.currentPort = currentPort;
        }
        @Override
        public void run() {
            statusField.setText("正在扫描端口：" + currentPort + "\n");
        }
    }




    public static void main(String[] args) {
        new Scanner().init();
    }

    private String getServerType(int port){
        switch(port){
            case 20:return "FTP Data";
            case 21:return "FTP Control";
            case 23:return "TELNET";
            case 25:return "SMTP";
            case 38:return "RAP";
            case 53:return "DNS";
            case 79:return "FINGER";
            case 80:return "HTTP";
            case 110:return "POP";
            case 161:return "SNMP";
            case 443:return "HTTPS";
            case 1443:return "SQL";
            case 8000:return "QICQ";
            default:return "未知服务";
        }
    }

    // 判断输入的IP是否合法
    private boolean checkIP(String str) {
        Pattern pattern = Pattern
                .compile("^((\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5]"
                        + "|[*])\\.){3}(\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5]|[*])$");
        return pattern.matcher(str).matches();
    }


}



















