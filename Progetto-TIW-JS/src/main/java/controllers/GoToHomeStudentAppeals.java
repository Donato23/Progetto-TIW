package controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
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

import dao.AppealDAO;
import dao.CourseDAO;
import javaBeans.Appeal;
import javaBeans.Course;
import javaBeans.User;

/**
 * Servlet implementation class GoToHomeStudentAppeals
 */
@WebServlet("/GoToHomeStudentAppeals")
@MultipartConfig
public class GoToHomeStudentAppeals extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GoToHomeStudentAppeals() {
        super();
        // TODO Auto-generated constructor stub
    }
    
    public void init() throws ServletException{
    	ServletContext context = getServletContext();
		
		try {
			String driver = context.getInitParameter("dbDriver");
			String url = context.getInitParameter("dbUrl");
			String user = context.getInitParameter("dbUser");
			String password = context.getInitParameter("dbPassword");
			Class.forName(driver);
			connection = DriverManager.getConnection(url, user, password);

		} catch (ClassNotFoundException e) {
			throw new UnavailableException("Can't load database driver");
		} catch (SQLException e) {
			throw new UnavailableException("Couldn't get db connection");
		}
	}
    
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String loginpath = getServletContext().getContextPath() + "/index.html";
		User u = null;
		HttpSession s = request.getSession();
		if (s.isNew() || s.getAttribute("user") == null) {
			response.sendRedirect(loginpath);
			return;
		} else {
			u = (User) s.getAttribute("user");
			if (!u.getRuolo().equals("studente")) {
				response.sendRedirect(loginpath);
				return;
			}
		}
		AppealDAO appealDAO = new AppealDAO(connection);
		CourseDAO courseDAO = new CourseDAO(connection);
		List<Appeal> appelli = null;
		List<Course> corsi = null;
		try {
			String corso = request.getParameter("idCorso");
			int idCorso; 
			
			if (corso != null) {
				idCorso = Integer.parseInt(corso);
				corsi = courseDAO.findCoursesByStudent(u.getMatricola());
				if (!corsi.stream().mapToInt(c -> c.getId()).anyMatch(i -> i == idCorso)) {//studente frequenta quel corso
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					response.getWriter().println("Student does not attend this course");
					return;
				}
				appelli = appealDAO.findAppealByCourseAndStudent(u.getMatricola(), idCorso);
			}
			else {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing course parameter");
				return;
			}
		} catch (NumberFormatException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("PAR ERROR: parameter is not valid");
			return;
		} catch (SQLException e) {
			// throw new ServletException(e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Database access failed");
			return;
		}

		String json = new Gson().toJson(appelli);
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(json);
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
