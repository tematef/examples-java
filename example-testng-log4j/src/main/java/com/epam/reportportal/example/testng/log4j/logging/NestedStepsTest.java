package com.epam.reportportal.example.testng.log4j.logging;

import com.epam.reportportal.annotations.Step;
import org.testng.annotations.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NestedStepsTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(NestedStepsTest.class);

  @Test
  public void test() {
    step();
    LOGGER.info("hello from test");
  }

  @Step
  public void step() {
    LOGGER.info("step");
  }
}
