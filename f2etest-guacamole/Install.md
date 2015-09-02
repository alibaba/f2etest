1. 增加yum源

	[http://mirrors.aliyun.com/help/centos](http://mirrors.aliyun.com/help/centos)

	注：选择正确的系统版本号下载
	
		yum clean all
		yum clean metadata
		yum clean dbcache
		yum makecache
	
2. 安装java

	下载：[http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880260.html](http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880260.html)
		
	> mkdir /usr/java

	> cd /usr/java

	> wget http://download.oracle.com/otn-pub/java/jdk/7u79-b15/jdk-7u79-linux-x64.tar.gz?AuthParam=1440926059_deb5f2aa485976bed6105e9a5b2fbf42
		
	> tar -zxvf jdk-7u75-linux-x64.gz
		
	> vi /etc/profile
		
		JAVA_HOME=/usr/java/jdk1.7.0_75
		CLASSPATH=.:$JAVA_HOME/lib.tools.jar
		PATH=$JAVA_HOME/bin:$PATH
		export JAVA_HOME CLASSPATH PATH
			
	> source /etc/profile
		
	> java -version
	

3. 安装tomcat7

	下载：[http://tomcat.apache.org/download-70.cgi](http://tomcat.apache.org/download-70.cgi)
		
	> cd /usr/local/
	
	> wget http://mirrors.hust.edu.cn/apache/tomcat/tomcat-7/v7.0.64/bin/apache-tomcat-7.0.64.tar.gz

	> tar -xzvf apache-tomcat-7.0.64.tar.gz
		
	> mv apache-tomcat-7.0.64 tomcat7
		
	> cd /etc/init.d 
		
	> vi tomcat7
		
		#!/bin/bash
		# description: Tomcat Start Stop Restart
		# processname: tomcat
		# chkconfig: 234 20 80
		JAVA_HOME=/usr/java/jdk1.7.0_75
		export JAVA_HOME
		PATH=$JAVA_HOME/bin:$PATH
		export PATH
		CATALINA_HOME=/usr/local/tomcat7

		case $1 in
		start)
		sh $CATALINA_HOME/bin/startup.sh
		;; 
		stop)   
		sh $CATALINA_HOME/bin/shutdown.sh
		;; 
		restart)
		sh $CATALINA_HOME/bin/shutdown.sh
		sh $CATALINA_HOME/bin/startup.sh
		;; 
		esac    
		exit 0
		
	> chmod 755 tomcat7

	> chkconfig --add tomcat7

	> chkconfig --level 234 tomcat7 on

	> chkconfig --list tomcat7	
		
	> service tomcat7 start
	
4. 安装guacamole-server

	> yum install cairo-devel libpng-devel uuid-devel freerdp* libvncserver-devel openssl-devel

	> ln -s /usr/local/lib/freerdp/guacsnd.so /usr/lib64/freerdp/
		
	> tar -xzf guacamole-server-0.9.3.tar.gz

	> cd guacamole-server-0.9.3

	> ./configure --with-init-dir=/etc/init.d

	> make

	> make install

	> ldconfig
		
	> chkconfig --add guacd

	> chkconfig guacd on

	> chkconfig --list guacd

	> service guacd start

	> netstat -panl | grep guacd

	详细请参考官方安装手册：[http://guac-dev.org/doc/gug/installing-guacamole.html](http://guac-dev.org/doc/gug/installing-guacamole.html)
	

5. 安装guacamole-client

	> cp guacamole-0.9.3.war /usr/local/tomcat7/webapps/guacamole.war

	> mkdir /etc/guacamole

	> mkdir /root/.guacamole

	> vi /etc/guacamole/guacamole.properties
			
		# Hostname and port of guacamole proxy
		guacd-hostname: localhost
		guacd-port:     4822
		enable-websocket: true
		enable-clipboard-integration: true

		auth-provider: net.sourceforge.guacamole.net.auth.noauth.NoAuthenticationProvider
		noauth-config: /etc/guacamole/noauth-config.xml

		# auth-provider: net.sourceforge.guacamole.net.basic.BasicFileAuthenticationProvider
		# basic-user-mapping: /etc/guacamole/user-mapping.xml

	> ln -s /etc/guacamole/guacamole.properties /root/.guacamole
	
	> vi /etc/guacamole/noauth-config.xml
	
		<configs>
		    <config name="f2etest-ie6" protocol="rdp">
		        <param name="hostname" value="10.0.0.1" />
		        <param name="port" value="3389" />
		        <param name="enable-drive" value="true" />
		        <param name="drive-path" value="/home/guacdshare" />
		    </config>
		    <config name="f2etest-ie7" protocol="rdp">
		        <param name="hostname" value="10.0.0.2" />
		        <param name="port" value="3389" />
		        <param name="enable-drive" value="true" />
		        <param name="drive-path" value="/home/guacdshare" />
		    </config>
		    <config name="f2etest-ie8" protocol="rdp">
		        <param name="hostname" value="10.0.0.3" />
		        <param name="port" value="3389" />
		        <param name="enable-drive" value="true" />
		        <param name="drive-path" value="/home/guacdshare" />
		    </config>
		    <config name="f2etest-ie9" protocol="rdp">
		        <param name="hostname" value="10.0.0.4" />
		        <param name="port" value="3389" />
		        <param name="enable-drive" value="true" />
		        <param name="drive-path" value="/home/guacdshare" />
		    </config>
		    <config name="f2etest-ie10" protocol="rdp">
		        <param name="hostname" value="10.0.0.5" />
		        <param name="port" value="3389" />
		        <param name="enable-drive" value="true" />
		        <param name="drive-path" value="/home/guacdshare" />
		    </config>
		    <config name="f2etest-ie11" protocol="rdp">
		        <param name="hostname" value="10.0.0.6" />
		        <param name="port" value="3389" />
		        <param name="enable-drive" value="true" />
		        <param name="drive-path" value="/home/guacdshare" />
		    </config>
		</configs>
	
	> service tomcat7 restart