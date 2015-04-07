package edu.warbot.online;

import edu.warbot.agents.ControllableWarAgent;
import edu.warbot.agents.WarAgent;
import edu.warbot.agents.enums.WarAgentType;
import edu.warbot.game.Team;
import edu.warbot.game.WarGame;
import edu.warbot.game.WarGameSettings;
import edu.warbot.online.logs.GameLog;
import edu.warbot.online.logs.RGB;
import edu.warbot.online.messaging.AgentMessage;
import edu.warbot.online.messaging.ClassicMessage;
import edu.warbot.online.messaging.EndMessage;
import edu.warbot.online.messaging.InitMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.util.MimeTypeUtils;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by BEUGNON on 11/03/2015.
 *
 * Classe représentant une partie jouée sur le web
 * en streaming
 *
 * @author beugnon
 */
public class WebGame extends WarGame
{
    private final String user;

    private GameLog gameLog;

    private SimpMessageSendingOperations messagingTemplate;

    private boolean firstCall;

    public int getTick() {
        return tick;
    }

    public void setTick(int tick) {
        this.tick = tick;
    }

    private int tick;

    public WebGame(String user,SimpMessageSendingOperations messagingTemplate, WarGameSettings settings)
    {
        super(settings);
        tick = 0;
        this.user = user;
        this.firstCall = true;
        this.messagingTemplate = messagingTemplate;
        this.gameLog = new GameLog();
    }

    @Override
    public void setGameStarted() {
        super.setGameStarted();
    }

    @Override
    public void doAfterEachTick()
    {
        super.doAfterEachTick();
        if(isFirstCall())
        {
            sendInitMessage();
            firstCall = false;
        }
        for (Team t : getPlayerTeams())
        {

            for(WarAgent a : t.getAllAgents()) {
                if (a instanceof ControllableWarAgent) {
                    Map<String, Object> map = (this.getGameLog().addOrUpdateControllableEntity((ControllableWarAgent) a));
                //    sendMessage(new AgentMessage(map));
                } else {
                    Map<String, Object> map = this.getGameLog().addOrUpdateEntity(a);
              //      sendMessage(new AgentMessage(map));
                }
            }
        }

        for(WarAgent a : getMotherNatureTeam().getAllAgents())
        {
            Map<String,Object> map = this.getGameLog().addOrUpdateEntity(a);
            //sendMessage(new AgentMessage(map));
        }


        if(getGameMode().getEndCondition().isGameEnded())
            setGameOver();
    }

    protected boolean isFirstCall()
    {
        return firstCall;
    }

    protected void sendInitMessage() {
        //Send init message
        Map<String,Object> content = new HashMap<>();

        //Prepare Environment Variables for client
        Map<String,Object> environment = new HashMap<>();
        environment.put("width",getSettings().getSelectedMap().getWidth());
        environment.put("height",getSettings().getSelectedMap().getHeight());
        environment.put("mapName",getSettings().getSelectedMap().getName());
        //TODO trouver un autre moyen d'envoyer les limites de la carte
       // environment.put("walls",getSettings().getSelectedMap().getMapForbidArea());

        content.put("environment",environment);

        //Prepare Team Variables for client
        List<Map<String,Object>> teams = new ArrayList<>();
        for(Team t : getAllTeams())
        {
            Map<String,Object> team = new HashMap<>();
            team.put("name",t.getName());
            team.put("color",(new RGB(t.getColor().getRed(),t.getColor().getGreen(),t.getColor().getRed())).toString());
            teams.add(team);
        }
        content.put("teams", teams);

        //Prepare Agent Variables for client
        List<Map<String,Object>> agents = new ArrayList<>();
        for(Team t : getAllTeams())
        {
            for(WarAgent a : t.getAllAgents())
            {
                if (a instanceof ControllableWarAgent)
                    agents.add(this.getGameLog().addControllableAgent((ControllableWarAgent) a));
                else
                    agents.add(this.getGameLog().addEntity(a));

            }


        }
        content.put("agents", agents);

        //Send message
        sendMessage(new InitMessage(content));
    }

    @Override
    public void setGameOver()
    {
        super.setGameOver();
        sendMessage(new EndMessage("end game"));
    }




    public void sendMessage(ClassicMessage cm)
    {
        if(cm.getHeader().equals("init") || cm.getHeader().equals("end") || cm.getHeader().equals("synchro")) {
            this.getMessageSender().
                    convertAndSendToUser
                            (getUser(), "/queue/game", cm);
        }
        else
        {
            AgentMessage a = (AgentMessage) cm;
            if(a.getContent().size() > 1) // If useful data send it
            {
                this.getMessageSender().
                        convertAndSendToUser
                                (getUser(), "/queue/game.agents." + a.getContent().get("name"), a);
            }
        }
    }


    public SimpMessageSendingOperations getMessageSender()
    {
        return this.messagingTemplate;
    }

    public String getUser()
    {
        return user;
    }

    public GameLog getGameLog() {
        return gameLog;
    }
}