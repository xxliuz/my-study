# Collections

## 1.`HashMap`

从 `HashMap `的类注释中，我们可以得到如下信息：

- 允许` null `值，不同于 `HashTable` ，是线程不安全的；
- 影响`HashMap`实例性能的两个参数：初始容量`initial capacity`和负载因子`load factor`
- `load factor`（负载因子） 默认值是 0.75， 是均衡了时间和空间损耗算出来的值，较高的值会减少空间开销（扩容减少，数组大小增长速度变慢），但增加了查找成本（hash 冲突增加，链表长度变长），不扩容的条件：数组容量 > 需要的数组大小 /`load factor`；
- 如果有很多数据需要储存到 `HashMap` 中，建议` HashMap `的容量一开始就设置成足够的大小，这样可以防止在其过程中不断的扩容，影响性能；
- `HashMap` 是非线程安全的，我们可以自己在外部加锁，或者通过 `Collections`#`synchronizedMap` 来实现线程安全，`Map m = Collections.synchronizedMap(new HashMap(...));` `Collections`#`synchronizedMap` 的实现是在每个方法上加上了` synchronized` 锁；
- 在迭代过程中，如果 `HashMap` 的结构被修改，会快速失败。

### 1.1  结构

`HashMap` 继承关系，核心成员变量，主要构造函数：

```java
public class HashMap<K,V> extends AbstractMap<K,V>
    	implements Map<K,V>, Cloneable, Serializable {
    	 //---------------------------------默认值---------------------------------
    	 //初始容量为 16,必须为2的幂
         static final int DEFAULT_INITIAL_CAPACITY = 1 << 4;
    
         //最大容量，必须是 2 <= 1<<30 的幂。
         static final int MAXIMUM_CAPACITY = 1 << 30;
    
         //负载因子默认值
         static final float DEFAULT_LOAD_FACTOR = 0.75f;
    
         //（桶上的）链表长度大于等于8时，链表转化成红黑树
         static final int TREEIFY_THRESHOLD = 8;
    
         //（桶上的）红黑树大小小于等于6时，红黑树转化成链表
         static final int UNTREEIFY_THRESHOLD = 6;
    
         // 当数组容量大于 64 时，链表才会转化成红黑树
         static final int MIN_TREEIFY_CAPACITY = 64;
    	
    	//--------------------------------属性-----------------------------------
        // 负载因子，当已有槽点点占总槽点数的比值，达到后扩容
        // 可理解为，一个数组能用的槽点只用 ~%
        final float loadFactor;
        
         // 记录迭代过程中 HashMap 结构（数组）是否发生变化，如果有变化，迭代时会 fail-fast
         transient int modCount;
    
         // HashMap 的实际大小（数组大小），注意两点：
         // 1.具体节点（node）的数量并没有成员变量做记录，只是在每次遍历一个桶时用binCount作为局部变量计数
         // 2.size可能不准(因为当你拿到这个值的时候，可能又发生了变化)
         transient int size;
    
         // 存放数据的数组
         transient Node<K,V>[] table;
    
         // 阈值，两个作用：初始容量 --> 扩容阈值
         int threshold;  
         
         //-------------------------------Node---------------------------------------------
         // 链表的节点
         // 注：一个哈希桶中的node，hash和key可能都不相同，因为槽点是通过hash%(n-1)得到的
         static class Node<K,V> implements Map.Entry<K,V> {//Map.Entry是个接口
            final int hash; // 当前node的hash值，作用是决定位于哪个哈希桶，是通过key的hashCode计算来的
            final K key; // 可能是包装类实例可能是自定义对象，不同key可能有相同hash
            V value;
            Node<K,V> next; //指向当前node的下一个节点，构成单向链表
    
            Node(int hash, K key, V value, Node<K,V> next) {
                this.hash = hash;
                this.key = key;
                this.value = value;
                this.next = next;
            }
    
            public final K getKey()        { return key; }
            public final V getValue()      { return value; }
            public final String toString() { return key + "=" + value; }
    
            public final int hashCode() {
                // Objects.hashCode 防止空指针
                return Objects.hashCode(key) ^ Objects.hashCode(value);
            }
    
            public final V setValue(V newValue) {
                V oldValue = value;
                value = newValue;
                return oldValue;
            }
    
            public final boolean equals(Object o) {
                if (o == this)
                    return true;
                if (o instanceof Map.Entry) {
                    Map.Entry<?,?> e = (Map.Entry<?,?>)o;
                    if (Objects.equals(key, e.getKey()) &&
                        Objects.equals(value, e.getValue()))
                        return true;
                }
                return false;
            }
        }
        
         // 红黑树的节点
         // 注意：这里是直接继承LinkedListMap.Entry
     	static final class TreeNode<K,V> extends LinkedHashMap.Entry<K,V> {
     	
     	//---------------------------构造方法----------------------------------------------
     	// 传入初始化数组节点，负载因子
     	public HashMap(int initialCapacity, float loadFactor) {
            if (initialCapacity < 0)
                throw new IllegalArgumentException("Illegal initial capacity: " +
                                                   initialCapacity);
            // 若传入的初始化容量 > 最大，使用最大
            if (initialCapacity > MAXIMUM_CAPACITY)
                initialCapacity = MAXIMUM_CAPACITY;
            if (loadFactor <= 0 || Float.isNaN(loadFactor))
                throw new IllegalArgumentException("Illegal load factor: " +
                                                   loadFactor);
            this.loadFactor = loadFactor;
            this.threshold = tableSizeFor(initialCapacity);
        }
        
        // 负载因子
        public HashMap(int initialCapacity) {
        	// 使用默认负载因子
            this(initialCapacity, DEFAULT_LOAD_FACTOR);
        }
    
        // 使用16,0.75
        public HashMap() {
            this.loadFactor = DEFAULT_LOAD_FACTOR; // all other fields defaulted
        }
    }
```

这里主要关注Capacity和threshold

#### 1.1.1 `Capacity`必须是2的幂

- 目的：保证计算槽点位置tab[i = (n - 1) & hash] 的结果在 [0,Cap-1] 之间
- 实现
  - 空参构造：16
  - 指定`InitialCap`：通过` threshold`=`tableForSize` 计算出初始容量，得到 >= cap 且最接近的2的幂 比如 给定初始化大小 19，实际上初始化大小为 32，为 2 的 5 次方
  - 后续扩容 resize() >> 1

```java
    static final int tableSizeFor(int cap) {
            int n = cap - 1;
            n |= n >>> 1;
            n |= n >>> 2;
            n |= n >>> 4;
            n |= n >>> 8;
            n |= n >>> 16;
            return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
        } 
```

#### 1.1.2 `threshold`阈值

`threshold`有两个作用：

1. 初始容量：用于指定容量时的初始化，`=tableSizeFor(initialCap)`，在`resize()`中使用 注：1.此时`thresho`肯定是2的幂 2.初始化完成后就直接变成 `newThr `=` NewCap*0.75` 成为扩容阈值
2. 扩容阈值：然后用于记录数组扩容阈值，=数组容量*0.75，在`putVal()`中使用 注：当`threshod`=`Integer.MAX_VALUE`无法再扩容

### 1.2 方法解析&`api`

#### 1.2.1 hash

计算每个Node的hash值，这个hash值是确定哈希桶的依据

- 那为什么不直接拿key作为分桶依据，而是要hash值？因为不是所有的key都是Integer
- 那得到这个hash值后，怎么计算槽点？具体的槽点` = (length-1) % hash = hash % (length-1)`

```java
// jdk8比jdk7引入了红黑树，所以hash方法进行了简化
static final int hash(Object key) {
        int h;
    	// null = 0
        return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
 }
```

从代码中看出，要注意以下两点：

1. 如果某个类型要作为`hashmap`的key，必须要有`hashcode`方法
2. 将`hashCode`值再右移16位是为了让算出来的hash更分散，以减少哈希冲突

#### 1.2.2 向Hash表中添加元素

##### `put()`

```java
 public V put(K key, V value) {
        	// 传入hash值的目的在于，定位目标桶
            return putVal(hash(key), key, value, false, true);
    }
```

##### `putVal()`

1. 判断数组有无初始化，若无先初始化   注：跟`ArrayList`一样，不是一开始就初始化好数组，而是在第一次添加元素再初始化
2. 执行新增逻辑
   - 无`hash`冲突：即当前位置为空，直接增加
     1. 在方法最后`modCount`++，记录`hashmap`数组发生变化
     2. `if (++size > threshold) resize()`; 判断是否要扩容   注：size记录的是数组大小，只有无hash冲突时，才会size++然后判断扩容
     3. 最后返回null
   - hash冲突下：需要找到位置e（key与hash相同节点 或 队尾），追加或判断时候覆盖旧值
     - 数组上的这个节点的与要新增节点key与hash相同
     - 红黑树，使用红黑树的方式新增
     - 链表，依次遍历，`binCount`记录链表长度，判断有无key与hash相同的节点
       - e != null 若`onlyIfAbsent`=false（覆盖旧值） ,返回`oldValue`
       - e == null （追加到队尾），if (`binCount` >= `TREEIFY_THRESHOLD `- 1) 判断是否需要链表转红黑树

