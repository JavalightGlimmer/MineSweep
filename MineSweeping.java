package com.faith.game.mineSweeping;

import com.sun.awt.AWTUtilities;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.PropertyResourceBundle;
import java.util.Random;
import java.util.ResourceBundle;

public class MineSweeping {
    public static void main(String[] args) {
        new MyJframe();
    }
}

//画布
class MyJframe extends JFrame{

    private int INIT_ROW = 6;//初始行数
    private int INIT_COLUMN = 6;//初始列数
    private JPanel j1;
    private JPanel j2;
    private JPanel gameP;
    private JButton[][] jbtns;
    private int[][] data;
    private  int VALUE_OF_MINE = -1;//雷的值
    private boolean[][] isOpen;//用于判断格子是否翻开，默认未翻开为flase
    private int countForNotMines;//用于统计用户剩余的非雷数，刷新之后要清零
    private int countForMines;//用于统计雷的数量,且新建一个游戏窗口，雷数是固定的。
    private int tIForSaveVal = -1;//暂存鼠标指向的格子的横坐标
    private int tJForSaveVal = -1;//暂存鼠标指向的格子的纵坐标
    private ButtonGroup voiceSettings;//多选框
    private JRadioButton voiceJRB1;//声音设置选项
    private JRadioButton voiceJRB2;//声音设置选项
    private JButton voiceConfirm;//声音确认按钮
    private static int voiceFlag;//声音设置传值元素
    private ButtonGroup animeSettings;//多选框
    private JRadioButton animeJRB1;//动画设置选项
    private JRadioButton animeJRB2;//动画设置选项,可添加dlc
    private JButton animeConfirm;//动画确认按钮
    private static int animeFlag;//动画设置传值元素
    private JLabel timeLabel;//用于显示游戏用时
    private int seconds;//统计游戏用时
    private Timer timer;//用于处理计时
    private static int gameTime;//场次
    private static int winTime;//胜利场次
    private static double winRate;//胜率

