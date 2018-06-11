package com.knoldus.hello.hello.impl;

import com.google.inject.AbstractModule;
import com.knoldus.hello.hello.api.ExternalService;
import com.lightbend.lagom.javadsl.server.ServiceGuiceSupport;
import com.knoldus.hello.hello.api.HelloService;

/**
 * The module that binds the HelloService so that it can be served.
 */
public class HelloModule extends AbstractModule implements ServiceGuiceSupport {
  @Override
  protected void configure() {
    bindService(HelloService.class, HelloServiceImpl.class);
    bindClient(ExternalService.class);
  }
}
