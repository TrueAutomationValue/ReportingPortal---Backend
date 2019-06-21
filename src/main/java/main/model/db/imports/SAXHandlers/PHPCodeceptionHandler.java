package main.model.db.imports.SAXHandlers;

import main.exceptions.RPException;
import main.model.db.imports.Handler;
import main.model.dto.*;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static main.model.db.imports.enums.ResultStatus.*;

public class PHPCodeceptionHandler extends Handler {

    private TestRunDto testRun = new TestRunDto();
    private TestSuiteDto testSuite = new TestSuiteDto();
    private List<TestResultDto> results = new ArrayList<TestResultDto>();
    private TestResultDto result = new TestResultDto();
    private List<TestDto> tests = new ArrayList<TestDto>();
    private TestDto test = new TestDto();
    private String currentElement = "";
    private Date currentTimeSlot;
    private Calendar calendar = Calendar.getInstance();
    private boolean testcaseIsFound = false;

    public PHPCodeceptionHandler() throws RPException {
        super();
        result.setFail_reason("$blank");
        testRun.setFinish_time(new Date());
    }

    @Override
    public void startDocument() throws SAXException {
    }

    @Override
    public void endDocument() throws SAXException {
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        currentElement = qName;
        if (qName.equals("testsuite")) {
            calendar.setTime(testRun.getFinish_time());
            Long suiteTime = Math.round(Double.parseDouble(attributes.getValue("time")));
            calendar.add(Calendar.SECOND, -suiteTime.intValue());
            testRun.setStart_time(calendar.getTime());
            currentTimeSlot = testRun.getFinish_time();
            testSuite.setName(attributes.getValue("name"));
        } else if (qName.equals("testcase")) {
            testcaseIsFound = true;
            test.setName(attributes.getValue("feature"));
            
            result.setFinal_result_id(PASSED.getValue());
            result.setFinish_date(currentTimeSlot);
            calendar.setTime(currentTimeSlot);
            Long testTime = Math.round(Double.parseDouble(attributes.getValue("time")));
            calendar.add(Calendar.SECOND, -testTime.intValue());
            currentTimeSlot = calendar.getTime();
            result.setStart_date(currentTimeSlot);
        } else if (qName.equals("failure") || qName.equals("error")) {
            
            result.setFinal_result_id(FAILED.getValue());
        } else if(qName.equals("skipped")){
            
            result.setFinal_result_id(PENDING.getValue());
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        currentElement = "";
        if (qName.equals("testcase") || (testcaseIsFound && qName.equals(""))) {
            test.setInternalId(test.getName());
            tests.add(test);
            test = new TestDto();
            result.setInternalTestId(test.getName());
            if(result.getFail_reason() != null && result.getFail_reason().equals("$blank")) result.setFail_reason("");
            results.add(result);
            result = new TestResultDto();
            result.setFail_reason("$blank");
            testcaseIsFound = false;
        }
    }

    @Override
    public void characters(char ch[], int start, int length) throws SAXException {
        String value = new String(ch,start,length);
        if(currentElement.equals("failure") || currentElement.equals("error")){
            String res = result.getFail_reason();
            if (res == null || res.equals("$blank")) res = "";
            result.setFail_reason("");
            result.setFail_reason(res.concat(value));
        }
    }

    public TestSuiteDto getTestSuite(){
        return testSuite;
    }

    public TestRunDto getTestRun(){ return testRun; }

    public List<TestDto> getTests(){
        return tests;
    }

    public List<TestResultDto> getTestResults(){
        return results;
    }
}

