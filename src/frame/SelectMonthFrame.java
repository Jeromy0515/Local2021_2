package frame;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.sql.PreparedStatement;
import java.util.Iterator;
import java.util.TreeSet;

import javax.swing.JLabel;
import javax.swing.JPanel;

public class SelectMonthFrame extends BaseFrame{
	TreeSet<Integer> monthList = new TreeSet<Integer>();
	public SelectMonthFrame() {
		super("월 선택", 300, 300);
		
		JPanel centerPanel = new JPanel(new GridLayout(0,4));
		JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		for (int i = 1; i <= 12; i++) {
			centerPanel.add(new MonthPanel(i));
		}
		southPanel.add(createComponent(createButton("확인", e->confirm()),60,30));
		add(centerPanel,BorderLayout.CENTER);
		add(southPanel,BorderLayout.SOUTH);
	}
	
	class MonthPanel extends JPanel{
		@Override
		protected void paintComponent(Graphics g) {
			g.setColor(Color.yellow);
			g.fillOval(10,10,50,50);
			
			g.setColor(Color.black);
			g.drawOval(10,10,50,50);
		}
		
		public MonthPanel(int month) {
			setLayout(new GridBagLayout());
			JLabel label = createLabel(month+"월");
			add(label);
			addMouseListener(new MouseAdapter() {
				public void mouseClicked(java.awt.event.MouseEvent e) {
					label.setForeground(Color.red);
					monthList.add(month);
				}
			});
		}
	}
	
	@Override
	public void closeFrame() {
		previousFrame();
	}
	
	private void confirm() {
		if(monthList.size() == 0) {
			errorMessage("구매 월을 선택하세요.");
			return;
		}

		Iterator<Integer> iter = monthList.iterator();
		boolean hadPList = false;
		while(iter.hasNext()) {
			try (PreparedStatement pst = conn.prepareStatement("select * from purchase where u_no = ? and year(pu_date) = ? and month(pu_date) = ?;")){
				pst.setObject(1, uNo);
				pst.setObject(2, now.getYear());
				pst.setObject(3, iter.next());
				if(pst.executeQuery().next()) {
					hadPList = true;
					break;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		if(!hadPList) 
			errorMessage("구매 내역이 없습니다.");
		
		openFrame(new PurchaseListFrame(monthList));
	}

	public static void main(String[] args) {
		new SelectMonthFrame().setVisible(true);
	}
}
