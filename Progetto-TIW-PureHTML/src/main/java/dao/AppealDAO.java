package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javaBeans.Appeal;

public class AppealDAO {
	private Connection con;

	public AppealDAO(Connection connection) {
		this.con = connection;
	}
	
	public List<Appeal> findAppealByCourseAndStudent(int matricolaStudente, int idCorso) throws SQLException{
		List<Appeal> listOfAppeals = new ArrayList<>();
		Appeal a= null;
		String query = "SELECT corso,appello FROM iscritto WHERE matricolastudente = ? AND corso = ? ORDER BY appello DESC";
		ResultSet result = null;
		PreparedStatement pstatement = null;
		try {
			pstatement = con.prepareStatement(query);
			pstatement.setInt(1, matricolaStudente);
			pstatement.setInt(2, idCorso);
			result = pstatement.executeQuery();
			while (result.next()) {
				a = new Appeal();
				a.setIdCorso(result.getInt("corso"));
				a.setData(result.getDate("appello"));
				listOfAppeals.add(a);
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
		return listOfAppeals;
	}
	
	public List<Appeal> findAppealByCourseAndProfessor(int matricolaDocente, int idCorso) throws SQLException{
		List<Appeal> listOfAppeals = new ArrayList<>();
		Appeal a= null;
		String query = "SELECT idcorso,data FROM appello natural join insegna WHERE matricoladocente = ? AND idcorso = ? ORDER BY data DESC";
		ResultSet result = null;
		PreparedStatement pstatement = null;
		try {
			pstatement = con.prepareStatement(query);
			pstatement.setInt(1, matricolaDocente);
			pstatement.setInt(2, idCorso);
			result = pstatement.executeQuery();
			while (result.next()) {
				a = new Appeal();
				a.setIdCorso(result.getInt("idcorso"));
				a.setData(result.getDate("data"));
				listOfAppeals.add(a);
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
		return listOfAppeals;
	}
	

}
