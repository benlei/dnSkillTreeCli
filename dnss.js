//======================================
// Pak/DNT related extractor/compiler
//======================================
// This file requires the following ENV vars to be set in order for it to work:
// - DN_LEVEL_CAP - The level cap
// - DN_OUT_DIR - The directory where the JSON files will be written to
// - DN_UISTRING_PATH - The location of the uistring.xml file
//======================================

var JSystem = Java.type("java.lang.System")
var JString = Java.type("java.lang.String")
var JInteger = Java.type("java.lang.Integer")
var JFile = Java.type("java.io.File")
var JFileOutputStream = Java.type("java.io.FileOutputStream")
var JDocumentBuilderFactory = Java.type("javax.xml.parsers.DocumentBuilderFactory")

//======================================
// Pak object filter
//======================================
var regExps = {
    dnt: /\\(skilltable_character.*|skillleveltable_character.*|skilltreetable|jobtable|playerleveltable|itemtable|weapontable)\.dnt$/i,
    jobicon: /^\\resource\\ui\\mainbar\\jobicon.*/i,
    skillicon: /^\\resource\\ui\\mainbar\\skillicon.*/i,
    uistring: /^\\resource\\uistring\\uistring.xml$/i,
    version: /version.cfg$/i,
    skilltree: /^\\resource\\ui\\skill\\.*\.dds/i,
}

var filter = function(node) {
    for (i in regExps) {
        if (regExps[i].test(node.path) && node.size != 0) {
            return true
        }
    }

    return false
}


//======================================
// DNT compiling
//======================================
var LEVEL_CAP
var JSON_OUTPUT_DIR
var UISTRING_PATH
var skills = []
var skillLevels = []
var jobs = []
var playerLevels = []
var skillTree = []
var items = []
var weapons = []
var accumulate = function(entries, cols, file) {
    var name = file.getName()
    if (name.startsWith("skilltable")) {
        skills = skills.concat(entries)
    } else if (name.startsWith("skilltreetable")) {
        skillTree = skillTree.concat(entries)
    } else if (name.startsWith("skillleveltable")) {
        skillLevels = skillLevels.concat(entries);
    } else if (name.startsWith("jobtable")) {
        jobs = jobs.concat(entries)
    } else if (name.startsWith("playerleveltable")) {
        playerLevels = playerLevels.concat(entries)
    } else if (name.startsWith("itemtable")) {
        items = items.concat(entries)
    } else if (name.startsWith("weapontable")) {
        weapons = weapons.concat(entries)
    }
}

