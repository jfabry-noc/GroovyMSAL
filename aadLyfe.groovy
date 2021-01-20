// https://mvnrepository.com/artifact/com.microsoft.azure/msal4j
@Grapes(
    @Grab(group='com.microsoft.azure', module='msal4j', version='1.8.1')
    )
// https://mvnrepository.com/artifact/com.nimbusds/oauth2-oidc-sdk
@Grapes(
    @Grab(group='com.nimbusds', module='oauth2-oidc-sdk', version='8.33', scope='runtime')
)

import com.microsoft.aad.msal4j.ClientCredentialFactory
import com.microsoft.aad.msal4j.ClientCredentialParameters
import com.microsoft.aad.msal4j.ConfidentialClientApplication
import com.microsoft.aad.msal4j.IAuthenticationResult
import com.nimbusds.oauth2.sdk.http.HTTPResponse
import groovy.json.JsonSlurper
import java.util.concurrent.CompletableFuture


// Function to get an access token.
def GetAccessToken(String clientId, String authority, String secret, String scope) {
    // Create the app.
    ConfidentialClientApplication app = ConfidentialClientApplication.builder(clientId, ClientCredentialFactory.createFromSecret(secret)).authority(authority).build()
    ClientCredentialParameters clientCredentialParam = ClientCredentialParameters.builder(Collections.singleton(scope)).build()
    def future = app.acquireToken(clientCredentialParam)

    // Return to the caller.
    def result = future.get()
    result
}

// Function to query the graph for users.
def GetUsersListFromGraph(accessToken) {
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
}

// Call for an access token.
def accessToken = GetAccessToken(aadConfig.client_id, aadConfig.authority, aadConfig.secret, aadConfig.scope)
println accessToken.getClass().getName()
def userList = GetUsersListFromGraph(accessToken)
println userList
