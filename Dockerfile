FROM centos

### Guacamole Server ###
# 更多详细安装信息可参考 Guacamole 官方 <https://guacamole.apache.org/doc/gug/installing-guacamole.html>
# 拷贝定制过的 guacamole-server 并解压
ADD f2etest-guacamole/guacamole-server-0.9.3.tar.gz /home/guacdshare

WORKDIR /home/guacdshare

# 安装需要使用到的依赖包
RUN yum install -y wget gcc initscripts sudo lsof automake autoconf libtool make \
  # 安装 `guacd` 依赖
  cairo-devel libpng-devel uuid-devel freerdp* libvncserver-devel openssl-devel \
  # 编译前创建 `freerdp` 的软连接
  && ln -s /usr/local/lib/freerdp/guacsnd.so /usr/lib64/freerdp/ \
  && ln -s /usr/local/lib/freerdp/guacdr.so /usr/lib64/freerdp/

WORKDIR /home/guacdshare/guacamole-server-0.9.3

# 运行配置脚本并将 `guacd` 添加到 `/etc/init.d` 目录
RUN ./configure --with-init-dir=/etc/init.d \
  # 编译
  && make \
  # 安装构建的组件
  && make install \
  # 更新已安装库的缓存
  && ldconfig \
  # 设置运行时自动启动
  && chkconfig --add guacd \
  && chkconfig guacd on 


### Guacamole Client ###
WORKDIR /root

## JAVA ##
# 下载 JDK 1.8
RUN wget --no-cookies --no-check-certificate --header "Cookie: gpw_e24=http%3A%2F%2Fwww.oracle.com%2F; oraclelicense=accept-securebackup-cookie" "http://download.oracle.com/otn-pub/java/jdk/8u141-b15/336fa29ff2bb4ef291e347e091f7f4a7/jdk-8u141-linux-x64.tar.gz" \
  && mkdir /usr/java

WORKDIR /usr/java

# 拷贝并解压 JDK
RUN cp /root/jdk-8u141-linux-x64.tar.gz ./ \
  && tar xzf jdk-8u141-linux-x64.tar.gz

# 设置 JAVA 运行环境
ENV JAVA_HOME /usr/java/jdk1.8.0_141
ENV PATH $JAVA_HOME/bin;$PATH
ENV CLASSPATH .:$JAVA_HOME/lib

## TOMCAT ##
WORKDIR /usr/local

# 下载并解压 Tomcat
RUN wget http://mirrors.hust.edu.cn/apache/tomcat/tomcat-8/v8.5.38/bin/apache-tomcat-8.5.38.tar.gz \
  && tar -xzvf apache-tomcat-8.5.38.tar.gz \
  && mv apache-tomcat-8.5.38 tomcat8

# 拷贝定制过的 WAR 包
COPY f2etest-guacamole/guacamole-0.9.3.war /usr/local/tomcat8/webapps/guacamole.war

# 拷贝 guacamole 配置文件和免登配置文件
COPY f2etest-docker/guacamole.properties /etc/guacamole/
COPY f2etest-docker/noauth-config.xml /etc/guacamole/

# 拷贝 Tomcat 服务脚本到 `/etc/init.d` 目录
COPY f2etest-docker/tomcat8 /etc/init.d/

WORKDIR /etc/init.d

# 增加 Tomcat 服务让其运行时自动启动
RUN chmod 755 tomcat8 \
  && chkconfig --add tomcat8 \
  && chkconfig --level 234 tomcat8 on \
  && mkdir /root/.guacamole \
  && ln -s /etc/guacamole/guacamole.properties /root/.guacamole


### F2etst Web ###
## Node Env ##
WORKDIR /usr/local

# 下载 node v10 并解压
RUN wget https://nodejs.org/dist/v10.15.1/node-v10.15.1-linux-x64.tar.xz \
  && xz -d node-v10.15.1-linux-x64.tar.xz \
  && tar -xf node-v10.15.1-linux-x64.tar \
  && mv node-v10.15.1-linux-x64 node \
  # 给 `node`, `npm` 命令创建软连接
  && ln -s /usr/local/node/bin/node /usr/bin/node \
  && ln -s /usr/local/node/bin/npm /usr/bin/npm \
  # 安装 pm2 [node 应用进程管理器]
  && npm i -g pm2 \
  # 给 `pm2` 命令创建软连接
  && ln -s /usr/local/node/bin/pm2 /usr/bin/pm2

## MySQL ##
# 下载安装 MySQL
RUN wget http://dev.mysql.com/get/mysql-community-release-el7-5.noarch.rpm \
  && yum localinstall -y mysql-community-release-el7-5.noarch.rpm \
  && yum install -y mysql-community-server

## Nginx ##
# 拷贝 nginx 源配置
COPY f2etest-docker/nginx.repo /etc/yum.repos.d/

# yum 安装 nginx
RUN yum-config-manager --enable nginx-mainline \
  && yum install -y nginx

COPY f2etest-docker/nginx.conf /etc/nginx/

# 拷贝 Web 应用程序
COPY f2etest-web /home/f2etest-web
WORKDIR /home/f2etest-web/
# 安装 node 依赖包
RUN npm i


# 拷贝初始化脚本（初始化数据库表、`pm2` 启动 Web、启动 Nginx）
COPY f2etest-docker/setup.sh ./
RUN chmod 777 setup.sh

EXPOSE 80

# 指定容器启动程序（以防出现权限不足情况）
ENTRYPOINT ["/usr/sbin/init"]
