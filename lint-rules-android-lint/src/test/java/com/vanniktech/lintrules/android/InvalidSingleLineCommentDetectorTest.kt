package com.vanniktech.lintrules.android

import com.android.tools.lint.checks.infrastructure.TestFiles.gradle
import com.android.tools.lint.checks.infrastructure.TestFiles.java
import com.android.tools.lint.checks.infrastructure.TestFiles.kt
import com.android.tools.lint.checks.infrastructure.TestLintTask.lint
import org.junit.Test

class InvalidSingleLineCommentDetectorTest {
  @Test fun invalidSingleLineCommentNoSpace() {
    lint()
      .files(
        java(
          """
          package foo;

          class Example {
            public void foo() {
              //Something.
            }
          }"""
        ).indented()
      )
      .issues(ISSUE_INVALID_SINGLE_LINE_COMMENT)
      .run()
      .expect(
        """
          |src/foo/Example.java:5: Warning: Comment does not contain a space at the beginning [InvalidSingleLineComment]
          |    //Something.
          |    ~~~
          |0 errors, 1 warnings""".trimMargin()
      )
      .expectFixDiffs(
        """
          |Fix for src/foo/Example.java line 4: Add space:
          |@@ -5 +5
          |-     //Something.
          |+     // Something.
          |""".trimMargin()
      )
  }

  @Test fun invalidSingleLineCommentNotStartingCapitalLetter() {
    lint()
      .files(
        java(
          """
          package foo;

          class Example {
            public void foo() {
              // something.
            }
          }"""
        ).indented()
      )
      .issues(ISSUE_INVALID_SINGLE_LINE_COMMENT)
      .run()
      .expect(
        """
          |src/foo/Example.java:5: Warning: Comments first word should be capitalized [InvalidSingleLineComment]
          |    // something.
          |       ^
          |0 errors, 1 warnings""".trimMargin()
      )
      .expectFixDiffs(
        """
          |Fix for src/foo/Example.java line 4: Capitalized first word:
          |@@ -5 +5
          |-     // something.
          |+     // Something.
          |""".trimMargin()
      )
  }

  @Test fun invalidSingleLineCommentNoPeriodAtEnd() {
    lint()
      .files(
        java(
          """
          package foo;

          class Example {
            public void foo() {
              // Something
            }
          }"""
        ).indented()
      )
      .issues(ISSUE_INVALID_SINGLE_LINE_COMMENT)
      .run()
      .expect(
        """
          |src/foo/Example.java:5: Warning: Comment does not end with a period [InvalidSingleLineComment]
          |    // Something
          |    ~~~~~~~~~~~~
          |0 errors, 1 warnings""".trimMargin()
      )
      .expectFixDiffs(
        """
          |Fix for src/foo/Example.java line 4: Add period:
          |@@ -5 +5
          |-     // Something
          |+     // Something.
          |""".trimMargin()
      )
  }

  @Test fun validSingleLineCommentEndingWithPeriod() {
    lint()
      .files(
        java(
          """
          package foo;

          class Example {
            public void foo() {
              // Something. Do not modify!
              // Something. Do not modify.
              // Something. Do not modify?
              // Something. (Do not modify)
            }
          }"""
        ).indented()
      )
      .issues(ISSUE_INVALID_SINGLE_LINE_COMMENT)
      .run()
      .expectClean()
  }

  @Test fun invalidSingleLineCommentNoSpaceBefore() {
    lint()
      .files(
        java(
          """
          package foo;

          class Example {
            public void foo() {
              int foo = 5 + 5;// Something.
            }
          }"""
        ).indented()
      )
      .issues(ISSUE_INVALID_SINGLE_LINE_COMMENT)
      .run()
      .expect(
        """
          |src/foo/Example.java:5: Warning: Comment declaration is not preceded by a single space [InvalidSingleLineComment]
          |    int foo = 5 + 5;// Something.
          |                   ~~~
          |0 errors, 1 warnings""".trimMargin()
      )
      .expectFixDiffs(
        """
          |Fix for src/foo/Example.java line 4: Add space:
          |@@ -5 +5
          |-     int foo = 5 + 5;// Something.
          |+     int foo = 5 + 5; // Something.
          |""".trimMargin()
      )
  }

  @Test fun invalidSingleLineCommentIgnoresLinks() {
    lint()
      .files(
        java(
          """
          package foo;

          class Example {
            public void foo() {
              String link1 = "https://android.com/";
              String link2 = "http://android.com/";
              String link3 = "market://details?id=5";
            }
          }"""
        ).indented()
      )
      .issues(ISSUE_INVALID_SINGLE_LINE_COMMENT)
      .run()
      .expectClean()
  }

