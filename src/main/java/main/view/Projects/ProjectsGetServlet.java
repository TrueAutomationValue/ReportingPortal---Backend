package main.view.Projects;

import main.Session;
import main.view.BaseServlet;
import main.view.IPost;
import main.model.dto.ProjectDto;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@WebServlet("/project/get")
public class ProjectsGetServlet extends BaseServlet implements IPost {

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp){
        setPostResponseHeaders(resp);
        setEncoding(resp);

        try {
            Session session = createSession(req);
            String requestedJson = getRequestJson(req);
            ProjectDto projectTemplate = mapper.mapObject(ProjectDto.class, requestedJson);
            List<ProjectDto> projects = session.getProjectController().get(projectTemplate);
            setJSONContentType(resp);
            resp.getWriter().write(mapper.serialize(projects));
        }catch (Exception e) {
            handleException(resp, e);
        }
    }

    @Override
    public void doOptions(HttpServletRequest req, HttpServletResponse resp){
        setPostResponseHeaders(resp);
    }
}
