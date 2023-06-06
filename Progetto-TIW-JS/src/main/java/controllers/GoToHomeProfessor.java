package controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;

import dao.CourseDAO;
import javaBeans.Course;
import javaBeans.User;

/**
 * Servlet implementation class GoToHomeProfessor
 */
@WebServlet("/GoToHomeProfessor")
@MultipartConfig
public class GoToHomeProfessor extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;

	public GoToHomeProfessor() {
		super();
	}

	public void init() throws ServletException {
		try {
			ServletContext servletContext = getServletContext();
			String driver = servletContext.getInitParameter("dbDriver");
			String url = servletContext.getInitParameter("dbUrl");
			String user = servletContext.getInitParameter("dbUser");
			String password = servletContext.getInitParameter("dbPassword");
			Class.forName(driver);
			connection = DriverManager.getConnection(url, user, password);

		} catch (ClassNotFoundException e) {
			throw new UnavailableException("Can't load database driver");
		} catch (SQLException e) {
			throw new UnavailableException("Couldn't get db connection");
		}
	}

	public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		String loginpath = getServletContext().getContextPath() + "/index.html";
		User u = null;
		HttpSession s = req.getSession();
		if(s.isNew() || s.getAttribute("user") == null) {
			res.sendRedirect(loginpath);
			return;
		}else {
			u = (User) s.getAttribute("user");
			if(!u.getRuolo().equals("docente")) {
				res.sendRedirect(loginpath);
				return;
			}
		}
		
		CourseDAO cDao = new CourseDAO(connection);
		List<Course> courses = new ArrayList<>();

		// Find topic with ID, if not present return error, otherwise obtain messages
		try {
			courses = cDao.findCoursesByProfessor(u.getMatricola());

		} catch (SQLException e) {
			res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			res.getWriter().println("Database access failed");
		}
		
		String json = new Gson().toJson(courses);
		res.setStatus(HttpServletResponse.SC_OK);
		res.setContentType("application/json");
		res.setCharacterEncoding("UTF-8");
		res.getWriter().write(json);
	}
	

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

	public void destroy() {
		try {
			if (connection != null) {
				connection.close();
			}
		} catch (SQLException sqle) {
		}
	}
}
