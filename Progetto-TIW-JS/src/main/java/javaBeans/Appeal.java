package javaBeans;


public class Appeal {
	private int idCorso;
	private String data;
	public int getIdCorso() {
		return idCorso;
	}
	public void setIdCorso(int idCorso) {
		this.idCorso = idCorso;
	}
	public String getData() {
		return data;
	}
	public void setData(String data) {
		this.data = data;
	}
	@Override
	public boolean equals(Object obj) {
		Appeal other = null; 
		if(this==obj) return true;
		if(obj == null || !(obj instanceof Appeal))return false;
		other = (Appeal)obj;
		return  (other.idCorso == this.idCorso && other.data.equals(this.data));
	}
	
	
}
