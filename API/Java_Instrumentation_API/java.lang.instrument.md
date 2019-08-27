# java.lang.instrument package

## What is Instrumentation

刚开始的时候，我接触到`instrumentation`这个单词，感到很难理解。

首先，我们抛开Java的语言环境，从“英语单词”的语言环境，来看一下作为单词的instrument和instrumentation究竟是什么意思。

instrument

- a musical instrument, for example a piano or a guitar 一件乐器（音乐视角）
- a tool or device used for a particular task, especially for delicate or scientific work 一个工具；一个设备（科学视角）
- a device used for measuring speed, distance, temperature, etc. in a vehicle or on a piece of machinery （车辆、机器的）一个仪器，一个仪表 （交通工具视角）
- something that is used by sb in order to achieve sth; a person or thing that makes sth happen 促成某事的人（或事物）；手段 （人的视角）

通过上面的4条解释，我们或许可以这样理解instrument：它是一个有形的、实实在在存在的东西，它能够帮助我们完成某种事情。例如，在音乐领域，instrument可以是一件乐器，帮助我们演奏音乐；在科学领域，instrument可以是一个设备，帮助我们完成某种科学工作；在交通工具领域，instrument可以是一个仪表，可以帮助我们监测行驶速度。总的来说，instrument就是一件可以帮助我们完成某种事情的物件。

> instrument也可以作为动词，表示“为某种东西装配工具”。instrument: equip (something) with measuring instruments. 例句：engineers have instrumented rockets to study the upper atmosphere

instrumentation

- the instruments that are used to perform a piece of music （演奏一首音乐的）所有乐器
- a set of instruments used in operating a vehicle or a piece of machinery （一套）仪器，仪表

从上面的2条解释，我们或许可以这样理解instrumentation：它是多个instrument的集合。换句话说，instrument是一个工具，而instrumentation是一组工具（类似于“工具箱”）。

> Instrumentation is a collective term for measuring instruments that are used for indicating, measuring and recording physical quantities such as flow, temperature, level, distance, angle, or pressure.

接下来，我们进入到“计算机编程”的语言环境

In the context of **computer programming**, instrumentation refers to an ability to monitor or measure the level of a product's performance, to diagnose errors, and to write trace information.

最后，我们进入到“Java”的语言环境

Package `java.lang.instrument` provides services that allow **Java programming language agents** to instrument programs running on the JVM. The mechanism for instrumentation is modification of the byte-codes of methods<sub>注：instrumentation的机制就是修改method中的bytecode（字节码）</sub>.

An agent is deployed as a JAR file<sub>注：agent可以抽象的理解，也可以具体的理解。从抽象的角度来说，agent是一个概念，它表示我们要达到某种目的的中间手段，其中“某种目的”是根据场景不同而不同，而“中间手段”就表示通过修改method的bytecode来实现“某种目的”。从具体的角度来说，agent的现实存在形式是一个jar文件</sub>. An attribute in the JAR file manifest specifies the agent class which will be loaded to start the agent<sub>注：这里讲的是jar文件里面包含的MANIFEST.MF文件</sub>. For implementations that support a command-line interface, an agent is started by specifying an option on the command-line<sub>注：第一种启动方式，是通过command-line的方式，是在JVM启动之前加载</sub>. Implementations may also support a mechanism to start agents some time after the VM has started<sub>注：第二种启动方式，是在JVM已经启动之后再加载</sub>. For example, an implementation may provide a mechanism that allows a tool to attach to a running application, and initiate the loading of the tool's agent into the running application. The details as to how the load is initiated, is implementation dependent.

## Command-Line Interface

An implementation is not required to provide a way to start agents from the command-line interface. On implementations that do provide a way to start agents from the command-line interface, an agent is started by adding this option to the command-line:

```bash
-javaagent:jarpath[=options]
```

`jarpath` is the path to the agent JAR file. `options` is the agent options. This switch may be used multiple times on the same command-line, thus creating multiple agents. More than one agent may use the same jarpath. An agent JAR file must conform to the JAR file specification.

The manifest of the agent JAR file must contain the attribute `Premain-Class`. The value of this attribute is the name of the agent class. The agent class must implement a `public static premain` method similar in principle to the main application entry point. After the Java Virtual Machine (JVM) has initialized, each `premain` method will be called in the order the agents were specified, then the real application `main` method will be called. Each `premain` method must return in order for the startup sequence to proceed.<sub>注：这里需要做一个实验，让一个premain方法sleep一会儿，看看是否会进入第二个premain</sub>

The `premain` method has one of two possible signatures. The JVM first attempts to invoke the following method on the agent class:

```java
public static void premain(String agentArgs, Instrumentation inst);
```

If the agent class does not implement this method then the JVM will attempt to invoke:

```java
public static void premain(String agentArgs);
```

The agent class may also have an `agentmain` method for use when the agent is started after VM startup. When the agent is started using a command-line option, the `agentmain` method is not invoked.

The agent class will be loaded by the system class loader (see `ClassLoader.getSystemClassLoader`). This is the class loader which typically loads the class containing the application `main` method. The `premain` methods will be run under the same security and classloader rules as the application `main` method. There are no modeling restrictions on what the agent `premain` method may do. Anything application `main` can do, including creating threads, is legal from `premain`.

Each agent is passed its agent `options` via the `agentArgs` parameter. The agent `options` are passed as a single string, any additional parsing should be performed by the agent itself.

```bash
-javaagent:jarpath[=options]
```

```java
public static void premain(String agentArgs, Instrumentation inst);
```

If the agent cannot be resolved (for example, because the agent class cannot be loaded, or because the agent class does not have an appropriate `premain` method), the JVM will abort. If a `premain` method throws an uncaught exception, the JVM will abort.

## Starting Agents After VM Startup

An implementation may provide a mechanism to start agents sometime after the the VM has started. The details as to how this is initiated are implementation specific but typically the application has already started and its `main` method has already been invoked. In cases where an implementation supports the starting of agents after the VM has started the following applies:

- (1) The manifest of the agent JAR must contain the attribute `Agent-Class`. The value of this attribute is the name of the agent class.

- (2) The agent class must implement a `public static agentmain` method.

- (3) The system class loader (`ClassLoader.getSystemClassLoader`) must support a mechanism to add an agent JAR file to the **system class path**<sub>注：system class path是指什么呢？</sub>.

The agent JAR is appended to the system class path. This is the class loader that typically loads the class containing the application `main` method. The agent class is loaded and the JVM attempts to invoke the `agentmain` method. The JVM first attempts to invoke the following method on the agent class:

```java
public static void agentmain(String agentArgs, Instrumentation inst);
```

If the agent class does not implement this method then the JVM will attempt to invoke:

```java
public static void agentmain(String agentArgs);
```

The agent class may also have an `premain` method for use when the agent is started using a command-line option. When the agent is started after VM startup the `premain` method is not invoked.

The agent is passed its agent options via the `agentArgs` parameter. The agent options are passed as a single string, any additional parsing should be performed by the agent itself.

The `agentmain` method should do any necessary initialization required to start the agent. When startup is complete the method should return. If the agent cannot be started (for example, because the agent class cannot be loaded, or because the agent class does not have a conformant `agentmain` method), the JVM will not abort. If the `agentmain` method throws an uncaught exception it will be ignored.

## Manifest Attributes



<sub>注：</sub>

## Reference

- [Java™ Platform, Standard Edition 8 API Specification](https://docs.oracle.com/javase/8/docs/api/)
- [Package java.lang.instrument](https://docs.oracle.com/javase/8/docs/api/java/lang/instrument/package-summary.html)