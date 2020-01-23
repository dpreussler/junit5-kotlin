package berlin.preussler.junit5

import org.junit.jupiter.api.*
import org.junit.jupiter.params.ParameterizedTest
import kotlin.reflect.KClass

sealed class Letters {
    object A: Letters()
    object B: Letters()
    object C: Letters()
}

sealed class Family {
    class Mother: Family()
    class Father: Family()
    sealed class Children: Family() {
        class Son: Children()
        class Daughter: Children()
        sealed class GrandChildren: Children() {
            class GrandSon: GrandChildren()
            class GradDaughter: GrandChildren()
        }
    }
}

sealed class Mixed {
    data class A(val item: String): Mixed()
    data class B(val item: String?): Mixed()
    data class C(val item: Int): Mixed()
    data class D(val item: Float): Mixed()
    data class E(val item: Long): Mixed()
    data class F(val item: Short): Mixed()
    data class G(val item: Byte): Mixed()
    data class H(val item1: String, val item2: Float): Mixed()
}

sealed class Custom {
    data class YouDontKnowMe(val me: Me): Custom()
    class Me
}

class SealedClassesSourceTest {

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class Singletos {

        val items = mutableListOf<String>()

        @AfterAll
        fun check() {
            Assertions.assertEquals(listOf("A", "B", "C"), items)
        }

        @ParameterizedTest
        @SealedClassesSource
        fun build(letter: Letters) {
            items.add(letter::class.simpleName!!)
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class Nesting {

        val items = mutableListOf<String>()

        @AfterAll
        fun check() {
            Assertions.assertEquals(listOf("Mother", "Father", "Son", "Daughter", "GrandSon", "GradDaughter"), items)
        }

        @ParameterizedTest
        @SealedClassesSource
        fun build(member: Family) {
            items.add(member::class.simpleName!!)
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class Constructors {

        val items = mutableListOf<String>()

        @AfterAll
        fun check() {
            Assertions.assertEquals(listOf("A", "B", "C", "D", "E", "F", "G", "H"), items)
        }

        @ParameterizedTest
        @SealedClassesSource
        fun build(member: Mixed) {
            items.add(member::class.simpleName!!)
        }
    }

    @ParameterizedTest
    @SealedClassesSource(factoryClass = CustomFactory::class)
    fun `can check custom types`(item: Custom) {
        Assertions.assertTrue(item is Custom.YouDontKnowMe)
    }

    class CustomFactory : SealedClassesSource.TypeFactory {
        override fun create(what: KClass<*>) = Custom.YouDontKnowMe(Custom.Me())
    }
}