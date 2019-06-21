package main.view.Projects;

import main.Session;
import main.model.dto.ProjectDto;
import main.view.BaseServlet;
import main.view.IPost;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@WebServlet("/project/create")
public class ProjectCreateServlet extends BaseServlet implements IPost {
    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) {
        setPostResponseHeaders(resp);
        resp.addHeader("Access-Control-Expose-Headers", "id");
        resp.addHeader("Access-Control-Expose-Headers", "jsonRequested");
        setEncoding(resp);

        try {
            Session session = createSession(req);
            ProjectDto project = mapper.mapObject(ProjectDto.class, getRequestJson(req));
            resp.setHeader("id", session.getProjectController().create(project).getId().toString());
        }catch (Exception e) {
            handleException(resp, e);
        }
    }

    @Override
    public void doOptions(HttpServletRequest req, HttpServletResponse resp) {
        setPostResponseHeaders(resp);
        resp.addHeader("Access-Control-Allow-Headers", "id");
        resp.addHeader("Access-Control-Allow-Headers", "jsonRequested");
    }
}
