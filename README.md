# e-SMT
The e-SMT module performs Statistical Machine Translation (SMT) using DKT-Project-specific wrapper scripts built on top of Moses, an open-source SMT system written in C++ (http://www.statmt.org/moses/). Given a trained model, new input can be translated in NIF format.

Note, the current implementation translates from German (source-language: de) into English (target-language: en). The system is constantly being updated and newer models as well as language pairs will be added (Spanish-English and Arabic-English). 

## Endpoint

http://api.digitale-kuratierung.de/api/e-smt

### Input
The API conforms to the general NIF API specifications. For more details, see:
http://persistence.uni-leipzig.org/nlp2rdf/specification/api.html
In addition to the input and Content-type (header) parameters, the following parameters have to be set to perform Statistical Machine Translation on the input:  

`source-lang`: The language of the input text. For now, only German (`de`) is supported.  
  
`target-lang`: The language of the output text. For now, only English (`en`) is supported. 


### Output
A document in NIF format.

Example cURL post for e-smt:
`curl -X POST --header "Content-type:text/plain" "http://api.digitale-kuratierung.de/api/e-smt?source-lang=de&target-lang=en&input=hallo+welt"`
