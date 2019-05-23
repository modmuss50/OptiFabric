# OptiFabric

![](https://ss.modmuss50.me/javaw_2019-05-22_20-33-34.jpg)

__Note:__ This project is not related or supported by either Fabric or Optifine.

__Note:__ This project does not contain Optifine, you must download it separately!

## Installing

After installing fabric for 1.14, you will need to place the OptiFabric mod jar as well as the optifine installer in the mods folder.

Fabric Loader should be the latest version from the [Fabric Website](https://fabricmc.net/use/)

If you are using Fabric API version 0.2.7 is recommended, you can download it from [here](https://minecraft.curseforge.com/projects/fabric/files/2702281). Newer (0.3.0+) versions may have issues with shaders, a fix is being worked on.

## Links

### [OptiFabric Downloads](https://minecraft.curseforge.com/projects/optifabric)

### [Optifine Download](https://optifine.net/downloads) (current 1.14 builds are under the preview section)

## Issues

If you happen to find an issue and you believe it is to do with OptiFabric and not Optifine or a mod please open an issue [here](https://github.com/modmuss50/OptiFabric/issues) 


Note Fabric, Optifine and OptiFabric are still all in beta, so things will be broken.

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
