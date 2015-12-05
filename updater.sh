#!/usr/bin/env bash
MAZE_STATIC=/home/maze/maze-static
NPM_ROOT=/maze
export DN_VERSION_PATH=${MAZE_STATIC}/Version.cfg
export DN_LEVEL_CAP=90
export DN_UISTRING_PATH=${MAZE_STATIC}/resource/uistring/uistring.xml

export DN_OUT_DIR=$(mktemp -d)

jjs updater-na.js

if [[ $? -ne 0 ]]; then
  exit 0
fi

dn pak -xf -e maze.js ${DN_OUT_DIR}/*.pak ${MAZE_STATIC}

# clear files
rm -rf ${DN_OUT_DIR}/*


export DN_OUT_DIR=${NPM_ROOT}/public/json

# compile dnt
dn dnt -c maze.js ${MAZE_STATIC}/resource/ext/*.dnt

mv -f ${DN_OUT_DIR}/db.json ${NPM_ROOT}/db.json

# convert skillicons
cd ${MAZE_STATIC}/resource/ui/mainbar
dn dds -fp --png *.dds
mv -f *.png ${NPM_ROOT}/public/images/ui/mainbar/

# convert skill trees
cd ${MAZE_STATIC}/resource/ui/skill
dn dds -fp --png *.dds
mv -f *.png ${NPM_ROOT}/public/images/ui/skill/
