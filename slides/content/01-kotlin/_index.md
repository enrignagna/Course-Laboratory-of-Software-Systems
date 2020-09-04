 
+++

title = "Introduction to Laboratory of Software Systems"
description = "Description of the course"
outputs = ["Reveal"]

[reveal_hugo]
transition = "slide"
transition_speed = "fast"
custom_theme = "custom-theme.scss"
custom_theme_compile = true

[reveal_hugo.custom_theme_options]
targetPath = "css/custom-theme.css"
enableSourceMap = true

+++

# {{< course_name >}}

## Kotlin (for Scala developers)

### [Danilo Pianini](mailto:danilo.pianini@unibo.it)

{{< today >}}

---

# Why Kotlin

JetBrains-made modern programming language 
* Focused on "practical use" (whatever that means)

Gaining momentum since Google adopted is as *official Android language* (along with Java and C++)

Clearly inspired by a mixture of Java, C#, Scala, and Groovy

**In this course** -- we'll need it for Gradle and *internal* domain specific languages

---

# Philosophy: Kotlin vs. Scala

**Sca**_la_ is a **scalable** _language_
* Few core constructs that enable a huge variety of programming patterns
* Born in academia
* State of the art type checker with advanced features
    * Higher Kinded Types
    * Type lambdas
    * Enough to do type programming...

**Kotlin** is somewhat *a better java*
* Born in industry, for the industry
* Many more "core" constructs and keywords than Scala
* Focused on getting productive quickly and reducing programming errors
* Focus on multi-target (can compile towards JVM, JavaScript, and native)
    * Scala can as well by the way
    * Kotlin puts more care into bidirectional compatibility

---

# Kotlin 101
## Defining functions, constant, variables

Similar to Scala. The keyword `def` is replaced by `fun`
```kotlin
val x = 10 // constant
var y = 20 // variable, can be reassigned
fun foo() = 20 // function definition, single expression
fun bar(): Int { // same as above with multiple expression
    return 20 // requires a return in this form...
}
fun baz() { } // Unless it returns Unit

```

---

# Kotlin 101
## Function parameters and return types

Much like Scala:

* All parameters are named, but can be invoked positionally as well
* Parameters can have defaults
* Types are annotatedd after the parameter name
* Invocation can be positional or by name, with the rule that once a named parameter is used, subsequent parameters must be named as well

```kotlin
fun foo(a: Int = 0, b: String = "foo"): Int = TODO() // TODO() is a builtin function throwing a `NotImplementedError`
foo(1, "bar") // OK, positional
foo(a = 1, b = "bar") // OK, named
foo(1, b = "bar") // OK, hybrid
foo(a = 1, "bar") // error: no value passed for parameter 'b'
foo() // OK, both defaults
foo(1) // OK, same as foo(1, "foo")
foo("bar") // error: type mismatch: inferred type is String but Int was expected
foo(b = "bar") // OK, same as foo(0, "bar")
```

---

# Kotlin 101
## Top level functions

Kotlin supports top level functions

```kotlin
fun foo() {
    ...
}
```

By contrast, Scala requires to put them in an `object`

```scala
object Foo {
    def foo() {
        ...
    }
}
```

When targeting the JVM, Kotlin simply generates a `FileNameKt` class behind the scenes where the function is stored.
The behaviour can be controlled via annotations.


---

# Kotlin 101
## Program entry point

Naming a function `main` makes it a valid entry point:

```kotlin
fun main() = println("Hello World") // Valid entry point
```
```kotlin
fun main(arguments: Array<String>) = println("Hello World") // Valid entry point
```
```kotlin
fun main(arguments: Array<String>) {
    println("Hello World") // Return type is Unit, no need to return
} 
```

---

# Kotlin 101
## Nullable types

Every Kotlin type exists in two forms: normal, and nullable (likely inspired by Ceylon).
<br/>
Nullable types are suffixed by a `?` and require special handling
<br/>
`null` can't be assigned to non nullable types!
* Nullables are the Kotlin way to deal with `Option` types

```kotlin
var foo = "bar" // Okay, type is String
var baz: String? = foo // Okay, normal types can be assigned to nullables
foo = baz // error: type mismatch: inferred type is String? but String was expected
foo = null // error: null can not be a value of a non-null type String
```

---

# Kotlin 101
## Accessing nullable types

Nullable types memebers can't be accessed by `.`.
```kotlin
var baz: String? = "foo"
baz.length // error: only safe (?.) or non-null asserted (!!.) calls are allowed...
// on a nullable receiver of type String?
```

### Safe call operator `?.`
Performs runtime access to a member of a nullable object if it's not `null`, otherwise returns `null`
* Somewhat similar to Scala's `Option`'s `map` (but no monad involved)
```kotlin
var baz: String? = "foo"
baz?.length // returns 3, return type is "Int?", in fact...
val bar: Int = baz?.length // ...error: type mismatch: inferred type is Int? but Int was expected
baz = null
baz?.length // returns null, return type is still "Int?"
```

---

# Kotlin 101

### Non-null assertion `!!`
Also known as: *I want my code to break badly at runtime*
<br/>
* Invalidates the whole point of having nullable types by asserting that the nullable object is not `null` at runtime
* It should be **never** used
    * In fact its ugly syntax is so *ugly by purpose*

```kotlin
var baz: String? = "foo"
baz!!{{<comment_frag " // Returns 'foo', type String (non nullable)" >}}
baz!!.length{{<comment_frag " // returns 3, return type is Int" >}}
baz = null
baz!!{{<comment_frag " // throws a KotlinNullPointerException, like the good ol'times!" >}}
```

