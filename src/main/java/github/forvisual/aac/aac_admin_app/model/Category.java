package github.forvisual.aac.aac_admin_app.model;

public class Category {
	
	final public int num;
	final public String name;
	
	public Category(int num, String name) {
		super();
		this.num = num;
		this.name = name;
	}
	
	
	
	public int getNum() {
		return num;
	}



	public String getName() {
		return name;
	}



	@Override
	public String toString() {
		return String.format("[%2d] %s", num, name);
	}
	
	

}
