package com.transit.ticketsales.domain;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectFile;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;

/**
 * Runs the agreed examples exactly as the domain expert agreed on them.
 * No copy of the examples in Java: the .feature file next to the glossary is the test.
 */
@Suite
@IncludeEngines("cucumber")
@SelectFile("contexts/ticket-sales/scenarios/refund.feature")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "com.transit.ticketsales.domain")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty")
class RefundScenarioTest {
}
