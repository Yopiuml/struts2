/*
 * Copyright (c) 2005 Opensymphony. All Rights Reserved.
 */
package org.apache.struts.action2.spring.lifecycle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * ApplicationContextListener that sets up the environment so that XWork and
 * Webwork can load data and information from Spring. Relies on Spring's
 * {@link org.springframework.web.context.ContextLoaderListener}having been
 * called first.
 *
 * @author sms
 * @deprecated Please configure struts.property to use {@link org.apache.struts.action2.spring.WebWorkSpringObjectFactory}
 */
public class SpringObjectFactoryListener implements ServletContextListener {
    private static final Log log = LogFactory.getLog(SpringObjectFactoryListener.class);

    public void contextInitialized(ServletContextEvent event) {
        log.fatal("SpringObjectFactoryListener is deprecated and no longer does anything - you should remove it from web.xml\n" +
                "Please set struts.objectFactory = spring to enable Spring-WebWork integration.");
    }

    public void contextDestroyed(ServletContextEvent arg0) {
        // Nothing to do.
    }
}
