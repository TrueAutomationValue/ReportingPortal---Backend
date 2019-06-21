package main.view.Tests;

import main.Session;
import main.view.BaseServlet;
import main.view.IGet;

import javax.naming.directory.InvalidAttributesException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/test/move")
public class MoveTestServlet extends BaseServlet implements IGet {

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) {
        setGetResponseHeaders(resp);
        try {
            validateGet(req);
            Session session = createSession(req);
            session.getProjectController().moveTest(
                    Integer.parseInt(req.getParameter("from")),
                    Integer.parseInt(req.getParameter("to")),
                    Boolean.parseBoolean(req.getParameter("remove")),
                    Integer.parseInt(req.getParameter("projectId")));
        }catch (Exception e) {
            handleException(resp, e);
        }
    }

    @Override
    public void doOptions(HttpServletRequest req, HttpServletResponse resp){
        setOptionsResponseHeaders(resp);
    }

    private void validateGet(HttpServletRequest req) throws InvalidAttributesException {
        if(!req.getParameterMap().containsKey("from") || !req.getParameterMap().containsKey("to") || !req.getParameterMap().containsKey("remove") || !req.getParameterMap().containsKey("projectId")){
            throw new InvalidAttributesException("You have missed one of the required parameter: from, to, remove");
        }
    }
}
