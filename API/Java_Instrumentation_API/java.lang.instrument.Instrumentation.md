# Instrumentation

```java
public interface Instrumentation
```

This class provides services needed to instrument Java programming language code. Instrumentation is the addition of byte-codes to methods for the purpose of gathering data to be utilized by tools. Since the changes are purely additive, these tools do not modify application state or behavior.

There are two ways to obtain an instance of the `Instrumentation` interface:

- (1) When a JVM is launched in a way that indicates an agent class. In that case an `Instrumentation` instance is passed to the `premain` method of the agent class.

```java
public static void premain(String agentArgs, Instrumentation inst);
```

- (2) When a JVM provides a mechanism to start agents sometime after the JVM is launched. In that case an `Instrumentation` instance is passed to the `agentmain` method of the agent code.

```java
public static void agentmain(String agentArgs, Instrumentation inst);
```

Once an agent acquires an `Instrumentation` instance, the agent may call methods on the instance at any time.

## 第一批方法

```java
boolean isRedefineClassesSupported()
boolean isRetransformClassesSupported()
```

- `boolean isRedefineClassesSupported()`

Returns whether or not the current JVM configuration supports redefinition of classes. The ability to redefine an already loaded class is an optional capability of a JVM. Redefinition will only be supported if the `Can-Redefine-Classes` manifest attribute is set to true in the agent JAR file (as described in the package specification) and the JVM supports this capability. During a single instantiation of a single JVM, multiple calls to this method will always return the same answer.

- `boolean isRetransformClassesSupported()`

Returns whether or not the current JVM configuration supports retransformation of classes. The ability to retransform an already loaded class is an optional capability of a JVM. Retransformation will only be supported if the `Can-Retransform-Classes` manifest attribute is set to `true` in the agent JAR file  and the JVM supports this capability. During a single instantiation of a single JVM, multiple calls to this method will always return the same answer.

## 第二批方法

```java
addTransformer(ClassFileTransformer transformer)
addTransformer(ClassFileTransformer transformer, boolean canRetransform)

removeTransformer(ClassFileTransformer transformer)
```

- `addTransformer(ClassFileTransformer transformer, boolean canRetransform)`

Registers the supplied `transformer`<sub>注：这个方法的主要功能就是注册transformer，而transformer是这个方法的第一个参数</sub>. All future class definitions will be seen by the `transformer`, except definitions of classes upon which any registered `transformer` is dependent<sub>注：这是从ClassLoader的角度进行说明，ClassLoader会加载许多的类，我们把ClassLoader加载transformer作为一个时间分界点，在这个时间分界点之后加载的classes会被transformer进行处理，但是在这个时间分界点之前加载的classes并不会被transformer进行处理</sub>. The `transformer` is called when classes are loaded<sub>注：第1/3个调用时机：当类加载的时候</sub>, when they are redefined<sub>注：第2/3个调用时机：当类redefine的时候</sub>. and if `canRetransform`<sub>注：这是方法的第二个参数</sub> is `true`, when they are retransformed<sub>注：第3/3个调用时机：当类retransform的时候</sub>. See `ClassFileTransformer.transform` for the order of transform calls. If a transformer throws an exception during execution, the JVM will still call the other registered transformers in order. The same transformer may be added more than once, but it is strongly discouraged -- avoid this by creating a new instance of transformer class.

- `void addTransformer(ClassFileTransformer transformer)`

Same as `addTransformer(transformer, false)`.

- `boolean removeTransformer(ClassFileTransformer transformer)`

Unregisters the supplied `transformer`. Future class definitions will not be shown to the `transformer`. Removes the most-recently-added matching instance of the `transformer`. Due to the multi-threaded nature of class loading, it is possible for a transformer to receive calls after it has been removed. Transformers should be written defensively to expect this situation.

## 第三批方法

```java
boolean isModifiableClass(Class<?> theClass)
void redefineClasses(ClassDefinition... definitions)
void retransformClasses(Class<?>... classes)
```

### isModifiableClass

- `boolean isModifiableClass(Class<?> theClass)`

Determines whether a class is modifiable by `retransformation` or `redefinition`. If a class is modifiable then this method returns `true`. If a class is not modifiable then this method returns `false`.

For a class to be retransformed, `isRetransformClassesSupported()` must also be `true`. But the value of `isRetransformClassesSupported()` does not influence the value returned by this function. For a class to be redefined, `isRedefineClassesSupported()` must also be `true`. But the value of `isRedefineClassesSupported()` does not influence the value returned by this function.

**Primitive classes** (for example, `java.lang.Integer.TYPE`) and **array classes** are never modifiable.

### redefineClasses

- `void redefineClasses(ClassDefinition... definitions)`

Redefine the supplied set of classes using the supplied class files.

This method is used to replace the definition of a class without reference to the existing class file bytes, as one might do when recompiling from source for fix-and-continue debugging. Where the existing class file bytes are to be transformed (for example in bytecode instrumentation) `retransformClasses` should be used.

This method operates on a set in order to allow interdependent changes to more than one class at the same time (a redefinition of class A can require a redefinition of class B).

