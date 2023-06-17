package controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
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
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import dao.AppealDAO;
import dao.ProfessorDAO;
import javaBeans.Appeal;
import javaBeans.Mark;
import javaBeans.User;

/**
 * Servlet implementation class GetRegisteredStudentsWithoutEvaluationByAppeal
 */
@WebServlet("/GetRegisteredStudentsWithoutEvaluationByAppeal")
@MultipartConfig
public class GetRegisteredStudentsWithoutEvaluationByAppeal extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetRegisteredStudentsWithoutEvaluationByAppeal() {
        super();
        // TODO Auto-generated constructor stub
    }
    
    public void init() throws ServletException{
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
		
		Map<User, Mark> registeredStudentsWithoutEvaluations = new HashMap<>();
		List<Appeal> appelliDocente = null;
		Appeal appeal = new Appeal();
		int idCorso;
		Date appealDate = null;
		try {
			String corso = request.getParameter("idCorso");
			String appello = request.getParameter("dataAppello");
			
			if(appello != null && corso != null) {
				idCorso = Integer.parseInt(corso);
				appealDate = Date.valueOf(appello);
				
				appeal.setData(appello);
				appeal.setIdCorso(idCorso);
				
				//controllo che l'appello sia tenuto dal professore e che sia un verbale associato a quell'appello
				appelliDocente = appealDAO.findAppealByCourseAndProfessor(u.getMatricola(), idCorso);
				
				if(!appelliDocente.contains(appeal)){
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					response.getWriter().println("Wrong parameter");
				}
				
				registeredStudentsWithoutEvaluations = professorDAO.findRegisteredStudentsWithoutEvaluationByAppeal(idCorso, appealDate);
			}
			else {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().println("Missing parameters");
				return;
			}
			
	
		}catch(IllegalArgumentException e){
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("PAR ERROR: parameter is not valid");
			return;
		}
		catch (SQLException e) {
			// throw new ServletException(e);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Failure in database extraction");
			return;
		}
		
		JsonArray registeredStudentsData = new JsonArray();
		
		for(User key : registeredStudentsWithoutEvaluations.keySet()){
			JsonObject studentData = new JsonObject();
			studentData.addProperty("studentId", key.getMatricola());
			studentData.addProperty("studentName", key.getNome());
			studentData.addProperty("studentSurname", key.getCognome());
			studentData.addProperty("studentEmail", key.getMail());
			studentData.addProperty("studentDegree", key.getCorsoDiLaurea());
			studentData.addProperty("studentMark", registeredStudentsWithoutEvaluations.get(key).getMark());
			studentData.addProperty("studentEvaluationState", registeredStudentsWithoutEvaluations.get(key).getStatoValutazione().toString());
			registeredStudentsData.add(studentData);
		}
		
		String json = new Gson().toJson(registeredStudentsData);
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(json);
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