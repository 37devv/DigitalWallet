package com.oepfelbaum.digitalwalletbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class DigitalWalletBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(DigitalWalletBackendApplication.class, args);
	}

}
