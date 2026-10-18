# Raw Types & Generics — Migration Guide (Java 5+)

## Overview
Raw types (unparameterized generic collections like `List`, `Map`, `Set` without
type parameters) suppress compile-time type safety checks and can cause
ClassCastException at runtime. They are a legacy holdover from pre-Java-5 code.

## Legacy Raw Type Patterns (Java 4 and earlier)
```java
// Raw type — no compile-time type checking
List items = new ArrayList();
items.add("Hello");
items.add(42);           // silently allows mixed types
String s = (String) items.get(1); // ❌ ClassCastException at runtime!

// Raw Map — no key/value type safety
Map map = new HashMap();
map.put("name", "Alice");
map.put("age", 30);
String name = (String) map.get("name"); // requires unsafe cast

// Raw Iterator
Iterator it = items.iterator();
while (it.hasNext()) {
    String str = (String) it.next(); // unchecked cast — dangerous
}

// Raw Comparable
class Employee implements Comparable {
    public int compareTo(Object other) { // Object — no type safety
        Employee e = (Employee) other;
        return this.name.compareTo(e.name);
    }
}
```

## Modern Parameterized Generic Patterns (Java 5+)
```java
// Properly parameterized — compile-time type safety
List<String> items = new ArrayList<>();
items.add("Hello");
// items.add(42); // ✅ Compile error — type mismatch

String s = items.get(0); // no cast needed!

// Generic Map with explicit types
Map<String, Object> props = new HashMap<>();
Map<String, Integer> scores = new LinkedHashMap<>();
scores.put("Alice", 95);
scores.put("Bob", 87);

// Enhanced for loop — no iterator boilerplate
for (String item : items) {
    System.out.println(item.toUpperCase());
}

// Generic Comparable
class Employee implements Comparable<Employee> {
    private final String name;
    
    @Override
    public int compareTo(Employee other) { // typed — no cast needed
        return this.name.compareTo(other.name);
    }
}
```

## Java 21 Best Practices — var + Generics
```java
// Use var for local type inference (Java 10+)
var employees = new ArrayList<Employee>();
var scores = new HashMap<String, Integer>();

// Wildcards for flexible APIs
public double sumScores(List<? extends Number> numbers) {
    return numbers.stream().mapToDouble(Number::doubleValue).sum();
}

// Bounded type parameters
public <T extends Comparable<T>> T max(List<T> items) {
    return items.stream().max(Comparator.naturalOrder()).orElseThrow();
}
```

## Collections Factory Methods (Java 9+)
```java
// Immutable collections — concise factory methods
List<String> immutableList = List.of("a", "b", "c");
Set<Integer>  immutableSet  = Set.of(1, 2, 3);
Map<String, Integer> immutableMap = Map.of("one", 1, "two", 2);

// Map.copyOf / List.copyOf — defensive copy
List<String> copy = List.copyOf(mutableList);
```

## Key Rules
1. Always parameterize generic types — never use raw `List`, `Map`, `Set`
2. Use `var` for local variables to reduce verbosity while preserving type safety
3. Use `List.of()` / `Map.of()` for unmodifiable collections (Java 9+)
4. Prefer bounded wildcards `<? extends T>` for flexible read-only parameters
