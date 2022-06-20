// package joberstein.portfolio;

// import com.fasterxml.jackson.databind.ObjectMapper;
// import com.fasterxml.jackson.databind.SerializationFeature;

// import joberstein.portfolio.apiGateway.MessagesApi;
// import software.amazon.awscdk.core.App;

// public class TemplateBuilderTest {
    
//     private final static ObjectMapper JSON =
//         new ObjectMapper().configure(SerializationFeature.INDENT_OUTPUT, true);

//     @Test
//     public void testStack() throws IOException {
//         App app = new App();
//         var stack = new MessagesApi(app, "test");

//         // synthesize the stack to a CloudFormation template and compare against
//         // a checked-in JSON file.
//         JsonNode actual = JSON.valueToTree(app.synth().getStackArtifact(stack.getArtifactId()).getTemplate());

//         assertThat(new ObjectMapper().createObjectNode()).isEqualTo(actual);
// }
