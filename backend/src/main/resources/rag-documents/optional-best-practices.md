# Optional Best Practices — Migration Guide (Java 8–21)

## Overview
`Optional<T>` (Java 8) is intended to express the potential absence of a return value.
The most dangerous misuse is calling `Optional.get()` without first calling `isPresent()`,
which throws `NoSuchElementException`. Java 9–11 added safer alternative methods.

## Anti-Pattern (Dangerous Usage)
```java
// WRONG: Direct get() without safety check — throws NoSuchElementException!
Optional<User> user = userRepo.findById(id);
String name = user.get().getName(); // ❌ throws if empty

// WRONG: Using Optional as method parameter (anti-pattern per JDK docs)
public void process(Optional<String> value) { ... } // ❌

// WRONG: Optional in fields (serialization issues)
private Optional<String> middleName; // ❌

// Redundant isPresent() check (verbose)
if (opt.isPresent()) {
    doSomething(opt.get());
}
```

## Safe Modern Usage (Java 9–21)
```java
// Safe extraction with orElse / orElseGet / orElseThrow
String name = userRepo.findById(id)
    .map(User::getName)
    .orElse("Anonymous");

// orElseGet — lazy evaluation (prefer when default creation is expensive)
String result = optional.orElseGet(() -> computeExpensiveDefault());

// orElseThrow — explicit exception semantics (Java 10+, no args = NoSuchElementException)
User user = userRepo.findById(id)
    .orElseThrow(() -> new UserNotFoundException("User not found: " + id));

// ifPresent — side effects only
optional.ifPresent(value -> logger.info("Found: {}", value));

// ifPresentOrElse (Java 9+)
optional.ifPresentOrElse(
    value -> process(value),
    () -> logger.warn("Value absent, skipping processing")
);

// filter + map chaining (functional style)
optional.filter(s -> !s.isBlank())
        .map(String::toUpperCase)
        .ifPresent(System.out::println);

// or() — provide alternative Optional (Java 9+)
Optional<User> result = primaryRepo.findById(id)
    .or(() -> fallbackRepo.findById(id));

// stream() — bridge Optional to Stream (Java 9+)
List<String> names = users.stream()
    .map(userRepo::findById)
    .flatMap(Optional::stream) // filters empties
    .map(User::getName)
    .toList();
```

## Key Rules
1. Never call `Optional.get()` without an `isPresent()` check (use orElseThrow instead)
2. Don't use Optional as fields or method parameters — only as return types
3. Prefer `orElseGet` over `orElse` when the default value is expensive to create
4. Use `flatMap` when chaining methods that return Optional