---

# Kotlin 101

### Elvis operator `?:`
Yeah it's actually named after Elvis Presley due to his haircut 😉
<br/>
* Returns the left operand if it's not `null`, otherwise the right one

```kotlin
var baz: String? = "foo"
baz ?: "bar" // Returns "foo", type String
baz?.length ?: 0 // returns 3, return type is Int
baz = null
baz ?: "bar" // Returns "bar", type String
baz?.length ?: 0 // returns 0, return type is Int
```

---

# Kotlin 101

## Platform types
Kotlin targets the JVM, JavaScript, and native code
<br/>
*None of them has nullable types!*

Nullability is unknown for types coming from the target platform, how to deal with them?
1. {{< frag c="Always consider them nullable (safe, but very unpleasant)" >}}
1. {{< frag c="Always consider them non nullable (code is lightweight and nice, but unsafe)" >}}

---

# Kotlin 101

## Platform types
Kotlin considers all foreign values whose nullability is unknown as *platform types*
* Their type is suffixed by `!` (e.g., `java.util.Date!`)
* At first use, their type is *implicitly disambiguated* (either nullable or non-nullable)
    * Namely, platform types can be used as non-nullable...
* Runtime nullability checks are put in place by the compiler (*fail fast!*)
    * ...but their actual nullablity is checked at use-site
* Platform types *can't be created* in Kotlin! They only come from interaction with "platform code"
* If the target platform offers some way to assert nullability, Kotlin tries to use it
    * e.g., if a Java method/parameter is annotated with `@NotNull` (or similar common alternatives) it will be interpreted as a non-nullable type

---

# Kotlin 101

## Type hierarchy

* In Java
    * top type: `Object`
    * bottom type: no bottom type
* In Scala
    * top type: `Any`
    * bottom type: `Nothing`
* In Kotlin:
    * top type: `{{<frag c="Any">}}`
    * bottom type:

---

# Kotlin 101

## Type hierarchy

* In Java
    * top type: `Object`
    * bottom type: no bottom type
* In Scala
    * top type: `Any`
    * bottom type: `Nothing`
* In Kotlin:
    * top type: ~~`Any`~~ `{{<frag c="Any?">}}`
    * bottom type: `{{<frag c="Nothing">}}`

---

# Kotlin 101

## `Boolean`s

Exactly as Java/Scala, but with nullability:
* `Boolean`: `true`/`false`
* `Boolean?`: `true`/`false`/`null`
*  `&&`, `!!`, and `!` operators work for *non-nullable* `Boolean`s.

Likewise Scala, boxing under the JVM is dealt with by the compiler
<br/>
`Boolean?` are always boxed (to be able to account for `null`)

---

# Kotlin 101

## Numeric types

Same as Scala, +nullability, +*unsigned experimental types*:
* `Byte`, `Short`, `Int`, `Long`, `Float`, `Double`
    * And nullable equivalents, always boxed under the JVM
* `UByte`, `UShort`, `UInt`, `ULong`

---

# Kotlin 101

## Issues of implicit numeric types conversion

Implicit type conversion to "bigger" types is source of nasty errors when automatic boxing is involved.
<br/>
Consider the following Scala code:
```scala
Double.NaN == Double.NaN // false, OK, as every sane language
Double.NaN equals Double.NaN // true! Boxing + Singleton make equality inconsistent!
```
Another example:
```scala
val a: Int = 1
val b: Long = a
a == b // true
a equals b // false
```
This causes a chain of issues, as `==` and `equals` do a different job, as do `##` and `hashCode`: `Map`s can become very surprising!


---
# Kotlin 101

## Numeric type conversions in Kotlin

Kotlin numeric types are converted manually to prevent these issues:
```kotlin
val i: Int = 1
val l: Long = 1
val l: Long = i{{<comment_frag " // error: type mismatch: inferred type is Int but Long was expected" >}}
val l: Long = i.toLong(){{<comment_frag " // OK" >}}
i + l{{<comment_frag " // OK, operators are overloaded" >}}
l + i{{<comment_frag " // OK, operators are overloaded" >}}
```

---

# Kotlin 101

## Numeric literals

```kotlin
1234567 // Literal Int
1_234_567 // Literal Int, underscored syntax (preferable)
123L // Literal Long
1.0 // Literal Double
123e4 // Literal Double in scientific notation
1d // Nope :)
1f // Literal Float
1u // Literal UInt
0123 // error: unsupported [literal prefixes and suffixes] (no octal)
0xCAFE // Hex literal Int
0xCAFEBABE // Hex literal Long (automatic, as it does not fit an Int)
0x0000000 // Hex literal Int, even it'd fit a Byte
0b1111111_11111111_11111111_11111111 // Binary Int (Integer.MAX_INT)
0b11111111_11111111_11111111_11111111 // Binary Long
0b11111111_11111111_11111111_11111111u // Binary UInt!
0xFFFF_FFFF_FFFFu // ULong
```

---

# Kotlin 101

## Strings and templating

Spiced up version of Java strings, Groovy-style templating:
* `$` begins a template expression
* Curly brackets must be used to disambiguate in case of calls inside the template: `${}`

