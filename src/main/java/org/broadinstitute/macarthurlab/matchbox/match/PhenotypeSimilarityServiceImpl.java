package org.broadinstitute.macarthurlab.matchbox.match;

import org.broadinstitute.macarthurlab.matchbox.entities.Patient;
import org.monarchinitiative.exomiser.core.phenotype.*;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
@Service
public class PhenotypeSimilarityServiceImpl implements PhenotypeSimilarityService {

    private final PhenotypeMatchService phenotypeMatchService;

    public PhenotypeSimilarityServiceImpl(PhenotypeMatchService phenotypeMatchService) {
        this.phenotypeMatchService = phenotypeMatchService;
    }

    @Override
    public PhenotypeSimilarityScorer buildPhenotypeSimilarityScorer(Patient patient) {
        ModelScorer modelScorer = setUpModelScorer(patient);
        return new PhenotypeSimilarityScorerImpl(modelScorer);
    }

    private ModelScorer setUpModelScorer(Patient patient) {
        List<String> queryPatientPhenotypes = PhenotypeSimilarityService.getObservedPhenotypeIds(patient);
        List<PhenotypeTerm> queryPhenotypeTerms = phenotypeMatchService.makePhenotypeTermsFromHpoIds(queryPatientPhenotypes);
        PhenotypeMatcher hpHpQueryMatcher = phenotypeMatchService.getHumanPhenotypeMatcherForTerms(queryPhenotypeTerms);
        return PhenodigmModelScorer.forSameSpecies(hpHpQueryMatcher);
    }

}
