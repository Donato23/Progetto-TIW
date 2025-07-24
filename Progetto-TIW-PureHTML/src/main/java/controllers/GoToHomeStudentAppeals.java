package controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import dao.AppealDAO;
import dao.CourseDAO;
import javaBeans.Appeal;
import javaBeans.Course;
import javaBeans.User;

/**
 * Servlet implementation class GoToHomeStudentAppeals
 */
@WebServlet("/GoToHomeStudentAppeals")
public class GoToHomeStudentAppeals extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection;
	private TemplateEngine templateEngine;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GoToHomeStudentAppeals() {
        super();
        // TODO Auto-generated constructor stub
    }
    
    public void init() throws ServletException{
    	ServletContext context = getServletContext();
    	ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(context);
		templateResolver.setTemplateMode(TemplateMode.HTML);
		this.templateEngine = new TemplateEngine();
		this.templateEngine.setTemplateResolver(templateResolver);
		templateResolver.setSuffix(".html");
		
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
		String courseName = null;
		String alertMessage = null;
		try {
			String corso = request.getParameter("idCorso");
			int idCorso; 
			
			if (corso != null) {
				idCorso = Integer.parseInt(corso);
				corsi = courseDAO.findCoursesByStudent(u.getMatricola());
				if (!corsi.stream().mapToInt(c -> c.getId()).anyMatch(i -> i == idCorso)) {//studente frequenta quel corso
					response.sendRedirect(getServletContext().getContextPath() + "/GoToHomeStudent");
					return;
				}
				appelli = appealDAO.findAppealByCourseAndStudent(u.getMatricola(), idCorso);
				courseName = courseDAO.findCoursesByID(idCorso).getNome();
			}
			else {
				alertMessage = "Missing Parameter";
				//response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing course parameter");
				//return;
			}
		} catch (NumberFormatException e) {
			alertMessage = "PAR ERROR: parameter is not valid";
			//response.getWriter().append("PAR ERROR: parameter is not valid");
			//return;
		} catch (SQLException e) {
			// throw new ServletException(e);
			alertMessage = "Failure in student's appeals database extraction";
			//response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Failure in student's appeals database extraction");
			//return;
		}
		String path = "/WEB-INF/HomeStudentAppeals.html";
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		ctx.setVariable("appelli", appelli);
		ctx.setVariable("courseName", courseName);
		ctx.setVariable("alertMessage", alertMessage);
		templateEngine.process(path, ctx, response.getWriter());
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
