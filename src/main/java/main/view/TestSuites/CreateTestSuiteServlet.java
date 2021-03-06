package main.view.TestSuites;

import main.Session;
import main.model.dto.TestSuiteDto;
import main.view.BaseServlet;
import main.view.IPost;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;


@WebServlet("/suite/create")
public class CreateTestSuiteServlet extends BaseServlet implements IPost {
    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) {
        setPostResponseHeaders(resp);
        resp.addHeader("Access-Control-Expose-Headers", "id");
        setEncoding(resp);

        try {
            Session session = createSession(req);
            String requestedJson = getRequestJson(req);
            TestSuiteDto testSuite = mapper.mapObject(TestSuiteDto.class, requestedJson);
            testSuite = session.getProjectController().create(testSuite);
            resp.setHeader("id", testSuite.getId().toString());
        } catch (Exception e) {
            handleException(resp, e);
        }
    }

    @Override
    public void doOptions(HttpServletRequest req, HttpServletResponse resp) {
        setPostResponseHeaders(resp);
        resp.addHeader("Access-Control-Allow-Headers", "id");
    }
}
