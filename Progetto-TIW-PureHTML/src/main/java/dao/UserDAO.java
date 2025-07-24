package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javaBeans.User;

public class UserDAO {
	private Connection con;

	public UserDAO(Connection connection) {
		this.con = connection;
	}
	
	public User checkCredentials(String userId, String password) throws SQLException{
		String query = "SELECT * FROM utente WHERE matricola = ? AND password = ?";
		ResultSet result = null;
		PreparedStatement pstatement = null;
		User user = null;
		
		try {
			pstatement = con.prepareStatement(query);
			pstatement.setString(1, userId);
			pstatement.setString(2, password);
			result = pstatement.executeQuery();
			
			if(result.next()) {
				user = new User();
				user.setRuolo(result.getString("ruolo"));
				user.setMail(result.getString("email"));
				user.setCorsoDiLaurea(result.getString("corsodilaurea"));
				user.setMatricola(result.getInt("matricola"));
				user.setNome(result.getString("nome"));
				user.setCognome(result.getString("cognome"));
				return user;
			}
			
			return null;
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
	}
}
