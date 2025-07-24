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
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import dao.CourseDAO;
import javaBeans.Course;
import javaBeans.User;

/**
 * Servlet implementation class GoToHomeProfessor
 */
@WebServlet("/GoToHomeProfessor")
public class GoToHomeProfessor extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;

	public GoToHomeProfessor() {
		super();
	}

	public void init() throws ServletException {
		try {
			ServletContext servletContext = getServletContext();
			ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);
			templateResolver.setTemplateMode(TemplateMode.HTML);
			this.templateEngine = new TemplateEngine();
			this.templateEngine.setTemplateResolver(templateResolver);
			templateResolver.setSuffix(".html");
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

			String path = "/WEB-INF/HomeProfessorCourses.html";
			ServletContext servletContext = getServletContext();
			final WebContext ctx = new WebContext(req, res, servletContext, req.getLocale());
			ctx.setVariable("courses", courses);
			templateEngine.process(path, ctx, res.getWriter());

		} catch (SQLException e) {
			res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database access failed");
		}
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
