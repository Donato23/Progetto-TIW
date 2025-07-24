package javaBeans;

public class Mark {
	
	private String mark;
	private EvaluationState statoValutazione;


	public String getMark() {
		return mark;
	}

	public void setMark(String mark) {
		this.mark = mark;
	}

	public EvaluationState getStatoValutazione() {
		return statoValutazione;
	}

	public void setStatoValutazione(EvaluationState statoValutazione) {
		this.statoValutazione = statoValutazione;
	}
	
	public boolean rejectable() {
		
		int markValue=0;
		
		try {
			markValue=Integer.parseInt(mark);
		}catch(NumberFormatException e) {
			if(mark.equals("30L")) {
				markValue=31;
			}
		}
		return( markValue>=18 && markValue <=31 && statoValutazione==EvaluationState.PUBBLICATO );
	}
	
	
	
}
