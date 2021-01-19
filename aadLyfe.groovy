// https://mvnrepository.com/artifact/com.microsoft.azure/msal4j
@Grapes(
    @Grab(group='com.microsoft.azure', module='msal4j', version='1.8.1')
    )

import com.microsoft.aad.msal4j.ClientCredentialFactory;
import com.microsoft.aad.msal4j.ClientCredentialParameters;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import groovy.json.JsonSlurper

// Main code. Open the config file.
JsonSlurper slurper = new JsonSlurper()
def credPath = "./o365.json"
def aadConfig = [:]

try {
    aadConfig = slurper.parse(new File(credPath))
} catch ( Exception e ) {
    println "Borked opening the file at: ${credPath}."
    println "ERROR: ${e}"
}
println aadConfig

