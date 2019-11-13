# java.lang.instrument.ClassFileTransformer

<!-- TOC -->

- [1. 唯一的方法transform](#1-唯一的方法transform)
- [2. 外部处理](#2-外部处理)
    - [2.1. 两种类型的transformer（从外部决定）](#21-两种类型的transformer从外部决定)
    - [2.2. 被调用的三种情况（从外部决定）](#22-被调用的三种情况从外部决定)
    - [2.3. 多个transformer的调用顺序（从外部决定）](#23-多个transformer的调用顺序从外部决定)
- [3. 内部处理](#3-内部处理)
    - [3.1. 对于classfileBuffer的理解](#31-对于classfilebuffer的理解)
    - [3.2. 返回值](#32-返回值)
    - [3.3. 返回值的合法性校验](#33-返回值的合法性校验)
    - [3.4. 抛出异常情况的处理](#34-抛出异常情况的处理)

<!-- /TOC -->

```java
public interface ClassFileTransformer
```

An agent provides an implementation of this interface in order to transform **class files**. The transformation occurs before the class is defined by the JVM.

Note the term **class file** is used to mean **a sequence of bytes in class file format, whether or not they reside in a file**.<sub>这里给出了class file一个宽泛的定义，只要是符合class file format的bytes就行，并不要求这些bytes是一定存在于一个文件当中</sub>

## 1. 唯一的方法transform

```java
byte[] transform(ClassLoader loader,
                 String className,
                 Class<?> classBeingRedefined,
                 ProtectionDomain protectionDomain,
                 byte[] classfileBuffer)
          throws IllegalClassFormatException
```

The implementation of this method may transform the supplied class file and return a new replacement class file.

## 2. 外部处理

### 2.1. 两种类型的transformer（从外部决定）

> 是由`Instrumentation.addTransformer(ClassFileTransformer,boolean)`决定的

There are two kinds of transformers, determined by the `canRetransform` parameter of `Instrumentation.addTransformer(ClassFileTransformer,boolean)`:

- (1) **retransformation capable transformers** that were added with `canRetransform` as `true`
- (2) **retransformation incapable transformers** that were added with `canRetransform` as `false` or where added with `Instrumentation.addTransformer(ClassFileTransformer)`

### 2.2. 被调用的三种情况（从外部决定）

> 是由外部的调用方法（`ClassLoader.defineClass`、`Instrumentation.redefineClasses`和`Instrumentation.retransformClasses`）决定的

Once a transformer has been registered with `addTransformer`, the transformer will be called for **every new class definition**<sub>注：第一种情况，新定义的类（第一次加载的类）</sub> and **every class redefinition**<sub>注：第二种情况，重新定义的类（第二次或更多次加载的类）</sub>. **Retransformation capable transformers** will also be called on **every class retransformation**<sub>注：第三种情况，可以进行“变换、转换”的类</sub>. For **retransformations**, the **retransformation incapable transformers** are not called, instead the result of the previous transformation is reused. In all other cases, this method is called.

The request for **a new class definition**<sub>注：第一种情况</sub> is made with `ClassLoader.defineClass` or its native equivalents.

The request for **a class redefinition**<sub>注：第二种情况</sub> is made with `Instrumentation.redefineClasses` or its native equivalents.

The request for **a class retransformation**<sub>注：第三种情况</sub> is made with `Instrumentation.retransformClasses` or its native equivalents.

The transformer is called during the processing of the request, before the class file bytes have been verified or applied<sub>注：这里应该是表达“先处理，再验证”的思路</sub>.

### 2.3. 多个transformer的调用顺序（从外部决定）

> 同样是由`Instrumentation.addTransformer(ClassFileTransformer,boolean)`添加的顺序决定的

When there are multiple transformers<sub>注：当有多个transformer的情况，会作为chain进行处理，上一个的输出，作为下一个的输入</sub>, transformations are composed by chaining the transform calls. That is, the byte array returned by one call to transform becomes the input (via the `classfileBuffer` parameter) to the next call.

Transformations are applied in the following order:

- (1) Retransformation incapable transformers
- (2) Retransformation incapable native transformers
- (3) Retransformation capable transformers
- (4) Retransformation capable native transformers

> 我不明白，native transformers是什么

Within each of these groupings, transformers are called in the order registered. Native transformers are provided by the `ClassFileLoadHook` event in the Java Virtual Machine Tool Interface).

## 3. 内部处理

### 3.1. 对于classfileBuffer的理解

The input (via the `classfileBuffer` parameter) to the first transformer is:

- for **new class definition**, the bytes passed to `ClassLoader.defineClass`
- for **class redefinition**, `definitions.getDefinitionClassFile()` where `definitions` is the parameter to `Instrumentation.redefineClasses`
- for **class retransformation**, the bytes passed to **the new class definition** or, **if redefined, the last redefinition, with all transformations made by retransformation incapable transformers reapplied automatically and unaltered**; for details see `Instrumentation.retransformClasses`

### 3.2. 返回值

If the implementing method determines that **no transformations are needed, it should return `null`**. Otherwise, it should create a **new `byte[]` array**, copy the input `classfileBuffer` into it, along with all desired transformations, and return the new array. The input `classfileBuffer` must not be modified.

### 3.3. 返回值的合法性校验

In the retransform and redefine cases, the transformer must support the redefinition semantics: if a class that the transformer changed during initial definition is later retransformed or redefined, the transformer must insure that the second class output class file is a legal redefinition of the first output class file.

### 3.4. 抛出异常情况的处理

If the transformer throws an exception (which it doesn't catch), subsequent transformers will still be called and the load, redefine or retransform will still be attempted. Thus, throwing an exception has the same effect as returning `null`.

To prevent unexpected behavior when unchecked exceptions are generated in transformer code, a transformer can catch `Throwable`. If the transformer believes the `classFileBuffer` does not represent a validly formatted class file, it should throw an `IllegalClassFormatException`; while this has the same effect as returning `null`. it facilitates the logging or debugging of format corruptions.

