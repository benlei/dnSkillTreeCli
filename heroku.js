//======================================
// Pak object filter
//======================================
var regExps = {
    dnt: /\\(skilltable_character.*|skillleveltable_character.+|skilltreetable|jobtable|playerleveltable)\.dnt$/i,
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
var skills = []
var skillLevels = []
var jobs = []
var playerLevels = []
var skillTree = []
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
    }
}

var compile = function() { // DuplicatedSkillType = for checking if in same 'group'
    var uistring = []
    var uistringFile = new java.io.File("D:\\resource\\uistring\\uistring.xml")
    var document = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(uistringFile)
    document.getDocumentElement().normalize()

    var nodes = document.getElementsByTagName("message")
    for (var i = 0; i < nodes.getLength(); i++) {
        var e = nodes.item(i)
        uistring[parseInt(e.getAttribute("mid"))] = e.getFirstChild().getData()
    }

    jsons = []
    jobs.filter(function(job) job.Service).forEach(function(job) {
        // fix a few things
        job.MaxSPJob1 = Number(job.MaxSPJob1.toFixed(3))
        job.EnglishName = job.EnglishName.toLowerCase()

        json = {
            EnglishName: job.EnglishName,
            JobName: uistring[job.JobName],
            JobIcon: job.JobIcon,
            SkillTree: [],
            Skills: {},
        }

        // primary class
        if (job.JobNumber == 2) {
            var job1 = jobs.filter(function(j) j.ID == job.ParentJob)[0]
            var job0 = jobs.filter(function(j) j.ID == job1.ParentJob)[0]
            json.Set = [
                job0.EnglishName.toLowerCase(),
                job1.EnglishName.toLowerCase(),
                job.EnglishName,
            ]

            json.MaxSPJob = [
                job.MaxSPJob0,
                job.MaxSPJob1,
                job.MaxSPJob2,
            ]
        }


        // setup skill table
        jobSkills = skills.filter(function(s) s.NeedJob == job.ID)
        jobSkillsID = jobSkills.map(function(s) s.ID)
        skillTree.filter(function(t) jobSkillsID.indexOf(t.SkillTableID) > -1).forEach(function(t) {
            json.SkillTree[t.TreeSlotIndex] = t.SkillTableID

            // setup initial Skills with job sp req
            json.Skills[t.SkillTableID] = {
                NeedSP: [t.NeedBasicSP1, t.NeedFirstSP1, t.NeedSecondSP1]
            }

            var skill = json.Skills[t.SkillTableID]

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

        // setup skill levels
        jobSkills.forEach(function(s) {
            var levels = skillLevels.filter(function(l) jobSkillsID.indexOf(l.SkillIndex))
            var skill = json.Skills[s.ID]
            skill.Name = uistring[s.NameID]
            skill.MaxLevel = s.MaxLevel
            skill.SPMaxLevel = s.SPMaxLevel
            skill.IconImageIndex = s.IconImageIndex
            skill.SkillType = s.SkillType
            skill.Levels = []

            // SkillDuplicate is uncommon
            if (s.SkillDuplicate > 0) {
                skill.SkillDuplicate = s.SkillDuplicate
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
            levels.filter(function(l) l.ApplyType == 0 && l.LevelLimit <= s.MaxLevel).forEach(function(l) {

            })

            // PvP
            levels.filter(function(l) l.ApplyType != 0 && l.LevelLimit <= s.MaxLevel).forEach(function(l) {

            })
        })

        // setup levels
        print(JSON.stringify(json, null, 2))

        exit()

        jsons.push(json)
    })




    // example of outputting utf8 bytes
//    out = new java.io.FileOutputStream("D:\\test.txt")
//    out.write(uistring[85].getBytes("UTF-8"))
//    out.close()
}

//load("heroku_funcs.js");