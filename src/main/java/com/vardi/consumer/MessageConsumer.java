package com.vardi.consumer;

import com.vardi.model.VehicleBuilder;
import com.vardi.model.VehiclePosition;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

@Component
public class MessageConsumer {
    @JmsListener(destination = "${fleetman.position.queue}")
    public void processPositionMessageFromQueue(Map<String, String> incomingMessage) {
        Date convertedDatestamp = new java.util.Date();

        VehiclePosition newReport = new VehicleBuilder()
                .withName(incomingMessage.get("vehicle"))
                .withLat(new BigDecimal(incomingMessage.get("lat")))
                .withLng(new BigDecimal(incomingMessage.get("long")))
                .withTimestamp(convertedDatestamp)
                .build();

        System.out.println(newReport);
    }
}
