package frame;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import model.MenuInform;

public class ProductListFrame extends BaseFrame{
	JTextField tfs[] = new JTextField[3]; //상품명 최저가격 죄대가격
	DefaultTableModel model = new DefaultTableModel(null,new Object[] {"상품번호","상품 카테고리","상품 이름","상품 가격","상품 재고","상품 설명"});
	JTable table = new JTable(model) {
		public boolean isCellEditable(int row, int column) {
			return false;
		}
	};
	JScrollPane tableScroll = createComponent(new JScrollPane(table),800,100);
	ArrayList<JLabel> labelList = new ArrayList<JLabel>();
	JPanel imagePanel = new JPanel(new GridLayout(0,3));
	public ProductListFrame() {
		super("상품목록", 1000, 600);
		setLayout(null);
		
		for (int i = 0; i < tfs.length; i++) {
			tfs[i] = new JTextField(10);
		}
		
		JPanel aPanel = createComponent(new JPanel(new BorderLayout()),20,40,200,120);
		JPanel gridPanel = new JPanel(new GridLayout(0,2,20,10));
		gridPanel.add(createLabel("상품명"));
		gridPanel.add(tfs[0]);
		gridPanel.add(createLabel("최저 가격"));
		gridPanel.add(tfs[1]);
		gridPanel.add(createLabel("최대 가격"));
		gridPanel.add(tfs[2]);
		
		aPanel.add(gridPanel,BorderLayout.CENTER);
		aPanel.add(createComponent(createButton("검색", e->search()),200,30),BorderLayout.SOUTH);
		
		JPanel bPanel = createComponent(new JPanel(new BorderLayout()),0,160,230,400);
		JPanel labelPanel = new JPanel(new GridLayout(0,1,0,20));
		
		labelPanel.add(createLabel("카테고리",new Font("굴림",1,20)));
		
		try (PreparedStatement pst = conn.prepareStatement("select * from category;")){
			ResultSet rs = pst.executeQuery();
			
			while(rs.next()) {
				
				JLabel label = createLabel(rs.getString("c_Name"),null,JLabel.LEFT);
				label.addMouseListener(new MouseAdapter() {
					
					@Override
					public void mouseClicked(MouseEvent e) {
						imagePanel.removeAll();
						model.setNumRows(0);
						
						setcPanel("select * from product as p \r\n"
								+ "inner join category as c \r\n"
								+ "on p.c_no = c.c_no\r\n"
								+ "where c.c_name = ?", label.getText());
						
						for (int i = 0; i < labelList.size(); i++) {
							if(labelList.get(i).getForeground().equals(Color.red))
								labelList.get(i).setForeground(Color.black);
						}
						label.setForeground(Color.red);
					}
				});
				labelList.add(label);
				labelPanel.add(label);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		setcPanel("select * from product as p \r\n"
				+ "inner join category as c \r\n"
				+ "on p.c_no = c.c_no\r\n"
				+ "where c.c_name = ?", "정육");
		labelList.get(0).setForeground(Color.red);
		
		table.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount() == 2) {
					openFrame(new PurchaseFrame(String.valueOf(table.getValueAt(table.getSelectedRow(), 2))));
				}
			}
		});
		JScrollPane scrollPane = new JScrollPane(labelPanel);
		JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		btnPanel.add(createComponent(createButtonWithoutMargin("구매목록", e->openFrame(new SelectMonthFrame())),80,30));
		bPanel.add(scrollPane,BorderLayout.CENTER);
		bPanel.add(btnPanel,BorderLayout.SOUTH);
		JPanel cPanel = createComponent(new JPanel(new BorderLayout()),240,40,730,500);
		cPanel.add(new JScrollPane(imagePanel),BorderLayout.CENTER);
		cPanel.add(tableScroll,BorderLayout.SOUTH);
		
		add(createComponent(createLabel("회원:"+uName, new Font("굴림",1,20)), 5, 10,200,25));
		add(createComponent(createButtonWithoutMargin("출석 이벤트", e->openFrame(new CalendarFrame())), 880,5,90,30));
		add(aPanel);
		add(bPanel);
		add(cPanel);
	}
	
	class ImagePanel extends JPanel{

		public ImagePanel(String pName,String pExplanation) {
			super(new BorderLayout());
			JLabel image = new JLabel(getImage(pName, 230, 150));
			setToolTipText(pExplanation);
			setBorder(BorderFactory.createLineBorder(Color.black));
			
			add(image,BorderLayout.CENTER);
			add(createLabel(pName,null,JLabel.CENTER),BorderLayout.SOUTH);
			
			addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					if(e.getClickCount() == 2) {
						openFrame(new PurchaseFrame(pName));
					}
				}
			});
		}
	}
	
	private void setcPanel(String sql,String cName) {
		try (PreparedStatement pst = conn.prepareStatement(sql)){
			pst.setObject(1, cName); // cName = lable.getText();
			ResultSet rs = pst.executeQuery();
			
			if(rs == null) {
				informMessage("검색결과가 없습니다.");
				return;
			}
			new MouseAdapter() {
			};
			while(rs.next()) {
				model.addRow(new Object[] {rs.getInt("p_no"),rs.getString("c_name"),rs.getString("p_name"),
						String.format("%,d", rs.getInt("p_price")),rs.getInt("p_stock"),rs.getString("p_explanation")});
				imagePanel.add(new ImagePanel(rs.getString("p_name"), rs.getString("p_explanation")));
			}
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		revalidate();
	}
	
	private void search() {
		imagePanel.removeAll();
		model.setNumRows(0);
		
		String cName = null;
		for (int i = 0; i < labelList.size(); i++) {
			if(labelList.get(i).getForeground().equals(Color.red)) 
				cName = labelList.get(i).getText();
		}
		
		if(tfs[0].getText().isEmpty() && tfs[1].getText().isEmpty() && tfs[2].getText().isEmpty()) {
			setcPanel("select * from product as p \r\n"
					+ "inner join category as c \r\n"
					+ "on p.c_no = c.c_no\r\n"
					+ "where c.c_name = ?", cName);
			return;
		}
		
		int min = 0;
		int max = 0;
		try {
			min = tfs[1].getText().isEmpty() ? Integer.MIN_VALUE : Integer.parseInt(tfs[1].getText());
			max = tfs[2].getText().isEmpty() ? Integer.MAX_VALUE : Integer.parseInt(tfs[2].getText());
		} catch (Exception e) {
			errorMessage("최대 가격과 최저가격은 숫자로 입력해주세요.");
			return;
		}
		
		if(min > max) {
			errorMessage("최대 각격은 최저 가격보다 커야 합니다.");
			return;
		}
		
		setcPanel("select * from product as p \r\n"
				+ "inner join category as c\r\n"
				+ "on p.c_no = c.c_no\r\n"
				+ "where c_name = ? and p_name like concat('%','"+tfs[0].getText()+"','%') \r\n"
				+ "and (p_price between "+min+" and "+max+")", cName);
	}
	
	@Override
	public void closeFrame() {
		previousFrame();
	}
	

}
