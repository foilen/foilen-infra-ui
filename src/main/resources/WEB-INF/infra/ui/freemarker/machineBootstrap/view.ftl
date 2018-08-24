<#include "/common/header.ftl">

<h3>${machineName}</h3>

<pre>
# Install base applications
echo "deb https://dl.bintray.com/foilen/debian stable main" | tee /etc/apt/sources.list.d/foilen.list
apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys 379CE192D401AB61

apt update && \
apt -y dist-upgrade && \
apt -y autoremove

apt install -y haveged docker.io docker-sudo

# Add swap memory (5G in 5 1G files. Useful if you want to easily remove some G later)
for i in {1..5}; do
  SWAP_FILE=/var/swap.$i
  echo Generating $SWAP_FILE
  fallocate -l 1G $SWAP_FILE
  chmod 600 $SWAP_FILE
  /sbin/mkswap $SWAP_FILE
  echo $SWAP_FILE swap swap defaults 0 0 >> /etc/fstab
  /sbin/swapon $SWAP_FILE
done

# Enable SSHD password authentication
if egrep '^PasswordAuthentication no$' /etc/ssh/sshd_config > /dev/null ; then
  echo Enabling SSHD password authentication
  sed 's/^PasswordAuthentication no$/#PasswordAuthentication no/g' /etc/ssh/sshd_config > /etc/ssh/sshd_config.tmp
  mv /etc/ssh/sshd_config.tmp /etc/ssh/sshd_config
  service sshd restart
fi

# Join the cluster
docker run -ti \
  --rm \
  --env HOSTFS=/hostfs/ \
  --env MACHINE_HOSTNAME=${machineName} \
  --hostname ${machineName} \
  --volume $DATA_DIR:/data \
  --volume /etc:/hostfs/etc \
  --volume /home:/hostfs/home \
  --volume /usr/bin/docker:/usr/bin/docker \
  --volume /usr/lib/x86_64-linux-gnu/libltdl.so.7.3.1:/usr/lib/x86_64-linux-gnu/libltdl.so.7 \
  --volume /var/run/docker.sock:/var/run/docker.sock \
  foilen/foilen-infra-bootstrap:latest \
  --join --uiApiBaseUrl ${uiApiBaseUrl} --uiApiUserId ${uiApiUserId} --uiApiUserKey ${uiApiUserKey} --info | tee bootstrap.log
</pre>

<#include "/common/footer.ftl">
