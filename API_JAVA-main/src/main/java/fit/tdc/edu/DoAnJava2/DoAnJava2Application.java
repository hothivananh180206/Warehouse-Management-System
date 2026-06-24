package fit.tdc.edu.DoAnJava2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class DoAnJava2Application {

	public static void main(String[] args) {
		SpringApplication.run(DoAnJava2Application.class, args);
	}

}