var compile = function() {
    LEVEL_CAP = parseInt(JSystem.getenv("DN_LEVEL_CAP"))
    JSON_OUTPUT_DIR = JSystem.getenv("DN_OUT_DIR")
    UISTRING_PATH = JSystem.getenv("DN_UISTRING_PATH")

    //================================================
    // Setup the UI String
    //================================================
    var uistring = []
    var uistringFile = new JFile(UISTRING_PATH)
    var document = JDocumentBuilderFactory.newInstance().newDocumentBuilder().parse(uistringFile)
    document.getDocumentElement().normalize()
    var nodes = document.getElementsByTagName("message")
    for (var i = 0; i < nodes.getLength(); i++) {
        var e = nodes.item(i)
        uistring[parseInt(e.getAttribute("mid"))] = e.getFirstChild().getData()
    }

    // the backend db
    var db = {Jobs: {}, Lookup: {}, JobTree: []}

    //================================================
    // generate the job info, skill tree, and skills
    //================================================
    jobs.filter(function(job) job.Service).forEach(function(job) {
        // fix a few things
        job.MaxSPJob1 = Number(job.MaxSPJob1.toFixed(3))
        job.EnglishName = job.EnglishName.toLowerCase()

        // init the db skilltree for this job
        db.Jobs[job.PrimaryID] = {EnglishName: job.EnglishName, SkillTree: [], LookupSet: []}

        var json = {JobID: job.PrimaryID}

        // primary class
        if (job.JobNumber == 2) {
            var job1 = jobs.filter(function(j) j.PrimaryID == job.ParentJob)[0]
            db.Jobs[job.PrimaryID].Line = [job1.ParentJob, job.ParentJob, job.PrimaryID]
        }


        // setup skill table
        jobSkills = skills.filter(function(s) s.NeedJob == job.PrimaryID)
        jobSkillsID = jobSkills.map(function(s) s.PrimaryID)
        jobSkillTree = skillTree.filter(function(t) jobSkillsID.indexOf(t.SkillTableID) > -1)
        jobSkillTreeIDs = jobSkillTree.map(function(t) t.SkillTableID)
        jobSkillTree.filter(function(t) jobSkillsID.indexOf(t.SkillTableID) > -1).forEach(function(t) {
            db.Jobs[job.PrimaryID].SkillTree[t.TreeSlotIndex] = t.SkillTableID

            // setup initial Skills with job sp req
            json[t.SkillTableID] = {
                NeedSP: [t.NeedBasicSP1, t.NeedFirstSP1, t.NeedSecondSP1]
            }

            var skill = json[t.SkillTableID]

            // setup the parent job hash
            if (t.ParentSkillID1 > 0) {
                skill.ParentSkills = {}
                skill.ParentSkills[t.ParentSkillID1] = t.NeedParentSkillLevel1
            }

            if (t.ParentSkillID2 > 0) {
                skill.ParentSkills[t.ParentSkillID2] = t.NeedParentSkillLevel2
            }

            if (t.ParentSkillID3 > 0) {
                skill.ParentSkills[t.ParentSkillID3] = t.NeedParentSkillLevel3
            }
        })

        // ensure sizes are always 24
        if (db.Jobs[job.PrimaryID].SkillTree.length < 24) {
            for (var i = db.Jobs[job.PrimaryID].SkillTree.length; i < 24; i++) {
                db.Jobs[job.PrimaryID].SkillTree.push(null)
            }
        }

        var newSkillTree = []
        for (var i = 0; i < db.Jobs[job.PrimaryID].SkillTree.length; i += 4) {
            newSkillTree.push(db.Jobs[job.PrimaryID].SkillTree.slice(i, i + 4))
        }

        db.Jobs[job.PrimaryID].SkillTree = newSkillTree

        // setup skill levels
        jobSkills.filter(function(s) jobSkillTreeIDs.indexOf(s.PrimaryID) > -1).forEach(function(s) {
            var levels = skillLevels.filter(function(l) l.SkillIndex == s.PrimaryID)
            var skill = json[s.PrimaryID]
            skill.NameID = s.NameID
            skill.MaxLevel = s.MaxLevel
            skill.SPMaxLevel = s.SPMaxLevel
            skill.SkillType = s.SkillType
            skill.Levels = {}

            // sprite stuff
            skill.Sprite = JString.format("%1$02d", new JInteger(parseInt(s.IconImageIndex / 200) + 1))
            skill.IconRow = parseInt((s.IconImageIndex % 200) / 10)
            skill.IconCol = s.IconImageIndex % 10

            db.Lookup[s.NameID] = uistring[s.NameID]
            db.Jobs[job.PrimaryID].LookupSet.push(s.NameID)

            // BaseSkillID is when two skills can't be set at same time
            if (s.BaseSkillID > 0) {
                skill.BaseSkillID = s.BaseSkillID
            }

            // weapons can be uncommon + order doesn't matter
            if (s.NeedWeaponType1 > -1 || s.NeedWeaponType2 > -1) {
                skill.NeedWeaponType = []
                if (s.NeedWeaponType1 > -1) {
                    skill.NeedWeaponType.push(s.NeedWeaponType1)
                }

                if (s.NeedWeaponType2 > -1) {
                    skill.NeedWeaponType.push(s.NeedWeaponType2)
                }
            }

            // PvE
            levels.filter(function(l) l.SkillLevel > 0 && l.SkillLevel <= s.MaxLevel).forEach(function(l) {
                if (! skill.Levels[l.SkillLevel]) {
                    skill.Levels[l.SkillLevel] = {}
                }

                level = skill.Levels[l.SkillLevel]
                var applyType = {
                    DelayTime: l.DelayTime, // cooldown
                    DecreaseSP: l.DecreaseSP, // really is MP...
                    SkillExplanationID: l.SkillExplanationID,
                    SkillExplanationIDParam: l.SkillExplanationIDParam,
                }

                if (! level.ApplyType)  {
                    level.ApplyType = []
                }

                if (l.ApplyType == 0) { // PvE
                    level.LevelLimit = l.LevelLimit // required level
                    level.SkillPoint = l.NeedSkillPoint
                    level.ApplyType[0] = applyType
                } else { // PvP
                    level.ApplyType[1] = applyType
                }

                // add uistring
                db.Lookup[l.SkillExplanationID] = uistring[l.SkillExplanationID]
                if (db.Jobs[job.PrimaryID].LookupSet.indexOf(l.SkillExplanationID) == -1) {
                    db.Jobs[job.PrimaryID].LookupSet.push(l.SkillExplanationID)
                }
                if (l.SkillExplanationIDParam) {
                    l.SkillExplanationIDParam.split(",").forEach(function(param) {
                        if (param.startsWith("{") && param.endsWith("}")) {
                            var uistringID = parseInt(param.substring(1, param.length - 1))
                            if (db.Jobs[job.PrimaryID].LookupSet.indexOf(uistringID) == -1) {
                                db.Jobs[job.PrimaryID].LookupSet.push(uistringID)
                            }
                            db.Lookup[uistringID] = uistring[uistringID]
                        }
                    })
                }
            })
        })

        write(job.EnglishName, json)
    })

    //================================================
    // get the map of all jobs
    //================================================
    jobs.filter(function(job) job.Service).forEach(function(job) {
        db.Jobs[job.PrimaryID].ParentJob = job.ParentJob
        db.Jobs[job.PrimaryID].JobNumber = job.JobNumber
        db.Jobs[job.PrimaryID].JobName = uistring[job.JobName]
        db.Jobs[job.PrimaryID].IconRow = parseInt(job.JobIcon / 9)
        db.Jobs[job.PrimaryID].IconCol = job.JobIcon % 9

        if (job.JobNumber == 2) { // can reassign multiple times, not a big deal
            db.SP = [job.MaxSPJob0, job.MaxSPJob1, job.MaxSPJob2]
        }
    })


    //================================================
    // get the player levels
    //================================================
    db.Levels = playerLevels.filter(function(p) p.PrimaryID <= LEVEL_CAP).map(function(p) p.SkillPoint)


    //================================================
    // get the weapons
    //================================================
    var weaponTypeNameIDs = {}
    items.filter(function(i) i.NameID == 1000006853 && i.LevelLimit == 1)
        .reduce(function(p,c) {
            var find = p.filter(function(_p) _p.NameIDParam == c.NameIDParam)
            if (find.length == 0) {
                p.push(c)
            }
            return p
        }, [])
        .forEach(function(i) {
            var weapon = weapons.filter(function(w) w.PrimaryID == i.PrimaryID)[0]
            weaponTypeNameIDs[weapon.EquipType] = uistring[i.NameIDParam.substring(1, i.NameIDParam.length - 1)]
        })

    db.Weapons = weaponTypeNameIDs


    //================================================
    // setup the job tree
    //================================================
    jobs.filter(function(job) job.Service && job.JobNumber == 0).forEach(function(job) {
        db.JobTree.push({ID: job.PrimaryID, Advancements: []})
    });

    jobs.filter(function(job) job.Service && job.JobNumber == 1).forEach(function(job) {
        db.JobTree.filter(function(j) j.ID == job.ParentJob)[0].Advancements.push({ID: job.PrimaryID, Advancements: []})
    });


    jobs.filter(function(job) job.Service && job.JobNumber == 2).forEach(function(job) {
        db.JobTree.forEach(function(j) {
            var adv = j.Advancements.filter(function(j2) j2.ID == job.ParentJob)
             adv[0] && adv[0].Advancements.push({ID: job.PrimaryID})
        })
    });

    write("db", db)
}

var write = function(path, json) {
    var out = new JFileOutputStream(new JFile(JSON_OUTPUT_DIR, path + ".json"))
    out.write(JSON.stringify(json).getBytes("UTF-8"))
    out.close()
}
