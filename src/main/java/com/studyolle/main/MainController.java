package com.studyolle.main;

import com.studyolle.account.CurrentUser;
import com.studyolle.domain.Account;
import lombok.ToString;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    @GetMapping("/")
    public String home(@CurrentUser Account account, Model model) {
        if (account != null) {
            model.addAttribute(account);

            return "index";
        }

        return "login";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
