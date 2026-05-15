package com.example.ai_engineering_enablement_portal;

import org.springframework.boot.SpringApplication;

public class TestAiEngineeringEnablementPortalApplication {

	public static void main(String[] args) {
		SpringApplication.from(AiEngineeringEnablementPortalApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
