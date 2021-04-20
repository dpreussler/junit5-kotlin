[![Build Status](https://travis-ci.org/dpreussler/junit5-kotlin.svg?branch=master)](https://travis-ci.org/dpreussler/junit5-kotlin) 

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/de.jodamob.junit5/junit5-kotlin/badge.svg)](https://maven-badges.herokuapp.com/maven-central/de.jodamob.junit5/junit5-kotlin)
[![Jitpack](https://jitpack.io/v/dpreussler/junit5-kotlin.svg)](https://jitpack.io/#dpreussler/junit5-kotlin)


# junit5-kotlin
Extensions for Junit5 for Kotlin programming language

## SealedClassesSource
Creates instances of sealed classes for `@Parametrized` tests

Usage:

```kotlin
@ParameterizedTest
@SealedClassesSource
fun test(item: SomeClass)
```

Can handle: 
- nested sealed classes
- singletons `object`
- empty constructors 
- constructors made out of primitive types

You can pass in a `TypeFactory` for creating custom instances like for mocking

You can extend the `DefaultTypeFactory` for creating custom instances while reusing the creation of constructors with single paramter constructors with basic types and empty constructors.


```kotlin
sealed class Fruit {
  object Orange : Fruit()
  object Banana : Fruit()
  data class Apple(color: String) : Fruit()
}

class FruitTypeFactory : DefaultTypeFactory() {
    override fun create(what: KClass<*>) = when (what) {
        Apple::class -> Fruit.Apple(
            color = "red"
        )
        else -> super.create(what)
    }
}
```

Get it:

```groovy
dependencies {
    implementation 'de.jodamob.junit5:junit5-kotlin:0.0.1'
}
```


## License


The MIT License (MIT)

Copyright (c) 2020 Danny Preussler

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.