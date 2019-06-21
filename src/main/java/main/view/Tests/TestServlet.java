package main.view.Tests;

import main.Session;
import main.model.dto.TestDto;
import main.view.BaseServlet;
import main.view.IDelete;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@WebServlet("/test")
public class TestServlet extends BaseServlet implements IDelete {


    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) {
        setPostResponseHeaders(resp);
        setEncoding(resp);

        try {
            Session session = createSession(req);
            TestDto test = new TestDto();
            test.getSearchTemplateFromRequestParameters(req);

            boolean withChildren = false;
            if(req.getParameterMap().containsKey("withChildren")){
                withChildren = req.getParameter("withChildren").equals("1");
            }

            List<TestDto> tests = session.getProjectController().get(test, withChildren);
            setJSONContentType(resp);
            resp.getWriter().write(mapper.serialize(tests));
        }catch (Exception e) {
            handleException(resp, e);
        }
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) {
        setPostResponseHeaders(resp);
        resp.addHeader("Access-Control-Expose-Headers", "id");
        setEncoding(resp);

        try {
            Session session = createSession(req);
            String requestedJson = getRequestJson(req);
            TestDto test = mapper.mapObject(TestDto.class, requestedJson);
            test = session.getProjectController().create(test, true);
            resp.setHeader("id", test.getId().toString());
        }catch (Exception e) {
            handleException(resp, e);
        }
    }

    @Override
    public void doPut(HttpServletRequest req, HttpServletResponse resp) {
        setPostResponseHeaders(resp);
        resp.addHeader("Access-Control-Expose-Headers", "id");
        setEncoding(resp);

        try {
            Session session = createSession(req);
            String requestedJson = getRequestJson(req);
            List<TestDto> tests = mapper.mapObjects(TestDto.class, requestedJson);
            session.getProjectController().updateMultipleTests(tests);
        }catch (Exception e) {
            handleException(resp, e);
        }
    }

    @Override
    public void doDelete(HttpServletRequest req, HttpServletResponse resp) {
        setDeleteResponseHeaders(resp);

        try {
            Session session = createSession(req);
            TestDto test = new TestDto();
            test.setId(Integer.parseInt(req.getParameter("id")));
            test.setProject_id(Integer.parseInt(req.getParameter("projectId")));
            session.getProjectController().delete(test);
        }catch (Exception e) {
            handleException(resp, e);
        }
    }

    @Override
    public void doOptions(HttpServletRequest req, HttpServletResponse resp) {
        setOptionsResponseHeaders(resp);
    }
}
