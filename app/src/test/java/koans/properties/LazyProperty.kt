package koans.properties

import org.junit.Assert
import org.junit.Test

/**
 * https://play.kotlinlang.org/koans/Properties/Lazy%20property/Task.kt
 */
class LazyProperty {

    @Test
    fun lazyProperty_test() {
        val customLazyProperty = CustomLazyProperty { 100 }
        Assert.assertEquals(customLazyProperty.lazy, 100)
    }
}

class CustomLazyProperty(val initializer: () -> Int) {
    var value: Int? = null
    val lazy: Int
        get() {
            if (value == null) {
                value = initializer()
            }
            return value!!
        }
}