    /*本来ResourceBundle.getBundle()默认可以读到的文件只在resource资源包下
    但是以下的代码和静态代码块可以使其读到文件夹之外的资源
    */
    private static ResourceBundle bundle;
    private static BufferedInputStream inputStream;
    static {
        String proFilePath = System.getProperty("user.dir") +"\\winRate.properties";
        try {
            inputStream = new BufferedInputStream(new FileInputStream(proFilePath));
            bundle = new PropertyResourceBundle(inputStream);
            inputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //初始游戏窗口
    public MyJframe() {
        setFrameSize(INIT_ROW,INIT_COLUMN);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        add(setPanelStyle(INIT_ROW, INIT_COLUMN));
        add(setJMB(),BorderLayout.NORTH);
        setMines(6,INIT_ROW,INIT_COLUMN);//先初始化数据，再往按钮对应赋值
        setButtuns(gameP,INIT_ROW,INIT_COLUMN);
        initData();//最开始初始化一次胜率数据
        pack();//可以尽可能使设置按照你的代码来布局
        setResizable(false);
        setVisible(true);
        //关闭一次窗口保存一次数据
        writeWinRate(this);

    }

    //自定义游戏窗口
    public MyJframe(int mineNums, int ROW,int COLUMN) {
        if (mineNums == 0 && ROW ==1 && COLUMN == 1) {
            playVoice("不如跳舞");
            setLocation(533,150);
            JPanel jPanel = new JPanel();
            jPanel.setBackground(Color.white);
            JButton jButton = new JButton();
            jButton.setPreferredSize(new Dimension(300,300));
            jPanel.add(jButton);
            add(jPanel);
            jButton.setIcon(new ImageIcon("img/跳舞.gif"));
            pack();
            setVisible(true);

            jButton.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    JOptionPane.showMessageDialog(null,"获得成就：来之极易的成功"
                            ,null,1,new ImageIcon("img/吃饭.gif"));
                }
            });

        }else if (mineNums == 1 && ROW ==1 && COLUMN == 1){
            playVoice("呼吸");
            setLocation(533,150);
            JPanel jPanel = new JPanel();
            jPanel.setBackground(Color.white);
            JButton jButton = new JButton();
            jButton.setPreferredSize(new Dimension(300,300));
            jPanel.add(jButton);
            add(jPanel);
            pack();
            jButton.setIcon(new ImageIcon("img/漩涡.gif"));
            setVisible(true);
            jButton.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    JOptionPane.showMessageDialog(null,"达成成就：不可能完成的任务"
                            ,null,-1,new ImageIcon("img/demon.png"));
                }
            });
        }else {//非特殊游戏窗口
            writeWinRate(this);
            setFrameSize(ROW,COLUMN);
            setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            add(setPanelStyle(ROW,COLUMN));
            add(setJMB(),BorderLayout.NORTH);
            setMines(mineNums,ROW,COLUMN);
            setButtuns(gameP,ROW,COLUMN);
            pack();//可以尽可能使设置按照你的代码来布局
            setResizable(false);
            setVisible(true);
        }

    }

    //其他窗口
    public MyJframe(String frameName,MyJframe thisFrame){
        if ("自定义".equals(frameName)){
            writeWinRate(this);
            setTitle("自定义");
            setBounds(533,140,300,300);
            setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            setResizable(false);
            JPanel jPanel1 = new JPanel();
            JPanel jPanel2 = new JPanel();
            JPanel jPanel3 = new JPanel();
            JPanel jPanel4 = new JPanel();

            JLabel ROWLABEL = new JLabel("行数(1~27)");
            JLabel COLLABEL = new JLabel("列数(4~53)");
            JLabel MINELABEL = new JLabel("        地雷数");
            JLabel tips = new JLabel("点击按钮提交，空格回车没用");
            TextField ROWTEXT = new TextField();
            TextField COLTEXT = new TextField();
            TextField MINETEXT = new TextField();
            JButton summit = new JButton("提交");

            jPanel1.add(ROWLABEL);
            jPanel1.add(ROWTEXT);
            jPanel2.add(COLLABEL);
            jPanel2.add(COLTEXT);
            jPanel3.add(MINELABEL);
            jPanel3.add(MINETEXT);
            jPanel4.add(tips);
            jPanel4.add(summit);
            add(jPanel1);
            add(jPanel2);
            add(jPanel3);
            add(jPanel4);
            setLayout(new GridLayout(4,1));

            ROWTEXT.addKeyListener(new KeyAdapter() {
                @Override
                public void keyTyped(KeyEvent e) {
                    if (e.getKeyChar() == '\b'){
                    }else if ("0123456789".indexOf(e.getKeyChar()) < 0){
                        JOptionPane.showMessageDialog(null,"行和列不输入数字您在想什么？\n想卡出bug吗？","来自作者的疑问",JOptionPane.ERROR_MESSAGE,new ImageIcon("img/小人.png"));
                        ROWTEXT.setText("");
                    }
                }
            });

            COLTEXT.addKeyListener(new KeyAdapter() {
                @Override
                public void keyTyped(KeyEvent e) {
                    if (e.getKeyChar() == '\b'){
                    }else if ("0123456789".indexOf(e.getKeyChar()) < 0){
                        JOptionPane.showMessageDialog(null,"行和列不输入数字您在想什么？\n想卡出bug吗？","来自作者的疑问",JOptionPane.ERROR_MESSAGE,new ImageIcon("img/小人.png"));
                        ROWTEXT.setText("");
                    }
                }
            });

            MINETEXT.addKeyListener(new KeyAdapter() {
                @Override
                public void keyTyped(KeyEvent e) {
                    if (e.getKeyChar() == '\b'){
                    }else if ("0123456789".indexOf(e.getKeyChar()) < 0){
                        JOptionPane.showMessageDialog(null,"您扫的雷应该是自然数吧？","来自作者的疑问",JOptionPane.ERROR_MESSAGE,new ImageIcon("img/小人.png"));
                        ROWTEXT.setText("");
                    }
                }
            });


            summit.addActionListener(new MyActionnListener(this,ROWTEXT,COLTEXT,MINETEXT));

            setVisible(true);

        }else if ("胜利".equals(frameName)){
            winTime++;//赢一次加一个胜场
            setBounds(558,500,250,80);
            JPanel jPanel = new JPanel();
            JLabel jLabel = new JLabel("恭喜获得胜利!");
            JButton jButton = new JButton("确定");

            jPanel.add(jLabel);
            jPanel.add(jButton);
            add(jPanel,BorderLayout.CENTER);
            setVisible(true);

            jButton.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    thisFrame.frushButtons();
                    thisFrame.seconds = 0;
                    thisFrame.timer.start();
                    dispose();
                }
            });
            jButton.addKeyListener(new KeyAdapter() {
                @Override
                public void keyTyped(KeyEvent e) {
                    if (e.getKeyChar() == ' '){
                        thisFrame.frushButtons();
                        thisFrame.seconds = 0;
                        thisFrame.timer.start();
                        dispose();
                    }
                }
            });
        }else if("失败".equals(frameName)){
            setBounds(558,500,250,80);
            JPanel jPanel = new JPanel();
            JLabel jLabel = new JLabel("BINGO！踩雷啦");
            JButton jButton = new JButton("确定");

            jPanel.add(jLabel);
            jPanel.add(jButton);
            add(jPanel,BorderLayout.CENTER);
            setVisible(true);

            jButton.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    thisFrame.frushButtons();
                    thisFrame.seconds = 0;
                    thisFrame.timer.start();
                    dispose();
                }
            });
            jButton.addKeyListener(new KeyAdapter() {
                @Override
                public void keyTyped(KeyEvent e) {
                    if (e.getKeyChar() == ' '){
                        thisFrame.frushButtons();
                        thisFrame.seconds = 0;
                        thisFrame.timer.start();
                        dispose();
                    }
                }
            });
        }else if ("初级".equals(frameName)){
            //this.setVisible(false);//这里的this指的是……啥我也不太清楚
            new MyJframe(10,10,10).setTitle(frameName);
        }else if ("中级".equals(frameName)){
            new MyJframe(25,15,15).setTitle(frameName);
        }else if ("高级".equals(frameName)){
            new MyJframe(80,20,20).setTitle(frameName);
        }else if ("终级".equals(frameName)){
            new MyJframe(1430,27,53).setTitle(frameName);
        }else if ("帮助".equals(frameName)){
            JTextArea textArea = new JTextArea();
            textArea.setText("游戏规则：\n    点击鼠标左键翻开格子，右键设置格子为旗子或问号来作为辅助判断，\n" +
                    "数字代表周围一圈格子中总共含有的地雷数量，翻开所有非地雷格子取得胜利。\n\n" +
                    "快捷键操作：\n" +
                    "    D：翻开鼠标所指格子\n" +
                    "    F：给格子设置旗子或问号\n" +
                    "    R：重新开始当前游戏\n" +
                    "    空格：确定按钮");
            add(textArea);
            setLocation(533,200);
            pack();
            setVisible(true);
        }else if ("声音设置".equals(frameName)){
            voiceSettings = new ButtonGroup();
            voiceJRB1 = new JRadioButton("默认");
            voiceJRB1.setSelected(true);
            voiceJRB2 = new JRadioButton("静音");
            voiceConfirm = new JButton("确认");
            voiceSettings.add(voiceJRB1);
            voiceSettings.add(voiceJRB2);
            add(voiceJRB1);
            add(voiceJRB2);
            add(voiceConfirm);
            setLayout(new FlowLayout());
            setResizable(false);
            setLocation(533,200);
            pack();
            setVisible(true);

            voiceConfirm.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (voiceJRB1.isSelected()){
                        voiceFlag = 0;//默认为0，有声音
                    }else if (voiceJRB2.isSelected()){
                        voiceFlag = 1;//1为静音
                    }
                    dispose();
                }
            });
        }else if ("动画设置".equals(frameName)){
            animeSettings = new ButtonGroup();
            animeJRB1 = new JRadioButton("礼花");
            animeJRB1.setSelected(true);
            animeJRB2 = new JRadioButton("无");
            animeConfirm = new JButton("确认");
            animeSettings.add(animeJRB1);
            animeSettings.add(animeJRB2);
            add(animeJRB1);
            add(animeJRB2);
            add(animeConfirm);
            setLayout(new FlowLayout());
            setResizable(false);
            setLocation(533,200);
            pack();
            setVisible(true);

            animeConfirm.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (animeJRB1.isSelected()){
                        animeFlag = 0;//默认为0，有动画
                    }else if (animeJRB2.isSelected()){
                        animeFlag = 1;//1为无
                    }
                    dispose();
                }
            });
        }else if ("胜率统计".equals(frameName)){
            setTitle("胜率");
            setIconImage(null);
            setBounds(533,140,150,150);
            JLabel gameTimeLabel = new JLabel("      游戏总场次：" + gameTime);
            winRate = ((double) winTime/gameTime)*100;
            //String.format()控制格式，下式代表保留两位小数
            JLabel winRateLabel = new JLabel("      胜率：" + String.format("%.2f",winRate) + "%");
            setLayout(new GridLayout(2,1));
            setResizable(false);
            add(gameTimeLabel);
            add(winRateLabel);
            setVisible(true);
        }
    }

    //设置frame格式
    private void setFrameSize(int ROW,int COLUMN){
        setTitle("扫雷");
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setBounds((1366-(COLUMN*25+40))/2,(768-(ROW*25+101))/2,COLUMN*25+40,ROW*25+90);//这个数值很关键，40和101不要动
    }

    //设置Jpanel格式
    private JPanel setPanelStyle(int ROW,int COLUMN){
        j1 = new JPanel();
        j1.setBorder(new EmptyBorder(10,10,10,10));//在面板内扣
        j1.setBackground(Color.GREEN);
        j1.setLayout(new BorderLayout());
        j2 = new JPanel();
        j2.setBorder(new EmptyBorder(10,10,10,10));
        j2.setBackground(new Color(245, 212, 111, 163));
        j2.setLayout(new BorderLayout());
        gameP = new JPanel();
        gameP.setBackground(new Color(245, 212, 111, 163));
        gameP.setLayout(new GridLayout(ROW,COLUMN));
        j1.add(j2,BorderLayout.CENTER);
        j2.add(gameP, BorderLayout.CENTER);
        return j1;
    }

    //设置菜单栏
    private JMenuBar setJMB(){
        JMenuBar jb = new JMenuBar();
        JMenu jm1 = new JMenu("开始");
        JMenuItem jmi0 = new JMenuItem("重新开始");
        JMenuItem[] jmis = new JMenuItem[5];
        jmis[0] = new JMenuItem("初级");
        jmis[1] = new JMenuItem("中级");
        jmis[2] = new JMenuItem("高级");
        jmis[3] = new JMenuItem("终级");
        jmis[4] = new JMenuItem("自定义");
        JMenu jm2 = new JMenu("设置");
        JMenuItem jmi21 = new JMenuItem("声音");
        JMenuItem jmi22 = new JMenuItem("胜利动画");
        JMenu jm3 = new JMenu("查看");
        JMenuItem jmi31 = new JMenuItem("帮助");
        JMenuItem jmi32 = new JMenuItem("胜率统计");
        timeLabel = new JLabel("  用时" + seconds + "s");
        timer = new Timer(1000,new MyActionnListener());
        timer.start();//开始计时

        jb.add(jm1);
        jm1.add(jmi0);
        jm1.add(jmis[0]);
        jm1.add(jmis[1]);
        jm1.add(jmis[2]);
        jm1.add(jmis[3]);
        jm1.add(jmis[4]);
        jb.add(jm2);
        jm2.add(jmi21);
        jm2.add(jmi22);
        jb.add(jm3);
        jm3.add(jmi31);
        jm3.add(jmi32);
        jb.add(timeLabel);
        jmi0.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frushButtons();
            }
        });

        for (int i = 0; i < 5; i++) {
            jmis[i].addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    //setVisible(false);
                    dispose();//这个应该可以释放之前frame所占的资源，更好一些
                    new MyJframe(e.getActionCommand(), null);
                }
            });
        }

        jmi21.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new MyJframe("声音设置",null);
            }
        });

        jmi22.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new MyJframe("动画设置",null);
            }
        });

        jmi31.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new MyJframe("帮助",null);
            }
        });

        jmi32.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new MyJframe("胜率统计",null);
            }
        });
        return jb;
    }

    //设置按钮
    private void setButtuns(JPanel gameP,int ROW,int COLUMN){
        jbtns = new JButton[ROW][COLUMN];
        isOpen = new boolean[ROW][COLUMN];
        MyJframe tempF = this;
        for (int i = 0; i < ROW; i++) {
            for (int j = 0; j < COLUMN; j++) {
                if (data[i][j] == VALUE_OF_MINE)countForMines++;
            }
        }

        for (int i = 0; i < ROW; i++){
            for (int j = 0; j < COLUMN; j++){
                JButton jbtn = new JButton();
                gameP.add(jbtn);
                jbtns[i][j] = jbtn;
                isOpen[i][j] = false;
                jbtn.setPreferredSize(new Dimension(25,25));
                jbtn.setMaximumSize(new Dimension(28,28));
                jbtn.setIcon(new ImageIcon("img/按钮.png"));
                int I = i;
                int J = j;
                int finalCountForMines = countForMines;
                jbtn.addMouseListener(new MouseAdapter() {

                    @Override
                    public void mouseClicked(MouseEvent e) {

                        //翻开过的再点击就不起效果了
                        if (e.getButton() == 1 && isOpen[I][J] == false) {
                           /* Dimension d = jbtn.getSize();
                            System.out.println("height = " + d.height);
                            System.out.println("width = " + d.width);
*/
                            //点击之后判定是否胜利
                            if (data[I][J] == VALUE_OF_MINE) {//失败
                                gameTime++;//失败本身就会打开所有格子，所有格子都是true，不必额外设置
                                playVoice("爆炸");
                                timer.stop();
                                for (int ti = 0; ti < ROW; ti++) {
                                    for (int tj = 0; tj < COLUMN; tj++) {
                                        openOneButton(jbtns[ti][tj], ti, tj);
                                    }
                                }
                                new MyJframe("失败", tempF);
                                return;
                            } else {
                                openButtons(I, J, ROW, COLUMN);
                                if ((ROW * COLUMN - countForNotMines) == finalCountForMines) {//成功
                                    gameTime++;
                                    setIsOpenTrue();//都设置为翻开状态避免影响统计结果
                                    playVoice("烟花");
                                    new playAnime("胜利礼花").start();
                                    timer.stop();
                                    if ("终级".equals(tempF.getTitle())) {
                                        JOptionPane.showMessageDialog(null,
                                                "今天可以去买彩票啦","你居然触发了彩蛋③",-1,
                                                new ImageIcon("img/欢呼.jpg"));
                                    }else {
                                        new MyJframe("胜利", tempF);
                                    }
                                    return;
                                }
                            }
                            playVoice("按钮");
                            return;
                        }else if (e.getButton() == 3){
                            playVoice("按钮");
                            if (jbtn.getIcon().toString() == "img/按钮.png"){
                                jbtn.setIcon(new ImageIcon("img/红旗.png"));
                            }else if (jbtn.getIcon().toString() == "img/红旗.png"){
                                jbtn.setIcon(new ImageIcon("img/？.png"));
                            }else if (jbtn.getIcon().toString() == "img/？.png"){
                                jbtn.setIcon(new ImageIcon("img/按钮.png"));
                            }
                        }
                    }

                    @Override
                    public void mouseEntered(MouseEvent me) {
                        //鼠标进入后可以给两个特定变量传值
                        int x = me.getComponent().getX();
                        int y = me.getComponent().getY();
                        tIForSaveVal = y/((JButton)me.getSource()).getSize().height;
                        tJForSaveVal = x/((JButton)me.getSource()).getSize().width;
                        //System.out.println("("+tIForSaveVal+","+tJForSaveVal+")");
                    }
                });
                //与mouseEntered结合实现鼠标悬停按键开格子的功能
                jbtn.addKeyListener(new KeyAdapter() {
                    @Override
                    public void keyTyped(KeyEvent e) {
                        //等同于鼠标左键
                        if (e.getKeyChar() == 'd' && isOpen[tIForSaveVal][tJForSaveVal] == false){
                            if (data[tIForSaveVal][tJForSaveVal] == VALUE_OF_MINE) {//失败
                                gameTime++;
                                playVoice("爆炸");
                                timer.stop();
                                for (int ti = 0; ti < ROW; ti++) {
                                    for (int tj = 0; tj < COLUMN; tj++) {
                                        openOneButton(jbtns[ti][tj], ti, tj);
                                    }
                                }
                                new MyJframe("失败", tempF);
                            } else {
                                openButtons(tIForSaveVal, tJForSaveVal, ROW, COLUMN);
                                if ((ROW * COLUMN - countForNotMines) == finalCountForMines){//胜利
                                    timer.stop();
                                    gameTime++;
                                    setIsOpenTrue();
                                    if ("终级".equals(tempF.getTitle())) {
                                        JOptionPane.showMessageDialog(null,
                                                "今天可以去买彩票啦",null,-1,
                                                new ImageIcon("img/欢呼.jpg"));
                                    }else {
                                        playVoice("烟花");
                                        new playAnime("胜利礼花").start();
                                        new MyJframe("胜利", tempF);
                                    }
                                }
                            }
                            playVoice("按钮");
                            return;
                        }else if (e.getKeyChar() == 'f'){
                            playVoice("按钮");
                            if (jbtns[tIForSaveVal][tJForSaveVal].getIcon().toString() == "img/按钮.png"){
                                jbtns[tIForSaveVal][tJForSaveVal].setIcon(new ImageIcon("img/红旗.png"));
                            }else if (jbtns[tIForSaveVal][tJForSaveVal].getIcon().toString() == "img/红旗.png"){
                                jbtns[tIForSaveVal][tJForSaveVal].setIcon(new ImageIcon("img/？.png"));
                            }else if (jbtns[tIForSaveVal][tJForSaveVal].getIcon().toString() == "img/？.png"){
                                jbtns[tIForSaveVal][tJForSaveVal].setIcon(new ImageIcon("img/按钮.png"));
                            }
                        }else if (e.getKeyChar() == 'r'){//加一个用r刷新的功能
                            frushButtons();
                        }
                    }
                });
            }
        }
    }

    //刷新按钮和地雷
    private void frushButtons(){
        timer.stop();
        seconds = 0;//归0
        timer.start();
        int tempRow = this.jbtns.length;
        int tempCol = this.jbtns[0].length;
        int tempMineNums = this.countForMines;
        this.countForNotMines = 0;
        this.setMines(tempMineNums,tempRow,tempCol);

        for (int i = 0; i < tempRow; i++){
            for (int j = 0; j < tempCol; j++){
                this.jbtns[i][j].setIcon(new ImageIcon("img/按钮.png"));
                jbtns[i][j].setBackground(new Color(179, 179, 179, 255));
                isOpen[i][j] = false;//所有按钮的值设置为未翻开状态
                //已经在初始化的时候加过监听器了，所以这里不用加，否则会出现越来越多的确认窗口
                //也就是说这里只需要重置按钮状态就行
            }
        }
    }

    //翻开格子的动作,可以调试格子翻开后的颜色
    private void openOneButton(JButton jButton,int i, int j){
        //Button.setEnabled(false);//设置之后图片会失去色彩
        jButton.setOpaque(true);////isOpaque: true为不透明，false为透明
        jButton.setBackground(new Color(179, 179, 179, 255));
        isOpen[i][j] = true;
        if (data[i][j] != VALUE_OF_MINE)countForNotMines++;//统计非雷数

        if (data[i][j] == VALUE_OF_MINE){
            jButton.setIcon(new ImageIcon("img/地雷.png"));
            jButton.setBackground(Color.red);
        }else {
            if (data[i][j] == 0)jButton.setIcon(null);
            if (data[i][j] == 1)jButton.setIcon(new ImageIcon("img/1.png"));
            if (data[i][j] == 2)jButton.setIcon(new ImageIcon("img/2.png"));
            if (data[i][j] == 3)jButton.setIcon(new ImageIcon("img/3.png"));
            if (data[i][j] == 4)jButton.setIcon(new ImageIcon("img/4.png"));
            if (data[i][j] == 5)jButton.setIcon(new ImageIcon("img/5.png"));
            if (data[i][j] == 6)jButton.setIcon(new ImageIcon("img/6.png"));
            if (data[i][j] == 7)jButton.setIcon(new ImageIcon("img/7.png"));
            if (data[i][j] == 8)jButton.setIcon(new ImageIcon("img/8.png"));
        }

    }

    //所有按钮的值设置为翻开状态
    private  void setIsOpenTrue(){
        for (int i = 0; i < isOpen.length; i++){
            for (int j = 0; j < isOpen[0].length; j++){
                isOpen[i][j] = true;
            }
        }
    }

    //格子的级联打开
    private void openButtons(int i, int j, int ROW, int COLUNM){
        JButton jbtn = jbtns[i][j];
        /*
            下面这行代码很重要，必须放在这个位置，因为是一个格子是空格就会打开其周围一圈的格子，
            然后每个格子再递归，如果不先判断格子是否已经翻开，之前翻开过的格子会继续递归，光是
            相邻的两个格子就会无限互相递归地翻开。会出现StackOverflowError异常。
        */
        if (isOpen[i][j])return;//按钮没翻开就继续往下执行为翻开,翻开过的不会再执行oneButton()方法，非雷数只会计一次
        openOneButton(jbtn,i,j);

        if (data[i][j] == 0){//首先中心格子是0，然后判断其周围一圈的格子，不是雷就翻开
            if (i > 0 && j > 0 && data[i-1][j-1] != VALUE_OF_MINE)openButtons(i-1,j-1,ROW,COLUNM);
            if (i > 0 && data[i-1][j] != VALUE_OF_MINE)openButtons(i-1,j,ROW,COLUNM);
            if (i > 0 && j < COLUNM-1 && data[i-1][j+1] != VALUE_OF_MINE)openButtons(i-1,j+1,ROW,COLUNM);
            if (j > 0 && data[i][j-1] != VALUE_OF_MINE)openButtons(i,j-1,ROW,COLUNM);
            if (j < COLUNM-1 && data[i][j+1] != VALUE_OF_MINE)openButtons(i,j+1,ROW,COLUNM);
            if (i < ROW-1 && j > 0 && data[i+1][j-1] != VALUE_OF_MINE)openButtons(i+1,j-1,ROW,COLUNM);
            if (i < ROW-1 && data[i+1][j] != VALUE_OF_MINE)openButtons(i+1,j,ROW,COLUNM);
            if (i < ROW-1 && j < COLUNM-1 && data[i+1][j+1] != VALUE_OF_MINE)openButtons(i+1,j+1,ROW,COLUNM);
        }

    }

    //生成炸弹
    private void setMines(int mineNums,int ROW,int COLUNM){
        data = new int[ROW][COLUNM];
        Random random = new Random();
        for (int i = 0; i < mineNums; ){
            int r = random.nextInt(ROW);
            int c = random.nextInt(COLUNM);
            if (data[r][c] != VALUE_OF_MINE){
                data[r][c] = VALUE_OF_MINE;
                i++;//埋了一颗才能计数；
            }
        }

        //计算周边雷的数量
        for (int i = 0; i < ROW; i++){
            for (int j = 0; j < COLUNM; j++){
                if (data[i][j] == VALUE_OF_MINE) continue;
                int count = 0;
                if (i > 0 && j > 0 && data[i-1][j-1] == VALUE_OF_MINE)count++;//有左上角的雷才执行计数
                if (i > 0 && data[i-1][j] == VALUE_OF_MINE)count++;
                if (i > 0 && j < COLUNM-1 && data[i-1][j+1] == VALUE_OF_MINE)count++;//有右上角的雷才记
                if (j > 0 && data[i][j-1] == VALUE_OF_MINE)count++;
                if (j < COLUNM-1 && data[i][j+1] == VALUE_OF_MINE)count++;
                if (i < ROW-1 && j > 0 && data[i+1][j-1] == VALUE_OF_MINE)count++;
                if (i < ROW-1 && data[i+1][j] == VALUE_OF_MINE)count++;
                if (i < ROW-1 && j < COLUNM-1 && data[i+1][j+1] == VALUE_OF_MINE)count++;
                data[i][j] = count;
            }
        }
    }

    //监听器类
    class MyActionnListener implements ActionListener {
        private TextField ROWField;
        private TextField COLFiled;
        private TextField MINEFiled;
        private int ROW;
        private int COLUMN;
        private int MineNums;
        private MyJframe myJframe;

        public MyActionnListener() {
        }

        public MyActionnListener(MyJframe myJframe, TextField ROWField, TextField COLFiled, TextField MINEFiled) {
            this.myJframe = myJframe;
            this.ROWField = ROWField;
            this.COLFiled = COLFiled;
            this.MINEFiled = MINEFiled;
        }

        //timer线程执行的内容
        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() instanceof Timer){
                timeLabel.setText("  用时" + ++seconds + "s");
                timer.start();//让其反复执行
            }

            if ("提交".equals(e.getActionCommand())){
                try {
                    ROW = Integer.parseInt(ROWField.getText());
                    COLUMN = Integer.parseInt(COLFiled.getText());
                    MineNums = Integer.parseInt(MINEFiled.getText());
                    //临时注释：可以在判定胜利的环节判断弹出的彩蛋窗口
                    if (ROW == 1 && COLUMN ==1 && MineNums ==0){
                        MyJframe mF1 = new MyJframe(MineNums,ROW,COLUMN);
                        mF1.setTitle("恭喜你触发了彩蛋①");
                        mF1.setBounds(533,140,300,346);
                    }else if ((ROW == 1 && COLUMN ==1 && MineNums == 1)) {
                        MyJframe mF2 = new MyJframe(MineNums,ROW,COLUMN);
                        mF2.setTitle("恭喜你触发了彩蛋②");
                        mF2.setBounds(533,140,300,346);
                    }else {
                        if (ROW > 0 &&ROW < 28 && COLUMN < 54 && COLUMN > 3 && MineNums > -1 && MineNums < ROW*COLUMN){
                            new MyJframe(MineNums,ROW,COLUMN);
                        }else {
                            JOptionPane.showMessageDialog(null,
                                    "行的输入值【建议】在1~27\n列的输入值【建议】在4~53\n地雷数不应该超过格子总数",
                                    "来自作者善意的提醒",JOptionPane.WARNING_MESSAGE,
                                    new ImageIcon("img/小人.png"));
                        }
                    }
                }catch (NumberFormatException exception){
                    if ("".equals(ROWField.getText()) || "".equals(COLFiled.getText()) || "".equals(MINEFiled.getText())){
                        JOptionPane.showMessageDialog(null,"您应该知道三个格子都要填吧","来自作者的提醒",JOptionPane.WARNING_MESSAGE);
                    }else{
                        JOptionPane.showMessageDialog(null, "你有以为自己卡到bug的错觉吗？",
                                "来自憨憨作者的嘲笑", JOptionPane.PLAIN_MESSAGE, new ImageIcon("img/小人.png"));
                    }
                }
            }
        }
    }

    //音效
    private void playVoice(String wavFileName) {
        if (voiceFlag == 0) {
            String name = "voice/" + wavFileName + ".wav";
            Clip c;
            try {
                c = AudioSystem.getClip();
                c.open(AudioSystem.getAudioInputStream(new File(name)));
                if (wavFileName.equals("按钮") || wavFileName.equals("烟花") || wavFileName.equals("爆炸")) {
                    c.loop(0);
                } else {
                    c.loop(2);
                }
            } catch (LineUnavailableException e) {
                e.printStackTrace();
            } catch (UnsupportedAudioFileException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //播放动画
    class playAnime extends Thread{
        String gifName;
        public playAnime(String gifName){
            this.gifName = gifName;
        }
        @Override
        public void run() {
            if (animeFlag == 0) {
                String name = "img/" + gifName + ".gif";
                JFrame jFrame = new JFrame();
                jFrame.setUndecorated(true);//设置是否有边框修饰
                JLabel jLabel = new JLabel(new ImageIcon(name));
                jFrame.add(jLabel);
                AWTUtilities.setWindowOpacity(jFrame, 0.75f);//设置透明度
                jFrame.setBounds(419, 229, 527, 271);
                jFrame.setVisible(true);

                try {
                    sleep(2500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                jFrame.dispose();
                //run()方法执行完，线程会自动退出，然后已生成的frame也会释放
            }
        }
    }

    //初始化胜率相关数据
    private void initData(){
        //resourceboundle的getBundle写的是resources下的材料的目录
        gameTime = Integer.parseInt(bundle.getString("gameTime"));
        winTime = Integer.parseInt(bundle.getString("winTime"));
        winRate = Double.parseDouble(bundle.getString("winRate"));

    }

    //写入胜率,应该在关闭窗口后保存,初始窗口，自定义窗口与生成的其他游戏窗口需要加这个监听器
    private void writeWinRate(JFrame jFrame){
        jFrame.addWindowListener(new WindowAdapter() {
            /*
            Closing在程序停止前执行，也就是点x号可以执行，但是新建其他窗口的时候（这时候代码会销毁这个窗口）
            并不会执行保存动作。
            Closed在程序停止后执行，代码销毁窗口的时候可以执行保存动作，但是点击关闭窗口的时候不会执行保存
            综上所述，选用Closing事件比较合适。
             */
            @Override
            public void windowClosing(WindowEvent e) {
                OutputStream out = null;
                try {
                    out = new FileOutputStream("winRate.properties");
                    byte[] bytes ;
                    bytes = ("gameTime = " + gameTime + "\n" +
                            "winTime = " + winTime + "\n" +
                            "winRate = " + winRate + "\n").getBytes();
                    out.write(bytes);
                } catch (Exception exception) {
                    exception.printStackTrace();
                } finally {
                    if (out != null){
                        try {
                            out.close();
                        } catch (IOException exception) {
                            exception.printStackTrace();
                        }
                    }
                }
            }
        });

    }

}


