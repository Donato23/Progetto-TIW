package dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javaBeans.Appeal;
import javaBeans.Mark;
import javaBeans.User;
import javaBeans.EvaluationState;

public class StudentDAO {
	private Connection con;

	public StudentDAO(Connection connection) {
		this.con = connection;
	}
	
	public User findStudentById(int matricolaStudente) throws SQLException {
		User student = null;
		String query = "SELECT * FROM utente  where matricola = ?";
		ResultSet result = null;
		PreparedStatement pstatement = null;
		try {
			pstatement = con.prepareStatement(query);
			pstatement.setInt(1, matricolaStudente);
			result = pstatement.executeQuery();
			if (result.next()) {
				student = new User();
				student.setMatricola(result.getInt("matricola"));
				student.setNome(result.getString("nome"));
				student.setCognome(result.getString("cognome"));
				student.setRuolo(result.getString("ruolo"));
				student.setMail(result.getString("email"));
				student.setCorsoDiLaurea(result.getString("corsodilaurea"));
			}
		} catch (SQLException e) {
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
		
		return student;
	}
	
	public Mark findEvaluationByAppeal(int matricolaStudente, Appeal appello) throws SQLException{
		Mark mark = null;
		int idCorso = appello.getIdCorso();
		String data = appello.getData();
		String query = "SELECT voto,statovalutazione FROM iscritto WHERE (statovalutazione = 'PUBBLICATO' "
				+ "OR statovalutazione = 'RIFIUTATO' "
				+ "OR statovalutazione = 'VERBALIZZATO')"
				+ "AND matricolastudente = ? AND corso = ? AND appello = ?";
		ResultSet result = null;
		PreparedStatement pstatement = null;
		try {
			pstatement = con.prepareStatement(query);
			pstatement.setInt(1, matricolaStudente);
			pstatement.setInt(2, idCorso);
			pstatement.setString(3, data);
			result = pstatement.executeQuery();
			if(result.next()) {
				mark = new Mark();
				mark.setStatoValutazione(EvaluationState.valueOf(result.getString("statovalutazione")));
				mark.setMark(result.getString("voto"));
			}
					
		} catch (SQLException e) {
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
		return mark;
	}
	
	public boolean registeredForAppeal(int matricolaStudente, Appeal appello) throws SQLException {
		int idCorso = appello.getIdCorso();
		String data = appello.getData();
		String query = "SELECT voto,statovalutazione FROM iscritto "
				+ "WHERE matricolastudente = ? AND corso = ? AND appello = ?";
		ResultSet result = null;
		PreparedStatement pstatement = null;
		try {
			pstatement = con.prepareStatement(query);
			pstatement.setInt(1, matricolaStudente);
			pstatement.setInt(2, idCorso);
			pstatement.setString(3, data);
			result = pstatement.executeQuery();
			
			if(result.next()) {
				return true;
			}
					
		} catch (SQLException e) {
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
		return false;
		
	}
	public void rejectEvaluation(int matricolaStudente, Appeal appello) throws SQLException {
		int idCorso = appello.getIdCorso();
		String data = appello.getData();
		String query = "UPDATE iscritto SET statovalutazione = 'RIFIUTATO'"
				+ "WHERE statovalutazione = 'PUBBLICATO' "
				+ "AND matricolastudente = ? AND corso = ? AND appello = ?";
		PreparedStatement pstatement = null;
		con.setAutoCommit(false);
		try {
			pstatement = con.prepareStatement(query);
			pstatement.setInt(1, matricolaStudente);
			pstatement.setInt(2, idCorso);
			pstatement.setString(3, data);
			pstatement.executeUpdate();
			con.commit();
		} catch (SQLException e) {
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
}
