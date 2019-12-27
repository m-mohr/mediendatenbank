package mediendatenbank;

import java.util.Vector;

public class BibEntry {

	private Integer id;
	private String title;
	private String medium;
	private String year;
	private String details;
	private Integer type;

	public BibEntry(Integer id, String title, String medium, String year, String details, Integer type) {
		this.id = id;
		this.title = title;
		this.medium = medium;
		this.year = year;
		this.details = details;
		this.type = type;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setMedium(String medium) {
		this.medium = medium;
	}

	public void setYear(String year) {
		this.year = year;
	}

	public void setDetails(String details) {
		this.details = details;
	}

	public void setType(Integer type) {
		this.type = type;
	}
	
	public void setAudio() {
		this.setType(0);
	}
	
	public void setVideo() {
		this.setType(1);
	}

	public String getTitle() {
		return this.title;
	}

	public String getMedium() {
		return this.medium;
	}

	public String getYear() {
		return this.year;
	}

	public String getDetails() {
		return this.details;
	}

	public Integer getID() {
		return this.id;
	}

	public Integer getType() {
		return type;
	}
	
	public boolean isAudio() {
		return (type == 0);
	}
	
	public boolean isVideo() {
		return (type == 1);
	}

	public Vector getVector() {
		Vector v = new Vector();
		v.add(this.title);
		v.add(this.medium);
		v.add(this.year);
		v.add(this.details);
		v.add(this.id);
		return v;
	}
}
