package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import frame.BaseFrame;

public class Setting {
	
	static Connection conn = null;
	static Statement st = null;
	static {
		try {
			conn = DriverManager.getConnection("jdbc:mysql://localhost?serverTimezone=UTC&allowLoadLocalInfile=true","root","1234");
			st = conn.createStatement();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		try {
			execute("drop database if exists 2021지방_1;");
			
			execute("CREATE SCHEMA IF NOT EXISTS `2021지방_1` DEFAULT CHARACTER SET utf8 ;");
			
			execute("CREATE TABLE IF NOT EXISTS `2021지방_1`.`user` (\r\n"
					+ "  `u_no` INT(11) NOT NULL AUTO_INCREMENT,\r\n"
					+ "  `u_id` VARCHAR(20) NULL DEFAULT NULL,\r\n"
					+ "  `u_pw` VARCHAR(20) NULL DEFAULT NULL,\r\n"
					+ "  `u_name` VARCHAR(15) NULL DEFAULT NULL,\r\n"
					+ "  `u_phone` VARCHAR(20) NULL DEFAULT NULL,\r\n"
					+ "  `u_age` DATE NULL DEFAULT NULL,\r\n"
					+ "  `u_10percent` INT(11) NULL DEFAULT NULL,\r\n"
					+ "  `u_30percent` INT(11) NULL DEFAULT NULL,\r\n"
					+ "  PRIMARY KEY (`u_no`))\r\n"
					+ "ENGINE = InnoDB\r\n"
					+ "DEFAULT CHARACTER SET = utf8;");
			
			execute("CREATE TABLE IF NOT EXISTS `2021지방_1`.`coupon` (\r\n"
					+ "  `c_no` INT(11) NOT NULL AUTO_INCREMENT,\r\n"
					+ "  `u_no` INT(11) NULL DEFAULT NULL,\r\n"
					+ "  `c_date` VARCHAR(15) NULL DEFAULT NULL,\r\n"
					+ "  `c_10percent` INT(11) NULL DEFAULT NULL,\r\n"
					+ "  `c_30percent` INT(11) NULL DEFAULT NULL,\r\n"
					+ "  PRIMARY KEY (`c_no`),\r\n"
					+ "  INDEX `fk_coupon_u_no_idx` (`u_no` ASC) VISIBLE,\r\n"
					+ "  CONSTRAINT `fk_coupon_u_no`\r\n"
					+ "    FOREIGN KEY (`u_no`)\r\n"
					+ "    REFERENCES `2021지방_1`.`user` (`u_no`)\r\n"
					+ "    ON DELETE CASCADE\r\n"
					+ "    ON UPDATE CASCADE)\r\n"
					+ "ENGINE = InnoDB\r\n"
					+ "DEFAULT CHARACTER SET = utf8;");
			
			execute("CREATE TABLE IF NOT EXISTS `2021지방_1`.`attendance` (\r\n"
					+ "  `a_no` INT(11) NOT NULL AUTO_INCREMENT,\r\n"
					+ "  `u_no` INT(11) NULL DEFAULT NULL,\r\n"
					+ "  `a_date` DATE NULL DEFAULT NULL,\r\n"
					+ "  PRIMARY KEY (`a_no`),\r\n"
					+ "  INDEX `fk_attendance_user1_idx` (`u_no` ASC) VISIBLE,\r\n"
					+ "  CONSTRAINT `fk_attendance_user1`\r\n"
					+ "    FOREIGN KEY (`u_no`)\r\n"
					+ "    REFERENCES `2021지방_1`.`user` (`u_no`)\r\n"
					+ "    ON DELETE CASCADE\r\n"
					+ "    ON UPDATE CASCADE)\r\n"
					+ "ENGINE = InnoDB\r\n"
					+ "DEFAULT CHARACTER SET = utf8;");
			
			execute("CREATE TABLE IF NOT EXISTS `2021지방_1`.`category` (\r\n"
					+ "  `c_no` INT(11) NOT NULL AUTO_INCREMENT,\r\n"
					+ "  `c_name` VARCHAR(10) NULL DEFAULT NULL,\r\n"
					+ "  PRIMARY KEY (`c_no`))\r\n"
					+ "ENGINE = InnoDB\r\n"
					+ "DEFAULT CHARACTER SET = utf8;");
			
			execute("CREATE TABLE IF NOT EXISTS `2021지방_1`.`product` (\r\n"
					+ "  `p_no` INT(11) NOT NULL AUTO_INCREMENT,\r\n"
					+ "  `c_no` INT(11) NULL DEFAULT NULL,\r\n"
					+ "  `p_name` VARCHAR(20) NULL DEFAULT NULL,\r\n"
					+ "  `p_price` INT(11) NULL DEFAULT NULL,\r\n"
					+ "  `p_stock` INT(11) NULL DEFAULT NULL,\r\n"
					+ "  `p_explanation` VARCHAR(150) NULL DEFAULT NULL,\r\n"
					+ "  PRIMARY KEY (`p_no`),\r\n"
					+ "  INDEX `fk_product_category1_idx` (`c_no` ASC) VISIBLE,\r\n"
					+ "  CONSTRAINT `fk_product_category1`\r\n"
					+ "    FOREIGN KEY (`c_no`)\r\n"
					+ "    REFERENCES `2021지방_1`.`category` (`c_no`)\r\n"
					+ "    ON DELETE CASCADE\r\n"
					+ "    ON UPDATE CASCADE)\r\n"
					+ "ENGINE = InnoDB\r\n"
					+ "DEFAULT CHARACTER SET = utf8;");
			
			execute("CREATE TABLE IF NOT EXISTS `2021지방_1`.`purchase` (\r\n"
					+ "  `pu_no` INT(11) NOT NULL AUTO_INCREMENT,\r\n"
					+ "  `p_no` INT(11) NULL DEFAULT NULL,\r\n"
					+ "  `pu_price` INT(11) NULL DEFAULT NULL,\r\n"
					+ "  `pu_count` INT(11) NULL DEFAULT NULL,\r\n"
					+ "  `coupon` INT(11) NULL DEFAULT NULL,\r\n"
					+ "  `u_no` INT(11) NULL DEFAULT NULL,\r\n"
					+ "  `pu_date` DATE NULL DEFAULT NULL,\r\n"
					+ "  PRIMARY KEY (`pu_no`),\r\n"
					+ "  INDEX `fk_purchase_user_idx` (`u_no` ASC) VISIBLE,\r\n"
					+ "  INDEX `fk_purchase_product1_idx` (`p_no` ASC) VISIBLE,\r\n"
					+ "  CONSTRAINT `fk_purchase_user`\r\n"
					+ "    FOREIGN KEY (`u_no`)\r\n"
					+ "    REFERENCES `2021지방_1`.`user` (`u_no`)\r\n"
					+ "    ON DELETE CASCADE\r\n"
					+ "    ON UPDATE CASCADE,\r\n"
					+ "  CONSTRAINT `fk_purchase_product1`\r\n"
					+ "    FOREIGN KEY (`p_no`)\r\n"
					+ "    REFERENCES `2021지방_1`.`product` (`p_no`)\r\n"
					+ "    ON DELETE CASCADE\r\n"
					+ "    ON UPDATE CASCADE)\r\n"
					+ "ENGINE = InnoDB\r\n"
					+ "DEFAULT CHARACTER SET = utf8;");
			
			execute("use 2021지방_1");
			execute("drop user if exists 'user'@'%';");
			execute("create user 'user'@'%' identified by '1234';");
			execute("grant select, insert, delete, update on `2021지방_1`.* to 'user'@'%';");
			execute("flush privileges");
			
			execute("set global local_infile = 1;");
			
			for(String table:"user,coupon,attendance,category,product,purchase".split(",")) {
				execute("load data local infile './제2과제 datafile/"+table+".txt' into table "+table
						+ " fields terminated by '\t' lines terminated by '\n' ignore 1 lines");
			}
			BaseFrame.informMessage("셋팅 성공");
		} catch (Exception e) {
			BaseFrame.errorMessage("셋팅 실패");
		}
	}
	static void execute(String sql) throws Exception{
			st.execute(sql);
	}
	
}
