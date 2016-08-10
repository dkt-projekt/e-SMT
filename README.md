# e-SMT
The e-SMT module performs Statistical Machine Translation (SMT) using DKT-Project-specific wrapper scripts built on top of Moses, an open-source SMT system written in C++ (http://www.statmt.org/moses/). Given a trained model, new input can be translated and displayed in NIF format.

Note, the current implementation translates in the following four directions: 
* German to English
* English to German
* Spanish to English
* English to Spanish

The system is constantly being updated and newer models as well as language pairs will be added (Arabic-English). 

## Endpoint

http://api.digitale-kuratierung.de/api/e-smt

### Input
The API conforms to the general NIF API specifications. For more details, see:
http://persistence.uni-leipzig.org/nlp2rdf/specification/api.html
In addition to the input and Content-type (header) parameters, the following parameters have to be set to perform Statistical Machine Translation on the input:  

`source-lang`: The language of the input text. 
  
`target-lang`: The language of the output text. 

For now, only the following language pairs are supported. English (`en`), German (`de`), and Spanish (`es`) 


### Output
A document in NIF format.

Example cURL post for e-smt:
`curl -X POST --header "Content-type:text/plain" -d 'Click on the right button.' "http://api.digitale-kuratierung.de/api/e-smt?source-lang=de&target-lang=en"`


## Notes on Moses Statistical Machine Translation Software
This end-point (hosted on the dkt-api server) relies on external software and assumes that Moses (the MT engine) is installed at: /usr/local/mt/bin, and the handler script (translate_main.sh) is placed at: /usr/local/mt

Moses and its dependencies can be locally installed, following the instructions at http://www.statmt.org/moses/?n=Development.GetStarted. Please contact Ankit Srivastava (firstName.lastName@dfki.de) for more information.
