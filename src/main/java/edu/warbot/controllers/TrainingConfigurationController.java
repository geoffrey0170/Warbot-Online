package edu.warbot.controllers;

import edu.warbot.form.TrainingConfigurationForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Created by jimmy on 13/04/15.
 */

@Controller
@Secured({"ROLE_USER","ROLE_ADMIN"})
public class TrainingConfigurationController {

    final Logger logger = LoggerFactory.getLogger(TrainingConfigurationController.class);

    @RequestMapping(value = "configuration/list", method = RequestMethod.GET)
    public String listEditor() {

        return "configuration-editor/select";
    }

    @RequestMapping(value = "configuration/create", method = RequestMethod.POST)
    public String createEditor() {

        return "configuration-editor/editor";
    }

    @RequestMapping(value = "configuration/new",method = RequestMethod.GET)
    public String createEditor(Model model)
    {
        model.addAttribute("formZone", new TrainingConfigurationForm());
        return "configuration-editor/create";
    }




}
