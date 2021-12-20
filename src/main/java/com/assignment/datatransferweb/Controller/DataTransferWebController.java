package com.assignment.datatransferweb.Controller;

import com.assignment.datatransferweb.Service.DataControllerWebService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;


@RestController
@RequiredArgsConstructor
public class DataTransferWebController {
    private final DataControllerWebService dataControllerWebService;

    @PostMapping("/capture")
    public void captureImage(HttpServletRequest request) { dataControllerWebService.captureImage(request); }

    @GetMapping("/send")
    public void sendImage(HttpServletRequest request) { dataControllerWebService.sendImage(request); }

    @GetMapping("/receive")
    public void receiveImage(HttpServletRequest request) { dataControllerWebService.receiveImage(request); }
}