package frame;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.WindowAdapter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class LoginFrame extends BaseFrame{
	JTextField tfId = new JTextField(14), tfPw = new JTextField(14);
	JLabel signUp = createLabel("회원가입",null,JLabel.RIGHT);
	
	public LoginFrame() {
		super("로그인", 360, 180);
		
		JPanel gridPanel = new JPanel(new GridLayout(0,2,-90,20));
		gridPanel.add(createLabel("   아이디"));
		gridPanel.add(tfId);
		gridPanel.add(createLabel("비밀번호"));
		gridPanel.add(tfPw);
		
		JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT,10,0));
		centerPanel.add(gridPanel);
		centerPanel.add(createComponent(createButtonWithoutMargin("로그인", e->login()),70,80));
		
		addWindowListener(new WindowAdapter() {
			
			public void windowOpened(java.awt.event.WindowEvent e) {
				stack.push(LoginFrame.this);
			};
		});
		
		signUp.addMouseListener(new MouseAdapter() {
			public void mouseClicked(java.awt.event.MouseEvent e) {
				if(SwingUtilities.isLeftMouseButton(e)) {
					openFrame(new SignUpFrame());
				}
			};
		});
		
		add(createLabel("기능마켓", new Font("맑은 고딕",Font.BOLD,24), JLabel.CENTER),BorderLayout.NORTH);
		add(centerPanel,BorderLayout.CENTER);
		add(signUp,BorderLayout.SOUTH);
	}
	
	private void login() {
		
		String id = tfId.getText();
		String pw = tfPw.getText();
		
		if(id.isEmpty() || pw.isEmpty()) {
			informMessage("빈칸이 존재합니다.");
			return;
		}
		
		if(id.equals("admin") && pw.equals("1234")) {
			openFrame(new ProductManagementFrame());
			return;
		}
		
		try (PreparedStatement pst = conn.prepareStatement("select * from user where u_id = ? and u_pw = ?")){
			pst.setObject(1, id);
			pst.setObject(2, pw);
			ResultSet rs = pst.executeQuery();
			if(rs.next()) {
				user10percent = rs.getInt("u_10percent");
				user30percent = rs.getInt("u_30percent");
				uNo = rs.getInt("u_no");
				uName = rs.getString("u_Name");
				informMessage(uName+"님 환영합니다.");
				openFrame(new ProductListFrame());
			}else {
				errorMessage("회원정보가 일치하지 않습니다.");
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	@Override
	public void closeFrame() {
		System.exit(0);
	}
	
	public static void main(String[] args) {
		new LoginFrame().setVisible(true);
	}
}
