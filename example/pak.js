var REGEX = {
    dnt: /\\(skilltable_character.*|skillleveltable_character.*|skilltreetable|jobtable|playerleveltable|itemtable.*|glyphskilltable|weapontable)\.dnt$/i,
    jobicon: /^\\resource\\ui\\mainbar\\jobicon.*/i,
    skillicon: /^\\resource\\ui\\mainbar\\skillicon.*/i,
    uistring: /^\\resource\\uistring\\uistring\.xml$/i,
    uitemplatetexture: /uit_gesturebutton\.dds/i,
    skilltree: /^\\resource\\ui\\skill\\.*\.dds/i
};

// var REGEX = {
//     dnt: /\.dnt$/i,
//     ui: /^\\resource\\ui\\.*/i,
//     uistring: /^\\resource\\uistring\\uistring\.xml$/i,
//     uitemplatetexture: /uit_gesturebutton\.dds/i
// };

var filter = function (pakFile) {
    for (var key in REGEX) {
        if (REGEX[key].test(pakFile.getPath()) &&
            pakFile.getCompressedSize() != 0 &&
            pakFile.getSize() != 0) {
            return true;
        }
    }

    return false;
};