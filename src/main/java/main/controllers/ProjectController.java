package main.controllers;

import com.mysql.cj.core.conf.url.ConnectionUrlParser;
import com.mysql.cj.core.conf.url.ConnectionUrlParser.Pair;
import main.exceptions.RPException;
import main.exceptions.RPPermissionsException;
import main.model.db.dao.project.*;
import main.model.dto.*;
import main.utils.DateUtils;
import main.utils.LDAP.LDAPAuthenticator;
import main.utils.RSA.RSAUtil;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.UnsupportedEncodingException;
import java.security.PrivateKey;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ProjectController extends BaseController {
    private ProjectDao projectDao;
    private CustomerController customerController;
    private MilestoneDao milestoneDao;
    private ImportTokenDao importTokenDao;
    private FinalResultDao finalResultDao;
    private BodyPatternDao bodyPatternDao;
    private ResultResolutionDao resultResolutionDao;
    private TestDao testDao;
    private TestResultDao testResultDao;
    private TestResultStatDao testResultStatDao;
    private TestRunDao testRunDao;
    private TestSuiteDao testSuiteDao;
    private UserDao userDao;
    private ProjectUserDao projectUserDao;
    private Test2SuiteDao test2SuiteDao;
    private TestRunLabelDao testRunLabelDao;
    private TestRunStatisticDao testRunStatisticDao;
    private SuiteStatisticDao suiteStatisticDao;
    private PasswordDao passwordDao;
    private UserSessionDao userSessionDao;
    private ImportDao importDao;
    private Suite2DashboardDao suite2DashboardDao;
    private SuiteDashboardDao suiteDashboardDao;

    public ProjectController(UserDto user) {
        super(user);
        projectDao = new ProjectDao();
        customerController = new CustomerController(user);
        milestoneDao = new MilestoneDao();
        importTokenDao = new ImportTokenDao();
        finalResultDao = new FinalResultDao();
        finalResultDao = new FinalResultDao();
        bodyPatternDao = new BodyPatternDao();
        resultResolutionDao = new ResultResolutionDao();
        testDao = new TestDao();
        testResultDao = new TestResultDao();
        testResultStatDao = new TestResultStatDao();
        testRunDao = new TestRunDao();
        testSuiteDao = new TestSuiteDao();
        userDao = new UserDao();
        projectUserDao = new ProjectUserDao();
        test2SuiteDao = new Test2SuiteDao();
        testRunLabelDao = new TestRunLabelDao();
        testRunStatisticDao = new TestRunStatisticDao();
        suiteStatisticDao = new SuiteStatisticDao();
        passwordDao = new PasswordDao();
        userSessionDao = new UserSessionDao();
        importDao = new ImportDao();
        suite2DashboardDao = new Suite2DashboardDao();
        suiteDashboardDao = new SuiteDashboardDao();
    }

    public ProjectDto create(ProjectDto template) throws RPException {
        if(baseUser.isAdmin()){
            ProjectDto project = projectDao.create(template);
            updateProjectPermissions(project);
            return project;
        }else{
            throw new RPPermissionsException("Account is not allowed to create Projects", baseUser);
        }
    }
    public MilestoneDto create(MilestoneDto template) throws RPException {
        if(baseUser.isManager() || baseUser.getProjectUser(template.getProject_id()).isEditor()){
            return milestoneDao.create(template);
        }else{
            throw new RPPermissionsException("Account is not allowed to create Milestones", baseUser);
        }
    }
    public ImportTokenDto create(ImportTokenDto template) throws RPException {
        if(baseUser.isAdmin() || baseUser.isManager() || baseUser.getProjectUser(template.getId()).isManager() || baseUser.getProjectUser(template.getId()).isAdmin()){
            return importTokenDao.create(template);
        }else{
            throw new RPPermissionsException("Account is not allowed to create Import Token", baseUser);
        }
    }
    public FinalResultDto create(FinalResultDto template) throws RPException {
        if(baseUser.isAdmin()){
            return finalResultDao.create(template);
        }else{
            throw new RPPermissionsException("Account is not allowed to create Final Result", baseUser);
        }
    }
    public BodyPatternDto create(BodyPatternDto template) throws RPException {
        if(baseUser.isAdmin() || baseUser.isManager() || baseUser.getProjectUser(template.getProject_id()).isEditor()){
            return bodyPatternDao.create(template);
        }else{
            throw new RPPermissionsException("Account is not allowed to create Body Pattern", baseUser);
        }
    }
    public ResultResolutionDto create(ResultResolutionDto template) throws RPException{
        if(baseUser.isAdmin() || baseUser.isManager() || baseUser.getProjectUser(template.getProject_id()).isAdmin() || baseUser.getProjectUser(template.getProject_id()).isManager()){
            return resultResolutionDao.create(template);
        }else{
            throw new RPPermissionsException("Account is not allowed to create Result Resolution", baseUser);
        }
    }
    public TestDto create(TestDto template, boolean updateSuites) throws RPException{
        if(baseUser.isManager() || baseUser.getProjectUser(template.getProject_id()).isEditor()){
            TestDto test = testDao.create(template);
            if(updateSuites){
                test.setSuites(template.getSuites());
                updateSuites(test);
            }
            return test;
        }else{
            throw new RPPermissionsException("Account is not allowed to create Test", baseUser);
        }
    }
    public TestResultDto create(TestResultDto template) throws RPException{
        if(baseUser.isManager() || baseUser.getProjectUser(template.getProject_id()).isEditor()){
            return testResultDao.create(template);
        }else{
            throw new RPPermissionsException("Account is not allowed to create Test Result", baseUser);
        }
    }
    public TestRunDto create(TestRunDto template) throws RPException{
        if(baseUser.isManager() || baseUser.getProjectUser(template.getProject_id()).isEditor()){
            TestRunDto testRun = testRunDao.create(template);
            if(template.getId() == null){
                createPendingResults(testRun);
            }
            return testRun;
        }else{
            throw new RPPermissionsException("Account is not allowed to create Test Run", baseUser);
        }
    }
    public TestSuiteDto create(TestSuiteDto template) throws RPException{
        if(baseUser.isManager() || baseUser.getProjectUser(template.getProject_id()).isEditor()){
            return testSuiteDao.create(template);
        }else{
            throw new RPPermissionsException("Account is not allowed to create Test Suite", baseUser);
        }
    }
    public UserDto create(UserDto template) throws RPException{
        if(baseUser.isAdmin() || baseUser.getId().equals(template.getId())){
            if(template.getPassword() != null){
                template.setPassword(saltPassword(template, template.getPassword()));
            }
            return userDao.create(template);
        }else{
            throw new RPPermissionsException("Account is not allowed to create User", baseUser);
        }
    }
    public ProjectUserDto create(ProjectUserDto template) throws RPException{
        if(baseUser.isAdmin() || baseUser.getProjectUser(template.getProject_id()).isAdmin()){
            return projectUserDao.create(template);
        }else{
            throw new RPPermissionsException("Account is not allowed to create Project User", baseUser);
        }
    }
    public Test2SuiteDto create(Test2SuiteDto template) throws RPException{
        if(baseUser.isManager() || baseUser.getProjectUserBySuiteId(template.getSuite_id()).isEditor()){
            return test2SuiteDao.create(template);
        }else{
            throw new RPPermissionsException("Account is not allowed to create Project User", baseUser);
        }
    }
    public SuiteDashboardDto create(SuiteDashboardDto template) throws RPException{
        if(baseUser.isManager() || baseUser.getProjectUser(template.getProject_id()).isEditor()){
            template.setId(suiteDashboardDao.create(template).getId());
            updateSuites2Dashboard(template);
            return template;
        }else{
            throw new RPPermissionsException("Account is not allowed to create Suite Dashboards", baseUser);
        }
    }

    public List<ProjectDto> get(ProjectDto template) throws  RPException {
        template.setUser_id(baseUser.getId());
        List<ProjectDto> projects = projectDao.searchAll(template);
        return fillCustomers(projects);
    }
    public List<MilestoneDto> get(MilestoneDto template) throws  RPException {
        if(baseUser.isFromGlobalManagement() || baseUser.getProjectUser(template.getProject_id()).isViewer()){
            return milestoneDao.searchAll(template);
        }else{
            throw new RPPermissionsException("Account is not allowed to view Milestones", baseUser);
        }
    }
    public List<FinalResultDto> get(FinalResultDto template) throws  RPException {
        return finalResultDao.searchAll(template);
    }
    public List<BodyPatternDto> get(BodyPatternDto template) throws  RPException {
        if(baseUser.isAdmin() || baseUser.isManager() || baseUser.getProjectUser(template.getProject_id()).isViewer()){
            return bodyPatternDao.searchAll(template);
        }else{
            throw new RPPermissionsException("Account is not allowed to view Body Patterns", baseUser);
        }
    }
    public List<ResultResolutionDto> get(ResultResolutionDto template) throws  RPException {
        return resultResolutionDao.searchAll(template);
    }
    public List<TestDto> get(TestDto template, boolean withChildren) throws  RPException {
        if(baseUser.isFromGlobalManagement() || baseUser.getProjectUser(template.getProject_id()).isViewer()){
            return fillTests(testDao.searchAll(template), withChildren);
        }else{
            throw new RPPermissionsException("Account is not allowed to view Milestones", baseUser);
        }
    }
    public List<TestResultDto> get(TestResultDto template, Integer limit) throws  RPException {
        if(baseUser.isFromGlobalManagement() || baseUser.getProjectUser(template.getProject_id()).isViewer()){
            template.setLimit(limit);
            return fillResults(testResultDao.searchAll(template));
        }else{
            throw new RPPermissionsException("Account is not allowed to view Test Results", baseUser);
        }
    }
    public List<TestRunDto> get(TestRunDto template, boolean withChildren, Integer limit) throws  RPException {
        if(baseUser.isFromGlobalManagement() || baseUser.getProjectUser(template.getProjectIdById()).isViewer()){
            template.setLimit(limit);
            return fillTestRuns(testRunDao.searchAll(template), withChildren);
        }else{
            throw new RPPermissionsException("Account is not allowed to view Test Run", baseUser);
        }
    }
    public List<TestSuiteDto> get(TestSuiteDto template, boolean withChildren) throws  RPException {
        if(baseUser.isFromGlobalManagement() || baseUser.getProjectUser(template.getProject_id()).isViewer()){
            return fillTestSuites(testSuiteDao.searchAll(template), withChildren);
        }else{
            throw new RPPermissionsException("Account is not allowed to view Test Suites", baseUser);
        }
    }
    public List<UserDto> get(UserDto template) throws  RPException {
        return toPublicUsers(userDao.searchAll(template));
    }

    public List<ProjectUserDto> get(ProjectUserDto template) throws  RPException {
        if(baseUser.isFromGlobalManagement() || baseUser.getProjectUser(template.getProject_id()).isViewer()){
            return fillProjectUsers(projectUserDao.searchAll(template));
        }else{
            throw new RPPermissionsException("Account is not allowed to view Project Users", baseUser);
        }
    }
    public List<TestResultStatDto> get(TestResultStatDto template) throws  RPException {
        if(baseUser.isFromGlobalManagement() || baseUser.getProjectUser(template.getProject_id()).isViewer()){
            return testResultStatDao.searchAll(template);
        }else{
            throw new RPPermissionsException("Account is not allowed to view Test Result Statistic", baseUser);
        }
    }
    public List<TestRunLabelDto> get(TestRunLabelDto template) throws RPException{
        return testRunLabelDao.searchAll(template);
    }
    public List<TestRunStatisticDto> get(TestRunStatisticDto template) throws RPException{
        return testRunStatisticDao.searchAll(template);
    }
    public List<SuiteStatisticDto> get(SuiteStatisticDto template) throws RPException{
        if(baseUser.isFromGlobalManagement() || baseUser.getProjectUser(template.getProjectId()).isViewer()){
            return suiteStatisticDao.searchAll(template);
        }else{
            throw new RPPermissionsException("Account is not allowed to view Suite Statistic", baseUser);
        }
    }
    public List<ImportDto> get(ImportDto template) throws  RPException{
        if(baseUser.isFromGlobalManagement() || baseUser.getProjectUser(template.getProject_id()).isViewer()){
            return importDao.searchAll(template);
        }else{
            throw new RPPermissionsException("Account is not allowed to view Imports", baseUser);
        }
    }
    public List<SuiteDashboardDto> get(SuiteDashboardDto template) throws RPException{
        if(baseUser.isManager() || baseUser.getProjectUser(template.getProject_id()).isEditor()){
            return fillSuiteDashboards(suiteDashboardDao.searchAll(template));
        }else{
            throw new RPPermissionsException("Account is not allowed to view Suite Dashboards", baseUser);
        }
    }

    public boolean delete(ProjectDto template) throws RPException {
        if(baseUser.isAdmin()){
            return projectDao.delete(template);
        }else{
            throw new RPPermissionsException("Account is not allowed to delete Projects", baseUser);
        }
    }
    public boolean delete(MilestoneDto template) throws  RPException {
        if(baseUser.isManager() || baseUser.getProjectUser(template.getProject_id()).isEditor()){
            return milestoneDao.delete(template);
        }else{
            throw new RPPermissionsException("Account is not allowed to delete Milestones", baseUser);
        }
    }
    public boolean delete(FinalResultDto template) throws  RPException {
        if(baseUser.isAdmin()){
            return finalResultDao.delete(template);
        }else{
            throw new RPPermissionsException("Account is not allowed to delete Final Result", baseUser);
        }
    }
    public boolean delete(BodyPatternDto template) throws  RPException {
        if(baseUser.isAdmin() || baseUser.isManager() || baseUser.getProjectUser(template.getProject_id()).isEditor()){
            return bodyPatternDao.delete(template);
        }else{
            throw new RPPermissionsException("Account is not allowed to delete Body Pattern", baseUser);
        }
    }
    public boolean delete(ResultResolutionDto template) throws  RPException {
        if(baseUser.isAdmin()){
            return resultResolutionDao.delete(template);
        }else{
            throw new RPPermissionsException("Account is not allowed to delete Result Resolution", baseUser);
        }
    }
    public boolean delete(TestDto template) throws  RPException {
        if(baseUser.isManager() || baseUser.getProjectUser(template.getProject_id()).isEditor()){
            return testDao.delete(template);
        }else{
            throw new RPPermissionsException("Account is not allowed to delete Test", baseUser);
        }
    }
    public boolean delete(TestResultDto template) throws  RPException {
        if(baseUser.isManager() || baseUser.getProjectUser(template.getProject_id()).isEditor()){
            return testResultDao.delete(template);
        }else{
            throw new RPPermissionsException("Account is not allowed to delete Test Result", baseUser);
        }
    }
    public boolean delete(TestRunDto template) throws  RPException {
        if(baseUser.isManager() || baseUser.getProjectUser(template.getProject_id()).isEditor()){
            return testRunDao.delete(template);
        }else{
            throw new RPPermissionsException("Account is not allowed to delete Test Run", baseUser);
        }
    }
    public boolean delete(TestSuiteDto template) throws  RPException {
        if(baseUser.isManager() || baseUser.getProjectUserBySuiteId(template.getId()).isManager()){
            return testSuiteDao.delete(template);
        }else{
            throw new RPPermissionsException("Account is not allowed to delete TestSuite", baseUser);
        }
    }
    public boolean delete(UserDto template) throws  RPException {
        if(baseUser.isAdmin()){
            return userDao.delete(template);
        }else{
            throw new RPPermissionsException("Account is not allowed to delete User", baseUser);
        }
    }
    public boolean delete(ProjectUserDto template) throws  RPException {
        if(baseUser.isAdmin() || baseUser.getProjectUser(template.getProject_id()).isAdmin()){
            return projectUserDao.delete(template);
        }else{
            throw new RPPermissionsException("Account is not allowed to delete Project User", baseUser);
        }
    }
    public boolean delete(Test2SuiteDto template) throws  RPException {
        if(baseUser.isManager() || baseUser.getProjectUserBySuiteId(template.getSuite_id()).isEditor()){
            return test2SuiteDao.delete(template);
        }else{
            throw new RPPermissionsException("Account is not allowed to delete Test from Suite", baseUser);
        }
    }
    public boolean delete(SuiteDashboardDto template) throws RPException{
        if(baseUser.isManager() || baseUser.getProjectUser(template.getProject_id()).isEditor()){
            return suiteDashboardDao.delete(template);
        }else{
            throw new RPPermissionsException("Account is not allowed to delete Suite Dashboards", baseUser);
        }
    }

    public boolean updateMultipleTestResults(List<TestResultDto> entities) throws RPException {
        if(entities.size() > 0 && (baseUser.isManager() || baseUser.getProjectUser(entities.get(0).getProject_id()).isEditor())){
            return testResultDao.updateMultiply(entities);
        }else{
            throw new RPPermissionsException("Account is not allowed to update Test Result", baseUser);
        }
    }

    public boolean updateMultipleTests(List<TestDto> entities) throws RPException {
        if(entities.size() > 0 && (baseUser.isManager() || baseUser.getProjectUser(entities.get(0).getProject_id()).isEditor())){
            for (TestDto test : entities) {
                updateSuites(test);
            }
            return testDao.updateMultiply(entities);
        }else{
            throw new RPPermissionsException("Account is not allowed to update Test ", baseUser);
        }
    }

    public boolean isTokenValid(String token, Integer projectId) throws RPException {
        String actualHash = DigestUtils.md5Hex(token + "advbc1671-nlksdui-ff");
        ImportTokenDto tokenDTO = new ImportTokenDto();
        tokenDTO.setId(projectId);
        List<ImportTokenDto> importTokens = importTokenDao.searchAll(tokenDTO);

        if(importTokens.size() > 0){
            String expectedHash = (importTokens.get(0)).getImport_token();
            return Objects.equals(actualHash, expectedHash);
        }

        return false;
    }

    public void moveTest(int from, int to, boolean remove, int projectId) throws RPException {
        TestDto oldTest = new TestDto();
        oldTest.setId(from);
        oldTest.setProject_id(projectId);
        TestDto newTest = new TestDto();
        newTest.setId(to);
        newTest.setProject_id(projectId);
        oldTest = get(oldTest, true).get(0);
        newTest = get(newTest, true).get(0);

        for (TestResultDto result : oldTest.getResults()) {
            List<TestResultDto> newResults = newTest.getResults();
            TestResultDto existingResult = newResults.stream().filter(x -> x.getTest_run_id().equals(result.getTest_run_id())).findFirst().orElse(null);
            if(existingResult == null){
                result.setTest(newTest);
                create(result);
            }
        }

        if(remove){
            delete(oldTest);
        }
    }

    public UserDto updatePassword(PasswordDto password) throws RPException {
        UserDto user = new UserDto();
        user.setId(password.getUser_id());
        user = get(user).get(0);

        password.setOld_password(saltPassword(user, password.getOld_password()));
        password.setPassword(saltPassword(user, password.getPassword()));
        passwordDao.create(password);

        user = get(user).get(0);
        user.setSession_code(generateSessionCode(user));
        return userDao.create(user);
    }

    public UserDto auth(String authString, boolean ldap) throws RPException {
        Base64 base64= new Base64();
        String authStringDecoded = StringUtils.newStringUtf8(base64.decode(authString));
        String[] authStringSplit = authStringDecoded.split(":");
        Pair<String, PrivateKey> privateKeyPair
                = RSAUtil.keystore.stream().filter(x -> Objects.equals(x.left, authStringSplit[0])).findFirst().orElse(null);
        PrivateKey key = Objects.requireNonNull(privateKeyPair).right;
        String password;
        try {
            password = RSAUtil.getDecrypted(authStringSplit[1], key);
        } catch (Exception e) {
            throw new RPException("Password Decryption Error");
        } finally {
            RSAUtil.keystore.removeIf(x -> Objects.equals(x.left, authStringSplit[0]));
        }

        UserDto user = ldap ? handleLDAPAuthorization(authStringSplit[0], password) : checkUser(authStringSplit[0], password);

        if(user != null){
            user.setSession_code(generateSessionCode(user));
            updateSession(user);
            return user;
        }

        throw new RPException("Seems your password was updated. Log in again please.");
    }

    public UserDto checkSession(String session) throws RPException {
        Base64 base64 = new Base64();
        DateUtils dates = new DateUtils();
        String[] strings = StringUtils.newStringUtf8(base64.decode(session)).split(":");
        UserDto user = new UserDto();
        user.setUser_name(strings[0]);
        List<UserDto> users = userDao.searchAll(user);
        if(users.size() > 0){
            user = users.get(0);
            if(!user.getSession_code().equals(session)){
                throw new RPException("Credentials you've provided are not valid. Reenter please.");
            }
            if(new Date().after(dates.fromyyyyMMdd(strings[2]))){
                throw new RPException("Session Expired.");
            }
        }
        else{
            throw new RPException("Credentials you've provided are not valid. Reenter please.");
        }

        return user;
    }

    public List<ProjectUserDto> getProjectUserForPermissions(ProjectUserDto template) throws  RPException {
        return projectUserDao.searchAll(template);
    }

    private List<SuiteDashboardDto> fillSuiteDashboards(List<SuiteDashboardDto> dashboards) throws RPException {
        if(dashboards.size() < 1){
            return dashboards;
        }
        List<SuiteDashboardDto> filledSuiteDashboards = new ArrayList<>();
        TestSuiteDto testSuiteDto = new TestSuiteDto();
        testSuiteDto.setProject_id(dashboards.get(0).getProject_id());
        List<TestSuiteDto> projectSuites = testSuiteDao.searchAll(testSuiteDto);
        for (SuiteDashboardDto filledSuiteDashboard : dashboards) {
            Suite2DashboardDto suite2DashboardDto = new Suite2DashboardDto();
            suite2DashboardDto.setDashboard_id(filledSuiteDashboard.getId());
            List<Suite2DashboardDto> suite2Dashboards = suite2DashboardDao.searchAll(suite2DashboardDto);
            filledSuiteDashboard.setSuites(
                    projectSuites.stream().filter(x ->
                            suite2Dashboards.stream().filter(y -> y.getSuite_id().equals(x.getId())).findFirst().orElse(null)
                                    != null).collect(Collectors.toList()));

            filledSuiteDashboards.add(filledSuiteDashboard);
        }
        return filledSuiteDashboards;
    }

    private void updateSuites2Dashboard(SuiteDashboardDto template) throws RPException {
        if(template.getSuites() != null){
            for (TestSuiteDto suite: template.getSuites()) {
                Suite2DashboardDto suite2DashboardDto = new Suite2DashboardDto();
                suite2DashboardDto.setSuite_id(suite.getId());
                suite2DashboardDto.setDashboard_id(template.getId());
                suite2DashboardDao.create(suite2DashboardDto);
            }
        }
    }

    private List<UserDto> toPublicUsers(List<UserDto> users) {
        for (UserDto user :
                users) {
            user.setPassword("");
            user.setSession_code("");
        }
        return users;
    }

    private void updateSession(UserDto user) throws RPException {
        UserSessionDto update = new UserSessionDto();
        update.setUser_id(user.getId());
        update.setSession_code(user.getSession_code());
        userSessionDao.create(update);
    }

    private void updateProjectPermissions(ProjectDto entity) throws RPException {
        if(entity.getCustomer() != null && entity.getCustomer().getAccounting() == 1 && entity.getId() != 0){
            updatePermissions(entity.getCustomer().getId(), entity.getId());
        }
    }

    //TODO create
    private void updatePermissions(Integer customer_id, Integer project_id) throws RPException {
    }

    //TODO Refactoring
    private List<ProjectDto> fillCustomers(List<ProjectDto> projects) throws RPException {
        List<ProjectDto> filledProjects = new ArrayList<>();
        List<CustomerDto> customerDtoList = customerController.get(new CustomerDto(), true);
        for (ProjectDto filledProject : projects) {
            if (filledProject.getCustomer_id() != null) {
                int customerId = filledProject.getCustomer_id();
                filledProject.setCustomer(customerDtoList.stream().filter(x -> x.getId() == customerId).findFirst().orElse(null));
            } else {
                filledProject.setCustomer(null);
            }
            filledProjects.add(filledProject);
        }
        return filledProjects;
    }

    //TODO Refactoring
    private void updateSuites(TestDto test) throws RPException {
        Test2SuiteDto test2SuiteDto = new Test2SuiteDto();
        test2SuiteDto.setTest_id(test.getId());
        List<Test2SuiteDto> oldSuites = test2SuiteDao.searchAll(test2SuiteDto);
        if(test.getSuites() != null && test.getSuites().size() > 0){
            for (TestSuiteDto newSuite : test.getSuites()) {
                Test2SuiteDto alreadyExists = oldSuites.stream().filter(x -> Objects.equals(x.getSuite_id(), newSuite.getId())).findAny().orElse(null);
                if (alreadyExists != null) {
                    oldSuites.removeIf(x -> Objects.equals(x.getSuite_id(), alreadyExists.getSuite_id()));
                } else {
                    Test2SuiteDto newTest2Suite = new Test2SuiteDto();
                    newTest2Suite.setSuite_id(newSuite.getId());
                    newTest2Suite.setTest_id(test.getId());
                    test2SuiteDao.create(newTest2Suite);
                }
            }
        }

        if(oldSuites.size() > 0 ){
            for (Test2SuiteDto oldSuite : oldSuites) {
                test2SuiteDao.delete(oldSuite);
            }
        }
    }

    //TODO Refactoring
    private List<TestDto> fillTests(List<TestDto> tests, boolean withChildren) throws RPException {
        List<TestDto> filledTests = new ArrayList<>();
        if (tests.size() > 0) {
            ProjectUserDto projectUserDto = new ProjectUserDto();
            projectUserDto.setProject_id(tests.get(0).getProject_id());
            List<ProjectUserDto> projectUsers = get(projectUserDto);
            TestSuiteDto testSuiteDto = new TestSuiteDto();
            testSuiteDto.setProject_id(tests.get(0).getProject_id());
            List<TestSuiteDto> testSuites = testSuiteDao.searchAll(testSuiteDto);
            List<Test2SuiteDto> test2Suites = new ArrayList<>();
            for(TestSuiteDto testSuite : testSuites){
                Test2SuiteDto test2Suite = new Test2SuiteDto();
                test2Suite.setSuite_id(testSuite.getId());
                test2Suites.addAll(test2SuiteDao.searchAll(test2Suite));
            }


            for (TestDto test:tests) {
                if (test.getDeveloper_id() != null) {
                    test.setDeveloper(projectUsers.stream().filter(x -> x.getUser().getId().equals(test.getDeveloper_id())).findFirst().orElse(null));
                }

                List<Test2SuiteDto> testSuiteLinks = test2Suites.stream().filter(x -> x.getTest_id().equals(test.getId())).collect(Collectors.toList());
                test.setSuites(convertToSuites(testSuiteLinks, testSuites));

                if (withChildren) {
                    TestResultDto testResultTemplate = new TestResultDto();
                    testResultTemplate.setTest_id(test.getId());
                    testResultTemplate.setProject_id(test.getProject_id());
                    test.setResults(get(testResultTemplate, 10000));
                }
                filledTests.add(test);
            }
        }
        return filledTests;
    }

    //TODO Refactoring
    private List<TestSuiteDto> convertToSuites(List<Test2SuiteDto> test2Suites, List<TestSuiteDto> suites) throws RPException {
        return test2Suites.stream().map(test2suite
                -> suites.stream().filter(x -> x.getId().equals(test2suite.getSuite_id())).findFirst().orElse(null)).collect(Collectors.toList());
    }

    //TODO Refactoring
    private List<TestResultDto> fillResults(List<TestResultDto> results) throws RPException {

        if(results.size() > 0){
            List<FinalResultDto> finalResults = get(new FinalResultDto());
            List<ResultResolutionDto> resolutions = get(new ResultResolutionDto());
            TestDto testTemplate = new TestDto();
            testTemplate.setProject_id(results.get(0).getProject_id());
            List<TestDto> tests = get(testTemplate, false);

            for (TestResultDto result: results){

                result.setFinal_result(finalResults.stream().filter( x -> x.getId().equals(result.getFinal_result_id())).findFirst().orElse(null));
                result.setTest(tests.stream().filter(x -> x.getId().equals(result.getTest_id())).findFirst().orElse(null));
                result.setTest_resolution(resolutions.stream().filter( x -> x.getId().equals(result.getTest_resolution_id())).findFirst().orElse(null));

                if(result.getAssignee() != null){
                    ProjectUserDto projectUserDto = new ProjectUserDto();
                    projectUserDto.setUser_id(result.getAssignee());
                    projectUserDto.setProject_id(result.getProject_id());
                    result.setAssigned_user(get(projectUserDto).get(0));
                }
            }
        }

        return results;
    }

    private List<TestRunDto> fillTestRuns(List<TestRunDto> testRuns, boolean withChildren) throws RPException {

        if(testRuns.size() > 0){
            testRuns = fillMilestonesAndSuites(testRuns);

            if(withChildren){
                testRuns = fillTestRunResults(testRuns);
            }
        }
        return testRuns;
    }

    private List<TestSuiteDto> fillTestSuites(List<TestSuiteDto> testSuites, boolean withChildren) throws RPException {
        if(withChildren){
            for (TestSuiteDto suite: testSuites){
                TestDto testTemplate = new TestDto();
                testTemplate.setTest_suite_id(suite.getId());
                testTemplate.setProject_id(suite.getProject_id());
                List<TestDto> tests = get(testTemplate, false);
                suite.setTests(tests);
            }
        }
        return testSuites;
    }

    private List<TestRunDto> fillTestRunResults(List<TestRunDto> testRuns) throws RPException {
        for (TestRunDto testRun : testRuns) {
            TestResultDto testResultTemplate = new TestResultDto();
            testResultTemplate.setTest_run_id(testRun.getId());
            testResultTemplate.setProject_id(testRun.getProject_id());
            List<TestResultDto> results = get(testResultTemplate, 10000);
            testRun.setTestResults(results);
        }
        return testRuns;
    }

    private List<TestRunDto> fillMilestonesAndSuites(List<TestRunDto> testRuns) throws RPException {
        TestSuiteDto suiteTemplate = new TestSuiteDto();
        suiteTemplate.setProject_id(testRuns.get(0).getProject_id());
        MilestoneDto milestoneTemplate = new MilestoneDto();
        milestoneTemplate.setProject_id(testRuns.get(0).getProject_id());

        List<TestSuiteDto> suites = get(suiteTemplate, false);
        List<TestRunLabelDto> labels = get(new TestRunLabelDto());
        List<MilestoneDto> milestones = get(milestoneTemplate);

        for (TestRunDto testRun : testRuns) {
            testRun.setMilestone(milestones.stream().filter(x -> x.getId().equals(testRun.getMilestone_id())).findFirst().orElse(null));
            testRun.setTest_suite(suites.stream().filter(x -> x.getId().equals(testRun.getTest_suite_id())).findFirst().orElse(null));
            testRun.setLabel(labels.stream().filter(x -> x.getId().equals(testRun.getLabel_id())).findFirst().orElse(null));
        }
        return testRuns;
    }

    private List<ProjectUserDto> fillProjectUsers(List<ProjectUserDto> projectUsers) throws RPException {
        for(ProjectUserDto projectUser: projectUsers){
            projectUser.setUser(new UserDto());
            projectUser.getUser().setId(projectUser.getUser_id());
            projectUser.setUser(get(projectUser.getUser()).get(0));
        }
        return projectUsers;
    }

    private void createPendingResults(TestRunDto testRunTemplate) throws RPException {
        TestDto testTemplate = new TestDto();
        testTemplate.setTest_suite_id(testRunTemplate.getTest_suite_id());
        testTemplate.setProject_id(testRunTemplate.getProject_id());
        List<TestDto> tests = get(testTemplate, false);
        for (TestDto test: tests) {
            TestResultDto pendingTestResult = new TestResultDto();
            pendingTestResult.setProject_id(test.getProject_id());
            pendingTestResult.setStart_date(testRunTemplate.getStart_time());
            pendingTestResult.setFinish_date(testRunTemplate.getStart_time());
            pendingTestResult.setTest_id(test.getId());
            pendingTestResult.setTest_run_id(testRunTemplate.getId());
            pendingTestResult.setFinal_result_id(3);
            pendingTestResult.setTest_resolution_id(1);
            pendingTestResult.setDebug(testRunTemplate.getDebug());
            create(pendingTestResult);
        }
    }

    private UserDto checkUser(String user_name, String password) throws RPException{
        UserDto user = new UserDto();
        user.setUser_name(user_name);
        List<UserDto> users = userDao.searchAll(user);

        if(users.size() > 0){
            user = users.get(0);
            String correctHex = user.getPassword();
            String actualHex = saltPassword(user, password);
            if(correctHex.equals(actualHex)){
                return user;
            }
        }

        throw new RPException("Credentials you've provided are not valid. Reenter please.");
    }

    private UserDto handleLDAPAuthorization(String userName, String password) throws RPException {
        LDAPAuthenticator ldap = new LDAPAuthenticator();
        UserDto user;
        user = ldap.tryAuthWithLdap(userName, password);
        if(user != null){
            UserDto templateUser = new UserDto();
            templateUser.setUser_name(user.getUser_name());
            List<UserDto> users = get(templateUser);

            if(users.size() > 0){
                user.setId(users.get(0).getId());
            }

            String saltPass = saltPassword(user, password);
            user.setPassword(saltPass);
            user.setLdap_user(1);
            user = create(user);
            user.setSession_code(generateSessionCode(user));
            user = create(user);
        }
        return user;
    }

    private String generateSessionCode(UserDto user) {
        Base64 base64= new Base64();
        DateUtils dates = new DateUtils();
        String encode = null;
        try {
            encode = base64.encodeToString((user.getUser_name() + ":" + UUID.randomUUID().toString() + ":" + dates.toyyyyMMdd(dates.addDays(new Date(), 1))).getBytes("utf-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return encode;
    }

    private String saltPassword(UserDto user, String password){
        String passHash = DigestUtils.md5Hex(password);
        return DigestUtils.md5Hex(user.getEmail()+passHash+"kjr1fdd00das");
    }
}
