package com.substitution;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Main {
    public static void main(String[] args) {

//        FHIRController controller = FHIRController.getInstance();

        // Dose form 10219000 -> tablet, 10210000 -> Capsule Hard
        // Requesat by SPOR Code
        // Request by ATC
//        System.out.println(controller.getMedicationKnowledge("C08CA01","10219000"));
        SpringApplication.run(Main.class, args);
//        System.out.println(controller.getMedicationKnowledge("100000085259","10219000").size());

    }
}