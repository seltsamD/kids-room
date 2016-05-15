
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec" %>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>

<html lang="en">
<head>
	<meta charset="UTF-8">
	<title>Add location</title>

	<link rel="stylesheet" type="text/css" href="resources/css/admin-new-style.css">
</head>

<body>
    <div class="for-table">
        <form  class="for-table" action="adm-add-room" method="post" modelAttribute="room">

                <legend class="for-field"><strong>Add room</strong></legend>

                <div class="form-group">
                    <label class="for-field"> Room name <input type="text" name="name" required class="form-control"/></label>
                </div>
                <div class="form-group">
                    <label class="for-field"> Room capacity <input type="number" name="capacity" required class="form-control"/></label>
                </div>

                <div class="form-group">
                    <label class="for-field"> Room address <input type="text" name="address" required class="form-control"/></label>
                </div>

                <div class="form-group">
                    <label class="for-field"> City
                        <select name="cities" required class="form-control">
                            <c:forEach var="city" items="${cityList}" >
                                <option value="${city.idCity}">${city.nameCity}</option>
                            </c:forEach>
                        </select>
                    </label>
                </div>

                <div class="form-group">
                    <label class="for-field"> Room phone number <input type="number" name="phoneNumber" pattern="^[\d]{10,13}$" required class="form-control"/></label>
                </div>

                <div class="form-group">

                    <label class="for-field"> Room-manager
                        <select name="managers" required class="form-control">
                            <c:forEach var="manager" items="${managerList}" >
                                <option value="${manager.id}">${manager.firstName}</option>
                            </c:forEach>
                        </select>
                    </label>
                </div>

                <div class="form-group">
                    <button class="for-button" type="submit" name="submit">Submit</button>
                    <button class="for-button" type="reset" name="reset" >Cancel</button>
                </div>

        </form>
    </div>
</body>
</html>

