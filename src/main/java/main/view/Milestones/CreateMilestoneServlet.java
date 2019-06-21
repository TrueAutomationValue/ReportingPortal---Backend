package main.view.Milestones;

import main.Session;
import main.model.dto.MilestoneDto;
import main.view.BaseServlet;
import main.view.IPost;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/milestone/create")
public class CreateMilestoneServlet extends BaseServlet implements IPost {
    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) {
        setPostResponseHeaders(resp);
        resp.addHeader("Access-Control-Expose-Headers", "id");
        setEncoding(resp);

        try {
            Session session = createSession(req);
            String requestedJson = getRequestJson(req);
            MilestoneDto milestone = mapper.mapObject(MilestoneDto.class, requestedJson);
            Integer id = session.getProjectController().create(milestone).getId();
            resp.setHeader("id", id.toString());
        }catch (Exception e) {
            handleException(resp, e);
        }
    }

    @Override
    public void doOptions(HttpServletRequest req, HttpServletResponse resp){
        setPostResponseHeaders(resp);
        resp.addHeader("Access-Control-Allow-Headers", "id");
    }
}