```java
// 入参 hash：通过 hash 算法计算出来的值。用于定位目标桶
// 入参 onlyIfAbsent：absent缺席，表示只在key不存在时添加，默认为 false
// 返回的是oldValue，若不存在相同key就返回null
final V putVal(int hash, K key, V value, boolean onlyIfAbsent,
               boolean evict) {
    // n 表示数组的长度，i 为数组索引下标，p 为 i 下标位置的 Node 值
    Node<K,V>[] tab; Node<K,V> p; int n, i;
    // 如果数组为空，使用 resize 方法初始化
    if ((tab = table) == null || (n = tab.length) == 0)
        n = (tab = resize()).length;
    
    //---------------------------新增逻辑--------------------------------------------
    // 计算数组下标，实际上就是 hash %（n-1）
    // 并判断如果当前索引位置是空的，直接生成新的节点在当前索引位置上
    if ((p = tab[i = (n - 1) & hash]) == null)
        tab[i] = newNode(hash, key, value, null);
    // 如果当前索引位置有值的处理方法，即我们常说的如何解决 hash 冲突
    else {
        // e 当前节点的临时变量
        Node<K,V> e; K k;
        // 如果 key 的 hash 和值都相等，直接把当前下标位置的 Node 值赋值给临时变量
        // 注：这里是先判断key的hash值，因为hash不同key一定不同，
        //     然后再判断key，这里还要==key 是因为key可能是缓存池的包装类
        if (p.hash == hash &&
            ((k = p.key) == key || (key != null && key.equals(k))))
            e = p;
        // 如果是红黑树，使用红黑树的方式新增
        else if (p instanceof TreeNode)
            e = ((TreeNode<K,V>)p).putTreeVal(this, tab, hash, key, value);
        // 是个链表，把新节点放到链表的尾端
        else {
            // 自旋，链表头开始遍历
            // 注：这个binCount还有记录链表长度的作用
            for (int binCount = 0; ; ++binCount) {
                // p.next == null 表明此时已经遍历到了链表最后
                if ((e = p.next) == null) {
                    // 把新节点放到链表的尾部 
                    p.next = newNode(hash, key, value, null);
                    // 当链表的长度大于等于 8 时，链表转红黑树
                    // 注：在treeifyBin方法中还要判断size是否大于64，若小于64不会树化会扩容
                    if (binCount >= TREEIFY_THRESHOLD - 1)
                        treeifyBin(tab, hash);
                    break;
                }
                // 链表遍历过程中，发现有元素和新增的元素相等，结束循环
                if (e.hash == hash && ((k = e.key) == key || (key != null && key.equals(k))))
                    break;
                // 更改循环的当前元素，使 p 在遍历过程中，一直往后移动。
                p = e;
            }
        }
        
        // 说明链表中存在与key相同的节点
        if (e != null) {
            V oldValue = e.value;
            // 当 onlyIfAbsent 为 false 时，才会覆盖值 
            if (!onlyIfAbsent || oldValue == null)
                e.value = value;
            afterNodeAccess(e);
            // 返回老值
            return oldValue;
        }
    }
    
    //---------------------------------------------------------------------------------
    // 记录 HashMap 的数据结构发生了变化
    ++modCount;
    // 如果 HashMap 的实际大小大于扩容的门槛，开始扩容
    // 注：这里的逻辑是先增加再扩容，因为不一定是新增还有可能是覆盖
    if (++size > threshold)
        resize();
    
    // 对于LinkedListMap判断是否执行LRU条件之一（默认true）
    afterNodeInsertion(evict);
    return null;
}
```

#### 1.2.3 链表树化&向红黑树中添加元素

#### `treeifyBin()`

链表转化为红黑树

```java
final void treeifyBin(Node<K,V>[] tab, int hash) {
            int n, index; Node<K,V> e;
        	// 如果当前hash表的长度小于64，不会树化而是进行扩容
            if (tab == null || (n = tab.length) < MIN_TREEIFY_CAPACITY)
                resize();
            else if ((e = tab[index = (n - 1) & hash]) != null) {
                TreeNode<K,V> hd = null, tl = null;
                do {
                    TreeNode<K,V> p = replacementTreeNode(e, null);
                    if (tl == null)
                        hd = p;
                    else {
                        p.prev = tl;
                        tl.next = p;
                    }
                    tl = p;
                } while ((e = e.next) != null);
                if ((tab[index] = hd) != null)
                    hd.treeify(tab);
            }
        }
```

- 转换条件

  - 表长度大于等于 8，并且整个数组大小大于 64 时，才会转成红黑树
  - 当数组大小小于 64 时，只会触发扩容，不会转化成红黑树

- 链表长度阈值为什么是8？ 链表查询的时间复杂度是O (n)，红黑树的查询复杂度是 O (log (n))。在链表数据不多的时候，使用链表进行遍历也比较快，只有当链表数据比较多的时候，才会转化成红黑树，但红黑树需要的占用空间是链表的 2 倍，考虑到转化时间和空间损耗，所以我们需要定义出转化的边界值。 在考虑设计 8 这个值的时候，参考了泊松分布概率函数，由泊松分布中得出结论，链表各个长度的命中概率为：

  ```
  * 0:    0.60653066
  * 1:    0.30326533
  * 2:    0.07581633
  * 3:    0.01263606
  * 4:    0.00157952
  * 5:    0.00015795
  * 6:    0.00001316
  * 7:    0.00000094
  * 8:    0.00000006
  ```

  意思是，当链表的长度是 8 的时候，出现的概率是 0.00000006，不到千万分之一，所以说正常情况下，链表的长度不可能到达 8 ，而一旦到达 8 时，肯定是 hash 算法出了问题，所以在这种情况下，为了让`HashMap` 仍然有较高的查询性能，所以让链表转化成红黑树，我们正常写代码，使用` HashMap` 时，几乎不会碰到链表转化成红黑树的情况，毕竟概念只有千万分之一。

#### `putTreeVal()`

1. 首先判断新增的节点在红黑树上是不是已经存在，判断手段有如下两种：
   1. 如果节点没有实现 `Comparable` 接口，使用 `equals `进行判断；
   2. 如果节点自己实现了 `Comparable `接口，使用` compareTo` 进行判断。
2. 新增的节点如果已经在红黑树上，直接返回；不在的话，判断新增节点是在当前节点的左边还是右边，左边值小，右边值大；
3. 自旋递归 1 和 2 步，直到当前节点的左边或者右边的节点为空时，停止自旋，当前节点即为我们新增节点的父节点；
4. 把新增节点放到当前节点的左边或右边为空的地方，并于当前节点建立父子节点关系；
5. 进行着色和旋转，结束。

```java
//入参 h：key 的hash值
final TreeNode<K,V> putTreeVal(HashMap<K,V> map, Node<K,V>[] tab,
                               int h, K k, V v) {
    Class<?> kc = null;
    boolean searched = false;
    //找到根节点
    TreeNode<K,V> root = (parent != null) ? root() : this;
    //自旋
    for (TreeNode<K,V> p = root;;) {
        int dir, ph; K pk;
        // p hash 值大于 h，说明 p 在 h 的右边
        if ((ph = p.hash) > h)
            dir = -1;
        // p hash 值小于 h，说明 p 在 h 的左边
        else if (ph < h)
            dir = 1;
        //要放进去key在当前树中已经存在了(equals来判断)
        else if ((pk = p.key) == k || (k != null && k.equals(pk)))
            return p;
        //自己实现的Comparable的话，不能用hashcode比较了，需要用compareTo
        else if ((kc == null &&
                  //得到key的Class类型，如果key没有实现Comparable就是null
                  (kc = comparableClassFor(k)) == null) ||
                  //当前节点pk和入参k不等
                 (dir = compareComparables(kc, k, pk)) == 0) {
            if (!searched) {
                TreeNode<K,V> q, ch;
                searched = true;
                if (((ch = p.left) != null &&
                     (q = ch.find(h, k, kc)) != null) ||
                    ((ch = p.right) != null &&
                     (q = ch.find(h, k, kc)) != null))
                    return q;
            }
            dir = tieBreakOrder(k, pk);
        }

        TreeNode<K,V> xp = p;
        //找到和当前hashcode值相近的节点(当前节点的左右子节点其中一个为空即可)
        if ((p = (dir <= 0) ? p.left : p.right) == null) {
            Node<K,V> xpn = xp.next;
            //生成新的节点
            TreeNode<K,V> x = map.newTreeNode(h, k, v, xpn);
            //把新节点放在当前子节点为空的位置上
            if (dir <= 0)
                xp.left = x;
            else
                xp.right = x;
            //当前节点和新节点建立父子，前后关系
            xp.next = x;
            x.parent = x.prev = xp;
            if (xpn != null)
                ((TreeNode<K,V>)xpn).prev = x;
            //balanceInsertion 对红黑树进行着色或旋转，以达到更多的查找效率，着色或旋转的几种场景如下
            //着色：新节点总是为红色；如果新节点的父亲是黑色，则不需要重新着色；		   	
            //如果父亲是红色，那么必须通过重新着色或者旋转的方法，再次达到红黑树的5个约束条件
            //旋转： 父亲是红色，叔叔是黑色时，进行旋转
            //如果当前节点是父亲的右节点，则进行左旋
            //如果当前节点是父亲的左节点，则进行右旋
          
            //moveRootToFront 方法是把算出来的root放到根节点上
            moveRootToFront(tab, balanceInsertion(root, x));
            return null;
        }
    }
}	
```

