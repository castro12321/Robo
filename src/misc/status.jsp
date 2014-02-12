<%@ page import="
java.util.*,com.iborg.net.*
" %>

<html>
  <head>
    <title>communication manager status page</title>
  </head>
  
  <blockquote>
	<br><b> Users </b><br>
  <%
	Map map;
	Set keys;
	Iterator iterator;

      map = CommunicationManager.getUsers();
        
	keys = map.keySet();
      iterator = keys.iterator();
      while( iterator.hasNext()) {
      	Object key = iterator.next();
		UserRecord userRecord = (UserRecord)map.get(key);
		Iterator iter = userRecord.getAdvertised().iterator();
		while(iter.hasNext()) {
			Object advertisement = iter.next();
		%>
	      	<%=advertisement%><br>
      	<%
		}
		
    	}
  %>


	<br><b> Current Sockets </b><br>
  <%
      map = CommunicationManager.getCurrentSockets();
      long timeStamp = (new Date()).getTime();  
      keys = map.keySet();
	if(keys.size() > 0) {
	%>
	<table border=1>
	<tr>
	<td align=center>Socket</td>
	<td align=center>IP</td>
	<td align=center>created</td>
	<td align=center>Number of Reads</td>
	<td align=center>Bytes read</td>
	<td align=center>Last Read</td>
	<td align=center>Number of Writes</td>
	<td align=center>Bytes written</td>
	<td align=center>Last Written</td>
	<%
      iterator = keys.iterator();
      while( iterator.hasNext()) {
		%>
            <tr>
        	<%
            String socketName = (String)iterator.next();
		SocketInfo socketInfo = (SocketInfo) CommunicationManager.getSocketInfo().get(socketName);
		%>
            <td align=right><%=socketName%></td>
		<td align=right><%=socketInfo.ip%></td>
		<td align=right><%=timeStamp - socketInfo.created%></td>
		<td align=right><%=socketInfo.reads%></td>
		<td align=right><%=socketInfo.bytesRead%></td>
		<td align=right><%=timeStamp - socketInfo.lastRead%></td>
		<td align=right><%=socketInfo.writes%></td>
		<td align=right><%=socketInfo.bytesWritten%></td>
		<td align=right><%=timeStamp - socketInfo.lastWrite%></td>
            </tr>
        	<%
	}
  %>
	</table>
	<%
	} else {
	%>
	no open sockets <br>
	<%
	}
	%>

  </blockquote>
  </body>
</html>

