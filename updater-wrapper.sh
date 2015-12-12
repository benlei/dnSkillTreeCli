#/usr/bin/env bash
cd ~maze
sudo -u maze ./updater.sh
if [[ $? -eq 0 ]]; then
  supervisorctl restart maze
fi
