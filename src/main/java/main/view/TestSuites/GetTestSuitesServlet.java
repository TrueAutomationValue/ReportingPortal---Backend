package main.view.TestSuites;

import main.Session;
import main.model.dto.TestSuiteDto;
import main.view.BaseServlet;
import main.view.IPost;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;

@WebServlet("/suite/get")
public class GetTestSuitesServlet extends BaseServlet implements IPost {

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) {
        setPostResponseHeaders(resp);
        setEncoding(resp);

        try {
            Session session = createSession(req);
            String requestedJson = getRequestJson(req);
            boolean withChildren = false;
            if(req.getParameterMap().containsKey("withChildren")){
                withChildren = req.getParameter("withChildren").equals("1");
            }
            TestSuiteDto testSuite = mapper.mapObject(TestSuiteDto.class, requestedJson);
            List<TestSuiteDto> testSuites = session.getProjectController().get(testSuite, withChildren);
            setJSONContentType(resp);
            resp.getWriter().write(mapper.serialize(testSuites));
        }catch (Exception e) {
            handleException(resp, e);
        }
    }

    @Override
    public void doOptions(HttpServletRequest req, HttpServletResponse resp) {
        setOptionsResponseHeaders(resp);
        resp.addHeader("Access-Control-Allow-Headers", "id");
    }
}
