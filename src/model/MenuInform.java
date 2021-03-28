package model;

public class MenuInform {
	public int productNo;
	public String name;
	public int price;
	public String explanation;
	public int count;
	public String category;

	public MenuInform(int productNo, String name, int price, String explanation, int count, String category) {
		this.productNo = productNo;
		this.explanation = explanation;
		this.name = name;
		this.price = price;
		this.count = count;
		this.category = category;
	}
	
}
