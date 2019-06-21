package main.view.Projects;

import main.Session;
import main.model.dto.ProjectDto;
import main.view.BaseServlet;
import main.view.IDelete;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/project")
public class ProjectRemoveServlet extends BaseServlet implements IDelete{
    @Override
    public void doDelete(HttpServletRequest req, HttpServletResponse resp) {
        setDeleteResponseHeaders(resp);

        try {
            Session session = createSession(req);
            ProjectDto projectDto = new ProjectDto();
            projectDto.setId(Integer.parseInt(req.getParameter("projectId")));
            session.getProjectController().delete(projectDto);
        }catch (Exception e) {
            handleException(resp, e);
        }
    }

    @Override
    public void doOptions(HttpServletRequest req, HttpServletResponse resp) {
        setOptionsResponseHeaders(resp);
    }
}
