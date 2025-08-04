package com.myorg;

import software.constructs.Construct;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import com.google.gson.Gson;

import software.amazon.awscdk.CfnOutput;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.Fn;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
//import software.amazon.awscdk.aws_apigatewayv2_authorizers.HttpLambdaAuthorizer;
import software.amazon.awscdk.services.s3.Bucket;

import software.amazon.awscdk.services.apigateway.*;

import software.amazon.awscdk.services.apigateway.RequestAuthorizer;

import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.logs.LogGroup;

public class ZtestStack extends Stack {
	
    public ZtestStack(final Construct scope, final String id) {
        this(scope, id, null);
    }
    
    private JsonObject read_swagger() throws Exception {
    	
        Yaml yaml = new Yaml();
        
        try (FileInputStream inputStream = new FileInputStream("src/main/resources/swagger.yaml")) {
            Map<String, Object> data = yaml.load(inputStream);
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonString = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(data);
            
            return new Gson().fromJson(jsonString, JsonObject.class);
        }
    }
    
    public ZtestStack(final Construct scope, final String id, final StackProps props) {
    	
    	
        super(scope, id, props);

        // Create an S3 bucket
        {
        	Bucket bucket = Bucket.Builder.create(this, "ZZZZZZ202503152319").versioned(true).removalPolicy(software.amazon.awscdk.RemovalPolicy.DESTROY).build();
        	
        	

			CfnOutput.Builder.create(this, "BucketNameOutput")
			    .description("The name of the S3 bucket")
			    .value(bucket.getBucketName())
			    .exportName("MyBucketName")
			    .build();
			
			try {
				
				String bucketName = Fn.importValue("MyBucketName");
				
				System.out.println("Bucket name: " + bucketName);
				
        		Files.writeString(Path.of("BucketName.txt"), bucketName);
        		
        	} catch (Exception ex) {
        		ex.printStackTrace();
        	}
        	
        	//System.out.println("bucket.getBucketName(): " + bucket.getBucketName());
        }
        
		/*// lambda and api
		{
			
			// create a function
	        Function lambdaAuthorizer = Function.Builder.create(this, "LambdaAuthorizer").runtime(software.amazon.awscdk.services.lambda.Runtime.NODEJS_22_X).handler("index.handler").code(Code.fromAsset("lambda")).memorySize(128).timeout(Duration.seconds(10)).build();
	        System.out.println("lambdaAuthorizer.getFunctionArn(): " + lambdaAuthorizer.getFunctionArn());
	        
	        // create an authorizer
	        software.amazon.awscdk.services.apigateway.RequestAuthorizer.Builder requestAuthorizerBuilder = RequestAuthorizer.Builder.create(this, "LambdaAuth");
	        requestAuthorizerBuilder.authorizerName("LambdaCustomAuthorizer");
	        requestAuthorizerBuilder.handler(lambdaAuthorizer);
	        List<String> listAuthSrc = new ArrayList<String>();
	        listAuthSrc.add(IdentitySource.header("Authorization"));
	        requestAuthorizerBuilder.identitySources(listAuthSrc);
	        RequestAuthorizer requestAuthorizer = requestAuthorizerBuilder.build();
	        System.out.println("requestAuthorizer.getAuthorizerArn(): " + requestAuthorizer.getAuthorizerArn());
	        
	        // create an api builder
	        software.amazon.awscdk.services.apigateway.RestApi.Builder restApiBuilder = RestApi.Builder.create(this, "ExternalHttpApi");
	        restApiBuilder.restApiName("ExternalHttpService");
	        restApiBuilder.description("API Gateway that integrates with an external HTTP endpoint");
	        restApiBuilder.apiKeySourceType(software.amazon.awscdk.services.apigateway.ApiKeySourceType.AUTHORIZER);
	        
	        // create a stage with logging enabled
	        software.amazon.awscdk.services.apigateway.StageOptions.Builder stageOptionsBuilder = software.amazon.awscdk.services.apigateway.StageOptions.builder();
	        stageOptionsBuilder.stageName("poc");
	        stageOptionsBuilder.loggingLevel(MethodLoggingLevel.INFO);
	        stageOptionsBuilder.dataTraceEnabled(true);
	        stageOptionsBuilder.throttlingRateLimit(250);
	        stageOptionsBuilder.throttlingBurstLimit(250);
	        stageOptionsBuilder.accessLogDestination(new LogGroupLogDestination(LogGroup.Builder.create(this, "ApiGatewayLogGroup").logGroupName("lg-my-api-cdk").removalPolicy(RemovalPolicy.DESTROY).build()));
	        stageOptionsBuilder.accessLogFormat(AccessLogFormat.custom("{\"api_id\": \"$context.apiId\", \"authenticate_error\": \"$context.authenticate.error\", \"authenticate_latency\": \"$context.authenticate.latency\", \"authenticate_status\": \"$context.authenticate.status\", \"authorize_error\": \"$context.authorize.error\", \"authorize_latency\": \"$context.authorize.latency\", \"authorize_status\": \"$context.authorize.status\", \"authorizer_error\": \"$context.authorizer.error\", \"authorizer_integrationLatency\": \"$context.authorizer.integrationLatency\", \"authorizer_integrationStatus\": \"$context.authorizer.integrationStatus\", \"authorizer_latency\": \"$context.authorizer.latency\", \"authorizer_requestId\": \"$context.authorizer.requestId\", \"authorizer_status\": \"$context.authorizer.status\", \"context_authorizer_principalId\": \"$context.authorizer.principalId\", \"extendedRequestId\": \"$context.extendedRequestId\", \"httpMethod\": \"$context.httpMethod\", \"integration_error\": \"$context.integration.error\", \"integration_integrationStatus\": \"$context.integration.integrationStatus\", \"integration_latency\": \"$context.integration.latency\", \"integration_requestId\": \"$context.integration.requestId\", \"integration_status\": \"$context.integration.status\", \"integrationLatency\": \"$context.integrationLatency\", \"integrationStatus\": \"$context.integrationStatus\", \"ip\": \"$context.identity.sourceIp\", \"protocol\": \"$context.protocol\", \"requestId\": \"$context.requestId\", \"requestTime\": \"$context.requestTime\", \"resource_id\": \"$context.resourceId\", \"resourcePath\": \"$context.resourcePath\", \"responseLatency\": \"$context.responseLatency\", \"responseLength\": \"$context.responseLength\", \"stage\": \"$context.stage\", \"status\": \"$context.status\", \"user_agent\": \"$context.identity.userAgent\"}"));
	        
	        // assign stage to api
	        StageOptions stageOptions = stageOptionsBuilder.build();
	        restApiBuilder.deployOptions(stageOptions);
	        
	        // create an api
			RestApi api = restApiBuilder.build();
			String externalHttpEndpoint = "https://squid-app-o8e56.ondigitalocean.app/echo";
			HttpIntegration httpIntegration = new HttpIntegration(externalHttpEndpoint, null);
			api.getRoot().addResource("external").addMethod("GET", httpIntegration);
			api.getRoot().addResource("external-with-auth").addMethod("GET", httpIntegration, MethodOptions.builder().authorizationType(AuthorizationType.CUSTOM).authorizer(requestAuthorizer).apiKeyRequired(true).build());
			System.out.println("api.getUrl(): " + api.getUrl());
			
			// print stage URL
			CfnOutput.Builder.create(this, "ApiUrl").value(api.getUrl() + stageOptions.getStageName()).description("The URL of the API in the " + stageOptions.getStageName() + " stage").build();
			
			// create sandbox stage for the api
			//Stage stage = Stage.Builder.create(this, "stage-sandbox").stageName("sandbox").deployment(software.amazon.awscdk.services.apigateway.Deployment.Builder.create(this, "MyApiDeployment").api(api).build()).build();
			
	        // Optionally, you can output the URL for the API in the new stage
	        //CfnOutput.Builder.create(this, "ApiUrl").value(api.getUrl() + stage.getStageName()).description("The URL of the API in the dev stage").build();
		}*/
		
		/*// api gateway from swagger
		{
			RestApi api = RestApi.Builder.create(this, "MySwaggerApi")
			.restApiName("MySwaggerApi")
			.description("API created from Swagger definition")
			.swagger(SwaggerDefinition.fromAsset("src/main/resources/swagger.yaml")) // Specify the path to the Swagger file
			.build();
		}*/
    }
}
