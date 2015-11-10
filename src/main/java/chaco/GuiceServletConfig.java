package chaco;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;

import chaco.config.Config;
import chaco.module.BasicModule;
import chaco.module.WebModule;
import chaco.module.WebRequestScopedModule;

public class GuiceServletConfig extends GuiceServletContextListener {

	private ServletContext servletContext;

	@Override
	public void contextInitialized(ServletContextEvent servletContextEvent) {
		servletContext = servletContextEvent.getServletContext();
		super.contextInitialized(servletContextEvent);
	}

	@Override
	protected Injector getInjector() {
		return Guice.createInjector(
			buildBasicModule(),
			new WebModule(),
			new WebRequestScopedModule(),
			new ServletModule() {
				@Override
				protected void configureServlets() {
					serve("/img/*", "/css/*", "/js/*").with(DefaultServlet.class);
					serve("/*").with(Servlet.class);
				}
			});
	}

	private BasicModule buildBasicModule() {
		Object config = servletContext.getAttribute("chaco.config");
		if (config != null && config instanceof Config) {
			return new BasicModule((Config)config);
		} else {
			return new BasicModule();
		}
	}
}
