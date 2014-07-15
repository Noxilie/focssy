focssy
======

Forge Client-Server Synchronizer mod for minecraft.

How it should work (client-side):
======
1) minecraft/config/focssy.cfg
---> modpackUrl=http://place.your.url.here/

You should place url of your "update server" here, really.

2) At minecraft-startup focssy will try to connect to http://place.your.url.here/ and do all the job for you :)


How it should work (server-side):
======
1) At your http://place.your.url.here/ you should have something like this:
```
|-[mods]
|     |----mod1.jar
|     |----mod2.zip
|
|
|-config.zip
|-modlist.txt
|-umodlist.txt

```
mods - directory that contains all your current client mods

config.zip - archive of your minecraft/config directory

modlist.txt - list of all your client mods that every client should have

umodlist.txt - list of unwanted mod-ids(if client have it, it will be deleted, this extremely usefull when testing modpacks in team)


2) modlist.txt syntax - modId,modVersion,modFileName separated by three whitespace characters. One row - one mod.
(generated automatically by the server-side mod)
```
somemod1   0.1   someMod1_v0.1.jar
somemod2   0.6   someMod2_v0.6.zip
somemod3   0.2   someMod3_v0.2.jar
```
3) umodlist.txt - just a list of ids. One row - one id.
```
somemod4
somemod5
