# Java Records (JEP 395) Best Practices & Refactoring Guide

## Overview
Records provide a compact syntax for declaring transparent, immutable data-carrier classes.

## Refactoring Rules
1. Replace POJOs / DTOs containing only private fields, getters, `equals()`, `hashCode()`, and `toString()` with `record RecordName(...) {}`.
2. Do not use Records for mutable domain entities (e.g. JPA @Entity classes).
3. Use compact constructors for validation:
   ```java
   public record UserDto(String name, int age) {
       public UserDto {
           Objects.requireNonNull(name);
           if (age < 0) throw new IllegalArgumentException();
       }
   }
   ```
4. Record components are implicitly `private final` and accessor methods match component names (e.g. `user.name()` instead of `user.getName()`).