红黑树的 5 个原则：

- 节点是红色或黑色
- 根是黑色
- 所有叶子都是黑色
- 从任一节点到其每个叶子的所有简单路径都包含相同数目的黑色节点
- 从每个叶子到根的所有路径上不能有两个连续的红色节点

### 1.3 扩容resize

resize()有两个作用，1.初始化数组，2.扩容。

1. 计算新数组大小：
   - 初始化
     - 指定容量，`newCap`=`threshold`
     - 未指定容量，`newCap`=16
   - 扩容：`newCap`=`oldCap` * 2
2. 执行扩容策略：逐个遍历所有槽点
   - 当前槽点只有一个node，直接重新分配到新数组即可`newTab[e.hash & (newCap - 1)]`
   - 当前槽点下是红黑树
   - 当前槽点下是链表：因为链表中所有node的hash和key相同，而现在数组扩容了两倍，所以现在的想法是将当前链等分成两部分
     1. 怎么等分成两部分？分成high链与low链，两链关系可近似理解为单双数节点
     2. 怎么实现？Head用来标识链首，Tail用来尾连接
     3. 两链放在新数组哪里？：low链置于`newTab[ j ]`，high链置于`newTab[ j + oldCap ]`（ j 表示在原来数组位置）

```java
final Node<K,V>[] resize() {
        Node<K,V>[] oldTab = table;
        int oldCap = (oldTab == null) ? 0 : oldTab.length;
        int oldThr = threshold;
        int newCap, newThr = 0;
    	// 情况一：oldCap>0表示要扩容
        if (oldCap > 0) {
            // 当老数组容量已达最大值时无法再扩容
            if (oldCap >= MAXIMUM_CAPACITY) {
                threshold = Integer.MAX_VALUE;
                return oldTab;
            }
            // Cap * 2 ， Thr * 2
            else if ((newCap = oldCap << 1) < MAXIMUM_CAPACITY &&
                     oldCap >= DEFAULT_INITIAL_CAPACITY)
                newThr = oldThr << 1; // double threshold
        }
    	// 情况二：初始化，且已指定初始化容量
        else if (oldThr > 0) // initial capacity was placed in threshold
            newCap = oldThr;
    	// 情况三：初始化，未指定初始化容量
        else {               
            // 以默认初始化容量16进行初始化
            newCap = DEFAULT_INITIAL_CAPACITY;
            // 16 * 0.75 = 12
            newThr = (int)(DEFAULT_LOAD_FACTOR * DEFAULT_INITIAL_CAPACITY);
        }
    	// 用指定容量初始化后，更新其扩容阈值
        if (newThr == 0) {
            float ft = (float)newCap * loadFactor;
            newThr = (newCap < MAXIMUM_CAPACITY && ft < (float)MAXIMUM_CAPACITY ?
                      (int)ft : Integer.MAX_VALUE);
        }
        threshold = newThr;
        @SuppressWarnings({"rawtypes","unchecked"})
    	// 执行初始化，建立新数组
            Node<K,V>[] newTab = (Node<K,V>[])new Node[newCap];
    	// 将新数组附给table
        table = newTab;
    	
    	//-------------------------------扩容策略-----------------------------------------------
    	// 若是执行的初始化，old=null，就不用走这里的扩容策略了
        if (oldTab != null) {
            // 遍历原数组
            for (int j = 0; j < oldCap; ++j) {
                Node<K,V> e;
                // 当原数组的当前槽点有值时,将其赋给e
                if ((e = oldTab[j]) != null) {
                    // 释放原数组当前节点内存，帮助GC
                    oldTab[j] = null;
                    // 若当前槽点只有一个值，直接找到新数组相应位置并赋值
                    if (e.next == null)
                        newTab[e.hash & (newCap - 1)] = e;
                    // 若当前节点下是红黑树
                    else if (e instanceof TreeNode)
                        ((TreeNode<K,V>)e).split(this, newTab, j, oldCap);
                    // 当前节点下是链表
                    else { // preserve order
                        // 将该链表分成两条链，low低位链 和 high高位链
                        // Head标识链头，Tail用来连接
                        Node<K,V> loHead = null, loTail = null;
                        Node<K,V> hiHead = null, hiTail = null;
                        Node<K,V> next;
                        // do while((e = next) != null)
                        do {
                            next = e.next;
                            // 低位链连接
                            if ((e.hash & oldCap) == 0) {
                                if (loTail == null)
                                    loHead = e;
                                else
                                    loTail.next = e;
                                loTail = e;
                            }
                            // 高位链连接
                            else {
                                if (hiTail == null)
                                    hiHead = e;
                                else
                                    hiTail.next = e;
                                hiTail = e;
                            }
                        } while ((e = next) != null);
                        // 将低位链放置于老链同一下标
                        // eg.原来当前节点位于oldTab[2],则现在低位链还置于newTab[2]
                        if (loTail != null) {
                            loTail.next = null;
                            newTab[j] = loHead;
                        }
                        // 将高位链放置于老链下标+oldCap
                        // eg.原来当前节点位于oldTab[2],则现在高位链置于newTab[2+8=10]
                        if (hiTail != null) {
                            hiTail.next = null;
                            newTab[j + oldCap] = hiHead;
                        }
                    }
                }
            }
        }
        return newTab;
    }
```

### 1.4 从Hash表中获取元素

#### `get()`

计算hash，并调用`getNode`

```java
public V get(Object key) {
            Node<K,V> e;
        	// 如果是null直接返回null
            return (e = getNode(hash(key), key)) == null ? null : e.value;
    }
```

#### `getNode()`

1. 根据hash值定位数组的索引位置
2. equals 判断当前节点是否是我们需要寻找的 key
   - 是的话直接返回
   - 不是则判断当前节点有无 next 节点，有的话判断是链表类型，还是红黑树类型。
3. 分别走链表和红黑树不同类型的查找方法

```java
// 传入的hash值用于确定哈系桶，key用于确定具体节点
final Node<K,V> getNode(int hash, Object key) {
        Node<K,V>[] tab; Node<K,V> first, e; int n; K k;
        // 数组不为空 && hash算出来的索引下标有值，
        if ((tab = table) != null && (n = tab.length) > 0 &&
            (first = tab[(n - 1) & hash]) != null) {
            // hash 和 key 的 hash 相等，直接返回
            if (first.hash == hash &&
                ((k = first.key) == key || (key != null && key.equals(k))))
                return first;
            // hash不等，看看当前节点的 next 是否有值
            if ((e = first.next) != null) {
                // 使用红黑树的查找
                if (first instanceof TreeNode)
                    return ((TreeNode<K,V>)first).getTreeNode(hash, key);
                // 采用自旋方式从链表中查找 key，e 为链表的头节点
                do {
                    // 如果当前节点 hash == key 的 hash，并且 equals 相等，当前节点就是我们要找的节点
                    // 先比较hash效率高，因为hash一定是数字，key可能是包装类可能是自定义对象
                    if (e.hash == hash &&
                        ((k = e.key) == key || (key != null && key.equals(k))))
                        return e;
                    // 否则，把当前节点的下一个节点拿出来继续寻找
                } while ((e = e.next) != null);
            }
        }
        return null;
    }
```

#### find()

红黑树寻找指定节点

1. 从根节点递归查找；
2. 根据 hashcode，比较查找节点，左边节点，右边节点之间的大小，根本红黑树左小右大的特性进行判断；
3. 判断查找节点在第 2 步有无定位节点位置，有的话返回，没有的话重复 2，3 两步；
4. 一直自旋到定位到节点位置为止。

```java
final TreeNode<K,V> find(int h, Object k, Class<?> kc) {
            TreeNode<K,V> p = this;
            do {
                int ph, dir; K pk;
                TreeNode<K,V> pl = p.left, pr = p.right, q;
                if ((ph = p.hash) > h)
                    p = pl;
                else if (ph < h)
                    p = pr;
                else if ((pk = p.key) == k || (k != null && k.equals(pk)))
                    return p;
                else if (pl == null)
                    p = pr;
                else if (pr == null)
                    p = pl;
                else if ((kc != null ||
                          (kc = comparableClassFor(k)) != null) &&
                         (dir = compareComparables(kc, k, pk)) != 0)
                    p = (dir < 0) ? pl : pr;
                else if ((q = pr.find(h, k, kc)) != null)
                    return q;
                else
                    p = pl;
            } while (p != null);
            return null;
        }
```

### 1.5 迭代器

Map 对 key、value 和 entity（节点） 都提供了迭代器。这些迭代器都是通过`map.~Set().iterator()`进行调用

- 迭代`key：HashMap --keySet()--> KeySet --iterator()--> KeyIterator`
- 迭代`value：HashMap --values()--> Values--iterator()--> ValueIterator`
- 迭代`key：HashMap --entrySet()--> EntrySet--iterator()--> EntryIterator`

