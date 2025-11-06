package com.amit.mymarket;

import org.springframework.boot.SpringApplication;

public class TestMymarketApplication {

	public static void main(String[] args) {
		SpringApplication.from(MyMarketApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
