package com.substitution.services;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.api.ServerValidationModeEnum;
import com.substitution.utils.helper.SubstanceEquivalence;
import org.hl7.fhir.r4b.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


@RestController
public class DiscountController {


    @Autowired
    private DiscountServices discountService;

    @GetMapping("/getEquivalentSubstance")
    public ResponseEntity<SubstanceEquivalence> getEquivalentSubstance(@RequestParam(required = true, name = "substance") String substance) {
        SubstanceEquivalence incomeObj = new SubstanceEquivalence();
        incomeObj.set_substance(substance);
        return new ResponseEntity<SubstanceEquivalence>(discountService.discountCalculator(incomeObj), HttpStatus.OK);
    }

    @GetMapping("/equivalentMedicationList")
    public String getEquivalent(@RequestParam(required = true, name = "substance") String substance, @RequestParam(required = true, name = "doseform") String doseform, @RequestParam(required = false, name = "country") String targetCountry) {
        String country = targetCountry == null || targetCountry.isEmpty() ? "100000072172" : targetCountry; // if targetCountry is not set, then force Estonia
        // Create a context
        FhirContext ctx = FhirContext.forR4B();
        // Disable server validation (don't pull the server's metadata first)
        ctx.getRestfulClientFactory().setServerValidationMode(ServerValidationModeEnum.NEVER);
        // Now create a client and use it
        IGenericClient client = ctx.newRestfulGenericClient("https://jpa.unicom.datawizard.it/fhir");

        List<MedicinalProductDefinition> mpdList = new ArrayList<>();

        // Invoke the client
        Bundle ingredientResults = client
                .search()
                .forResource(Ingredient.class)
                .where(Ingredient.SUBSTANCE_CODE.exactly().code(substance))
                .returnBundle(Bundle.class)
                .execute();

        if (ingredientResults.getEntry().size() != 0) {
            for (Bundle.BundleEntryComponent entry : ingredientResults.getEntry()) {
                Ingredient ingredient = (Ingredient) entry.getResource();

                // This should be second layer of filtering by Reference Substance (Just Amlodipine no modifier)
                System.out.println(ingredient.getSubstance().getStrength().get(0).getReferenceStrength().get(0).getSubstance().getConcept().getCoding().get(0).getCode());
                // ingredient.getFor().get(x) as for in Ingredient has:
                // 0 -> MedicinalProductDefinition
                // 1 -> ManufacturedItemDefinition
                // 2 -> AdministrableProductDefinition

                // This code should be removed once the URL of the FHIR server is by country
                MedicinalProductDefinition mpd = client.read().resource(MedicinalProductDefinition.class).withUrl(ingredient.getFor().get(0).getReference())
                        .execute();
                CodeableConcept languages = mpd.getName().get(0).getCountryLanguage().get(0).getLanguage();
                if (languages != null && !languages.isEmpty()) {
                    String code = languages.getCoding().get(0).getCode();
//                String display = languages.getCoding().get(0).getDisplay();
//                System.out.println("Language: " + code + " " + display);

                    // Limit data by country
                    if (code.equals(country)) {

                        AdministrableProductDefinition adp = client.read().resource(AdministrableProductDefinition.class).withUrl(ingredient.getFor().get(2).getReference()).execute();
                        if (adp.getAdministrableDoseForm().getCoding().get(0).getCode().equals(doseform)) {
                            mpdList.add(mpd);
                        }
                    }
                }
            }
        } else {
            // Specific Substance has zero results ex. no results for Amlodipine Besilate
            // So we need to check one level higher by ATC (or SPOR equivalent code)

            // get parent of requested substance
//            getEquivalentSubstance(substance).getBody().get_substance()
            substance = getEquivalentSubstance(substance).getBody().get_response();//substance.equals("100000090079") ? "C08CA01" : "100000095065"; // force amlodipine either ATC or SPOR
            Bundle mpdResults = client
                    .search()
                    .forResource(MedicinalProductDefinition.class)
                    .where(MedicinalProductDefinition.PRODUCT_CLASSIFICATION.exactly().code(substance))
                    .and(MedicinalProductDefinition.NAME_LANGUAGE.exactly().code(country))
                    .returnBundle(Bundle.class)
                    .execute();

            System.out.println( mpdResults.getEntry().size());
            Collection<String> mpdIDs = new ArrayList<>();
            mpdResults.getEntry().forEach(bundleEntryComponent -> {
                MedicinalProductDefinition mpd = (MedicinalProductDefinition) bundleEntryComponent.getResource();
                System.out.println(mpd.getIdBase());
                mpdIDs.add(mpd.getId());
                mpdList.add(mpd);
            });
            Bundle adp = client.search().forResource(AdministrableProductDefinition.class)
                    .where(AdministrableProductDefinition.FORM_OF.hasAnyOfIds(mpdIDs))
                    .returnBundle(Bundle.class)
                    .execute();

//            System.out.println(ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(adp));
        }


        System.out.println(mpdList.size());

        Bundle bundle = new Bundle();
        mpdList.forEach(medicinalProductDefinition -> {
            bundle.addEntry().setResource(medicinalProductDefinition);
        });
        return ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(bundle);
    }
}