虽然是不同的迭代器，但是它们本质上却没有区别：

1. 都继承了`HashIterator`
2. 都只有一个方法：next()，而且里面调用的都是 `HashIterator.nextNode()`，只不过最后在node中取值不同

```java
final class KeyIterator extends HashIterator
    implements Iterator<K> {
    public final K next() { return nextNode().key; } // 调用父类的nextNode方法，返回node的key
}

final class ValueIterator extends HashIterator
    implements Iterator<V> {
    public final V next() { return nextNode().value; } // 调用父类的nextNode方法，返回node的value
}

final class EntryIterator extends HashIterator
    implements Iterator<Map.Entry<K,V>> {
    public final Map.Entry<K,V> next() { return nextNode(); }  // 调用父类的nextNode方法，返回node
}
```

#### `HashIterator`

```java
 abstract class HashIterator {
            Node<K,V> next;        // next entry to return
            Node<K,V> current;     // current entry
            int expectedModCount;  // for fast-fail
            int index;             // current slot
    
            HashIterator() {
                expectedModCount = modCount;
                Node<K,V>[] t = table;
                current = next = null;
                index = 0;
                if (t != null && size > 0) { // advance to first entry
                    do {} while (index < t.length && (next = t[index++]) == null);
                }
            }
    ......
        }
```

#### `hasNext()`

判断当前node在桶中是否有下一个node

```java
 public final boolean hasNext() {
        return next != null;
    }
```

#### `nextNode()`

获取当前节点的下一个node。

- 整体的迭代策略是逐个桶遍历，可理解成外层是遍历数组，内层是遍历链表（红黑树）
- 该方法屏蔽了node处于不同桶所带来的差异，就好像所有元素在一个桶中。

```java
final Node<K,V> nextNode() {
    Node<K,V>[] t;
    // 记录next结点
    Node<K,V> e = next; 
    // 若在遍历时对HashMap进行结构性的修改则会抛出异常
    if (modCount != expectedModCount)
        throw new ConcurrentModificationException();
    // 下一个结点为空，抛出异常
    if (e == null)
        throw new NoSuchElementException();
    // 如果下一个结点为空 && table不为空，表示当前桶中所有结点已经遍历完
    // 注：核心！！！实现了跨桶遍历
    if ((next = (current = e).next) == null && (t = table) != null) {
        // 寻找下一个不为空的桶：未到最后一个槽点 && 下一个槽点不为空
        do {} while (index < t.length && (next = t[index++]) == null);
    }
    return e;
}
```

#### `remove()`

```java
public final void remove() {
        Node<K,V> p = current;
        // 当前结点为空，抛出异常
        if (p == null)
            throw new IllegalStateException();
        // 若在遍历时对HashMap进行结构性的修改则会抛出异常
        if (modCount != expectedModCount)
            throw new ConcurrentModificationException();
        // 若在遍历时对HashMap进行结构性的修改则会抛出异常
        current = null;
        K key = p.key;
        // 移除结点
        removeNode(hash(key), key, null, false, false);
        // 赋最新值
        expectedModCount = modCount;
    }
```

### 1.6 question

#### 1.6.1 `HashMap `底层数据结构是什么？

答：`HashMap` 底层是数组 + 链表 + 红黑树的数据结构

- 数组的主要作用是方便快速查找，时间复杂度是 O(1)，默认大小是 16，数组的下标索引是通过 key 的 `hashcode` 计算出来的，数组元素叫做 Node
- 当多个 key 的` hashcode `一致，但 key 值不同时，单个 Node 就会转化成链表，链表的查询复杂度是 O(n)
- 当链表的长度大于等于 8 并且数组的大小超过 64 时，链表就会转化成红黑树，红黑树的查询复杂度是 O(log(n))，简单来说，最坏的查询次数相当于红黑树的最大深度。

#### 1.6.2 为解决 hash 冲突，大概有哪些办法？

答：

1. 好的 hash 算法
2. 自动扩容，当数组大小快满的时候，采取自动扩容，可以减少 hash 冲突;
3. hash 冲突发生时，采用链表来解决;
4. hash 冲突严重时，链表会自动转化成红黑树，提高遍历速度。

#### 1.6.3`HashMap` 是如何扩容的？

- 扩容时机：
  1. 初始化：put 时，发现数组为空，进行初始化扩容，默认扩容大小为 16;
  2. 扩容：put 成功后，发现现有数组大小大于扩容的门阀值时，进行扩容，扩容为老数组大小的 2 倍;
- 扩容的阀值是 `threshold`，每次扩容时` threshold `都会被重新计算，门阀值等于数组的大小 * 影响因子（0.75）。
- 新数组初始化之后，需要将老数组的值拷贝到新数组上，链表和红黑树都有自己拷贝的方法。

#### 1.6.4  hash 冲突时怎么办？

答：hash 冲突指的常出现于不同的 key 计算得到相同的` hashcode` 情况。

- 如果桶中元素原本只有一个或已经是链表了，新增元素直接追加到链表尾部；
- 如果桶中元素已经是链表，并且链表个数大于等于 8 时，此时有两种情况：
  1. **如果此时数组大小小于 64，数组再次扩容，链表不会转化成红黑树;**
  2. **如果数组大小大于 64 时，链表就会转化成红黑树。**

这里不仅仅判断链表个数大于等于 8，还判断了数组大小，数组容量小于 64 没有立即转化的原因，猜测主要是因为红黑树占用的空间比链表大很多，转化也比较耗时，所以数组容量小的情况下冲突严重，我们可以先尝试扩容，看看能否通过扩容来解决冲突的问题。

#### 1.6.5 为什么链表个数大于等于 8 时，链表要转化成红黑树了？

答：这实际是两个问题

1. 为什么要转换成红黑树？ 当链表个数太多了，遍历可能比较耗时，转化成红黑树，可以使遍历的时间复杂度降低。但转化成红黑树会有空间和转化耗时的成本。
2. 为什么是节点个数大于等于8？ 通过泊松分布公式计算，正常情况下，链表个数出现 8 的概念不到千万分之一，所以说正常情况下，链表都不会转化成红黑树，这样设计的目的，是为了防止非正常情况下，比如 hash 算法出了问题时，导致链表个数轻易大于等于 8 时，仍然能够快速遍历。

#### 1.6.6 红黑树什么时候转变成链表?

答：当节点的个数小于等于 6 时，红黑树会自动转化成链表，主要还是考虑红黑树的空间成本问题，当节点个数小于等于 6 时，遍历链表也很快，所以红黑树会重新变成链表。

#### 1.6.7 `HashMap`在 put 时，如果数组中已经有了这个 key，我不想把 value 覆盖怎么办？取值时，如果得到的 value 是空时，想返回默认值怎么办？

答：

- 如果数组有了 key，但不想覆盖 value ，可以选择 **`putIfAbsent` **方法，这个方法有个内置变量` onlyIfAbsent`，内置是 true ，就不会覆盖，我们平时使用的 put 方法，内置 `onlyIfAbsent `为 false，是允许覆盖的。
- 取值时，如果为空，想返回默认值，可以使用 `getOrDefault `方法，方法第一参数为 key，第二个参数为你想返回的默认值，如 `map.getOrDefault(“2”,“0”)`，当 map 中没有 key 为 2 的值时，会默认返回 0，而不是空。

#### 1.6.8 通过以下代码进行删除，是否可行？

```java
HashMap<String,String > map = Maps.newHashMap();
map.put("1","1");
map.put("2","2");
map.forEach((s, s2) -> map.remove("1"));
```

答：不行，会报错误` ConcurrentModificationException`，`forEach`源码如下：

```java
public void forEach(BiConsumer<? super K, ? super V> action) {
        Node<K,V>[] tab;
        if (action == null)
            throw new NullPointerException();
        if (size > 0 && (tab = table) != null) {
            int mc = modCount; // 开始循环之前modCount被赋值给mc
            for (int i = 0; i < tab.length; ++i) {
                for (Node<K,V> e = tab[i]; e != null; e = e.next)
                    action.accept(e.key, e.value);
            }
            if (modCount != mc) // 删除时remove方法会修改modCount，但mc没变
                throw new ConcurrentModificationException();
        }
    }
```

建议使用迭代器的方式进行删除，原理同 `ArrayList` 迭代器原理.



# 2.`ArrayList`

## 2.1 结构

`ArrayList`继承关系，核心成员变量，主要构造函数：

