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
import eu.freme.common.conversion.rdf.RDFConstants.RDFSerialization;
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

import de.dkt.common.niftools.NIFWriter;

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
    
    

    // endpoint for vanilla translation
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
            // false indicates vanilla translation service
            System.out.println("Translation in progress...\n");
            String resultString = new TranslateSegment().executeCommandTranslate(inputString, sourceLang, targetLang, false);
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
    
 // endpoint for translation pre-process: tokenization
    @RequestMapping(value = "/e-smt/tokenize", method = {RequestMethod.POST, RequestMethod.GET})
    public ResponseEntity<String> tokenize(
            @RequestHeader(value = "Accept", required = false) String acceptHeader,
            @RequestHeader(value = "Content-Type", required = false) String contentTypeHeader,
            @RequestParam(value = "input", required = false) String input,
            @RequestParam(value = "lang", required = false) String iLang,
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

            // get shell script (with inputString, inputLang) and write result to resultString
            // true indicates tokenization and false indicates detokenization
            String resultString = new TranslateSegment().executeCommandTokenize(inputString, iLang, true);
          


            if (!model.getNsPrefixMap().containsValue(RDFConstants.itsrdfPrefix)) {
                model.setNsPrefix("itsrdf", RDFConstants.itsrdfPrefix);
            }

            Literal literal = model.createLiteral(resultString, iLang);
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
    
 // endpoint for translation post-process: detokenization
    @RequestMapping(value = "/e-smt/detokenize", method = {RequestMethod.POST, RequestMethod.GET})
    public ResponseEntity<String> detokenize(
            @RequestHeader(value = "Accept", required = false) String acceptHeader,
            @RequestHeader(value = "Content-Type", required = false) String contentTypeHeader,
            @RequestParam(value = "input", required = false) String input,
            @RequestParam(value = "lang", required = false) String iLang,
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

            // get shell script (with inputString, inputLang) and write result to resultString
            // true indicates tokenization and false indicates detokenization
            String resultString = new TranslateSegment().executeCommandTokenize(inputString, iLang, false);
          


            if (!model.getNsPrefixMap().containsValue(RDFConstants.itsrdfPrefix)) {
                model.setNsPrefix("itsrdf", RDFConstants.itsrdfPrefix);
            }

            Literal literal = model.createLiteral(resultString, iLang);
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
    
    
 // endpoint for translation post-process: recasing
    @RequestMapping(value = "/e-smt/recase", method = {RequestMethod.POST, RequestMethod.GET})
    public ResponseEntity<String> recase(
            @RequestHeader(value = "Accept", required = false) String acceptHeader,
            @RequestHeader(value = "Content-Type", required = false) String contentTypeHeader,
            @RequestParam(value = "input", required = false) String input,
            @RequestParam(value = "lang", required = false) String iLang,
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

            // get shell script (with inputString, inputLang) and write result to resultString
            // true indicates lowercasing and false indicates recasing
            String resultString = new TranslateSegment().executeCommandCase(inputString, iLang, false);
          


            if (!model.getNsPrefixMap().containsValue(RDFConstants.itsrdfPrefix)) {
                model.setNsPrefix("itsrdf", RDFConstants.itsrdfPrefix);
            }

            Literal literal = model.createLiteral(resultString, iLang);
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
    
 // endpoint for translation pre-process: lowercasing
    @RequestMapping(value = "/e-smt/lowercase", method = {RequestMethod.POST, RequestMethod.GET})
    public ResponseEntity<String> lccase(
            @RequestHeader(value = "Accept", required = false) String acceptHeader,
            @RequestHeader(value = "Content-Type", required = false) String contentTypeHeader,
            @RequestParam(value = "input", required = false) String input,
            @RequestParam(value = "lang", required = false) String iLang,
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

            // get shell script (with inputString, inputLang) and write result to resultString
            // true indicates lowercasing and false indicates recasing
            String resultString = new TranslateSegment().executeCommandCase(inputString, iLang, true);
          


            if (!model.getNsPrefixMap().containsValue(RDFConstants.itsrdfPrefix)) {
                model.setNsPrefix("itsrdf", RDFConstants.itsrdfPrefix);
            }

            Literal literal = model.createLiteral(resultString, iLang);
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
    
 // endpoint for translation pre-process: split document into sentences or segments
    @RequestMapping(value = "/e-smt/splitdoc", method = {RequestMethod.POST, RequestMethod.GET})
    public ResponseEntity<String> split(
            @RequestHeader(value = "Accept", required = false) String acceptHeader,
            @RequestHeader(value = "Content-Type", required = false) String contentTypeHeader,
            @RequestParam(value = "input", required = false) String input,
            @RequestParam(value = "lang", required = false) String iLang,
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

            // get shell script (with inputString, inputLang) and write result to resultString
            // true indicates lowercasing and false indicates recasing
            String resultString = new TranslateSegment().executeCommandSplit(inputString, iLang);
          


            if (!model.getNsPrefixMap().containsValue(RDFConstants.itsrdfPrefix)) {
                model.setNsPrefix("itsrdf", RDFConstants.itsrdfPrefix);
            }

            Literal literal = model.createLiteral(resultString, iLang);
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
    
    // endpoint for crosslingual projection, i.e. output a nif document aligning source words to target words
    @RequestMapping(value = "/e-smt/xlingual", method = {RequestMethod.POST, RequestMethod.GET})
    public ResponseEntity<String> smtalign(
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
            if (nifParameters.getInformat().equals(RDFConstants.RDFSerialization.PLAINTEXT)) {  //input is plaintext
                model = ModelFactory.createDefaultModel();
                rdfConversionService.plaintextToRDF(model, nifParameters.getInput(), null, nifParameterFactory.getDefaultPrefix());
            } else {   // input is NIF
                model = rdfConversionService.unserializeRDF(postBody, nifParameters.getInformat());
            }

            Statement firstPlaintext = rdfConversionService.extractFirstPlaintext(model);
            Resource subject = firstPlaintext.getSubject();
            String inputString = firstPlaintext.getObject().asLiteral().getString();

            // get shell script (with inputString, sourceLang, targetLang) and write result to resultString
            // true indicates getting phrase trace and alignment points eventually
            String resultString = new TranslateSegment().executeCommandTranslate(inputString, sourceLang, targetLang, true);
            
            // result contains phrase traces, we need source string, target string, alignment points
            XlingualProjection xling = new XlingualProjection();
            String source = new String(inputString);
            String target = xling.getTarget(resultString);
            //String alignPoints = xling.ExtractAlignments(source, target, resultString);


            if (!model.getNsPrefixMap().containsValue(RDFConstants.itsrdfPrefix)) {
                model.setNsPrefix("itsrdf", RDFConstants.itsrdfPrefix);
            }
            
            

            
            
            //model = getRdfConversionService().unserializeRDF(resultString, RDFSerialization.TURTLE);
            
            // Method 1: Preliminaries: context of the NIF document
            // add more prefixes to the model
            // Can perhaps move this to freme.common.RDFConstants
            model.setNsPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
            model.setNsPrefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
            model.setNsPrefix("nif-ann", "http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-annotation#");
            
            Literal literal = model.createLiteral(target, targetLang);
            subject.addLiteral(model.getProperty(RDFConstants.itsrdfPrefix + "target"), literal);
            
            // Method 2: Source Language Phrases
            String[] sourceWords = source.split(" ");
            int start =0;
            int end = 0;
            for(int i=0; i < sourceWords.length; i++){ // Traverse through each word
            	end += sourceWords[i].length();
            	//String uri = "http://dkt-project.eu/ns/#char=" + start + "," + end;
        		//Resource annotation= model.createResource(uri);
        		
        		//Property type = model.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
        		//annotation.addProperty(type,model.createResource(RDFConstants.nifPrefix + "Phrase"));
        		
        		model = NIFWriter.addAnnotationMTSource(model,start,end,sourceWords[i]);
        		
        		start += end;
            }
            
    			
            // Method 3: Target Language Phrases
            // Extract words of the source sentence
            
            
        

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


