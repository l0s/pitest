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
package org.pitest.mutationtest.execute;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.pitest.extension.common.TestUnitDecorator;
import org.pitest.functional.SideEffect;
import org.pitest.mutationtest.instrument.TimeoutLengthStrategy;
import org.pitest.testapi.ResultCollector;
import org.pitest.testapi.TestUnit;
import org.pitest.util.Unchecked;

public final class MutationTimeoutDecorator extends TestUnitDecorator {

  private final TimeoutLengthStrategy timeOutStrategy;
  private final SideEffect            timeOutSideEffect;
  private final long                  executionTime;

  public MutationTimeoutDecorator(final TestUnit child,
      final SideEffect timeOutSideEffect,
      final TimeoutLengthStrategy timeStrategy, final long executionTime) {
    super(child);
    this.timeOutSideEffect = timeOutSideEffect;
    this.executionTime = executionTime;
    this.timeOutStrategy = timeStrategy;
  }

  @Override
  public void execute(final ClassLoader loader, final ResultCollector rc) {

    final long maxTime = this.timeOutStrategy
        .getAllowedTime(this.executionTime);

    final FutureTask<?> future = createFutureForChildTestUnit(loader, rc);
    executeFutureWithTimeOut(maxTime, future);
    if (!future.isDone()) {
      this.timeOutSideEffect.apply();
    }

  }

  private void executeFutureWithTimeOut(final long maxTime,
      final FutureTask<?> future) {
    try {
      future.get(maxTime, TimeUnit.MILLISECONDS);
    } catch (final TimeoutException ex) {
      // swallow
    } catch (final InterruptedException e) {
      // swallow
    } catch (final ExecutionException e) {
      throw Unchecked.translateCheckedException(e);
    }
  }

  private FutureTask<?> createFutureForChildTestUnit(final ClassLoader loader,
      final ResultCollector rc) {
    final FutureTask<?> future = new FutureTask<Object>(createCallableForChild(
        loader, rc));
    final Thread thread = new Thread(future);
    thread.setDaemon(true);
    thread.setName("mutationTestThread");
    thread.start();
    return future;
  }

  private Callable<Object> createCallableForChild(final ClassLoader loader,
      final ResultCollector rc) {
    return new Callable<Object>() {

      public Object call() throws Exception {
        child().execute(
            loader,
            new TimingMetaDataResultCollector(rc,
                MutationTimeoutDecorator.this.executionTime));
        return null;
      }

    };
  }

}
