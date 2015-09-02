guacamole
======================

guacamole这一款支持HTML5的开源远程桌面解决方案，包括两部分：

1. guacamole-server: 是个网关，支持rdp,vnc等协议
2. guacamole-client：是一个基于JAVA开发的客户端

f2etest-guacamole版本基于官方guacamole-client进行如下变更和加强：

1. 去掉了登录限制，允许未登录也能调用
2. 增加了扩展参数，允许传递username, password, program等参数，以便对接F2etest
3. 打开了底部输入框，并且完美优化了输入工作模式
4. 支持剪切板双向直通，完美打通本地和服务器之间的剪切板交换
5. 和F2etest无缝对接，可以显示当前应用的图标和标题

安装请参考：[Install.md](./Install.md)