@file:Suppress("UnstableAPIUSage") // We know that Lint API's aren't final.

package com.vanniktech.lintrules.android

import com.android.tools.lint.checks.infrastructure.TestFiles.java
import com.android.tools.lint.checks.infrastructure.TestLintTask.lint
import org.junit.Test

class InvalidImportDetectorTest {
  private val r = java(
    """
      package foo;

      public final class R {
        public static final class drawable {
        }
      }"""
  ).indented()

  private val internal = java(
    """
      package com.foo.internal;

      public final class Foo {
      }"""
  ).indented()

  @Test fun normalRImport() {
    lint()
      .files(
        r,
        java(
          """
          package foo;

          import foo.R;

          class Example {
          }"""
        ).indented()
      )
      .issues(ISSUE_INVALID_IMPORT)
      .run()
      .expectClean()
  }

  @Test fun rDrawableImport() {
    lint()
      .files(
        r,
        java(
          """
          package foo;

          import foo.R.drawable;

          class Example {
          }"""
        ).indented()
      )
      .issues(ISSUE_INVALID_IMPORT)
      .run()
      .expect(
        """
          |src/foo/Example.java:3: Warning: Forbidden import [InvalidImport]
          |import foo.R.drawable;
          |       ~~~~~~~~~~~~~~
          |0 errors, 1 warnings""".trimMargin()
      )
  }

  @Test fun internalImport() {
    lint()
      .files(
        internal,
        java(
          """
          package foo;

          import com.foo.internal.Foo;

          class Example {
          }"""
        ).indented()
      )
      .issues(ISSUE_INVALID_IMPORT)
      .run()
      .expect(
        """
          |src/foo/Example.java:3: Warning: Forbidden import [InvalidImport]
          |import com.foo.internal.Foo;
          |       ~~~~~~~~~~~~~~~~~~~~
          |0 errors, 1 warnings""".trimMargin()
      )
  }

  @Test fun libraryImportingTheirOwnInternals() {
    lint()
      .files(
        internal,
        java(
          """
          package com.foo;

          import com.foo.internal.Foo;

          class Example {
          }"""
        ).indented()
      )
      .issues(ISSUE_INVALID_IMPORT)
      .run()
      .expectClean()
  }

  @Test fun generatedSqldelightCode() {
    lint()
      .files(
        internal,
        java(
          """
          package com.foo;

          import com.squareup.sqldelight.db.SqlDriver;
          import com.squareup.sqldelight.internal.copyOnWriteList;

          class Example {
          }"""
        ).indented()
      )
      .issues(ISSUE_INVALID_IMPORT)
      .run()
      .expectClean()
  }
}
