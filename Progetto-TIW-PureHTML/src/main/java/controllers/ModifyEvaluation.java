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
import dao.StudentDAO;
import javaBeans.Appeal;
import javaBeans.User;


/**
 * Servlet implementation class PublishEvaluation
 */
@WebServlet("/ModifyEvaluation")
public class ModifyEvaluation extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;

	public ModifyEvaluation() {
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
			if (!u.getRuolo().equals("docente")) {
				response.sendRedirect(loginpath);
				return;
			}
		}
		
		String sId = request.getParameter("studentId");
		String appealDate = request.getParameter("appealDate");
		String courseId = request.getParameter("courseId");
		String alertMessage = null;
		if(sId == null || appealDate == null || courseId == null) {
			alertMessage = "Missing Parameters";
			String path = "/WEB-INF/ModifyStudentEvaluation.html";
			ServletContext servletContext = getServletContext();
			final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
			ctx.setVariable("alertMessage", alertMessage);
			templateEngine.process(path, ctx, response.getWriter());
			//response.sendRedirect(getServletContext().getContextPath() + "/GetRegisteredStudentsByAppeal?dataAppello=" + appealDate + "&idCorso=" + courseId + "&sortBy=matricola&order=ASC");
			return;
			//response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameters in evaluation publication");
			//return;
		}
		
		int studentId;
		Date dataAppello = null;
		int idCorso;
		List<Appeal> appelliDocente = null;
		AppealDAO appealDAO = new AppealDAO(connection);
		Appeal appeal = new Appeal();
		
		try {
			studentId = Integer.parseInt(sId);
			dataAppello = Date.valueOf(appealDate);
			idCorso = Integer.parseInt(courseId);
			appeal.setData(dataAppello);
			appeal.setIdCorso(idCorso);
		}catch (IllegalArgumentException e) {
			response.sendRedirect(getServletContext().getContextPath() + "/GetRegisteredStudentsByAppeal?dataAppello=" + appealDate + "&idCorso=" + courseId + "&sortBy=matricola&order=ASC");
			return;
		}
		
		StudentDAO studentDao = new StudentDAO(connection); 
		User student = null;
		try {
			appelliDocente = appealDAO.findAppealByCourseAndProfessor(u.getMatricola(), idCorso);
			
			if(!appelliDocente.contains(appeal)){
				response.sendRedirect(getServletContext().getContextPath() + "/GetRegisteredStudentsByAppeal?dataAppello=" + appeal.getData() + "&idCorso=" + appeal.getIdCorso() + "&sortBy=matricola&order=ASC");
				return;
			}
			
			student = studentDao.findStudentById(studentId);
			if(student == null) {
				alertMessage = "Parameter must be a valid student ID";
			}
			if(student!=null && student.getRuolo().equals("docente")) {
				alertMessage = "Parameter must be a student ID";
				//response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parameter must be a student ID");
				//return;
			}
			if(student!=null && alertMessage==null && !studentDao.registeredForAppeal(studentId, appeal)) {
				alertMessage = "StudentId must be of a registered student";
				//response.sendError(HttpServletResponse.SC_BAD_REQUEST, "StudentId must be of a registered student");
				//return;
			}
		}catch (SQLException e) {
			alertMessage = "Failure of student search in database";
			//response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Failure of student search in database");
			//return;
		}
		
		String path = "/WEB-INF/ModifyStudentEvaluation.html";
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		ctx.setVariable("student", student);
		ctx.setVariable("appealDate", appeal.getData());
		ctx.setVariable("courseId", appeal.getIdCorso());
		ctx.setVariable("alertMessage", alertMessage);
		templateEngine.process(path, ctx, response.getWriter());
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
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
		
		ProfessorDAO professorDao = new ProfessorDAO(connection);
		int studentId = -1;
		Appeal selAppeal = new Appeal();
		int mark;
		List<Appeal> appelliDocente = null;
		AppealDAO appealDAO = new AppealDAO(connection);
		User student = null;
		StudentDAO studentDao = new StudentDAO(connection);
		String alertMessage = null;
		try {
			String mStu = request.getParameter("studentId");
			String corso = request.getParameter("courseId");
			String appello = request.getParameter("appealDate");
			String evaluation = request.getParameter("evaluation");
			
			if(appello != null && corso != null && mStu != null && evaluation != null) {
				selAppeal.setIdCorso(Integer.parseInt(corso));
				selAppeal.setData(Date.valueOf(appello));
				studentId = Integer.parseInt(mStu);
			}else {
				alertMessage = "Missing Parameters";
				String path = "/WEB-INF/ModifyStudentEvaluation.html";
				ServletContext servletContext = getServletContext();
				final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
				ctx.setVariable("alertMessage", alertMessage);
				templateEngine.process(path, ctx, response.getWriter());
				//response.sendRedirect(getServletContext().getContextPath() + "/GetRegisteredStudentsByAppeal?dataAppello=" + appealDate + "&idCorso=" + courseId + "&sortBy=matricola&order=ASC");
				return;
				//response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameters in evaluation publication");
				//return;
			}
			
			if(!(evaluation.equals("") || evaluation.equals("ASSENTE") || 
					evaluation.equals("RIMANDATO") || evaluation.equals("RIPROVATO") || evaluation.equals("30L"))) {
				mark = Integer.parseInt(evaluation);
				if(mark < 18 || mark > 30) {

					alertMessage = "Evaluation not valid";
					String path = "/WEB-INF/ModifyStudentEvaluation.html";
					ServletContext servletContext = getServletContext();
					final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
					ctx.setVariable("alertMessage", alertMessage);
					templateEngine.process(path, ctx, response.getWriter());
					//response.sendRedirect(getServletContext().getContextPath() + "/GetRegisteredStudentsByAppeal?dataAppello=" + appealDate + "&idCorso=" + courseId + "&sortBy=matricola&order=ASC");

					return;
					//response.getWriter().append("PAR ERROR: Parameter is not valid");
					//return;
				}
			}
			
			appelliDocente = appealDAO.findAppealByCourseAndProfessor(u.getMatricola(), selAppeal.getIdCorso());
			
			if(!appelliDocente.contains(selAppeal)){
				response.sendRedirect(getServletContext().getContextPath() + "/GetRegisteredStudentsByAppeal?dataAppello=" + selAppeal.getData() + "&idCorso=" + selAppeal.getIdCorso() + "&sortBy=matricola&order=ASC");
				return;
			}
			
			student = studentDao.findStudentById(studentId);
			if(student.getRuolo().equals("docente")) {
				alertMessage = "Parameter must be a student ID";
				String path = "/WEB-INF/ModifyStudentEvaluation.html";
				ServletContext servletContext = getServletContext();
				final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
				ctx.setVariable("alertMessage", alertMessage);
				templateEngine.process(path, ctx, response.getWriter());
				//response.sendRedirect(getServletContext().getContextPath() + "/GetRegisteredStudentsByAppeal?dataAppello=" + appealDate + "&idCorso=" + courseId + "&sortBy=matricola&order=ASC");
				return;
				//response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parameter must be a student ID");
				//return;
			}
			if(!studentDao.registeredForAppeal(studentId, selAppeal)) {
				alertMessage = "StudentId must be of a registered student";
				String path = "/WEB-INF/ModifyStudentEvaluation.html";
				ServletContext servletContext = getServletContext();
				final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
				ctx.setVariable("alertMessage", alertMessage);
				templateEngine.process(path, ctx, response.getWriter());
				//response.sendRedirect(getServletContext().getContextPath() + "/GetRegisteredStudentsByAppeal?dataAppello=" + appealDate + "&idCorso=" + courseId + "&sortBy=matricola&order=ASC");
				return;
				//response.sendError(HttpServletResponse.SC_BAD_REQUEST, "StudentId must be of a registered student");
				//return;
			}
			
			professorDao.insertEvaluation(studentId, evaluation, selAppeal);
		} catch (IllegalArgumentException e) {
			alertMessage = "PAR ERROR: Parameter is not valid";
			String path = "/WEB-INF/ModifyStudentEvaluation.html";
			ServletContext servletContext = getServletContext();
			final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
			ctx.setVariable("alertMessage", alertMessage);
			templateEngine.process(path, ctx, response.getWriter());
			//response.sendRedirect(getServletContext().getContextPath() + "/GetRegisteredStudentsByAppeal?dataAppello=" + appealDate + "&idCorso=" + courseId + "&sortBy=matricola&order=ASC");
			return;
			//response.getWriter().append("PAR ERROR: Parameter is not valid");
		} catch (SQLException e) {
			// throw new ServletException(e);
			alertMessage = "Failure in project database extraction";
			String path = "/WEB-INF/ModifyStudentEvaluation.html";
			ServletContext servletContext = getServletContext();
			final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
			ctx.setVariable("alertMessage", alertMessage);
			templateEngine.process(path, ctx, response.getWriter());
			//response.sendRedirect(getServletContext().getContextPath() + "/GetRegisteredStudentsByAppeal?dataAppello=" + appealDate + "&idCorso=" + courseId + "&sortBy=matricola&order=ASC");
			return;
			//response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Failure in project database extraction");
		}
		String ctxpath = getServletContext().getContextPath();
		String path = ctxpath + "/GetRegisteredStudentsByAppeal?dataAppello=" + selAppeal.getData() + "&idCorso=" + selAppeal.getIdCorso() + "&sortBy=matricola&order=ASC";
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
