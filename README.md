focssy
======

Forge Client-Server Synchronizer mod for minecraft.

How it should work (client-side):
1) minecraft/config/focssy.cfg
---> modpackUrl=http://place.your.url.here/

You should place url of your "update server" here, really.

2) When you'll try to connect to some mc-server with mods that you don't have on your client, focssy will try to connect to http://place.your.url.here/ and do all the job for you :)



How it should work (server-side):
1) At your http://place.your.url.here/ you should have something like this:

|-[mods]
|     |----mod1.jar
|     |----mod2.zip
|
|
|-config.zip
|-modlist.txt
|-badmods.txt

mods - directory that contains all your current client mods
config.zip - archive of your minecraft/config directory
modlist.txt - list of all your client mods
badmods.txt - list of mods that don't have mcmod.info

2) modlist.txt syntax - modId,modVersion,modFileName separated by three whitespace characters. One row - one mod.

somemod1   0.1   someMod1_v0.1.jar
somemod2   0.6   someMod2_v0.6.zip
somemod3   0.2   someMod3_v0.2.jar


3) badmods.txt syntax - modName (part of modFileName that you don't expect to change). One row - one name.

So, if your modFileName is something like this: mygreatsupermod_ver0.0.0.3.zip
The modName should be: mygreatsupermod
