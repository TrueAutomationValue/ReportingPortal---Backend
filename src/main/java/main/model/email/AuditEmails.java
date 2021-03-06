package main.model.email;

import main.exceptions.RPException;
import main.model.db.dao.audit.AuditDao;
import main.model.db.dao.audit.AuditStatisticDao;
import main.model.db.dao.project.UserDao;
import main.model.dto.*;
import org.json.JSONException;

import javax.naming.NamingException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class AuditEmails extends Emails {

    private AuditStatisticDao auditStatisticController;

    public AuditEmails() {
        auditStatisticController = new AuditStatisticDao();
    }

    public List<EmailDto> GetUpcomingEmails() throws SQLException, IOException, JSONException, NamingException, IllegalAccessException, InstantiationException, RPException {
        List<AuditStatisticDto> audits = auditStatisticController.getAll();
        List<String> recipients = getRecipients();

        List<EmailDto> emails = new ArrayList<>();
        emails.addAll(getUpcomingAudits(14, audits, recipients));
        emails.addAll(getUpcomingAudits(30, audits, recipients));
        emails.addAll(getUpcomingAudits(0, audits, recipients));

        return emails;
    }

    public List<EmailDto> GetOverdueEmails() throws SQLException, IOException, JSONException, NamingException, IllegalAccessException, InstantiationException, RPException {
        List<AuditStatisticDto> audits = auditStatisticController.getAll();
        List<String> recipients = getRecipients();
        List<AuditStatisticDto> overdueAudits = audits.stream()
                .filter(audit -> dateUtils.compareByDateOnly(getDueDate(audit), dateUtils.addDays(new Date(), -1)))
                .collect(Collectors.toList());

        List<EmailDto> emails = new ArrayList<>();

        for(AuditStatisticDto overdueAudit: overdueAudits){
            EmailDto email = new EmailDto();
            email.setSubject("[REPORTING PORTAL] Overdue Project Audit - " + overdueAudit.getName());
            email.setContent("<p style=\"font-family: Calibri, sans-serif; font-size: 11pt; line-height: 4px;\">Next Audit Date for the <strong><a style=\"color: #6b6b6b;\" href=\""
                    + hostUri() +"#/project/" + overdueAudit.getId()+"\">" + overdueAudit.getName() +"</a>&nbsp; </strong>project is overdue and requires your attention.</p>\n" +
                    "<p style=\"font-family: Calibri, sans-serif; font-size: 11pt; line-height: 4px;\">Please follow this <a style=\"color: #6b6b6b;\" href=\""
                    + hostUri() + "#/audit\">link</a> to access Audits Dashboard.</p>\n" +
                    "<p>&nbsp;</p>" +
                    "<p style=\"font-family: Calibri, sans-serif; font-size: 10pt; line-height: 2px;\">Best Regards,</p>\n" +
                    "<p style=\"font-family: Calibri, sans-serif; font-size: 10pt; line-height: 2px;\">Reporting Portal Administration</p>\n" +
                    "<p><img src=\"cid:logo\"></p>\n" +
                    "<p><span style=\"font-family: Calibri; font-size: 7.5pt; color: #7f7f7f;\">This message is automatically generated by Notification Assistant for Reporting Portal.<br /> If you think it was sent incorrectly, please contact your Reporting Portal administrators.</span></p>");
            List<String> customRecipients = new ArrayList<>(recipients);

            if(!Objects.equals(overdueAudit.getLast_created_id(), overdueAudit.getLast_submitted_id())){
                customRecipients.addAll(getCustomRecipients(overdueAudit.getLast_created_id()));
            }
            customRecipients = customRecipients.stream().distinct().collect(Collectors.toList());
            email.setRecipients(customRecipients);
            emails.add(email);
        }

        return emails;
    }

    private List<EmailDto> getUpcomingAudits(int daysFromToday, List<AuditStatisticDto> audits, List<String> recipients) throws RPException {
        List<EmailDto> emails = new ArrayList<>();
        List<AuditStatisticDto> upcomingAudits = audits.stream()
                .filter(audit -> dateUtils.compareByDateOnly(getDueDate(audit), dateUtils.addDays(new Date(), daysFromToday)))
                .collect(Collectors.toList());

        String days = (daysFromToday == 0) ? "today" : String.valueOf(daysFromToday);

        for(AuditStatisticDto upcAudit: upcomingAudits){
            EmailDto email = new EmailDto();
            email.setSubject("[REPORTING PORTAL] Upcoming Project Audit - " + upcAudit.getName());
            email.setContent("<p style=\"font-family: Calibri, sans-serif; font-size: 11pt; line-height: 4px;\">Next Audit for the <strong><a style=\"color: #6b6b6b;\" href=\""
                    + hostUri() +"#/project/" + upcAudit.getId()+"\">" + upcAudit.getName() + "</a> </strong>project should be submitted within<strong> "
                    + days + " days.</strong></p>\n" +
                    "<p style=\"font-family: Calibri, sans-serif; font-size: 11pt; line-height: 4px;\">Please follow this <a style=\"color: #6b6b6b;\" href=\""
                    + hostUri() + "#/audit\">link</a> to access Audits Dashboard and create a new Audit.</p>\n" +
                    "<p>&nbsp;</p>\n" +
                    "<p style=\"font-family: Calibri, sans-serif; font-size: 10pt; line-height: 2px;\">Best Regards,</p>\n" +
                    "<p style=\"font-family: Calibri, sans-serif; font-size: 10pt; line-height: 2px;\">Reporting Portal Administration</p>\n" +
                    "<p><img src=\"cid:logo\"></p>\n" +
                    "<p><span style=\"font-family: Calibri; font-size: 7.5pt; color: #7f7f7f;\">This message is automatically generated by Notification Assistant for Reporting Portal.<br /> If you think it was sent incorrectly, please contact your Reporting Portal administrators.</span></p>");
            email.setRecipients(recipients);
            emails.add(email);
        }
        return emails;
    }

    private List<String> getRecipients() throws RPException {
        UserDao userDao = new UserDao();
        UserDto user = new UserDto();
        List<UserDto> users = userDao.searchAll(user);
        users = users.stream().filter(userX -> userX.getAudit_admin() == 1 && userX.getAudit_notifications() == 1).collect(Collectors.toList());
        return users.stream().map(UserDto::getEmail).collect(Collectors.toList());
    }

    private List<String> getCustomRecipients(Integer id) throws RPException {
        AuditDao auditDao = new AuditDao();
        AuditDto auditTemplate = new AuditDto();
        auditTemplate.setId(id);
        List<AuditorDto> users = auditDao.searchAll(auditTemplate).get(0).getAuditors();
        users = users.stream().filter(userX -> userX.getAudit_notifications() == 1).collect(Collectors.toList());
        return users.stream().map(auditor -> auditor.getEmail()).collect(Collectors.toList());
    }

    private Date getDueDate(AuditStatisticDto auditStat){
        if(auditStat.getLast_created_id() != null
                && !Objects.equals(auditStat.getLast_created_id(), auditStat.getLast_submitted_id())){
            return auditStat.getLast_created_due_date();
        }
        else if(auditStat.getLast_submitted_id() != null){
            return dateUtils.addMonth(auditStat.getLast_submitted_date(), 6);
        }else{
            return dateUtils.addMonth(auditStat.getCreated(), 6);
        }
    }
}