```java
public class ArrayList<E> extends AbstractList<E>
    	implements List<E>, RandomAccess, Cloneable, java.io.Serializable{
    	
    	//默认数组大小10
        private static final int DEFAULT_CAPACITY = 10;
    
        //数组存放的容器
        private static final Object[] EMPTY_ELEMENTDATA = {};
    
        //数组使用的大小，注：length是整个数组的大小
        private int size;
          
        //空数组，用于空参构造
        private static final Object[] DEFAULTCAPACITY_EMPTY_ELEMENTDATA = {};
    		
    	//真正保存数据的数组，注：这里是Object类型 ===> 构造时传入泛型是必要的
      	transient  Object[] elementData;
    	
    	//---------------------------------------------------------------------
        
        //无参数直接初始化，数组大小为空
        //注：ArrayList 无参构造器初始化时，默认大小是空数组，并不是大家常说的 10，
        //   10 是在第一次 add 的时候扩容的数组值
        public ArrayList() {
            this.elementData = DEFAULTCAPACITY_EMPTY_ELEMENTDATA;
        }
        
        //指定容量初始化
        public ArrayList(int initialCapacity) {
            if (initialCapacity > 0) {
              this.elementData = new Object[initialCapacity];
            } else if (initialCapacity == 0) {
              this.elementData = EMPTY_ELEMENTDATA;
            } else {
              throw new IllegalArgumentException("Illegal Capacity: "+
                                                 initialCapacity);
            }
        }
    
        //指定初始数据初始化
        // <? extends E>：类型，E及E的子类们
        // Collection<? extends E>：E及E的子类的集合
        public ArrayList(Collection<? extends E> c) {
            //elementData 是保存数组的容器，默认为 null
            elementData = c.toArray();
            //如果给定的集合（c）数据有值
            // size
            if ((size = elementData.length) != 0) {
                // c.toArray might (incorrectly) not return Object[] (see 6260652)
                //如果集合元素类型不是 Object 类型，我们会转成 Object
                if (elementData.getClass() != Object[].class) {
                    elementData = Arrays.copyOf(elementData, size, Object[].class);
                }
            } else {
                // 给定集合（c）无值，则默认空数组
                this.elementData = EMPTY_ELEMENTDATA;
            }
        }
    }
```

## 2.2 方法解析&`api`

### 2.2.1 增加

#### `add()`

增加单个元素到容器中

```java
 public boolean add(E e) {
        //确保数组大小足够，不够需要扩容（期望容量=size+1）
        ensureCapacityInternal(size + 1);  // Increments modCount!!
        //直接赋值，线程不安全的
        //注：这里没有非空判断，因此可以加入null
        elementData[size++] = e;
    	// 这里虽然是boolean，但一般只会返回true
        return true;
    }
```

#### `addAll()`

批量增加，即增加多个元素（集合）到容器中

```java
	public boolean addAll(Collection<? extends E> c) {
        Object[] a = c.toArray();
        int numNew = a.length;
        // 确保容量充足，整个过程只会扩容一次（期望容量=size+a.length)
        ensureCapacityInternal(size + numNew);  // Increments modCount
        // 直接将要加入的集合拷贝到elementData后面即可
        // 注：Arrays.copyOf适用于1-->2的拷贝,而sys..适用于对原数组或指定数组的操作
        System.arraycopy(a, 0, elementData, size, numNew);
        size += numNew;
        // 只有要增加的集合为空时，返回false
        return numNew != 0;
      }
```

### 2.2.2 扩容

#### `ensureCapacityInternal()`

计算期望的最小容量

```java
    private void ensureCapacityInternal(int minCapacity) {
      // 只有当elementData为空（即构造时没有传入容量 && 第一次扩容)，才使用默认大小10
      if (elementData == DEFAULTCAPACITY_EMPTY_ELEMENTDATA) {
        minCapacity = Math.max(DEFAULT_CAPACITY, minCapacity);
      }
      // 判断是否需要扩容
      ensureExplicitCapacity(minCapacity);
    }
```

#### `ensureExplicitCapacity()`

判断是否需要扩容，并修改`modCount`记录数组变化。这里需要明白一点，该方法没必要返回`bool`值，因为不能因为容量不够就不放

```java
    private void ensureExplicitCapacity(int minCapacity) {
      // 记录数组被修改
      modCount++;
      // 如果我们期望的最小容量大于目前数组的长度，那么就扩容
      // 注：这里当minCapacity=length时也不扩容
      if (minCapacity - elementData.length > 0)
        grow(minCapacity);
    }
```

#### `grow()`

执行扩容，因为数组在创建时大小就确定了，所以所谓的扩容并不是将当前数组变大了，而是创建一个新的大数组，然后将原来数组元素拷贝过去，最后再将`elementData`指针指向这个新数组。

```java
    private void grow(int minCapacity) {
      int oldCapacity = elementData.length;
      // newCapacity = 1.5 oldCapacity
      int newCapacity = oldCapacity + (oldCapacity >> 1);
    
      // 如果扩容后的值 < 我们的期望值，就以期望值进行扩容
      if (newCapacity - minCapacity < 0)
        newCapacity = minCapacity;
    
      // 如果扩容后的值 > jvm 所能分配的数组的最大值，那么就用 Integer 的最大值
      if (newCapacity - MAX_ARRAY_SIZE > 0)
        newCapacity = hugeCapacity(minCapacity);
     
      // 通过复制进行扩容
      elementData = Arrays.copyOf(elementData, newCapacity);
    }
```

我们需要注意的四点是：

1. 新增时，并没有对值进行严格的校验，所以 `ArrayList` 是允许 null 值的。
2. 扩容的规则并不是翻倍，是原来容量大小 + 容量大小的一半，即原来容量的 1.5 倍。
3. `ArrayList `中的数组的最大值是` Integer.MAX_VALUE`，超过这个值，`JVM` 就不会给数组分配内存空间了。 源码在扩容的时候，有数组大小溢出意识，就是说扩容后数组下界不能小于 0，上界不能大于 Integer 的最大值
4. 扩容完成之后，赋值是非常简单的，直接往数组上添加元素即可：`elementData [size++] = e`。也正是通过这种简单赋值，没有任何锁控制，所以这里的操作是线程不安全的

**扩容的本质**

扩容是通过这行代码来实现的：`Arrays.copyOf(elementData, newCapacity);`，这行代码描述的本质是数组之间的拷贝，扩容是会先新建一个符合我们预期容量的新数组，然后把老数组的数据拷贝过去，我们通过 `System.arraycopy` 方法进行拷贝，此方法是 native 的方法，源码如下：

```java
    /**
     * @param src     被拷贝的数组
     * @param srcPos  从数组那里开始
     * @param dest    目标数组
     * @param destPos 从目标数组那个索引位置开始拷贝
     * @param length  拷贝的长度 
     * 此方法是没有返回值的，通过 dest 的引用进行传值
     */
    public static native void arraycopy(Object src, int srcPos,
                                        Object dest, int destPos,
                                        int length);
```

我们可以通过下面这行代码进行调用，`newElementData `表示新的数组：

```java
    System.arraycopy(elementData, 0, newElementData, 0,Math.min(elementData.length,newCapac
```

### 2.2.3 删除

#### `remove()`

寻找要删除元素的索引

```java
    public boolean remove(Object o) {
      // 如果要删除的值是 null，找到第一个值是 null 的删除
      // 注：这里把null单独出来，是因为e[idx]==o, 而非空是e[idx].equals(o)
      if (o == null) {
        for (int index = 0; index < size; index++)
          if (elementData[index] == null) {
            fastRemove(index);
            return true;
          }
      } else {
        // 如果要删除的值不为 null，找到第一个和要删除的值相等的删除
        for (int index = 0; index < size; index++)
          // 这里是根据  equals 来判断值相等的，相等后再根据索引位置进行删除
          if (o.equals(elementData[index])) {
            fastRemove(index);
            return true;
          }
      }
      return false;
    }
```

我们需要注意的两点是：

- 新增的时候是没有对 null 进行校验的，所以删除的时候也是允许删除 null 值的
- 找到值在数组中的索引位置，是通过 equals 来判断的，如果数组元素不是基本类型（Integer，String等），而是自定义类型（如User，Item等），则需要重写equals方法

#### `fastRemove()`

执行删除，即数组拷贝

```java
    private void fastRemove(int index) {
      // 记录数组的结构要发生变动了
      modCount++;
      // numMoved 表示删除 index 位置的元素后，需要从 index 后移动多少个元素到前面去
      // 减 1 的原因，是因为 size 从 1 开始算起，index 从 0开始算起
      int numMoved = size - index - 1;
      if (numMoved > 0)
        // 从 index +1 位置开始被拷贝，拷贝的起始位置是 index，长度是 numMoved
        System.arraycopy(elementData, index+1, elementData, index, numMoved);
      //数组最后一个位置赋值 null，帮助 GC
      elementData[--size] = null;
    }
```

#### `removeAll()`

批量删除

```java
    public boolean removeAll(Collection<?> c) {
        Objects.requireNonNull(c);
        return batchRemove(c, false);
      }
```

#### `batchRemove()`

`ArrayList` 在批量删除时，如果程序执行正常，只有一次 for 循环，如果程序执行异常，才会加一次拷贝

