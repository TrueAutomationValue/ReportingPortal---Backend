package main.view.TestSuites;


import main.Session;
import main.model.dto.TestSuiteDto;
import main.view.BaseServlet;
import main.view.IDelete;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/suite")
public class TestSuiteServlet extends BaseServlet implements IDelete {
    @Override
    public void doDelete(HttpServletRequest req, HttpServletResponse resp) {
        setDeleteResponseHeaders(resp);

        try {
            Session session = createSession(req);
            TestSuiteDto testSuite = new TestSuiteDto();
            testSuite.setId(Integer.parseInt(req.getParameter("id")));
            testSuite.setProject_id(Integer.parseInt(req.getParameter("projectId")));
            session.getProjectController().delete(testSuite);
        }catch (Exception e) {
            handleException(resp, e);
        }
    }

    @Override
    public void doOptions(HttpServletRequest req, HttpServletResponse resp) {
        setOptionsResponseHeaders(resp);
    }
}
