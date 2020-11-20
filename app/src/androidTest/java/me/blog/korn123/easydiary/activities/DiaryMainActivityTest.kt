package me.blog.korn123.easydiary.activities

import android.util.Log
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.filters.LargeTest
import androidx.test.ext.junit.runners.AndroidJUnit4
import me.blog.korn123.easydiary.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.rules.activityScenarioRule
import me.blog.korn123.easydiary.helper.AAF_TEST
import me.blog.korn123.easydiary.helper.EasyDiaryDbHelper

@RunWith(AndroidJUnit4::class)
@LargeTest
class DiaryMainActivityTest {

    @get:Rule
    var activityScenarioRule = activityScenarioRule<DiaryMainActivity>()

    @Test
    fun test_01() {

        // Type text and then press the button.
        Espresso.onView(withId(R.id.query))
                .perform(ViewActions.typeText("Hello"), ViewActions.closeSoftKeyboard())
//        Espresso.onView(withId(R.id.delete)).perform(ViewActions.click())

        // Check that the text was changed.
        Espresso.onView(withId(R.id.query)).check(ViewAssertions.matches(ViewMatchers.withText("Hello")))
    }

    @Test
    fun test_02() {

        // Type text and then press the button.
        Espresso.onView(withId(R.id.query))
                .perform(ViewActions.typeText("Hello"), ViewActions.closeSoftKeyboard())

        Log.i(AAF_TEST, "Start")
        Log.w(AAF_TEST, "Start")
        EasyDiaryDbHelper.getTemporaryInstance().let {
            EasyDiaryDbHelper.readDiary(null, realmInstance = it).forEach { diary -> Log.i(AAF_TEST, diary.title ?: "") }
        }
        Log.i(AAF_TEST, "End")

        // Check that the text was changed.
        Espresso.onView(withId(R.id.query)).check(ViewAssertions.matches(ViewMatchers.withText("Hello")))
    }
}