import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.*;
import java.io.*;
import java.util.*;


public class Server extends JFrame implements Runnable, ListSelectionListener, ActionListener{
    private Socket s = null;
    private ServerSocket ss = null;
    private Container c = getContentPane();
    private ArrayList<ChatThread> users = new ArrayList<ChatThread>();
    DefaultListModel<String> dl = new DefaultListModel<String>();
    private JList<String> userList = new JList<String>(dl);

    private JPanel jpl = new JPanel();
    private JButton jbt = new JButton("踢出聊天室");
    private JButton jbt1 = new JButton("群发消息");
    //群发消息输入栏
    private JTextField jtf = new JTextField();
    //定义一个FlowLayout
    //private FlowLayout flowLayout = new FlowLayout(FlowLayout.CENTER,10,10);
    //定义一个GridLayout
    //private GridLayout gridLayout = new GridLayout(2,2,10,10);

    //private BorderLayout borderLayout = new BorderLayout();

    public Server() throws Exception{
        this.setTitle("服务器端");
        //this.setLayout(new BorderLayout());
        this.add(userList, "North");//放在北面


        //仅将群发消息输入栏设为一栏
        jtf.setColumns(1);

        //jpl.setLayout(flowLayout);
        jpl.setLayout(new BorderLayout());
        jpl.add(jtf, BorderLayout.NORTH);
        jpl.add(jbt,BorderLayout.CENTER);
        jpl.add(jbt1, BorderLayout.WEST);
        //jpl.add(jtf);
        jbt1.addActionListener(this);

        this.add(jpl, "South");

        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setLocation(400,100);
        this.setSize(300, 400);
        this.setVisible(true);
        this.setAlwaysOnTop(true);
        ss = new ServerSocket(9999);
        new Thread(this).start();
    }
    @Override
    public void run() {
        while(true){
            try{
                s = ss.accept();
                ChatThread ct = new ChatThread(s);
                users.add(ct);



                ct.start();
            }catch (Exception ex){
                ex.printStackTrace();
                javax.swing.JOptionPane.showMessageDialog(this,"服务器异常退出！");
                System.exit(0);
            }
        }
    }

    //List选择事件监听
    @Override
    public void valueChanged(ListSelectionEvent e) {

    }

    //群发消息按钮点击事件监听
    @Override
    public void actionPerformed(ActionEvent e) {
        for(ChatThread ct : users){
            ct.ps.println(jtf.getText());
        }
        //发送完后，是输入框中内容为空
        jtf.setText("");
    }

    public class ChatThread extends Thread{
        private Socket s = null;
        private BufferedReader br = null;
        private PrintStream ps = null;
        public boolean canRun = true;
        String nickName = null;
        public ChatThread(Socket s) throws Exception{
            this.s = s;
            br = new BufferedReader(new InputStreamReader(s.getInputStream()));
            ps = new PrintStream(s.getOutputStream());
        }
        public void run(){
            while(canRun){
                try{
                    String msg = br.readLine();
                    String[] strs = msg.split("#");
                    if(strs[0].equals("LOGIN")){
                        nickName = strs[1];
                        dl.addElement(nickName);
                        sendMessage(nickName+"上线了！");
                    }else if(strs[0].equals("MSG")){
                        sendMessage(nickName+"说："+strs[1]);
                    }else if(strs[0].equals("OUT")){//收到来自客户端的下线消息
                        dl.removeElement(nickName);
                        userList.updateUI();//更新List列表
                    }
                }catch (Exception e){}
            }
        }

        public void sendMessage(String msg){
            for(ChatThread ct : users){
                ct.ps.println(msg);
            }
        }
    }



    public static void main(String[] args) throws Exception{
        new Server();
    }


}

