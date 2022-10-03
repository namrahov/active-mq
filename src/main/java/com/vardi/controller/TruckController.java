package com.vardi.controller;

import com.vardi.service.TruckService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping
public class TruckController {

    private final TruckService truckService;

    public TruckController(TruckService truckService) {
        this.truckService = truckService;
    }

    @GetMapping
    public void sendData() {
        truckService.sendData();
    }
}
