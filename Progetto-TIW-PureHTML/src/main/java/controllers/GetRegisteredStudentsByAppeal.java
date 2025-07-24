package controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import javaBeans.Mark;
import javaBeans.User;

/**s
 * Servlet implementation class GoToHomeProfessorAppeals
 */
@WebServlet("/GetRegisteredStudentsByAppeal")
public class GetRegisteredStudentsByAppeal extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection;
	private TemplateEngine templateEngine;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetRegisteredStudentsByAppeal() {
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
			if (!u.getRuolo().equals("docente")) {
				response.sendRedirect(loginpath);
				return;
			}
		}
		
		ProfessorDAO professorDAO = new ProfessorDAO(connection);
		AppealDAO appealDAO = new AppealDAO(connection);
		
		Map<User, Mark> registeredStudentsEvaluations = new HashMap<>();
		List<Appeal> appelliDocente = null;
		Appeal appeal = new Appeal();
		int idCorso=-1;
		Date appealDate = null;
		String sortBy = null;
		String order = null;
		List<String> sortByWhitelist = new ArrayList<>();
		List<String> orderWhitelist = new ArrayList<>();
		String alertMessage = null;
		sortByWhitelist.add("matricola");
		sortByWhitelist.add("nome");
		sortByWhitelist.add("cognome");
		sortByWhitelist.add("email");
		sortByWhitelist.add("corsodilaurea");
		sortByWhitelist.add("voto");
		sortByWhitelist.add("statovalutazione");
		orderWhitelist.add("ASC");
		orderWhitelist.add("DESC");
		try {
			String corso = request.getParameter("idCorso");
			String appello = request.getParameter("dataAppello");
			sortBy = request.getParameter("sortBy");
			order = request.getParameter("order");
			
			if(appello != null && corso != null && sortBy != null && order != null) {
				idCorso = Integer.parseInt(corso);
				appealDate = Date.valueOf(appello);
				
				appeal.setData(appealDate);
				appeal.setIdCorso(idCorso);
				
				//controllo che l'appello sia tenuto dal professore
				appelliDocente = appealDAO.findAppealByCourseAndProfessor(u.getMatricola(), idCorso);
				
				if(!appelliDocente.contains(appeal)){
					response.sendRedirect(getServletContext().getContextPath() + "/GoToHomeProfessorAppeals?idCorso=" + corso);
					return;
				}
				
				if(!sortByWhitelist.contains(sortBy) || !orderWhitelist.contains(order)) {
					response.sendRedirect(getServletContext().getContextPath() + "/GetRegisteredStudentsByAppeal?idCorso="+corso+"&dataAppello="+appello+"&sortBy=matricola&order=ASC");
					return;
				}
				
				registeredStudentsEvaluations = professorDAO.findRegisteredStudentsByAppeal(idCorso, appealDate, sortBy, order);
			}
			else {
				alertMessage = "Missing Parameter";
				//response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameter");
				//return;
			}
			
	
		}catch(IllegalArgumentException e){
			alertMessage = "PAR ERROR: parameter is not valid";
			//response.getWriter().append("PAR ERROR: parameter is not valid");
			//return;
		}
		catch (SQLException e) {
			// throw new ServletException(e);
			alertMessage = "Failure in database extraction";
			//response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Failure in database extraction");
			//return;
		}
		String path = "/WEB-INF/RegisteredStudents.html";
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		ctx.setVariable("registeredStudentsEvaluations", registeredStudentsEvaluations);
		ctx.setVariable("appealDate", appealDate);
		ctx.setVariable("courseId", idCorso);
		ctx.setVariable("sortBy", sortBy);
		ctx.setVariable("order", order);
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