```java
    // complement 参数默认是 false,false 的意思是数组中不包含 c 中数据的节点往头移动
    // true 意思是数组中包含 c 中数据的节点往头移动，这个是根据你要删除数据和原数组大小的比例来决定的
    // 如果你要删除的数据很多，选择 false 性能更好，当然 removeAll 方法默认就是 false。
    private boolean batchRemove(Collection<?> c, boolean complement) {
      final Object[] elementData = this.elementData;
      // r 表示当前循环的位置、w 位置之前都是不需要被删除的数据，w 位置之后都是需要被删除的数据
      int r = 0, w = 0;
      boolean modified = false;
      
        // 双指针执行删除
        try {
        // 从 0 位置开始判断，当前数组中元素是不是要被删除的元素，不是的话移到数组头
        for (; r < size; r++)
          if (c.contains(elementData[r]) == complement)
            elementData[w++] = elementData[r];
      	} finally {
        // r 和 size 不等，说明在 try 过程中发生了异常，在 r 处断开
        // 把 r 位置之后的数组移动到 w 位置之后(r 位置之后的数组数据都是没有判断过的数据，
        // 这样不会影响没有判断的数据，判断过的数据可以被删除）
        if (r != size) {
          System.arraycopy(elementData, r,
                           elementData, w,
                           size - r);
          w += size - r;
        }
            
        // w != size 说明数组中是有数据需要被删除的
        // 如果 w、size 相等，说明没有数据需要被删除
        if (w != size) {
          // w 之后都是需要删除的数据，赋值为空，帮助 gc。
          for (int i = w; i < size; i++)
            elementData[i] = null;
          modCount += size - w;
          size = w;
          modified = true;
        }
      }
      return modified;
    }
```

### 2.2.4 修改

#### `set()`

修改指定索引的元素

```java
    public E set(int index, E element) {
        rangeCheck(index);
    
        E oldValue = elementData(index);
        elementData[index] = element;
        return oldValue;
    }
```

### 2.2.5 迭代器

#### `iterator()`

iterator 方法的作用是给用户返回迭代器

```java
    public Iterator<E> iterator() {
        return new Itr();
    }

    /**
    *Itr是对迭代器的实现
    */
    private class Itr implements Iterator<E>{
        
        // 迭代过程中，下一个元素的位置，默认从 0 开始。
        int cursor;
        
    	// 记录当前元素索引，因为next获取到元素后cursor++
        // 单独出来的意义还在于多线程下防止重复删除，因为删除后
        int lastRet = -1; 
    	
        // expectedModCount 表示迭代过程中，期望的版本号；modCount 表示数组实际的版本号
        int expectedModCount = modCount;
        
        //...
    }
```

- `modCount`：保证在当前迭代中，不在对集合进行增加删除操作，add/remove均会改变modCount
- `expectedModCount`：记录在迭代开始前的`modCount`，在迭代过程中若出现变化则抛异常

#### `hasNext()`

判断是否还有下一个被迭代元素，即是否已经到数组尾部了（index=length-1）

```java
    public boolean hasNext() {
           // cursor 表示下一个元素的位置，size 表示实际大小，如果两者相等，说明已经没有元素可以迭代了，	    
           // 如果不等，说明还可以迭代
      	   return cursor != size;
    }
```

#### `next()`

返回当前元素，并为下一次迭代做准备（cursor+1）。这里注意一点，如果在迭代时，数组被修改了，那迭代就出错了，所以迭代时原则上不允许增删(可以修改set)

```java
    public E next() {
           //迭代过程中，判断版本号有无被修改，有被修改，抛 ConcurrentModificationException 异常
           // 注：增、删都会引起modCount改变，但修改（set）不会
           checkForComodification();
           //本次迭代过程中，元素的索引位置
           int i = cursor;
           if (i >= size)
              throw new NoSuchElementException();
           // 注：ArrayList.this.~可以拿到外部类的属性
           Object[] elementData = ArrayList.this.elementData;
           if (i >= elementData.length)
              throw new ConcurrentModificationException();
            // 下一次迭代时，元素的位置，为下一次迭代做准备
            cursor = i + 1;
            // 返回元素值
            return (E) elementData[lastRet = i];
        }
        
        // 版本号比较
        final void checkForComodification() {
          if (modCount != expectedModCount)
            throw new ConcurrentModificationException();
        }
```

#### `remove()`

提供一个迭代时，可以删除当前元素的方法 */

```java
    public void remove() {
          // 如果上一次操作时，数组的位置已经小于 0 了，说明数组已经被删除完了
          if (lastRet < 0)
            throw new IllegalStateException();
          //迭代过程中，判断版本号有无被修改，有被修改，抛 ConcurrentModificationException 异常
          checkForComodification();
    
          try {
            // 调用ArrayList的删除方法，即modCount也会++
            ArrayList.this.remove(lastRet);
            // 更新cursor，其实也就是回退一位
            cursor = lastRet;
            // -1 表示元素已经被删除，这里也防止重复删除
            lastRet = -1;
            // 删除元素时 modCount 的值已经发生变化，在此赋值给 expectedModCount，保证下次迭代时一致
            expectedModCount = modCount;
          } catch (IndexOutOfBoundsException ex) {
            throw new ConcurrentModificationException();
          }
     }
```

#### `toArray()`

常用于将List转为数组

```java
    public Object[] toArray() {
        return Arrays.copyOf(elementData, size);
    }
```

在`ArrayList`中还有一个方法：`toArray(T[])`。但该方法需注意size与length的关系，注意避免出现错误

```java
    public <T> T[] toArray(T[] a) {
      // 如果数组长度不够，按照 List 的大小进行拷贝，return 的时候返回的都是正确的数组
      if (a.length < size)
        return (T[]) Arrays.copyOf(elementData, size, a.getClass());
        
      // 长度刚好则对传入数组进行拷贝
      System.arraycopy(elementData, 0, a, 0, size);
        
      // 数组长度大于 List 大小的，赋值为 null（其后空间GC）
      if (a.length > size)
        a[size] = null;
      return a;
    }
```

## 2.3 question

### 2.3.1 说说对 `ArrayList` 的理解？

`ArrayList` 内容很多，可以先从总体架构入手，然后再以某个细节作为突破口，比如这样：`ArrayList `底层数据结构是个数组，其` API` 都做了一层对数组底层访问的封装，比如说 add 方法的过程是……

另外，对 `LinkedList` 的理解也是同样套路。

### 2.3.2 扩容相关问题

**`ArrayList `无参数构造器构造，现在 add 一个值进去，此时数组的大小是多少，下一次扩容前最大可用大小是多少？**

答：此处数组的大小是 1，下一次扩容前最大可用大小是 10，因为 `ArrayList` 第一次扩容时，是有默认值的，默认值是 10，在第一次 add 一个值进去时，数组的可用大小被扩容到 10 了。

**如果连续往 list 里面新增值，增加到第 11 个的时候，数组的大小是多少？**

答：这里实际问的是扩容的公式，当增加到 11 的时候，此时我们希望数组的大小为 11，但实际上数组的最大容量只有 10，不够了就需要扩容，扩容的公式是：`oldCapacity + (oldCapacity>> 1)`，`oldCapacity` 表示数组现有大小，目前场景计算公式是：10 + 10 ／2 = 15，然后我们发现 15 已经够用了，所以数组的大小会被扩容到 15。

**数组初始化，被加入一个值后，如果使用 `addAll` 方法，一下子加入 15 个值，那么最终数组的大小是多少？**

答：在上一题中已经计算出来数组在加入一个值后，实际大小是 1，最大可用大小是 10 ，现在需要一下子加入 15 个值，那我们期望数组的大小值就是 16，此时数组最大可用大小只有 10，明显不够，需要扩容，扩容后的大小是：10 + 10 ／2 = 15，这时候发现扩容后的大小仍然不到我们期望的值 16，这时候源码中有一种策略如下：

```java
// newCapacity 本次扩容的大小，minCapacity 我们期望的数组最小大小
// 如果扩容后的值 < 我们的期望值，我们的期望值就等于本次扩容的大小
if (newCapacity - minCapacity < 0)
    newCapacity = minCapacity;
```

所以最终数组扩容后的大小为 16。具体源码请参考grow方法。

**现在有一个很大的数组需要拷贝，原数组大小是 `5k`，请问如何快速拷贝？**

答：因为原数组比较大，如果新建新数组的时候，不指定数组大小的话，就会频繁扩容，频繁扩容就会有大量拷贝的工作，造成拷贝的性能低下，所以在新建数组时，指定新数组的大小为` 5k` 即可。

**源码扩容过程有什么值得借鉴的地方？**

答：有两点：

- 扩容的思想值得学习，通过自动扩容的方式，让使用者不用关心底层数据结构的变化，封装得很好，1.5 倍的扩容速度，可以让扩容速度在前期缓慢上升，在后期增速较快，大部分工作中要求数组的值并不是很大，所以前期增长缓慢有利于节省资源，在后期增速较快时，也可快速扩容。
- 扩容过程中，有数组大小溢出的意识，比如要求扩容后的数组大小，不能小于 0，不能大于 Integer 的最大值。

### 2.3.3  删除相关问题

**有一个 `ArrayList`，数据是 2、3、3、3、4，中间有三个 3，现通过 `for (int i=0;i<list.size ();i++) `的方式，想把值是 3 的元素删除，请问可以删除干净么？最终删除的结果是什么，为什么？删除代码如下：**

```java
List<String> list = new ArrayList<String>() {{
  add("2");
  add("3");
  add("3");
  add("3");
  add("4");
}};
for (int i = 0; i < list.size(); i++) {
  if (list.get(i).equals("3")) {
    list.remove(i);
  }
}
```

