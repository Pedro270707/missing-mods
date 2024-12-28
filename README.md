## Missing Mods
This mod allows you to include mods to download if they are missing. Useful if your mod hosting platform (CurseForge/Modrinth) does not have the mods, but you want to use them in your modpack without breaking the mod's license.

### How to add a missing mod
Run your modpack once. This file will be generated under `config/missing-mods.json`:

```json
{
  "optional": [],
  "required": []
}
```

These are JSON arrays. You can add mods to the arrays, with each mod being a JSON object (`{}`). Here are the properties of each specified mod:

| Property key     | Type                                                  | Description                                                                                                        |
|------------------|-------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------|
| `id`             | String                                                | Required. The ID of the mod.                                                                                       |
| `link`           | String                                                | Required. A download link of the mod.                                                                              |
| `valid_versions` | String                                                | Optional. The accepted versions of the mod. Defaults to `*` (any version).                                         |
| `environment`    | String                                                | Optional. The environment which the mod is to be installed in. Can be `*`, `client`, or `server`. Defaults to `*`. |
| `reason`         | [Text](https://minecraft.wiki/w/Raw_JSON_text_format) | Optional. A reason for the mod not to be included in the modpack by default.                                       |

Here's an example with two mods:

```json
{
  "optional": [
    {
      "id": "deeperdarker",
      "link": "https://www.curseforge.com/minecraft/mc-mods/deeperdarker/files/6007575",
      "valid_versions": "1.3.3",
      "environment": "*",
      "reason": {
        "text": "Deeper and Darker is a cool mod."
      }
    }
  ],
  "required": [
    {
      "id": "ftbquests",
      "link": "https://www.curseforge.com/minecraft/mc-mods/ftb-quests-fabric/files/5816793",
      "valid_versions": "2001.4.9",
      "environment": "server",
      "reason": {
        "text": "FTB Quests is unavailable on Modrinth."
      }
    }
  ]
}
```