  @Test fun invalidSingleLineCommentIgnoresNoPmd() {
    lint()
      .files(
        java(
          """
          package foo;

          class Example {
            public void foo() {
              // NOPMD
            }
          }"""
        ).indented()
      )
      .issues(ISSUE_INVALID_SINGLE_LINE_COMMENT)
      .run()
      .expectClean()
  }

  @Test fun invalidSingleLineCommentAllowsStartingDigit() {
    lint()
      .files(
        java(
          """
          package foo;

          class Example {
            public void foo() {
              // 2 plus 4 equals 6.
            }
          }"""
        ).indented()
      )
      .issues(ISSUE_INVALID_SINGLE_LINE_COMMENT)
      .run()
      .expectClean()
  }

  @Test fun invalidSingleLineCommentIgnoresNoInspection() {
    lint()
      .files(
        java(
          """
          package foo;

          class Example {
            public void foo() {
              //noinspection
            }
          }"""
        ).indented()
      )
      .issues(ISSUE_INVALID_SINGLE_LINE_COMMENT)
      .run()
      .expectClean()
  }

  @Test fun invalidSingleLineCommentIgnoresCommentConstant() {
    lint()
      .files(
        kt(
          """
           private const val COMMENT = "//"
           """
        ).indented()
      )
      .issues(ISSUE_INVALID_SINGLE_LINE_COMMENT)
      .run()
      .expectClean()
  }

  @Test fun invalidSingleLineCommentLinkInComment() {
    lint()
      .files(
        java(
          """
          package foo;

          class Example {
            public void foo() {
              // http://stackoverflow.com/a/38480079
              // This is expected. http://stackoverflow.com/a/38480079
            }
          }"""
        ).indented()
      )
      .issues(ISSUE_INVALID_SINGLE_LINE_COMMENT)
      .run()
      .expectClean()
  }

  @Test fun invalidSingleLineCommentIgnoresJustComment() {
    lint()
      .files(
        java(
          """
          package foo;

          class Example {
            public void foo() {
              //
            }
          }"""
        ).indented()
      )
      .issues(ISSUE_INVALID_SINGLE_LINE_COMMENT)
      .run()
      .expectClean()
  }

  @Test fun invalidSingleLineCommentIgnoresJustCommentWithTrailingWhitespace() {
    lint()
      .files(
        java(
          java(
            """
          package foo;

          class Example {
            public void foo() {
              //
            }
          }"""
          ).indented().contents.replace("//", "// ")
        )
      )
      .issues(ISSUE_INVALID_SINGLE_LINE_COMMENT)
      .run()
      .expect(
        """
          |src/foo/Example.java:5: Warning: Comment contains trailing whitespace [InvalidSingleLineComment]
          |    //
          |    ~~~
          |0 errors, 1 warnings""".trimMargin().replace("//", "// ")
      )
      .expectFixDiffs(
        """
          |Fix for src/foo/Example.java line 4: Remove trailing whitespace:
          |@@ -5 +5
          |-     //
          |+     //
          |""".trimMargin().replace("-     //", "-     // ")
      )
  }

  @Test fun invalidSingleLineCommentTrailingWhitespace() {
    lint()
      .files(
        java(
          java(
            """
          package foo;

          class Example {
            public void foo() {
              // Something.
            }
          }"""
          ).indented().contents.replace("// Something.", "// Something. ")
        )
      )
      .issues(ISSUE_INVALID_SINGLE_LINE_COMMENT)
      .run()
      .expect(
        """
          |src/foo/Example.java:5: Warning: Comment contains trailing whitespace [InvalidSingleLineComment]
          |    // Something.
          |    ~~~~~~~~~~~~~~
          |0 errors, 1 warnings""".trimMargin().replace("// Something.", "// Something. ")
      )
      .expectFixDiffs(
        """
          |Fix for src/foo/Example.java line 4: Remove trailing whitespace:
          |@@ -5 +5
          |-     // Something.
          |+     // Something.
          |""".trimMargin().replace("-     // Something.", "-     // Something. ")
      )
  }

  @Test fun worksOnGradleFiles() {
    lint()
      .files(
        gradle(
          """
          buildscript {
            repositories {
              mavenCentral() // we need this.
            }
          }"""
        ).indented()
      )
      .issues(ISSUE_INVALID_SINGLE_LINE_COMMENT)
      .run()
      .expect(
        """
          |build.gradle:3: Warning: Comments first word should be capitalized [InvalidSingleLineComment]
          |    mavenCentral() // we need this.
          |                      ^
          |0 errors, 1 warnings""".trimMargin()
      )
      .expectFixDiffs(
        """
          |Fix for build.gradle line 2: Capitalized first word:
          |@@ -3 +3
          |-     mavenCentral() // we need this.
          |+     mavenCentral() // We need this.
          |""".trimMargin()
      )
  }
}
