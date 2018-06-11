package com.knoldus.hello.hello.api;

import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.transport.Method;
import static com.lightbend.lagom.javadsl.api.Service.restCall;
import static com.lightbend.lagom.javadsl.api.Service.named;



public interface ExternalService  extends Service {

    ServiceCall<NotUsed, ExternalResponse> getExposedUrl();

    @Override
    default Descriptor descriptor() {
        // @formatter:off
        return named("external").withCalls(
                restCall(Method.GET, "/api/users/2", this::getExposedUrl)
        ).withAutoAcl(true);
       // @formatter:on

    }
}
