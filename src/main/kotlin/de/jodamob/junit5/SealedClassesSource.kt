package de.jodamob.junit5


import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import org.junit.jupiter.params.support.AnnotationConsumer
import org.junit.platform.commons.util.Preconditions
import java.util.Objects
import kotlin.reflect.*

/**
 * {@code @SealedClassesSource} is an junit ArgumentsSource for sealed classes.
 *
 * <p>The sealed class instances will be provided as arguments to the annotated
 * {@code @ParameterizedTest} method.
 *
 *
 * <p>by default only sealed classes with objects or empty constructors are supported, use
 * {@link #factoryClass} attribute to provide a factory to create your specific instance.
 *
 * @see org.junit.jupiter.params.provider.ArgumentsSource
 * @see org.junit.jupiter.params.ParameterizedTest
 */
@Target(AnnotationTarget.TYPE, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@ArgumentsSource(SealedClassesArgumentsProvider::class)
annotation class SealedClassesSource(
    val factoryClass: KClass<out TypeFactory> = DefaultTypeFactory::class,
    val names: Array<String> = [],
    val mode: Mode = Mode.INCLUDE
) {

    // factory to create actual instances of a class
    interface TypeFactory {
        fun create(what: KClass<*>): Any
    }

    enum class Mode {
        INCLUDE,
        EXCLUDE
    }
}

// default factory that can return singletons and instances of classes with empty constructor
open class DefaultTypeFactory: SealedClassesSource.TypeFactory {

    override fun create(what: KClass<*>): Any {
        return what.objectInstance ?: what.constructors.first().create()
    }

    private fun KFunction<Any>.create() =
        if (parameters.isEmpty()) call()
        else call(*(parameters.map { it.createArgument() }.toTypedArray()))

    // grab known types for constructor arguments
    private fun KParameter.createArgument(): Any? {
        return type.createArgument()
    }

    private fun KType.createArgument(): Any? {
        return when (classifier) {
            Int::class -> 0
            Byte::class -> 0.toByte()
            Short::class -> 0.toShort()
            String::class -> ""
            Float::class -> 0f
            Long::class -> 0L
            Boolean::class -> false
            Throwable::class -> Throwable()
            List::class -> emptyList<Any>()
            Map::class -> emptyMap<Any, Any>()
            Set::class -> emptySet<Any>()
            else -> when {
                classifier?.isArray() == true -> {
                    java.lang.reflect.Array.newInstance(
                        arguments.first().type!!.createArgument()!!.javaClass,
                        0
                    )
                }
                classifier?.isEnum() == true -> {
                    val enumJavaClass = (classifier as KClass<out Enum<*>>).javaObjectType
                    Util.getEnumConstantByName(
                        enumJavaClass,
                        enumJavaClass.fields.first().name
                    )
                }

                else -> null
            }
        }
    }
}

private fun KClassifier.isEnum(): Boolean = (this as KClass<*>).supertypes.any { t ->
    (t.classifier as KClass<out Any>).qualifiedName == "kotlin.Enum" }

private fun KClassifier.isArray(): Boolean = (this as KClass<*>).qualifiedName == "kotlin.Array"


internal class SealedClassesArgumentsProvider : ArgumentsProvider, AnnotationConsumer<SealedClassesSource> {

    private lateinit var source: SealedClassesSource
    private val factory by lazy { source.factoryClass.java.newInstance() }


    override fun provideArguments(context: ExtensionContext) =
        determineClass(context)
            .sealedSubclasses // children
            .squashed() // flatten the tree
            .filter(source.names, source.mode)
            .map { factory.create(it) } // create instances
            .map { Arguments.of(it) } // convert to junit arguments
            .stream()

    override fun accept(source: SealedClassesSource) {
        this.source = source
    }

    // flatten a tree and keep only leaves
    private fun <T : Any> List<KClass<out T>>.squashed(): List<KClass<out T>> = flatMap {
        (if (it.sealedSubclasses.isEmpty()) listOf(it) else emptyList()) + it.sealedSubclasses.squashed<T>()
    }

    private fun <T : Any> List<KClass<out T>>.filter(
        names: Array<String>,
        mode: SealedClassesSource.Mode
    ): List<KClass<out T>> = filter { clazz ->
        if (names.isNotEmpty()) {
            when (mode) {
                SealedClassesSource.Mode.EXCLUDE -> !names.contains(clazz.simpleName)
                SealedClassesSource.Mode.INCLUDE -> names.contains(clazz.simpleName)
            }
        } else {
            true
        }
    }


    // extracts the class from method parameter, inspired bu org.junit.jupiter.params.provider.EnumArgumentsProvider.determineEnumClass(ExtensionContext)
    private fun determineClass(context: ExtensionContext): KClass<*> {
        val parameterTypes = context.requiredTestMethod.parameterTypes
        Preconditions.condition(parameterTypes.isNotEmpty()) {
            "Test method must declare at least one parameter: ${context.requiredTestMethod.toGenericString()}"
        }
        return parameterTypes[0].kotlin
    }
}
