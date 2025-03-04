package com.vanniktech.lintrules.android

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.Category.Companion.CORRECTNESS
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Detector.UastScanner
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.LintFix
import com.android.tools.lint.detector.api.Scope.JAVA_FILE
import com.android.tools.lint.detector.api.Severity.WARNING
import org.jetbrains.uast.UAnnotated
import org.jetbrains.uast.UClass
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UMethod
import org.jetbrains.uast.UVariable
import java.util.EnumSet

private val annotationOrder = listOf(
  // Important ones.
  "Deprecated",
  "Override",
  "Test",
  "Ignore",
  // Suppressing the suppressed.
  "Suppress",
  "SuppressLint",
  "SuppressWarnings",
  // Annotations.
  "Documented",
  "Retention",
  "JvmStatic",
  // Dagger / JSR 305.
  "Provides",
  "Binds",
  "BindsInstance",
  "BindsOptionalOf",
  "Singleton",
  "Component",
  "SubComponent",
  "AssistedModule",
  "Module",
  "Inject",
  "IntoMap",
  "IntoSet",
  "Qualifier",
  "Reusable",
  "Named",
  "MapKey",
  // Nullability.
  "Nullable",
  "NonNull",
  "NotNull",
  // Checking the things.
  "CheckResult",
  "CheckReturnValue",
  // ButterKnife.
  "BindAnim",
  "BindArray",
  "BindBitmap",
  "BindBool",
  "BindColor",
  "BindDimen",
  "BindDrawable",
  "BindFloat",
  "BindFont",
  "BindInt",
  "BindString",
  "BindView",
  "BindViews",
  // Json.
  "Json",
  "JsonQualifier",
  // Retroauth.
  "Authenticated",
  // Retrofit 2.
  "GET",
  "POST",
  "PUSH",
  "PATCH",
  "DELETE",
  "Query",
  "Path",
  "Body",
  // Threads from support annotations.
  "MainThread",
  "UiThread",
  "WorkerThread",
  "BinderThread",
  "AnyThread",
  // Other things from support annotations.
  "RestrictTo",
  "Keep",
  "CallSuper",
  "TargetApi",
  "SdkConstant",
  "IntDef",
  "StringDef",
  "RequiresApi",
  "RequiresPermission",
  "RequiresPermission.Read",
  "RequiresPermission.Write",
  "VisibleForTesting",
  // AutoValue + extensions.
  "AutoValue",
  "ParcelAdapter",
  // Resources from support annotations.
  "AnimatorRes",
  "AnimRes",
  "AnyRes",
  "ArrayRes",
  "AttrRes",
  "BoolRes",
  "ColorRes",
  "DimenRes",
  "DrawableRes",
  "FontRes",
  "FractionRes",
  "IdRes",
  "IntegerRes",
  "InterpolatorRes",
  "LayoutRes",
  "MenuRes",
  "PluralsRes",
  "RawRes",
  "StringRes",
  "StyleableRes",
  "TransitionRes",
  "XmlRes",
  // Colors from support annotations.
  "ColorInt",
  "ColorLong",
  // Others from support annotations.
  "Dimension",
  "GuardedBy",
  "HalfFloat",
  "Px",
  // Ranges and sizes from support annotations.
  "IntRange",
  "FloatRange",
  "Size"
)

private val jetbrainsNullityAnnotations = listOf(
  "org.jetbrains.annotations.Nullable",
  "org.jetbrains.annotations.NotNull"
)

val ISSUE_WRONG_ANNOTATION_ORDER = Issue.create(
  "WrongAnnotationOrder",
  "Checks that Annotations comply with a certain order.",
  "Annotations should always be applied with the same order to have consistency across the code base.",
  CORRECTNESS, PRIORITY, WARNING,
  Implementation(AnnotationOrderDetector::class.java, EnumSet.of(JAVA_FILE))
)

class AnnotationOrderDetector : Detector(), UastScanner {
  override fun getApplicableUastTypes() = listOf<Class<out UElement>>(UVariable::class.java, UMethod::class.java, UClass::class.java)

  override fun createUastHandler(context: JavaContext) = AnnotationOrderVisitor(context)

  class AnnotationOrderVisitor(private val context: JavaContext) : UElementHandler() {
    override fun visitVariable(node: UVariable) {
      // Workaround https://groups.google.com/forum/#!topic/lint-dev/BaRimyf40tI
      processAnnotations(node, node)
    }

    override fun visitMethod(node: UMethod) {
      processAnnotations(node, node)
    }

    override fun visitClass(node: UClass) {
      processAnnotations(node, node)
    }

    private fun processAnnotations(element: UElement, modifierListOwner: UAnnotated) {

      val annotations = context.evaluator.getAllAnnotations(modifierListOwner, false)
        .filter { it.qualifiedName !in jetbrainsNullityAnnotations }
        .mapNotNull { it.qualifiedName?.split(".")?.lastOrNull() }
        .distinct()

      val (recognizedAnnotations, unrecognizedAnnotations) = annotations.partition { annotationOrder.contains(it) }
      val isInCorrectOrder = isInCorrectOrder(annotations)

      if (!isInCorrectOrder && recognizedAnnotations.isNotEmpty()) {
        val correctOrder = annotationOrder
          .filter { recognizedAnnotations.contains(it) }
          .plus(unrecognizedAnnotations)
          .joinToString(separator = " ") { "@$it" }

        val fix = LintFix.create()
          .replace()
          .text(annotations.joinToString(separator = " ") { "@$it" })
          .with(correctOrder)
          .range(context.getLocation(element))
          .name("Fix order")
          .autoFix()
          .build()

        context.report(ISSUE_WRONG_ANNOTATION_ORDER, element, context.getNameLocation(element), "Annotations are in wrong order. Should be $correctOrder", fix)
      }
    }

    @Suppress("Detekt.LabeledExpression") private fun isInCorrectOrder(annotations: List<String>): Boolean {
      var size = 0
      return annotationOrder.contains(annotations.firstOrNull()) && annotations.all {
        if (annotationOrder.contains(it)) {
          for (i in size until annotationOrder.size) {
            size++

            if (it == annotationOrder[i]) {
              return@all true
            }
          }

          return@all false
        }

        return@all true
      }
    }
  }
}
