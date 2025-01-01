package com.famtree.famtree;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import javax.sql.DataSource;
import java.sql.Connection;

@SpringBootApplication
@RestController
public class FamtreeApplication {

	@Autowired
	private DataSource dataSource;

	public static void main(String[] args) {
		SpringApplication.run(FamtreeApplication.class, args);
	}

	@GetMapping("/test-db")
	public String testConnection() {
		try (Connection conn = dataSource.getConnection()) {
			return "Database connection successful! Connected to: " + 
				   conn.getMetaData().getURL();
		} catch (Exception e) {
			return "Database connection failed: " + e.getMessage();
		}
	}

}
