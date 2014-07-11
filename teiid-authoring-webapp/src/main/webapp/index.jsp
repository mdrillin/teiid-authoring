<%
  final String queryString = request.getQueryString();
  final String redirectURL = "org.teiid.TeiidAuthoringWebApp/TeiidAuthoring.html" + (queryString == null ? "" : "?" + queryString);
  response.sendRedirect(redirectURL);
%>
