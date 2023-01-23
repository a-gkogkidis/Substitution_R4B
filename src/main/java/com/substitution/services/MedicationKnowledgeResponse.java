package com.substitution.services;

import lombok.Getter;
import lombok.Setter;
import org.hl7.fhir.r4b.model.MedicinalProductDefinition;

import java.util.List;

@Getter
@Setter
public class MedicationKnowledgeResponse {

        private Boolean success;
        private Integer count;
        private List<MedicinalProductDefinition> data;

        @Getter
        @Setter
        private class MedicationKnowledgeData {

        }
}
