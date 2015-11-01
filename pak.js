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
            return true;
        }
    }
    return false;
}