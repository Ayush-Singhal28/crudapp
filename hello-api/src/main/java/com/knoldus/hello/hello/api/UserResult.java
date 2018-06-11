package com.knoldus.hello.hello.api;

import com.lightbend.lagom.serialization.Jsonable;
import lombok.Builder;
import lombok.Setter;

@Builder
@Setter
public class UserResult implements Jsonable {
    String firstName ;
   int loyaltyNumber;
}
