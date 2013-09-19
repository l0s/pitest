/*
 * Copyright 2010 Henry Coles
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

package org.pitest.mutationtest.instrument;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pitest.coverage.domain.TestInfo;
import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.functional.Option;
import org.pitest.mutationtest.execute.MutationTimeoutDecorator;
import org.pitest.mutationtest.execute.Reporter;
import org.pitest.mutationtest.execute.TimeOutSystemExitSideEffect;
import org.pitest.testapi.TestUnit;

public class TimeOutDecoratedTestSource {

  private final Map<String, TestUnit> allTests = new HashMap<String, TestUnit>();
  private final TimeoutLengthStrategy timeoutStrategy;
  private final Reporter              r;

  public TimeOutDecoratedTestSource(
      final TimeoutLengthStrategy timeoutStrategy,
      final List<TestUnit> allTests, final Reporter r) {
    this.timeoutStrategy = timeoutStrategy;
    mapTests(allTests);
    this.r = r;
  }

  private void mapTests(final List<TestUnit> tests) {
    for (final TestUnit each : tests) {
      this.allTests.put(each.getDescription().getQualifiedName(), each);
    }
  }

  public List<TestUnit> translateTests(final List<TestInfo> testsInOrder) {
    return FCollection.flatMap(testsInOrder, testToTestUnit());
  }

  private F<TestInfo, Option<TestUnit>> testToTestUnit() {
    return new F<TestInfo, Option<TestUnit>>() {

      public Option<TestUnit> apply(final TestInfo a) {
        final TestUnit tu = TimeOutDecoratedTestSource.this.allTests.get(a
            .getName());
        if (tu != null) {
          return Option
              .<TestUnit> some(new MutationTimeoutDecorator(tu,
                  new TimeOutSystemExitSideEffect(
                      TimeOutDecoratedTestSource.this.r),
                  TimeOutDecoratedTestSource.this.timeoutStrategy, a.getTime()));
        }
        return Option.none();
      }

    };
  }

}
