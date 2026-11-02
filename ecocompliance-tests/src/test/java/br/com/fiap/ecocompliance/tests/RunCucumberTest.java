package br.com.fiap.ecocompliance.tests;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;

@Suite
@IncludeEngines("cucumber")
@SelectPackages("features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "br.com.fiap.ecocompliance.tests.steps")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty, summary, "
        + "html:target/cucumber-reports/cucumber.html, "
        + "json:target/cucumber-reports/cucumber.json, "
        + "junit:target/cucumber-reports/cucumber-junit.xml")
public class RunCucumberTest {
}
