package main;

import main.controllers.AuditController;
import main.controllers.CustomerController;
import main.controllers.ProjectController;
import main.controllers.SettingsController;
import main.exceptions.RPException;
import main.model.db.dao.project.UserDao;
import main.model.db.imports.Importer;
import main.model.db.imports.enums.TestNameNodeType;
import main.model.dto.ProjectUserDto;
import main.model.dto.TestRunDto;
import main.model.dto.UserDto;
import main.model.email.TestRunEmails;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.util.List;
import java.util.stream.Collectors;

public class Session {

    private UserDto user;
    private String session;

    public Session(String sessionId) throws RPException {
        if(isSessionValid(sessionId)){
            setUserMembership();
        }
    }

    private void setUserMembership() throws RPException {
        ProjectUserDto projectUserDto = new ProjectUserDto();
        projectUserDto.setUser_id(user.getId());
        user.setProjectUsers(getProjectController().getProjectUserForPermissions(projectUserDto));
    }

    public List<ProjectUserDto> getProjectPermissions(){
        return user.getProjectUsers();
    }

    public List<ProjectUserDto> getProjectPermissions(Integer projectId){
        return user.getProjectUsers().stream().filter(x -> x.getProject_id().equals(projectId)).collect(Collectors.toList());
    }

    public Session(){
        user = new UserDto();
        user.setAdmin(1);
        user.setUnit_coordinator(1);
        user.setManager(1);
    }

    public boolean isSessionValid() throws RPException {
        return  isSessionValid(session);
    }

    private boolean isSessionValid(String sessionId) throws RPException {
        if(sessionId != null){
            UserDao userDao = new UserDao();
            user = userDao.IsAuthorized(sessionId);
            session = sessionId;
            return user != null;
        }
        user = null;
        return false;
    }

    public Importer getImporter(List<String> filePaths, TestRunDto testRunTemplate, String pattern, String format, TestNameNodeType nodeType, boolean singleTestRun) throws RPException {
        try {
            return new Importer(filePaths, testRunTemplate, pattern, format, nodeType, singleTestRun, user);
        } catch (ParserConfigurationException | SAXException e) {
            throw new RPException("Some Internal SAX error: " + e.getMessage());
        }
    }

    public TestRunEmails getTestRunEmails(){
        return new TestRunEmails();
    }

    public AuditController getAuditController() {
        return new AuditController(user);
    }

    public ProjectController getProjectController () throws RPException {
        return new ProjectController(user);
    }

    public CustomerController getCustomerController () {
        return new CustomerController(user);
    }

    public SettingsController getSettingsController () {
        return new SettingsController(user);
    }

    public UserDto getCurrentUser() {
        return user;
    }

    public void setCurrentUser(UserDto user) throws RPException {
        this.user = user;
        setUserMembership();
    }
}
