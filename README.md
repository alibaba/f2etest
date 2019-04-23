F2etest
===================

![imgs/logo.png](https://raw.githubusercontent.com/alibaba/f2etest/master/imgs/logo.png)

F2etest是一个面向前端、测试、产品等岗位的多浏览器兼容性测试整体解决方案。

注：F2e test = Front to End Test = 前端测试

- 最新文档：[语雀文档](https://www.yuque.com/artist/f2etest)、[Github Page](https://alibaba.github.io/f2etest/build/#/artist/f2etest/intro)

浏览器云
=================================

在之前，多浏览器兼容性人肉测试，我们一般有三种解决方案：

1. 本机安装大量的虚拟机，一个浏览器一个虚拟机，优点：真实，缺点：消耗硬盘资源，消耗CPU资源，打开慢，无法同时打开多个虚拟机
2. 使用IeTester等模拟软件，优点：体积小，资源消耗小，缺点：不真实，很多特性不能代表真实浏览器
3. 公用机器提供多种浏览器，优点：不需要本地安装，不消耗本机资源，缺点：资源利用率低，整体资源消耗非常恐怖

现在，有了F2etest，一台普通的4核CPU的服务器，我们就可以提供给20人以上同时使用。

在这之前我们需要20台机器，相比之下，至少10倍的硬件利用率提升。

再加上我们平时做多浏览器测试时，并不是满负荷工作。经常是测试一会，开发一会，或者是忙点别的事，因此理论并发能力至少可以再乘2，就是说一台4核服务器，理论上跑40人同时在线，非常轻松。

但是在此之前，如果我们是单人虚拟机模式的话，当你没在测试时，CPU、内存、硬盘，全部都是处在浪费状态。

相比之前的方案，我们有以下优势：

1. 10倍以上硬件利用率，降低企业运营成本
2. 非常棒的用户体验，极大的提高测试效率
3. 真实浏览器环境，还原真实测试场景

在这个解决方案中，我们使用了以下技术：

1. Guacamole: 开源的HTML5远程解决方案
2. Windows Server: Server版Windows，最大化复用机器资源
3. hostsShare: 跨浏览器，跨服务器的hosts共享

WebDriver云
=================================

WebDriver云利用Windows Server多用户的特性，将执行机的使用效率提升10倍以上。

之前1台执行机只能跑1个任务，现在同样的硬件配置，我们可以跑10个任务。

下面的截图看上去有130个执行机节点，传统情况下我们需要130台服务器。

但是我们实际上才5台！

相比较官方的Selenium Grid，我们有以下优势：

1. 10倍以上硬件利用率：传统WebDriver 1台执行机仅能跑1个Job，而我们一台机器可以高并发跑N个Job，这个N取决于机器配置，理论上硬件利用率相当于传统节点的10倍以上
2. 支持独立hosts绑定：每次申请节点时，可以指定不同的hosts绑定，保证同一台机器上不同节点的Job不会相互干扰
3. 所有节点支持远程在线调试：利用Guacamole的在线远程功能，当自动化出现问题时，我们能非常便利的对自动化进展进行即时监控和调试

UI Recorder
=================================

UI Recorder是一款几乎零成本的整体自动化测试解决方案，一次自测等于多次测试，测一个浏览器等于测多个浏览器！

我们的目标：让自测不要重复测！让自动化变成零成本！让自动化持续保障您的业务！

以下是本产品的特点：

1. 自测 = 自动化测试：对于开发人员来讲，自测是开发流程中缺一不可的过程，我们要实现的目标就是自测过程中即可同步的录制出自动化脚本，实现真正的零成本自动化
2. 无干扰录制：所作操作均无需交互干扰，鼠标、键盘、alert弹框、文件上传，完全按照正常自测流程操作即可(以下操作除外：悬停事件、断言、变量)
3. 本地生成脚本：录制的脚本存储在用户本机，用户可以自行在录制的基础上进行修改定制，更自由更开放
4. 丰富的断言：支持以下断言类型，val、text、displayed、enabled、selected、attr、css、url、title、cookie、localStorage、sessionStorage
5. 支持数据Mock：我们支持Faker变量功能，支持强大的数据Mock
6. 支持公共用例: 用例之间允许相互引用，可以将某些公用的操作步骤录制为公用用例，以进一步提升录制效率
7. 支持执行截图：每次执行后，允许生成截图日志，以方便出问题时排查诊断

要了解详细细节，可以查看我们的视频教程：[http://v.youku.com/v_show/id_XMTY4NTk5NjI4MA==.html](http://v.youku.com/v_show/id_XMTY4NTk5NjI4MA==.html)

UI Recorder目前已经对F2etest进行了解偶，在非F2etest环境下也可以使用，只要是标准WebDriver协议就可以支持。

但是，我们更建议UI Recorder配合F2etest的WebDriver云来使用，更低的执行机成本，更强大的调试功能。

具体如何配合使用，F2etest部署完成，打开F2etest-Web后，可以看到详细使用说明。

仓库地址： [https://github.com/alibaba/uirecorder](https://github.com/alibaba/uirecorder)

Karma + F2etest
=================================

Karma是由Google开源的JS单元测试执行过程管理工具，这是一款异常强大并且高可扩展性的测试工具，拥有一个非常活跃且完善的生态圈。

Karma支持运行在任何WebDriver协议的浏览器上，因此结合F2etest提供的WebDriver云，就可以批量快速的运行在大量的浏览器上，从而满足单元测试的多浏览器测试需求。

具体如何配置，F2etest-Web中可以看到详细的教程，并且我们提供了完整的配置文件下载。

成功案例
=================================

[![imgs/alibaba.png](https://raw.githubusercontent.com/alibaba/f2etest/master/imgs/alibaba.png)](http://www.alibabagroup.com/)

如果您使用了我们的解决方案，请联系我们添加在此处。

产品截图
=================================

浏览器云
-------------------

![imgs/screenshot1.jpg](https://raw.githubusercontent.com/alibaba/f2etest/master/imgs/screenshot1.jpg)

![imgs/screenshot2.jpg](https://raw.githubusercontent.com/alibaba/f2etest/master/imgs/screenshot2.jpg)

Chrome插件:

![imgs/chrome.png](https://raw.githubusercontent.com/alibaba/f2etest/master/imgs/chrome.png)

Windows:

![imgs/windows.jpg](https://raw.githubusercontent.com/alibaba/f2etest/master/imgs/windows.jpg)

Mac:

![imgs/mac.jpg](https://raw.githubusercontent.com/alibaba/f2etest/master/imgs/mac.jpg)

WebDriver云
--------------------

![imgs/webdriver1.jpg](https://raw.githubusercontent.com/alibaba/f2etest/master/imgs/webdriver1.jpg)

Karma + F2etest
--------------------

![imgs/jsunit.png](https://raw.githubusercontent.com/alibaba/f2etest/master/imgs/jsunit.png)

UI Recorder
---------------------

UI Recorder请查看视频教程：[http://v.youku.com/v_show/id_XMTY4NTk5NjI4MA==.html](http://v.youku.com/v_show/id_XMTY4NTk5NjI4MA==.html)

安全风险警示(非常重要)
==================

由于本系统基于Windows Server体系搭建，因此系统的安全性完全取决于部署人的安全部署能力。

如果您希望部署本系统，请确保以下几点：

1. 严禁将本系统部署在公网环境，仅可部署在内网环境中使用，作为内部测试用途
2. 请将Windows Server服务端升级到最新版本及补丁，以保证没有出现安全漏洞
3. 请将User用户之间做到完全隔离，仅提供User用户文件的访问权限，别的任何权限请勿多余授权
4. 请将f2etest-client仅设置为管理员拥有权限，防止API接口被恶意访问

安装教程
===================

安装请前往安装教程页面：[INSTALL.md](https://github.com/alibaba/f2etest/blob/master/INSTALL.md)

第3方安装及使用教程：

1. [http://shaofan.org/f2etest/](http://shaofan.org/f2etest/)
2. [http://shaofan.org/ui-recorder/](http://shaofan.org/ui-recorder/)

感谢
===================

* Guacamole: [http://guac-dev.org/](http://guac-dev.org/)
* Nodejs: [http://nodejs.org/](http://nodejs.org/)
* async: [https://github.com/caolan/async](https://github.com/caolan/async)
* ejs: [https://github.com/mde/ejs](https://github.com/mde/ejs)
* express: [https://github.com/expressjs/express](https://github.com/expressjs/express)
* jwebdriver: [https://github.com/yaniswang/jWebDriver](https://github.com/yaniswang/jWebDriver)
* mysql: [https://github.com/felixge/node-mysql](https://github.com/felixge/node-mysql)
* request: [https://github.com/request/request](https://github.com/request/request)<!-- GITCONTRIBUTOR_START -->

## Contributors

|[<img src="https://avatars1.githubusercontent.com/u/970390?v=4" width="100px;"/><br/><sub><b>yaniswang</b></sub>](https://github.com/yaniswang)<br/>|[<img src="https://avatars2.githubusercontent.com/u/18617837?v=4" width="100px;"/><br/><sub><b>Stngle</b></sub>](https://github.com/Stngle)<br/>|
| :---: | :---: |


This project follows the git-contributor [spec](https://github.com/xudafeng/git-contributor), auto updated at `Thu Mar 21 2019 12:10:44 GMT+0800`.

<!-- GITCONTRIBUTOR_END -->