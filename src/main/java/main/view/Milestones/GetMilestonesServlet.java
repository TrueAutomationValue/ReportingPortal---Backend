package main.view.Milestones;

import main.Session;
import main.view.BaseServlet;
import main.view.IPost;
import main.model.dto.MilestoneDto;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@WebServlet("/milestone/get")
public class GetMilestonesServlet extends BaseServlet implements IPost {
    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) {
        setPostResponseHeaders(resp);
        setEncoding(resp);

        try {
            Session session = createSession(req);
            String requestedJson = getRequestJson(req);
            MilestoneDto milestoneTemplate = mapper.mapObject(MilestoneDto.class, requestedJson);
            List<MilestoneDto> milestones = session.getProjectController().get(milestoneTemplate);
            setJSONContentType(resp);
            resp.getWriter().write(mapper.serialize(milestones));
        } catch (Exception e) {
            handleException(resp, e);
        }
    }

    @Override
    public void doOptions(HttpServletRequest req, HttpServletResponse resp) {
        setPostResponseHeaders(resp);
    }
}
