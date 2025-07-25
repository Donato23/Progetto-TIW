package controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.SQLException;

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
import dao.StudentDAO;
import javaBeans.Appeal;
import javaBeans.Course;
import javaBeans.Mark;
import javaBeans.User;

/**
 * Servlet implementation class GetEvaluationByAppeal
 */
@WebServlet("/GetEvaluationByAppeal")
public class GetEvaluationByAppeal extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection;
	private TemplateEngine templateEngine;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public GetEvaluationByAppeal() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public void init() throws ServletException {
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
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
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
		StudentDAO studentDAO = new StudentDAO(connection);
		CourseDAO courseDAO = new CourseDAO(connection);
		Mark mark = null;
		Course selCourse = null;
		Appeal selAppeal = new Appeal(); 
		String alertMessage = null;
		
		try {
			int mStu = u.getMatricola();
			String corso = request.getParameter("idCorso");
			String appello = request.getParameter("dataAppello");
			if(appello != null && corso != null) {
				selAppeal.setIdCorso(Integer.parseInt(corso));
				selAppeal.setData(Date.valueOf(appello));
				
				if (!studentDAO.registeredForAppeal(mStu, selAppeal)) {
					response.sendRedirect(getServletContext().getContextPath() + "/GoToHomeStudentAppeals?idCorso="+corso);
					return;
				}	
				mark = studentDAO.findEvaluationByAppeal(mStu,selAppeal);
				selCourse = courseDAO.findCoursesByID(Integer.parseInt(corso));
			}
			else {
				alertMessage = "Missing Parameter";
			}
			
			
		} catch (IllegalArgumentException e) {
			alertMessage = "PAR ERROR: parameter is not valid";
			//response.getWriter().append("PAR ERROR: parameter is not valid");
			//return;
		} catch (SQLException e) {
			// throw new ServletException(e);
			alertMessage = "Failure in database extraction";
			//response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Failure in project database extraction");
			//return;
		}
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		String path = "/WEB-INF/Evaluation.html";
		ctx.setVariable("appello", selAppeal);
		ctx.setVariable("mark", mark);
		ctx.setVariable("corso", selCourse);
		ctx.setVariable("alertMessage", alertMessage);
		templateEngine.process(path, ctx, response.getWriter());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
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
