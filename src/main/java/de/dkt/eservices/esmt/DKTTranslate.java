/**
 * Copyright (C) 2015 3pc, Art+Com, Condat, Deutsches Forschungszentrum
 * für Künstliche Intelligenz, Kreuzwerke (http://digitale-kuratierung.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.dkt.eservices.esmt;

import com.hp.hpl.jena.rdf.model.*;
import eu.freme.common.conversion.etranslate.TranslationConversionService;
import eu.freme.common.conversion.rdf.RDFConstants;
import eu.freme.common.conversion.rdf.RDFConversionService;
import eu.freme.common.exception.BadRequestException;
import eu.freme.common.exception.FREMEHttpException;
import eu.freme.common.rest.BaseRestController;
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
public class DKTTranslate extends BaseRestController {
    Logger logger = Logger.getLogger(DKTTranslate.class);
    @Autowired
    TranslationConversionService translationConversionService;

    @Autowired
    RestHelper restHelper;

    @Autowired
    RDFConversionService rdfConversionService;

    @Autowired
    NIFParameterFactory nifParameterFactory;
    
    


    @RequestMapping(value = "/e-smt", method = {RequestMethod.POST, RequestMethod.GET})
    public ResponseEntity<String> translate(
            @RequestHeader(value = "Accept", required = false) String acceptHeader,
            @RequestHeader(value = "Content-Type", required = false) String contentTypeHeader,
            @RequestParam(value = "input", required = false) String input,
            @RequestParam(value = "source-lang", required = false) String sourceLang,
            @RequestParam(value = "target-lang", required = false) String targetLang,
            @RequestBody (required = false) String postBody,
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
            Resource subject = firstPlaintext.getSubject();
            String inputString = firstPlaintext.getObject().asLiteral().getString();

            // get shell script (with inputString, sourceLang, targetLang) and write result to resultString
            String resultString = new TranslateSegment().executeCommand(inputString, sourceLang, targetLang);
            // replace with ProcessBuilder eventually


            if (!model.getNsPrefixMap().containsValue(RDFConstants.itsrdfPrefix)) {
                model.setNsPrefix("itsrdf", RDFConstants.itsrdfPrefix);
            }

            Literal literal = model.createLiteral(resultString, targetLang);
            subject.addLiteral(model.getProperty(RDFConstants.itsrdfPrefix + "target"), literal);

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


