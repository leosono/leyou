package com.leyou.order.controller;

import com.leyou.order.service.NotifyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class NotifyController {

    @Autowired
    private NotifyService notifyService;

    @PostMapping(value = "/notify", produces = "application/xml")
    public ResponseEntity<Map<String,String>> handleNotify(@RequestBody Map<String,String> result){
        return ResponseEntity.ok(notifyService.handleNotify(result));
    }
}
