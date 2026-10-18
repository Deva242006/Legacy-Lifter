# Java 21 Pattern Matching — Migration Guide (JEP 441)

## Overview
Java 21 delivers Pattern Matching for switch (JEP 441) as a standard feature.
It eliminates boilerplate instanceof chains and legacy switch statements.

## Legacy Pattern (Java 8/11)
```java
// Old-style instanceof chains — verbose and error-prone
if (obj instanceof String) {
    String s = (String) obj;
    System.out.println(s.length());
} else if (obj instanceof Integer) {
    Integer i = (Integer) obj;
    System.out.println(i * 2);
}

// Old-style switch statement
switch (day) {
    case MONDAY:
    case FRIDAY:
        numLetters = 6;
        break;
    case TUESDAY:
        numLetters = 7;
        break;
    default:
        throw new IllegalStateException("Invalid day: " + day);
}
```

## Modern Java 21 Pattern (JEP 441)
```java
// Pattern Matching for instanceof (JEP 394, Java 16+)
if (obj instanceof String s) {
    System.out.println(s.length());
} else if (obj instanceof Integer i) {
    System.out.println(i * 2);
}

// Pattern Matching Switch (JEP 441, Java 21)
int numLetters = switch (day) {
    case MONDAY, FRIDAY -> 6;
    case TUESDAY        -> 7;
    case THURSDAY, SATURDAY -> 8;
    case WEDNESDAY      -> 9;
    case SUNDAY         -> 6;
};

// Sealed class exhaustive switch (type patterns)
String result = switch (shape) {
    case Circle c    -> "Circle with radius " + c.radius();
    case Rectangle r -> "Rectangle " + r.width() + "x" + r.height();
    case Triangle t  -> "Triangle with base " + t.base();
};
```

## Guarded Patterns
```java
// Java 21 — when clause for additional conditions
String format = switch (obj) {
    case Integer i when i > 0 -> "positive int: " + i;
    case Integer i            -> "non-positive int: " + i;
    case String s when !s.isBlank() -> "non-blank string: " + s;
    case null, default        -> "other: " + obj;
};
```

## Key Benefits
- Eliminates redundant cast after instanceof
- Switch expressions return values (no break needed)
- Compiler enforces exhaustiveness for sealed types
- Null safety: can handle null as a pattern case
