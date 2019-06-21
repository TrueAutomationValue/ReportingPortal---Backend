package main.view.TestRuns;

import main.Session;
import main.model.dto.TestRunDto;
import main.view.BaseServlet;
import main.view.IPost;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;

@WebServlet("/testrun/create")
public class CreateTestRunServlet extends BaseServlet implements IPost {
    
    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) {
        setPostResponseHeaders(resp);
        setEncoding(resp);
        resp.addHeader("Access-Control-Expose-Headers", "id");

        try {
            Session session = createSession(req);
            String requestedJson = getRequestJson(req);
            TestRunDto testRun = mapper.mapObject(TestRunDto.class, requestedJson);
            testRun = session.getProjectController().create(testRun);
            resp.setHeader("id", testRun.getId().toString());
        }catch (Exception e) {
            handleException(resp, e);
        }
    }

    @Override
    public void doOptions(HttpServletRequest req, HttpServletResponse resp) {
        setPostResponseHeaders(resp);
        resp.addHeader("Access-Control-Allow-Headers", "id");
    }
}
