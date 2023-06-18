package dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.sql.Time;
import javaBeans.Appeal;
import javaBeans.Report;
import javaBeans.User;

public class ReportDAO {
	private Connection con;

	public ReportDAO(Connection connection) {
		this.con = connection;
	}
	
	public Report createReport(Appeal appello) throws SQLException {
		int idCorso = appello.getIdCorso();
		String dataAppello = appello.getData();
		
		int codiceVerbale;
		String dataVerbale = LocalDate.now().toString();
		LocalTime ora = LocalTime.now();
		Map<User,String> datiVerbale = null;
		
		Report newReport = new Report();
		newReport.setData(dataVerbale);
		newReport.setOra(ora.toString());
		
		String query0 = "SELECT MAX(codice) AS ultimocodice FROM verbale";
		String query1 = "INSERT into verbale (codice, data, ora) VALUES(?, ?, ?)";
		String query2 = "UPDATE iscritto SET verbale = ? , statovalutazione = 'VERBALIZZATO' "
				+ " WHERE statovalutazione = 'PUBBLICATO'"
				+ " AND corso = ? AND appello = ? ";
		String query3 = "UPDATE iscritto SET verbale = ?, voto = 'RIMANDATO', statovalutazione = 'VERBALIZZATO' "
				+ " WHERE statovalutazione = 'RIFIUTATO' "
				+ " AND corso = ? AND appello = ? ";
		PreparedStatement pstatement0 = null;
		PreparedStatement pstatement1 = null;
		PreparedStatement pstatement2 = null;
		PreparedStatement pstatement3 = null;
		ResultSet result = null;
		
		con.setAutoCommit(false);
		
		try {
			
			//trovo il primo codice da assegnare al nuovo report
			pstatement0 = con.prepareStatement(query0);
			result = pstatement0.executeQuery();
			if(!result.next())//non ci sono verbali 
				codiceVerbale = 1;
			else codiceVerbale = result.getInt("ultimocodice")+1;
			newReport.setId(codiceVerbale);
			
			// creo il nuovo report
			pstatement1 = con.prepareStatement(query1);
			pstatement1.setInt(1, codiceVerbale);
			pstatement1.setDate(2, Date.valueOf(dataVerbale));
			pstatement1.setTime(3, Time.valueOf(ora));
			pstatement1.executeUpdate();
			
			// update statovalutazione e aggiungo codiceVerbale in iscritto per i voti non rifiutati
			pstatement2 = con.prepareStatement(query2);
			pstatement2.setInt(1, codiceVerbale);
			pstatement2.setInt(2, idCorso);
			pstatement2.setString(3, dataAppello);
			pstatement2.executeUpdate();
			
			// update statovalutazione e aggiungo codiceVerbale in iscritto per i voti rifiutati
			pstatement3 = con.prepareStatement(query3);
			pstatement3.setInt(1, codiceVerbale);
			pstatement3.setInt(2, idCorso);
			pstatement3.setString(3, dataAppello);
			pstatement3.executeUpdate();
			
			// trovo i dati degli studenti da mostrare nel verbale
			datiVerbale = findReportData(codiceVerbale);
			if(datiVerbale.isEmpty()) {
				con.rollback();
				return null;
			}
			
			newReport.setStudentData(datiVerbale);
			
			con.commit();
		} catch (SQLException e) {
			con.rollback();
			return null;
		} finally {
			con.setAutoCommit(true);
			try {
				if (result!=null) {
					result.close();
				}
				if (pstatement1 != null) {
					pstatement1.close();
				}
				if (pstatement2 != null) {
					pstatement2.close();
				}
				if (pstatement3 != null) {
					pstatement3.close();
				}
			} catch (Exception e1) {}
			
		}
		return newReport;
	}
	
	public List<Report> findReportByAppeal(Appeal appello) throws SQLException{
		int idCorso = appello.getIdCorso();
		String dataAppello = appello.getData();
		Map<User,String> datiVerbale= null;
		List<Report> reports = new ArrayList<>();
		Report report = null;
		
		String query = "SELECT DISTINCT(codice), data, ora FROM iscritto, verbale  "
				+ "WHERE verbale=codice AND corso = ? AND appello = ? ";
		PreparedStatement pstatement = null;
		ResultSet result = null;
		
		con.setAutoCommit(false);
		
		try {
			pstatement = con.prepareStatement(query);
			pstatement.setInt(1, idCorso);
			pstatement.setString(2, dataAppello);
			result = pstatement.executeQuery();
			
			while(result.next()) {
				report = new Report();
				report.setId(result.getInt("codice"));
				report.setData(result.getDate("data").toString());
				report.setOra(result.getTime("ora").toLocalTime().toString());
				datiVerbale = findReportData(result.getInt("codice"));
				report.setStudentData(datiVerbale);
				reports.add(report);
			}
			con.commit();
		} catch (SQLException e) {
			con.rollback();
			return null;
		}finally {
			con.setAutoCommit(true);
			try {
				if (result!=null) {
					result.close();
				}
				if (pstatement != null) {
					pstatement.close();
				}
			} catch (Exception e1) {}
			
		}
		return reports;
	}
	public Report findReportById(int codiceVerbale) throws SQLException {
		Report report = new Report();
		Map<User, String> studentData = null;
		String query = "SELECT * FROM verbale WHERE codice = ?";
		PreparedStatement pstatement = null;
		ResultSet result = null;
		
		try {
			
			pstatement = con.prepareStatement(query);
			pstatement.setInt(1, codiceVerbale);
			result = pstatement.executeQuery();
			
			if (!result.next())// non ci sono verbali
				return null;
			
			report.setId(result.getInt("codice"));
			report.setData(result.getDate("data").toString());
			report.setOra(result.getTime("ora").toLocalTime().toString());
			
			studentData = findReportData(codiceVerbale);
			if(studentData == null)
				return null;
			report.setStudentData(studentData);
			System.out.println(studentData);
		} catch (SQLException e) {
			return null;
		} finally {
			try {
				if (result != null) {
					result.close();
				}
				if (pstatement != null) {
					pstatement.close();
				}
			} catch (Exception e1) {
			}

		}
		return report;
	}
			
	private Map<User, String> findReportData(int codiceVerbale) throws SQLException{
		Map<User,String> datiVerbale = new HashMap<>();
		User studente;
		String voto;
		String query = "SELECT matricolastudente, nome, cognome, voto FROM iscritto, utente "
				+ "WHERE verbale = ? AND matricolastudente = matricola";
		PreparedStatement pstatement = null;
		ResultSet result = null;
		
		pstatement = con.prepareStatement(query);
		pstatement.setInt(1, codiceVerbale);
		result = pstatement.executeQuery();
		int i=0;
		while(result.next()) {
			i++;
			studente = new User();
			studente.setRuolo("studente");
			studente.setMatricola(result.getInt("matricolastudente"));
			studente.setNome(result.getString("nome"));
			studente.setCognome(result.getString("cognome"));
			voto = result.getString("voto");
			datiVerbale.put(studente, voto);
		}
		return datiVerbale;
	}
}
