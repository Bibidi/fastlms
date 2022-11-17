package com.zerobase.fastlms.main.controller;

import com.zerobase.fastlms.component.MailComponents;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@RequiredArgsConstructor
@Controller
public class MainController {

    private final MailComponents mailComponents;

    @RequestMapping("/")
    public String index() {
//
//        String email = "kbd0000@naver.com";
//        String subject = "안녕하세요. 제로베이스입니다. ";
//        String text = "<p>안녕하세요.</p><p>반값습니다.</p><a href=\"\">링크</a>";

//        mailComponents.sendMail(email, subject, text);
        return "index";
    }

    @RequestMapping("/hello")
    public void hello(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter printWriter = response.getWriter();

        String msg = "<html>" +
                "<head>" +

                "</head>" +
                "<body>" +
                "<p>hello</p>" +
                "<p>fastlms website!!!!</p>" +
                "<p>안녕하세요 !!!! ====> </p>" +
                "</body>" +
                "</html>";

        printWriter.write(msg);
        printWriter.close();
    }
}
