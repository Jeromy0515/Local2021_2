package frame;

import java.awt.GridLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.PreparedStatement;
import java.time.LocalDate;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class SignUpFrame extends BaseFrame{
	JTextField tfs[] = new JTextField[6]; //이름, 아이디, 비밀번호, 비밀번호체크, 전화번호, 생년월일
	JButton signUpBtn = createComponent(createButtonWithoutMargin("회원가입", e->signUp()), 220,300,80,30);
	boolean checkOverlap = false;
	
	public SignUpFrame() {
		super("회원가입",400,400);
		setLayout(null);
		String txt[] = {"이름:","아이디:","비밀번호","비밀번호체크:","전화번호","생년월일:"};
		
		JPanel gridPanel = createComponent(new JPanel(new GridLayout(0,2,-80,20)),10, 10, 270, 250);
		
		for (int i = 0; i < tfs.length; i++) {
			tfs[i] = new JTextField(14);
			gridPanel.add(createLabel(txt[i]));
			gridPanel.add(tfs[i]);
		}
		
		for (int i = 0; i < tfs.length; i++) {
			tfs[i].addKeyListener(new KeyAdapter() {
				@Override
				public void keyReleased(KeyEvent e) {
					for (int j = 0; j < tfs.length; j++) {
						if(tfs[j].getText().isEmpty()) {
							signUpBtn.setEnabled(false);
							return;
						}
					}
					signUpBtn.setEnabled(true);
				}
			});
		}
		
		tfs[1].addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				checkOverlap = false;
			}
		});
		
		tfs[4].addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				if(tfs[4].getText().length() > 12) {
					e.consume();
					return;
				}
				
				if(e.getKeyChar() >= 48 && e.getKeyChar() <= 57 || e.getKeyChar() == 8) { // 숫자와 백스페이스키만 입력받음
					if(e.getKeyChar() != 8) { // 8 = backspace 숫자일경우 일정 길이마다 - 붙여주기
						if(tfs[4].getText().length() == 3)
							tfs[4].setText(tfs[4].getText()+"-");
						if(tfs[4].getText().length() == 8)
							tfs[4].setText(tfs[4].getText()+"-");
					}			
				}else {
				errorMessage("문자는 입력이 불가합니다.");
				e.consume();
				tfs[4].setText("");
				tfs[4].requestFocus();
			}
				
				
			}
		});
	
		signUpBtn.setEnabled(false);
		
		add(gridPanel);
		add(createComponent(createButtonWithoutMargin("중복확인", e->overlap()), 285,50,80,30));
		add(signUpBtn);
		add(createComponent(createButtonWithoutMargin("취소", e->previousFrame()), 310,300,60,30));
		
	}
	
	private void overlap(){
		String id = tfs[1].getText().trim();
		if(id.isEmpty()) {
			errorMessage("아이디를 입력하세요.");
			return;
		}
		
		try (PreparedStatement pst = conn.prepareStatement("select * from user where u_id = ?")){
			pst.setObject(1, id);
			if(pst.executeQuery().next()) {
				errorMessage("이미 존재하는 아이디입니다.");
				tfs[1].setText("");
				tfs[1].requestFocus();
				return;
			}else {
				informMessage("사용가능한 아이디입니다.");
				checkOverlap = true;
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	private void signUp(){
		if(!checkOverlap) {
			errorMessage("아이디 중복확인을 해주세요.");
			return;
		}
		
		if(!tfs[2].getText().equals(tfs[3].getText())) {
			errorMessage("비밀번호가 일치하지 않습니다.");
			return;
		}
		
		if(!tfs[2].getText().matches("(?=.*[A-z])(?=.*[^A-z1-9])(?=.*[0-9]).{4,}")) {
			errorMessage("비밀번호를 확인해주세요.");
			return;
		}
		
		try {
			String arr[] = tfs[5].getText().split("-");
			LocalDate date = LocalDate.parse(String.format("%d-%02d-%02d",Integer.parseInt(arr[0]),Integer.parseInt(arr[1]),Integer.parseInt(arr[2])));
			if(date.isAfter(LocalDate.now())) {
				throw new Exception();
			}
		} catch (Exception e) {
			errorMessage("생년월일을 확인해주세요.");
			return;
		}
		
		try (PreparedStatement pst = conn.prepareStatement("insert into user values(0,?,?,?,?,?,0,0)")){
			int cnt = 1;
			for(int i:new int[]{1,2,0,4,5}) {
				pst.setObject(cnt, tfs[i].getText());
				cnt++;
			}
			
			pst.execute();
			
			informMessage("회원가입이 완료되었습니다.");
			
			openFrame(new LoginFrame());
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void closeFrame() {
		previousFrame();
	}
	
}
