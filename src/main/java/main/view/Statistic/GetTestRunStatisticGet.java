package main.view.Statistic;

import main.Session;
import main.model.dto.TestRunStatisticDto;
import main.view.BaseServlet;
import main.view.IPost;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;

@WebServlet("/stats/testrun")
public class GetTestRunStatisticGet extends BaseServlet implements IPost {

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) {
        setPostResponseHeaders(resp);
        setEncoding(resp);

        try {
            Session session = createSession(req);
            String requestedJson = getRequestJson(req);
            TestRunStatisticDto testRunStatistic = mapper.mapObject(TestRunStatisticDto.class, requestedJson);
            List<TestRunStatisticDto> testRunStatistics = session.getProjectController().get(testRunStatistic);
            setJSONContentType(resp);
            resp.getWriter().write(mapper.serialize(testRunStatistics));
        }catch (Exception e) {
            handleException(resp, e);
        }
    }

    @Override
    public void doOptions(HttpServletRequest req, HttpServletResponse resp) {
        setPostResponseHeaders(resp);
    }
}
