import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.awt.event.*;


public class Client extends JFrame implements Runnable,ActionListener{
    private JButton jbt = new JButton("发送消息");
    private JTextField jtf = new JTextField();
    private JTextArea jta = new JTextArea();
    private BufferedReader br = null;
    private PrintStream ps = null;
    private String nickName = null;
    //同步在线好友与选择好友聊天的按钮部分
    private JButton jbt1 = new JButton("私发消息");
    DefaultListModel<String> dl = new DefaultListModel<String>();
    //加滚动条
    private JScrollPane js = new JScrollPane(jta);

    private JList<String> userList = new JList<String>(dl);
    //用jList从java.awt.Component中继承的方法setSize()固定jlist的大小
    private JPanel jpl = new JPanel();
    private JPanel jpl1 = new JPanel();

    JTextArea jTextArea = new JTextArea();
    JTextField jTextField = new JTextField();
    String suser = new String();
    boolean flag = false;

    public Client() throws Exception{
        js.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        jpl1.setLayout(new BorderLayout());
        jpl1.add(jbt, BorderLayout.SOUTH);
        jpl1.add(js,BorderLayout.CENTER);

        //this.add(jbt, BorderLayout.SOUTH);
        this.add(jtf, BorderLayout.NORTH);
        this.add(jpl1, BorderLayout.CENTER);
        jtf.setFont(new Font("宋体", Font.PLAIN, 15));
        jta.setFont(new Font("宋体", Font.PLAIN, 15));
        jta.setBackground(Color.PINK);


        //鼠标事件，点击
        jbt.addActionListener(this);
        jbt1.addActionListener(this);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setAlwaysOnTop(true);

        nickName = JOptionPane.showInputDialog("输入昵称");
        this.setTitle(nickName + "的聊天室");
        this.setSize(300, 400);
        this.setVisible(true);

        jpl.setLayout(new BorderLayout());
        jpl.add(userList, BorderLayout.CENTER);
        jpl.add(jbt1, BorderLayout.SOUTH);

        //jbt1.setFont(new Font("宋体", Font.PLAIN, 15));
        //jbt1.setPreferredSize(new Dimension(50,40));

        this.add(jpl, BorderLayout.WEST);

        //建设同步在线好友与选择好友聊天的部分
        /*this.add(userList, BorderLayout.WEST);
        userList.setSize(200,this.getHeight());*/

        Socket s = new Socket("127.0.0.1", 9999);
        br = new BufferedReader(new InputStreamReader(s.getInputStream()));
        ps = new PrintStream(s.getOutputStream());
        new Thread(this).start();
        ps.println("LOGIN#" + nickName);

        //键盘事件，实现当输完要发送的内容后，直接按回车键，实现发送
        //监听键盘相应的控件必须是获得焦点（focus）的情况下才能起作用
        jtf.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_ENTER) {
                    ps.println("MSG#" + nickName + "#" + jtf.getText());
                    //发送完后，是输入框中内容为空
                    jtf.setText("");
                }
            }
        });

        jtf.setFocusable(true);

        //监听系统关闭事件，退出时给服务器端发出指定消息
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                ps.println("OFLINE#" + nickName);
                //System.out.println("监听到系统关闭");
            }
        });

    }
    public void run(){
        while (true){
            try{
                String msg = br.readLine();
                String[] strs = msg.split("#");
                //判断是否为服务器发来的登陆信息
                if(strs[0].equals("LOGIN")){
                    if(!strs[1].equals(nickName)){//!Objects.equals(strs[1], nickName)
                        jta.append(strs[1] + "上线啦！\n");
                        dl.addElement(strs[1]);
                        userList.repaint();
                    }
                }else if(strs[0].equals("MSG")){
                    if(!strs[1].equals(nickName)){
                        jta.append(strs[1] + "说：" + strs[2] + "\n");
                    }else{
                        jta.append("我说：" + strs[2] + "\n");
                    }
                }else if(strs[0].equals("USERS")){
                    dl.addElement(strs[1]);
                    userList.repaint();
                } else if(strs[0].equals("ALL")){
                    jta.append("全体消息：" + strs[1] + "\n");
                }else if(strs[0].equals("OFLINE")){
                    dl.removeElement(strs[1]);
                    userList.repaint();
                }else if((strs[2].equals(nickName) || strs[1].equals(nickName)) && strs[0].equals("SMSG")){
                    if(!strs[1].equals(nickName)){
                        jTextArea.append(strs[1] + "说：" + strs[3] + "\n");
                    }else{
                        jTextArea.append("我说：" + strs[3] + "\n");
                    }
                }else if((strs[2].equals(nickName) || strs[1].equals(nickName))&& strs[0].equals("FSMSG")){//只有发信人和私信人能看（第一次）
                    if(strs[2].equals(nickName)){//如果被私信人是自己则显示系统消息
                        jTextArea.append(strs[1] + "说：" + strs[3] + "\n");
                        jta.append("系统：" + strs[1] + "私信了你" + "\n");
                    }else{//若自己为发信人
                        jTextArea.append( "我说：" + strs[3] + "\n");
                    }

                }
                //jta.append(msg + "\n");
            }catch (Exception ex){
                ex.printStackTrace();
                javax.swing.JOptionPane.showMessageDialog(this, "您已被请出聊天室！");
                System.exit(0);
            }
        }

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String label = e.getActionCommand();
        if(label.equals("发送消息")){
            handleSend();
        }else if(label.equals("私发消息") && !userList.isSelectionEmpty()){//未点击用户不执行
            suser = userList.getSelectedValuesList().get(0);
            handleSec();
        }else if(label.equals("发消息")){
            handleSS();
        }
    }

    public void handleSS(){
        if(flag){
            ps.println("SMSG#" + nickName + "#" + suser + "#" + jTextField.getText());
            jTextField.setText("");
        }else{//首次私信格式为"FSMSG#  发信人  # 收信人 # 内容
            ps.println("FSMSG#" + nickName + "#" + suser + "#" + jTextField.getText());
            jTextField.setText("");
            flag = true;
        }

    }

    public void handleSend(){
        //发送信息时标识一下来源
        ps.println("MSG#" + nickName + "#" + jtf.getText());
        //发送完后，是输入框中内容为空
        jtf.setText("");
    }

    public void handleSec(){
        JFrame jFrame = new JFrame();
        //JTextArea jTextArea = new JTextArea();
        JButton jButton = new JButton("发消息");
        //JTextField jTextField = new JTextField();

        jFrame.add(jButton, BorderLayout.SOUTH);
        jFrame.add(jTextField, BorderLayout.NORTH);
        jFrame.add(jTextArea,BorderLayout.CENTER);

        jButton.addActionListener(this);
        jTextArea.setFont(new Font("宋体", Font.PLAIN,15));
        jTextArea.setBackground(Color.CYAN);
        jFrame.setSize(400,300);
        jFrame.setLocation(400,150);
        jFrame.setTitle("与" + userList.getSelectedValuesList().get(0) + "私聊");
        jFrame.setVisible(true);

    }

    public static void main(String[] args)throws Exception{
        new Client();
    }
}

