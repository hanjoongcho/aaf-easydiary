package koans.properties

import org.junit.Assert
import org.junit.Test

/**
 * https://play.kotlinlang.org/koans/Properties/Properties/Task.kt
 */
class Properties {

    @Test
    fun properties_test() {
        val propertyExample = PropertyExample()
        propertyExample.propertyWithCounter = 1
        Assert.assertEquals(propertyExample.counter, 1)
        propertyExample.propertyWithCounter = 1
        propertyExample.propertyWithCounter = 1
        propertyExample.propertyWithCounter = 1
        Assert.assertEquals(propertyExample.counter, 4)
    }
}

class PropertyExample() {
    var counter = 0
    var propertyWithCounter: Int? = null
        set(v) {
            field = v
            counter++
        }
}