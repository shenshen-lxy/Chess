package chessAI;

import java.awt.BorderLayout;
import java.awt.event.*;
import javax.swing.*;
import java.awt.Dimension;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;

/**
 * 五子棋视图类
 * 负责棋盘显示以及与用户交互
 *
 */
public class UI {
	//窗口对象
	JFrame frame;
	
	//棋盘控制器对象
	Chess chess = new Chess();
	
	//棋盘面板对象
	ChessPanel chessPanel;
	
	/**
	 * 创建窗口
	 * 绑定事件监听
	 */
	public void create() {
		
		//初始化窗口
		frame = new JFrame("人机对弈五子棋");
		
		//初始化棋盘面板以及添加
		chessPanel = new ChessPanel();
		frame.add(chessPanel);
		
		//初始化棋盘控制器
		int yes=JOptionPane.showConfirmDialog(null,"是否加载上次保存的棋局？","Question",JOptionPane.YES_NO_OPTION );
		if(yes==JOptionPane.YES_OPTION) {
			readBoard();
		}

		// 顶部工具栏
		JToolBar bar = new JToolBar();//创建工具栏
		frame.add(bar, BorderLayout.NORTH);//添加
		bar.setBorderPainted(false);//设置工具栏不画边框
		
		//第一个工具：保存
		Icon baocunicon = new ImageIcon(UI.class.getResource("/image/save.png"));//Icon
		JButton errorAction = new JButton(" 保存   ", baocunicon);//Action
		errorAction.setOpaque(true);//透明
		errorAction.setFocusPainted(false);//去掉焦点框
		errorAction.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
					saveBoard();
			}
		});
		bar.add(errorAction);//添加Action
		
		//第二个工具：重开一局
		Icon icon = new ImageIcon(UI.class.getResource("/image/restart.png"));//Icon
		JButton restartAction = new JButton("重新开始", icon);//Action
		restartAction.setToolTipText("重新开始");
		restartAction.setOpaque(true);//透明
		restartAction.setFocusPainted(false);//去掉焦点框
		restartAction.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				restartBoard();//重开棋局
			}
		});
		bar.add(restartAction);//添加Action
		JLabel lb1 = new JLabel("                                                                 ");
		bar.add(lb1);
		
		//第三个工具：玩家先手
		Icon shouxianIcon = new ImageIcon(UI.class.getResource("/image/shouxian.png"));
		final JButton firstAction = new JButton("玩家先手", shouxianIcon);
		firstAction.setOpaque(true);
		firstAction.setBorderPainted(true);
		firstAction.setFocusPainted(false);
		firstAction.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				
				if(firstAction.getText().equals("玩家先手")){
					firstAction.setText("机器先手");
					//使用棋局控制器设置先手
					Chess.first=chess.AI;
				}else{
					firstAction.setText("玩家先手");
					//使用棋局控制器设置先手
					Chess.first=chess.PLAYER;
				}
				
			}
		});
		bar.add(firstAction);

		//为棋盘面板设置鼠标监听事件
		chessPanel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				showChess(chessPanel, e);
			}
		});
		
		//设置frame的相关属性
		frame.setSize(476, 532);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		frame.setResizable(false);
	}

	/**
	 * 棋局重开事件处理函数
	 */
	public void restartBoard(){
		chess.restart();//棋盘控制器初始化棋盘
		chessPanel.clearBoard();//棋盘view清除棋子重绘
		if(Chess.first==Chess.AI){
			//如果AI先手，AI需要先落子
			Location location=chess.start();
			chess.play(location.getX(), location.getY(),Chess.AI);
			//棋盘面板控制落子的显示
			chessPanel.doPlay(location.getX(),location.getY(), Chess.AI);
		}
	}
	
	//保存棋局的函数
	public void saveBoard() {
		try {
	    	 FileOutputStream f1=new FileOutputStream("savechess.txt",false);
	    	 FileOutputStream f2=new FileOutputStream("savelist.txt",false);
	    	 ObjectOutputStream out1=new ObjectOutputStream(f1);
	    	 ObjectOutputStream out2=new ObjectOutputStream(f2);
	    	 out1.writeObject(chess);
	    	 out2.writeObject(chessPanel.list);
	    	 out1.close();
	    	 out2.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	//读取保存的棋局
	public void readBoard() {
		try {
			FileInputStream f1=new FileInputStream("savechess.txt");
			FileInputStream f2=new FileInputStream("savelist.txt");
			ObjectInputStream in1=new ObjectInputStream(f1);
			ObjectInputStream in2=new ObjectInputStream(f2);
			chess=(Chess)in1.readObject();
			chessPanel.list=(List<Location>)in2.readObject();
			in1.close();
			in2.close();
			System.out.println("已读取上次保存的棋局");
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 棋盘面板的鼠标点击事件
	 * @param chessPanel
	 * @param e
	 */
	public void showChess(ChessPanel chessPanel, MouseEvent e) {
		//点击的位置坐标
		int x = e.getX();
		int y = e.getY();
		
		//转化为棋盘上的行列值
		int col = (x - 5) / 30;
		int row = (y - 5) / 30;
		
		//玩家落子有效
		boolean isEnable = chess.play(row, col,Chess.PLAYER);
		if (isEnable) {
			// 棋盘面板绘制棋子
			chessPanel.doPlay(row, col, Chess.PLAYER);
			
			//玩家胜利
			if (chess.isWin(row, col, Chess.PLAYER)){
				JOptionPane.showMessageDialog(frame, "人获胜", "恭喜玩家胜利了！",JOptionPane.WARNING_MESSAGE);  
				restartBoard();//初始化棋盘
				return;
			}
			
			//棋局控制器分析后获取落子位置
			Location result = chess.explore();
			
			//棋盘控制器控制AI落子
			chess.play(result.getX(), result.getY(),Chess.AI);
			
			// 棋盘面板绘制棋子
			chessPanel.doPlay(result.getX(), result.getY(), Chess.AI);
			
			//AI胜利
			if (chess.isWin(result.getX(), result.getY(),Chess.AI)){
				JOptionPane.showMessageDialog(frame, "机器获胜", "你输给了机器了！",JOptionPane.WARNING_MESSAGE); 
				restartBoard();
				return;
			}
			
		} else System.out.println("坐标无效!");
	}
}
