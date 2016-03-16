package org.ega_archive.elixirbeacon.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DataUseDefinition {
  
    CONSENT_CODE("Consent Code", "0.1", "http://journals.plos.org/plosgenetics/article?id=10.1371/journal.pgen.1005772"), 
    ADAM ("ADA-M", "0.2", "http://p3g.org/sites/default/files/site/default/files/ADAM_introductiontext_21Jan2016.pdf");
    
    private String name;
    
    private String version;
    
    private String furtherDetails;

}
