package de.dkt.eservices.esmt;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import eu.freme.common.conversion.etranslate.TranslationConversionService;
import eu.freme.common.conversion.rdf.RDFConstants;
import eu.freme.common.conversion.rdf.RDFConversionService;
import eu.freme.common.exception.BadRequestException;
import eu.freme.common.exception.FREMEHttpException;
import eu.freme.common.exception.InternalServerErrorException;
import eu.freme.common.rest.NIFParameterFactory;
import eu.freme.common.rest.NIFParameterSet;
import eu.freme.common.rest.RestHelper;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


/**
 * Created by ansr01 on 14/04/16.
 */

@RestController
public class TranslateSegment {
    Logger logger = Logger.getLogger(TranslateSegment.class);
    @Autowired
    TranslationConversionService translationConversionService;

    @Autowired
    RestHelper restHelper;

    @Autowired
    RDFConversionService rdfConversionService;

    @Autowired
    NIFParameterFactory nifParameterFactory;


    @RequestMapping(value = "/e-smt", method = RequestMethod.POST)
    public ResponseEntity<String> translate(
            @RequestHeader(value = "Accept") String acceptHeader,
            @RequestHeader(value = "Content-Type") String contentTypeHeader,
            @RequestParam("language") String language,
            @RequestBody String postBody,
            @RequestParam Map<String, String> allParams) {

        NIFParameterSet nifParameters = restHelper.normalizeNif(postBody,
                acceptHeader, contentTypeHeader, allParams, false);

        Model model = null;

        try {
            if (nifParameters.getInformat().equals(RDFConstants.RDFSerialization.PLAINTEXT)) {
                model = ModelFactory.createDefaultModel();
                rdfConversionService.plaintextToRDF(model, nifParameters.getInput(), null, nifParameterFactory.getDefaultPrefix());
            } else {

                model = rdfConversionService.unserializeRDF(postBody, nifParameters.getInformat());
            }

            Statement firstPlaintext = rdfConversionService.extractFirstPlaintext(model);

            String input = firstPlaintext.getObject().asLiteral().getString();
         // call shell script and extract output to convert to nif



        return restHelper.createSuccessResponse(model, nifParameters.getOutformat());
        }catch (FREMEHttpException e){
            logger.error("Error", e);
            throw e;
        } catch (Exception e) {
            logger.error("Error", e);
            throw new BadRequestException(e.getMessage());
        }
    }

}


