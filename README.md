![The mod logo.](https://github.com/HRudyPlayZ/MCInstanceLoader/blob/main/logo.png?raw=true)

![GitHub](https://img.shields.io/github/license/HRudyPlayZ/MCInstanceLoader?color=%23389AD5&style=for-the-badge)
[![forthebadge](https://forthebadge.com/images/badges/made-with-java.svg)](https://forthebadge.com)
[![forthebadge](https://forthebadge.com/images/badges/built-with-love.svg)](https://forthebadge.com)

A small mod to allow Minecraft to directly install a modpack in the .mcinstance format, a simplified distribution format for modpacks. It lets people download files, bundle overrides and much more. The game asks for a reboot after installation and disables the mcinstance file to let the game restart properly.

# This is the main branch.
There won't be any code hosted here, only general info. You may want to look at this branch instead:
- [1.7.10](https://github.com/HRudyPlayZ/MCInstanceLoader/tree/1.7.10)

(others might come later on, feel free to help port the mod to any version you'd like. Forge, Fabric, 1.16, 1.3.2, anything, this mod is MIT licensed after all.)

## So basically what is the .mcinstance format?
The `MCInstance` format (`.mcinstance`) consists of a repurposed ZIP file, with a specfic layout inside, and containing things like `.packconfig` files which are very similar to the INI format.
This mod aims to allow its installation inside the game so it can be used as a cross-platform modpack format. Support for it could also be added inside 3rd-party launchers to directly allow people to double click the file and install the pack.

## Features
Here are some features included in both this mod and the format:
- Bundle any file inside the overrides folder, just like other formats. Files there get copied to the minecraft root directory (usually `.minecraft`).
- A carryover folder for modpack players. That way players can put any file in it, and they will get merged as well, overwritting what the pack might have downloaded/bundled before.
- Allows to clear folders (so scripts, logs etc can become empty on each pack update). A security is in place for the `config`,`saves` and `carryover` folders. It cannot clear the `mods` folder as well.
- Download files from the internet (any URL) and save them anywhere in the minecraft folder.
-  API support for both Modrinth and Curseforge (There's also the option to guess the correct download URL for CF, without having to use the API).
- Hash checks for downloaded files (`SHA-512`, `SHA-256`, `SHA-1`, `MD5` or `CRC32` for now)
- StopModReposts checks, to discourage the use of repost websites. I may make this toggleable in the future, haven't decided yet.
- Sided files, so you can only download certain files on server or on the client. Useful for GUI mods and what not.
- A complete log system in place so you can easily check issues specific to this mod. Combined with a verbose option for Forge logs.
- Forge progressbar integration, displays a new progress bar in the loading screen, so people can see how many mods are left to be downloaded.
- Info GUI that replaces the main menu, and can show a list of errors or notify the user of the installation success. This is to make sure that people restart the game and correctly load mods that might've been added.
- Auto-quit timer in the GUI, you can configure the time it takes before the game automatically quits. You can also disable the feature if you prefer.
-  Customisable success message on the GUI.
-  Support for any mod that changes the main menu, there's even the option to add specific class paths just in case.
- Automatic `pack.mcinstance` disabling after success, with the possibility to delete the file, or disable that feature instead. This is necessary to let people play the game after a succesful install.

## So how do i use it?
Check out the wiki pages to learn more about this mod and the mcinstance format.

## Credits & licensing
Licensing: 

Even though this mod is distributed under [MIT](https://github.com/HRudyPlayZ/MCInstanceLoader/blob/main/LICENSE), this license doesn't apply to any file in the `net.lingala.zip4j` package, as this is the source code for the [zip4j](https://github.com/srikanth-lingala/zip4j) library, and only distributed here for ease of use. The zip4j library is licensed under the [Apache License 2.0](https://github.com/srikanth-lingala/zip4j/blob/master/LICENSE) and the version bundled here is the 2.9.1 release.

Credits:
- srikanth-lingala for creating the `zip4j` library, which this mod uses.
- AstroTibs for making `OptionsEnforcer` which i got inspired by.
- Janrupf and HansWasser for creating `ModDirector` which inspired this mod and where a good portion of the web code comes from.
