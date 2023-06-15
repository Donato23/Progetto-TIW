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
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import dao.AppealDAO;
import dao.ReportDAO;
import javaBeans.Appeal;
import javaBeans.Report;
import javaBeans.User;

/**
 * Servlet implementation class CreateReport
 */
@WebServlet("/CreateReport")
@MultipartConfig
public class CreateReport extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public CreateReport() {
        super();
        // TODO Auto-generated constructor stub
    }
    public void init() throws ServletException {
		ServletContext servletContext = getServletContext();
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
		
		if(corso == null || dataAppello == null || report == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Missing parameters");
			return;
		}
		try {
			appeal.setData(dataAppello);
			appeal.setIdCorso(Integer.parseInt(corso));
			
			//controllo che l'appello sia tenuto dal professore e che sia un verbale associato a quell'appello
			appelliDocente = appealDAO.findAppealByCourseAndProfessor(u.getMatricola(), Integer.parseInt(corso));
			verbaliAppello = reportDAO.findReportByAppeal(appeal);
			
			newReport = reportDAO.findReportById(Integer.parseInt(report));
			if(newReport == null) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().println("Report not found ");
				return;
			}
			if(!appelliDocente.contains(appeal) || !verbaliAppello.contains(newReport) ){
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().println("No appeals on this date for this course");
				return;
			}
		}catch (IllegalArgumentException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Bad parameter - Parameter was not of required type");
			return;
		}catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
			response.getWriter().println("Failure in database extraction and of reportData");
			return;
		}
		
		JsonObject reportData = new JsonObject();
		JsonArray studentsData = new JsonArray();
		reportData.addProperty("id", newReport.getId());
		reportData.addProperty("data", newReport.getData());
		reportData.addProperty("ora", newReport.getOra());
		for(User key : newReport.getStudentData().keySet()) {
			JsonObject studentData = new JsonObject();
			studentData.addProperty("studentId", key.getMatricola());
			studentData.addProperty("studentName", key.getNome());
			studentData.addProperty("studentSurname", key.getCognome());
			studentData.addProperty("studentMark", newReport.getStudentData().get(key));
			studentsData.add(studentData);
		}
		reportData.add("studentsData", studentsData);
		
		String json = new Gson().toJson(reportData);
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(json);
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
		
		if(corso == null || dataAppello == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Missing parameters in report creation");
			return;
		}
		
		try {
			appeal.setData(dataAppello);
			appeal.setIdCorso(Integer.parseInt(corso));
			
			//controllo che l'appello sia tenuto dal professore e che sia un verbale associato a quell'appello
			appelliDocente = appealDAO.findAppealByCourseAndProfessor(u.getMatricola(), Integer.parseInt(corso));
			if(!appelliDocente.contains(appeal)){
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().println("No appeals on this date for this course");
				return;
			}
			
			newReport = reportDAO.createReport(appeal);
			
//			if(newReport == null) {
//				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
//				response.getWriter().println("Impossible to create a new report, there are no published evaluations");
//				return;
//			}
		}catch (IllegalArgumentException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("PAR ERROR: Parameter is not valid");
			return;
		}catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
			response.getWriter().println("Failure in database extraction");
			return;
		}
		
		String json = new Gson().toJson(-1) ;
		
		if(newReport != null) {
			json = new Gson().toJson(newReport.getId()); 
			System.out.println("sono qui");
		}
		
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(json);
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
