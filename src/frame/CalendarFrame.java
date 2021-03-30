package frame;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

public class CalendarFrame extends BaseFrame{
	ArrayList<Integer> dateList = new ArrayList<Integer>();
	public CalendarFrame() {
		super("출석", 350, 420);
		setLayout(new BorderLayout(0,10));
		String date[] = {"일","월","화","수","목","금","토"};
		JPanel centerPanel = new JPanel(new BorderLayout(0,10));
		JPanel dateLabelPanel = new JPanel(new GridLayout(0,7,15,0));
		JPanel datePanel = new JPanel(new GridLayout(0,7));
		for (int i = 0; i < date.length; i++) {
			JLabel label = new JLabel(date[i],JLabel.CENTER);
			if(i==0)
				label.setForeground(Color.red);
			else if(i==6)
				label.setForeground(Color.blue);
			dateLabelPanel.add(label);
		}
		
		try (PreparedStatement pst = conn.prepareStatement(
				"select day(a_date) as day from attendance "
				+ "where u_no = ? and year(a_date) = ? and month(a_date) = ?")){
			pst.setObject(1, uNo);
			pst.setObject(2, now.getYear());
			pst.setObject(3, now.getMonthValue());
			ResultSet rs = pst.executeQuery();
			while(rs.next()) {
				dateList.add(rs.getInt("day"));
			}
		} catch (Exception e) {
			e.printStackTrace();
			
		}
		
//		for (int i = 0; i < dateList.size(); i++) {
//			System.out.println(dateList.get(i));
//		}
		
		boolean atStart = false;
		for(int i=1;i<=now.lengthOfMonth();i++) {
			if(!atStart) {
				if(i-1 == now.atStartOfDay().getDayOfWeek().getValue()) {
					atStart = true;
					i = 1;
				}else {
					datePanel.add(new JPanel());
				}
			}
			
			if(atStart) 
				datePanel.add(new DatePanel(i));
		}
		
		JPanel southPanel = createComponent(new JPanel(null),1,70);
		southPanel.add(createComponent(createButtonWithoutMargin("쿠폰 받기", e->getCoupon()),230,30,90,30));
		
		centerPanel.add(dateLabelPanel,BorderLayout.NORTH);
		centerPanel.add(datePanel,BorderLayout.CENTER);
		add(createLabel(now.getYear()+"년 "+now.getMonthValue()+"월",new Font(null,1,20)),BorderLayout.NORTH);
		add(centerPanel,BorderLayout.CENTER);
		add(southPanel,BorderLayout.SOUTH);
		
	}

	
	@Override
	public void closeFrame() {
		previousFrame();
	}
	
	class DatePanel extends JPanel{
		int day;
		boolean click = false, check = false;
		@Override
		public void paintComponent(Graphics g) {
			if(!click) {
				for (int i = 0; i < dateList.size(); i++) {
					if(dateList.get(i) == day) {
						g.setColor(Color.black);
						g.drawOval(5,5,35,35);
						if(dateList.get(i) == now.getDayOfMonth())
							check = true;
						break;
					}else if(day == now.getDayOfMonth() && check) {
						g.setColor(Color.red);
						g.drawOval(5,5,35,35);		
						break;
					}
				}
			}	
			
			if(click) {
				if(day != now.getDayOfMonth()) {
					errorMessage("출석체크가 불가능한 날짜입니다.");
					return;
				}
				if(!check) {
					super.paintComponent(g); // super.paintComponent(g) 호출시 그렸던거 지워줌
					g.setColor(Color.black);
					g.drawOval(5,5,35,35);
					try (PreparedStatement pst = conn.prepareStatement("insert into attendance values(0,?,?)")){
						pst.setObject(1, uNo);
						pst.setObject(2, now.toString());
						pst.execute();
					} catch (Exception e) {
						e.printStackTrace();
					}
					check = true;
				}else {
					errorMessage("이미 출석체크를 했습니다.");
					return;
				}
			}
			
		}
		
		public DatePanel(int day) {
			super(new GridBagLayout());
			this.day = day;
			setBorder(new LineBorder(Color.black));
			
			addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					attendance();
				}
			});
			add(createLabel(day+""));
		}
		private void attendance() {
			click = true;
			paint(getGraphics());
		}
	}
	
	private void getCoupon() {
		
		try (PreparedStatement pst = conn.prepareStatement("select * from coupon where c_date = ? and u_no = ?")){
			pst.setObject(1, now.getYear()+"-"+String.format("%02d",now.getMonthValue()));
			pst.setObject(2, uNo);
			ResultSet rs = pst.executeQuery();
			if(rs.next()) {
				if(rs.getInt("c_10percent") == 1 || rs.getInt("c_30percent") == 1) {
					errorMessage("쿠폰을 이미 받았습니다.");
					return;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(dateList.size() >= 5) {
			updateCoupon(1, 1);
			openFrame(new CouponFrame(true));
			
		}else if(dateList.size() >= 3) {
			updateCoupon(1, 0);
			openFrame(new CouponFrame(false));
		}
	}
	
	private void updateCoupon(int tenPercent,int thirtyPercent) {
		try (PreparedStatement pst = conn.prepareStatement("insert into coupon values (0,?,?,?,?)")){
			pst.setObject(1, uNo);
			pst.setObject(2, now.getYear()+"-"+now.getMonthValue());
			pst.setObject(3, tenPercent);
			pst.setObject(4, thirtyPercent);
			pst.execute();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try (PreparedStatement pst = conn.prepareStatement("update user set u_10percent = u_10percent + ?,u_30percent = u_30percent + ? where u_no = ?")){
			pst.setObject(1, tenPercent);
			pst.setObject(2, thirtyPercent);
			pst.setObject(3, uNo);
			pst.execute();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
}
