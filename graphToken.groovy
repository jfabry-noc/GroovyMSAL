// https://mvnrepository.com/artifact/com.microsoft.azure/msal4j
@Grapes(
    @Grab(group='com.microsoft.azure', module='msal4j', version='1.8.1')
    )
// https://mvnrepository.com/artifact/com.nimbusds/oauth2-oidc-sdk
@Grapes(
    @Grab(group='com.nimbusds', module='oauth2-oidc-sdk', version='8.33', scope='runtime')
)
// https://mvnrepository.com/artifact/org.slf4j/slf4j-jdk14
@Grapes(
    @Grab(group='org.slf4j', module='slf4j-jdk14', version='1.7.30', scope='test')
)

import com.microsoft.aad.msal4j.ClientCredentialFactory;
import com.microsoft.aad.msal4j.ClientCredentialParameters;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.microsoft.aad.msal4j.IClientCredential;
import com.microsoft.aad.msal4j.MsalException;
import com.microsoft.aad.msal4j.SilentParameters;
import groovy.json.JsonSlurper
import java.util.concurrent.CompletableFuture


// Function to get an access token.
def GetAccessToken(String clientId, String authority, String secret, String scope) {
    // Create the app.

    ConfidentialClientApplication app = ConfidentialClientApplication.builder(clientId, ClientCredentialFactory.createFromSecret(secret)).authority(authority).build()
    ClientCredentialParameters clientCredentialParam = ClientCredentialParameters.builder(Collections.singleton(scope)).build()
    def result = app.acquireToken(clientCredentialParam).join()

    // Return to the caller.
    result
}

// Function to query the graph for users.
def GetUsersListFromGraph(accessToken) {
    try {
        // Define the connection.
        def conn = new URL("https://graph.microsoft.com/v1.0/users").openConnection()
        conn.setReadTimeout(5000)
        conn.setRequestMethod("GET")
        conn.setRequestProperty("Authorization", "Bearer " + accessToken)
        conn.setRequestProperty("Accept", "application/json")

        // Check the response.
        def responseCode = conn.getResponseCode()
        if ( responseCode == 200 ) {
            def responseText = conn.content.text
            println responseText
        } else {
            println "Error with the HTTP call! The response was ${responseCode}."
            System.exit(2)
        }
    } catch ( Exception e ) {
        println "Error making the connection. Error was: ${e}"
        System.exit(3)
    }
}

// Main code. Open the config file.
JsonSlurper slurper = new JsonSlurper()
def credPath = "./o365.json"
def aadConfig = [:]
try {
    aadConfig = slurper.parse(new File(credPath))
} catch ( Exception e ) {
    println "Borked opening the file at: ${credPath}."
    println "ERROR: ${e}"
    System.exit(1)
}

// Call for an access token.
def accessToken = GetAccessToken(aadConfig.client_id, aadConfig.authority, aadConfig.secret, aadConfig.scope)
def tokenValue = accessToken.accessToken()
def userList = GetUsersListFromGraph(tokenValue)
println userList

// Gracefully exit.
System.exit(0)
