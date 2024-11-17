# Contract Testing with Pact, Kafka, avro and Spring Boot'

## The Problem
From a testing perspective, we can write some API integration tests, run them against the test environment, and then verify if we are receiving the correct data from the provider.

### Mock Services — Testing in isolation without invoking the real service:
- It doesn’t give us confidence that the mock provider accurately represents the real provider.
- The same applies to the consumer. Additionally, it doesn’t prevent broken changes from being deployed in a dedicated environment, as the provider or the consumer can deploy broken changes to production, especially if they are on different pipelines and don’t regularly trigger each other’s tests.


### Here we will summarize the testing types performed for microservices testing and its challenges
- **Unit Testing** — Test with mock services/data
  - Lacks confidence for release.
  - Mock providers may not accurately represent the real provider and the same holds for consumers.
  - Broken changes can be deployed, especially if on a different pipeline.
- **Integration Testing** - Tests actual endpoints
  - When a consumer makes a request, instead of communicating with a mock provider, it talks to the real provider.
  - However, having too many of these tests becomes complex in microservices.
  - They are slow and brittle, particularly when dealing with frequent data changes.
  - Moreover, they require dedicated environments where services integrate, leading to extensive test maintenance.
  - Additionally, they don’t prevent the deployment of broken changes.

> **Q: Why didn’t unit test cases catch the failure?** <br/>
> A: We mocked the response of the external microservice.
> 
> **Q: Why did you mock the external dependency?** <br/>
> A: Unit tests should be conducted in an isolated manner with stubbing/mocking dependencies.
> 
> **Q: Why did you have the wrong mock response?** <br/>
> A: Below is the contract we agreed upon while consuming the response from the All Courses microservice. So, we are using this mocked contract to test our code.
> 
> **Q: What was the reason for the exact failure?** <br/>
> A: We are parsing JSON and grabbing the field to sum up all the courses. The provider microservice has changed the contract and is now sending the field name as “pricing.” Our code base cannot identify the “pricing” field, and it failed to parse the JSON. Unit tests did not catch this because we are using a mock response that has the “price” field. According to that mock response, the test passed. However, when QA tested it with the real service in their end-to-end or integration test, it broke.
> 
> **Q: Why did you not update your change to the consumer?** <br/>
> A: Many teams are consuming this service, and we are not sure which team is using what field. So, we just updated our documentation for the change we made, and they must check our documentation every time.
> 
> **Q: How one can monitor these contract changes every time, if the provider doesn’t reach us for the exact changes made?** <br/>
> A: Why worry? A team will however catch the issue on their e2E testing. and we can fix it
> 

> **Q: Is there any tool, where can define our expectations on what fields in the JSON contract we are using** <br>
> The provider-side test cases will break where they try to make some changes that are in contract with other systems


## Consumer Driven Contract Testing: <br/>why pact contract testing is created on consumer side?
Pact contract testing is created on the **consumer side** because of the **consumer-driven contract** approach. Here's why this design choice is important:
1. **Consumer Expectations First:** The consumer defines the expectations for how the service (provider) should behave. Since the consumer relies on the provider’s API or service, it’s essential to know exactly what format and behavior to expect. By defining the contract on the consumer side, it reflects the actual needs of the consumer, ensuring that the provider's implementation will meet those needs.
2. **Simplifies Consumer Testing:** When consumers define the contract, they can write tests that validate their expected behavior from the provider without waiting for the provider to be ready. The consumer can mock the provider’s behavior based on the contract, enabling early testing of integration even before the provider's service is fully implemented.
3. **Reduces Provider’s Burden:** The provider doesn’t need to know about all possible consumers beforehand. It only needs to verify that it can fulfill the contract defined by the consumer. This reduces the need for the provider to manage tests for every possible consumer.
4. **Encourages Decoupling:** This approach helps decouple the development process. Consumers can evolve their requirements independently, and the provider can implement changes as long as they still honor the contract. This flexibility allows for parallel development of services with minimal coordination.
5. **Improved Collaboration:** By focusing on the consumer’s needs, Pact contract testing fosters a collaboration where the provider and consumer can align on expectations. It ensures that the contract reflects real use cases and can adapt to evolving consumer requirements.

<hr/>

