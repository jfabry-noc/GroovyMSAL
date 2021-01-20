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

import com.microsoft.aad.msal4j.ClientCredentialFactory
import com.microsoft.aad.msal4j.ClientCredentialParameters
import com.microsoft.aad.msal4j.ConfidentialClientApplication
import com.microsoft.aad.msal4j.IAuthenticationResult
import groovy.json.JsonSlurper
import java.util.concurrent.CompletableFuture


private static IAuthenticationResult getAccessTokenByClientCredentialGrant(String clientId, String authority, String secret, String scope) throws Exception {
    ConfidentialClientApplication app = ConfidentialClientApplication.builder(
            clientId,
            ClientCredentialFactory.createFromSecret(secret))
            .authority(authority)
            .build();

    // With client credentials flows the scope is ALWAYS of the shape "resource/.default", as the
    // application permissions need to be set statically (in the portal), and then granted by a tenant administrator
    ClientCredentialParameters clientCredentialParam = ClientCredentialParameters.builder(
            Collections.singleton(scope))
            .build();

    CompletableFuture<IAuthenticationResult> future = app.acquireToken(clientCredentialParam);
    return future.get();
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
//def accessToken = GetAccessToken(aadConfig.client_id, aadConfig.authority, aadConfig.secret, aadConfig.scope)
def accessToken = getAccessTokenByClientCredentialGrant(aadConfig.client_id, aadConfig.authority, aadConfig.secret, aadConfig.scope)
println accessToken
def userList = GetUsersListFromGraph(accessToken)
println userList
