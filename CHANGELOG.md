## [1.4.1](https://github.com/raidcraft/rcskills/compare/v1.4.0...v1.4.1) (2020-12-21)


### Bug Fixes

* **api:** return PlayerSkill in activate() and deactivate() methods ([6bc7486](https://github.com/raidcraft/rcskills/commit/6bc74867e0b1013e3126f3aeb5d7f1136f2c8abe))

# [1.4.0](https://github.com/raidcraft/rcskills/compare/v1.3.0...v1.4.0) (2020-12-21)


### Features

* **api:** add additional methods to create and load skills from a class ([e0e256d](https://github.com/raidcraft/rcskills/commit/e0e256d3a17fd3bfa79624193ef558a886811368))

# [1.3.0](https://github.com/raidcraft/rcskills/compare/v1.2.0...v1.3.0) (2020-12-20)


### Bug Fixes

* reload skills when /rcsa reload is called ([725b09f](https://github.com/raidcraft/rcskills/commit/725b09f1adef8d3d14190847b2d4ee95f82fdd8d))
* unload player skills when server shuts down ([d8e999b](https://github.com/raidcraft/rcskills/commit/d8e999b9fbcf2c81cd47986406ea092706e76154))


### Features

* skills can now depend on plugins ([a78db31](https://github.com/raidcraft/rcskills/commit/a78db313731bcd03f753cb337a5b043ca3bd82f1))

# [1.2.0](https://github.com/raidcraft/rcskills/compare/v1.1.1...v1.2.0) (2020-12-20)


### Bug Fixes

* refresh playerskill from database when fetched from skill context ([086b558](https://github.com/raidcraft/rcskills/commit/086b558fc139ad9eed76f4c50911b3f2b06de5a9))


### Features

* activate skill directly if it needs no skill slots ([e731f78](https://github.com/raidcraft/rcskills/commit/e731f78f0f45d124ae33c6820848e765b176501d))
* display details about exp gain ([135836c](https://github.com/raidcraft/rcskills/commit/135836c90c960d3b281c4a7ecf126a386bef0e2b))
* load skill modules from sub directory ([dbd4b08](https://github.com/raidcraft/rcskills/commit/dbd4b083422c350063dc356922c4a5df9cf02110))

## [1.1.1](https://github.com/raidcraft/rcskills/compare/v1.1.0...v1.1.1) (2020-12-20)


### Bug Fixes

* expose ebeanwrapper as api ([f5af42d](https://github.com/raidcraft/rcskills/commit/f5af42dd148f0262527763f4a8d609c9dc3c5f35))

# [1.1.0](https://github.com/raidcraft/rcskills/compare/v1.0.0...v1.1.0) (2020-12-20)


### Bug Fixes

* save skill state before activating it ([ff48f83](https://github.com/raidcraft/rcskills/commit/ff48f839ffed6e932d20ed4c7c509025f49b943a))


### Features

* add data store to PlayerSkill ([1622bdb](https://github.com/raidcraft/rcskills/commit/1622bdb073c03e339769a7f92878f2203ea4c4c3))
* add exp online time skill ([7b997d7](https://github.com/raidcraft/rcskills/commit/7b997d76a71f0491a06acf5ab21e212eef5fa832))

# 1.0.0 (2020-12-18)


### Bug Fixes

* **build:** do not fail on javadocs errors ([858ad1b](https://github.com/raidcraft/skills/commit/858ad1bdddcbe393cb83a544b169ec42b51223c0))
* **release:** use jdk11 when building with jitpack ([ffc6c31](https://github.com/raidcraft/skills/commit/ffc6c3118fed05074c2264cd6b155760fbff84f5))
* buying of skills now uses the economy wrapper ([5d6e975](https://github.com/raidcraft/skills/commit/5d6e975dc31abd99708d7894ee715445f13c616a))
* calculate free skillslots based on their configured value instead of pure count ([b11cb3e](https://github.com/raidcraft/skills/commit/b11cb3ebd4a2cb4a6de54dd445d2ec62d784b3cf))
* create new database migrations ([efbea28](https://github.com/raidcraft/skills/commit/efbea28b7b05325c79f340ae4eb3f996e34fcd11))
* delay loading of other skill types by 1 tick ([e59c492](https://github.com/raidcraft/skills/commit/e59c4929621fc9c5e557bf2be58fb60f738b995f))
* hide level progress bar after timeout ([bffeac2](https://github.com/raidcraft/skills/commit/bffeac23c982d92914e1ae6fbfdc36b8b8485d7f))
* set default skill status to NOT_PRESENT ([341a01c](https://github.com/raidcraft/skills/commit/341a01c3d44846976d3494431dd7ba0b14318324))
* **cmd:** add extra permission to show player info of others ([22ad811](https://github.com/raidcraft/skills/commit/22ad81187bd73aa05592e3d6923d68d25152c937))
* store ConfiguredSkill data in database ([fea0a36](https://github.com/raidcraft/skills/commit/fea0a36c2a09f0ce044dbe7fc4907fc13ce9f1c2))
* unlock skill directly when added and requirement checks pass ([21f59c7](https://github.com/raidcraft/skills/commit/21f59c72472baf1e54892693211a66ff6b95c4ff))


### Features

* add /rcs buy skill command and fix messages ([89d7b5e](https://github.com/raidcraft/skills/commit/89d7b5e6cfc73e2ee8f22b4f72dc4fa3df381330))
* add admin commands to add and remove skills from players ([4eac45c](https://github.com/raidcraft/skills/commit/4eac45c91c2ee2caddd61f9d712c12506fa75906))
* add buy confirm option when buying skills - improve a lot of the messages ([53579ad](https://github.com/raidcraft/skills/commit/53579ad41a280f3b4df09739be1164e85f0706b2))
* add config to let players gain skillpoints and slots on levelup ([c3c26dc](https://github.com/raidcraft/skills/commit/c3c26dcccb2cf53cdcce034df4c3d5f6ea6dbc7f))
* add database models and basic api ([aa0ca68](https://github.com/raidcraft/skills/commit/aa0ca6853ddffc576e7ccbe1a5fa8f11b7ac1833))
* add default required skill permission based on alias ([bc55156](https://github.com/raidcraft/skills/commit/bc55156b000dcd06f6018fc49ba955e93d690d4e))
* add detailed /rcs command for player information ([edcecc5](https://github.com/raidcraft/skills/commit/edcecc5bf7d8e3fbb29ce3804c3a396a87412be8))
* add first command ([67b7b49](https://github.com/raidcraft/skills/commit/67b7b4936a12892c113e369c57be7bde22d233e8))
* add individual player level table ([8dde16e](https://github.com/raidcraft/skills/commit/8dde16e96f00656f15a3cc78d32ff4c340b77a5f))
* add level requirement type ([a6927e8](https://github.com/raidcraft/skills/commit/a6927e863652c1bf1947d3d874055063f772fa6a))
* add money and skillpoint requirements ([d5a2549](https://github.com/raidcraft/skills/commit/d5a2549949889d7fca39b440d67c3c845d70c597))
* add option to customize requirement name and description per skill ([7e69894](https://github.com/raidcraft/skills/commit/7e6989486b45c5f09428ae21072ee9eb9772b070))
* add required level to skill list ([d1bf066](https://github.com/raidcraft/skills/commit/d1bf06614092987779c8f086f2b14abe18a55bd5))
* add skill activation command ([e0825f8](https://github.com/raidcraft/skills/commit/e0825f84b2b9fe6f5740393df2a299b1e535a33a))
* add skill and requirement types with annotations ([ac3798f](https://github.com/raidcraft/skills/commit/ac3798f4c861c9d6516554e1abe5c51944b7a2d2))
* add skill categories ([bd3d871](https://github.com/raidcraft/skills/commit/bd3d871f04aa77c1e54a91d23996f1a51b41401f))
* add skill money and skillpoint cost ([545a911](https://github.com/raidcraft/skills/commit/545a911392a0aaa537285e1bc76718504151d594))
* add SkillFactory and automatic scanning of all plugins for registered skills ([4fb4596](https://github.com/raidcraft/skills/commit/4fb4596fd4c56f3b141c18425e6e91efc7e04992))
* add skillslots to limit the number of active skills ([137067c](https://github.com/raidcraft/skills/commit/137067c172a195c98330e2199221f7b38b703e52))
* add sound and visual effect when a skill is unlocked ([68829b7](https://github.com/raidcraft/skills/commit/68829b7a2587161818165e38968401e46dc4e126))
* added player history ([ca19e83](https://github.com/raidcraft/skills/commit/ca19e8307daab77b01863d8a873eddaa87cb0d52))
* added skillslots and slot requirements ([267505a](https://github.com/raidcraft/skills/commit/267505ad1fefe97066664bf79d739598fb95d5a6))
* adjust total exp based on player level ([314b5e5](https://github.com/raidcraft/skills/commit/314b5e55273a320fe5e71e4c36df90b9b54dd36e))
* apply skills on player login and remove them on logout ([54e44a9](https://github.com/raidcraft/skills/commit/54e44a933c00c0330ef2bf2ca585eea6ee0903bf))
* broadcast levelup to all players ([51cb0c0](https://github.com/raidcraft/skills/commit/51cb0c0e529b5217c2676d2c197057bf7dd6f7c6))
* finish player skill saving into the database ([c232643](https://github.com/raidcraft/skills/commit/c23264310c97aa01a853a1664271a14d5e4cc628))
* implement leveling and level formula customization ([579ade6](https://github.com/raidcraft/skills/commit/579ade6f699174ecb067be87ed12163f6428ebd7))
* implement skill loading and the PermissionSkill ([79acda5](https://github.com/raidcraft/skills/commit/79acda543b75737d8f08ba34653157f2ccd72927))
* load skill instance per player ([9417381](https://github.com/raidcraft/skills/commit/9417381e63881b612d1af83a605adbc9d2cc4c3e))
* show exp and level progress bar ([036f0bc](https://github.com/raidcraft/skills/commit/036f0bc1a9a18aae90667acf7bbe40dc9ff71b5e))
* show level up title message ([e72bd5f](https://github.com/raidcraft/skills/commit/e72bd5f6002c3944af399e0415c9f398dffdfdb1))
* show skill unlock action title ([85c17bd](https://github.com/raidcraft/skills/commit/85c17bdcea654c403b1f776cbfab16b3b0e60a3b))
* **api:** add events for skill unlock and activate ([dc08ab7](https://github.com/raidcraft/skills/commit/dc08ab70a20abd8d3a377d98f00b81a779c9d9ce))
* **cmd:** add /rcs help command ([c4c1aa1](https://github.com/raidcraft/skills/commit/c4c1aa151196ab0d2a426a3c2dd5acf5bea60e8a))
* **cmd:** add commands for adding exp, level and skillpoints ([27147fd](https://github.com/raidcraft/skills/commit/27147fde2a999ca014a53448f06317c0e3b02231))
* replace active/unlocked with skill status enum for more flexibility ([2e88dc7](https://github.com/raidcraft/skills/commit/2e88dc77aa927f7458e9da67bf68301e66b24ea3))
* store skill configuration in the database ([e0d66b3](https://github.com/raidcraft/skills/commit/e0d66b34242acefb10fc6136da5fe966f9a11a22))
* **cmd:** add add and remove skill command ([b3e6d7e](https://github.com/raidcraft/skills/commit/b3e6d7e6c9a503f533c951204f6353fa0681d4d1))


### BREAKING CHANGES

* implementations of skill now require to extend AbstractSkill or provide an option to the PlayerSkill