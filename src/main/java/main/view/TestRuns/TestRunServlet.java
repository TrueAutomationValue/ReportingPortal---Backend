package main.view.TestRuns;

import main.Session;
import main.model.dto.TestRunDto;
import main.view.BaseServlet;
import main.view.IDelete;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/testrun")
public class TestRunServlet  extends BaseServlet implements IDelete {
    @Override
    public void doDelete(HttpServletRequest req, HttpServletResponse resp) {
        setDeleteResponseHeaders(resp);

        try {
            Session session = createSession(req);
            TestRunDto testRun = new TestRunDto();
            testRun.setId(Integer.parseInt(req.getParameter("id")));
            testRun.setProject_id(Integer.parseInt(req.getParameter("projectId")));
            session.getProjectController().delete(testRun);
        }catch (Exception e) {
            handleException(resp, e);
        }
    }

    @Override
    public void doOptions(HttpServletRequest req, HttpServletResponse resp) {
        setOptionsResponseHeaders(resp);
    }
}
