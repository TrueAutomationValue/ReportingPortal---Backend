package main.view.Milestones;

import main.Session;
import main.model.dto.MilestoneDto;
import main.view.BaseServlet;
import main.view.IDelete;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/milestone")
public class MilestoneServlet extends BaseServlet implements IDelete {
    @Override
    public void doDelete(HttpServletRequest req, HttpServletResponse resp) {
        setDeleteResponseHeaders(resp);

        try {
            Session session = createSession(req);
            MilestoneDto milestoneDto = new MilestoneDto();
            milestoneDto.setId(Integer.parseInt(req.getParameter("id")));
            session.getProjectController().delete(milestoneDto);
        }catch (Exception e) {
            handleException(resp, e);
        }
    }

    @Override
    public void doOptions(HttpServletRequest req, HttpServletResponse resp) {
        setOptionsResponseHeaders(resp);
    }
}