```kotlin
val batman = "Batman"
// Groovy templating and Java-style concatenation both work
"${Double.NaN}".repeat(10) + " $batman!"{{<comment_frag " // NaNNaNNaNNaNNaNNaNNaNNaNNaNaN Batman!" >}}
"Batman is $batman.length characters long"{{<comment_frag " // Batman is Batman.length characters long" >}}
"Batman is ${batman.length} characters long"{{<comment_frag " // Batman is 6 characters long" >}}
```
---

# Kotlin 101

## Raw Strings

Triple-double-quoted strings are considered *raw strings*
* `\` is a normal character
* newlines are intended as part of the string
* Very handy for writing regular expressions
* `$`-templating still works
    * writing a dollar symbols requires some tricks

```kotlin
val dante = """
    Tanto gentile e tanto onesta pare
    la donna mia quand'ella altrui saluta,
    ch'ogne lingua devèn, tremando, muta
    e li occhi non l'ardiscon di guardare.
    """.trimIndent() // Indentation can be trimmed
val finalWordsEndingInA = """\W*(\w*a)\W*${'$'}""".toRegex(RegexOption.MULTILINE) // See how $ must be escaped
finalWordsEndingInA.findAll(dante).map { it.groups[1]?.value }.toList() {{<comment_frag " // [saluta, muta]" >}}
```

---

# Kotlin 101

## Packages and imports

Same as Java, plus aliasing.
<br/>
Imports go at the top of file, no locally scoped imports as in Scala
* There are no `implicit`s in Kotlin, the `import` statement does not modify context


```kotlin
package it.unibo.lss.experiments
import it.unibo.lss.ddd.Entity // Available as Entity locally
import org.company.someproduct.Entity as SomeProductEntity // Aliasing, accessible as SomeProductEntity
```

---

# Kotlin 101

## Varargs

Functions can have a parameter marked as `vararg `, accepting multiple entries
* Typically the last one (but not mandatorily as in Java)
* Maps to an `Array<out >

```kotlin
fun printall(vararg strings: String) {
    strings.forEach { println(it) } // We'll discuss this syntax later...
}
printall("Lorem", "ipsum", "dolor", "sit", "amet")
```

---

# Kotlin 101

## Naming in Kotlin

Kotlin is less permissive than Scala:
* Arbitrary symbols are not accepted as valid function names
* ...unless you explicitly surround them with backtics

