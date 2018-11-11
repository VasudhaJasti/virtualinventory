package com.capturerx.cumulus4.virtualinventory.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

@RestController
public class SwaggerRediretsController {

    @GetMapping(path="/swagger")
    public ModelAndView SwaggerRedirect() {
        return new ModelAndView(
                new RedirectView("/swagger-ui.html", true)
        );
    }
}