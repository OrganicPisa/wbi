package com.broadcom.wbi.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

@Controller
public class UIClientController {

    @RequestMapping(value = {"/404"})
    public String notFound(HttpServletRequest req, Model model) {
        return "404";
    }

    @RequestMapping(value = {"/403"})
    public String notPermission(HttpServletRequest req, Model model) {
        return "403";
    }

    @RequestMapping(value = {"/login"}, method = {RequestMethod.GET})
    public String login(HttpServletRequest request, Model model,
                        @RequestParam(value = "error", defaultValue = "false") final Boolean error,
                        @RequestParam(value = "logout", defaultValue = "false") final Boolean logout,
                        @RequestParam(value = "message", defaultValue = "") final String message) {
        String referrer = request.getHeader("Referer");
        if (referrer != null && referrer.toLowerCase().indexOf("login") != -1) {
            referrer = "/home";
        }
        request.getSession().setAttribute("url_prior_login", referrer);
        if (logout) {
            SecurityContextHolder.clearContext();
            model.addAttribute("logout", true);
        } else {
            if (error) {
                model.addAttribute("loginError", true);
            }
            if (message != null && !message.trim().isEmpty()) {
                model.addAttribute("message", message);
                model.addAttribute("loginError", true);
            }
        }
        return "login";
    }

    @RequestMapping(value = {"/welcome", "/", "/index", "/home"})
    public String index(HttpServletRequest req, Model model) {
        return "index";
    }

    @RequestMapping(value = {"/header"})
    public String header(HttpServletRequest req, Model model) {
        return "header";
    }

    @RequestMapping(value = {"/footer"})
    public String footer(HttpServletRequest req, Model model) {
        return "footer";
    }

    @RequestMapping(value = {"/active"})
    public String active(HttpServletRequest req, Model model) {
        return "forward:/";
    }

    @RequestMapping(value = {"/archived"})
    public String archived(HttpServletRequest req, Model model) {
        return "forward:/";
    }

    @RequestMapping(value = {"/segment/{type}"})
    public String index(HttpServletRequest req, Model model, @PathVariable String type) {
        if (type.equalsIgnoreCase("index")) {
            return "segment/index";
        }
        return "segment/partials/" + type;
    }

    @RequestMapping(value = {"/program/{type}"})
    public String programIndex(HttpServletRequest req, Model model, @PathVariable String type) {
        return "program/" + type;
    }

    @RequestMapping(value = {"/program/{type}/{page}"})
    public String programIndex(HttpServletRequest req, Model model, @PathVariable String type, @PathVariable String page) {
        if (type.equalsIgnoreCase("index")) {
            return "program/index";
        }
        if (type.equalsIgnoreCase("new")) {
            if (page.equalsIgnoreCase("index"))
                return "program/new/index";
            else if (page.equalsIgnoreCase("sidebar"))
                return "program/new/sidebar";
        } else {
            if (page.equalsIgnoreCase("index") || page.trim().isEmpty()) {
                return "program/" + type + "/index";
            } else if (page.equalsIgnoreCase("sidebar")) {
                return "program/" + type + "/sidebar";
            }
        }
        return "program/" + type + "/partials/" + page;
    }

    @RequestMapping(value = {"/program/{type}/{pid}/{rid}/{page}"})
    public String programIndex(HttpServletRequest req, Model model, @PathVariable int pid, @PathVariable int rid,
                               @PathVariable String type, @PathVariable String page) {
        return "forward:/";
    }

    @RequestMapping(value = {"/report/{type}"}, method = {RequestMethod.GET})
    public ModelAndView type(HttpServletRequest req, ModelMap model, @PathVariable String type) {
        return new ModelAndView("/report/" + type);
    }

    @RequestMapping(value = {"/report/{type}/{page}"})
    public String reportIindex(HttpServletRequest req, Model model, @PathVariable String type, @PathVariable String page) {
        if (page.equalsIgnoreCase("index")) {
            return "report/" + type + "/index";
        } else if (page.equalsIgnoreCase("sidebar")) {
            return "report/" + type + "/sidebar";
        }
        return "report/" + type + "/partials/" + page;
    }

    @RequestMapping(value = {"/report/{rtype}/{type}/{page}"})
    public String reportIndex(HttpServletRequest req, Model model,
                              @PathVariable String rtype, @PathVariable String type,
                              @PathVariable String page) {
        return "forward:/";
    }
}
