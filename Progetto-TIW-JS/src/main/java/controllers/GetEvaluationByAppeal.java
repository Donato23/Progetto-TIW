package controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;

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
import com.google.gson.JsonObject;

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
@MultipartConfig
public class GetEvaluationByAppeal extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public GetEvaluationByAppeal() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public void init() throws ServletException {
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
		
		try {
			int mStu = u.getMatricola();
			String corso = request.getParameter("idCorso");
			String appello = request.getParameter("dataAppello");
			if(appello != null && corso != null) {
				selAppeal.setIdCorso(Integer.parseInt(corso));
				selAppeal.setData(appello);
			}
			if (!studentDAO.registeredForAppeal(mStu, selAppeal)) {
				response.sendRedirect(getServletContext().getContextPath() + "/GoToHomeStudentAppeals?idCorso="+corso);
				return;
			}	
			mark = studentDAO.findEvaluationByAppeal(mStu,selAppeal);
			selCourse = courseDAO.findCoursesByID(Integer.parseInt(corso));
			
		} catch (IllegalArgumentException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("PAR ERROR: parameter is not valid");
			return;
		} catch (SQLException e) {
			// throw new ServletException(e);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Failure in database extraction");
			return;
		}
		// String json = new Gson().toJson(registeredStudentsEvaluations);
		Gson gson = new Gson();
		JsonObject evaluationData = new JsonObject();
		evaluationData.addProperty("userId", gson.toJson(u.getMatricola()));
		evaluationData.addProperty("nomeStudente", gson.toJson(u.getNome()));
		evaluationData.addProperty("cognomeStudente", gson.toJson(u.getCognome()));
		evaluationData.addProperty("idCorso", gson.toJson(selCourse.getId()));
		evaluationData.addProperty("nomeCorso", gson.toJson(selCourse.getNome()));
		evaluationData.addProperty("dataAppello", gson.toJson(selAppeal.getData()));
		String parsedMark = null;
		String parsedStatoValutazione = null;
		if(mark != null) {
			parsedMark = gson.toJson(mark.getMark());
			parsedStatoValutazione = gson.toJson(mark.getStatoValutazione());
		}
		evaluationData.addProperty("mark", parsedMark);
		evaluationData.addProperty("statoValutazione", parsedStatoValutazione);

		String json = gson.toJson(evaluationData);
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(json);
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