```scala
def ##°@??%&@^^() = 1 // Super ok for Scala: def $hash$hash$u00B0$at$qmark$qmark$percent$amp$at$up$up(): Int
```
```kotlin
fun `##°@??%&@^^`() = 1 // OK
`##°@??%&@^^`() // 1. Must be invoked with backticks!
val `val` = "Hey look I can name things with keywords!"
val `names can also contain spaces` = 1
```

* General rule: **avoid it**
* It might be needed for interoperability with other languages, e.g. if a Java field is named `val`
* Tolerated in tests with Junit (but Kotlin-native suites as Kotest do not need it)

```kotlin
class JunitTest {
    @Test
    fun `404 errors should cause a wait and retry`() { // Nice and very clear name
        TODO()
    }
}
```

---

# Kotlin 101

## Local functions

Functions can contain other functions (as in Scala)

```kotlin
fun factorial(n: UInt): ULong {
    // tailrec forces optimization of tail recursion (and blocks compilation if recursion is non-tail)
    tailrec fun factorialWithAccumulator(current: UInt, accumulator: ULong): ULong = when {
        current >= n -> accumulator * current
        else -> factorialWithAccumulator(current + 1u, accumulator * current)
    }
    return factorialWithAccumulator(1u, 1u)
}
```
Warning: local functions often hinder clarity

---

# Kotlin 101 -- Flow control

## `if`
* `if`/`else` is an expression and works just as in Scala
* No ternary operator
* `if` alone is not an expression

---

# Kotlin 101 -- Flow control

### `for`
* **No classic** `for(init; condition; then) { block }` loop
* Only available as `for`/`in`: `for (element in collection) { block }`
* **Not a powerful combinator** like Scala's `for`
* *Rarely used* (I think I might have used it twice in my career)

---

# Kotlin 101 -- Flow control

### `while` and `do`/`while`
* Same as Java, but with *visibility of variables defined in the `do`-block*

```kotlin
import kotlin.random.Random
val lucky = 6
var attempts = 0
do {
    val draw = Random.nextInt(lucky + 1)
    attempts++
} while (draw != lucky) // draw is visible here
println("Launched $attempts dice before a lucky shot")
```

---

# Kotlin 101 -- Flow control

### `when`
Kotlin *does not support pattern matching* as Scala does (unfortunately)
<br/>
The `when` block is somewhat a mild surrogate, more similar to a `switch` on steroids
<br/>
The base version (without subject) is a more elegant "`if`/`else if`/`else`" chain

```kotlin
fun countBatmans(subject: String) = when {
    subject.length < "batman".length -> 0
    subject.length < 2 * "batman".length && subject.contains("batman") -> 1
    else -> ".*?(batman)".toRegex().findAll(subject).count().toInt()
}
```
* `when` is an expression in any case

---

# Kotlin 101 -- Flow control

### `when (subject)`
Checks if the value of subjects is the same of the expression on the right

```kotlin
fun baseForSingleDigitOrNull(digit: UInt) = when(digit) {
    0u, 1u -> "binary"
    2u -> "ternary"
    in 0u..7u -> "octal" // This is a range!
    in 0u..15u -> "hexadecimal"
    in 0u..36u -> "base36"
    else -> null
}
```
* `when` with subject can be used to elegantly check for subtypes
```kotlin
fun splitAnything(input: Any) = when(input) {
    is Int -> input / 2 // No need to cast! The compiler infers type automatically (smart cast)
    is String -> input.substring(input.length / 2)
    is Double -> input / 2
    else -> TODO()
}
```

---

# Kotlin 101 -- Flow control

### `when` 
Checks if the value of subjects is the same of the expression on the right

```kotlin
fun baseForSingleDigitOrNull(digit: UInt) = when(digit) {
    0u, 1u -> "binary"
    2u -> "ternary"
    in 0u..7u -> "octal" // This is a range!
    in 0u..15u -> "hexadecimal"
    in 0u..36u -> "base36"
    else -> null
}
```
* `when` is an expression in any case

---

# Kotlin 101 -- Flow control

### Jumping
**Jumping is awful, imperative, and you should not use it**
<br/>
...but someone might and you must be able to understand it...

* `break` and `continue` work as in Java
* `return` does not, as we will see when discussing higher order functions...

#### labeling
* Any expression can be labeled: `label@ 1` is a valid expression
* `break`, `continue`, and `return` can be *qualified* with a label

```kotlin
outerloop@ for (i in 1..100) {
    for (j in 1..100) {
        if (i * j == i + j) {
            println("$i * $j equals $i + $j")
            break@outerloop // Qualified break
        }
    }
}
```

---

# Kotlin 102 -- OOP

## Classes

* Similar to Scala, the keyword `class` introduces a class definition
* Object construction does not require `new`
    * `new` is not a Kotlin keyword at all
* Objecs get built from classes by just invoking the class name:

```Kotlin
class Foo
Foo() // a new Foo is created, no new keyword
```

---

# Kotlin 102 -- OOP

## Classes and members

Kotlin classes have two types of members: **methods** and **properties**

| Language / Member Type |            Fields            | Methods | Properties |
|------------------------|:----------------------------:|---------|------------|
| Java                   | *Yes*                        | *Yes*   | **No**     |
| Scala                  | *Yes*                        | *Yes*   | **No**     |
| Kotlin                 | **No** (Hidden)              | *Yes*   | *Yes*      |
| C#                     | *Yes*                        | *Yes*   | *Yes*      |

In Scala, at the caller site, methods and fields are hard to distinguish, as parentheses for 0-ary method calls are optional

In Kotlin, methods/functions (except when defined `infix`) are invoked with mandatory parentheses
<br/>
properties are instead invoked without parentheses

---

# Kotlin 102 -- OOP

## Properties vs. fields

Properties and fields are conceptually different
* *fields* **are** the object's state
* *properties* are a way to **access/change** the object's state

It's considered a good practice in languages without properties (Java in particular) to hide (*incapsulate*) fields (Object's actual state)
and provide access only via `get`/`set` methods: the actual state representation may change with no change to the API.

In Kotlin, fields are entirely hidden, and cannot be exposed in any way, enforcing the aforementioned convention at the language level.

---

# Kotlin 102 -- OOP

## Defining properties for classes

```kotlin
class Foo {
    val bar = 1
    var baz: String? = null
    val bazLength: Int
        get() = baz?.length ?: 0
    var stringRepresentation: String = ""
        get() = baz ?: field
        set(value) {
            field = "custom: $value"
        }

}
val foo = Foo()
foo.bar = 3{{<comment_frag " // error: val cannot be reassigned" >}}
foo.stringRepresentation{{<comment_frag " // empty string" >}}
foo.stringRepresentation = "zed"{{<comment_frag " // 'custom: zed'" >}}
```

---

# Kotlin 102 -- OOP

## Backing fields

The keyword `field` allows access to a backing field of a property
<br/>
**in case it is present**

The Kotlin compiler, in fact, generates backing fields only where it's needed

```kotlin
class Student {
    var id: String? = null // Backing field generated
    val identifier: String = "Student[${id ?: "unknown"}]" // No backing field
}
```

When designing with Kotlin, you must consider methods and properties, and forget about fields.

---

# Kotlin 102 -- OOP

## Defining methods

Methods are defined as `fun`ctions within the scope of a class

```kotlin
class MutableComplex {
    var real: Double = 0.0
    var imaginary: Double = 0.0
    fun plus(other: MutableComplex): MutableComplex = MutableComplex().also {
        it.real = real + other.real
        it.imaginary = imaginary + other.imaginary
    }
}
val foo = MutableComplex()
foo.real = 1.0
foo.imaginary = 2.0
val bar = MutableComplex()
bar.real = 4.1
bar.imaginary = 0.1
val baz = foo.plus(bar)
"${baz.real}+${baz.imaginary}i"{{<comment_frag " // 5.1+2.1i" >}}
```

---

# Kotlin 102 -- OOP

## Interfaces

* Similar to Java 8+
* Methods can be implemented
* Can host properties
    * And their accessors can be implemented
    * Properties in interfaces *do not have backing fields*
* Both properties and methods can be implemented there
* Scala-like mixins not supported
    * A Kotlin `interface` cannot be a subclass of a Kotlin `class`
```scala
class A
trait B extends A // All fine in Scala
```
```kotlin
open class A
interface B : A // error: an interface cannot inherit from a class
```
So, no mixins

---

# Kotlin 102 -- OOP

## Implementing interfaces

Much like Java. Subtyping keyword is `:`, overrides must be marked with `override`:
```kotlin
interface Shape {
    val area: Double
    val perimeter: Double
}
interface Shrinkable {
    fun shrink(): Unit
}
class MutableCircle : Shape, Shrinkable {
    var radius = 1.0
    override val area get() = Math.PI * radius * radius
    // What if we remove "get()"?
    override val perimeter get() = 2 * Math.PI * radius
    override fun shrink() {
        radius /= 2
    }
}
```
---

# Kotlin 102 -- OOP

## Superclass disambiguation

A call to `super` can be qualified to disambiguate between conflincting interface declarations:

```kotlin
interface A {
    fun foo() = "foo"
}
interface B {
    fun foo() = "bar"
}
class C : A, B {
    override fun foo() = super<A>.foo() + super<B>.foo()
}
C().foo(){{<comment_frag " // foobar" >}}
```

---

# Kotlin 102 -- OOP

## Primary constructors and `init`

Similar to Scala, but code in the class body is not part of a constructor
<br/>
Primary constructor code (if any) must be in an `init` block

```kotlin
class Foo(
    val bar: String, // This is a val property of the class
    var baz: Int, // This is a var property of the class
    greeting: String = "Hello from constructor" // Constructor parameter, not a property. Default values allowed.
) {
    init {
        println(greeting)
    }
}
Foo("bar", 0)
```

---

# Kotlin 102 -- OOP

## Secondary `constructor`s

More constructors can be added to a class, but them:
1. Must call another constructor
2. The primary constructor must be in its *delegation calls chain*

Call to another constructor is performed using `:`

```kotlin
class Foo(val bar: String) {
    constructor(longBar: Long) : this("number ${longBar.toString()}")
    constructor(intBar: Int) : this(intBar.toLong())
}
Foo(1).bar // number 1
```

The primary constructor can be written in a longer form with the `constructor` keyword as well
```kotlin
class Foo constructor(val bar: String) // OK
```

---

# Kotlin 102 -- OOP

## Nullability and `lateinit`

It is possible that some `var` property needs to get initialized after the object construction:
```kotlin
class Son(val: Father)
class Father(var son: Son) // Impossible to build either
```
Solution 1: allow nullability (**BAD**)
```kotlin
class Son(val father: Father)
class Father(var son: Son? = null)
val father = Father()
val son = Son(father)
father.son = son
father.son.father // error, needs ?.
```
Solution 1: take responsibility from the compiler (*less bad*)
```kotlin
class Son(val father: Father)
class Father { lateinit var son: Son } // lateinit: I will initialize it later, stay cool
val father = Father()
father.son // UninitializedPropertyAccessException: lateinit property son has not been initialized
val son = Son(father)
father.son = son
father.son.father // OK!
```

---

# Kotlin 102 -- OOP

> **Design and document for inheritance or else prohibit it**<br/>
> *J. Bloch, Effective Java, Item 17*

## Closed hierarchies and `open`

Kotlin enforces EJ-17 by design: all classes are final if the keyword `open` is not specified
```kotlin
class A
class B : A() // error: this type is final, so it cannot be inherited from
open class A
class B : A() // OK
```
As in Scala, *the constructor of the superclass must be called at extension site*
<br/>
Differently than Scala, such invocatin *always requires parentheses*

---

# Kotlin 102 -- OOP

## `abstract` vs. `open`

The same effect of `open` can be achieved with `abstract`:
```kotlin
abstract class A
class B : A() // Perfectly fine
```
With abstract, however, the superclass cannot be created
<br/>
(and it should have actual `abstract` memebers anyway)
```kotlin
open class Open
abstract class Abstract
class FromOpen : Open()
class FromAbstract : Abstract()
FromAbstract() // OK
FromOpen() // OK
Open() // OK
Abstract() // error: cannot create an instance of an abstract class
```

---

# Kotlin 102 -- OOP

## Singleton `object`s

Same as Scala, but with explicit `companion`s
<br/>
In Scala
```scala
class A
object A // Same file and same name identify a companion
```
In Kotlin
```kotlin
class A {
    companion object // Companions are inner to classes 
}
A // refers to A.Companion
object A // This is an independent object
A // refers to the previously defined object
```

---

# Kotlin 102 -- OOP

## Information hiding

Simpler than Scala, more coherent than Java
* `public` -- default visibility, visible everywhere (API)
* `internal` -- visible to everything in this *"module"*
    * module $\Rightarrow$ a set of Kotlin files compiled together
* `protected` -- visible to subclasses (but *not* to other members of the package)
* `private` -- visible inside this class and its members

---

# Kotlin 102 -- OOP

## Visibility control

```kotlin
class Visibility internal constructor( // constructor is required to apply visibility restrictionss
    private val id: Int // Same as Scala
) { 
    protected var state = 0
        private set // visibility restriction for properties in get/set methods
}
```

---

# Kotlin 102 -- OOP Conventions

## Equality, hashing, string version

Same as Java, but for equality:
* `==` calls `equals`
* Java's stack variable comparison (`==`) is Kotlin's `===`

Kotlin does not suffer of Scala's equality issues
(no automatic conversion of types)

```scala
val a: Int = 1
val b: Long = a
a == b // true
a equals b // false O_O
```
Kotlin:
```kotlin
val a: Int = 1
val b: Long = a // error: type mismatch: inferred type is Int but Long was expected
val b: Long = a.toLong()
a == b // error: operator '==' cannot be applied to 'Int' and 'Long'
a.toLong() == b // true
a == b.toInt() // true
```

---

# Kotlin 102 -- OOP Conventions

## `infix` calls

Kotlin is less permissive than scala:
* In Scala, every instance method with a single parameter can be invoked as infix operator:
```scala
1 equals 1 // infix invocation of 1.equals(1)
```
* In Kotlin, this is not allowed:
```kotlin
1 equals 1 // error: 'infix' modifier is required on 'equals' in 'kotlin.Int'
```
* Kotlin requires that the `infix` keyword for a method to be usable as infix
* `infix` functions have *lower precedence* than operators

```kotlin
class Infix {
    infix fun andThen(s: String) = "in... $s ...fix!"
}
Infix() andThen "Foo" // in... Foo ...fix!
Infix() andThen "Foo" + "Bar" {{<comment_frag " // in... FooBar ...fix" >}}
Infix() andThen "Foo" + "Bar" + Infix() andThen "Baz" {{<comment_frag " // error: unresolved reference: andThen (searched in String)" >}}
```

---

# Kotlin 102 -- OOP Conventions

## Operator creation

In Scala, operator names are valid method names, and prefix and infix calls are automatic:
* Very much the whole language philosophy: few concepts, high scalability
* Easy to abuse, degenerating to esoteric operators
    * Especially when software is written by people with different background
```scala
executer(:/(host, port) / target << reqBody >- { fromRespStr }) // Using Databinder Dispatch
val graph = Graph((jfc ~+#> fra)(Any()), (fra ~+#> dme)(Any()) // Using ScalaGraph
```
Operators are succint, but cryptic, and their meaning changes with context

This has been a source of cricism, Kotlin **does not allow to define custom operators**
* At most, back-ticked names, but some characters are disallowed (`>`, `/`, `:`, etc.)
* Clumsy, defies the reason why one would use them (terse and succint code)

```kotlin
class A { infix fun `~+#-`(other: A) = "I'm an arcane operator" }
A() `~+#-` A() // I'm an arcane operator
```

---

# Kotlin 102 -- OOP Conventions

## Operator overloading

Kotlin allows for a limited set of operators to be defined/overloaded
* Method names must match a convention
* Methods must be annotated with the `operator` keyword

```kotlin
class Complex(val real: Double, val imaginary: Double) {
    operator fun plus(other: Complex) = Complex(real + other.real, imaginary + other.imaginary)
    operator fun plus(other: Double) = plus(Complex(other, 0.0))
    override fun toString() = real.toString() + when {
        imaginary == 0.0 -> ""
        imaginary > 0.0 -> "+${imaginary}i"
        else -> "${imaginary}i"
    }
}
Complex(1.0, 1.0) + 3.4 // 4.4+1.0i
```

---

# Kotlin 102 -- OOP Conventions

## Unary Operator overloading table

| Expression | Method Name | Translation |
|------------|:-----------:|-------------|
| `+x`       | `unaryPlus`     | `x.unaryPlus()` |
| `-x`       | `unaryMinus`    | `x.unaryMinus()` |
| `++x`      | `inc`           | `x.inc().also { x = it }` |
| `x++`      | `inc`           | `x.also { x = it.inc() }` |
| `--x`      | `dec`           | `x.dec().also { x = it }` |
| `x--`      | `dec`           | `x.also { x = it.dec() }` |
| `!x`       | `not`           | `x.not()`    |
| `x()`      | `invoke`           | `x.invoke()`    |

Function invocation is an operator and can be overloaded!
<br/>
This will turn useful in future...

---

# Kotlin 102 -- OOP Conventions

## Binary Operator overloading: arithmetic

| Expression | Method Name | Translation |
|------------|:-----------:|-------------|
| `x + y` | `plus` | `x.plus(y)` |
| `x - y` | `minus` | `x.minus(y)` |
| `x * y` | `times` | `x.times(y)` |
| `x / y` | `div` | `x.div(y)` |
| `x % y` | `rem` | `x.rem(y)` |

---

# Kotlin 102 -- OOP Conventions

## Binary Operator overloading: assignment

| Expression | Method Name | Translation |
|------------|:-----------:|-------------|
| `x += y` | `plusAssign` | `x.plusAssign(y)` |
| `x -= y` | `minusAssign` | `x.minusAssign(y)` |
| `x *= y` | `timesAssign` | `x.timesAssign(y)` |
| `x /= y` | `divAssign` | `x.divAssign(y)` |
| `x %= y` | `remAssign` | `x.remAssign(y)` |

* Assignment functions *can be defined only if their arithmetic equivalent is undefined*.
* If an aritmetic operator `op` is defined, the compiler infers the assign version as:
    * `a op= b` $\Rightarrow$ `a = a op b`


---

# Kotlin 102 -- OOP Conventions

## Binary Operator overloading: comparison

| Expression | Method Name | Translation |
|------------|:-----------:|-------------|
| `x == y` | `equals` | `x?.equals(y) ?: (b === null)` |
| `x != y` | `equals` | `!(x?.equals(y) ?: (b === null))` |
| `x > y` | `compareTo` | `x.compareTo(y) > 0` |
| `x < y` | `compareTo` | `x.compareTo(y) < 0` |
| `x >= y` | `compareTo` | `x.compareTo(y) >= 0` |
| `x <= y` | `compareTo` | `x.compareTo(y) <= 0` |

---

# Kotlin 102 -- OOP Conventions

## Binary Operator overloading: others

| Expression | Method Name | Translation |
|------------|:-----------:|-------------|
| `x..y` | `rangeTo` | `x.rangeTo(y)` |
| `x in y` | `contains` | `y.contains(x)` |
| `x !in y` | `contains` | `!y.contains(x)` |
| `x[y]` | `get` | `x.get(y)` |
| `x(y)` | `invoke` | `x.invoke(y)` |

---

# Kotlin 102 -- OOP Conventions

## Ternary Operator overloading

| Expression | Method Name | Translation |
|------------|:-----------:|-------------|
| `x[y, z]` | `get` | `x.get(y, z)` |
| `x[y] = z` | `set` | `x.set(y) = z` |
| `x(y, z)` | `invoke` | `x.invoke(y, z)` |

---

# Kotlin 102 -- OOP Conventions

## n-ary Operator overloading

| Expression | Method Name | Translation |
|------------|:-----------:|-------------|
| `x[y, ..., z]` | `get` | `x.get(y, ..., z)` |
| `x[y, ..., z] = a` | `set` | `x.set(y, ..., z) = a` |
| `x(y, ..., z)` | `invoke` | `x.invoke(y, ..., z)` |

---

# Kotlin 103 -- Generics

## Compared with Java and Scala

Kotlin's type system supports generics
* Handier than Java's
* **way** less powerful than Scala's
* No higher kinded types (they are in Scala)
* No type lambdas (they are in Scala)
* Declaration-site variance (absent in Java)
* Generic type reification via inlining (not found in Java nor Scala)
```scala
trait Functor[F[_]] // There is no Kotlin equivalent for these lines
type MapFunctor = Functor[({ type T[A] = Map[Int, A] })#T]
```

---

# Kotlin 103 -- Generics

## Base syntax

Syntax similar to Java generics

```kotlin
class Foo<A, B : CharSequence>
fun <T : Comparable<T>> maxOf3(first: T, second: T, third: T): T = when {
    first >= second && first >= third -> first
    second >= third -> second
    else -> third
}
```

* type upper bounds can be specified with `:`
* if no bound is specified, the generic is *nullable*!

```kotlin
fun <T> className(receiver: T) = receiver::class.simpleName
// error: expression in a class literal has a nullable type 'T', use !! to make the type non-nullable
```

---

# Kotlin 103 -- Generics

## `where`

In case multiple bounds are present, the definition can become cumbersome
<br/>
Kotlin provides a `where` keyword to specify type bounds separately from the rest of the signature

```kotlin
// From an actual Alchemist interface
interface NavigationStrategy<T, P, A, L, R, N, E>
    where P : Position<P>, P : Vector<P>,
          A : GeometricTransformation<P>,
          L : ConvexGeometricShape<P, A>,
          N : ConvexGeometricShape<P, A> {
// Interface content, if any
}

// Function syntax
fun <T, P, A, L, R, N, E> navigationStrategy()
    where P : Position<P>, P : Vector<P>,
          A : GeometricTransformation<P>,
          L : ConvexGeometricShape<P, A>,
          N : ConvexGeometricShape<P, A> = TODO()
```
---

# Kotlin 103 -- Generics

## Variance and type projection

Kotlin supports (co/contro)variance using:
* `<out T>` to mark covariance (similar to Java's `<? extends T>`)
* `<in T>` to mark controvariance (similar to Java's `<? super T>`)
* `<*>` to mark that only the bound is known for the type (similar to Java's `<?>`)

Type variant in Kotlin is expressed *at declaration site*!
* In Java type variance is only for methods
* In Kotlin type variance is only for classes and interfaces

```kotlin
interface ProduceAndConsume<in X, out Y> {
    fun consume(x: X): Any = TODO(){{<comment_frag " // OK" >}}
    fun consume2(y: Y): Any = TODO(){{<comment_frag " // type parameter Y is declared as 'out' but occurs in 'in' position in type Y" >}}
    fun produce(): Y = TODO(){{<comment_frag " // OK" >}}
    fun produce2(): X = TODO(){{<comment_frag " // error: type parameter X is declared as 'in' but occurs in 'out' position in type X" >}}
}
```

---

# Kotlin 103 -- Generics

## Type reification

Generics at runtime can be dealt with two strategies:
* **erasure**: generic information is used by the compiler, but it's discarded at runtime
    * Java / Scala
* **monomorphization**: concrete type are emitted when generic types are actually used
    * Rust / C#

Delicate balance between executable size, performance, and usability

Kotlin uses erasure, but allows to control inlining via the `inline` keyword.
<br>
In inlined functions, *types can be locally monomorphized*!
<br>
Local monomorphization is expressed with the `reified` keyword.


---

# Kotlin 103 -- Generics

## Type reification example

```kotlin
inline fun <reified T> checkIsType(a: Any): T = a is T // instance check on a generic!
checkIsType<Long>(1) // false
checkIsType<Long>(1L) // true
```

Note on Java interoperability:
* `inline` functions get inlined if the caller is Kotlin-compiled code,
they don't if they are called by other bytecode-targeting compilers (`javac`, `scalac`...)
* `reified` types *requires* inlining to perform the local monorphization:
the function code is *copied* on call site, and the compiler must know how to do it

$\Rightarrow$ Can't be used if interoperability is a concern
* or a wrapper must be provided

---

# Kotlin 103 -- Collections

Similar to Scala, but based (for the JVM target) on the Java implementation
* No `toJava()`/`toScala()` equivalent
* `List`, `Set`, `Map` are *unmodifiable* but not guaranteed *immutable*
    * e.g., at runtime, `List` may be backed by an `ArrayList`
    * clients calling from Java will see mutable collections
    * Under the JVM, the immutable interfaces are erased at runtime
* Mutable collections are available via `Mutable`(`List`/`Set`/`Map`)
* As in Scala, invocation of functional manipulation on collections returns a new collection
* Differently than Scala, when a collection is returned, the type is usually `List`
    * Type is lost, no higher kinded types in Kotlin to express it
* `Sequence`s prevent a collection creation at each step
* `Flow`s represent collections that are processed in parallel
* Creation usually via functions `flowOf`/`listOf`/`mapOf`/`sequenceOf`/`setOf`

---

# Kotlin 201 -- Advanced OOP

## Data classes

Very similar to Scala's `case class`es:
* inheritance prohibited (Scala allows non-`case` classes to inherit from `case` classes)
* `equals`, `hashCode`, `toString` for free
* `copy` function, to be used to generate new immutable objects
* `component1`, `component2`, ..., `componentN` functions, called in case of destructuring

`Pair` and `Triple` provided by the standard library
<br/>
(`Tuple4`, `Tuple5`, and so on are not in standard library as opposed as Scala)

---

# Kotlin 201 -- Advanced OOP

## Destructuring declarations

If a class has `operator` functions named called `componentX` with `X` an integer from `1`,
they can be "destructured".
<br>
This feature is *way* less powerful than Scala's pattern matching.

```kotlin
// to is an inline function that creates a Pair, similar to Scala's ->
val ferrari2021 = "Ferrari" to Pair("Sainz", "Leclerc")
val (team, lineup) = ferrari2021
team // "Ferrari"
lineup // Sainz to Leclerc
val (driver1, driver2) = lineup
driver1 // Sainz
driver2 // Leclerc
```
```kotlin
class A {
    operator fun component1() = 1
    operator fun component2() = 2
    operator fun component3() = 3
}
val (a, b, c) = A()
"$a$b$c"
```

---

# Kotlin 201 -- Advanced OOP

## Sealed hierarchies

Similar to Scala's `sealed trait`s:
* `class`es, not supported for `interface`s
* subtypes must be defined inside the sealed class
* sealed hierarchies proved *exhaustive checking* inside `where` clauses

```kotlin
sealed class Booze {
    class Rum : Booze()
    class Whisky : Booze()
    class Vodka : Booze()
}
fun goGetMeA(beverage: Booze) = when (beverage) {
    is Booze.Rum -> "Diplomatico"
    is Booze.Whisky -> "Caol Ila"
    is Booze.Vodka -> "Zubrowka"
}
goGetMeA(Booze.Rum())
```

---

# Kotlin 201 -- Advanced OOP

## Nested and inner classes

* Nesting a class inside another does not allow access to outer members
    * It's equivalent to a Java's `static` inner class
* To create an inner class, the `inner` modifier must be explicit

```kotlin
class Outer {
    private val readMeIfYouCan = 1
    class Nested {
        init { println(readMeIfYouCan) } // error: unresolved reference: readMeIfYouCan
    }
}
```
```kotlin
class Outer { class Nested }
Outer.Nested() // OK
```
```kotlin
class Outer {
    private val readMeIfYouCan = 1
    inner class Inner {
        init { println(readMeIfYouCan) } // ok
    }
}
Outer.Inner() // error: constructor of inner class Inner can be called only with receiver of containing class
Outer().Inner() // OK
```
---

# Kotlin 201 -- Advanced OOP

## Enum classes

Same as Java, with Kotlin syntax

## Object expressions

`object` expressions replace anonymous classes

```kotlin
interface Test {
    fun first(): Unit
    fun second(): Unit
}
object : Test {
    override fun first() { }
    override fun second() { }
}
```
---

# Kotlin 201 -- Advanced OOP

## Type aliases
* Types can be aliased
* Only at the top level
* Type aliases in kotlin **are not** Scala's `type` definitions
* Kotlin has no equivalent of Scala's `type`

```kotlin
typealias Drivers = Pair<String, String>
typealias Team = Pair<String, Drivers>
typealias Formula1 = Map<String, Team>
val `f1 2020` = mapOf(
    Team("Ferrari", Drivers("Vettel", "Leclerc")),
    Team("RedBull", Drivers("Versbatten", "Albon")),
    Team("Merdeces", Drivers("Hamilton", "Bottas")),
)
`f1 2020` // Map<String, Pair<String, Pair<String, String>>>
```

---

## extended OOP
delegation and `by`
contract implementation by delegation (favor composition over inheritance)
delegated properties and variables
delegated properties https://kotlinlang.org/docs/reference/delegated-properties.html
lazy
observable
delegation via map (mutability via MutableMap)
implementing a custom delegate for properties

## functional kotlin
function types
function types and nullability
lambda expressions
trailing lambdas
implicit single parameters
closures
return from lambda
return at label
destructuring in lambdas
anonymous functions (rarely used)
invoke convention
inline functions

## extensions
extension functions vs implicits
extension properties (no backing field)
nullable receivers
companion extensions
generic extensions
extensions resolution
extension members (scope control)
visibility of private members

## extensions + functional
function types with receiver

## standard library
let / run / apply / with
arrays
primitive arrays
ranges

# left out
enum classes
noinline
crossinline
coroutines
java interoperability
inline classes -- mentioned and skipped, ref to https://kotlinlang.org/docs/reference/inline-classes.html

## DSLs in Kotlin (type-safe builders)