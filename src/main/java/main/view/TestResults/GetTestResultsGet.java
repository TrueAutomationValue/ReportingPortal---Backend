package main.view.TestResults;

import main.Session;
import main.model.dto.TestResultDto;
import main.view.BaseServlet;
import main.view.IPost;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;

@WebServlet("/testresult/get")
public class GetTestResultsGet extends BaseServlet implements IPost {

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp){
        setPostResponseHeaders(resp);
        setEncoding(resp);

        try {
            Session session = createSession(req);
            String requestedJson = getRequestJson(req);
            TestResultDto testResultTemplate = mapper.mapObject(TestResultDto.class, requestedJson);
            List<TestResultDto> testResults = session.getProjectController().get(testResultTemplate, 100000);
            setJSONContentType(resp);
            resp.getWriter().write(mapper.serialize(testResults));
        }catch (Exception e) {
            handleException(resp, e);
        }
    }

    @Override
    public void doOptions(HttpServletRequest req, HttpServletResponse resp){
        setOptionsResponseHeaders(resp);
    }
}
