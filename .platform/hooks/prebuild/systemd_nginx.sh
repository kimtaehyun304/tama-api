#!/bin/bash
# EB 배포 시 systemd override 자동 적용

OVERRIDE_DIR=/etc/systemd/system/nginx.service.d
mkdir -p $OVERRIDE_DIR

cat <<EOF > $OVERRIDE_DIR/override.conf
[Service]
LimitNOFILE=200000
LimitNOFILESoft=200000
EOF

sudo systemctl daemon-reexec
sudo systemctl restart nginx