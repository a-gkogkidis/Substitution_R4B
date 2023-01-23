//package com.substitution.utils;
//
//import ca.uhn.fhir.context.FhirContext;
//import ca.uhn.fhir.rest.client.api.IGenericClient;
//import ca.uhn.fhir.rest.client.api.ServerValidationModeEnum;
//import com.substitution.services.DiscountController;
//import com.substitution.services.DiscountServices;
//
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class FHIRController {
//
//    private IGenericClient client;
//
//    private static FHIRController controllerInstance;
//    private FHIRController(String countryEndpoint) {
//
//        // Create a context
//        FhirContext ctx = FhirContext.forR4();
//        // Disable server validation (don't pull the server's metadata first)
//        ctx.getRestfulClientFactory().setServerValidationMode(ServerValidationModeEnum.NEVER);
//        // Now create a client and use it
//        client = ctx.newRestfulGenericClient(countryEndpoint);
//    }
//
//    public static FHIRController getInstance(){
//        if(controllerInstance==null){
//            controllerInstance = new FHIRController("https://jpa.unicom.datawizard.it/fhir");
//        }
//        return controllerInstance;
//    }
//
//    public List<MedicationKnowledge> getMedicationKnowledge(String substanceCode, String doseForm) {
//        Bundle bundle = null;
//        // Substance code provided is ATC if it contains any letter.
//        // Otherwise, it should be in SPOR code
//        if (substanceCode.matches(".*[a-zA-Z].*")) {
//            // Invoke the client
//            bundle = client
//                    .search()
//                    .forResource(MedicationKnowledge.class)
//                    .where(MedicationKnowledge.CLASSIFICATION.exactly().code(substanceCode))
//                    .and(MedicationKnowledge.DOSEFORM.exactly().code(doseForm))
//                    // Missing includes here
//                    .returnBundle(Bundle.class)
//                    .prettyPrint()
//                    .encodedJson()
//                    .execute();
//        } else {
//            // Invoke the client
//            bundle = client
//                    .search()
//                    .forResource(MedicationKnowledge.class)
//                    .where(MedicationKnowledge.INGREDIENT_CODE.exactly().code(substanceCode))
//                    .and(MedicationKnowledge.DOSEFORM.exactly().code(doseForm))
//                    // Missing includes here
//                    // .include()
//                    .returnBundle(Bundle.class)
//                    .prettyPrint()
//                    .encodedJson()
//                    .execute();
//        }
//        if (bundle != null) {
//            System.out.println("Will call Spring action?");
//            DiscountController dc = new DiscountController();
//            dc.getIncomeAge("");
//            List<MedicationKnowledge> listMedicationKnowledge = new ArrayList<>();
//            // response to list
//            bundle.getEntry().forEach(entry -> {
//                if (entry.getResource() instanceof MedicationKnowledge) {
//                    listMedicationKnowledge.add((MedicationKnowledge) entry.getResource());
//                }
//            });
//
//            listMedicationKnowledge.forEach(medicationKnowledge -> {
//
//                List<CodeableConcept> listConcept = medicationKnowledge.getMedicineClassification().get(0).getClassification();
//                listConcept.forEach(lconcept -> {
//                    lconcept.getCoding().forEach(code -> {
//                        System.out.println(code.toString());
//                    });
//                });
//
//                medicationKnowledge.getIngredient().forEach(ingredient -> {
//                    System.out.println(ingredient.getStrength().getNumerator().getValue());
//                });
//
//                medicationKnowledge.getSynonym().forEach(name -> {
//                    System.out.println(name.toString());
//                });
//
//                if (medicationKnowledge.getIngredient().size() > 0) {
//                    System.out.println("Found ingredient");
//                    System.out.println(medicationKnowledge.getIngredient().get(0).getItemReference().getReference()
//                    );
//                    String reference = medicationKnowledge.getIngredient().get(0).getItemReference().getReference();
//                    Substance sub = client.read().resource(Substance.class).withId(reference.replace("Substance/","")).execute();
//                    System.out.println(sub.getCode().getCoding().get(0).getCode());
//                }
//            });
//
//            return listMedicationKnowledge;
//        }
//        return null;
//    }
//
//    public Bundle getMedicationKnowledgeBundle(String substanceCode, String doseForm) {
//        Bundle bundle = null;
//        // Substance code provided is ATC if it contains any letter.
//        // Otherwise, it should be in SPOR code
//        if (substanceCode.matches(".*[a-zA-Z].*")) {
//            // Invoke the client
//            bundle = client
//                    .search()
//                    .forResource(MedicationKnowledge.class)
//                    .where(MedicationKnowledge.CLASSIFICATION.exactly().code(substanceCode))
//                    .and(MedicationKnowledge.DOSEFORM.exactly().code(doseForm))
//                    // Missing includes here
//                    .returnBundle(Bundle.class)
//                    .prettyPrint()
//                    .encodedJson()
//                    .execute();
//        } else
//            // Invoke the client
//            bundle = client
//                    .search()
//                    .forResource(MedicationKnowledge.class)
//                    .where(MedicationKnowledge.INGREDIENT_CODE.exactly().code(substanceCode))
//                    .and(MedicationKnowledge.DOSEFORM.exactly().code(doseForm))
//                    // Missing includes here
//                    // .include()
//                    .returnBundle(Bundle.class)
//                    .prettyPrint()
//                    .encodedJson()
//                    .execute();
//
//        if (bundle != null) {
//            List<MedicationKnowledge> listMedicationKnowledge = new ArrayList<>();
//            // response to list
//            bundle.getEntry().forEach(entry -> {
//                if (entry.getResource() instanceof MedicationKnowledge) {
//                    listMedicationKnowledge.add((MedicationKnowledge) entry.getResource());
//                }
//            });
//
//            listMedicationKnowledge.forEach(medicationKnowledge -> {
//
//                List<CodeableConcept> listConcept = medicationKnowledge.getMedicineClassification().get(0).getClassification();
//                listConcept.forEach(lconcept -> {
//                    lconcept.getCoding().forEach(code -> {
//                        System.out.println(code.toString());
//                    });
//                });
//
//                medicationKnowledge.getIngredient().forEach(ingredient -> {
//                    System.out.println(ingredient.getStrength().getNumerator().getValue());
//                });
//
//                medicationKnowledge.getSynonym().forEach(name -> {
//                    System.out.println(name.toString());
//                });
//
//                if (medicationKnowledge.getIngredient().size() > 0) {
//                    System.out.println("Found ingredient");
//                    System.out.println(medicationKnowledge.getIngredient().get(0).getItemReference().getReference()
//                    );
//                    String reference = medicationKnowledge.getIngredient().get(0).getItemReference().getReference();
//                    Substance sub = client.read().resource(Substance.class).withId(reference.replace("Substance/","")).execute();
//                    System.out.println(sub.getCode().getCoding().get(0).getCode());
//                }
//            });
//
//            return bundle;
//        }
//        return null;
//    }
//}