# Implementation Example
<img src="https://miro.medium.com/v2/resize:fit:1400/format:webp/1*LFB-YfrYr8ZwgFbFHBq3PA.gif"/>

## Prerequisites:
- Java 17+
- Spring Boot 3.x
- Maven (or Gradle)
- Pact-JVM library: PACT JVM Junit 5 for [Consumer ](https://mvnrepository.com/artifact/au.com.dius.pact.consumer/junit5) | PACT JVM Junit 5 for [producer](https://mvnrepository.com/artifact/au.com.dius.pact.provider/junit5)
- Optional: [Pact Broker Configuration]()

````css
provider-service/
├── src/main/java/com/example/provider/
│   ├── ProviderApplication.java
│   └── controller/
│       └── DataController.java
├── src/test/java/com/example/provider/
│   └── ProviderPactTest.java
├── pom.xml

````

## Step1. Setup Dependencies (Maven)

### [Pact Consumer Dependency:](https://mvnrepository.com/artifact/au.com.dius.pact.consumer/junit5)
````xml
    <!-- Pact Consumer Dependency -->
    <dependency>
      <groupId>au.com.dius.pact.consumer</groupId>
      <artifactId>junit5</artifactId>
      <version>4.6.5</version>
      <scope>test</scope>
    </dependency>

    <!--Optional: Pact Maven Plugin for Pact Provider Verification-->
    <plugin>
      <groupId>au.com.dius.pact.provider</groupId>
      <artifactId>maven</artifactId>
      <version>4.1.11</version>
      <configuration>
        <pactBrokerUrl>https://localhost:9292</pactBrokerUrl>
        <!--<pactBrokerUrl>https://arpangroup.pactflow.io/</pactBrokerUrl>
        <pactBrokerToken>0CSDSGVBGWMSDSDBCG</pactBrokerToken>-->
      </configuration>
    </plugin>
````
**pactBrokerUrl:** By specifying the pactBrokerUrl, this plugin connects to the Pact Broker to fetch the Pact files and verifies the provider against those contracts.

**Typical Workflow with This Plugin:**
1. **Consumer publishes a Pact:** The consumer (client) generates a Pact file that defines the expected interactions with the provider (API requests/responses).
2. **Pact Broker:** The Pact file is pushed to a Pact Broker (specified by pactBrokerUrl).
3. **Provider verification:** The provider uses the Pact Maven plugin to download the Pact files from the Pact Broker, and the plugin will verify that the provider’s API matches the interactions defined in the Pact file.



### [Pact Provider Dependency:](https://mvnrepository.com/artifact/au.com.dius.pact.provider/junit5)
````xml
<!-- Pact Provider Dependency -->
<dependency>
  <groupId>au.com.dius.pact.provider</groupId>
  <artifactId>junit5</artifactId>
  <version>4.6.5</version>
  <scope>test</scope>
</dependency>
````


## Step2. Consumer Test
Now, Let's Create a consumer test using Pact to create a contract JSON file.

````java
@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "student-provider")
//@PactTestFor(providerName = "student-provider", providerType = ProviderType.ASYNCH, pactVersion = PactSpecVersion.V4, port = 8081)
class ConsumerPactTest {

    @Pact(consumer = "ConsumerService")
    public V4Pact createStudentDetailsPact(PactDslWithProvider builder) throws Exception{
        // Define the expected response body using PactDslJsonBody
        /*PactDslJsonBody jsonBody = new PactDslJsonBody()
            .stringType("studentName", "John Doe")
            .stringType("studentId", "S12345")
            .integerType("age", 20);*/
        
        // or, define the expected response from a json file (suitable for big json response)
        File file = ResourceUtils.getFile("src/test/resources/UserResponse200.json");
        String jsonBody = new String(Files.readAllBytes(file.toPath()));       
        
        // Build the Pact interaction
        return builder
              .given("GET /api/getUserDetails")
                .uponReceiving("A GET request for user details")
                .method("GET")
                //.headers("Accept", "application/json")
                //.headers(Map.of("Content-Type", "application/json"))
                .path("/api/getUserDetails")
              .willRespondWith()
                .status(200)
                //.body(LambdaDsl.newJsonBody((body) -> body.stringType("name", "Sample Data")).build())
                //.body(jsonBody)
                .body(jsonBody, "application/json")
                .toPact(V4Pact.class);
    }

