package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javaBeans.Course;

public class CourseDAO {
	private Connection con;

	public CourseDAO(Connection connection) {
		this.con = connection;
	}
	
	public List<Course> findCoursesByStudent(int matricolaStudente) throws SQLException{
		List<Course> listOfCourses = new ArrayList<>();
		Course c = null;
		String query = "SELECT * FROM frequenta, corso  where matricolastudente = ? AND idcorso = id ORDER BY nome DESC";
		ResultSet result = null;
		PreparedStatement pstatement = null;
		try {
			pstatement = con.prepareStatement(query);
			pstatement.setInt(1, matricolaStudente);
			result = pstatement.executeQuery();
			while (result.next()) {
				c = new Course();
				c.setId(result.getInt("idcorso"));
				c.setNome(result.getString("nome"));
				listOfCourses.add(c);
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
		return listOfCourses;
	}

	public List<Course> findCoursesByProfessor(int matricolaDocente) throws SQLException{
		
		List<Course> listOfCourses = new ArrayList<>();
		Course c = null;
		String query = "SELECT * FROM insegna, corso  where matricoladocente = ? AND idcorso = id ORDER BY nome DESC";
		ResultSet result = null;
		PreparedStatement pstatement = null;
		try {
			pstatement = con.prepareStatement(query);
			pstatement.setInt(1, matricolaDocente);
			result = pstatement.executeQuery();
			while (result.next()) {
				c = new Course();
				c.setId(result.getInt("idcorso"));
				c.setNome(result.getString("nome"));
				listOfCourses.add(c);
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
		return listOfCourses;
	}

	public Course findCoursesByID(int idCorso) throws SQLException {
		Course c = null;
		String query = "SELECT * FROM corso  where id = ?";
		ResultSet result = null;
		PreparedStatement pstatement = null;
		try {
			pstatement = con.prepareStatement(query);
			pstatement.setInt(1, idCorso);
			result = pstatement.executeQuery();
			if (result.next()) {
				c = new Course();
				c.setId(result.getInt("id"));
				c.setNome(result.getString("nome"));
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
		
		return c;
	}

}
