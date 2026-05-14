package com.design.order_management_system.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DocumentationConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerToken";
    private static final String INSTRUCTIONS = """
            ### Getting Started & Authentication
            
            To access secured endpoints, follow these steps:
            1. Expand the **Authentication** section below.
            2. Click **Try it out** on the `/auth/login` endpoint.
            3. Use the pre-filled **Seeded User** request body and click **Execute**.
            4. Copy the returned `sToken` value in the response.
            5. Click the green **Authorize** button at the top of this page, paste the token, and click Authorize;
            
            You can access all the secured APIs using this token now.
            NOTE: All APIs require authentication except POST /auth/login.
            """;

    @Bean
    OpenAPI customOpenAPI() {
        var contactInfo = new Contact()
                .name("Devang Hemant Bhagwat")
                .email("dbhagwat512@gmail.com");
        var securityScheme = new SecurityScheme()
                .name(SECURITY_SCHEME_NAME)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT");

        var securityComponent = new Components()
                .addSecuritySchemes(SECURITY_SCHEME_NAME, securityScheme);
        var securityRequirement = new SecurityRequirement()
                .addList(SECURITY_SCHEME_NAME);
        var information = new Info()
                .title("Order Management System")
                .version("1.0")
                .description(INSTRUCTIONS)
                .contact(contactInfo);
        return new OpenAPI()
                .info(information)
                .components(securityComponent)
                .addSecurityItem(securityRequirement);
    }
}