If a redefined method has active stack frames, those active frames continue to run the bytecodes of the original method. The redefined method will be used on new invokes.

This method does not cause any initialization except that which would occur under the customary JVM semantics. In other words, redefining a class does not cause its initializers to be run. The values of static variables will remain as they were prior to the call.

Instances of the redefined class are not affected.

The redefinition may change method bodies, the constant pool and attributes. The redefinition must not add, remove or rename fields or methods, change the signatures of methods, or change inheritance. These restrictions maybe be lifted in future versions. The class file bytes are not checked, verified and installed until after the transformations have been applied, if the resultant bytes are in error this method will throw an exception.

If this method throws an exception, no classes have been redefined.

Parameters: `definitions` - array of classes to redefine with corresponding definitions; **a zero-length array** is allowed, in this case, this method does nothing

### retransformClasses

- `void retransformClasses(Class<?>... classes)`

This function facilitates the instrumentation of already loaded classes. When classes are initially loaded<sub>注：第一个时机，初始被加载</sub> or when they are redefined<sub>注：第二个时机，被重新定义的时候</sub>, the initial class file bytes can be transformed with the `ClassFileTransformer`. This function reruns the transformation process (whether or not a transformation has previously occurred). This retransformation follows these steps:

- (1) starting from the initial class file bytes
- (2) for each transformer that was added with `canRetransform` `false`, the bytes returned by transform during the last class load or redefine are reused as the output of the transformation<sub>注：如果canRetransform是false的话，ClassFileTransformer的transform并不执行</sub>; note that this is equivalent to reapplying the previous transformation, unaltered; except that transform is not called
- (3) for each transformer that was added with `canRetransform` `true`, the `transform` method is called in these transformers<sub>如果canRetransform是true，那么ClassFileTransformer的transform就会执行</sub>
- (4) the transformed class file bytes are installed as the new definition of the class

**The initial class file bytes**<sub>注：这里解释了initial class file bytes究竟是什么</sub> represent the bytes passed to `ClassLoader.defineClass` or `redefineClasses` (before any transformations were applied), however they might not exactly match them<sub>注：这里又进一步说，其实initial class file bytes和`ClassLoader.defineClass`并不是完全的相等</sub>. The **constant pool**<sub>注：这里提到常量池，是为继续前面说的两种bytes并不是完全相同</sub> might not have the same layout or contents. The constant pool may have more or fewer entries. Constant pool entries may be in a different order; however, constant pool indices in the bytecodes of methods will correspond. Some attributes may not be present. Where order is not meaningful, for example the order of methods, order might not be preserved.

This method operates on a set<sub>这里是讲这个方法接收的参数是一个`Class<?>`数组</sub> in order to allow interdependent changes to more than one class at the same time (a retransformation of class A can require a retransformation of class B).

If a retransformed method has active stack frames, those active frames continue to run the bytecodes of the original method. The retransformed method will be used on new invokes.<sub>这个讲的真是很细节，修改之后的method产生效果的时机</sub>

This method does not cause any initialization<sub>注：这里谈到不会调用初始化方法，应该就是`static{}`方法</sub> except that which would occur under the customary JVM semantics. In other words, redefining a class does not cause its initializers to be run. The values of `static` variables will remain as they were prior to the call.

Instances of the retransformed class are not affected.<sub>注：这里讲了instance不受影响</sub>

The retransformation may change method bodies, the constant pool and attributes<sub>注：可以修改什么</sub>. The retransformation must not add, remove or rename fields or methods, change the signatures of methods, or change inheritance<sub>注：不可以修改什么</sub>. These restrictions maybe be lifted in future versions<sub>注：未来的展望</sub>. The class file bytes are not checked, verified and installed until after the transformations have been applied, if the resultant bytes are in error this method will throw an exception.<sub>注：这里讲到了对于修改之后的bytes进行各种合法性校验</sub>

If this method throws an exception, no classes have been retransformed.

Parameters: `classes` - array of classes to retransform; **a zero-length array** is allowed, in this case, this method does nothing

## 第四批方法

```java
Class[] getAllLoadedClasses()
Class[] getInitiatedClasses(ClassLoader loader)
long getObjectSize(Object objectToSize)
```

- `Class[] getAllLoadedClasses()`

Returns an array of all classes currently loaded by the JVM.

- `Class[] getInitiatedClasses(ClassLoader loader)`

Returns an array of all classes for which `loader` is an initiating loader. If the supplied `loader` is null, classes initiated by the **bootstrap class loader** are returned.

- `long getObjectSize(Object objectToSize)`

Returns an implementation-specific approximation of the amount of storage consumed by the specified object. The result may include some or all of the object's overhead, and thus is useful for comparison within an implementation but not between implementations. The estimate may change during a single invocation of the JVM.

## 第五批方法

```java
void appendToBootstrapClassLoaderSearch(JarFile jarfile)
void appendToSystemClassLoaderSearch(JarFile jarfile)
boolean isNativeMethodPrefixSupported()
void setNativeMethodPrefix(ClassFileTransformer transformer, String prefix)
```

## Reference

- [Interface Instrumentation](https://docs.oracle.com/javase/8/docs/api/java/lang/instrument/Instrumentation.html)
