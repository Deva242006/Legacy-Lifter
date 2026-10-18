# Sealed Classes & Interfaces — Migration Guide (JEP 409, Java 17+)

## Overview
Sealed classes (JEP 409, Java 17 standard) restrict which classes can extend or
implement them. They work synergistically with pattern matching switch to create
closed type hierarchies with compiler-enforced exhaustiveness.

## Legacy Pattern (Java 8/11 — Abstract Classes)
```java
// Old open abstract class — any class anywhere can extend Shape
public abstract class Shape {
    public abstract double area();
}

public class Circle extends Shape {
    private final double radius;
    public Circle(double radius) { this.radius = radius; }
    public double area() { return Math.PI * radius * radius; }
}

public class Rectangle extends Shape {
    private final double width, height;
    public double area() { return width * height; }
}

// Consumer must handle unknown subtypes — cannot be exhaustive
double area = shape.area(); // works, but instanceof chain needed for type-specific logic
```

## Modern Java 17+ Sealed Class Pattern (JEP 409)
```java
// Sealed abstract class — only permits listed subclasses
public sealed abstract class Shape
        permits Circle, Rectangle, Triangle {
    public abstract double area();
}

// Permitted subclasses must be final, sealed, or non-sealed
public final class Circle extends Shape {
    private final double radius;
    public Circle(double radius) { this.radius = radius; }
    public double area() { return Math.PI * radius * radius; }
}

public final class Rectangle extends Shape {
    private final double width, height;
    public Rectangle(double width, double height) {
        this.width = width;
        this.height = height;
    }
    public double area() { return width * height; }
}

public final class Triangle extends Shape {
    private final double base, height;
    public double area() { return 0.5 * base * height; }
}
```

## Sealed Interface Pattern
```java
// Sealed interface — algebraic data types (ADT style)
public sealed interface Result<T>
        permits Result.Success, Result.Failure {

    record Success<T>(T value) implements Result<T> {}
    record Failure<T>(String error, Exception cause) implements Result<T> {}
}

// Pattern Matching Switch — exhaustive (compiler verified)
String message = switch (result) {
    case Result.Success<String> s -> "Got: " + s.value();
    case Result.Failure<String> f -> "Error: " + f.error();
};
```

## Abstract Class to Sealed Migration Steps
1. Add `sealed` modifier to the abstract class declaration
2. Add `permits Subclass1, Subclass2, ...` listing all known subclasses
3. Mark each permitted subclass as `final` (no further extension) or `sealed` (further restricted)
4. Replace `instanceof` chains with exhaustive `switch` expressions

## Key Benefits
- Compiler enforces exhaustiveness in switch expressions over sealed types
- Documents intent: this hierarchy is closed, no external subclasses allowed
- Enables more aggressive compiler optimizations
- Works naturally with Java Records for data-oriented programming
