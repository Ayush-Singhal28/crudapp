package com.knoldus.hello.hello.api;

import com.lightbend.lagom.serialization.Jsonable;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class Details implements Jsonable {
    private static final long serialVersionUID = 1L;
    String firstName;
    String lastName;
    String dateOfBirth;
}
