package frame;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileSystemView;

public class ModifyProductFrame extends BaseFrame{
	JComboBox<String> cb = new JComboBox<String>();
	JTextField tfs[] = new JTextField[4];
	JTextField tfCatgory = new JTextField(8);
	String pName = null;
	ImageIcon icon;
	JLabel image;
	String modifiedImagePath = "";
	int pNo;
	boolean change = false;
	String category = "",price = "" , stock = "", explanation = "";
	
	public ModifyProductFrame(String pName) {
		super("상품수정", 320, 450);
		
		this.pName = pName;
		icon = getImage(pName, 300, 200);
		image = new JLabel(icon);
		
		JPanel northPanel = new JPanel(new BorderLayout());
		northPanel.add(createButton("사진 넣기", e->changeImage()),BorderLayout.SOUTH);
		northPanel.add(image,BorderLayout.CENTER);
		for (int i = 0; i < tfs.length; i++) {
			if(i == tfs.length-1) {
				tfs[i] = new JTextField(12);
				break;
			}
			tfs[i] = new JTextField(8);
		}
		
		
		tfCatgory.setVisible(true);
		JPanel centerPanel = new JPanel(null);
		String labelText[] = {"상품명:","카테고리:","가격:","재고:","설명:"};
		int y = 5;
		for (int i = 0; i < 5; i++) {
			centerPanel.add(createComponent(createLabel(labelText[i]),5,y,70,20));
			y += 30;
		}
		tfs[0].setEnabled(false);
		centerPanel.add(createComponent(tfs[0], 80, 5, 100, 20));
		centerPanel.add(createComponent(cb, 80, 35, 100, 20));
		centerPanel.add(createComponent(tfs[1], 80, 65, 100, 20));
		centerPanel.add(createComponent(tfs[2], 80, 95, 100, 20));
		centerPanel.add(createComponent(tfs[3], 80, 125, 190, 20));
		centerPanel.add(createComponent(tfCatgory, 190, 35, 100, 20));
		tfCatgory.setVisible(false);
		
		setComboBox();
		setMenuInform();
		cb.addActionListener(e->{
			if(cb.getSelectedItem().equals("기타")) {
				tfCatgory.setVisible(true);
			}else {
				tfCatgory.setVisible(false);
			}
		});
		JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		southPanel.add(createButton("수정", e->modify()));
		southPanel.add(createButton("취소", e->previousFrame()));
		
		category = (String)cb.getSelectedItem();
		price = tfs[1].getText();
		stock = tfs[2].getText();
		explanation = tfs[3].getText();
		
		add(northPanel,BorderLayout.NORTH);
		add(centerPanel,BorderLayout.CENTER);
		add(southPanel,BorderLayout.SOUTH);
		
	}
	
	private void setComboBox() {
		try (PreparedStatement pst = conn.prepareStatement("select * from category")){
			ResultSet rs = pst.executeQuery();
			while(rs.next()) {
				cb.addItem(rs.getString("c_name"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		cb.addItem("기타");
	}
	
	private void setMenuInform() {
		try (PreparedStatement pst = conn.prepareStatement("select * from product as p inner join category as c on p.c_no = c.c_no where p_name = ?")){
			pst.setObject(1, pName);
			ResultSet rs = pst.executeQuery();
			if(rs.next()) {
				pNo = rs.getInt("p_no");
				tfs[0].setText(pName);
				tfs[1].setText(rs.getInt("p_price")+"");
				cb.setSelectedItem(rs.getString("c_name"));
				tfs[2].setText(rs.getInt("p_stock")+"");
				tfs[3].setText(rs.getString("p_explanation"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void modify() {
		
		if(category.isEmpty() || price.isEmpty() || stock.isEmpty() || explanation.isEmpty()) {
			errorMessage("빈칸이 있습니다.");
			return;
		}
		
		if(cb.getSelectedItem().equals("기타") && tfCatgory.getText().isEmpty()) {
			errorMessage("카테고리를 입력해주세요.");
			return;
		}
		
		if(cb.getSelectedItem().equals("기타")) {
			for (int i = 0; i < cb.getItemCount(); i++) {
				if(cb.getItemAt(i).equals(tfCatgory.getText())) {
					errorMessage("이미 있는 카테고리입니다.");
					return;
				}
			}
		}
		
		try {
			Integer.parseInt(tfs[1].getText());
			Integer.parseInt(tfs[2].getText());
		} catch (Exception e) {
			errorMessage("숫자로 입력하세요.");
			return;
		}
		
		if(category.equals((String)cb.getSelectedItem()) && price.equals(tfs[1].getText())&& 
				stock.equals(tfs[2].getText()) && explanation.equals(tfs[3].getText()) && !change) {
			errorMessage("수정한 내용이 없습니다.");
			return;
		}
		
		informMessage("상품정보가 수정되었습니다.");
		
		Image image = icon.getImage();
		BufferedImage bufferedImage = new BufferedImage(300, 200, BufferedImage.TYPE_INT_BGR);
		bufferedImage.createGraphics().drawImage(image, 0, 0, this);
		
		try {
			ImageIO.write(bufferedImage, "jpg", new File("./제2과제 datafile/이미지/" + tfs[0].getText() + ".jpg"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		if(cb.getSelectedItem().equals("기타")) {
			try (PreparedStatement pst = conn.prepareStatement("insert into category values(0,?)")){
				pst.setObject(1, tfCatgory.getText());
				pst.execute();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		try (PreparedStatement pst = conn.prepareStatement("update product set c_no = (select c_no from category where c_name = ?),p_price = ?, p_stock = ?, p_explanation = ? where p_no = ?")){
			pst.setObject(1, cb.getSelectedItem().equals("기타") ? tfCatgory.getText():cb.getSelectedItem());
			pst.setObject(2, tfs[1].getText());
			pst.setObject(3, tfs[2].getText()); 
			pst.setObject(4, tfs[3].getText());
			pst.setObject(5, pNo);
			pst.execute();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		
	}
	
	private void changeImage() {
		JFileChooser chooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory()); // 디렉토리 설정
		chooser.setDialogTitle(""); // 창의 제목
		chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES); // 파일 선택 모드

		int returnVal = chooser.showOpenDialog(null); // 열기용 창 오픈

		if (returnVal == JFileChooser.APPROVE_OPTION) { // 열기를 클릭
			modifiedImagePath = chooser.getSelectedFile().toString();
			System.out.println(modifiedImagePath);
			image.setIcon(icon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(modifiedImagePath)
					.getScaledInstance(300,200,Image.SCALE_SMOOTH)));
			change = true;
			
		}

	}
	
	@Override
	public void closeFrame() {
		previousFrame();
	}
	
}
