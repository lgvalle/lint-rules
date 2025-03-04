package com.vanniktech.lintrules.rxjava2

import com.android.tools.lint.checks.infrastructure.TestFiles.java
import com.android.tools.lint.checks.infrastructure.TestLintTask.lint
import org.junit.Test

class RxJava2DisposableDisposeCallDetectorTest {
  @Test fun callingCompositeDisposableDispose() {
    lint()
      .files(
        rxJava2(),
        java(
          """
            package foo;

            import io.reactivex.disposables.CompositeDisposable;

            class Example {
              public void foo() {
                CompositeDisposable cd = null;
                cd.dispose();
              }
            }"""
        ).indented()
      )
      .issues(ISSUE_DISPOSABLE_DISPOSE_CALL)
      .run()
      .expect(
        """
            |src/foo/Example.java:8: Warning: Calling dispose instead of clear [RxJava2DisposableDisposeCall]
            |    cd.dispose();
            |       ~~~~~~~
            |0 errors, 1 warnings""".trimMargin()
      )
      .expectFixDiffs(
        """
            |Fix for src/foo/Example.java line 7: Fix it:
            |@@ -8 +8
            |-     cd.dispose();
            |+     cd.clear();
            |""".trimMargin()
      )
  }
}
