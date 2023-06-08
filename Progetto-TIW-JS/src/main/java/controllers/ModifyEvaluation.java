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

import dao.AppealDAO;
import dao.ProfessorDAO;
import dao.StudentDAO;
import javaBeans.Appeal;
import javaBeans.User;


/**
 * Servlet implementation class PublishEvaluation
 */
@WebServlet("/ModifyEvaluation")
@MultipartConfig
public class ModifyEvaluation extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;

	public ModifyEvaluation() {
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
		
		if(sId == null || appealDate == null || courseId == null) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Missing parameters");
			return;
		}
		
		int studentId;
		//Date dataAppello = null;
		int idCorso;
		List<Appeal> appelliDocente = null;
		AppealDAO appealDAO = new AppealDAO(connection);
		Appeal appeal = new Appeal();
		try {
			studentId = Integer.parseInt(sId);
			//dataAppello = Date.valueOf(appealDate);
			idCorso = Integer.parseInt(courseId);
			appeal.setData(appealDate);
			appeal.setIdCorso(idCorso);
		}catch (IllegalArgumentException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Bad parameter - Parameter is not valid");
			return;
		}
		
		StudentDAO studentDao = new StudentDAO(connection); 
		User student = null;
		try {
			appelliDocente = appealDAO.findAppealByCourseAndProfessor(u.getMatricola(), idCorso);
			
			if(!appelliDocente.contains(appeal)){
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().println("No appeals on this date for this course");
				return;
			}
			
			student = studentDao.findStudentById(studentId);
			if(student.getRuolo().equals("docente")) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().println("Parameter must be a student ID");
				return;
			}
			if(!studentDao.registeredForAppeal(studentId, appeal)) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().println("StudentId must be of a registered student");
				return;
			}
		}catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
			response.getWriter().println("Failure of student search in database");
			return;
		}
		
		String json = new Gson().toJson(student);
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(json);
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
		try {
			String mStu = request.getParameter("studentId");
			String corso = request.getParameter("courseId");
			String appello = request.getParameter("appealDate");
			String evaluation = request.getParameter("evaluation");
			
			if(appello != null && corso != null && mStu != null && evaluation != null) {
				selAppeal.setIdCorso(Integer.parseInt(corso));
				//selAppeal.setData(Date.valueOf(appello));
				selAppeal.setData(appello);
				studentId = Integer.parseInt(mStu);
			}else {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameters in evaluation publication");
				return;
			}
			
			if(!(evaluation.equals("") || evaluation.equals("ASSENTE") || 
					evaluation.equals("RIMANDATO") || evaluation.equals("RIPROVATO") || evaluation.equals("30L"))) {
				mark = Integer.parseInt(evaluation);
				if(mark < 18 || mark > 30) {
					response.sendError(HttpServletResponse.SC_BAD_REQUEST, "PAR ERROR: Parameter is not valid");
					return;
				}
			}
			
			appelliDocente = appealDAO.findAppealByCourseAndProfessor(u.getMatricola(), selAppeal.getIdCorso());
			
			if(!appelliDocente.contains(selAppeal)){
				response.sendRedirect(getServletContext().getContextPath() + "/GetRegisteredStudentsByAppeal?dataAppello=" + selAppeal.getData() + "&idCorso=" + selAppeal.getIdCorso() + "&sortBy=matricola&order=ASC");
				return;
			}
			
			student = studentDao.findStudentById(studentId);
			if(student.getRuolo().equals("docente")) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parameter must be a student ID");
				return;
			}
			if(!studentDao.registeredForAppeal(studentId, selAppeal)) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "StudentId must be of a registered student");
				return;
			}
			
			professorDao.insertEvaluation(studentId, evaluation, selAppeal);
		} catch (IllegalArgumentException e) {
			response.getWriter().append("PAR ERROR: Parameter is not valid");
		} catch (SQLException e) {
			// throw new ServletException(e);
			response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Failure in project database extraction");
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
