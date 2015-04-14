package edu.warbot.code_editor_gestion;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import edu.warbot.models.Account;
import edu.warbot.models.WebCode;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;


/**
 * Created by quent on 14/04/2015.
 */
public class CodeEditorListener implements ApplicationListener<ApplicationEvent> {

    private HashMap<Account, WebCode> userCodeLocks;
    private HashMap<WebCode, Account> codeUserLocks;
    private String logIn;

    public CodeEditorListener() {
        userCodeLocks = new HashMap<Account, WebCode>();
        codeUserLocks = new HashMap<WebCode, Account>();
        logIn = "/editor/register";
    }


    public void onApplicationEvent(ApplicationEvent event) {

        if(event instanceof SessionConnectEvent) {
            handleCodeLock((SessionConnectEvent) event);
        } else if(event instanceof SessionDisconnectEvent) {
            handleCodeUnlock((SessionDisconnectEvent) event);
        }
    }

    private void handleCodeLock(SessionConnectEvent event) {
        SimpMessageHeaderAccessor headers = SimpMessageHeaderAccessor.wrap(event.getMessage());
        String nameAccount = headers.getUser().getName();


    }

    private void handleCodeUnlock(SessionDisconnectEvent event) {
        SimpMessageHeaderAccessor headers = SimpMessageHeaderAccessor.wrap(event.getMessage());
        String nameAccount = headers.getUser().getName();

        codeUserLocks.remove(nameAccount);

    }


    public HashMap<Account,WebCode> getLogs() {
        return userCodeLocks;
    }

    public void setLogs(HashMap<Account, WebCode> locks) {
        this.userCodeLocks = locks;
    }

    public String getLogIn() {
        return logIn;
    }

    public void setLogIn(String logIn) {
        this.logIn = logIn;
    }

    public HashMap<WebCode, Account> getReverseLogs() {
        return codeUserLocks;
    }

    public void setReverseLogs(HashMap<WebCode, Account> reverseLogs) {
        this.codeUserLocks = reverseLogs;
    }

    public void lock(Account account, WebCode code) {
        addToLocks(account, code);
    }

    public void unlock(Account account, WebCode code) {
        removeFromLocks(account, code);
    }

    public void addToLocks(Account user, WebCode code) {
        userCodeLocks.put(user, code);
        codeUserLocks.put(code, user);
    }

    public void removeFromLocks(Account user, WebCode code) {
        userCodeLocks.remove(user);
        codeUserLocks.remove(code);
    }
}
