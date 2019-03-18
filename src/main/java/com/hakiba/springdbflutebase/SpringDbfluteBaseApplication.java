package com.hakiba.springdbflutebase;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"org.docksidestage.dbflute"})
public class SpringDbfluteBaseApplication {

	public static void main(String[] args) {

		SpringApplication.run(SpringDbfluteBaseApplication.class, args);
	}

}
