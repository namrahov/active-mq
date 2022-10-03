package com.vardi.service;

import com.vardi.utils.VehicleNameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jms.UncategorizedJmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class TruckService {
    private final JmsTemplate jmsTemplate;
    @Value("${fleetman.position.queue}")
    private String queueName;

    private static SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    public TruckService(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    public void sendData() {
        List<Map<String, List<String>>> mapList = setUpData();
        //System.out.println(mapList);
        for (Map<String, List<String>> reports : mapList) {


            for (List<String> rows : reports.values()) {
                for (String nextReport : rows) {
                    if(nextReport.isEmpty()) break;
                    String[] data = nextReport.split("\"");
                    String lat = data[1];
                    String longitude = data[3];

                    // Spring will convert a HashMap into a MapMessage using the default MessageConverter.
                    HashMap<String, String> positionMessage = new HashMap<>();
                    Optional<String> names = reports.entrySet()
                            .stream()
                            .filter(entry -> Objects.equals(entry.getValue(), rows))
                            .map(Map.Entry::getKey)
                            .findFirst();

                    positionMessage.put("vehicle", names.get());
                    positionMessage.put("lat", lat);
                    positionMessage.put("long", longitude);
                    positionMessage.put("time", formatter.format(new java.util.Date()));

                    sendToQueue(positionMessage);
                   // System.out.println(positionMessage);

                    // We have an element of randomness to help the queue be nicely
                    // distributed
                    delay(Math.random() * 10000 + 2000);
                }
            }
        }
    }

    private List<Map<String, List<String>>> setUpData() {

        PathMatchingResourcePatternResolver path = new PathMatchingResourcePatternResolver();
        List<Map<String, List<String>>> mapList = new ArrayList<>();
        try {
            for (Resource nextFile : path.getResources("tracks/*")) {
                Map<String, List<String>> reports = new HashMap<>();
                URL resource = nextFile.getURL();
                File f = new File(resource.getFile());

                String vehicleName = VehicleNameUtils.prettifyName(f.getName());

                InputStream targetStream = new FileInputStream(f);
                try (Scanner sc = new Scanner(targetStream)) {
                    List<String> thisVehicleReports = new ArrayList<>();
                    while (sc.hasNextLine()) {
                        String nextReport = sc.nextLine();
                        thisVehicleReports.add(nextReport);
                    }
                    reports.put(vehicleName, thisVehicleReports);
                }
                mapList.add(reports);
            }

            return mapList;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void sendToQueue(Map<String, String> positionMessage) {
        boolean messageNotSent = true;
        while (messageNotSent) {
            // broadcast this report
            try {
                jmsTemplate.convertAndSend(queueName, positionMessage);
                messageNotSent = false;
            } catch (UncategorizedJmsException e) {
                // we are going to assume that this is due to downtime - back off and go again
                delay(5000);
            }
        }
    }

    private void delay(double d) {
        try {
            Thread.sleep((long) d);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
