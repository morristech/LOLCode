package net.linxdroid.lolcode.filebrowser;

public class Item implements Comparable<Item>{
	private String  name;
	private String  data;
	private String  date;
	private String  path;
	private String  image;
	private boolean hidden;
	
	public Item(String n,String d, String dt, String p, String img, boolean hdn)
	{
		name = n;
		data = d;
		date = dt;
		path = p; 
		image = img;
		hidden = hdn;
	}
	public String getName()
	{
		return name;
	}
	public String getData()
	{
		return data;
	}
	public String getDate()
	{
		return date;
	}
	public String getPath()
	{
		return path;
	}
	public String getImage() {
		return image;
	}
	public boolean isHidden() {
		return hidden;
	}
	
	public int compareTo(Item o) {
		if(this.name != null)
			return this.name.toLowerCase().compareTo(o.getName().toLowerCase()); 
		else 
			throw new IllegalArgumentException();
	}
}
