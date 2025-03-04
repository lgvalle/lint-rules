@file:Suppress("UnstableAPIUSage") // We know that Lint API's aren't final.

package com.vanniktech.lintrules.android

import com.android.SdkConstants.ATTR_ID
import com.android.resources.ResourceUrl
import com.android.tools.lint.detector.api.Category.Companion.CORRECTNESS
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.LayoutDetector
import com.android.tools.lint.detector.api.Scope.Companion.RESOURCE_FILE_SCOPE
import com.android.tools.lint.detector.api.Severity.WARNING
import com.android.tools.lint.detector.api.XmlContext
import org.w3c.dom.Attr

val ISSUE_MATCHING_VIEW_ID = Issue.create(
  "MatchingViewId", "Flags view ids that don't match with the file name.",
  "When the layout file is named activity_home all of the containing ids should be prefixed with activityHome to avoid ambiguity between different layout files across different views.",
  CORRECTNESS, PRIORITY, WARNING,
  Implementation(MatchingViewIdDetector::class.java, RESOURCE_FILE_SCOPE)
)

class MatchingViewIdDetector : LayoutDetector() {
  override fun getApplicableAttributes() = listOf(ATTR_ID)

  override fun visitAttribute(context: XmlContext, attribute: Attr) {
    val id = ResourceUrl.parse(attribute.value)?.name ?: return
    val fixer = MatchingIdFixer(context, id)
    val isAndroidId = attribute.value.startsWith("@android:id/")
    val isUsingViewBinding = context.project.buildModule?.buildFeatures?.viewBinding == true

    if (fixer.needsFix() && !isAndroidId && !isUsingViewBinding) {
      val fix = fix()
        .replace()
        .text(id)
        .with(fixer.fixedId())
        .autoFix()
        .build()

      context.report(ISSUE_MATCHING_VIEW_ID, attribute, context.getValueLocation(attribute), "Id should start with: ${fixer.expectedPrefix}", fix)
    }
  }
}
