# Java Stream API Improvements — Migration Guide (Java 16+)

## Overview
Java 16 introduced `Stream.toList()` as a direct replacement for the verbose
`Collectors.toList()` collector. Additional Stream improvements arrived in
Java 9–16: `Stream.ofNullable()`, `takeWhile()`, `dropWhile()`, and `iterate()`.

## Legacy Pattern (Java 8/11)
```java
// Verbose Collectors.toList()
List<String> filtered = items.stream()
    .filter(s -> s.startsWith("A"))
    .collect(Collectors.toList());

// Mutable list — unintended side effects
List<Integer> numbers = Stream.of(1, 2, 3)
    .collect(Collectors.toList());
numbers.add(99); // compiles but risky semantics

// Old stream processing
List<String> result = list.stream()
    .map(String::toUpperCase)
    .sorted()
    .collect(Collectors.toUnmodifiableList());
```

## Modern Java 16+ Pattern
```java
// Stream.toList() — concise, unmodifiable, Java 16+
List<String> filtered = items.stream()
    .filter(s -> s.startsWith("A"))
    .toList();

// Stream.ofNullable() — safe null handling (Java 9+)
Stream.ofNullable(possiblyNullValue)
    .map(String::toUpperCase)
    .forEach(System.out::println);

// takeWhile / dropWhile (Java 9+)
List<Integer> leading = Stream.of(1, 2, 3, 4, 5, 1)
    .takeWhile(n -> n < 4)
    .toList(); // [1, 2, 3]

// Improved iterate with predicate (Java 9+)
Stream.iterate(1, n -> n <= 10, n -> n * 2)
    .forEach(System.out::println);
```

## Collectors Modernization
```java
// Java 12+ - Collectors.teeing()
Map.Entry<Long, Long> evenOdd = numbers.stream()
    .collect(Collectors.teeing(
        Collectors.filtering(n -> n % 2 == 0, Collectors.counting()),
        Collectors.filtering(n -> n % 2 != 0, Collectors.counting()),
        Map::entry
    ));

// Grouping with downstream collectors
Map<String, List<Employee>> byDept = employees.stream()
    .collect(Collectors.groupingBy(Employee::department));
```

## Key Benefits
- `toList()` returns an unmodifiable list (safer by default)
- Eliminates 25+ chars of boilerplate per stream terminal
- `ofNullable` eliminates manual null checks before streaming
- `takeWhile`/`dropWhile` enable lazy short-circuit evaluation
