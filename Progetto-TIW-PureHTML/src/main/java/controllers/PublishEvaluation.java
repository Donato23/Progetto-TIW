package controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
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
import dao.ProfessorDAO;
import javaBeans.Appeal;
import javaBeans.User;

/**
 * Servlet implementation class PublishEvaluation
 */
@WebServlet("/PublishEvaluation")
public class PublishEvaluation extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection;
	private TemplateEngine templateEngine; 
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public PublishEvaluation() {
        super();
        // TODO Auto-generated constructor stub
    }
    
	public void init() throws ServletException {
		ServletContext servletContext = getServletContext();
		ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);
		templateResolver.setTemplateMode(TemplateMode.HTML);
		this.templateEngine = new TemplateEngine();
		this.templateEngine.setTemplateResolver(templateResolver);
		templateResolver.setSuffix(".html");
		try {
			ServletContext context = getServletContext();
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
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String loginpath = getServletContext().getContextPath() + "/index.html";
		User u = null;
		HttpSession s = request.getSession();
		if (s.isNew() || s.getAttribute("user") == null) {
			response.sendRedirect(loginpath);
			return;
		} else {
			u = (User) s.getAttribute("user");
			if (!u.getRuolo().equals("docente")) {
				response.sendRedirect(loginpath);
				return;
			}
		}
		
		String appealDate = request.getParameter("appealDate");
		String courseId = request.getParameter("courseId");
		String alertMessage = null;
		List<Appeal> appelliDocente = null;
		AppealDAO appealDAO = new AppealDAO(connection);

		if(appealDate == null || courseId == null) {
			alertMessage = "Missing parameters in evaluation publication";
			String path = "/WEB-INF/RegisteredStudents.html";
			ServletContext servletContext = getServletContext();
			final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
			ctx.setVariable("registeredStudentsEvaluations", null);
			ctx.setVariable("appealDate", appealDate);
			ctx.setVariable("courseId", courseId);
			ctx.setVariable("sortBy", "matricola");
			ctx.setVariable("order", "ASC");
			ctx.setVariable("alertMessage", alertMessage);
			templateEngine.process(path, ctx, response.getWriter());
			//response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameters in evaluation publication");
			return;
		}
		Appeal appeal = new Appeal();
		try {
			appeal.setData(Date.valueOf(appealDate));
			appeal.setIdCorso(Integer.parseInt(courseId));
		}catch (IllegalArgumentException e) {
			alertMessage = "Bad parameter - Parameter was not of required type";
			String path = "/WEB-INF/RegisteredStudents.html";
			ServletContext servletContext = getServletContext();
			final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
			ctx.setVariable("registeredStudentsEvaluations", null);
			ctx.setVariable("appealDate", appealDate);
			ctx.setVariable("courseId", courseId);
			ctx.setVariable("sortBy", "matricola");
			ctx.setVariable("order", "ASC");
			ctx.setVariable("alertMessage", alertMessage);
			templateEngine.process(path, ctx, response.getWriter());
			//response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameters in evaluation publication");
			return;
			//response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Bad parameter - Parameter was not of required type");
			//return;
		}
		
		ProfessorDAO professorDao = new ProfessorDAO(connection); 
		try {
			appelliDocente = appealDAO.findAppealByCourseAndProfessor(u.getMatricola(), appeal.getIdCorso());
			
			if(!appelliDocente.contains(appeal)){
				response.sendRedirect(getServletContext().getContextPath() + "/GetRegisteredStudentsByAppeal?dataAppello=" + appeal.getData() + "&idCorso=" + appeal.getIdCorso() + "&sortBy=matricola&order=ASC");
				return;
			}
			
			professorDao.publishEvaluation(appeal);
		}catch (SQLException e) {
			alertMessage = "Failure in publishing evaluation in database";
			String path = "/WEB-INF/RegisteredStudents.html";
			ServletContext servletContext = getServletContext();
			final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
			ctx.setVariable("registeredStudentsEvaluations", null);
			ctx.setVariable("appealDate", appealDate);
			ctx.setVariable("courseId", courseId);
			ctx.setVariable("sortBy", "matricola");
			ctx.setVariable("order", "ASC");
			ctx.setVariable("alertMessage", alertMessage);
			templateEngine.process(path, ctx, response.getWriter());
			//response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameters in evaluation publication");
			return;
			//response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Failure in publishing evaluation in database");
			//return;
		}
		
		String ctxpath = getServletContext().getContextPath();
		String path = ctxpath + "/GetRegisteredStudentsByAppeal?dataAppello=" + appeal.getData() + "&idCorso=" + appeal.getIdCorso()+"&sortBy=matricola&order=ASC";
		response.sendRedirect(path);
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
