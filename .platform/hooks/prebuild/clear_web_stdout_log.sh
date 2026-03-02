#!/bin/bash
#aws event bridge 스케줄러로 cloudwatch logs에서 s3로 매일 백업해두니 어제 로그는 지워도 됨 (ec2 스토리지 줄이기)
#cloudwatch logs를 2일간 보관하고 어제 로그를 백업하고 지우면 안전
#참고로 cloudwatch logs는 ec2 로그를 카피하는 것이다.

# prebuild에서 실행될 스크립트
LOG_FILE="/var/log/web.stdout.log"

# 어제 날짜 계산 (Feb 19 형식)
YESTERDAY=$(date -d "yesterday" "+%b %e")

# 로그 백업
cp $LOG_FILE "${LOG_FILE}.bak"

# 어제 로그만 삭제
sed -i "/^$YESTERDAY/d" $LOG_FILE

# cron 등록 (root 권한 필요)
echo "0 1 * * * root /home/ec2-user/.platform/hooks/prebuild/01_clear_logs.sh" > /etc/cron.d/clear_yesterday_logs
chmod 644 /etc/cron.d/clear_yesterday_logs