package javaBeans;

import java.time.LocalTime;
import java.util.Map;
import java.sql.Date;

public class Report {
	private int id;
	private String data;
	private String ora;
	private Map<User,String> studentData;
	
	public Map<User, String> getStudentData() {
		return studentData;
	}
	public void setStudentData(Map<User, String> studentData) {
		this.studentData = studentData;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getData() {
		return data;
	}
	public void setData(String data) {
		this.data = data;
	}
	public String getOra() {
		return ora;
	}
	public void setOra(String ora) {
		this.ora = ora;
	}
	@Override
	public boolean equals(Object obj) {
		Report other = null; 
		if(this==obj) return true;
		if(obj == null || !(obj instanceof Report))return false;
		other = (Report)obj;
		return  (other.id == this.id);
	}
	
	

}
