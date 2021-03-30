package frame;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseMotionAdapter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.Iterator;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

public class PurchaseListFrame extends BaseFrame{
	DefaultTableModel model = new DefaultTableModel(null,new Object[] {"구매날짜","상품 번호","상품명","상품 가격","주문 개수","금액","쿠폰"}) {
		public boolean isCellEditable(int row, int column) {
			return false;
		};
	};
	JTable table = new JTable(model);
	JScrollPane scrollPane = new JScrollPane(table);
	JLabel monthLabel;
	JTextField tfTotal = new JTextField(10);
	int total = 0;
	JPopupMenu popupMenu = new JPopupMenu();
	Iterator<Integer> iter;
	TreeSet<Integer> monthList;
	StringBuilder builder = new StringBuilder();
	
	public PurchaseListFrame(TreeSet<Integer> monthList) {
		super("구매리스트", 600, 500);
		JPanel northPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JPanel centerPanel = new JPanel(new BorderLayout());
		
		table.removeColumn(table.getColumn("쿠폰"));
		this.monthList = monthList;
		setTable();
		tfTotal.setHorizontalAlignment(JTextField.RIGHT);
		tfTotal.setText(String.format("%,d", total));
		for (int i = 0; i < table.getColumnCount(); i++) {
			table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
		}
		
		builder.delete(builder.length()-1, builder.length());
		builder.append("월");
		
		monthLabel = createLabel(uName+" "+builder.toString());
		
		northPanel.add(monthLabel);
		northPanel.add(createButton("월 선택", e->openFrame(new SelectMonthFrame())));
		JPanel vbPanel = createComponent(new JPanel(new FlowLayout(FlowLayout.RIGHT)),400,35);
		JButton viewAllBtn = createButton("전체보기", e->viewAll());
		vbPanel.add(viewAllBtn);
		northPanel.add(vbPanel);
		northPanel.setBorder(BorderFactory.createEmptyBorder(15,0,0,0));
		
		centerPanel.add(scrollPane);
		centerPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5,5,20,5),
				BorderFactory.createLineBorder(Color.black)));
		
		southPanel.add(createLabel("총 금액"));
		southPanel.add(tfTotal);
		
		if(monthList.size() == 12)
			viewAll();
		table.addMouseListener(new MouseAdapter() {
			
			public void mouseReleased(java.awt.event.MouseEvent e) {
				JMenuItem menuItem = new JMenuItem();
				
				int row = table.rowAtPoint(e.getPoint());
				int col = table.columnAtPoint(e.getPoint());
				
				table.changeSelection(row, col, false,false);
				
				LocalDate selectedDay = LocalDate.parse(table.getValueAt(row, 0).toString());
				
				if(SwingUtilities.isRightMouseButton(e)) {
					popupMenu.removeAll();
					if(selectedDay.toString().equals(now.toString())) {
						popupMenu.add(menuItem = new JMenuItem("구매 취소"));
					}else {
						popupMenu.add(menuItem = new JMenuItem("구매내역 삭제"));
					}
					popupMenu.show(e.getComponent(), e.getX(), e.getY());
				}else if(e.getClickCount() == 2){
					if(confirmMeseage("재구매 하시겠습니까?","정보") == JOptionPane.YES_OPTION)
						openFrame(new PurchaseFrame((String)table.getValueAt(row, 2)));
				}
				menuItem.addActionListener(event->{
					JMenuItem item = (JMenuItem)event.getSource();
					if(item.getText().equals("구매내역 삭제")) {
						deletePurchaseList(row);
						informMessage("삭제되었습니다.");
						setTable();
					}else {
						try (PreparedStatement pst = conn.prepareStatement("update product set p_stock = p_stock + ? where p_no = ?")){
							pst.setObject(1, table.getValueAt(row, 4));
							pst.setObject(2, table.getValueAt(row, 1));
							pst.execute();
						} catch (Exception e3) {
							e3.printStackTrace();
						}
						int coupon = Integer.parseInt(String.valueOf(model.getValueAt(row,6)));
						if(coupon == 1)
							updateCoupon("u_10percent",1);
						else if(coupon == 2)
							updateCoupon("u_30percent",1);
						deletePurchaseList(row);
						informMessage("취소되었습니다.");
						setTable();
					}
					
				});
			}
		});
		
		add(northPanel,BorderLayout.NORTH);
		add(centerPanel,BorderLayout.CENTER);
		add(southPanel,BorderLayout.SOUTH);
		
	}
	private void setTable() {
		model.setNumRows(0);
		iter = monthList.iterator();
		while(iter.hasNext()) {
			int month = iter.next();
			builder.append(month+",");
			try (PreparedStatement pst = conn.prepareStatement(
					"select * from purchase as pc "
					+ "inner join product as p "
					+ "on p.p_no = pc.p_no "
					+ "where u_no = ? and year(pu_date) = ? and month(pu_date) = ?;")){
				pst.setObject(1, uNo);
				pst.setObject(2, now.getYear());
				pst.setObject(3, month);
				ResultSet rs = pst.executeQuery();
				
				while(rs.next()) {
					int amount = rs.getInt("pu_price")*rs.getInt("pu_count");
					model.addRow(new Object[] {rs.getString("pu_date"),rs.getInt("p_no"),rs.getString("p_name"),
							String.format("%,d",rs.getInt("pu_price")),rs.getInt("pu_count"),String.format("%,d",amount),rs.getInt("coupon")});
					total += amount;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		
	}
	private void viewAll() {
		monthLabel.setText(uName+" 전체");
		model.setNumRows(0);
		total = 0;
		
		try (PreparedStatement pst = conn.prepareStatement(
				"select * from purchase as pc "
				+ "inner join product as p "
				+ "on p.p_no = pc.p_no "
				+ "where u_no = ? ")){
			pst.setObject(1, uNo);
			ResultSet rs = pst.executeQuery();
			while(rs.next()) {
				int amount = rs.getInt("pu_price")*rs.getInt("pu_count");
				model.addRow(new Object[] {rs.getString("pu_date"),rs.getInt("p_no"),rs.getString("p_name"),
						String.format("%,d",rs.getInt("pu_price")),rs.getInt("pu_count"),String.format("%,d",amount),rs.getInt("coupon")});
				total += amount;
			}
			tfTotal.setText(String.format("%,d", total));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	private void deletePurchaseList(int row) {
		try (PreparedStatement pst = conn.prepareStatement("delete from purchase where pu_date = ? and p_no = ?")){
			pst.setObject(1, table.getValueAt(row, 0));
			pst.setObject(2, table.getValueAt(row, 1));
			pst.execute();
		} catch (Exception e2) {
			e2.printStackTrace();
		}
		repaint();
		revalidate();
	}
	
	@Override
	public void closeFrame() {
		previousFrame();
	}
	
}
