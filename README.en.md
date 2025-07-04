<p align="center">
  <a href="https://github.com/lxwise/elastic-desktop-manager/">
        <img src="./doc/elastic-desktop-manager.png" alt="elastic-desktop-manager">
  </a>
</p>

<img align="right" width="110" src="./doc/es-logo.png">

> 🚀🚀🚀 Faster, better performing, more stable ES desktop (GUI) management client, compatible with Windows, Mac, Linux, outstanding performance, easy to load large amounts of data

<p align="center">
   <a target="_blank" href="https://github.com/lxwise/elastic-desktop-manager">
      <img src="https://img.shields.io/hexpm/l/plug.svg"/>
      <img src="https://img.shields.io/badge/release-v1.0.1-green"/>
       <img src="https://img.shields.io/badge/elasticsearch-7%2B-green"/>
      <img src="https://img.shields.io/badge/java-21%2B-%23F27E3F"/>
       <img src="https://img.shields.io/badge/javafx-23%2B-%23F27E3F"/>
   </a>
</p>



[**简体中文**](https://gitee.com/lxwise/elastic-desktop-manager/blob/master/README.md)

[**English**](https://github.com/lxwise/elastic-desktop-manager/blob/master/README.en.md)

## :wrench:elastic-desktop-manager

`elastic-desktop-manager` is a cross-platform `Elasticsearch` query and management tool developed based on JavaFX, with a built-in high-level `Java` high-level client (`High Level REST Client`), aiming to provide a one-stop graphical `Elasticsearch` operation experience for developers, operation and maintenance personnel, data analysts and other users.

🚀The tool supports a rich set of `Elasticsearch` query capabilities, including:

🖥  **Graphical query builder**: No need to write `JSON` or `DSL`, build complex query conditions (Boolean conditions, ranges, fuzzy, nested, etc.) in a visual way, lowering the threshold for use.

🔍 **Aggregation query support**: Built-in aggregation query module supports multi-dimensional and multi-level aggregation, which facilitates data grouping and statistical analysis.

:hammer:**REST API request support**: Provides direct `REST` request execution function, which can facilitate debugging of any `API` interface, replacing tools such as `Postman` and `curl`. 

:triangular_flag_on_post:**Elasticsearch SQL query support**: Supports `SQL` query syntax, making it easier for non-technical personnel to quickly understand and operate ES data.  

📊  **Index, status and performance monitoring**: Real-time view of cluster health status, node information, shard allocation, index list, document quantity, storage usage and other indicators to assist daily operation and maintenance.

💾 **Export and formatting function**: Query results support beautified display and export in `CSV` and `JSON`, which facilitates result storage and reuse.

💦  **Strong version compatibility**: Adapt to `Elasticsearch 7.x` and `8.x` series, with excellent compatibility.

🔁 **Automatic update detection**: Automatically check for new versions at each startup (configurable)



## 🔗Project address

**Gitee address：** [https://gitee.com/lxwise/elastic-desktop-manager](https://gitee.com/lxwise/elastic-desktop-manager)

**Github address：** [https://github.com/lxwise/elastic-desktop-manager](https://github.com/lxwise/elastic-desktop-manager)



## :star::star::star:Star

Although I know that most people like to get something for free like the author, they all leave after reading and downloading the source code. But I still want to ask all the friends who like this project: **Star**, **Star**, **Star**. Only with your **Star** can more people see this project, and more like-minded friends will join in improving this project. Please move your cute little hands and give this project a **Star**. **Also welcome everyone to submit PR and improve the project together**.



## :dash::dash::dash:Download and install

You can download the `exe`, `msi`, `zip`, `rpm`, `deb`, `pkg`, `dmg` installation packages from [github](https://github.com/lxwise/elastic-desktop-manager/releases) or [gitee](https://gitee.com/lxwise/elastic-desktop-manager/releases).

### 🛠 Download

**Github:** https://github.com/lxwise/elastic-desktop-manager/releases

### 👇Alternative downloads

**BaiduNetdisk:** https://pan.baidu.com/s/1c3KZEfwUJ3fPb08cPJ0Jmg?pwd=bvj8



### 🛠 Installation

### ✅ Windows

Download `elastic-desktop-manager.exe`, `elastic-desktop-manager.msi` or `compressed package` version

Double-click to install or run after unzipping

### 🍎 macOS 

Download `elastic-desktop-manager-1.0.1.dmg`, or start it through the `jar` package:

### 🐧 Linux

Download `elastic-desktop-manager-1.0.1-1.x86_64.rpm`, or start it through the `jar` package:

### 💦Other

Download `elastic-desktop-manager.jar` and run `java -jar elastic-desktop-manager.jar`




## :airplane::airplane::airplane:Take off!

| Function         | Light Color                                                  | Dark                                                         | Dracula                                                      | NordDark                                                     |
| ---------------- | ------------------------------------------------------------ | ------------------------------------------------------------ | ------------------------------------------------------------ | ------------------------------------------------------------ |
| Start            | ![启动](./doc/%E5%90%AF%E5%8A%A81.png)                      | -                                                            | -                                                            | -                                                            |
| Home             | ![首页1](./doc/%E9%A6%96%E9%A1%B51.png)                     | ![首页2](./doc/%E9%A6%96%E9%A1%B52.png)                     | ![首页3](./doc/%E9%A6%96%E9%A1%B53.png)                     | ![首页4](./doc/%E9%A6%96%E9%A1%B54.png)                     |
| Node             | ![节点1](./doc/节点1.png)                                   | ![节点2](./doc/%E8%8A%82%E7%82%B92.png)                     | ![节点3](./doc/%E8%8A%82%E7%82%B93.png)                     | ![节点4](./doc/%E8%8A%82%E7%82%B94.png)                     |
| Sharding         | ![分片1](./doc/分片1.png)                                   | ![分片2](./doc/%E5%88%86%E7%89%872.png)                     | ![分片3](./doc/%E5%88%86%E7%89%873.png)                     | ![分片4](./doc/%E5%88%86%E7%89%874.png)                     |
| index            | ![索引1](./doc/索引1.png)                                   | ![索引2](./doc/%E7%B4%A2%E5%BC%952.png)                     | ![索引3](./doc/%E7%B4%A2%E5%BC%953.png)                     | ![索引4](./doc/%E7%B4%A2%E5%BC%954.png)                     |
| Rest             | ![rest1](./doc/rest1.png)                                   | ![rest2](./doc/rest2.png)                                   | ![rest3](./doc/rest3.png)                                   | ![rest4](./doc/rest4.png)                                   |
| Sql              | ![sql1](./doc/sql1.png)                                     | ![sql2](./doc/sql2.png)                                     | ![sql3](./doc/sql3.png)                                     | ![sql4](./doc/sql4.png)                                     |
| Query            | ![查询1](./doc/查询1.png)                                   | ![查询2](./doc/%E6%9F%A5%E8%AF%A22.png)                     | ![查询3](./doc/%E6%9F%A5%E8%AF%A23.png)                     | ![查询4](./doc/%E6%9F%A5%E8%AF%A24.png)                     |
| Query conditions | ![查询条件1](./doc/%E6%9F%A5%E8%AF%A2%E6%9D%A1%E4%BB%B61.png) | ![查询条件2](./doc/%E6%9F%A5%E8%AF%A2%E6%9D%A1%E4%BB%B62.png) | ![查询条件3](./doc/%E6%9F%A5%E8%AF%A2%E6%9D%A1%E4%BB%B63.png) | ![查询条件4](./doc/%E6%9F%A5%E8%AF%A2%E6%9D%A1%E4%BB%B64.png) |
| Query Update     | ![查询更新1](./doc/%E6%9F%A5%E8%AF%A2%E6%9B%B4%E6%96%B01.png) | ![查询更新2](./doc/%E6%9F%A5%E8%AF%A2%E6%9B%B4%E6%96%B02.png) | ![查询更新3](./doc/%E6%9F%A5%E8%AF%A2%E6%9B%B4%E6%96%B03.png) | ![查询更新4](./doc/%E6%9F%A5%E8%AF%A2%E6%9B%B4%E6%96%B04.png) |
| Connect          | ![连接1](./doc/%E8%BF%9E%E6%8E%A51.png)                     | ![连接2](./doc/%E8%BF%9E%E6%8E%A52.png)                     | ![连接3](./doc/%E8%BF%9E%E6%8E%A53.png)                     | ![连接4](./doc/%E8%BF%9E%E6%8E%A54.png)                     |
| set up           | ![设置1](./doc/%E8%AE%BE%E7%BD%AE1.png)                     | ![设置2](./doc/%E8%AE%BE%E7%BD%AE2.png)                     | ![设置3](./doc/%E8%AE%BE%E7%BD%AE3.png)                     | ![设置4](./doc/%E8%AE%BE%E7%BD%AE4.png)                     |




## :gift:Sponsor

- You can give me a Star on [github](https://github.com/lxwise/elastic-desktop-manager) or [gitee](https://gitee.com/lxwise/elastic-desktop-manager) ⭐⭐ ⭐

- WeChat/Alipay sponsorship code [If you find it useful, please give the author some White Rabbit 🐇🐇🐇 milk candy:star2::star2::star2:]

    

    <p align="center">
      <img src="./doc/weChatPay.png" width="350px" style="margin-right: 20px;"/>
      <img src="./doc/alipay_shot.png" width="250px"/>
    </p>

    


## 🏆Milestone

- 2025-07-05: The project is officially open source
- 2025-05-17: Provide running log display query
- 2025-04-22: Optimize interaction logic and provide internal version
- 2025-04-13: Startup page, page data cache
- 2025-04-06: Internationalization, automatic update support
- 2025-03-29: Extract common components, es operation extract asynchronous tasks
- 2025-03-18: Table paging support, field copy, field search
- 2025-03-09: Gift, about me, theme switching
- 2025-02-27: Graphical query, update, delete
- 2025-02-16: Graphical query builder and related layouts
- 2025-01-26: Sql query, batch query, result export
- 2025-01-06: REST page, REST command history
- 2025-12-15: Index information, text viewing and search components
- 2024-12-06: Node information
- 2024-11-29: Sharding information
- 2024-11-19: Cluster health information, scheduled task components
- 2024-11-05: System settings and themes
- 2024-10-29: Global menu and routing
- 2024-10-23: Home page layout
- 2024-10-17: Submit the first code
- 2024-10-13: Project incubation


##  :triangular_ruler:Dev Build

> Tips: This is a development environment, used to run the complete project. **Ordinary users** can directly download the installation package from the previous page

|     Tool     |         Illustrate         |
| :----------: | :------------------------: |
|     IDEA     | Java Development Tools IDE |
| SceneBuilder |  JavaFx Development Tools  |

| Development Environment | Version |
| :---------------------: | :-----: |
|         OpenJDK         | 21.0.2  |
|         JavaFx          | 23.0.1  |
|      SceneBuilder       | 22.0.0  |
|      elasticsearch      | 7.10.0  |
|         kibana          | 7.10.0  |

## 📌 Precautions

Before running, make sure that `Elasticsearch` is started and can be connected in the target environment.

This project is a pure client tool and will not upload data or collect any user privacy information.



## 🙌 Grateful

**Thanks to all open source contributors**, thanks to the big guys in the group for answering questions, javafx communication group: **`518914410`**, thanks to all developers who use, give feedback and contribute. If you have suggestions or bug feedback during use, you are welcome to submit an issue or participate in the contribution.

