focssy
======

Forge Client-Server Synchronizer mod for minecraft.

Shoud be used with [focssyAssasin](https://github.com/Noxilie/focssyAssasin)

Place focssyAssasin.jar into focssy.zip

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
|-bmodlist.txt
|-umodlist.txt
|-somodlist.txt
|-comodlist.txt
```
mods - directory that contains all your current client mods

config.zip - archive of your minecraft/config directory

modlist.txt - list of all your client mods that every client should have

bmodlist.txt - list of "bad" mods (that don't have mcmod.info)

umodlist.txt - list of unwanted mod-ids(if client have it, it will be deleted)

somodlist.txt - list of ids of server-only mods

comodlist.txt - list of client-only mods

2) modlist.txt syntax - modId,modVersion,modFileName separated by three whitespace characters. One row - one mod.
(generated automatically by the server-side mod)
```
somemod1   0.1   someMod1_v0.1.jar
somemod2   0.6   someMod2_v0.6.zip
somemod3   0.2   someMod3_v0.2.jar
```

3) bmodlist.txt syntax - modId,modName (part of modFileName that you don't expect to change) separated by three whitespace characters. One row - one mod.
So, if your modFileName is something like this: mygreatsupermod_ver0.0.0.3.zip and it's id is "greatsupermod"
You should have something like this in your bmodlist.txt:
```
greatsupermod   mygreatsupermod
```
4) umodlist.txt and somodlist.txt syntax - just a lists of ids. One row - one id.
```
somemod4
somemod5
```
5) comodlist.txt - same as modlist.txt
