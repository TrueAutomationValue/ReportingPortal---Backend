package main.view.TestRuns;

import main.Session;
import main.model.dto.TestRunDto;
import main.view.BaseServlet;
import main.view.IPost;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;

@WebServlet("/testrun/get")
public class GetTestRunServlet extends BaseServlet implements IPost {

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) {
        setPostResponseHeaders(resp);
        setEncoding(resp);

        try {
            Session session = createSession(req);
            String requestedJson = getRequestJson(req);
            TestRunDto TestRunTemplate = mapper.mapObject(TestRunDto.class, requestedJson);
            boolean withChildren = false;
            Integer limit = 10000;
            if(req.getParameterMap().containsKey("withChildren")){
                withChildren = req.getParameter("withChildren").equals("1");
            }
            if(req.getParameterMap().containsKey("limit")){
                limit = Integer.parseInt(req.getParameter("limit"));
            }
            List<TestRunDto> testRuns = session.getProjectController().get(TestRunTemplate, withChildren, limit);
            setJSONContentType(resp);
            resp.getWriter().write(mapper.serialize(testRuns));
        } catch (Exception e) {
            handleException(resp, e);
        }
    }

    @Override
    public void doOptions(HttpServletRequest req, HttpServletResponse resp) {
        setPostResponseHeaders(resp);
    }
}