答：不能删除干净，最终删除的结果是 2、3、4，有一个 3 删除不掉，原因我们看下图： [![图片描述](https://camo.githubusercontent.com/79b851473bbda763ed5ac9d568f5ff8a2be1dc22f8f566635d860dc7f23db746/68747470733a2f2f696d672d626c6f672e6373646e696d672e636e2f696d675f636f6e766572742f61343639303039666436386166643539333431663131333233366239356235662e706e67)](https://camo.githubusercontent.com/79b851473bbda763ed5ac9d568f5ff8a2be1dc22f8f566635d860dc7f23db746/68747470733a2f2f696d672d626c6f672e6373646e696d672e636e2f696d675f636f6e766572742f61343639303039666436386166643539333431663131333233366239356235662e706e67)从图中我们可以看到，每次删除一个元素后，该元素后面的元素就会往前移动，而此时循环的 i 在不断地增长，最终会使每次删除 3 的后一个 3 被遗漏，导致删除不掉。

**还是上面的 `ArrayList `数组，我们通过增强 for 循环进行删除，可以么？**

答：不可以，会报错。因为增强 for 循环过程其实调用的就是迭代器的 next () 方法，当你调用 `list.remove ()` 方法进行删除时，`modCount `的值会 +1，而这时候迭代器中的 `expectedModCount `的值却没有变，导致在迭代器下次执行 next () 方法时，`expectedModCount != modCount `就会报 `ConcurrentModificationException` 的错误。

**还是上面的数组，如果删除时使用` Iterator.remove () `方法可以删除么，为什么？**

答：可以的，因为` Iterator.remove () `方法在执行的过程中，会把最新的` modCount` 赋值给 `expectedModCount`，这样在下次循环过程中，`modCount` 和 `expectedModCount `两者就会相等。

**以上三个问题对于 `LinkedList `也是同样的结果么？**

答：是的，虽然 `LinkedList` 底层结构是双向链表，但对于上述三个问题，结果和` ArrayList` 是一致的。

### 2.3.4 与`LinkedList`对比的问题

**`ArrayList `和 `LinkedList `有何不同？**

答：可以先从底层数据结构开始说起，然后以某一个方法为突破口深入，比如：

- 最大的不同是两者底层的数据结构不同，`ArrayList` 底层是数组，`LinkedList `底层是双向链表
- 两者的数据结构不同也导致了操作的 `API `实现有所差异，拿新增实现来说，`ArrayList` 会先计算并决定是否扩容，然后把新增的数据直接赋值到数组上，而` LinkedList` 仅仅只需要改变插入节点和其前后节点的指向位置关系即可。

**`ArrayList` 和 `LinkedList` 应用场景有何不同？**

答：

- ArrayList 更适合于快速的查找匹配，不适合频繁新增删除，像工作中经常会对元素进行匹配查询的场景比较合适
- LinkedList 更适合于经常新增和删除，对查询反而很少的场景。

**`ArrayList` 和` LinkedList `两者有没有最大容量？**

答：

- `ArrayList` 有最大容量的，为 Integer 的最大值，大于这个值` JVM `是不会为数组分配内存空间的
- `LinkedList `底层是双向链表，理论上可以无限大。但源码中，`LinkedList` 实际大小（size）用的是 int 类型，这也说明了 `LinkedList `不能超过 Integer 的最大值，不然会溢出。

**`ArrayList` 和` LinkedList `是如何对 null 值进行处理的？**

答：

- `ArrayList `允许 null 值新增，也允许 null 值删除。删除 null 值时，是从头开始，找到第一值是 null 的元素删除
- `LinkedList `新增删除时对 null 值没有特殊校验，是允许新增和删除的。

### 2.3.5 线程安全问题

**`ArrayList` 和 `LinedList `是线程安全的么，为什么？**

答：

- 当两者作为非共享变量时，比如说仅仅是在方法里面的局部变量时，是没有线程安全问题的，只有当两者是共享变量时，才会有线程安全问题。
- 主要的问题点在于多线程环境下，所有线程任何时刻都可对数组和链表进行操作，这会导致值被覆盖，甚至混乱的情况。就像`ArrayList` 自身的 `elementData`、`size`、`modConut` 在进行各种操作时，都没有加锁，而且这些变量的类型并非是可见（volatile）的，所以如果多个线程对这些变量进行操作时，可能会有值被覆盖的情况。

如果有线程安全问题，在迭代的过程中，会频繁报 `ConcurrentModificationException` 的错误，意思是在我当前循环的过程中，数组或链表的结构被其它线程修改了

**如何解决线程安全问题？**

答：Java 源码中推荐使用` Collections#synchronizedList` 进行解决，`Collections#synchronizedList` 的返回值是 List 的每个方法都加了 `synchronized `锁，保证了在同一时刻，数组和链表只会被一个线程所修改，但是性能大大降低，具体实现源码：

```JAVA
public boolean add(E e) {
    synchronized (mutex) {// synchronized 是一种轻量锁，mutex 表示一个当前 SynchronizedList
        return c.add(e);
    }
}
```

另外，还可以采用` JUC `的 `CopyOnWriteArrayList` 并发 List 来解决。



## 3.`LinkedList `

### 3.1 结构

`LinkedList `继承关系，核心成员变量，主要构造函数：

```java
    public class LinkedList<E>
        extends AbstractSequentialList<E>
        implements List<E>, Deque<E>, Cloneable, java.io.Serializable {
     	
     	// Node，双向链表
        private static class Node<E> {
            E item;// 节点值
            Node<E> next; // 指向的下一个节点
            Node<E> prev; // 指向的前一个节点
    
            // 初始化参数顺序分别是：前一个节点、本身节点值、后一个节点
            Node(Node<E> prev, E element, Node<E> next) {
                this.item = element;
                this.next = next;
                this.prev = prev;
            }
    	}
    	
    	//------------------------成员变量-------------------------------------
    	
    	transient int size = 0;
    	
    	// 记录头结点，它的前一个结点=null
        transient Node<E> first;
    	
    	// 记录尾结点，它的后一个结点=null
    	// 当 first = last = null时表示链表为空
    	// 当 first = last != null时表示只有一个节点
        transient Node<E> last;
        
        //--------------------------构造方法-------------------------------------
        
        public LinkedList() {
        }
        
         public LinkedList(Collection<? extends E> c) {
            this();
            addAll(c);
        }
        
        // ........
    }
```

### 3.2 方法解析&`api`

#### 3.2.1 尾插

追加节点时，我们可以选择追加到链表头部，还是追加到链表尾部，add 方法默认是从尾部开始追加，`addFirst `方法是从头部开始追加，我们分别来看下两种不同的追加方式：

##### **add()**

```java
    public boolean add(E e) {
            linkLast(e);
            return true;
    }
```

##### **`linkLast()`**

尾插的核心逻辑如下：

- `newNode.pre` = `last`
- `last.next` = `newNode` （注：考虑last=null情况（链表为空，这时仅更新头结点即可））
- `last` = `newNode`

```java
    void linkLast(E e) {
        // 把尾节点数据暂存，为last.next做准备，其实改变一下顺序就可以不要这个l了
        final Node<E> l = last;
        
        final Node<E> newNode = new Node<>(l, e, null); // 1
        last = newNode; // 2
    	
        // 空链表，l=null,l.next报空指针
        if (l == null)
            first = newNode;
        else
            l.next = newNode; // 3
        
        // size和版本更改
        size++;
        modCount++;
    }
```

#### 3.2.2 头插

要对LinkedList头插时是调用addFirst方法

##### `addFirst()`

```java
    public void addFirst(E e) {
            linkFirst(e);
    }
```

##### `linkFirst()`

头插核心逻辑如下：

- `newNode.next = first;` `* first.prev = newNode; `（注：考虑first=null（链表为空，只用更新last即可））`* first = newNode`;

```java
    private void linkFirst(E e) {
        // 头节点赋值给临时变量
        final Node<E> f = first;
        
        final Node<E> newNode = new Node<>(null, e, f); // 1
    
        first = newNode;  // 2
        
        // 链表为空，f=null, f.prev报空指针
        if (f == null)
            last = newNode;
        else
            f.prev = newNode;  // 3
        
        // 更新size和版本号
        size++;
        modCount++;
    }
```

#### 3.2.3 删除指定元素

节点删除的方式和追加类似，我们可以删除指定元素，或者从头部（尾部）删除，删除操作会把节点的值，前后指向节点都置为 null，帮助 GC 进行回收

##### `remove()`

删除指定元素；该方法在此处的作用是找到要删除的节点。注意，只有链表有这个节点且成功删除才返回true

```java
    public boolean remove(Object o) { 
            if (o == null) {
                for (Node<E> x = first; x != null; x = x.next) {
                    // null用 == 判断
                    if (x.item == null) {
                        unlink(x);
                        return true;
                    }
                }
            } else {
                for (Node<E> x = first; x != null; x = x.next) {
                    // 调用equals判断，若传入的类无equals需要重写
                    if (o.equals(x.item)) {
                        unlink(x);
                        return true;
                    }
                }
            }
            return false;  // 链表无要删除元素，或链表为空
    }
```

注：remove还可以根据索引删除

```java
    public E remove(int index) { 
            checkElementIndex(index); // 链表为空，抛出异常
            return unlink(node(index));
    }
```

##### unlink()

删除的核心逻辑如下：

- `x.prev.next = x.next `（注：考虑`x.prev=null`(x是first，直接更新first））
- `x.next.prev = x.prev.prev` （注：考虑`x.next=null`(x是last，直接更新last））

```java
    E unlink(Node<E> x) {
            // assert x != null;
            final E element = x.item;
            final Node<E> next = x.next;
            final Node<E> prev = x.prev;
    		
         	// 如果prev=null,则当前节点为头结点
            if (prev == null) {
                // 直接将头结点赋成next
                first = next;
            } else {
                prev.next = next; // 1
                x.prev = null; // 帮助 GC 回收该节点
            }
    		
         	// 如果next=null，则当前节点为尾结点
            if (next == null) {
                last = prev;
            } else {
                next.prev = prev; // 2
                x.next = null; // 帮助 GC 回收该节点
            }
    		
            x.item = null; // 帮助 GC 回收该节点
         
         	// 修改size及版本
            size--;
            modCount++;
         
            return element;
        }
```

#### 3.2.4 删除头节点

##### remove()

删除头节点，队列为空时抛出异常。这里注意，与删除指定元素时需要传入一个参数，而删除头节点时为空参。

```java
    public E remove() {
            return removeFirst();
    }
```

##### `removeFirst()`

判断当前链表时否为空

```java
    public E removeFirst() {
            final Node<E> f = first;
            if (f == null)
                throw new NoSuchElementException();
            return unlinkFirst(f);
     }
```

##### `unLinkFirst()`

执行删除头节点，具体删除逻辑如下

- `first.next.pre = null`;（注：考虑`first=null`（链表为空），` first.next=null`（尾结点，即链表仅一个节点））
- `first = first.next;`

```java
    private E unlinkFirst(Node<E> f) {
        
        final E element = f.item; // 拿出头节点的值，作为方法的返回值
        final Node<E> next = f.next; // 拿出头节点的下一个节点
        
        //帮助 GC 回收头节点
        f.item = null;
        f.next = null;
        
        first = next;  // 1
        
        // next为空表示链表只有一个节点
        if (next == null)
            last = null;
        else
            next.prev = null; // 2
        
        //修改链表大小和版本
        size--;
        modCount++;
        return element;
    }
```

从源码中我们可以了解到，链表结构的节点新增、删除都非常简单，仅仅把前后节点的指向修改下就好了，所以 LinkedList 新增和删除速度很快。

#### 3.2.5 查询

链表查询某一个节点是比较慢的，需要挨个循环查找才行，我们看看 `LinkedList` 的源码是如何寻找节点的

##### `get()`

根据索引进行查找

```java
   public E get(int index) {
            checkElementIndex(index);
            return node(index).item;
    }
```

##### `node()`

```java
    Node<E> node(int index) {
        // 如果 index 处于队列的前半部分，从头开始找，size >> 1 是 size 除以 2 的意思。
        if (index < (size >> 1)) {
            // 取头节点
            Node<E> x = first;
            // 直到 for 循环到 index 的前一个 node 停止
            for (int i = 0; i < index; i++)
                x = x.next;
            return x;
        } else {// 如果 index 处于队列的后半部分，从尾开始找
            // 取尾结点
            Node<E> x = last;
            // 直到 for 循环到 index 的后一个 node 停止
            for (int i = size - 1; i > index; i--)
                x = x.prev;
            return x;
        }
    }
```

从源码中我们可以发现，`LinkedList `并没有采用从头循环到尾的做法，而是采取了简单二分法，首先看看 index 是在链表的前半部分，还是后半部分。如果是前半部分，就从头开始寻找，反之亦然。通过这种方式，使循环的次数至少降低了一半，提高了查找的性能，这种思想值得我们借鉴

### 3.3迭代器

因为 `LinkedList` 要实现双向的迭代访问，所以使用 Iterator 接口肯定不行了，因为 Iterator 只支持从头到尾的访问。Java 新增了一个迭代接口，叫做：`ListIterator`，这个接口提供了向前和向后的迭代方法，如下所示：

| 迭代顺序                                                    | 方法                                       |
| ----------------------------------------------------------- | ------------------------------------------ |
| 从尾到头迭代方法                                            | `hasPrevious`、`previous`、`previousIndex` |
| 从头到尾迭代方法                                            | `hasNext`、`next`、`nextIndex`             |
| `**listIterator()**`                                        |                                            |
| 返回迭代器，可以传入index，表示从指定节点开始迭代，可前可后 |                                            |

```java
    public ListIterator<E> listIterator(int index) {
        checkPositionIndex(index);
        return new ListItr(index);
    }

    /**
    *ListItr,双向迭代器
    */
    private class ListItr implements ListIterator<E> {
        private Node<E> lastReturned;//上一次执行 next() 或者 previos() 方法时的节点位置
        private Node<E> next;//下一个节点
        private int nextIndex;//下一个节点的位置
        //expectedModCount：期望版本号；modCount：目前最新版本号
        private int expectedModCount = modCount;
        
        ListItr(int index) {
              // assert isPositionIndex(index);
              next = (index == size) ? null : node(index);
              nextIndex = index;
        }
    }
```

#### 3.3.1 从前向后迭代

##### `hasNext()`

判断还有没有下一个元素，还是通过index和size控制

```java
    public boolean hasNext() {
        return nextIndex < size;// 下一个节点的索引小于链表的大小，就有
    }
```

##### `next()`

取下一个元素，并后移

```java
    public E next() {
        //检查期望版本号有无发生变化
        checkForComodification();
        if (!hasNext())//再次检查
            throw new NoSuchElementException();
        // next 是当前节点，在上一次执行 next() 方法时被赋值的。
        // 第一次执行时，是在初始化迭代器的时候，next 被赋值的
        lastReturned = next;
        // next 是下一个节点了，为下次迭代做准备
        next = next.next;
        nextIndex++;
        return lastReturned.item;
    }
```

#### 3.3.2 从后向前迭代

##### `hasPrevious()`

如果上次节点索引位置大于 0，就还有节点可以迭代

```java
    public boolean hasPrevious() {
        return nextIndex > 0;
    }
```

##### `previous()`

```java
    public E previous() {
        checkForComodification();
        if (!hasPrevious())
            throw new NoSuchElementException();
        // next 为空场景：1:说明是第一次迭代，取尾节点(last);2:上一次操作把尾节点删除掉了
        // next 不为空场景：说明已经发生过迭代了，直接取前一个节点即可(next.prev)
        lastReturned = next = (next == null) ? last : next.prev;
        // 索引位置变化
        nextIndex--;
        return lastReturned.item;
    }
```

#### 3.3.3 删除：remove

迭代时，删除当前元素

```java 
  public void remove() {
        checkForComodification();
        // lastReturned 是本次迭代需要删除的值，分以下空和非空两种情况：
        // lastReturned 为空，说明调用者没有主动执行过 next() 或者 previos()，直接报错
        // lastReturned 不为空，是在上次执行 next() 或者 previos()方法时赋的值
        if (lastReturned == null)
            throw new IllegalStateException();
        Node<E> lastNext = lastReturned.next;
        //删除当前节点
        unlink(lastReturned);
        // next == lastReturned 的场景分析：从尾到头递归顺序，并且是第一次迭代，并且要删除最后一个元素的情况
        // 这种情况下，previous()方法里面设置了 lastReturned=next=last,所以 next 和l astReturned 会相等
        if (next == lastReturned)
            // 这时候 lastReturned 是尾节点，lastNext 是 null，所以 next 也是 null，	
            // 这样在 previous() 执行时，发现 next 是 null，就会把尾节点赋值给 next
            next = lastNext;
        else
            nextIndex--;
        lastReturned = null;
        expectedModCount++;
    }
```

### 3.4 Queue的实现

`LinkedList` 实现了 Queue 接口，在新增、删除、查询等方面增加了很多新的方法，这些方法在平时特别容易混淆，在链表为空的情况下，返回值也不太一样，下面列一个表格，方便大家记录：

|      | 返回异常  | 返回特殊值 | 底层实现                                       |
| ---- | --------- | ---------- | ---------------------------------------------- |
| 新增 | add()     | offer()    | 底层实现相同，offer直接调用add                 |
| 删除 | remove()  | poll()     | 链表为空时，remove 会抛出异常，poll 返回 null  |
| 查找 | element() | peek()     | 链表为空时，element 会抛出异常，peek 返回 null |

PS：`Queue` 接口注释建议 add 方法操作失败时抛出异常，但 `LinkedList `实现的 add 方法一直返回 true。` LinkedList `也实现了 `Deque `接口，对新增、删除和查找都提供从头开始，还是从尾开始两种方向的方法，比如 remove 方法，`Deque `提供了 `removeFirst `和` removeLast` 两种方向的使用方式，但当链表为空时的表现都和 remove 方法一样，都会抛出异常。