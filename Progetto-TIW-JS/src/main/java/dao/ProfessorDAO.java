package dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javaBeans.Mark;
import javaBeans.User;
import javaBeans.Appeal;
import javaBeans.EvaluationState;

public class ProfessorDAO {
	private Connection con;
	
	public ProfessorDAO(Connection connection) {
		this.con = connection;
	}
	
	public void insertEvaluation(int studentId, String voto, Appeal appeal) throws SQLException{
		String query = "UPDATE iscritto SET voto = ?, statovalutazione = 'INSERITO' WHERE (statovalutazione = 'NON_INSERITO' OR statovalutazione = 'INSERITO') AND matricolastudente = ? AND appello = ? AND corso = ?";
		con.setAutoCommit(false);
		PreparedStatement pstatement = null;
		
		try {
			pstatement = con.prepareStatement(query);
			pstatement.setString(1, voto);
			pstatement.setInt(2, studentId);
			pstatement.setDate(3, appeal.getData());
			pstatement.setInt(4, appeal.getIdCorso());
			
			pstatement.executeUpdate();
			con.commit();
		}catch(SQLException e) {
			con.rollback();
		}finally {
			con.setAutoCommit(true);
			try {
				if (pstatement != null) {
					pstatement.close();
				}
			} catch (Exception e1) {
				throw new SQLException("Cannot close statement");
			}
		}
	}
	
	public Map<User, Mark> findRegisteredStudentsByAppeal(int courseId, Date appealDate) throws SQLException{		
		String query = "SELECT * FROM iscritto, utente WHERE matricolastudente = matricola AND appello = ? AND corso = ?";
		// Using LinkedHashMap to keep insertion order
		Map<User, Mark> registeredStudents = new LinkedHashMap<>();
		PreparedStatement pstatement = null;
		//SQLServerCallableStatement pstatement = null;
		ResultSet result = null;
		User student = null;
		Mark mark = null;
		
		try{			
			pstatement = con.prepareStatement(query);
			pstatement.setDate(1, appealDate);
			pstatement.setInt(2, courseId);
			
			result = pstatement.executeQuery();
			while(result.next()) {
				student = new User();
				mark = new Mark();
				student.setRuolo("studente");
				student.setMatricola(result.getInt("matricola"));
				student.setNome(result.getString("nome"));
				student.setCognome(result.getString("cognome"));
				student.setMail(result.getString("email"));
				student.setCorsoDiLaurea(result.getString("corsodilaurea"));
				mark.setMark(result.getString("voto"));
				mark.setStatoValutazione(EvaluationState.valueOf(result.getString("statovalutazione")));
				
				registeredStudents.put(student, mark);
			}
		} catch (SQLException e){
			throw new SQLException(e);
		} finally {
			try {
				if (result != null) {
					result.close();
				}
			} catch (Exception e1) {
				throw new SQLException("Cannot close result");
			}
			try {
				if (pstatement != null) {
					pstatement.close();
				}
			} catch (Exception e1) {
				throw new SQLException("Cannot close statement");
			}
		}
		
		return registeredStudents;
	}
	
	
	public void publishEvaluation(Appeal appeal) throws SQLException{
		String query = "UPDATE iscritto SET statovalutazione = 'PUBBLICATO' WHERE statovalutazione = 'INSERITO' AND appello = ? AND corso = ?";
		PreparedStatement pstatement = null;
		
		con.setAutoCommit(false);
		
		try{
			pstatement = con.prepareStatement(query);
			pstatement.setDate(1, appeal.getData());
			pstatement.setInt(2, appeal.getIdCorso());
			
			pstatement.executeUpdate();
			con.commit();
		}catch(SQLException e) {
			con.rollback();
		}finally {
			con.setAutoCommit(true);
			
			try {
				if (pstatement != null) {
					pstatement.close();
				}
			} catch (Exception e1) {
				throw new SQLException("Cannot close statement");
			}
		}	
	}
	
//	public Report registerEvaluation(Date appealDate) throws SQLException{
//		String query = "UPDATE dbprogettotiw.iscritto SET statovalutazione = ? WHERE statovalutazione = ? AND appello = ?";
//		ReportDAO reportDAO = new ReportDAO(con);
//		Report report = null;
//		PreparedStatement pstatement = null;
//		
//		con.setAutoCommit(false);
//		
//		try{
//			pstatement = con.prepareStatement(query);
//			pstatement.setString(1, "VERBALIZZATO");
//			pstatement.setString(2, "PUBBLICATO");
//			pstatement.setDate(3, appealDate);
//			
//			pstatement.executeUpdate();
//			
//			report = reportDAO.createReport();
//			
//			con.commit();
//		}catch(SQLException e) {
//			con.rollback();
//		}finally {
//			con.setAutoCommit(true);
//			
//			try {
//				if (pstatement != null) {
//					pstatement.close();
//				}
//			} catch (Exception e1) {
//				throw new SQLException("Cannot close statement");
//			}
//		}
//		
//		return report;
//	}
}
