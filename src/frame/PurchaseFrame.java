package frame;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;

public class PurchaseFrame extends BaseFrame{
	JTextField tfs[] = new JTextField[3];
	JCheckBox tenPercent,thirtyPercent;
	JTextArea taEx = createComponent(new JTextArea(), 300, 150,300,150);
	ArrayList<ImagePanel> list = new ArrayList<ImagePanel>();
	JLabel selectedImage;
	public PurchaseFrame(String pName) {
		super("구매", 800, 600);
		
		setLayout(null);
		
		for (int i = 0; i < tfs.length; i++) {
			tfs[i] = createComponent(new JTextField(),220,30);
		}
		
		tfs[0].setEnabled(false);
		tfs[1].setEnabled(false);
		taEx.setLineWrap(true);
		taEx.setBorder(new LineBorder(Color.black));
		taEx.setEnabled(false);
		
		selectedImage = createComponent(new JLabel(getImage(pName, 200,200)),5,10,200,200);
		
		tenPercent = new JCheckBox("10% 할인 쿠폰 적용");
		thirtyPercent = new JCheckBox("30% 할인 쿠폰 적용");
		
		JPanel gridpPanel = createComponent(new JPanel(new FlowLayout(FlowLayout.LEFT,10,20)),240,5,300,200);
		gridpPanel.add(createLabel("제품명"));
		gridpPanel.add(tfs[0]);
		gridpPanel.add(createLabel("가격   "));
		gridpPanel.add(tfs[1]);
		gridpPanel.add(tenPercent);
		gridpPanel.add(thirtyPercent);
		gridpPanel.add(createLabel("수량   "));
		gridpPanel.add(tfs[2]);
		
		JPanel btnPanel = createComponent(new JPanel(new FlowLayout()),460,250,200,40);
		btnPanel.add(createComponent(createButton("구매하기", null),80,30));
		btnPanel.add(createComponent(createButton("취소하기", null),80,30));
		
		JPanel explanationPanel = createComponent(new JPanel(new BorderLayout()),10,230,400,150);
		explanationPanel.add(createLabel("상품 설명",null,JLabel.LEFT),BorderLayout.NORTH);
		explanationPanel.add(taEx,BorderLayout.CENTER);
		
		JPanel imageSlidePanel = createComponent(new JPanel(new BorderLayout()), 10, 400,760,200);
		JPanel slidePanel = new JPanel(null);
		try (PreparedStatement pst = conn.prepareStatement(
				"select * from product \r\n"
				+ "where c_no = (select c_no from product where p_name = ?) and p_name != ?")){
			pst.setObject(1, pName);
			pst.setObject(2, pName);
			ResultSet rs = pst.executeQuery();
			int i=0;
			while(rs.next()) {
				ImagePanel ip = createComponent(new ImagePanel(rs.getString("p_name")),i*100,0,100,130);
				list.add(ip);
				slidePanel.add(ip);
				i++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		new Thread(()->{
			while(true) {
				try {
					Thread.sleep(1000);
					for(int i = 0;i<100;i++) {
						for (int j = 0; j < list.size(); j++) {
							list.get(j).setLocation(list.get(j).getLocation().x - 1, 0);
							if(list.get(j).getLocation().x <= -100) 
								list.get(j).setLocation(list.size() * 100 - 100,0); // 
						}
						Thread.sleep(3);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
		
		imageSlidePanel.add(createLabel("같은 카테고리 목록",null,JLabel.LEFT),BorderLayout.NORTH);
		imageSlidePanel.add(slidePanel,BorderLayout.CENTER);
		
		setMenuInfo(pName);
		
		add(selectedImage);
		add(gridpPanel);
		add(explanationPanel);
		add(btnPanel);
		add(imageSlidePanel);
	}
	
	private void setMenuInfo(String pName) {
		try (PreparedStatement pst = conn.prepareStatement("select * from product where p_name = ?")){
			pst.setObject(1, pName);
			ResultSet rs = pst.executeQuery();
			if(rs.next()) {
				tfs[0].setText(rs.getString("p_name"));
				tfs[1].setText(String.format("%,d", rs.getInt("p_price")));
				taEx.setText(rs.getString("p_explanation"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	class ImagePanel extends JPanel{
		public ImagePanel(String pName) {
			super(new BorderLayout());
			JLabel image = new JLabel(getImage(pName, 100,130));
			
			addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					selectedImage = image;
					setMenuInfo(pName);
				}
			});
			setBorder(new LineBorder(Color.black));
			add(image,BorderLayout.CENTER);
			add(createLabel(pName,null,JLabel.CENTER),BorderLayout.SOUTH);
		}
	}
	
	@Override
	public void closeFrame() {
		previousFrame();
	}
	
	public static void main(String[] args) {
		new PurchaseFrame("4~6cm 등심").setVisible(true);
	}
}
