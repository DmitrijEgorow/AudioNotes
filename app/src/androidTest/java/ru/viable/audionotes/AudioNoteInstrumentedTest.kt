package ru.viable.audionotes

import android.app.Instrumentation
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.view.View
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso
import androidx.test.espresso.FailureHandler
import androidx.test.espresso.IdlingPolicies
import androidx.test.espresso.accessibility.AccessibilityChecks
import androidx.test.espresso.base.DefaultFailureHandler
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResult
import io.github.kakaocup.kakao.screen.Screen
import io.github.kakaocup.kakao.text.KButton
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.hamcrest.Matcher
import org.junit.AfterClass
import org.junit.Assert
import org.junit.Before
import org.junit.BeforeClass
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import ru.viable.audionotes.ui.MainActivity
import java.util.Locale
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@MediumTest
class AudioNoteInstrumentedTest {

    private var activityScenario: ActivityScenario<MainActivity>? = null
    private var handler: DescriptionFailureHandler? = null
    private var uiDevice: UiDevice? = null

    private val fabRes = "fab_add_new_audionote"

    private lateinit var appContext: Context
    private lateinit var mInstrumentation: Instrumentation

    @Before
    fun setUp() {
        mInstrumentation = InstrumentationRegistry.getInstrumentation()
        handler = DescriptionFailureHandler(mInstrumentation)
        Espresso.setFailureHandler(handler)

        uiDevice = UiDevice.getInstance(mInstrumentation)
        uiDevice?.pressHome()

        val nonLocalizedContext = mInstrumentation.targetContext
        val configuration = nonLocalizedContext.resources.configuration
        configuration.setLocale(Locale.UK)
        appContext = nonLocalizedContext.createConfigurationContext(configuration)

        val intent = Intent(appContext, MainActivity::class.java)
        activityScenario = ActivityScenario.launch(intent)

        addNewRecordingButtonId = appContext.resources.getIdentifier(
            fabRes,
            "id",
            appContext.opPackageName,
        )
    }

    @Test(timeout = MAX_TEST_TIMEOUT)
    fun addNewRecordingButtonTest() {
        Assert.assertNotEquals(0, addNewRecordingButtonId)
        addNewRecordingButtonCheckStep()
    }

    private fun addNewRecordingButtonCheckStep() = runTest {
        class ListScreen : Screen<ListScreen>() {
            val addNewRecording = KButton { withId(addNewRecordingButtonId) }
        }

        val screen = ListScreen()
        screen {
            addNewRecording.apply {
                isVisible()
                isDisabled()
            }
            handler?.appendExtraMessage("wait for initialization")
            SystemClock.sleep(MAX_TIMEOUT)
            addNewRecording.apply {
                isEnabled()
                isClickable()
                click()
            }
            SystemClock.sleep(MAX_TIMEOUT)
            addNewRecording.click()
            SystemClock.sleep(10_000)
        }
    }

    companion object {
        private const val MAX_TIMEOUT: Long = 4_000
        private const val MAX_TEST_TIMEOUT: Long = 30_000

        private var addNewRecordingButtonId = 0

        @BeforeClass
        @JvmStatic
        fun enableAccessibilityChecks() {
            IdlingPolicies.setMasterPolicyTimeout(5, TimeUnit.SECONDS)
            IdlingPolicies.setIdlingResourceTimeout(5, TimeUnit.SECONDS)
            AccessibilityChecks.enable().setRunChecksFromRootView(true)
                .setThrowExceptionFor(AccessibilityCheckResult.AccessibilityCheckResultType.WARNING)
                .setThrowExceptionFor(AccessibilityCheckResult.AccessibilityCheckResultType.ERROR)
                .setThrowExceptionFor(AccessibilityCheckResult.AccessibilityCheckResultType.INFO)
        }

        @AfterClass
        @JvmStatic
        fun printResult() {
            val mInstrumentation = InstrumentationRegistry.getInstrumentation()
            val uiDevice = UiDevice.getInstance(mInstrumentation)
            uiDevice.pressHome()
        }
    }
}

class DescriptionFailureHandler(instrumentation: Instrumentation) : FailureHandler {
    var extraMessage = StringBuilder("")
    var delegate: DefaultFailureHandler = DefaultFailureHandler(instrumentation.targetContext)

    override fun handle(error: Throwable?, viewMatcher: Matcher<View>?) {
        // Log anything you want here
        if (error != null) {
            val newError = Throwable(
                "$extraMessage ${error.message}",
                error.cause,
            )
            delegate.handle(newError, viewMatcher)
        }
    }

    fun appendExtraMessage(text: String) {
        extraMessage = extraMessage.append(text)
    }
}
