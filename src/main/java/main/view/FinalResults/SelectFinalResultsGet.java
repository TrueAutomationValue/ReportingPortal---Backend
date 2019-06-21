package main.view.FinalResults;

import main.Session;
import main.view.BaseServlet;
import main.view.IPost;
import main.model.dto.FinalResultDto;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@WebServlet("/final_result/get")
public class SelectFinalResultsGet extends BaseServlet implements IPost {

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) {
        setPostResponseHeaders(resp);
        setEncoding(resp);

        try {
            Session session = createSession(req);
            String requestedJson = getRequestJson(req);
            FinalResultDto finalResultDto = mapper.mapObject(FinalResultDto.class, requestedJson);
            List<FinalResultDto> finalResults = session.getProjectController().get(finalResultDto);
            setJSONContentType(resp);
            resp.getWriter().write(mapper.serialize(finalResults));
        }catch (Exception e) {
            handleException(resp, e);
        }
    }

    @Override
    public void doOptions(HttpServletRequest req, HttpServletResponse resp){
        setPostResponseHeaders(resp);
    }
}