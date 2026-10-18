# Anonymous Classes to Lambda — Migration Guide (Java 8+)

## Overview
Anonymous inner classes implementing single-abstract-method (SAM) interfaces
(also called functional interfaces) can be replaced by concise lambda expressions
in Java 8+. Method references further reduce boilerplate for common patterns.

## Legacy Anonymous Class Patterns (Java 7 and earlier)
```java
// Old Runnable — anonymous class
Thread t = new Thread(new Runnable() {
    @Override
    public void run() {
        System.out.println("Running in thread");
    }
});
t.start();

// Old Comparator — anonymous class
List<String> names = Arrays.asList("Charlie", "Alice", "Bob");
Collections.sort(names, new Comparator<String>() {
    @Override
    public int compare(String a, String b) {
        return a.compareTo(b);
    }
});

// Old ActionListener (Swing)
button.addActionListener(new ActionListener() {
    @Override
    public void actionPerformed(ActionEvent e) {
        handleClick(e);
    }
});

// Old Callable
ExecutorService executor = Executors.newFixedThreadPool(4);
Future<String> future = executor.submit(new Callable<String>() {
    @Override
    public String call() throws Exception {
        return computeResult();
    }
});
```

## Modern Java 8+ Lambda & Method Reference Patterns
```java
// Lambda — Runnable
Thread t = new Thread(() -> System.out.println("Running in thread"));
t.start();

// Java 21 Virtual Thread (best practice)
Thread.ofVirtual().start(() -> System.out.println("Virtual thread running"));

// Lambda — Comparator
List<String> names = Arrays.asList("Charlie", "Alice", "Bob");
names.sort(String::compareTo);                 // method reference
names.sort(Comparator.naturalOrder());         // built-in
names.sort(Comparator.comparing(String::length).thenComparing(Comparator.naturalOrder()));

// Lambda — ActionListener
button.addActionListener(e -> handleClick(e));
button.addActionListener(this::handleClick);   // method reference

// Lambda — Callable  
ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor(); // Java 21
Future<String> future = executor.submit(() -> computeResult());

// Predicate, Function, Consumer — common functional interfaces
Predicate<String> isLong = s -> s.length() > 10;
Function<String, Integer> toLength = String::length;
Consumer<String> printer = System.out::println;

// BiFunction
BiFunction<Integer, Integer, Integer> add = (a, b) -> a + b;

// Supplier
Supplier<List<String>> listFactory = ArrayList::new;
```

## Method Reference Types
```java
// Static method reference
Function<String, Integer> parse = Integer::parseInt;

// Instance method reference on arbitrary instance of type
Function<String, String> upper = String::toUpperCase;

// Instance method reference on specific instance
String prefix = "Hello";
Predicate<String> startsWith = prefix::startsWith;

// Constructor reference
Supplier<ArrayList<String>> newList = ArrayList::new;
Function<Integer, int[]> arrayFactory = int[]::new;
```

## Multi-line Lambda (Statement Body)
```java
// When lambda body needs multiple statements
Comparator<Employee> complex = (e1, e2) -> {
    int dept = e1.department().compareTo(e2.department());
    return dept != 0 ? dept : e1.name().compareTo(e2.name());
};
```

## Key Benefits
- Lambda expressions reduce boilerplate by 60-80% vs anonymous classes
- Method references express intent clearly (what, not how)
- Enables functional composition with Predicate.and(), Function.andThen()
- Better JIT optimization for lambdas vs anonymous class objects
