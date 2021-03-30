package frame;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;

import model.MenuInform;

public class PurchaseFrame extends BaseFrame{
	JTextField tfs[] = new JTextField[3];
	JCheckBox tenPercent,thirtyPercent;
	JTextArea taEx = createComponent(new JTextArea(), 300, 150,300,150);
	ArrayList<ImagePanel> list = new ArrayList<ImagePanel>();
	JLabel selectedImage;
	JPanel slidePanel = new JPanel(null);
	MenuInform mi;
	
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
		btnPanel.add(createComponent(createButtonWithoutMargin("구매하기", e->buy()),80,30));
		btnPanel.add(createComponent(createButtonWithoutMargin("취소하기", e->previousFrame()),80,30));
		
		JPanel explanationPanel = createComponent(new JPanel(new BorderLayout()),10,230,400,150);
		explanationPanel.add(createLabel("상품 설명",null,JLabel.LEFT),BorderLayout.NORTH);
		explanationPanel.add(taEx,BorderLayout.CENTER);
		
		JPanel imageSlidePanel = createComponent(new JPanel(new BorderLayout()), 10, 400,760,200);
		setImageSlidePanel(pName);
		
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
		mi = getSelectedMenu(pName);
		
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
	
	private void setImageSlidePanel(String pName) {
		slidePanel.removeAll();
		list.clear();
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
		revalidate();
	}
	
	private void buy() {
		int vol;
		int coupon = 0;
		String nowDate = now.toString();  //format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		
		try {
			 vol = Integer.parseInt(tfs[2].getText()); // 수량
			if(vol < 1 )
				throw new Exception();
		} catch (Exception e) {
			errorMessage("1개 이상의 수량을 입력하세요.");
			return;
		}
		
		if(tenPercent.isSelected() && thirtyPercent.isSelected()){
			errorMessage("할인 쿠폰은 중복  사용이 불가능합니다.");
			return;
		}
		
		if((user10percent == 0 && tenPercent.isSelected()) || (user30percent == 0 && thirtyPercent.isSelected())) {
			errorMessage("해당 쿠폰이 없습니다.");
			return;
		}
		
		coupon = tenPercent.isSelected() ? 1 : thirtyPercent.isSelected() ? 2  : 0;
		
		if(vol > mi.count) {
			errorMessage("재고가 부족합니다.");
			return;
		}
		
		try (PreparedStatement pst = conn.prepareStatement("select * from purchase where p_no = ? and pu_date = ? and u_no = ?")){
			
			pst.setObject(1, mi.productNo);
			pst.setObject(2, nowDate);
			pst.setObject(3, uNo);
			if(pst.executeQuery().next()) {
				errorMessage("동일한 상품을 이미 구매하였습니다.");
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if(confirmMeseage("총 각격이 "+String.format("%,d",getPrice(vol))+"원 입니다.\n결제하시겠습니까?", "결제") == JOptionPane.YES_OPTION) {
			informMessage("결제가 완료되었습니다.");
			try (PreparedStatement pst = conn.prepareStatement("insert into purchase values(0,?,?,?,?,?,?)")){
				pst.setObject(1, mi.productNo);
				pst.setObject(2, mi.price);
				pst.setObject(3, vol);
				pst.setObject(4, coupon);
				pst.setObject(5,uNo);
				pst.setObject(6, nowDate);
				
				pst.execute();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		try (PreparedStatement pst = conn.prepareStatement("update product set p_stock = p_stock - ? where p_no = ?")){
			pst.setObject(1, vol);
			pst.setObject(2, mi.productNo);
			pst.execute();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		if(tenPercent.isSelected() || thirtyPercent.isSelected()) {
//			String usedCoupon = tenPercent.isSelected() ? "u_10percent" : "u_30percent"; 
//			try (PreparedStatement pst = conn.prepareStatement("update user set "+usedCoupon+"= "+usedCoupon+"-1 where u_no = ?")){
//				pst.setObject(1, uNo);
//				pst.execute();
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
			updateCoupon(tenPercent.isSelected() ? "u_10percent" : "u_30percent", -1);
		}
		
		openFrame(new ProductListFrame());
			
		}
		
	}
	
	private int getPrice(int vol) {
		int pPrice = mi.price;
		
		if(tenPercent.isSelected()) {
			user10percent--;
			return (int) ((pPrice * vol) * 0.9);
		}else if(thirtyPercent.isSelected()) {
			user30percent--;
			return (int) ((pPrice * vol) * 0.7);
		}else {
			return pPrice * vol;
		}
	}
	
	private MenuInform getSelectedMenu(String pName) {
		try (PreparedStatement pst = conn.prepareStatement(
				"select * from product as p\r\n"
				+ "inner join category as c\r\n"
				+ "on c.c_no = p.c_no\r\n"
				+ "where p_name = ?")){
			
			pst.setObject(1, pName);
			ResultSet rs = pst.executeQuery();
			
			rs.next();
			
			return new MenuInform(rs.getInt("p_no"), rs.getString("p_name"), rs.getInt("p_price"), rs.getString("p_explanation"), rs.getInt("p_stock"), rs.getString("c_name"));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	class ImagePanel extends JPanel{
		public ImagePanel(String pName) {
			super(new BorderLayout());
			JLabel image = new JLabel(getImage(pName, 100,130));
			
			addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					if(e.getClickCount() == 2) {
						selectedImage.setIcon(getImage(pName, 200, 200));
						setImageSlidePanel(pName);
						setMenuInfo(pName);
						mi = getSelectedMenu(pName);
						tenPercent.setSelected(false);
						thirtyPercent.setSelected(false);
						tfs[2].setText("");
					}
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
