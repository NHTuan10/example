package com.example.vtcommon.vtweb;

import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import static io.gatling.javaapi.core.CoreDsl.constantUsersPerSec;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.http.HttpDsl.http;

public class VtSimulation extends Simulation {
    HttpProtocolBuilder httpProtocol =
            http.baseUrl("http://localhost:8080")
                    // set the "accept" header to a value suited for the expected response
                    .acceptHeader("text/html");

    ScenarioBuilder myScenario = scenario("My Scenario")
            .exec(
                    http("Request 1").get("/thread/name")
            );

    // Add the setUp block:
    {
        setUp(
                myScenario.injectOpen(constantUsersPerSec(2).during(10))
        ).protocols(httpProtocol);
    }
}