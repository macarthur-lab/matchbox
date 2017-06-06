package org.broadinstitute.macarthurlab.matchbox;

import org.broadinstitute.macarthurlab.matchbox.entities.GenomicFeature;
import org.broadinstitute.macarthurlab.matchbox.entities.Patient;
import org.broadinstitute.macarthurlab.matchbox.entities.PhenotypeFeature;
import org.broadinstitute.macarthurlab.matchbox.entities.Variant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class TestData {

    private TestData() {
    }

    /**
     * Returns a complete test patient
     *
     * @return a test patient
     */
    public static Patient getTestPatient() {

        //contact
        Map<String, String> contact = new HashMap<String, String>();
        contact.put("href", "http://www.ncbi.nlm.nih.gov/pubmed/23542699");
        contact.put("institution", "Children's Hospital of Eastern Ontario");
        contact.put("name", "Lijia Huang");

        //disorders
        List<Map<String, String>> disorders = new ArrayList<Map<String, String>>();
        Map<String, String> disorder1 = new HashMap<String, String>();
        disorder1.put("id", "MIM:614261");
        disorders.add(disorder1);
        Map<String, String> disorder2 = new HashMap<String, String>();
        disorder2.put("label", "#614261 MICROCEPHALY-CAPILLARY MALFORMATION SYNDROME; MICCAP");
        disorders.add(disorder2);

        //features
        List<PhenotypeFeature> features = new ArrayList<PhenotypeFeature>();
        PhenotypeFeature pf1 = new PhenotypeFeature("HP:0100026", "yes", "");
        PhenotypeFeature pf2 = new PhenotypeFeature("HP:0003561", "yes", "");
        PhenotypeFeature pf3 = new PhenotypeFeature("HP:0011451", "yes", "");
        PhenotypeFeature pf4 = new PhenotypeFeature("HP:0009879", "yes", "");
        PhenotypeFeature pf5 = new PhenotypeFeature("HP:0011097", "yes", "");
        PhenotypeFeature pf6 = new PhenotypeFeature("HP:0001263", "yes", "");
        features.add(pf1);
        features.add(pf2);
        features.add(pf3);
        features.add(pf4);
        features.add(pf5);
        features.add(pf6);

        //genomic features

        //GF1-gene
        Map<String, String> gene1 = new HashMap<String, String>();
        gene1.put("id", "STAMBP");
        //GF1-type
        Map<String, String> type1 = new HashMap<String, String>();
        type1.put("id", "SO:0001583");
        type1.put("label", "MISSENSE");
        //GF1-variant
        Variant variant1 = new Variant("GRCh37", "2", 74058108L, 0L, "", "");
        GenomicFeature genomicFeature1 = new GenomicFeature(gene1, variant1, 0L, type1);

        //GF2-gene
        Map<String, String> gene2 = new HashMap<String, String>();
        gene2.put("id", "STAMBP");
        //GF2-type
        Map<String, String> type2 = new HashMap<String, String>();
        type2.put("id", "SO:0001587");
        type2.put("label", "STOPGAIN");
        //GF2-variant
        Variant variant2 = new Variant("GRCh37", "2", 74074670L, 0L, "", "");
        GenomicFeature genomicFeature2 = new GenomicFeature(gene2, variant2, 0L, type2);

        List<GenomicFeature> genomicFeatures = new ArrayList<GenomicFeature>();
        genomicFeatures.add(genomicFeature1);
        genomicFeatures.add(genomicFeature2);

        String id = "P0000083";
        String label = "206_LR07-155a1";
        String species = "";
        String sex = "";
        String ageOfOnset = "";
        String inheritanceMode = "";
        Patient p = new Patient(
                id,
                label,
                contact,
                species,
                sex,
                ageOfOnset,
                inheritanceMode,
                disorders,
                features,
                genomicFeatures
        );
        return p;
    }

    /**
     * Returns 2 test patients
     */
    public static List<Patient> getTwoTestPatients() {

        Map<String, String> gene1 = new HashMap<String, String>();
        gene1.put("id", "ENSG00000178104");

        Map<String, String> gene2 = new HashMap<String, String>();
        gene2.put("id", "ENSG00000124356");

        GenomicFeature gFeature1 = new GenomicFeature(gene1,
                new Variant(),
                1l,
                new HashMap<>());

        List<GenomicFeature> gFeatures1 = new ArrayList<GenomicFeature>();
        gFeatures1.add(gFeature1);

        GenomicFeature gFeature2 = new GenomicFeature(gene2,
                new Variant(),
                1l,
                new HashMap<>());

        List<GenomicFeature> gFeatures2 = new ArrayList<GenomicFeature>();
        gFeatures2.add(gFeature1);
        gFeatures2.add(gFeature2);

        Patient testP1 = new Patient("testPatient1Id",
                "testPatient1Label",
                new HashMap<String, String>(),
                "testSpecies",
                "testSex1",
                "testAgeOfOnset1",
                "inheritanceMode1",
                new ArrayList<Map<String, String>>(),
                new ArrayList<PhenotypeFeature>(),
                gFeatures1
        );

        Patient testP2 = new Patient("testPatient2Id",
                "testPatient2Label",
                new HashMap<String, String>(),
                "testSpecies",
                "testSex2",
                "testAgeOfOnset2",
                "inheritanceMode2",
                new ArrayList<Map<String, String>>(),
                new ArrayList<PhenotypeFeature>(),
                gFeatures2
        );

        List<Patient> patients = new ArrayList<Patient>();
        patients.add(testP1);
        patients.add(testP2);
        return patients;
    }

    public static Map<String, String> geneIdentifiers() {
        Map<String, String> geneSymbolToEnsemblId = new HashMap<>();
        geneSymbolToEnsemblId.put("PDE4DIP", "ENSG00000178104");
        geneSymbolToEnsemblId.put("STAMBP", "ENSG00000124356");
        geneSymbolToEnsemblId.put("FGFR2", "ENSG00000066468");
        return geneSymbolToEnsemblId;
    }
}
