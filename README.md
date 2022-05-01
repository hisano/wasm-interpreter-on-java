# WebAssembly Interpreter on Java

WebAssembly Interpreter written in Java (Java 8, Pure Java, No library dependencies)

## Features (under development)

* Pure Java
* No library dependencies
* `wasm` file support
* Integer operations
* Control flow
* Functions
* Linear memory
 
## Example

```java
    byte[] wasmFileContent = Files.toByteArray(new File("add.wasm"));
    int result = new Interpreter(wasmFileContent).invoke("add", 1, 2);
    System.out.println("1 + 2 = " + result);
```

## How to build

```sh
$ ./mvnw install
```

## License

Apache License Version 2.0