    @Test
    @PactTestFor(providerName = "createStudentDetailsPact")
    void testStudentDetailsPact(MockServer mockServer) throws Exception {
        // Step1: define a expectedJson in string format
        // String expectedJson = "{\"studentId\":\"S123\", \"studentName\":\"tom\", \"age\":30}";
      
        // Step1.1: or define expectedJson like:
        StudentResponse expectedStudent = new StudentResponse("S123", "tom", 30);
      
        
        // Step2: define the actualJson response using an external endpoint
        /*RestTemplate restTemplate = new RestTemplate();        
        ResponseEntity<String> actualJson = restTemplate.getForEntity("http://localhost:8080/api/data", String.class);*/

        // Step2.1: or get response by calling the controller from the same application (when the provider logic is written on same application)
        /*studentController.setBaseUrl(mockServer.getUrl() + "/api/getStudentDetails");
        StudentResponse studentResponse = studentController.getStudentDetails("123");
        String actualJson = new ObjectMapper().writeValueAsString(studentResponse);*/

        // Step2.2: or get response by calling the controller without Autowiring the Controller class
        ResponseEntity<StudentResponse> studentResponse = restTemplate.getForEntity(mockServer.getUrl() + "/api/getUserDetails", StudentResponse.class);
        StudentResponse expectedStudent = studentResponse.getBody();              
      
        
        // Step3: Validate the response
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).contains("Sample Data");
    }
}
````

### Explanation:
1. **@ExtendWith:** ExtendWith is JUnit 5 annotation that is used to register extensions in JUnit tests. It facilitates the Pact features to the test class file to perform the contract testing
2. **@PactTestFor:** annotation is used to specify the provider against which the interactions defined in the test should be verified. The name below “student-provider” has to be mentioned the same as Provider and this binds the Provider and Consumer
   1. **providerType = ProviderType.ASYNCH:**
   2. **pactVersion = PactSpecVersion.V4:**
   3. **port = 8081:** Ensure that the provider service is running on the correct port (80801in your case).
3. **V4Pact:** The createPact method returns `V4Pact` (others are `RequestResponsePact`, `RequestResponsePact`, `MessagePact`) as we defined `pactVersion = V4` 
4. **.toPact(V4Pact.class):** This explicitly creates a `V4Pact` instance.
5. **@Pact:** annotation is used to mark the method that generates a Pact between the consumer and provider
6. **PactDslWithProvider:** object is automatically injected by Pact and it provides a fluent DSL for constructing a Pact between the consumer and provider. 
   1. Other available Builders are: [PactBuilder](), [MessagePactBuilder]() <br/><br/>
7. **PactTestFor:** It indicates that this test is associated with the interactions defined in the above interactions (or Mock Response)
8. **MockServer:** object is automatically injected by Pact and it provides the URL where the mock server is running
   1. **setBaseURL:** Override the host URL so that the API request is redirected to the Pact mock server
9. ssssas
10. sasasa




### Here is an example of create pact using `PactBuilder`
````java
@Pact(provider = "ProviderService", consumer = "ConsumerService")
public V4Pact createPact(PactBuilder builder) {
    return builder
        .usingLegacyMessageDsl(false)
        .expectsToReceive("GET REQUEST")
        .withRequest(request -> request
                .path("/api/pact")
                .method("GET"))
        .willRespondWith(response -> response
                .status(200)
                .headers(Map.of("Content-Type", "application/json"))
                .body(new PactDslJsonBody()
                        .booleanType("condition", true)
                        .stringType("name", "tom")))
        .toPact();
  }
````

### Here is an example of create pact using `MessagePactBuilder`
````java
@Pact(consumer = "student-consumer")
public MessagePact studentDetailsPact(MessagePactBuilder builder) throws Exception {
  File file = ResourceUtils.getFile("src/test/resources/StudentResponse200.json");
  String content = new String(Files.readAllBytes(file.toPath()));
  
  return builder
          .expectsToReceive("a student contract")
          //.withMetadata(Map.of(JSON_CONTENT_TYPE, KEY_CONTENT_TYPE))
          //.withContent(jsonBody)
          .withContent(content, "application/json")
          .toPact();
}
````











