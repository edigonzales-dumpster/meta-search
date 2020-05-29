package ch.so.agi.meta.server;

import java.io.IOException;

import javax.servlet.ServletException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import ch.so.agi.meta.shared.ApplicationConfig;
import ch.so.agi.meta.shared.ConfigResponse;
import ch.so.agi.meta.shared.ConfigService;

public class ConfigServiceImpl extends RemoteServiceServlet implements ConfigService {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ApplicationConfig config;

    @Override
    public void init() throws ServletException {
         super.init();
         SpringBeanAutowiringSupport.processInjectionBasedOnServletContext(this, getServletContext());
    }
    
    @Override
    public ConfigResponse configServer() throws IllegalArgumentException, IOException {
        ConfigResponse response = new ConfigResponse();
        response.setMyVar(config.getMyVar());
        return response;
    }

}
