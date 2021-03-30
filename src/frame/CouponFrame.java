package frame;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;

import javax.swing.JLabel;
import javax.swing.JPanel;

public class CouponFrame extends BaseFrame{
	public CouponFrame(boolean moreThanFive) {
		super("쿠폰", 620, 290);
		setLayout(null);
		
		JPanel imagePanel = createComponent(new JPanel() {
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				g.drawImage(getImage("쿠폰", 600, 250).getImage(), 0,0,null);				
			}
		},0,0,620,290);
		
		JPanel mainPanel = createComponent(new JPanel(new FlowLayout(FlowLayout.CENTER)),50,25,500,150);
		mainPanel.setBackground(new Color(0,0,0,0)); // Jpanel 투명하게
		if(moreThanFive)
			mainPanel.add(createLabel("10%, 30% 할인 쿠폰",new Font(null,1,50),JLabel.CENTER));
		else 
			mainPanel.add(createLabel("10% 할인 쿠폰",new Font(null,1,50),JLabel.CENTER));
		
		mainPanel.add(createLabel("<html><p color=red>쿠폰이 발급되었습니다.</p></html>",new Font("굴림",1,20),JLabel.CENTER));
		
		
		add(createComponent(createLabel("고객명: "+uName), 90, 150,150,20));
		add(createComponent(createLabel("발급날짜: "+now.toString()), 400, 150,200,20));
		add(createComponent(createLabel("사용기한: "+now.plusMonths(1).toString()), 400, 180,200,20));
		add(mainPanel);
		add(imagePanel);
		
	}
	
	@Override
	public void closeFrame() {
		previousFrame();
	}
	
	public static void main(String[] args) {
		new CouponFrame(true).setVisible(true);
	}
}
