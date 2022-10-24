## AXELOR Docgen 

**Title** : Module Docgen for Axelor

**Version** : 6.3.x (Axelor v6.3.x) - 4.1.x (Axelor v.6.1.x) - 3.1.2 (Axelor v.6.0)

**Description & motivation** : From template word, generate pdf document with using technologie aspose.
you can only use this module with [docgen server](https://git.avrsolutions.fr/avr/docgen)


**Installation instructions** : using with [PMA](https://git.avrsolutions.fr/avr/pma). Reference the git in your project in `pma.config.json` and the version and run pma script. 

example for `pma.config.json`

```json
{
    "axelor_version": "6.3",
    "overwrite": true,
    "keep_git": false,
    "modules": [
        {
            "git": "git.avrsolutions.fr/axelor-module/docgen.git",
            "version": "6.3.1"
        }
    ]
}

```

for more documentation go to [PMA](https://git.avrsolutions.fr/avr/pma)