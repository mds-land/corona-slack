package com.github.mdsina.corona;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;

@Controller("/corona")
public class SlackAppController {


    @Post(value = "/stats", consumes = {MediaType.APPLICATION_FORM_URLENCODED, MediaType.APPLICATION_JSON})
    public String dispatch(HttpRequest<String> request, @Body String body) throws Exception {
        return "Hello";
    }

}
