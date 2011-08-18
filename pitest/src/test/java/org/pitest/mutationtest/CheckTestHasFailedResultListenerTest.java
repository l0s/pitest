/*
 * Copyright 2011 Henry Coles
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.pitest.mutationtest;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.pitest.Description;
import org.pitest.DescriptionMother;
import org.pitest.TestResult;
import org.pitest.TimeoutException;
import org.pitest.functional.Option;
import org.pitest.mutationtest.results.DetectionStatus;


public class CheckTestHasFailedResultListenerTest {

  private CheckTestHasFailedResultListener testee;

  private Description description;

  @Before
  public void setUp() {
    this.testee = new CheckTestHasFailedResultListener();
    this.description = DescriptionMother.createEmptyDescription("foo");
  }

  @Test
  public void shouldReturnDetectionStatusOfSurvivedWhenNoFailuresOrErrors() {
    this.testee.onTestSuccess(new TestResult(this.description, null));
    assertEquals(DetectionStatus.SURVIVED, this.testee.status());
  }

  @Test
  public void shouldReturnDetectionStatusOfKilledWhenEncountersFailure() {
    this.testee.onTestFailure(new TestResult(this.description, null));
    assertEquals(DetectionStatus.KILLED, this.testee.status());
  }

  @Test
  public void shouldReturnDetectionStatusOfKilledWhenEncountersError() {
    this.testee.onTestError(new TestResult(this.description, null));
    assertEquals(DetectionStatus.KILLED, this.testee.status());
  }

  @Test
  public void shouldReturnDetectionStatusOfTimedOutWhenReceivesATimeoutException() {
    this.testee.onTestError(new TestResult(this.description, new TimeoutException("foo")));
    assertEquals(DetectionStatus.TIMED_OUT, this.testee.status());
  }

  @Test
  public void shouldRecordDescriptionOfLastFailingTest() {
    this.testee.onTestFailure(new TestResult(this.description, null));
    assertEquals(Option.some(this.description), this.testee.lastFailingTest());
  }

  @Test
  public void shouldRecordDescriptionOfLastErroringTest() {
    this.testee.onTestError(new TestResult(this.description, null));
    assertEquals(Option.some(this.description), this.testee.lastFailingTest());
  }

  @Test
  public void shouldRecordDescriptionOfLastTimedOutTest() {
    this.testee.onTestError(new TestResult(this.description, new TimeoutException("foo")));
    assertEquals(Option.some(this.description), this.testee.lastFailingTest());
  }
}
