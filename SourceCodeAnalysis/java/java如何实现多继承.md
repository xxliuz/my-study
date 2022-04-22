# java如何实现多继承

 众所周知，Java是一种面向对象的只允许单继承的语言，这是每个Java程序员从业者都知道定理。那么可不可以通过一些手段实现多继承呢？答案是可以！

实现多继承有三个方法：

> 多层继承

> 内部类

> 接口

### 1.多层继承

>如果要直接继承类，子类是不可以直接多继承的，但是可以通过多层继承来实现多继承，但多层继承一般不建议超过三次。

```java
class A{//父类A类
    private int num=10;
    public int getNum(){
        return this.num;
    }
    public void fun(){
        System.out.println("你今天真好看！");
    }
}
class B extends A{//B类继承A类
    private String name="张三";
    public String getName(){
        return this.name;
    }
    public void fun(){//方法覆写
        System.out.println(this.getNum());
        //父类私有域被继承但不可直接使用，需通过getter方法间接获得私有域的内容
    }
}
class C extends B{//C类继承B类，相当于间接继承A类
    private String name="刘能";
    public String getName(){
        return this.name;
    }
    public void fun(){//方法覆写（结果为覆写后的内容）
        System.out.println(this.getName());
        System.out.println(this.name);
    }
}
public class Test{
    public static void main(String[] args){
        A a=new A();
        a.fun();
        print(new B());//向上转型（优点在于子类可自动进行向上转型，可实现参数的统一）
        print(new C());
    }
    public static void print(A a){
        a.fun();
    }
}
```

## 2.内部类

```java
class A{//A类
    private int num=10;
    public int getNum(){
        return this.num;
    }
    public void fun(){
        System.out.println("你今天真好看！");
    }
}
class B {//B类（与A类无关）
    private String name="张三";
    public String getName(){
        return this.name;
    }
    public void fun(){
        System.out.println("昨天的你也很好看!");
    }
}
class C {//C类
    // private String name="刘能";
    class OneA extends A{//C中内部类继承A类
        public void printA(){
            System.out.println(this.getNum());
            fun();
        }
    }
    class OneB extends B{//C类内部类继承B类
        public void printB(){
            System.out.println(this.getName());
            fun();
        }
    }
    public void print(){//在C类中生成普通方法print()
        new OneA().printA();//匿名实例化OneA类对象并调用printA方法
        new OneB().printB();
    }
}
public class Test{
    public static void main(String[] args){
        C c=new C();//实例化C类对象
        c.print();//调用C中print方法
    }
}
```

### 3.接口

```java
//接口实现多继承
interface IA{//父接口A（接口为更纯粹的抽象类，结构组成只含全局常量和抽象方法）
    void funA();
}
interface IB {//父接口B（接口前添加I用以区分接口）
    void funB();
}
interface CImpl extends A,B{//接口可继承多个父接口，用,分隔开即可，子接口的命名可选择较为重要的父接口进行命名或自行命名，一般子接口后添加Impl用以区分
    void funC();
}
class Impl implements CImpl{//定义类实现接口（也可直接实现父接口（多个））
    public void funC(){//抽象方法的实现
        System.out.println("你昨天真好看！");
    }
    public void funA(){
        System.out.println("你今天真好看！");
    }
    public void funB(){
        System.out.println("你明天真好看!");
    }
}
public class Test{
    public static void main(String[] args){
        Impl im=new Impl();//实例化对象
        im.funA();
        im.funB();
        im.funC();
    }
}
```

java接口不仅可以继承接口，还可以多继承。接口是常量值和方法定义的集合。接口是一种特殊的抽象类。在java中不允许类多重继承的主要原因是，如果A同时继承B和C，而b和c同时有一个D方法，A如何决定该继承那一个呢？但接口不存在这样的问题，接口全都是抽象方法继承谁都无所谓，所以接口可以继承多个接口。

1. Java语言提供类、接口和继承等原语，为了简单起见，只支持类之间的单继承，但支持接口之间的多继承，并支持类与接口之间的实现机制。

2. java中类不支持多继承,只能单继承,但是可以多实现，java 中接口之间支持多继承,接口可以继承多个继承。

3. java中，类不支持多继承，接口支持多继承，接口的作用是拓展对象功能。当一个子接口继承了多个父接口时，说明子接口拓展了多个功能。当一个类实现该接口时，就拓展了多个的功能。