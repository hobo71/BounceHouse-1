<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<%if (request.getSession().getAttribute("userID") == null) {
	response.sendError(403, "You are not authorized to access this page.");
}
	if (!request.getSession().getAttribute("role").equals("admin")) {
		response.sendRedirect("GetContent");
		
	}%>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Generate Sales Reports</title>
</head>
<body>
<h1>
	<center>Sales Reports</center>
</h1>
<center>
	<a href = "ViewTotalEarnings">
			<br>
			<br>
			View Total Earnings
		</a>
		<a href = "viewEarningsByType.jsp">
			<br>
			<br>
			View Earnings By Type
		</a> 
		<a href = "ViewBestSellingItems">
			<br>
			<br>
			View Best Selling Items
		</a> 
		<a href = "ViewBestBuyers">
			<br>
			<br>
			View Best Buyers
		</a> 
		<a href = "adminFunctions.jsp">
			<br>
			<br>
			Return to admin functions page		
			</a> 
</center>
</body>
</html>