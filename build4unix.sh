#!/bin/bash
_this_dir=$(dirname $0)
mkdir "${_this_dir}/target"
(echo "#!/usr/bin/env java -jar"; cat "${_this_dir}"/target/tlscurl-*.jar) > "${_this_dir}/target/tlscurl"
cd "${_this_dir}/target"
chmod +x "tlscurl"
tar -zcvf "tlscurl_unix.tar.gz" "tlscurl"
