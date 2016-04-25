package servlets;

/*	
 * Servlet requires several exceptions to function, SQL functions,
 * and servlet/HTTP functions.
 */
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.servlet.annotation.WebServlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 
 * @author thomasbrown - Team 17
 * GetContent should be the main landing page after a successful login for a standard user.
 * Users will see links to view ongoing auctions, search, ask a question, see their acount, and manage/view alerts.
 * After these links, the user will be provided with a list of items and quick info on them.
 */

/*
 * The @WebServlet annotation prevents having to manually set up servlet paths in web.xml.
 * When linking to this servlet, simply specify the name in the annotation, and the server
 * will understand to link to the URL pattern below. Java servlets need not be linked to 
 * using their filename; only the class name is necessary. 
 * e.g. <a href = "Servlet"></a> is a valid link.
 */
@WebServlet(name = "GetContent", urlPatterns = {"/GetContent"})
public class GetContent extends HttpServlet {
	//Useless code according to the internet but Eclipse complains if you remove it.
	private static final long serialVersionUID = 1L;
       
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		//PrintWriter to write to HTML.
		PrintWriter writer  = response.getWriter();
		
		/*
		 * Set up page title and links. The structure of the println statement is only to mimic 
		 * the structure of a formatted HTML document. This structure is only maintained below 
		 * when it is conducive to readability; when this structure inhibits code readability,
		 * I do not use it.
		 */
		writer.println("<html>" 
						+	"<head>" 
						+		"<title>" 
						+			"Bouncehouse Emporium - Main" 
						+		"</title>" 
						+	"</head>" 
						+	"<body>" 
						+		"<center>" 
						+			"<h1>Bouncehouse Emporium</h1>" 
						+			"<h3>Central Hub</h3>" 
/*------------------>*/	+			"<a href = \"PLACEHOLDER\">Create An Auction</a> | " //Add link for auction creation here - Haikinh
						+			"<a href = \"ViewAuctions\">View Auctions></a> | " 
						+ 			"<a href = \"search.jsp\">Search</a> | "
/*------------------->*/+			"<a href = \"PLACEHOLDER\">Ask A Question/Contact Us</a> | " //Add link for Q/A page here - Tim
						+			"<a href = \"ViewAccount\">View Account</a> | "
						+			"<a href = \"ViewAlerts\">Manage Alerts</a> | "
						+			"<a href = \"RecievedAlerts\">View Recieved Alerts</a>"
						+		"</center>" 
						+		"<hr>" 
						+		"<center>"
						+			"<table>"
		);	
		
		//Create objects for connections, statements, and resultsets.
		Connection connection = null;
		Statement getItems = null;
		ResultSet items = null;
		
		//Open connection, create statement, and process request.
		try {
			//Opens the driver or something lol
			Class.forName("com.mysql.jdbc.Driver");
			
			//username and password below are placeholders - replace them 
			//Set up connection to local MySQL server using proper credentials. Create a new query.
			connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/proj_2016", "root", "cantrememberthefuckingpw");
			getItems = connection.createStatement();
			String query = "SELECT * FROM Items ";
			
			//Implement sorting:
			if (request.getParameter("sortBy").equals("bouncinessASC")) { //Bounciness low to high
				query.concat("ORDER BY Bounciness ASC;");
			} else if (request.getParameter("sortBy").equals("bouncinessDESC")) { //Bounciness high to low
				query.concat("ORDER BY Bounciness DESC;");
			} else if (request.getParameter("sortBy").equals("categoryASC")) { //Category A to Z
				query.concat("ORDER BY Category ASC;");
			} else if (request.getParameter("sortBy").equals("categoryDESC")) { //Category Z to A
				query.concat("ORDER BY Category DESC;");
			} else if (request.getParameter("sortBy").equals("colorASC")) { //Color A to Z
				query.concat("ORDER BY Color ASC;");
			} else if (request.getParameter("sortBy").equals("colorDESC")) { // Color Z to A
				query.concat("ORDER BY Color DESC;");
			} else if (request.getParameter("sortBy").equals("sizeASC")) { //Size XS to XL
				query.concat("ORDER BY Size ASC;");
			} else if (request.getParameter("sortBy").equals("sizeDESC")) { //Size XL to XS
				query.concat("ORDER BY Size DESC;");
			} else if (request.getParameter("sortBy").equals("subcategoryASC")) { //Subcategory A to Z
				query.concat("ORDER BY Subcategory ASC;");
			} else if (request.getParameter("sortBy").equals("subcategoryDESC")) { //Subcategory Z to A
				query.concat("ORDER BY Subcategory DESC;");
			} else {
				query.concat("ORDER BY ItemID ASC;"); //Otherwise sort in terms of itemID number low to high.
			}
			
			//Send query to MySQL.
			items = getItems.executeQuery(query);
			
			/*
			 * Process request and output HTML. I may update this to 
			 * be more compact and include a link to a ViewItem
			 * servlet that has more info.
			 */
			while (items.next()) {
				writer.println("<tr>"
							+		"<td>" + items.getString("Category") + "</td>"
							+		"<td>" + items.getString("Subcategory") + "</td>"
							+		"<td><img src = \"" + items.getString("Image") + "\" alt = "
									+ "\"Item #" + items.getInt("ItemID") + " height = \"150\""
									+ " width = \"150\"></td>"
							+ 		"<td>" + items.getString("Description") + "</td>"
							+ 		"<td>" + items.getInt("Bounciness") + "</td>"
							+		"<td>" + items.getString("Color") + "</td>"
							+		"<td>" + items.getString("Size") + "</td>"
							+		"<td><a href = \"CreateAlert?itemID=" + items.getInt("ItemID") + "\">Create Alert For This Item</a>"
							+ 	"</tr>" 
				);
			}
			
			writer.println("</table>"
					+ "<br>"
					+ "<br>"
					+ "<br>");
			
		} catch (SQLException sql) {
			//Catch exception thrown by statement or connection.
			writer.println("The server encountered an error when trying to retrieve the item list.<br>");
			if (sql.getCause() != null) {
				writer.println(sql.getCause() + "<br>");
			}
			if (sql.getMessage() != null) {
				writer.println(sql.getMessage() + "<br>");
			}
			sql.printStackTrace();
			writer.println("<br>");
		} catch(Exception e) {
			//Catch exception thrown by Class.forName
			writer.println("The server encountered an error while attempting to load the page.<br> ");
			if (e.getCause() != null) {
				writer.println(e.getCause() + "<br>");
			}
			if (e.getMessage() != null) {
				writer.println(e.getMessage() + "<br>");
			}
			e.printStackTrace();
			writer.println("<br>");
		} finally {
			//Close resultset, statement, connection.
			try {
				if (items != null) {
					items.close();
				}
				if (getItems != null) {
					getItems.close();
				}
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException sql) {
				/*
				 * Do nothing. The page has already loaded - no need to let the user know
				 * there was an error that doesn't affect them.
				 */
			} catch (Exception e) {
				/*
				 * Do nothing. The page has already loaded - no need to let the user know
				 * there was an error that doesn't affect them.
				 */
			}
			
			//Write closing html for page.
			writer.println(
									"</center>"
						+		"</body"
						+	"</html>"
			);
		}
	}

	//congrats on making it this far into the file
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
}