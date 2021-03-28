package frame;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.time.LocalDateTime;
import java.util.Stack;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.WindowConstants;

abstract public class BaseFrame extends JFrame{
	
	static LocalDateTime now = LocalDateTime.now();
	
	static String uName;
	
	static Connection conn = null;
	static {
		try {
			conn = DriverManager.getConnection("jdbc:mysql://localhost/2021지방_1?serverTimezone=UTC","user","1234");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	static Stack<JFrame> stack = new Stack<JFrame>();
	
	public BaseFrame(String title,int width,int height) {
		super(title);
		setSize(width,height);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				closeFrame();
			}
		});
	}
	
	
	
	public static JLabel createLabel(String text, Font font) {
		JLabel label = new JLabel(text);
		label.setFont(font);
		return label;
	}
	
	public static JLabel createLabel(String text) {
		JLabel label = new JLabel(text);
		label.setFont(new Font("굴림",Font.BOLD,12));
		return label;
	}
	
	public static JLabel createLabel(String text,Font font,int alig) {
		JLabel label;
		if(font != null) 
			label = createLabel(text,font);
		else
			label = createLabel(text);
		label.setHorizontalAlignment(alig);
		return label;
	}
	
	
	public static JButton createButton(String text,ActionListener act) {
		JButton button = new JButton(text);
		button.addActionListener(act);
		button.setMargin(new Insets(0,0,0,0));
		return button;
	}
	
	public static <T extends JComponent> T createComponent(T comp,int width,int height) {
		comp.setPreferredSize(new Dimension(width,height));
		return comp;
	}
	
	public static <T extends JComponent> T createComponent(T comp,int x,int y,int width,int height) {
		comp.setBounds(x,y,width,height);
		return comp;
	}
	
	public static void informMessage(String caption) {
		JOptionPane.showMessageDialog(null, caption,"정보",JOptionPane.INFORMATION_MESSAGE);
	}
	
	public static void errorMessage(String caption) {
		JOptionPane.showMessageDialog(null, caption,"경고",JOptionPane.ERROR_MESSAGE);
	}
	
	public static int confirmMeseage(String caption,String title) {
		return JOptionPane.showConfirmDialog(null, caption,title,JOptionPane.YES_NO_OPTION);
	}
	
	public static ImageIcon getImage(String imageName,int width,int height) {
		return new ImageIcon(
				Toolkit.getDefaultToolkit().getImage("./제2과제 datafile/이미지/"+imageName+".jpg").getScaledInstance(width,height,Image.SCALE_SMOOTH));
	}
	
	public void previousFrame() {
		dispose();
		stack.pop();
		stack.peek().setVisible(true);
	}
	
	public void openFrame(JFrame frame) {
		dispose();
		stack.push(frame);
		stack.peek().setVisible(true);
	}
	
	abstract public void closeFrame();
	
}
