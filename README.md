# The future of OptiFabric and Recommended alternatives

In 1.15 and 1.16 optifine has been casuing a lot of incompatibilities with a growing number of mods, this is due to the way optifine changes the vanilla code in increasingly invasive and incompatible ways. Fixing these crashes isn't easy or fun and takes a lot of time and energy that I don't have.

I have no plans to continue updating OptiFabric going forward, thus with the help of LambdAurora I have compiled a list of mods that replace and supersede Optifine:

## [Sodium](https://www.curseforge.com/minecraft/mc-mods/sodium)

[Sodium](https://www.curseforge.com/minecraft/mc-mods/sodium) is a free and open source fabric mod made by JellySquid that [drastically increases the performance](https://www.youtube.com/watch?v=0fAB6pJK6U4) of Minecraft. Sodium utilises modern rendering techniques along with a range of other optimisations.  More more information can be found on the download page linked.

![](https://cdn.discordapp.com/attachments/602805058316533770/731936807909851296/compare.png)


## Other recommended mods

### [Canvas](https://www.curseforge.com/minecraft/mc-mods/canvas-renderer)

[Canvas](https://www.curseforge.com/minecraft/mc-mods/canvas-renderer) focuses the use of shaders to improve the visual appearance of the game along with providing performance improvements. (*Incompatible with Sodium*)

### [Ok Zoomer](https://www.curseforge.com/minecraft/mc-mods/ok-zoomer) - [Logicial Zoom](https://www.curseforge.com/minecraft/mc-mods/logical-zoom) - [WI Zoom](https://www.curseforge.com/minecraft/mc-mods/wi-zoom)

These 3 mods are each a great replacement to optifine's much loved zoom feature. They provide more options than optifine's zoom functionality. (*You only need to install one*)

### [Colormatic](https://www.curseforge.com/minecraft/mc-mods/colormatic)

[Colormatic](https://www.curseforge.com/minecraft/mc-mods/colormatic) provides support for resource packs that use Optifine's custom color features.

### [LambDynamicLights](https://www.curseforge.com/minecraft/mc-mods/lambdynamiclights)

[LambDynamicLights](https://www.curseforge.com/minecraft/mc-mods/lambdynamiclights) add dynamic lights to Minecraft similar to Optifine's.

### [motioNO](https://www.curseforge.com/minecraft/mc-mods/motiono)

[motioNO](https://www.curseforge.com/minecraft/mc-mods/motiono) is a client only mod that prevents Minecraft from changing the FOV when sprinting.

### [Lithium](https://www.curseforge.com/minecraft/mc-mods/lithium) and [Phosphor](https://www.curseforge.com/minecraft/mc-mods/phosphor)

[Lithium](https://www.curseforge.com/minecraft/mc-mods/lithium) provides a great improvement to server performance while [Phosphor](https://www.curseforge.com/minecraft/mc-mods/phosphor) targets lighting performance. Both mods can be installed on the client or the server and are also made by Jelly Squid.


## Need Help?

If you need help installing any of these mods feel free to ask in the [fabric discord server](https://discord.gg/v6v4pMv) in the player-support channel. If you know of any mods that should be showcased here please get in contact with me.
# OptiFabric

![](https://ss.modmuss50.me/javaw_2019-05-22_20-33-34.jpg)

__Note:__ This project is not related or supported by either Fabric or Optifine.

__Note:__ This project does not contain Optifine, you must download it separately!

## Installing

After installing fabric for 1.15.2, you will need to place the OptiFabric mod jar as well as the optifine installer in the mods folder.

Fabric Loader should be the latest version from the [Fabric Website](https://fabricmc.net/use/)

If you need more help you can read a more detailed guide [here](https://github.com/modmuss50/OptiFabric/wiki/Install-Tutorial)


## Links

### [OptiFabric Downloads](https://minecraft.curseforge.com/projects/optifabric)

### [Optifine Download](https://optifine.net/downloads)

## Issues

If you happen to find an issue and you believe it is to do with OptiFabric and not Optifine or a mod please open an issue [here](https://github.com/modmuss50/OptiFabric/issues) 


## For Mod Devs

Add the following to your build.gradle

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    // replace OptiFabric:<version> with latest version on https://www.curseforge.com/minecraft/mc-mods/optifabric/files that fits your MC version
    modCompile 'com.github.modmuss50:OptiFabric:1.0.0-beta8'

    //Deps required for optifabric
    compile 'org.zeroturnaround:zt-zip:1.14'
} 
```

Put the standard Optifine jar in /run/mods

Class export can be enabled using the following VM Option, this will extract the overwritten classes to the .optifine folder, useful for debugging.

`-Doptifabric.extract=true`

## Screenshots

Feel free to open a PR with more screenshots.

![](https://ss.modmuss50.me/javaw_2019-05-22_20-36-25.jpg)

![](https://ss.modmuss50.me/javaw_2019-05-22_19-49-41.jpg)

## How it works

This would not have been possible without Chocohead's [Fabric-ASM](https://github.com/Chocohead/Fabric-ASM).

1. The mod looks for an optifine installer or mod jar in the current mods folder
2. If it finds an installer jar it runs the extract task in its own throwaway classloader.
3. The optifine mod jar is a set of classes that need to replace the ones that minecraft provides.
4. Optifine's replacement classes change the name of some lambada methods, so I take a good guess at the old name (using the original minecraft jar).
5. Remap optifine to intermediary (or yarn in development)
6. Move the patched classes out as they wont do much good on the classpath twice
7. Add optifine to the classpath
8. Register the patching tweaker for every class that needs replacing
9. Replace the target class with the class that was extracted, also do some more fixes to it, and make it public (due to access issues).
10. Hope it works
