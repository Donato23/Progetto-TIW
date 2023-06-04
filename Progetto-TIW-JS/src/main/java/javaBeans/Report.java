package javaBeans;

import java.time.LocalTime;
import java.util.Map;
import java.sql.Date;

public class Report {
	private int id;
	private Date data;
	private LocalTime ora;
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
	public Date getData() {
		return data;
	}
	public void setData(Date data) {
		this.data = data;
	}
	public LocalTime getOra() {
		return ora;
	}
	public void setOra(LocalTime ora) {
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
