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

import com.google.gson.Gson;
import com.google.gson.JsonObject;

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
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetRegisteredStudentsByAppeal() {
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
		
		Map<User, Mark> registeredStudentsEvaluations = new HashMap<>();
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
				
				registeredStudentsEvaluations = professorDAO.findRegisteredStudentsByAppeal(idCorso, appealDate);
			}
			else {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().println("Missing parameters");
			}
			
	
		}catch(IllegalArgumentException e){
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("PAR ERROR: parameter is not valid");
		}
		catch (SQLException e) {
			// throw new ServletException(e);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Failure in database extraction");
		}
		//String json = new Gson().toJson(registeredStudentsEvaluations);
		Gson gson = new Gson();
		JsonObject jsonObject = new JsonObject();
		for (Map.Entry<User, Mark> entry : registeredStudentsEvaluations.entrySet()) {
			JsonObject entryObject = new JsonObject();
			entryObject.addProperty("key", gson.toJson(entry.getKey()));
			entryObject.addProperty("value", gson.toJson(entry.getValue()));
			jsonObject.add(gson.toJson(entry.getKey()), entryObject);
		}
		String json = gson.toJson(jsonObject);
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