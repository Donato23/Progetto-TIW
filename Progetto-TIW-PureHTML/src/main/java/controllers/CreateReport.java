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
import dao.ReportDAO;
import javaBeans.Appeal;
import javaBeans.Report;
import javaBeans.User;

/**
 * Servlet implementation class CreateReport
 */
@WebServlet("/CreateReport")
public class CreateReport extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection;
	private TemplateEngine templateEngine;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public CreateReport() {
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
		Report newReport = null;
		Appeal appeal = new Appeal();
		ReportDAO reportDAO = new ReportDAO(connection);
		AppealDAO appealDAO = new AppealDAO(connection);
		String report = request.getParameter("idReport");
		String corso = request.getParameter("idCorso");
		String dataAppello = request.getParameter("dataAppello");
		List<Appeal> appelliDocente = null;
		List<Report> verbaliAppello = null;
		String alertMessage = null;
		
		if(corso == null || dataAppello == null || report == null) {
			alertMessage = "Missing parameters in creating report";
			//response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameters in creating report");
		}
		try {
			
			if(alertMessage == null) {
				appeal.setData(Date.valueOf(dataAppello));
				appeal.setIdCorso(Integer.parseInt(corso));
				
				//controllo che l'appello sia tenuto dal professore e che sia un verbale associato a quell'appello
				appelliDocente = appealDAO.findAppealByCourseAndProfessor(u.getMatricola(), Integer.parseInt(corso));
				verbaliAppello = reportDAO.findReportByAppeal(appeal);
				newReport = reportDAO.findReportById(Integer.parseInt(report));
			}
			if(newReport == null) {
				response.sendRedirect(getServletContext().getContextPath() + "/GetRegisteredStudentsByAppeal?idCorso="+corso+"&dataAppello="+dataAppello+"&sortBy=matricola&order=ASC");
				return;
			}
			if(!appelliDocente.contains(appeal) || !verbaliAppello.contains(newReport) ){
				response.sendRedirect(getServletContext().getContextPath() + "/GoToHomeProfessor");
				return;
			}
		}catch (IllegalArgumentException e) {
			alertMessage = "Bad parameter - Parameter was not of required type";
			//response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Bad parameter - Parameter was not of required type");
			//return;
		}catch (SQLException e) {
			alertMessage = "Failure in database extraction and of reportData";
			//response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Failure in database extraction and of reportData");
			//return;
		}
		
		String path = "/WEB-INF/Report.html";
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		ctx.setVariable("report", newReport);
		ctx.setVariable("appello", appeal);
		ctx.setVariable("alertMessage", alertMessage);
		templateEngine.process(path, ctx, response.getWriter());
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
		ReportDAO reportDAO = new ReportDAO(connection);
		AppealDAO appealDAO = new AppealDAO(connection);
		Report newReport = null;
		Appeal appeal = new Appeal();
		String corso = request.getParameter("idCorso");
		String dataAppello = request.getParameter("dataAppello");
		List<Appeal> appelliDocente = null;
		String alertMessage = null;
		
		if(corso == null || dataAppello == null) {
			alertMessage = "Missing parameters in creating report";
			String path = "/WEB-INF/Report.html";
			ServletContext servletContext = getServletContext();
			final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
			ctx.setVariable("report", newReport);
			ctx.setVariable("appello", appeal);
			ctx.setVariable("alertMessage", alertMessage);
			templateEngine.process(path, ctx, response.getWriter());
			//response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameters in creating report");
			return;
		}
		
		try {
			appeal.setData(Date.valueOf(dataAppello));
			appeal.setIdCorso(Integer.parseInt(corso));
			
			//controllo che l'appello sia tenuto dal professore e che sia un verbale associato a quell'appello
			appelliDocente = appealDAO.findAppealByCourseAndProfessor(u.getMatricola(), Integer.parseInt(corso));
			if(!appelliDocente.contains(appeal)){
				response.sendRedirect(getServletContext().getContextPath() + "/GoToHomeProfessor");
				return;
			}
			
			newReport = reportDAO.createReport(appeal);
			
			if(newReport == null) {
				response.sendRedirect(getServletContext().getContextPath() + "/GetRegisteredStudentsByAppeal?idCorso="+corso+"&dataAppello="+dataAppello+"&sortBy=matricola&order=ASC");
				return;
			}
		}catch (IllegalArgumentException e) {
			alertMessage = "Bad parameter - Parameter was not of required type";
			String path = "/WEB-INF/Report.html";
			ServletContext servletContext = getServletContext();
			final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
			ctx.setVariable("report", newReport);
			ctx.setVariable("appello", appeal);
			ctx.setVariable("alertMessage", alertMessage);
			templateEngine.process(path, ctx, response.getWriter());
			return;
			//response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Bad parameter - Parameter was not of required type");
			//return;
		}catch (SQLException e) {
			alertMessage = "Failure in database extraction and creatinig report";
			String path = "/WEB-INF/Report.html";
			ServletContext servletContext = getServletContext();
			final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
			ctx.setVariable("report", newReport);
			ctx.setVariable("appello", appeal);
			ctx.setVariable("alertMessage", alertMessage);
			templateEngine.process(path, ctx, response.getWriter());
			return;
			//response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Failure in database extraction and creatinig report");
			//return;
		}
		response.sendRedirect(getServletContext().getContextPath() + "/CreateReport?idReport="+newReport.getId() +"&idCorso="+corso+"&dataAppello="+dataAppello);
		return;
		
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
