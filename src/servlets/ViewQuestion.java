package servlets;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.NoSuchElementException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import model.SQLConnector;

/**
 * Servlet implementation class ViewQuestion
 */
@WebServlet("/question")
public class ViewQuestion extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public ViewQuestion() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (request.getSession().getAttribute("userID") == null) {
			response.sendError(403, "You are not authorized to access this page.");
			return;
		}
		try{
			int questionID = Integer.parseInt(request.getQueryString());
			if(questionID < 0) throw new NumberFormatException();

			Connection con = SQLConnector.getConnection();
			Statement s = con.createStatement();
			ResultSet rs = s.executeQuery("SELECT Q.*, U.FirstName, U1.Username FROM Question Q, User U, User U1 WHERE Q.QuestionID = "
					+ questionID+" AND U.UserID=Q.RepID AND U1.UserID=Q.UserID");
			if(!rs.next()) {throw new NoSuchElementException();}

			request.setAttribute("topic",rs.getString("Q.Topic"));
			request.setAttribute("qtext",rs.getString("Q.QText"));
			request.setAttribute("username",rs.getString("U1.Username"));
			request.setAttribute("repname",rs.getString("U.FirstName"));
			request.setAttribute("answer",rs.getString("Answer"));
			request.setAttribute("role", request.getSession().getAttribute("role"));
			request.setAttribute("qid", questionID);
			
			con.close();
			s.close();
			rs.close();
			request.getRequestDispatcher("WEB-INF/question.jsp?" + questionID).forward(request, response);
		}
		catch(IllegalAccessException | InstantiationException | ClassNotFoundException | SQLException e){
			System.out.println("Invalid Query Param (Negative or non Integer)");
			return;
		}
		catch(NumberFormatException| NoSuchElementException e ){
			System.out.println("Invalid Query Param (Negative or non Integer)");
			return;
		}
	}
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		if (request.getSession().getAttribute("userID") == null) {
			response.sendError(403, "You are not authorized to access this page.");
			return;
		}
		
		String answer = (String)request.getParameter("questionAnswer");
		int questionID = Integer.parseInt(request.getQueryString());
		try {
			Connection con = SQLConnector.getConnection();
			String sql = "UPDATE Question SET Answer = (?) WHERE QuestionID = "+questionID;
			PreparedStatement s = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			s.setString(1,answer);
			s.executeUpdate();
			
			con.close();
			s.close();
			response.sendRedirect("question?" + questionID);
		} catch (IllegalAccessException | InstantiationException | ClassNotFoundException | SQLException e) {
			response.sendRedirect("error.html");
			return;
		}

	}
}
