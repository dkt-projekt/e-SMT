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
import de.dkt.common.niftools.NIFReader;

import java.util.ArrayList;
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
            // Create a NIF model from the input parameters if the input is not already a NIF model
        	if (nifParameters.getInformat().equals(RDFConstants.RDFSerialization.PLAINTEXT)) {  //input is plaintext
                model = ModelFactory.createDefaultModel();
                rdfConversionService.plaintextToRDF(model, nifParameters.getInput(), null, nifParameterFactory.getDefaultPrefix());
            } else {   // input is NIF
                model = rdfConversionService.unserializeRDF(postBody, nifParameters.getInformat());
            }

            // Extract the input sentence to be translated from the NIF model
        	Statement firstPlaintext = rdfConversionService.extractFirstPlaintext(model);
            Resource subject = firstPlaintext.getSubject();
            String inputString = firstPlaintext.getObject().asLiteral().getString();

            // get shell script (with inputString, sourceLang, targetLang) and write result to resultString
            // true indicates getting phrase trace and alignment points
            String resultString = new TranslateSegment().executeCommandTranslate(inputString, sourceLang, targetLang, true);
            
            
            // result contains phrase traces, we need source string, target string, alignment points
            XlingualProjection xling = new XlingualProjection();
            // Temporary Solution: The source in phrase trace is tokenised (and lower-cased), 
            // so we will tokenize it to ensure the alignment points references do not mismatch
            String source = new String(new TranslateSegment().executeCommandTokenize(inputString, sourceLang, true));
            String target = xling.getTarget(resultString);
            ArrayList<String> alignPoints = xling.ExtractAlignments(source, target, resultString);
            
            // We will create a new model after nullifying the previous model,
            // because we want to replace the source string (main Resource) with the tokenised version that was used to obtain alignment points
            model = null;
            model = ModelFactory.createDefaultModel();
            rdfConversionService.plaintextToRDF(model, source, null, nifParameterFactory.getDefaultPrefix());
            //NIFWriter.addInitialString(model, source, NIFReader.extractDocumentWholeURI(model));


            if (!model.getNsPrefixMap().containsValue(RDFConstants.itsrdfPrefix)) {
                model.setNsPrefix("itsrdf", RDFConstants.itsrdfPrefix);
            }
            
            

            
            
            //model = getRdfConversionService().unserializeRDF(resultString, RDFSerialization.TURTLE);
            
            // Method 1: Preliminaries: context of the NIF document (including whole sentence translation)
            // add more prefixes to the model
            // Can perhaps move this to freme.common.RDFConstants
            model.setNsPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
            model.setNsPrefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
            model.setNsPrefix("nif-ann", "http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-annotation#");
            
            Statement fpt = rdfConversionService.extractFirstPlaintext(model);
            Resource sub = fpt.getSubject();
            Literal literal = model.createLiteral(target, targetLang);
            sub.addLiteral(model.getProperty(RDFConstants.itsrdfPrefix + "target"), literal);
            
            // Method 2: Source Language Phrases
           // String documentURI = NIFReader.extractDocumentWholeURI(model);
            Resource docResource = NIFReader.extractDocumentResourceURI(model);
            
            //String[] sourceWords = source.split(" ");
            String[] sourcePhrases = xling.getSrcPhrases(alignPoints);
            int start =0;
            int end = 0;
            String annUnit = "_:annotationUnit";
            String annIndex;
            int num;
            
            for(int i=0; i < sourcePhrases.length; i++){ // Traverse through each phrase
            	end = start + sourcePhrases[i].length();
            	//String uri = "http://dkt-project.eu/ns/#char=" + start + "," + end;
        		//Resource annotation= model.createResource(uri);
        		
        		//Property type = model.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
        		//annotation.addProperty(type,model.createResource(RDFConstants.nifPrefix + "Phrase"));
        		
            	num = i+1;
            	annIndex = new String(annUnit + num);
        		NIFWriter.addAnnotationMTSource(model,start,end,sourcePhrases[i],docResource,annIndex);
        		
        		start = end + 1;
            }
            
    			
            // Method 3: Target Language Phrases
            // Adding target language annotations using the AnonID from Jena Model to point to a blank node resource
            String[] targetPhrases = xling.getTrgPhrases(alignPoints);
            String annotated;
            for (int i=1; i<=targetPhrases.length; i++){ // Traverse through each target / annotation unit
            	annotated = new String("_:annotationUnit" + i);
            	NIFWriter.addAnnotationMTTarget(model, targetPhrases[i-1], targetLang, docResource, annotated);
            }
            
            
        

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


