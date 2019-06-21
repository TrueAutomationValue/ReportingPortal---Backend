package main.view.Projects;

import main.Session;
import main.model.dto.DtoMapper;
import main.model.dto.ImportTokenDto;
import main.view.BaseServlet;
import main.view.IPost;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/project/importToken")
public class ImportTokenServlet extends BaseServlet implements IPost {

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) {
        setPostResponseHeaders(resp);
        setEncoding(resp);

        try {
            Session session = createSession(req);
            DtoMapper<ImportTokenDto> mapper = new DtoMapper<ImportTokenDto>(ImportTokenDto.class){};
            ImportTokenDto tokenDto = mapper.mapObject(getRequestJson(req));
            resp.getWriter().write(String.format("{\"token\":\"%s\"}", session.getProjectController().create(tokenDto).getImport_token()));
        } catch (Exception e) {
            handleException(resp, e);
        }
    }

    @Override
    public void doOptions(HttpServletRequest req, HttpServletResponse resp) {
        setPostResponseHeaders(resp);
    }
}
