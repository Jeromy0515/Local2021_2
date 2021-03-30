package frame;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import model.MenuInform;

public class ProductManagementFrame extends BaseFrame{
	DefaultTableModel model = new DefaultTableModel(null,new Object[] {"상품번호","상품 카테고리","상품명","상품 가격","상품 재고","상품 설명"}) {
		public boolean isCellEditable(int row, int column) {
			return false;
		}
	};
	JTable table = new JTable(model) {
		@Override
		public java.awt.Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
			
			JComponent comp =(JComponent)super.prepareRenderer(renderer, row, column);
			if (row == getSelectedRow()) {
				comp.setBackground(Color.yellow);
			} else
				comp.setBackground(Color.white);
			return comp;
		};
	};
	JScrollPane scrollPane = new JScrollPane(table);
	JComboBox<String> cb = new JComboBox<String>();
	
	public ProductManagementFrame() {
		super("상품관리", 600,500);
		
		for (int i = 0; i < table.getColumnCount(); i++) {
			table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
		}
		JPanel centerPanel = new JPanel(new BorderLayout());
		centerPanel.add(scrollPane);
		centerPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		
		cb.addActionListener(e->setTable());

		table.addMouseListener(new MouseAdapter() {
			public void mouseClicked(java.awt.event.MouseEvent e) {
				int row = table.getSelectedRow();
				if(e.getClickCount() == 2) {
					openFrame(new ModifyProductFrame((String)table.getValueAt(row, 2)));
				}
			};
		});
		
		setComboBox();
		setTable();
		JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		southPanel.add(cb);
		
		add(centerPanel,BorderLayout.CENTER);
		add(southPanel,BorderLayout.SOUTH);
	}
	
	private void setComboBox() {
		try (PreparedStatement pst = conn.prepareStatement("select * from category")){
			ResultSet rs = pst.executeQuery();
			cb.addItem("전체");
			while(rs.next()) {
				cb.addItem(rs.getString("c_name"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void setTable() {
		model.setNumRows(0);
		String sql = null;
		
		if(cb.getSelectedItem().equals("전체")) 
			sql = "select * from product as p inner join category as c on p.c_no = c.c_no";
		else 
			sql = "select * from product as p inner join category as c on p.c_no = c.c_no where c_name = ?";
		
		try (PreparedStatement pst = conn.prepareStatement(sql)){
			if(!cb.getSelectedItem().equals("전체"))
				pst.setObject(1, cb.getSelectedItem());
			ResultSet rs = pst.executeQuery();
			while(rs.next()) {
				model.addRow(new Object[] {rs.getInt("p_no"),rs.getString("c_name"),rs.getString("p_name"),
						String.format("%,d",rs.getInt("p_price")),rs.getInt("p_stock"),rs.getString("p_explanation")});
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void closeFrame() {
		previousFrame();
	}
	
	public static void main(String[] args) {
		new ProductManagementFrame().setVisible(true);
	}
}
