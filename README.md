# thrift-rpc
thrift rpc framework，based on spring framework，zookeeper.......tags like dubbo
thrift是以前做过的一个项目要做服务化，后端平台是Java，前端业务层是php，thrift是跨语言的，性能也不错，但thrift只是一个rpc工具，原生的使用起来不太方便，而且服务发现之类的也没有，之前用过dubbo，
于是就模仿dubbo将thrift做了一个封装。可以像dubbo一样发布服务，使用zk作为注册中心。暂时没有类似于dubbo monitor一样的监控，后续又陆续的加上。
如果你的项目是纯Java的，推荐用dubbo，如果你的项目是多语言混战的，而且又不想用http，推荐用thrift或其他一些跨平台的rpc工具。

配置thrift服务
<!-- 配置thrift暴露的服务,下面这几个属性必须有，name可以重复，但是name+version绝对不能重复 -->
	<!-- thrift提供的服务名称，name，一般约定为thrift生成的的service名，如UserRpcService -->
	<thrift:service name="UserRpcService" class="com.wangyn.user.impl.v_1_0.UserRpcServiceImpl" version="1.0" />
其中：
Name为thrif对外暴露的服务名称。
Version 为服务接口版本号，注意服务名称name可以重复，但是name+version绝对不可重复，即一个服务可以对外提供多个版本。
Class为某个版本的服务的具体实现类。

