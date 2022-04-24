# Elasticsearch

## 什么是Elasticsearch(简称ES)

> Elasticsearch VS Solr: [https://www.cnblogs.com/jajian/p/9801154.html(opens new window)](https://www.cnblogs.com/jajian/p/9801154.html)

- 基于**Apache Lucene**构建的开源搜索引擎，提供一个分布式多用户能力的全文搜索引擎
- 用**Java**编写的，提供简单易用的**RESTFul API**，当前流行的企业级搜索引擎
- 轻松的**横向扩展**，可支持**PB**级的结构化或非结构化数据处理
- 可以准实时地快速存储、搜索、分析海量的数据(用于云计算中，能够达到实时搜索)

## 应用场景

- 海量数据分析引擎(聚合搜索)
- 站内搜索引擎
- 数据仓库

**Elasticsearch**有几个核心概念，从一开始理解这些概念会对整个学习过程有莫大的帮助

## 接近实时(NRT)

**Elasticsearch**是一个接近实时的搜索平台。这意味着，从索引一个文档直到这个文档能够被搜索到有一个轻微的延迟(通常是1秒)

## 集群(cluster)

一个集群就是由一个或多个节点组织在一起，它们共同持有你整个的数据，并一起提供索引和搜索功能。一个集群由一个唯一的名字标识，这个名字默认就是**Elasticsearch**。这个名字是重要的，因为一个节点只能通过指定某个集群的名字，来加入这个集群。在产品环境中显式地设定这个名字是一个好习惯，但是使用默认值来进行测试/开发也是不错的。

## 节点(node)

一个节点是你集群中的一个服务器，作为集群的一部分，它存储你的数据，参与集群的索引和搜索功能。和集群类似，一个节点也是由一个名字来标识的，默认情况下，这个名字是一个随机的漫威漫画角色的名字，这个名字会在启动的时候赋予节点。这个名字对于管理工作来说挺重要的，因为在这个管理过程中，你会去确定网络中的哪些服务器对应于**Elasticsearch**集群中的哪些节点。

一个节点可以通过配置集群名称的方式来加入一个指定的集群。默认情况下，每个节点都会被安排加入到一个叫做**Elasticsearch**的集群中，这意味着，如果你在你的网络中启动了若干个节点，并假定它们能够相互发现彼此，它们将会自动地形成并加入到一个叫做**Elasticsearch**的集群中。

在一个集群里，只要你想，可以拥有任意多个节点。而且，如果当前你的网络中没有运行任何**Elasticsearch**节点，这时启动一个节点，会默认创建并加入一个叫做**Elasticsearch**的集群。

## 索引(index)

一个索引就是一个拥有几分相似特征的文档的集合。比如说，你可以有一个客户数据的索引，另一个产品目录的索引，还有一个订单数据的索引。一个索引由一个名字来标识(必须全部是小写字母的)，并且当我们要对对应于这个索引中的文档进行索引、搜索、更新和删除的时候，都要使用到这个名字。索引类似于关系型数据库中**Database**的概念。在一个集群中，如果你想，可以定义任意多的索引。

## 类型(type)

在一个索引中，你可以定义一种或多种类型。一个类型是你的索引的一个逻辑上的分类/分区，其语义完全由你来定。通常，会为具有一组共同字段的文档定义一个类型。比如说，我们假设你运营一个博客平台并且将你所有的数据存储到一个索引中。在这个索引中，你可以为用户数据定义一个类型，为博客数据定义另一个类型，当然，也可以为评论数据定义另一个类型。类型类似于关系型数据库中**Table**的概念。

## 文档(document)

一个文档是一个可被索引的基础信息单元。比如，你可以拥有某一个客户的文档，某一个产品的一个文档，当然，也可以拥有某个订单的一个文档。文档以**JSON**(**Javascript Object Notation**)格式来表示，而**JSON**是一个到处存在的互联网数据交互格式。

在一个**index/type**里面，只要你想，你可以存储任意多的文档。注意，尽管一个文档，物理上存在于一个索引之中，文档必须被索引/赋予一个索引的**type**。文档类似于关系型数据库中**Record**的概念。实际上一个文档除了用户定义的数据外，还包括**_index**、**_type**和**_id**字段。

## 分片和复制(shards & replicas)

一个索引可以存储超出单个结点硬件限制的大量数据。比如，一个具有10亿文档的索引占据**1TB**的磁盘空间，而任一节点都没有这样大的磁盘空间；或者单个节点处理搜索请求，响应太慢。

为了解决这个问题，**Elasticsearch**提供了将索引划分成多份的能力，这些份就叫做分片。当你创建一个索引的时候，你可以指定你想要的分片的数量。每个分片本身也是一个功能完善并且独立的“索引”，这个“索引”可以被放置到集群中的任何节点上。

分片之所以重要，主要有两方面的原因：

- 允许你水平分割/扩展你的内容容量
- 允许你在分片（潜在地，位于多个节点上）之上进行分布式的、并行的操作，进而提高性能/吞吐量

至于一个分片怎样分布，它的文档怎样聚合回搜索请求，是完全由**Elasticsearch**管理的，对于作为用户的你来说，这些都是透明的。

在一个网络/云的环境里，失败随时都可能发生，在某个分片/节点不知怎么的就处于离线状态，或者由于任何原因消失了。这种情况下，有一个故障转移机制是非常有用并且是强烈推荐的。为此目的，**Elasticsearch**允许你创建分片的一份或多份拷贝，这些拷贝叫做复制分片，或者直接叫复制。复制之所以重要，主要有两方面的原因：

- 在分片/节点失败的情况下，提供了高可用性。因为这个原因，注意到复制分片从不与原/主要（original/primary）分片置于同一节点上是非常重要的。
- 扩展你的搜索量/吞吐量，因为搜索可以在所有的复制上并行运行

总之，每个索引可以被分成多个分片。一个索引也可以被复制0次（意思是没有复制）或多次。一旦复制了，每个索引就有了主分片（作为复制源的原来的分片）和复制分片（主分片的拷贝）之别。分片和复制的数量可以在索引创建的时候指定。在索引创建之后，你可以在任何时候动态地改变复制数量，但是不能改变分片的数量。

默认情况下，**Elasticsearch**中的每个索引被分片5个主分片和1个复制，这意味着，如果你的集群中至少有两个节点，你的索引将会有5个主分片和另外5个复制分片（1个完全拷贝），这样的话每个索引总共就有10个分片。一个索引的多个分片可以存放在集群中的一台主机上，也可以存放在多台主机上，这取决于你的集群机器数量。主分片和复制分片的具体位置是由ES内在的策略所决定的

## 形象比喻

- 索引：含有相同属性的文档集合
- 类型：索引可以定义一个或多个类型，文档必须属于一个类型
- 文档：可以被索引的基础数据单位
- 分片：每个索引都有多个分片，每个分片都是**Lucene**索引
- 备份：拷贝一份分片就完成分片的备份

百货大楼里有各式各样的商品，例如书籍、笔、水果等。书籍可以根据内容划分成不同种类，如科技类、教育类、悬疑推理等。悬疑推理类的小说中比较有名气的有《福尔摩斯探案集》、《白夜行》等

- 百货大楼 –> **Elasticsearch**数据库
- 书籍 –> 索引
- 悬疑推理 –> 类型
- 白夜行 –> 文档

## 数据结构表格对比

- **Mysql**和**Elasticsearch**对应关系

| Mysql               |     Elasticsearch     |
| :------------------ | :-------------------: |
| Database            |         Index         |
| Table               |         Type          |
| Row                 |       Document        |
| Column              |         Field         |
| Schema              |        Mapping        |
| Index               | Everything is indexed |
| SQL                 |       Query DSL       |
| SELECT * FROM table |    GET http://...     |
| UPDATE table SET    |    PUT http://...     |

- **Mysql**和**MongoDB**对应关系

| Mysql    |  MongoDB   |
| :------- | :--------: |
| Database |  Database  |
| Table    | Collection |
| Row      |  Document  |
| Column   |   Field    |

# 安装本地Elasticsearch

安装本地Elasticsearch，安装本地Elasticsearch-Head，本地Elasticsearch集群搭建

## 本地Elasticsearch单机搭建

当然首先要安装**JDK1.8**的环境及以上版本都行，不能低于**1.8**，安装**Windows**本地版，去**Elasticsearch**官网下载即可，不过找了很久都没有找到旧版本，最后下了最新版7.2，安装很简单，将下载的**zip**文件解压

#### 目录说明

| 目录名  |     说明     |
| :------ | :----------: |
| config  |   配置文件   |
| modules | 模块存放目录 |
| bin     |     脚本     |
| lib     |   第三方库   |
| plugins |  第三方插件  |

直接运行**bin**下的**elasticsearch.bat**这个文件即可启动，关闭窗口就是关闭服务，然后访问本机的**127.0.0.1:9200**即可，网页返回如下**JSON**

```json
{
  "name" : "WANG926454",
  "cluster_name" : "elasticsearch",
  "cluster_uuid" : "ht8iAPewRDidZk-qbZ2Eig",
  "version" : {
    "number" : "7.2.0",
    "build_flavor" : "default",
    "build_type" : "zip",
    "build_hash" : "508c38a",
    "build_date" : "2019-06-20T15:54:18.811730Z",
    "build_snapshot" : false,
    "lucene_version" : "8.0.0",
    "minimum_wire_compatibility_version" : "6.8.0",
    "minimum_index_compatibility_version" : "6.0.0-beta1"
  },
  "tagline" : "You Know, for Search"
}
```

复制代码

## 安装本地Elasticsearch-Head

一般情况下，我们都会通过一个可视化的工具来查看**ES**的运行状态和数据。这个工具我们一般选Head(opens new window)

- 提供了友好的**Web**界面，解决数据在界面显示问题
- 实现基本信息的查看和**Restful**请求的模拟以及数据的基本检索

**Elasticsearch-Head**依赖于**Node.js**，需要安装**Node.js**，查看**Github**介绍，该工具能直接对**Elasticsearch**的数据进行增删改查，因此存在安全性的问题，建议生产环境下不要使用该插件，**Node.js**版本必须**requires node >= 6.0**，简单的菜鸟教程安装就行:[https://www.runoob.com/nodejs/nodejs-install-setup.html (opens new window)](https://www.runoob.com/nodejs/nodejs-install-setup.html)，我Node.js环境的很早就已经安装了，执行下面命令先安装**grunt**

```bash
npm install -g grunt-cli 
```

复制代码

安装完成查看版本

```bash
grunt -version
```

复制代码

显示版本号OK

```bash
grunt-cli v1.3.2
```

复制代码

去[Github (opens new window)](https://github.com/mobz/elasticsearch-head)下载**Elasticsearch-Head**工具，解压到你的**Elasticsearch**根路径下**D:\Tools\elasticsearch-7.2.0\elasticsearch-head-master**，修改**Gruntfile.js**配置文件，如下添加**hostname: '\*'**

```json
connect: {
	server: {
		options: {
			hostname: '*',
			port: 9100,
			base: '.',
			keepalive: true
		}
	}
}
```

复制代码

然后在**D:\Tools\elasticsearch-7.2.0\elasticsearch-head-master**目录先安装下启动运行**Head**插件

```bash
npm install
grunt server or npm run start
```

复制代码

进**http://localhost:9100**发现连接不上，还需要配置下**Elasticsearch**，修改**Elasticsearch**安装目录下的**config/elasticsearch.yml**配置文件，在最下面添加下面两句配置，开启跨域

```yml
# 如果启用了HTTP端口，那么此属性会指定是否允许跨源REST请求，默认true
http.cors.enabled: true
# 如果http.cors.enabled的值为true，那么该属性会指定允许REST请求来自何处，默认localhost
http.cors.allow-origin: "*"
```

复制代码

重新打开**elasticsearch.bat**，启动完成进去**http://localhost:9100**连接，OK

#### 集群健康值

- **red**(差): 集群健康状况很差，虽然可以查询，但是已经出现了丢失数据的现象
- **yellow**(中): 集群健康状况不是很好，但是集群可以正常使用
- **green**(优): 集群健康状况良好，集群正常使用

## 安装本地Elasticsearch集群(分布式)

- 安装说明，安装三个节点，一个**Master**，两个**Slave**，名称要相同，**9500**端口为**Master**节点，其余两个为**Slave**节点

| 集群名称    |    IP-端口     |
| :---------- | :------------: |
| myEsCluster | 127.0.0.1:9500 |
| myEsCluster | 127.0.0.1:9600 |
| myEsCluster | 127.0.0.1:9700 |

- ES安装包解压出三份ES，修改每个**Elasticsearch**安装目录下的**config/elasticsearch.yml**配置文件

#### Master配置说明

```yml
# 设置支持Elasticsearch-Head
http.cors.enabled: true
http.cors.allow-origin: "*"
# 设置集群Master配置信息
cluster.name: myEsCluster
# 节点的名字，一般为Master或者Slave
node.name: master
# 节点是否为Master，设置为true的话，说明此节点为Master节点
node.master: true
# 设置网络，如果是本机的话就是127.0.0.1，其他服务器配置对应的IP地址即可(0.0.0.0支持外网访问)
network.host: 127.0.0.1
# 设置对外服务的Http端口，默认为 9200，可以修改默认设置
http.port: 9500
# 设置节点间交互的TCP端口，默认是9300
transport.tcp.port: 9300
# 手动指定可以成为Master的所有节点的Name或者IP，这些配置将会在第一次选举中进行计算
cluster.initial_master_nodes: ["127.0.0.1"]
```

复制代码

#### Slave配置说明

```yml
# 设置集群Slave配置信息
cluster.name: myEsCluster
# 节点的名字，一般为Master或者Slave
node.name: slave1
# 节点是否为Master，设置为true的话，说明此节点为master节点
node.master: false
# 设置对外服务的Http端口，默认为 9200，可以修改默认设置
http.port: 9600
# 设置网络，如果是本机的话就是127.0.0.1，其他服务器配置对应的IP地址即可(0.0.0.0支持外网访问)
network.host: 127.0.0.1
# 集群发现
discovery.seed_hosts: ["127.0.0.1:9300"]
```

复制代码

```yml
# 设置集群Slave配置信息
cluster.name: myEsCluster
# 节点的名字，一般为Master或者Slave
node.name: slave2
# 节点是否为Master，设置为true的话，说明此节点为master节点
node.master: false
# 设置对外服务的Http端口，默认为 9200，可以修改默认设置
http.port: 9700
# 设置网络，如果是本机的话就是127.0.0.1，其他服务器配置对应的IP地址即可(0.0.0.0支持外网访问)
network.host: 127.0.0.1
# 集群发现
discovery.seed_hosts: ["127.0.0.1:9300"]
```

复制代码

- 最后两个**Slave**配置只需要改相应的端口号即可，一个**slave1**：9600，一个**slave2**：9700
- 配置后完成后，启动一个**Master**，两个**Slave**，还有**Elasticsearch-Head**服务，此时页面可以查看ES集群的状态
- 访问[http://localhost:9500/_cat/nodes?v(opens new window)](http://localhost:9500/_cat/nodes?v)

```text
ip        heap.percent ram.percent cpu load_1m load_5m load_15m node.role master name
127.0.0.1           18          87   6                          mdi       *      master
127.0.0.1           16          87   6                          di        -      slave1
127.0.0.1           16          87   6                          di        -      slave2
```

复制代码

- 访问[http://localhost:9100(opens new window)](http://localhost:9100/)

![图示](https://docs.dolyw.com/Project/Elasticsearch/image/20190802001.png)

#  安装本地IK分词插件

安装本地Elasticsearch的IK分词插件和拼音分词插件

## [#](https://note.dolyw.com/elasticsearch/02-LocalInstallationIK.html#安装本地elasticsearch的ik分词插件)安装本地Elasticsearch的IK分词插件

去[https://github.com/medcl/elasticsearch-analysis-ik/releases (opens new window)](https://github.com/medcl/elasticsearch-analysis-ik/releases)下载对应**Elasticsearch**版本的IK分词插件**elasticsearch-analysis-ik-7.3.0.zip**这个文件，打开可以看到如下文件

```bash
commons-codec-1.9.jar
commons-logging-1.2.jar
config/
elasticsearch-analysis-ik-7.2.0.jar
httpclient-4.5.2.jar
httpcore-4.4.4.jar
plugin-descriptor.properties
plugin-security.policy
```

复制代码

没问题，就解压到你安装的**Elasticsearch**目录的**plugins**目录下，例如我的路径是这样的**D:\Tools\elasticsearch-7.2.0\plugins\elasticsearch-analysis-ik-7.2.0**

重启**Elasticsearch**，可以看到控制台打印日志

```bash
loaded plugin [analysis-ik]
```

复制代码

测试一下

```json
POST /_analyze
{
  "text":"中华人民共和国国徽",
  "analyzer":"ik_smart"
}
```

复制代码

返回

```json
{
	"tokens": [
		{
			"token": "中华人民共和国",
			"start_offset": 0,
			"end_offset": 7,
			"type": "CN_WORD",
			"position": 0
		},
		{
			"token": "国徽",
			"start_offset": 7,
			"end_offset": 9,
			"type": "CN_WORD",
			"position": 1
		}
	]
}
```

复制代码

```json
POST /_analyze
{
  "text":"中华人民共和国国徽",
  "analyzer":"ik_max_word"
}
```

复制代码

返回

```json
{
	"tokens": [
		{
			"token": "中华人民共和国",
			"start_offset": 0,
			"end_offset": 7,
			"type": "CN_WORD",
			"position": 0
		},
		{
			"token": "中华人民",
			"start_offset": 0,
			"end_offset": 4,
			"type": "CN_WORD",
			"position": 1
		},
		{
			"token": "中华",
			"start_offset": 0,
			"end_offset": 2,
			"type": "CN_WORD",
			"position": 2
		},
		{
			"token": "华人",
			"start_offset": 1,
			"end_offset": 3,
			"type": "CN_WORD",
			"position": 3
		},
		{
			"token": "人民共和国",
			"start_offset": 2,
			"end_offset": 7,
			"type": "CN_WORD",
			"position": 4
		},
		{
			"token": "人民",
			"start_offset": 2,
			"end_offset": 4,
			"type": "CN_WORD",
			"position": 5
		},
		{
			"token": "共和国",
			"start_offset": 4,
			"end_offset": 7,
			"type": "CN_WORD",
			"position": 6
		},
		{
			"token": "共和",
			"start_offset": 4,
			"end_offset": 6,
			"type": "CN_WORD",
			"position": 7
		},
		{
			"token": "国",
			"start_offset": 6,
			"end_offset": 7,
			"type": "CN_CHAR",
			"position": 8
		},
		{
			"token": "国徽",
			"start_offset": 7,
			"end_offset": 9,
			"type": "CN_WORD",
			"position": 9
		}
	]
}
```

复制代码

IK分词插件就这样安装成功了

## [#](https://note.dolyw.com/elasticsearch/02-LocalInstallationIK.html#安装本地elasticsearch的拼音分词插件)安装本地Elasticsearch的拼音分词插件

去[https://github.com/medcl/elasticsearch-analysis-pinyin/releases (opens new window)](https://github.com/medcl/elasticsearch-analysis-pinyin/releases)下载对应**Elasticsearch**版本的IK分词插件**elasticsearch-analysis-pinyin-7.2.0.zip**这个文件，打开可以看到如下文件

```bash
elasticsearch-analysis-pinyin-7.2.0.jar
nlp-lang-1.7.jar
plugin-descriptor.properties
```

复制代码

没问题，就解压到你安装的**Elasticsearch**目录的**plugins**目录下，例如我的路径是这样的**D:\Tools\elasticsearch-7.2.0\plugins\elasticsearch-analysis-pinyin-7.2.0**

重启**Elasticsearch**，可以看到控制台打印日志

```bash
loaded plugin [analysis-pinyin]
```

复制代码

测试一下

```json
POST /_analyze
{
  "text":"中华人民共和国国徽",
  "analyzer":"pinyin"
}
```

复制代码

返回

```json
{
	"tokens": [
		{
			"token": "zhong",
			"start_offset": 0,
			"end_offset": 0,
			"type": "word",
			"position": 0
		},
		{
			"token": "zhrmghggh",
			"start_offset": 0,
			"end_offset": 0,
			"type": "word",
			"position": 0
		},
		{
			"token": "hua",
			"start_offset": 0,
			"end_offset": 0,
			"type": "word",
			"position": 1
		},
		{
			"token": "ren",
			"start_offset": 0,
			"end_offset": 0,
			"type": "word",
			"position": 2
		},
		{
			"token": "min",
			"start_offset": 0,
			"end_offset": 0,
			"type": "word",
			"position": 3
		},
		{
			"token": "gong",
			"start_offset": 0,
			"end_offset": 0,
			"type": "word",
			"position": 4
		},
		{
			"token": "he",
			"start_offset": 0,
			"end_offset": 0,
			"type": "word",
			"position": 5
		},
		{
			"token": "guo",
			"start_offset": 0,
			"end_offset": 0,
			"type": "word",
			"position": 6
		},
		{
			"token": "guo",
			"start_offset": 0,
			"end_offset": 0,
			"type": "word",
			"position": 7
		},
		{
			"token": "hui",
			"start_offset": 0,
			"end_offset": 0,
			"type": "word",
			"position": 8
		}
	]
}
```

复制代码

拼音分词插件就这样安装成功了

#### [#](https://note.dolyw.com/elasticsearch/02-LocalInstallationIK.html#使用ik和拼音插件-详细使用可以查看github的文档)使用IK和拼音插件(详细使用可以查看Github的文档)

- 创建Index，拼音分词过滤

```json
PUT /book
{
	"settings": {
		"analysis": {
			"analyzer": {
				"pinyin_analyzer": {
					"tokenizer": "my_pinyin"
				}
			},
			"tokenizer": {
				"my_pinyin": {
					"type": "pinyin",
					"keep_separate_first_letter": false,
					"keep_full_pinyin": true,
					"keep_original": true,
					"limit_first_letter_length": 16,
					"lowercase": true,
					"remove_duplicated_term": true
				}
			}
		}
	}
}
```

复制代码

返回

```json
{
    "acknowledged": true,
    "shards_acknowledged": true,
    "index": "book"
}
```

复制代码

- 创建Mapping，属性使用过滤，name开启拼音分词，content开启IK分词，describe开启拼音加IK分词

```json
POST /book/_mapping
{
	"properties": {
		"name": {
			"type": "keyword",
			"fields": {
				"pinyin": {
					"type": "text",
					"store": false,
					"term_vector": "with_offsets",
					"analyzer": "pinyin_analyzer",
					"boost": 10
				}
			}
		},
		"content": {
			"type": "text",
			"analyzer": "ik_max_word",
			"search_analyzer": "ik_smart"
		},
		"describe": {
			"type": "text",
			"analyzer": "ik_max_word",
			"search_analyzer": "ik_smart",
			"fields": {
				"pinyin": {
					"type": "text",
					"store": false,
					"term_vector": "with_offsets",
					"analyzer": "pinyin_analyzer",
					"boost": 10
				}
			}
		},
		"id": {
			"type": "long"
		}
	}
}
```

复制代码

返回

```json
{
    "acknowledged": true
}
```

复制代码

这样Index以及属性分词就开启了

#### [#](https://note.dolyw.com/elasticsearch/02-LocalInstallationIK.html#注-搜索时-先查看被搜索的词被分析成什么样的数据-如果你搜索该词输入没有被分析出的参数时-是查不到的)注：搜索时，先查看被搜索的词被分析成什么样的数据，如果你搜索该词输入没有被分析出的参数时，是查不到的！！！

# SpringBoot整合Elasticsearch

SpringBoot整合Elasticsearch的方式(TransportClient、Data-ES、Elasticsearch SQL、REST Client)

## [#](https://note.dolyw.com/elasticsearch/03-SpringBootES.html#代码地址)代码地址

- Github：[https://github.com/dolyw/ProjectStudy/tree/master/Elasticsearch/01-SpringBoot-ES-Local(opens new window)](https://github.com/dolyw/ProjectStudy/tree/master/Elasticsearch/01-SpringBoot-ES-Local)
- Gitee(码云)：[https://gitee.com/dolyw/ProjectStudy/tree/master/Elasticsearch/01-SpringBoot-ES-Local(opens new window)](https://gitee.com/dolyw/ProjectStudy/tree/master/Elasticsearch/01-SpringBoot-ES-Local)

## [#](https://note.dolyw.com/elasticsearch/03-SpringBootES.html#软件架构)软件架构

1. SpringBoot + REST Client

## [#](https://note.dolyw.com/elasticsearch/03-SpringBootES.html#项目介绍)项目介绍

SpringBoot整合ES的方式(TransportClient、Data-ES、Elasticsearch SQL、REST Client)

- **TransportClient**

TransportClient即将弃用，所以这种方式不考虑

- **Data-ES**

Spring提供的封装的方式，好像底层也是基于TransportClient，Elasticsearch7.0后的版本不怎么支持，SpringBoot的Spring Boot Data Elasticsearch Starter最高版本2.1.7.RELEASE下载的是Spring Data Elasticsearch的3.1.5版本对应的是Elasticsearch 6.4.3版本，Spring Data Elasticsearch最新版3.1.10对应的还是Elasticsearch 6.4.3版本，我安装的是最新的Elasticsearch 7.2.0版本所以也没办法使用

- **Elasticsearch SQL**

将Elasticsearch的`Query DSL`用`SQL`转换查询，早期有一个第三方的插件Elasticsearch-SQL，后来随着官方也开始做这方面，这个插件好像就没怎么更新了，有兴趣的可以查看：[https://www.cnblogs.com/jajian/p/10053504.html(opens new window)](https://www.cnblogs.com/jajian/p/10053504.html)

- **REST Client**

官方推荐使用，所以我们采用这个方式，这个分为两个**Low Level REST Client**和**High Level REST Client**，**Low Level REST Client**是早期出的API比较简陋了，还需要自己去拼写`Query DSL`，**High Level REST Client**使用起来更好用，更符合面向对象的感觉，两个都使用下吧

## [#](https://note.dolyw.com/elasticsearch/03-SpringBootES.html#预览图示)预览图示

- 查询

![查询](https://docs.dolyw.com/Project/Elasticsearch/image/20190815001.gif)

- 添加

![添加](https://docs.dolyw.com/Project/Elasticsearch/image/20190815002.gif)

- 修改

![修改](https://docs.dolyw.com/Project/Elasticsearch/image/20190815003.gif)

- 删除

![删除](https://docs.dolyw.com/Project/Elasticsearch/image/20190815004.gif)

## [#](https://note.dolyw.com/elasticsearch/03-SpringBootES.html#代码示例)代码示例

### [#](https://note.dolyw.com/elasticsearch/03-SpringBootES.html#配置代码)配置代码

创建一个`SpringBoot 2.1.3`的`Maven`项目，这块不再详细描述，添加如下`REST Client`依赖

```xml
<elasticsearch.version>7.2.0</elasticsearch.version>

<!-- Java Low Level REST Client -->
<dependency>
    <groupId>org.elasticsearch.client</groupId>
    <artifactId>elasticsearch-rest-client</artifactId>
    <version>${elasticsearch.version}</version>
</dependency>

<!-- Java High Level REST Client -->
<dependency>
    <groupId>org.elasticsearch.client</groupId>
    <artifactId>elasticsearch-rest-high-level-client</artifactId>
    <version>${elasticsearch.version}</version>
</dependency>
```

复制代码

- 配置文件

```yml
server:
    port: 8080

spring:
    thymeleaf:
        # 开发时关闭缓存不然没法看到实时页面
        cache: false
        # 启用不严格检查
        mode: LEGACYHTML5

# Elasticsearch配置
elasticsearch:
    hostname: 127.0.0.1
    port: 9500
```

复制代码

```java
@Configuration
public class RestClientConfig {

    @Value("${elasticsearch.hostname}")
    private String hostname;

    @Value("${elasticsearch.port}")
    private int port;

    /**
     * LowLevelRestConfig
     *
     * @param
     * @return org.elasticsearch.client.RestClient
     * @author wliduo[i@dolyw.com]
     * @date 2019/8/12 18:56
     */
    @Bean
    public RestClient restClient() {
        // 如果有多个从节点可以持续在内部new多个HttpHost，参数1是IP，参数2是端口，参数3是通信协议
        RestClientBuilder clientBuilder = RestClient.builder(new HttpHost(hostname, port, "http"));
        // 设置Header编码
        Header[] defaultHeaders = {new BasicHeader("content-type", "application/json")};
        clientBuilder.setDefaultHeaders(defaultHeaders);
        // 添加其他配置，这些配置都是可选的，详情配置可看https://blog.csdn.net/jacksonary/article/details/82729556
        return clientBuilder.build();
    }

    /**
     * HighLevelRestConfig
     *
     * @param
     * @return org.elasticsearch.client.RestClient
     * @author wliduo[i@dolyw.com]
     * @date 2019/8/12 18:56
     */
    @Bean
    public RestHighLevelClient restHighLevelClient() {
        // 如果有多个从节点可以持续在内部new多个HttpHost，参数1是IP，参数2是端口，参数3是通信协议
        return new RestHighLevelClient(RestClient.builder(new HttpHost(hostname, port, "http")));
    }

}
```

复制代码

- 这样就配置完成了

### [#](https://note.dolyw.com/elasticsearch/03-SpringBootES.html#controller入口)Controller入口

- LowLevelRestController

```java
@RestController
@RequestMapping("/low")
public class LowLevelRestController {

    /**
     * logger
     */
    private static final Logger logger = LoggerFactory.getLogger(LowLevelRestController.class);

    /**
     * PATTERN
     */
    private static Pattern PATTERN = Pattern.compile("\\s*|\t|\r|\n");

    @Autowired
    private RestClient restClient;

    /**
     * 同步执行HTTP请求
     *
     * @param
     * @return org.springframework.http.ResponseEntity<java.lang.String>
     * @throws IOException
     * @author wliduo[i@dolyw.com]
     * @date 2019/8/8 17:15
     */
    @GetMapping("/es")
    public ResponseBean getEsInfo() throws IOException {
        Request request = new Request("GET", "/");
        // performRequest是同步的，将阻塞调用线程并在请求成功时返回Response，如果失败则抛出异常
        Response response = restClient.performRequest(request);
        // 获取请求行
        RequestLine requestLine = response.getRequestLine();
        // 获取host
        HttpHost host = response.getHost();
        // 获取状态码
        int statusCode = response.getStatusLine().getStatusCode();
        // 获取响应头
        Header[] headers = response.getHeaders();
        // 获取响应体
        String responseBody = EntityUtils.toString(response.getEntity());
        return new ResponseBean(HttpStatus.OK.value(), "查询成功", JSON.parseObject(responseBody));
    }


    /**
     * 异步执行HTTP请求
     *
     * @param
     * @return org.springframework.http.ResponseEntity<java.lang.String>
     * @author wliduo[i@dolyw.com]
     * @date 2019/8/8 17:15
     */
    @GetMapping("/es/async")
    public ResponseBean asynchronous() {
        Request request = new Request("GET", "/");
        restClient.performRequestAsync(request, new ResponseListener() {
            @Override
            public void onSuccess(Response response) {
                logger.info("异步执行HTTP请求并成功");
            }

            @Override
            public void onFailure(Exception exception) {
                logger.info("异步执行HTTP请求并失败");
            }
        });
        return new ResponseBean(HttpStatus.OK.value(), "异步请求中", null);
    }

    /**
     * 分词分页查询列表
     *
     * @param page
	 * @param rows
	 * @param keyword
     * @return com.example.common.ResponseBean
     * @author wliduo[i@dolyw.com]
     * @date 2019/8/9 15:32
     */
    @GetMapping("/book")
    public ResponseBean getBookList(@RequestParam(defaultValue = "1") Integer page,
                                    @RequestParam(defaultValue = "10") Integer rows,
                                    String keyword) {
        Request request = new Request("POST", new StringBuilder("/_search").toString());
        // 添加Json返回优化
        request.addParameter("pretty", "true");
        // 拼接查询Json
        IndexRequest indexRequest = new IndexRequest();
        XContentBuilder builder = null;
        Response response = null;
        String responseBody = null;
        try {
            builder = JsonXContent.contentBuilder()
                    .startObject()
                    .startObject("query")
                    .startObject("multi_match")
                    .field("query", keyword)
                    .array("fields", new String[]{"name", "desc"})
                    .endObject()
                    .endObject()
                    .startObject("sort")
                    .startObject("id")
                    .field("order", "desc")
                    .endObject()
                    .endObject()
                    .endObject();
            indexRequest.source(builder);
            // 设置请求体并指定ContentType，如果不指定会乱码
            request.setEntity(new NStringEntity(indexRequest.source().utf8ToString(), ContentType.APPLICATION_JSON));
            // 执行HTTP请求
            response = restClient.performRequest(request);
            responseBody = EntityUtils.toString(response.getEntity());
        } catch (IOException e) {
            return new ResponseBean(HttpStatus.NOT_FOUND.value(), "can not found the book by your id", null);
        }
        return new ResponseBean(HttpStatus.OK.value(), "查询成功", JSON.parseObject(responseBody));
    }

    /**
     * 根据Id获取ES对象
     *
     * @param id
     * @return org.springframework.http.ResponseEntity<java.lang.String>
     * @author wliduo[i@dolyw.com]
     * @date 2019/8/8 17:48
     */
    @GetMapping("/book/{id}")
    public ResponseBean getBookById(@PathVariable("id") String id) {
        Request request = new Request("GET", new StringBuilder("/book/book/")
                .append(id).toString());
        // 添加Json返回优化
        request.addParameter("pretty", "true");
        Response response = null;
        String responseBody = null;
        try {
            // 执行HTTP请求
            response = restClient.performRequest(request);
            responseBody = EntityUtils.toString(response.getEntity());
        } catch (IOException e) {
            return new ResponseBean(HttpStatus.NOT_FOUND.value(), "can not found the book by your id", null);
        }
        return new ResponseBean(HttpStatus.OK.value(), "查询成功", JSON.parseObject(responseBody));
    }

    /**
     * 添加ES对象, Book的ID就是ES中存储的Document的ID，ES的POST和PUT可以看下面这个文章
     * https://blog.csdn.net/z457181562/article/details/93470152
     *
     * @param bookDto
     * @return org.springframework.http.ResponseEntity<java.lang.String>
     * @throws IOException
     * @author wliduo[i@dolyw.com]
     * @date 2019/8/8 17:46
     */
    @PostMapping("/book")
    public ResponseBean add(@RequestBody BookDto bookDto) throws IOException {
        // Endpoint直接指定为Index/Type的形式
        /*Request request = new Request("POST", new StringBuilder("/book/book/").toString());*/
        // 防重复新增数据
        bookDto.setId(System.currentTimeMillis());
        Request request = new Request("PUT", new StringBuilder("/book/book/")
                .append(bookDto.getId()).append("/_create").toString());
        // 设置其他一些参数比如美化Json
        request.addParameter("pretty", "true");
        // 设置请求体并指定ContentType，如果不指定会乱码
        request.setEntity(new NStringEntity(JSONObject.toJSONString(bookDto), ContentType.APPLICATION_JSON));
        // 发送HTTP请求
        Response response = restClient.performRequest(request);
        // 获取响应体
        String responseBody = EntityUtils.toString(response.getEntity());
        return new ResponseBean(HttpStatus.OK.value(), "添加成功", JSON.parseObject(responseBody));
    }

    /**
     * 根据Id更新Book，ES的POST和PUT可以看下面这个文章
     *
     * https://blog.csdn.net/z457181562/article/details/93470152
     * @param bookDto
     * @return org.springframework.http.ResponseEntity<java.lang.String>
     * @throws IOException
     * @author wliduo[i@dolyw.com]
     * @date 2019/8/9 10:04
     */
    @PutMapping("/book")
    public ResponseBean update(@RequestBody BookDto bookDto) throws IOException {
        // 构造HTTP请求
        /*Request request = new Request("POST", new StringBuilder("/book/book/")
                .append(bookDto.getId()).append("/_update").toString());*/
        Request request = new Request("PUT", new StringBuilder("/book/book/")
                .append(bookDto.getId()).toString());
        // 设置其他一些参数比如美化Json
        request.addParameter("pretty", "true");
        /*// 将数据丢进去，这里一定要外包一层'doc'，否则内部不能识别
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("doc", new JSONObject(bookDto));*/
        // 设置请求体并指定ContentType，如果不指定会乱码
        request.setEntity(new NStringEntity(JSONObject.toJSONString(bookDto), ContentType.APPLICATION_JSON));
        // 执行HTTP请求
        Response response = restClient.performRequest(request);
        // 获取返回的内容
        String responseBody = EntityUtils.toString(response.getEntity());
        return new ResponseBean(HttpStatus.OK.value(), "更新成功", JSON.parseObject(responseBody));
    }

    /**
     * 使用脚本更新Name
     *
     * @param id
	 * @param bookDto
     * @return org.springframework.http.ResponseEntity<java.lang.String>
     * @throws IOException
     * @author wliduo[i@dolyw.com]
     * @date 2019/8/9 11:37
     */
    @PutMapping("/book/{id}")
    public ResponseEntity<String> update2(@PathVariable("id") String id, @RequestBody BookDto bookDto) throws IOException {
        // 构造HTTP请求
        Request request = new Request("POST", new StringBuilder("/book/book/")
                .append(id).append("/_update").toString());
        // 设置其他一些参数比如美化Json
        request.addParameter("pretty", "true");
        JSONObject jsonObject = new JSONObject();
        // 创建脚本语言，如果是字符变量，必须加单引号
        StringBuilder op1 = new StringBuilder("ctx._source.name=").append("'" + bookDto.getName() + "'");
        jsonObject.put("script", op1);
        request.setEntity(new NStringEntity(jsonObject.toString(), ContentType.APPLICATION_JSON));
        // 执行HTTP请求
        Response response = restClient.performRequest(request);
        // 获取返回的内容
        String responseBody = EntityUtils.toString(response.getEntity());
        return new ResponseEntity<>(responseBody, HttpStatus.OK);
    }

    /**
     * 根据ID删除
     *
     * @param id
     * @return org.springframework.http.ResponseEntity<java.lang.String>
     * @throws IOException
     * @author wliduo[i@dolyw.com]
     * @date 2019/8/8 17:54
     */
    @DeleteMapping("/book/{id}")
    public ResponseBean deleteById(@PathVariable("id") String id) throws IOException {
        Request request = new Request("DELETE", new StringBuilder("/book/book/")
                .append(id).toString());
        request.addParameter("pretty", "true");
        // 执行HTTP请求
        Response response = restClient.performRequest(request);
        // 获取结果
        String responseBody = EntityUtils.toString(response.getEntity());
        return new ResponseBean(HttpStatus.OK.value(), "删除成功", JSON.parseObject(responseBody));
    }
}
```

复制代码

- HighLevelRestController

```java
@RestController
@RequestMapping("/high")
public class HighLevelRestController {

    /**
     * logger
     */
    private static final Logger logger = LoggerFactory.getLogger(HighLevelRestController.class);

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    /**
     * 获取ES信息
     *
     * @param
     * @return com.example.common.ResponseBean
     * @throws IOException
     * @author wliduo[i@dolyw.com]
     * @date 2019/8/14 17:11
     */
    @GetMapping("/es")
    public ResponseBean getEsInfo() throws IOException {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // SearchRequest
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.source(searchSourceBuilder);
        // 查询ES
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        return new ResponseBean(HttpStatus.OK.value(), "查询成功", searchResponse);
    }

    /**
     * 列表查询
     *
     * @param page
     * @param rows
     * @param keyword
     * @return com.example.common.ResponseBean
     * @throws
     * @author wliduo[i@dolyw.com]
     * @date 2019/8/15 16:01
     */
    @GetMapping("/book")
    public ResponseBean list(@RequestParam(defaultValue = "1") Integer page,
                             @RequestParam(defaultValue = "10") Integer rows,
                             String keyword) throws IOException {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // 分页采用简单的from + size分页，适用数据量小的，了解更多分页方式可自行查阅资料
        searchSourceBuilder.from((page - 1) * rows);
        searchSourceBuilder.size(rows);
        // 查询条件，只有查询关键字不为空才带查询条件
        if (StringUtils.isNoneBlank(keyword)) {
            QueryBuilder queryBuilder = QueryBuilders.multiMatchQuery(keyword, "name", "desc");
            searchSourceBuilder.query(queryBuilder);
        }
        // 排序，根据ID倒叙
        searchSourceBuilder.sort("id", SortOrder.DESC);
        // SearchRequest
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.source(searchSourceBuilder);
        // 查询ES
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        SearchHits hits = searchResponse.getHits();
        // 获取总数
        Long total = hits.getTotalHits().value;
        // 遍历封装列表对象
        List<BookDto> bookDtoList = new ArrayList<>();
        SearchHit[] searchHits = hits.getHits();
        for (SearchHit searchHit : searchHits) {
            bookDtoList.add(JSON.parseObject(searchHit.getSourceAsString(), BookDto.class));
        }
        // 封装Map参数返回
        Map<String, Object> result = new HashMap<String, Object>(16);
        result.put("count", total);
        result.put("data", bookDtoList);
        return new ResponseBean(HttpStatus.OK.value(), "查询成功", result);
    }

    /**
     * 根据ID查询
     *
     * @param id
     * @return com.example.common.ResponseBean
     * @throws IOException
     * @author wliduo[i@dolyw.com]
     * @date 2019/8/15 14:10
     */
    @GetMapping("/book/{id}")
    public ResponseBean getById(@PathVariable("id") String id) throws IOException {
        // GetRequest
        GetRequest getRequest = new GetRequest(Constant.INDEX, id);
        // 查询ES
        GetResponse getResponse = restHighLevelClient.get(getRequest, RequestOptions.DEFAULT);
        BookDto bookDto = JSON.parseObject(getResponse.getSourceAsString(), BookDto.class);
        return new ResponseBean(HttpStatus.OK.value(), "查询成功", bookDto);
    }

    /**
     * 添加文档
     *
     * @param bookDto
     * @return com.example.common.ResponseBean
     * @throws
     * @author wliduo[i@dolyw.com]
     * @date 2019/8/15 16:01
     */
    @PostMapping("/book")
    public ResponseBean add(@RequestBody BookDto bookDto) throws IOException {
        // IndexRequest
        IndexRequest indexRequest = new IndexRequest(Constant.INDEX);
        Long id = System.currentTimeMillis();
        bookDto.setId(id);
        String source = JSON.toJSONString(bookDto);
        indexRequest.id(id.toString()).source(source, XContentType.JSON);
        // 操作ES
        IndexResponse indexResponse = restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
        return new ResponseBean(HttpStatus.OK.value(), "添加成功", indexResponse);
    }

    /**
     * 修改文档
     *
     * @param bookDto
     * @return com.example.common.ResponseBean
     * @throws
     * @author wliduo[i@dolyw.com]
     * @date 2019/8/15 16:02
     */
    @PutMapping("/book")
    public ResponseBean update(@RequestBody BookDto bookDto) throws IOException {
        // UpdateRequest
        UpdateRequest updateRequest = new UpdateRequest(Constant.INDEX, bookDto.getId().toString());
        updateRequest.doc(JSON.toJSONString(bookDto), XContentType.JSON);
        // 操作ES
        UpdateResponse updateResponse = restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);
        return new ResponseBean(HttpStatus.OK.value(), "修改成功", updateResponse);
    }

    /**
     * 删除文档
     *
     * @param id
     * @return com.example.common.ResponseBean
     * @throws
     * @author wliduo[i@dolyw.com]
     * @date 2019/8/15 16:02
     */
    @DeleteMapping("/book/{id}")
    public ResponseBean deleteById(@PathVariable("id") String id) throws IOException {
        // DeleteRequest
        DeleteRequest deleteRequest = new DeleteRequest(Constant.INDEX, id);
        // 操作ES
        DeleteResponse deleteResponse = restHighLevelClient.delete(deleteRequest, RequestOptions.DEFAULT);
        return new ResponseBean(HttpStatus.OK.value(), "删除成功", deleteResponse);
    }

}
```

复制代码

**LowLevelRestController**和**HighLevelRestController**一对比就能看出来了，**HighLevelRestController**使用起来更舒服

接口都实现了，实际请查看代码，最后用`Vue` + `ElementUI`写了一个前端界面

### [#](https://note.dolyw.com/elasticsearch/03-SpringBootES.html#前端界面)前端界面

- 界面实现网页common.html

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">

<head th:fragment="headVue(title)">
    <meta charset="utf-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title th:text="${title}">加载中</title>
    <link rel="shortcut icon" th:href="@{/favicon.ico}" type="image/x-icon"/>
    <link rel="stylesheet" th:href="@{element-ui/index.css}">
    <style>
        /* elementUI的确认弹出框时页面右侧缩小5px的解决方法 */
        body {
            padding-right:0 !important;
        }
        /* elementUI的Table表头错位的解决方法 */
        body .el-table th.gutter {
            display: table-cell!important;
        }
        body .el-table colgroup.gutter {
            display: table-cell!important;
        }
        label {
            font-weight: 700;
        }
    </style>
    <!-- 引入Vue，Element UI，Axios，Moment -->
    <script th:src="@{js/vue.min.js}"></script>
    <script th:src="@{element-ui/index.js}"></script>
    <script th:src="@{js/axios.min.js}"></script>
    <script th:src="@{js/moment.min.js}"></script>
</head>

</html>
```

复制代码

- 界面实现网页index.html

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">

<head th:include="/common/common :: headVue('随心')"></head>

<style>

	.clearfix:before,
	.clearfix:after {
		display: table;
		content: "";
	}
	.clearfix:after {
		clear: both
	}

	/* 谷歌浏览器滚动条美化 */
	::-webkit-scrollbar {
		width: 15px;
		height: 15px;
	}

	::-webkit-scrollbar-track,
	::-webkit-scrollbar-thumb {
		border-radius: 999px;
		border: 5px solid transparent;
	}

	::-webkit-scrollbar-track {
		box-shadow: 1px 1px 5px rgba(143, 143, 143, 0.2) inset;
	}

	::-webkit-scrollbar-thumb {
		min-height: 20px;
		background-clip: content-box;
		box-shadow: 0 0 0 5px rgba(143, 143, 143, 0.466) inset;
	}

	::-webkit-scrollbar-corner {
		background: transparent;
	}

</style>

<body>

<div id="app">

	<el-card>

		<div slot="header" class="clearfix">
			<span>Elasticsearch的CURD</span>
		</div>

		<!-- 查询表单 -->
		<el-form :inline="true" :model="searchForm" label-width="90px" size="small">

			<el-form-item>
				<el-input placeholder="请输入查询关键字" @keyup.enter.native="list(searchForm)" v-model="searchForm.keyword" clearable></el-input>
				<el-input v-show="false" placeholder="请输入查询关键字" @keyup.enter.native="list(searchForm)" v-model="searchForm.keyword" clearable></el-input>
			</el-form-item>

			<el-form-item>
				<el-button icon="el-icon-search" plain @click="list(searchForm)">查询</el-button>
			</el-form-item>

			<el-form-item>
				<el-button type="info" plain icon="el-icon-plus" @click="preAdd" :loading="genLoading">添加</el-button>
			</el-form-item>

		</el-form>

		<!-- 数据表格 -->
		<el-table v-loading="tableLoading" :data="tableData" @selection-change="handleSelectionChange" border>
			<!--<el-table-column type="selection" align="center" width="55"></el-table-column>-->
			<el-table-column type="index" align="center" min-width="60"></el-table-column>
			<el-table-column prop="id" label="ID" align="center" min-width="100" show-overflow-tooltip></el-table-column>
			<el-table-column prop="name" label="名称" align="center" min-width="100" show-overflow-tooltip></el-table-column>
			<el-table-column prop="desc" label="描述" align="center" min-width="80" show-overflow-tooltip></el-table-column>
			<!--<el-table-column prop="createTime" label="创建时间" align="center" min-width="100" show-overflow-tooltip>
				<template slot-scope="scope">
					{{ moment(scope.row.createTime).format('YYYY-MM-DD HH:mm:ss') }}
				</template>
			</el-table-column>-->
			<el-table-column label="操作" align="center" fixed="right" min-width="60">
				<template slot-scope="scope">
					<el-button size="mini" icon="el-icon-edit" type="primary" plain @click="preById(scope.row)" :loading="genLoading">修改</el-button>
					<el-button size="mini" icon="el-icon-delete" type="danger" plain @click="delById(scope.row)" :loading="genLoading">删除</el-button>
				</template>
			</el-table-column>
		</el-table>
		<div align="right" style="margin-top: 20px;">
			<el-pagination
				:current-page="searchForm.page"
				:page-sizes="[1, 8, 16, 32, 48]"
				:page-size="searchForm.rows"
				:total="totalCount"
				layout="total, sizes, prev, pager, next, jumper"
				@size-change="handleSizeChange"
				@current-change="handleCurrentChange"
			/>
		</div>
	</el-card>

	<el-dialog :title="title" :visible.sync="dialogVisible" width="40%">
		<el-form :model="bookDto" label-width="90px" style="width: 520px;">
			<el-form-item label="ID">
				<el-input v-model="bookDto.id" disabled="true" autocomplete="off" placeholder="ID自动生成"></el-input>
			</el-form-item>
			<el-form-item label="名称">
				<el-input v-model="bookDto.name" autocomplete="off"></el-input>
			</el-form-item>
			<el-form-item label="描述">
				<el-input v-model="bookDto.desc" autocomplete="off"></el-input>
			</el-form-item>
		</el-form>
		<div slot="footer" class="dialog-footer">
			<el-button :loading="genLoading" @click="dialogVisible = false">取 消</el-button>
			<el-button type="primary" :loading="genLoading" @click="deal">确 定</el-button>
		</div>
	</el-dialog>

</div>

<script type="text/javascript" th:inline="javascript">
    /*<![CDATA[*/
    var myApp = new Vue({
        el: '#app',
        data: {
            // 表格加载条控制
            tableLoading: false,
            // 按钮加载条控制
            genLoading: false,
            // Table数据
            tableData: [],
            // Table数据总条数
            totalCount: 0,
            // Table选择的数据
            multipleSelection: [],
            // 查询条件
            searchForm: {
                // 当前页
                page: 1,
                // 每页条数
                rows: 8,
                // 查询关键字
                keyword: ''
            },
            // 表详细弹出框标题
            title: '添加',
            // 表详细弹出框是否显示
            dialogVisible: false,
            // 操作对象
            bookDto: {
                // ID
                id: 1,
                // 名称
                name: '',
                // 描述
                desc: ''
            }
        },
        // 启动时就执行
        mounted: function() {
            // ES信息查询
            // this.queryES();
            // 列表查询
            this.list(this.searchForm);
        },
        methods: {
            // 查询ES信息
            queryES: function() {
                axios.get('/high/es').then(res => {
                    console.log(res);
                }).catch(err => {
                    console.log(err);
                    this.$message.error('查询失败');
                });
            },
            // 每页条数改变
            handleSizeChange: function(rows) {
                this.searchForm.rows = rows;
                // console.log(this.searchForm.rows);
                // 刷新列表
                this.list(this.searchForm);
            },
            // 当前页数改变
            handleCurrentChange: function(page) {
                this.searchForm.page = page;
                // 刷新列表
                this.list(this.searchForm);
            },
            // 选择数据改变触发事件
            handleSelectionChange(val) {
                this.multipleSelection = val;
            },
            // 列表查询
            list: function(searchForm) {
                // 加载显示
                this.tableLoading = true;
                axios.get('/high/book', {
                    params: {
                        'page': this.searchForm.page,
                        'rows': this.searchForm.rows,
                        'keyword': this.searchForm.keyword
                    }
                }).then(res => {
                    // console.log(res);
                    var data = res.data.data;
                    this.tableData = data.data;
                    this.totalCount = data.count;
                }).catch(err => {
                    console.log(err);
                    this.$message.error('查询失败');
                }).then(() => {
                    this.tableLoading = false;
            	});
            },
            // 添加
            preAdd: function() {
                this.genLoading = true;
                // this.$nextTick Dom渲染完执行
                this.$nextTick(() => {
                    this.title = "添加";
                    this.bookDto = {};
                    this.dialogVisible = true;
                    this.genLoading = false;
                });
            },
            // 预修改
            preById: function(row) {
                this.genLoading = true;
                this.title = "修改";
                this.dialogVisible = true;
                axios.get('/high/book/' + row.id).then(res => {
                    // console.log(res);
                    this.bookDto = res.data.data;
                }).catch(err => {
                    console.log(err);
                    this.$message.error('查询失败');
                }).then(() => {
                    this.genLoading = false;
                });
            },
            // 添加或者修改
            deal: function() {
                this.genLoading = true;
                if (this.bookDto.id) {
                    // ID存在修改
                    axios.put('/high/book', this.bookDto).then(res => {
                        if (res.data.code == 200) {
                            this.$message({
                                message: res.data.msg,
                                type: 'success'
                            });
                            this.dialogVisible = false;
                            // 列表查询必须慢点，ES没有事务性，查询太快，数据还没更新
                            this.tableLoading = true;
                            setTimeout(() => {this.list(this.searchForm);}, 1000);
                        } else {
                            this.$message.error('修改失败');
                        }
                    }).catch(err => {
                        console.log(err);
                        this.$message.error('修改失败');
                    }).then(() => {
                        this.genLoading = false;
                    });
                } else {
                    // ID不存在添加
                    axios.post('/high/book', this.bookDto).then(res => {
                        if (res.data.code == 200) {
                            this.$message({
                                message: res.data.msg,
                                type: 'success'
                            });
                            this.dialogVisible = false;
                            // 列表查询必须慢点，ES没有事务性，查询太快，数据还没更新
                            this.tableLoading = true;
                            setTimeout(() => {this.list(this.searchForm);}, 1000);
                        } else {
                            this.$message.error('添加失败');
                        }
                    }).catch(err => {
                        console.log(err);
                        this.$message.error('添加失败');
                    }).then(() => {
                        this.genLoading = false;
                    });
                }
            },
            // 删除
            delById: function (row) {
                this.genLoading = true;
                this.$confirm('是否确定删除', '提示', {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning'
                }).then(() => {
                    axios.delete('/high/book/' + row.id).then(res => {
                        if (res.data.code == 200) {
                            this.$message({
                                message: res.data.msg, 
                                type: 'success'
                            });
                            // 列表查询必须慢点，ES没有事务性，查询太快，数据还没更新
                            this.tableLoading = true;
                            setTimeout(() => {this.list(this.searchForm);}, 1000);
                        } else {
                            this.$message.error('删除失败');
                        }
                    }).catch(err => {
                        console.log(err);
                        this.$message.error('删除失败');
                    }).then(() => {
                        this.genLoading = false;
                    });
                }).catch(() => {
                    this.genLoading = false;
                });
            }
        }
    });
    /*]]>*/
</script>

</body>

</html>
```

复制代码

## [#](https://note.dolyw.com/elasticsearch/03-SpringBootES.html#安装教程)安装教程

运行项目src\main\java\com\example\Application.java即可，访问[http://localhost:8080 (opens new window)](http://localhost:8080/)即可

## [#](https://note.dolyw.com/elasticsearch/03-SpringBootES.html#搭建参考)搭建参考

1. 感谢MaxZing的在SpringBoot整合Elasticsearch的Java Rest Client:[https://www.jianshu.com/p/0b4f5e41405e(opens new window)](https://www.jianshu.com/p/0b4f5e41405e)
2. 感谢jacksonary的SpringBoot整合ES的三种方式（API、REST Client、Data-ES）:[https://blog.csdn.net/jacksonary/article/details/82729556(opens new window)](https://blog.csdn.net/jacksonary/article/details/82729556)
3. 感谢青青天空树的springboot整合elasticsearch7.2(基于官方high level client):[https://cloud.tencent.com/developer/article/1478267(opens new window)](https://cloud.tencent.com/developer/article/1478267)
4. 感谢zhyingke的elasticsearch7 基本语法(基于官方high level client):[https://blog.csdn.net/z457181562/article/details/93470152(opens new window)](https://blog.csdn.net/z457181562/article/details/93470152)
5. 感谢wangzhen3798的ElasticSearch中如何进行排序:[https://blog.csdn.net/wangzhen3798/article/details/83582474(opens new window)](https://blog.csdn.net/wangzhen3798/article/details/83582474)
6. 感谢WeirdLang的ElasticSearch RestHighLevelClient 通用操作:https://www.cnblogs.com/WeidLang/p/10245659.html

# Docker下Elasticsearch的使用

SpringBoot整合ES的方式(TransportClient、Data-ES、Elasticsearch SQL、REST Client)

## [#](https://note.dolyw.com/elasticsearch/04-DockerES.html#代码地址)代码地址

- Github：[https://github.com/dolyw/ProjectStudy/tree/master/Elasticsearch/02-SpringBoot-ES-Docker(opens new window)](https://github.com/dolyw/ProjectStudy/tree/master/Elasticsearch/02-SpringBoot-ES-Docker)
- Gitee(码云)：[https://gitee.com/dolyw/ProjectStudy/tree/master/Elasticsearch/02-SpringBoot-ES-Docker(opens new window)](https://gitee.com/dolyw/ProjectStudy/tree/master/Elasticsearch/02-SpringBoot-ES-Docker)

## [#](https://note.dolyw.com/elasticsearch/04-DockerES.html#项目介绍)项目介绍

详细的过程查看:

- SpringBoot整合Elasticsearch的方式(TransportClient、Data-ES、Elasticsearch SQL、REST Client): [SpringBoot整合Elasticsearch](https://note.dolyw.com/elasticsearch/03-SpringBootES.html)
- Docker环境下搭建Elasticsearch，Elasticsearch集群，Elasticsearch-Head以及IK分词插件和拼音分词插件: [https://note.dolyw.com/docker/03-Elasticsearch.html(opens new window)](https://note.dolyw.com/docker/03-Elasticsearch.html)

这个项目只是测试Docker版本的Elasticsearch是否安装无误，和之前本地版区别是Docker的ES版本升级到了7.3，字段添加了content，describe，之前的desc是关键字就改成了describe

# MySql数据同步Elasticsearch

MySql数据同步Elasticsearch的方式

1. 直接通过ES的API将数据写入到ES集群中
2. 监听MySQL的Binlog，分析Binlog将数据同步到ES集群中

> 考虑到订单系统ES服务的业务特殊性，对于订单数据的实时性较高，显然监听Binlog的方式相当于异步同步，有可能会产生较大的延时性，且方案2实质上跟方案1类似，但又引入了新的系统，维护成本也增高，所以订单中心ES采用了直接通过ES的API写入订单数据的方式，该方式简洁灵活，能够很好的满足订单中心数据同步到ES的需求

> 由于ES订单数据的同步采用的是在业务中写入的方式，当新建或更新文档发生异常时，如果重试势必会影响业务正常操作的响应时间，所以每次业务操作只更新一次ES，如果发生错误或者异常，在数据库中插入一条补救任务，有Worker任务会实时地扫这些数据，以数据库订单数据为基准来再次更新ES数据。通过此种补偿机制，来保证ES数据与数据库订单数据的最终一致性

## [#](https://note.dolyw.com/elasticsearch/05-MySqlES.html#直接通过es的api将数据写入到es集群中)直接通过ES的API将数据写入到ES集群中

待补充

## [#](https://note.dolyw.com/elasticsearch/05-MySqlES.html#监听mysql的binlog-分析binlog将数据同步到es集群中)监听MySQL的Binlog，分析Binlog将数据同步到ES集群中

待补充