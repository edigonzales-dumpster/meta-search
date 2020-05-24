package ch.so.agi.meta;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.ForwardedHeaderFilter;

import ch.so.agi.meta.server.ConfigServiceImpl;

@ServletComponentScan
@SpringBootApplication
@Configuration
public class BootGwtApplication {
	public static void main(String[] args) {
		SpringApplication.run(BootGwtApplication.class, args);
	}
	
    @Bean
    public ServletRegistrationBean configServletBean() {
        ServletRegistrationBean bean = new ServletRegistrationBean(new ConfigServiceImpl(), "/module1/config");
        bean.setLoadOnStartup(1);
        return bean;
    }
    
    @Bean
    public ForwardedHeaderFilter forwardedHeaderFilter() {
        return new ForwardedHeaderFilter();
    }
}
